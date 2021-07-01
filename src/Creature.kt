abstract class Creature(private val hp: Int, private val atk: Int, private val def: Int, symbol: Char,
                        private val pos: Position,private val board: Board):Piece(symbol) {
    /*
    * Creature stores all of the properties of enemies and players.
    * basically anything that is alive is a creature
    *
    * hp: hit points
    * maxHP: maximum value you can heal your hp
    * atk: attack damage done to other creatures
    * def: the resistance of an attack
    * board: a reference to the main board
    */
    val maxHP = hp

    abstract fun move()
    abstract fun attack(creature: Creature)

}