import kotlin.math.ceil

data class Strike(val damageAmt:Int, val slain:Boolean)

abstract class Creature(hp: Int, atk: Int, def: Int, symbol: Char,
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
    protected val maxHP = hp
    protected var hp = hp
        protected set(value) = if (value > maxHP) field = maxHP else field = value
    protected var atk = atk
        set(value) = if (value < 0) field = 0 else field = value
    protected var def = def
        set(value) = if (value < 0) field = 0 else field = value


    // takeDamage calculates damage based on the attackers atk value and returns the damage dealt
    open fun takeDamage(atk: Int): Int{
        val damageDealt = ceil((100/(100.0 + def)) * atk).toInt()
        hp -= damageDealt
        return damageDealt
    }
    
}