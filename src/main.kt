import java.io.File
import java.lang.Exception
import kotlin.random.Random

val randGen = Random(8472) // seed 8472 for testing purposes

data class Position(val row: Int, val col: Int)

class Chamber{
    /*
    * Camber is defined by the tiles contained within its walls.
    *
    * tiling: a list of floor pieces that make up the chamber
    * unoccupiedTiles: a set of integers representing the indexes of the tiles in tiling which are unoccupied
    * */

    private val tiling = mutableListOf<Tile>()
    private var unoccupiedTiles = mutableSetOf<Int>()

    fun append(tile: Tile){
        tiling.add(tile)
        unoccupiedTiles.add(tiling.lastIndex)
    }

    // random tile returns a randomly selected tile from tiling
    fun randomTile():Tile{
        val randIndex = unoccupiedTiles.random(randGen)
        unoccupiedTiles.remove(randIndex)
        return tiling[randIndex]
    }
    
    fun freeUpTiles(){
        unoccupiedTiles = (0..tiling.lastIndex).toMutableSet()
    }
}

class Board{
    /*
    * Board tracks everything. Just like a real board game it knows where the
    * player is and where all the potions are as well as all the enemies.
    *
    * grid: a 2d list representing all the squares on the board.
    * chambers: a list of all the rooms that exist on the current floor.
    * floor: the level of the dungeon the player is currently on.
    */
    private val grid  = mutableListOf<List<Piece>>()
    private val chambers = mutableListOf<Chamber>()
    private var floor = 1
    private var msgQ = ArrayDeque<String>()
    init {
        var row = 0
        File("src\\map.txt").forEachLine {
            val boardRow = mutableListOf<Piece>()
            for (col in 0..it.lastIndex) {
                boardRow += if (it[col] == '.' || it[col] == '+' || it[col] == '#') {
                                Tile(row, col, it[col])
                            } else {
                                Piece(it[col])
                            }
            }
            row++
            grid.add(boardRow)
        }
        createChambers()
    }

    var player = spawnPlayer()
        private set

    init {
        spawnEnemies()
        spawnItems()
        Merchant.hostile = false
    }

    // isHorizontalWall determines if the piece at i in row is the top or bottom wall of a chamber.
    private fun isHorizontalWall(row:List<Piece>,i:Int, direction: Int):Int{
        var j = i + direction

        while(row[j].toString() == "-" || row[j].toString() == "+"){
            j += direction
        }

        // not a horizontal wall
        if(row[j].toString() == "." ||
            !(row[j].toString() == "|" ||
               row[j].toString() == "+") ||         // corner doors
            row[j+1].toString() == "." ){
            return i
        }

        // horizontal wall
        return j
    }

    /*  scanChamber scans in a newly discovered camber starting at its topmost left-hand corner
     *  at position (paramRow, paramCol) in grid. */
    private fun scanChamber(paramRow:Int, paramCol:Int){
        var row = paramRow + 1
        var col = paramCol
        var direction = -1
        val chamber = chambers[chambers.lastIndex]

        // find all the tiles in the chamber in a back and forth scan
        while(isHorizontalWall(grid[row], col, direction) == col){
            var boardPiece = grid[row][col]
            while((boardPiece.toString() != "|" && boardPiece.toString() != "+") ||
                    grid[row+1][col].toString() == "." ||
                    (direction > 0 && grid[row][col+1].toString() == ".") ||
                    (direction < 0 && grid[row][col-1].toString() == ".")){

                col += direction
                if (boardPiece is Tile){
                    chamber.append(boardPiece)
                }
                boardPiece = grid[row][col]
            }

            row++
            direction *= -1
        }
    }


    // creatChambers scans through the map to locate and creates the chambers.
    private fun createChambers(){
        for (row in 1 until grid.lastIndex) {                      // loop over the board minus the boards border
            var col = 1
            while(col < grid[row].lastIndex) {
                if(grid[row][col].toString() == "|" && grid[row][col-1].toString() != "."){
                    val tmpCol = isHorizontalWall(grid[row], col, 1)
                    if (tmpCol != col && grid[row-1][tmpCol-1].toString() != "."){
                        col = tmpCol
                        chambers.add(Chamber())
                        scanChamber(row,col)
                    }
                }
                col++
            }
        }
    }

