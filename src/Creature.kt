abstract class Creature(hp: Int, val atk: Int, val def: Int, symbol: Char,
                        protected var pos: Position, protected val board: Board):Piece(symbol) {
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
    var hp = hp
        private set

    abstract fun attack(creature: Creature)
}