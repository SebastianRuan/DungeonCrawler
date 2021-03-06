interface Observer{
    fun update()
}


abstract class Enemy(hp: Int, atk: Int,def: Int, sym: Char, pos: Position, board:Board ):
    Creature(hp,atk,def,sym,pos, board), Observer {
    /*
    * Enemy defines the behaviour of the creatures that are attempting to destroy the player
    */

    override fun update() {
        val player = getPlayerInRange()
        if (player != null) attack() else move()
    }

    // getNewLocation randomly selects and returns a nearby tile
    protected fun getNewLocation(): Piece{
        val (row, col) = pos
        val rowChange = randGen.nextInt(-1,2)
        val colChange = randGen.nextInt(-1,2)
        return board.getFloorPiece(row + rowChange, col + colChange)
    }

    // walk moves the enemy from one tile to the next
    protected fun walk(floorPiece: Tile){
        floorPiece.movePiece( board.getFloorPiece(pos.row,pos.col) as Tile,this)
        pos = floorPiece.position.copy()
    }

    // getPlayerInRange determines if the player is close enough for the enemy to attack and returns the player if so
    protected fun getPlayerInRange(): BasePlayer?{
        val attackRadius = board.oneBlockRadius(board.getFloorPiece(pos.row,pos.col) as Tile)

        // see if player is withing a one block radius
        for (tile in attackRadius){
            if(tile.boardPiece is BasePlayer){
                return tile.boardPiece as BasePlayer
            }
        }
        return null
    }

    // move find and moves "this" to a new tile if possible.
    protected open fun move(){
            val floorPiece = getNewLocation()
            if (floorPiece is Tile && floorPiece.toString() != "+" && floorPiece.isEmpty) {
                walk(floorPiece)
            }
    }

    protected fun rollToHit(): Boolean{
        val diceRoll = randGen.nextInt(1,10)
        if(diceRoll <= 5){                                     // swing and a miss
            board.addMessage("The creature attacked you and ... missed!")
            return false
        }
        return true
    }

    protected open fun attack(): Strike {
        return if(rollToHit())  board.player.damage(this.atk) else Strike(0, false)
    }
    
    // damage determines the damage dealt with the attakers atk value. It also determines if the enemy has died and
    // processes this accordingly
    open fun damage(atk: Int): Strike{
        val damageDealt = takeDamage(atk)
        if (hp <= 0){
            board.addMessage("You did $damageDealt damage and killed the creature!")
            board.player.detach(this)
            return Strike(damageDealt,true)
        }
        board.addMessage("You did $damageDealt damage.")
        return Strike(damageDealt,false)
    }
}

class Vampire(pos:Position, board:Board): Enemy(50, 25, 25, 'V', pos,board){
    override fun attack(): Strike{
        val strike: Strike = super.attack()
        hp += (strike.damageAmt * 0.25).toInt()  // Vampire absorb life from player
        return strike
    }
}

class Werewolf(pos:Position, board:Board): Enemy(120, 30, 5, 'W', pos,board){

}

class Troll(pos:Position, board:Board): Enemy(120, 25, 15, 'T', pos,board){

    override fun move() {
        super.move()
        val diceRoll = randGen.nextInt(1,10)
        if(diceRoll > 5){ // swing and a miss
            hp += 10
        }
    }
}

class Goblin(pos:Position, board:Board): Enemy(70, 5, 10, 'N', pos,board){
    override fun attack(): Strike {
        if (rollToHit()) {
            board.player.loseGold(randGen.nextInt(1, 3)) // steal gold
            try {
                val strike = board.player.damage(atk)
                board.addMessage("The Goblin stole some gold from you.")
                return strike
            } catch (e: GameOver) {
                board.addMessage("The Goblin also stole some gold from your dead body.")
                throw e
            }
        }
        return Strike(0,false)
    }
}

class Merchant(pos:Position, board:Board): Enemy(30, 70, 5, 'M', pos,board){

    companion object{
        var hostile = false
    }

    override fun update() {
        val player = getPlayerInRange()
        if (player != null && hostile) attack() else move()  // only attack if hostile
    }

    override fun damage(atk: Int): Strike {
        hostile = true     // turn hostile when damaged
        return super.damage(atk)
    }
}


class Dragon(pos:Position,private val horde: Tile, board:Board): Enemy(150, 20, 20, 'D', pos,board){
    override fun move(){
        val floorPiece = getNewLocation()

        // don't move away from horde of gold
        if(floorPiece is Tile && floorPiece.toString() != "+" && floorPiece.isEmpty &&
            floorPiece in board.oneBlockRadius(horde)){
            walk(floorPiece)
        }
    }
}

class Phoenix(pos:Position, board:Board): Enemy(50, 35, 20, 'X', pos,board){
    private var reborn = false

    // rebirth restores the phoenix to full health and sends a message to the console
    private fun rebirth(){
        hp = maxHP
        reborn = true
        board.addMessage(
            "You kill the Phoenix. It explodes in a flash of flame. " +
                    "But wait... \nIt seems to be rising from the ashes." +
                    " It is REBORN!!"
        )
        board.player.attach(this)
    }

    override fun damage(atk: Int): Strike {
        val strike = super.damage(atk)
        if(hp <= 0 && randGen.nextInt(1,10) >= 5 && !reborn){ // chance of rebirth
            rebirth()
            return Strike(0, false)
        }
        return strike
    }
}

class EnemyFactory{
    /*
    * EnemyFactory is responsible for creating the many kinds of enemies the player fights
    */

    // getRandomEnemy randomly selects an enemy to spawn at position pos on board
    fun getRandomEnemy(pos:Position,board: Board): Enemy{
        val diceRoll = randGen.nextInt(1,19)
        return when(diceRoll){
            in 1..4 -> Werewolf(pos,board)   // probability 4/18
            in 5..7 -> Vampire(pos,board)    // probability 3/18
            in 8..12 -> Goblin(pos,board)    // probability 5/18
            in 13..14 -> Troll(pos,board)    // probability 2/18
            in 15..16 -> Phoenix(pos,board)  // probability 2/18
            else -> Merchant(pos,board)      // probability 2/18
        }

    }
}