abstract class Enemy(hp: Int, atk: Int,def: Int, sym: Char, pos: Position, board:Board ):
    Creature(hp,atk,def,sym,pos, board) {
    /*
    * Enemy defines the behaviour of the creatures that are attempting to destroy the player
    */

    fun move(){}

    override fun attack(creature: Creature) {
        TODO("Not yet implemented")
    }
}

class Vampier(pos:Position, board:Board): Enemy(50, 25, 25, 'V', pos,board){

}

class Werewolf(pos:Position, board:Board): Enemy(120, 30, 5, 'W', pos,board){

}

class Troll(pos:Position, board:Board): Enemy(120, 25, 15, 'T', pos,board){

}

class Goblin(pos:Position, board:Board): Enemy(70, 5, 10, 'N', pos,board){

}

class Merchant(pos:Position, board:Board): Enemy(30, 70, 5, 'M', pos,board){

}

class Dragon(pos:Position, board:Board): Enemy(150, 20, 20, 'D', pos,board){

}

class Phoenix(pos:Position, board:Board): Enemy(50, 35, 20, 'X', pos,board){

}

class EnemyFactory{
    /*
    * EnemyFactory is responsible for creating the many different kinds of enemies the player fights
    */

    // getRandomEnemy randomly selects an enemy to spawn at position pos on board
    fun getRandomEnemy(pos:Position,board: Board): Enemy{
        val diceRoll = randGen.nextInt(1,19)
        return when(diceRoll){
            in 1..4 -> Werewolf(pos,board)   // probability 4/18
            in 5..7 -> Vampier(pos,board)    // probability 3/18
            in 8..12 -> Goblin(pos,board)    // probability 5/18
            in 13..14 -> Troll(pos,board)    // probability 2/18
            in 15..16 -> Phoenix(pos,board)  // probability 2/18
            else -> Merchant(pos,board)      // probability 2/18
        }

    }
}