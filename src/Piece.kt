open class Piece(private val symbol: Char) {
    /*
    * Piece is the class that everything inherits from (except for board).
    * A piece object is like a piece in any board game whether it be the
    * player,enemy or an item.
    *
    * symbol: the icon used to display the piece on the screen/board
    */

    override fun toString(): String {
        return symbol.toString()
    }
}

class Tile(): Piece('.'){
    /*
    * Tile is a special piece (board square in this case) that can have
    * another piece on top of it. When this occurs we want to print the
    * piece on top of it rather than the tile's symbol '.'.
    *
    * boardPiece is the piece placed on the board square
    */

    private val boardPiece: Piece? = null

    override fun toString(): String {
        return boardPiece?.toString() ?: super.toString()
    }
}