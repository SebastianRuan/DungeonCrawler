import kotlin.math.ceil

data class Strike(val damageAmt:Int, val slain:Boolean)

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
        protected set(value) = if (value > maxHP) field = maxHP else field = value


    //
    open fun damage(atk: Int): Strike{
        val damageDealt = ceil((100/(100.0 + def)) * atk).toInt()
        hp -= damageDealt
        if (hp <= 0){
            if (this is Enemy){
                board.addMessage("You did $damageDealt damage and killed the creature!")
                board.player.detach(this)
                return Strike(damageDealt,true)
            } else {
                board.addMessage("You took $damageDealt damage and DIED!")
                throw GameOver("")
            }
        }
        if (this is Enemy){
            board.addMessage("You did $damageDealt damage.")
        } else {
            board.addMessage("You took $damageDealt damage.")
        }
        return Strike(damageDealt,false)
    }
}