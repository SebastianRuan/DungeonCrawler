interface Subject{
    val observers: MutableList<Observer>
    fun attach(observer: Observer){
        observers.add(observer)
    }
    fun detach(observer: Observer){
        //TODO: finish this
//        observers.filter {  }
    }
    fun notifyAllObservers(){
        observers.map { it.update() }
    }
}


abstract class Player(hp: Int,atk: Int, def: Int, position: Position, board: Board):
    Creature(hp, atk, def,'@', position, board), Subject {
    /*
     * Player is the general class for all possible races
     *
     * gold: the amount of money the player has
     */
    val gold = 0
    override val observers = mutableListOf<Observer>()
    

    fun move(direction: String){    
        // Select tile
        val (row, col) = pos
        val floorPiece = when(direction){
            "no" -> board.getFloorPiece(row-1, col)
            "ne" -> board.getFloorPiece(row-1, col+1)
            "ea" -> board.getFloorPiece(row, col+1)
            "se" -> board.getFloorPiece(row+1, col+1)
            "so" -> board.getFloorPiece(row+1, col)
            "sw" -> board.getFloorPiece(row+1, col-1)
            "we" -> board.getFloorPiece(row, col-1)
            else -> board.getFloorPiece(row-1, col-1)   
        }

        // Move player if possible
        pos = if(floorPiece is Tile && floorPiece.isEmpty){
            floorPiece.movePiece( board.getFloorPiece(row,col) as Tile,this)
            floorPiece.position.copy()
        } else if(floorPiece.toString() == "\\"){           // When Player moves onto stairs
            observers.clear()
            board.nextLevel()
        } else {
            throw InvalidMove("Cannot move there.\n")
        }
        
        notifyAllObservers()
    }

    override fun attack(creature: Creature) {
        TODO("Implement when we do combat")
    }

}

class Human(pos: Position, board: Board): Player(140, 20, 20, pos, board) {

}

class Dwarf(pos: Position, board: Board): Player(100, 20, 30, pos, board) {

}

class Elf(pos: Position, board: Board): Player(140, 30, 10, pos, board) {

}

class Orc(pos: Position, board: Board): Player(180, 30, 25, pos, board) {

}

class PlayerFactory{
    /*
    * PlayerFactory has the sole job of generating Player objects depending on what race the user chooses
    */

    fun getPlayer(type: String, pos: Position,  board: Board):Player{
        return when(type){
            "e" -> Elf(pos, board)
            "d" -> Dwarf(pos,board)
            "o" -> Orc(pos, board)
            else -> Human(pos, board)
        }
    }
}

class InvalidMove(message: String) : Exception(message)