interface Subject{
    val observers: MutableList<Observer>
    fun attach(observer: Observer){
        observers.add(observer)
    }
    fun detach(observer: Observer){
        observers.remove(observer)
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
    var gold = 0
        protected set(value) = if (value < 0) field = 0 else field = value
    override val observers = mutableListOf<Observer>()

    // dirToTile converts string direction (no, so, ea, etc) to a piece in specified direction
    private fun dirToTile(direction:String): Piece{
        // Select tile
        val (row, col) = pos
        return when(direction){
            "no" -> board.getFloorPiece(row-1, col)
            "ne" -> board.getFloorPiece(row-1, col+1)
            "ea" -> board.getFloorPiece(row, col+1)
            "se" -> board.getFloorPiece(row+1, col+1)
            "so" -> board.getFloorPiece(row+1, col)
            "sw" -> board.getFloorPiece(row+1, col-1)
            "we" -> board.getFloorPiece(row, col-1)
            else -> board.getFloorPiece(row-1, col-1)
        }
    }

    open fun move(direction: String){    

        val floorPiece = dirToTile(direction)
        // Move player if possible
        pos = if(floorPiece is Tile && (floorPiece.isEmpty || floorPiece.toString() == "G")){
            if (floorPiece.toString() == "G") {               // Get gold
                gold += (floorPiece.boardPiece as Gold).amt
                floorPiece.clear()
            }
            // move
            floorPiece.movePiece( board.getFloorPiece(pos.row,pos.col) as Tile,this)
            floorPiece.position.copy()
        }  else if (floorPiece.toString() == "\\"){           // When Player moves onto stairs
            observers.clear()
            board.nextLevel()
        } else {
            board.addMessage("Cannot move there.")
            return
        }
        
        notifyAllObservers()
    }

    // attack damages an enemy in direction
    open fun attack(direction: String) {
        val floorPiece = dirToTile(direction)
        if(floorPiece is Tile && floorPiece.boardPiece is Enemy){           // attack successfully targets an enemy
            val playersAttack: Strike = (floorPiece.boardPiece as Enemy).damage(atk)
            if(playersAttack.slain){
                gold += 1
                floorPiece.clear()
            }
            notifyAllObservers()

        } else if (floorPiece is Tile && !floorPiece.isEmpty){              // attack incorrectly targets an item
            val item = when(floorPiece.boardPiece){
                is Potion -> "potions"
                else -> "gold"
            }
            board.addMessage("Cannot attack $item.")
        } else if (floorPiece is Tile) {
            board.addMessage("Cannot attack an empty tile.")
        } else {
            board.addMessage("Cannot attack a wall.")
        }
    }

    fun loseGold(lost: Int){
        gold -= lost
    }
}

class Human(pos: Position, board: Board): Player(140, 20, 20, pos, board) {

}

class Dwarf(pos: Position, board: Board): Player(100, 20, 30, pos, board) {
    private fun doubleGold( superFn: () -> Unit){
        val prevGold = gold
        superFn()
        gold += gold - prevGold // double gold for dwarves
    }
    
    
    override fun move(direction: String) {
        doubleGold{super.move(direction)}
    }

    override fun attack(direction: String) {
        doubleGold{super.attack(direction)}
    }
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

class GameOver(message: String) : Exception(message)