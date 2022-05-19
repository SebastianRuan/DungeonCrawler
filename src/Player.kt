import kotlin.math.abs

interface Subject{
    val observers: MutableList<Observer>
    fun attach(observer: Observer){
        observers.add(observer)
    }
    fun detach(observer: Observer){
        observers.remove(observer)
    }
    fun notifyAllObservers(){
        observers.map { it.update() }
    }
}

// Player interface contains all methods a player character is allowed to call
interface Player: Subject{
    // move moves the player from one floor tile to another specified by direction which is one of no, ne, ea, se, so,
    //     sw, we and nw
    fun move(direction: String)

    // attack damages an enemy in direction
    fun attack(direction: String)

    // attack(String, Int) overloads attack(String) so the decorator can pass its attack value in
    //    DO NOT USE OUT OF PlayerDec Class OR ITS CHILDREN
    fun attack(direction: String, atkBuff: Int) // reserved for the decorator TODO: try to remove

    // damage determines the damage dealt with the attackers atk value. It also determines if the player has died and
    // ends the game if so.
    fun damage(atk: Int): Strike

    // damage(Int, Int) overloads damage(String) so the decorator can pass its defBuff value in
    fun damage(atk: Int, defBuff: Int): Strike // reserved for the decorator TODO: try to remove
    fun loseGold(lost: Int)
    fun drink(potion: Potion): PlayerDec?

    // dirToTile converts string direction (no, ne, ea, se, so, sw, we and nw) to a piece in specified direction
    fun dirToTile(direction: String): Piece

    // printStats displays the character's type, hp, def and atk on the screen
    fun printStats()

    // printStats applies buffs before printing stats
    fun printStats(atkBuff: Int, defBuff: Int) // reserved for the decorator TODO: try to remove

    // printType gets the specific type of PC the user chose
    fun printType(): String

    // clearPotions returns an undecorated player (potion buffs and de-buffs removed) and prints this fact to the user
    //    if hasPotions is set to true.
    fun clearPotions(hasPotions: Boolean = false): Player
}

