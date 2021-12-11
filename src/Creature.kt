import kotlin.math.ceil

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
        set(value) = if (value > maxHP) field = maxHP else field = value


    open fun damage(atk: Int){

        val damageDealt = ceil((100/(100.0 + def)) * atk).toInt()
        hp -= damageDealt
        if (hp <= 0){
            if (this is Enemy){
                throw KillMsg("You did $damageDealt damage and killed the creature!")
            } else {
                throw GameOver("You took $damageDealt damage and DIED!")
            }
        }
        if (this is Enemy){
            throw DamageMsg("You did $damageDealt damage.", damageDealt)
        } else {
            throw DamageMsg("You took $damageDealt damage.", damageDealt)
        }
    }
}