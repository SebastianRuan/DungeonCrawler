interface Observer{
    fun update()
}


abstract class Enemy(hp: Int, atk: Int,def: Int, sym: Char, pos: Position, board:Board ):
    Creature(hp,atk,def,sym,pos, board), Observer {
    /*
    * Enemy defines the behaviour of the creatures that are attempting to destroy the player
    */

    override fun update() {
        move()
    }

    // getNewLocation randomly selects and returns a nearby tile
    protected fun getNewLocation(): Piece{
        val (row, col) = pos
        val row_change = randGen.nextInt(-1,2)
        val col_change = randGen.nextInt(-1,2)
        return board.getFloorPiece(row + row_change, col + col_change)
    }

    // walk moves the enemy from one tile to the next
    protected fun walk(floorPiece: Tile){
        floorPiece.movePiece( board.getFloorPiece(pos.row,pos.col) as Tile,this)
        pos = floorPiece.position.copy()
    }

    // move find and moves "this" to a new tile if possible.
    open fun move(){
        val floorPiece = getNewLocation()

        if(floorPiece is Tile && floorPiece.toString() != "+" && floorPiece.isEmpty){
            walk(floorPiece)
        }
    }

    override fun attack(creature: Creature) {
        TODO("Not yet implemented")
    }
}

class Vampier(pos:Position, board:Board): Enemy(50, 25, 25, 'V', pos,board){

}

class Werewolf(pos:Position, board:Board): Enemy(120, 30, 5, 'W', pos,board){

}

class Troll(pos:Position, board:Board): Enemy(120, 25, 15, 'T', pos,board){

}

class Goblin(pos:Position, board:Board): Enemy(70, 5, 10, 'N', pos,board){

}

class Merchant(pos:Position, board:Board): Enemy(30, 70, 5, 'M', pos,board){

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

}

class EnemyFactory{
    /*
    * EnemyFactory is responsible for creating the many different kinds of enemies the player fights
    */

    // getRandomEnemy randomly selects an enemy to spawn at position pos on board
    fun getRandomEnemy(pos:Position,board: Board): Enemy{
        val diceRoll = randGen.nextInt(1,19)
        return when(diceRoll){
            in 1..4 -> Werewolf(pos,board)   // probability 4/18
            in 5..7 -> Vampier(pos,board)    // probability 3/18
            in 8..12 -> Goblin(pos,board)    // probability 5/18
            in 13..14 -> Troll(pos,board)    // probability 2/18
            in 15..16 -> Phoenix(pos,board)  // probability 2/18
            else -> Merchant(pos,board)      // probability 2/18
        }

    }
}