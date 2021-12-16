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
        if (player != null) attack(player) else move()
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
    protected fun getPlayerInRange(): Player?{
        val attackRadius = board.oneBlockRadius(board.getFloorPiece(pos.row,pos.col) as Tile)

        // see if player is withing a one block radius
        for (tile in attackRadius){
            if(tile.boardPiece is Player){
                return tile.boardPiece as Player
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

    protected fun rollToHit(){
        val diceRoll = randGen.nextInt(1,10)
        if(diceRoll <= 5){ // swing and a miss
            throw DamageMsg("The creature attacked you and ... missed!", 0)
        }
    }

    protected open fun attack(player: Player) {
        rollToHit()
        player.damage(this.atk)
    }
}

class Vampire(pos:Position, board:Board): Enemy(50, 25, 25, 'V', pos,board){
    override fun attack(player: Player) {
        try {
            super.attack(player)
        } catch (msg: DamageMsg){
            hp += (msg.damage * 0.25).toInt()  // Vampire absorb life from player
            throw msg
        }
    }
}

class Werewolf(pos:Position, board:Board): Enemy(120, 30, 5, 'W', pos,board){

}

class Troll(pos:Position, board:Board): Enemy(120, 25, 15, 'T', pos,board){

}

class Goblin(pos:Position, board:Board): Enemy(70, 5, 10, 'N', pos,board){
    override fun attack(player: Player) {
        rollToHit()
        player.loseGold(randGen.nextInt(1,3)) // steal gold
        try {
            player.damage(atk)
        } catch (e: GameOver){
            throw GameOver(e.message + "\nThe Goblin also stole some gold from your dead body.")
        } catch (e: DamageMsg){
            throw DamageMsg(e.message + "\nThe Goblin stole some gold from you.", e.damage)
        }
    }
}

class Merchant(pos:Position, board:Board): Enemy(30, 70, 5, 'M', pos,board){

    companion object{
        var hostile = false
    }

    override fun update() {
        val player = getPlayerInRange()
        if (player != null && hostile) attack(player) else move()
    }

    override fun damage(atk: Int) {
        hostile = true
        super.damage(atk)
    }
}


class Dragon(pos:Position,private val horde: Tile, board:Board): Enemy(150, 20, 20, 'D', pos,board){
    override fun move(){
        val floorPiece = getNewLocation()

        if(floorPiece is Tile && floorPiece.toString() != "+" && floorPiece.isEmpty &&
            floorPiece in board.oneBlockRadius(horde)){
            walk(floorPiece)
        }
    }
}

class Phoenix(pos:Position, board:Board): Enemy(50, 35, 20, 'X', pos,board){
    private var reborn = false

    private fun rebirth(){
        hp = maxHP
        reborn = true
        throw DamageMsg(
            "You kill the Phoenix. It explodes in a flash of flame. " +
                    "But wait... \nIt seems to be rising from the ashes." +
                    " It is REBORN!!",
            0
        )
    }


    override fun damage(atk: Int) {
        try {
            super.damage(atk)
        } catch (e: KillMsg){
            val diceRoll = randGen.nextInt(1,10)
            if(diceRoll <= 5 || reborn ){ // swing and a miss
                throw e
            } else {
                rebirth()
            }
        }
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