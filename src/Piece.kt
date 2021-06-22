open class Piece(private val symbol: Char) {
    override fun toString(): String {
        return symbol.toString()
    }
}

class Tile(symbol: Char): Piece(symbol){
    private val boardPiece: Piece? = null

    override fun toString(): String {
        return boardPiece?.toString() ?: super.toString()
    }
}