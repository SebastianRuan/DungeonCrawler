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
        private set
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

    fun move(direction: String){    

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
            throw InvalidMove("Cannot move there.")
        }
        
        notifyAllObservers()
    }

    // attack damages an enemy in direction
    fun attack(direction: String) {
        val floorPiece = dirToTile(direction)
        if(floorPiece is Tile && floorPiece.boardPiece is Enemy){           // attack successfully targets an enemy
            try {
                (floorPiece.boardPiece as Enemy).damage(atk)
            } catch (e: KillMsg){
                gold += 1
                this.detach(floorPiece.boardPiece as Enemy)
                floorPiece.clear()
                throw e
            } catch (enemyDamaged: DamageMsg){
                try {
                    notifyAllObservers()
                }catch (playerDamaged: DamageMsg){
                    throw DamageMsg("${enemyDamaged.message}\n${playerDamaged.message}")
                }catch (DEATH: GameOver){
                    throw GameOver("${enemyDamaged.message}\n${DEATH.message}")
                }
            }

        } else if (floorPiece is Tile && !floorPiece.isEmpty){              // attack incorrectly targets an item
            val item = when(floorPiece.boardPiece){
                is Potion -> "potions"
                else -> "gold"
            }
            throw InvalidMove("Cannot attack $item.")
        } else if (floorPiece is Tile) {
            throw InvalidMove("Cannot attack an empty tile.")
        } else {
            throw InvalidMove("Cannot attack a wall.")
        }
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

open class GameException(message: String): Exception(message)
class InvalidMove(message: String) : GameException(message)
class DamageMsg(message: String) : GameException(message)
class KillMsg(message: String) : GameException(message)
class GameOver(message: String) : GameException(message)