abstract class BasePlayer(hp: Int, atk: Int, def: Int, position: Position, board: Board):
    Creature(hp, atk, def,'@', position, board), Player {
    /*
     * BasePlayer is the parent class for all possible races. Please refer to the Player interface for a description
     *  of what each method does
     *
     * gold: the amount of money the player has
     */
    var gold = 0
        protected set(value) = if (value < 0) field = 0 else field = value

    override val observers = mutableListOf<Observer>()

    override fun dirToTile(direction:String): Piece{
        // Select tile
        val (row, col) = pos
        return when(direction){
            "no" -> board.getFloorPiece(row-1, col)
            "ne" -> board.getFloorPiece(row-1, col+1)
            "ea" -> board.getFloorPiece(row, col+1)
            "se" -> board.getFloorPiece(row+1, col+1)
            "so" -> board.getFloorPiece(row+1, col)
            "sw" -> board.getFloorPiece(row+1, col-1)
            "we" -> board.getFloorPiece(row, col-1)
            else -> board.getFloorPiece(row-1, col-1)
        }
    }

    override fun move(direction: String){    

        val floorPiece = dirToTile(direction)
        // Move player if possible
        pos = if(floorPiece is Tile && (floorPiece.isEmpty || floorPiece.toString() == "G")){
            if (floorPiece.toString() == "G") {               // Get gold
                gold += (floorPiece.boardPiece as Gold).amt
                floorPiece.clear()
            }
            // move
            floorPiece.movePiece( board.getFloorPiece(pos.row,pos.col) as Tile,this)
            floorPiece.position.copy()
        }  else if (floorPiece.toString() == "\\"){           // When Player moves onto stairs
            observers.clear()
            board.nextLevel()
        } else if (floorPiece.toString() == "$"){
            throw Win("YOU GOT THE TREASURE!! YOU WIN!!")
        }else {
            board.addMessage("Cannot move there.")
            return
        }
        
        notifyAllObservers()
    }

    override fun attack(direction: String) {
        val floorPiece = dirToTile(direction)
        if(floorPiece is Tile && floorPiece.boardPiece is Enemy){           // attack successfully targets an enemy
            val playersAttack: Strike = (floorPiece.boardPiece as Enemy).damage(atk)
            if(playersAttack.slain){
                gold += 1
                floorPiece.clear()
            }
            notifyAllObservers()

        } else if (floorPiece is Tile && !floorPiece.isEmpty){              // attack incorrectly targets an item
            val item = when(floorPiece.boardPiece){
                is Potion -> "potions"
                else -> "gold"
            }
            board.addMessage("Cannot attack $item.")
        } else if (floorPiece is Tile) {
            board.addMessage("Cannot attack an empty tile.")
        } else {
            board.addMessage("Cannot attack a wall.")
        }
    }

    override fun attack(direction: String, atkBuff: Int){
        atk += atkBuff
        attack(direction)
        atk -= atkBuff
    }

    override fun damage(atk: Int): Strike {
        val damageDealt = takeDamage(atk)
        if (hp <= 0){
            board.addMessage("You took $damageDealt damage and DIED!")
            throw GameOver("")
        }
        
        board.addMessage("You took $damageDealt damage.")
        return Strike(damageDealt,false)
    }

    override fun damage(atk: Int, defBuff:Int): Strike{
        def += defBuff
        val dmg = damage(atk)
        def -= defBuff
        return dmg
    }

    override fun loseGold(lost: Int){
        gold -= lost
    }
    
    override fun drink(potion: Potion): PlayerDec?{
        return when (potion.kind) {
            PotionType.H -> {
                this.hp += potion.amt
                board.addMessage("You drink a red coloured potion giving you ${potion.amt} health.")
                null
            }
            PotionType.A -> {
                board.addMessage("You drink a teal coloured potion changing your attack by ${potion.amt}.")
                AtkDec(board.player, potion.amt)
            }
            else -> {
                board.addMessage("You drink a green coloured potion changing your defence by ${potion.amt}.")
                DefDec(board.player, potion.amt)
            }
        }
    }

    override fun printStats(){
        println("Hp: $hp  Atk: $atk  Def: $def  Gold: $gold")
    }

    override fun printStats(atkBuff: Int, defBuff: Int){
        atk += atkBuff
        def += defBuff
        printStats()
        atk -= atkBuff
        def -= defBuff
    }

    override fun printType(): String {
        return "Player Type: ${javaClass.name}"
    }

    override fun clearPotions(hasPotions: Boolean): Player {
        if (hasPotions) board.addMessage("You feel the effects of the potions you drank wear off.")
        return this
    }
}

/*
* Decorators
*/

abstract class PlayerDec(protected val player: Player): Player {
    /*
     * PlayerDec is the parent class for the decorator design pattern
     *
     * observers: DON'T USE TODO: see if i can get rid of this
     */
    
     override val observers: MutableList<Observer>
        get() = player.observers

    override fun move(direction: String) {
        player.move(direction)
    }
    override fun attack(direction: String) {
        player.attack(direction)
    }
    
    override fun attack(direction: String, atkBuff: Int){
        player.attack(direction, atkBuff)
    }

    override fun damage(atk: Int): Strike {
        return player.damage(atk)
    }

    override fun damage(atk: Int, defBuff: Int): Strike{
        return player.damage(atk, defBuff)
    }

    override fun drink(potion: Potion): PlayerDec? {
        return player.drink(potion)
    }

    override fun loseGold(lost: Int) {
        player.loseGold(lost)
    }

    override fun dirToTile(direction: String): Piece {
        return player.dirToTile(direction)
    }

    override fun printStats() {
        return player.printStats()
    }

    override fun printType(): String {
        return player.printType()
    }

    override fun clearPotions(hasPotions: Boolean): Player {
        return player.clearPotions(true)
    }
}

