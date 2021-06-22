import java.io.File

class Chamber{
    val grid = mutableListOf<Tile>()
}

class Board{
    private val grid  = mutableListOf<List<Piece>>()
    private val chambers = mutableListOf<Chamber>()
    private var floor = 0

    init {
        File("src\\map.txt").forEachLine {
            val boardRow = it.map {char:Char ->
                                if (char != '.') {
                                    Piece(char)
                                } else {
                                    Tile(char)
                                }
                            }
            grid.add(boardRow)
        }
    }

    override fun toString(): String {
        var printRow = ""
        for (boardRow in grid){
            printRow += boardRow.map{it.toString()}.foldRight("\n"){prev,next -> prev+next}
        }
        return printRow
    }
}

fun main(){
    val board = Board()
    println(board)

}