    // spawnStaircase to next floor and return it's chamber number
    private fun spawnStaircase(): Int{
        val stairChamber = randGen.nextInt(chambers.size)
        if(floor == 5){
            chambers[stairChamber].randomTile().placePiece(Piece('$'))
        } else {
            chambers[stairChamber].randomTile().placePiece(Piece('\\'))
        }

        return stairChamber
    }

    // newPlayerPos finds and returns a tile for the player in a chamber other than the chamber with stairs
    private fun newPlayerPos(stairChamber: Int): Tile{
        val playerChambers = (0 until stairChamber).union( stairChamber+1..chambers.lastIndex) // avoid stairs
        return chambers[playerChambers.random(randGen)].randomTile()
    }

    // spawnPlayer asks the user for the race they want to be, then creates and places it on the board
    private fun spawnPlayer(): Player{

        val stairChamber = spawnStaircase()

        // get player race
        val factory = PlayerFactory()
        print("Choose a race (h)uman (d)warf (e)lf (o)rc: ")
        val race = readLine() ?: "h"

        // spawn and place player
        val tile = newPlayerPos(stairChamber)
        val retPlayer = factory.getPlayer(race,tile.position, this)
        tile.placePiece(retPlayer as Piece)
        return retPlayer
    }

    // repositionPlayer seeks to relocate the player on the board
    private fun repositionPlayer(): Position{
        val stairChamber = spawnStaircase()
        val tile = newPlayerPos(stairChamber)
        tile.placePiece(player as Piece)
        return tile.position
    }

    //getUnoccupiedTile randomly selects an empty tile from the grid
    private fun getUnoccupiedTile():Tile{
        val randChamber = chambers.random(randGen)
        return try {
             randChamber.randomTile()
        } catch (e: NoSuchElementException){    // no space in chamber 
            chambers.remove(randChamber)
            val largerRandChamber = chambers.random(randGen)
            chambers.add(randChamber)
            largerRandChamber.randomTile()
        }
    }

    // spawnEnemies creates 20 random enemies with probabilities given in EnemyFactory and returns the list to the board
    private fun spawnEnemies(){
        val factory = EnemyFactory()
        for(i in 1..20){
            val tile = getUnoccupiedTile()
            val enemy: Enemy = factory.getRandomEnemy(tile.position,this)
            tile.placePiece(enemy)
            player.attach(enemy)
        }
    }
    // oneBlockRadius finds all Tile objects that are in a one block radius of tile
    fun oneBlockRadius(tile: Tile): MutableList<Tile>{

        val pos = tile.position
        val topRow = pos.row - 1
        val bottomRow = pos.row + 1
        val colStart = pos.col - 1
        val colEnd = pos.col + 1
        val tiles = mutableListOf<Tile>()

        for (col in colStart..colEnd){

            // row above
            if (col >= 0 && topRow >= 0 && col < grid[0].size && topRow < grid.size &&
                grid[topRow][col] is Tile){
                    tiles.add(grid[topRow][col] as Tile)
            }

            // row below
            if (col >= 0 && bottomRow >= 0 && col < grid[0].size && bottomRow < grid.size &&
                grid[bottomRow][col] is Tile){
                tiles.add(grid[bottomRow][col] as Tile)
            }
        }

        // check left of tile
        val colLeft = pos.col - 1
        if (colLeft >= 0 && colLeft < grid[0].size &&
                grid[pos.row][colLeft] is Tile){
            tiles.add(grid[pos.row][colLeft] as Tile)
        }

        // check neighbour right of cell
        val colRight = pos.col + 1
        if (colRight >= 0 && colRight < grid[0].size &&
            grid[pos.row][colRight] is Tile){
            tiles.add(grid[pos.row][colRight] as Tile)
        }

        return tiles
    }