class AtkDec(player: Player, private val buff: Int): PlayerDec(player){
    /*
     * AtkDec imposes the buff or debuff of potions when attacking
     * 
     */
    
    // attack used to maintain player interface
    override fun attack(direction: String) {
        player.attack(direction,buff)
    }

    // attack used to pass attack buff along decoration pipeline
    override fun attack(direction: String, atkBuff: Int) {
        player.attack(direction, atkBuff + buff)
    }

    // printStats used to maintain player interface
    override fun printStats() {
        return player.printStats(buff,0)
    }

    // printStats used to pass buff stats along decorator pipeline
    override fun printStats(atkBuff: Int, defBuff: Int) {
        player.printStats(atkBuff + buff, defBuff)
    }
}



class DefDec(player: Player, private val buff: Int): PlayerDec(player){
    /*
     * DefDec imposes the buff or debuff of potions when defending 
     * 
     */

    // attack used to maintain player interface
    override fun damage(atk: Int): Strike {
        return player.damage(atk, buff)
    }

    // attack used to pass attack buff along decoration pipeline
    override fun damage(atk: Int, defBuff: Int): Strike {
        return player.damage(atk, defBuff + buff)
    }

    // printStats used to maintain player interface
    override fun printStats() {
        return player.printStats(0,buff)
    }

    // printStats used to pass buff stats along decorator pipeline
    override fun printStats(atkBuff: Int, defBuff: Int) {
        player.printStats(atkBuff, defBuff + buff)
    }
}


/*
* Player Children
*/

class Human(pos: Position, board: Board): BasePlayer(140, 20, 20, pos, board) {

}

class Dwarf(pos: Position, board: Board): BasePlayer(100, 20, 30, pos, board) {
    private fun doubleGold( superFn: () -> Unit){
        val prevGold = gold
        superFn()
        gold += gold - prevGold // double gold for dwarves
    }
    
    
    override fun move(direction: String) {
        doubleGold{super.move(direction)}
    }

    override fun attack(direction: String) {
        doubleGold{super.attack(direction)}
    }
}

class Elf(pos: Position, board: Board): BasePlayer(140, 30, 10, pos, board) {
    // override drink to make elves negate the effects of negative potions
    override fun drink(potion: Potion): PlayerDec? {
        val decoration: PlayerDec? = when (potion.kind) {
            PotionType.H -> {
                this.hp += abs(potion.amt)
                board.addMessage("You drink a red coloured potion giving you ${potion.amt} health.")
                null
            }
            PotionType.A -> {
                board.addMessage("You drink a teal coloured potion changing your attack by ${potion.amt}.")
                AtkDec(board.player, abs(potion.amt))
            }
            else -> {
                board.addMessage("You drink a green coloured potion changing your defence by ${potion.amt}.")
                DefDec(board.player, abs(potion.amt))
            }
        }
        
        if (potion.amt < 0){
            board.addMessage("Your elvin blood negates the negative effects of the potions making you feel\nstronger!")
        }
        return decoration
    }
}

class Orc(pos: Position, board: Board): BasePlayer(180, 30, 25, pos, board) {
    private fun doubleGold( superFn: () -> Unit){
        val prevGold = gold
        superFn()
        gold -= (gold - prevGold)/2 // halve gold for Orcs
    }


    override fun move(direction: String) {
        doubleGold{super.move(direction)}
    }

    override fun attack(direction: String) {
        doubleGold{super.attack(direction)}
    }
}

class PlayerFactory{
    /*
    * PlayerFactory has the sole job of generating Player objects depending on what race the user chooses
    */

    fun getPlayer(type: String, pos: Position,  board: Board):BasePlayer{
        return when(type){
            "e" -> Elf(pos, board)
            "d" -> Dwarf(pos,board)
            "o" -> Orc(pos, board)
            else -> Human(pos, board)
        }
    }
}

class GameOver(message: String) : Exception(message)
class Win(message: String) : Exception(message)