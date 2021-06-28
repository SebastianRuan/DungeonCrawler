import java.io.File

data class Position(val row: Int, val col: Int)

class Chamber{
    /*
    * Camber is defined by the tiles contained within its walls.
    *
    * tiling: a list of floor pieces that make up the chamber
    * */

    private val tiling = mutableListOf<Tile>()

    fun append(tile: Tile){
        tiling.add(tile)
    }

}

class Board{
    /*
    * Board tracks everything. Just like a real board game it knows where the
    * player is and where all the potions are as well as all the enemies.
    *
    * grid: a 2d list representing all the squares on the board
    * chambers: a list of all the rooms that exist on the current floor
    * floor: the level of the dungeon the player is currently on
    */
    private val grid  = mutableListOf<List<Piece>>()
    private val chambers = mutableListOf<Chamber>()
    private var floor = 0

    init {
        File("src\\map.txt").forEachLine {
            val boardRow = it.map {char:Char ->
                                if (char == '.') {
                                    Tile()
                                } else {
                                    Piece(char)
                                }
                            }
            grid.add(boardRow)
        }
        createChambers()
    }

    // isHorizontalWall determines if the piece at i in row is the top or bottom wall of a chamber.
    private fun isHorizontalWall(row:List<Piece>,i:Int, direction: Int):Int{
        var j = i + direction

        while(row[j].toString() == "-" || row[j].toString() == "+"){
            j += direction
        }

        // not a horizontal wall
        if(row[j].toString() == "." ||
            !(row[j].toString() == "|" ||
               row[j].toString() == "+") ||         // corner doors
            row[j+1].toString() == "." ){
            return i
        }

        // horizontal wall
        return j
    }

    /*  scanChamber scans in a newly discovered camber starting at its topmost left hand corner
     *  at position (paramRow, paramCol) in grid. */
    private fun scanChamber(paramRow:Int, parmCol:Int){
        var row = paramRow + 1
        var col = parmCol
        var direction = -1
        val chamber = chambers[chambers.lastIndex]

        // find all of the tiles in the chamber in a back and forth scan
        while(isHorizontalWall(grid[row], col, direction) == col){
            var boardPiece = grid[row][col]
            while((boardPiece.toString() != "|" && boardPiece.toString() != "+") ||
                    grid[row+1][col].toString() == "." ||
                    (direction > 0 && grid[row][col+1].toString() == ".") ||
                    (direction < 0 && grid[row][col-1].toString() == ".")){

                col += direction
                if (boardPiece is Tile){
                    chamber.append(boardPiece)
                }
                boardPiece = grid[row][col]
            }

            row++
            direction *= -1
        }
    }


    // creatChambers scans through the map to locate and creates the chambers.
    private fun createChambers(){
        for (row in 1 until grid.lastIndex) {                      // loop over the board minus the boards border
            var col = 1
            while(col < grid[row].lastIndex) {
                if(grid[row][col].toString() == "|" && grid[row][col-1].toString() != "."){
                    val tmpCol = isHorizontalWall(grid[row], col, 1)
                    if (tmpCol != col && grid[row-1][tmpCol-1].toString() != "."){
                        col = tmpCol
                        chambers.add(Chamber())
                        scanChamber(row,col)
                    }
                }
                col++
            }

        }
    }

    override fun toString(): String {
        var printRow = ""
        for (boardRow in grid){
            for(piece in boardRow){
                printRow += piece.toString()
            }
            printRow += "\n"
        }
        return printRow
    }
}

fun main(){
    val board = Board()
    println(board)

}