    // spawnItems creates gold and potion objects
    private fun spawnItems(){
        // spawn gold
        for(i in 1..10){
            val tile = getUnoccupiedTile()
            val diceRoll = randGen.nextInt(1,9)
            tile.placePiece(
                when(diceRoll){
                    in 1..5 -> Gold(1) // probability 5/8
                    in 6..7 -> Gold(2) // probability 2/8
                    else -> {
                        // spawn dragon to protect large gold horde
                        val blockRadius =  oneBlockRadius(tile).filter { it.isEmpty }
                        if (blockRadius.isEmpty()) throw Exception("ERROR No room for dragon")
                        val dragonTile = blockRadius.random(randGen)
                        val dragon = Dragon(dragonTile.position,tile,this)
                        dragonTile.placePiece(dragon)
                        player.attach(dragon)

                        Gold(6) // probability 1/8
                    }
                }
            )
        }

        // spawn potions
        for(i in 1..10){
            val tile = getUnoccupiedTile()
            val diceRoll = randGen.nextInt(1,7)

            tile.placePiece(
                when(diceRoll){                         // probabilities are each 1 in 6
                    1 -> Potion(PotionType.H, 10)
                    2 -> Potion(PotionType.A, 5)
                    3 -> Potion(PotionType.D, 5)
                    4 -> Potion(PotionType.H, -10)
                    5 -> Potion(PotionType.A, -5)
                    else -> Potion(PotionType.D, -5)
                }
            )
        }
    }

    // getFloorPiece extracts a tile or a piece that represents part of the board (e.g: "|", "-", etc.)
    fun getFloorPiece(row: Int, col: Int): Piece{
        return grid[row][col]
    }

    // nextLevel clears and sets the board up for the next level of the dungeon
    fun nextLevel(): Position{
        msgQ.addLast("You descend deeper!\n")
        floor++

        // Clear board
        for (row in grid) {
            for (piece in row) {
                if (piece is Tile) {
                    piece.clear()
                }
            }
        }
        
        for (chamber in chambers){
            chamber.freeUpTiles()
        }
        
        player = player.clearPotions()
        val pos = repositionPlayer()
        spawnEnemies()
        spawnItems()
        return pos
    }

    override fun toString(): String {
        // loop through the board and ask each piece to get its string version
        var printRow = ""
        for (boardRow in grid){
            for(piece in boardRow){
                printRow += piece.toString()
            }
            printRow += "\n"
        }
        return printRow
    }

    // addMessage pushes msg to the end of the message queue
    fun addMessage(msg: String){
        msgQ.addLast(msg)
    }

    // printMessages print any and all errors or info from the previous cmd
    private fun printMessages(){
        while (!msgQ.isEmpty()){
            println(msgQ.removeFirst())
        }
    }
    
    // decoratePlayer adds a potion's affects to the player
    private fun decoratePlayer(direction: String){
        val floorPiece = player.dirToTile(direction) as Tile
        val potion = floorPiece.boardPiece as Potion
        floorPiece.clear()
        player = player.drink(potion) ?: player
    }

    fun commandLine(): String{
        var prevLine = listOf<String>();

        while(true) {
            // print player stats
            println("\n\n")
            println(String.format("%s %59s", player.printType(), "floor: $floor"))
            player.printStats()
            print(this)

            printMessages()

            // get command
            print("Action (h for help): ")
            var line: List<String> = readLine()?.split(" ") ?: return "EOF"
            if(line[0] == ""){
                line = prevLine.toList()
            } else {
                prevLine = line.toList()
            }
            val cmd: String = line[0]
            val param1: String = if (line.size == 2) line[1] else ""

            try {
                // execute command
                when (cmd) {
                    "no", "ne", "ea", "se", "so", "sw", "we", "nw" -> player.move(cmd)

                    "q" -> return "quit"

                    "a" -> player.attack(param1)
                    
                    "u" -> if (player.dirToTile(param1) is Tile && 
                        (player.dirToTile(param1) as Tile).boardPiece is Potion) {
                        decoratePlayer(param1)
                        } else {
                            msgQ.addLast("There is no potion to the $param1")
                        }

                    else -> {
                        //TODO make sure the file reading works with other IDEs
                        msgQ.addLast(
                            File("src/help.txt").readLines().fold(""){
                                x:String, y: String -> x+y+"\n"
                            }
                        )
                    }
                }
            } catch (e: GameOver){
                print(this) //TODO: do we need to print 'this'
                printMessages()
                return "game over"
            } catch (e: Win){
                return "win"
            }
        }
    }
}

fun main(){
    while(true){
        val board = Board()
        val endType = board.commandLine()

        // End Program
        if (endType == "quit" || endType == "EOF") {
            return
        } else if (endType == "game over"){
            println("XXX Game Over XXX")
        } else {
            println("YOU GOT THE TREASURE!! YOU WIN!!")
        }

        // Replay?
        print("Play again? (y)es, (n)o: ")
        val ans = readLine()

        if (ans == "n" || ans == null) return
    }
}