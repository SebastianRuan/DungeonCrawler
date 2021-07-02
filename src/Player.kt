abstract class Player(hp: Int,atk: Int, def: Int, position: Position, board: Board):
    Creature(hp, atk, def,'P', position, board) {
    /*
     * Player is the general class for all possible races
     *
     * gold: the amount of money the player has
     */
    val gold = 0


    override fun move() {
        TODO("Implement when once spawning is complete")
    }

    override fun attack(creature: Creature) {
        TODO("Implement when we do combat")
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