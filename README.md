# Dungeon Crawler
This is an ascii art video game where you enter commands to make things happen.

## Compile and Develop
1. [Download intellij](https://www.jetbrains.com/idea/download/)
2. [Install kotlin plugin](https://www.jetbrains.com/help/idea/managing-plugins.html)
3. [Clone the project with intellij](https://www.jetbrains.com/help/idea/manage-projects-hosted-on-github.html#clone-from-GitHub)
4. Add a kotlin configuration:
    1. Click the "Add Configuration" in the top right corner.\
   ![config location](images/run_config_loc.png)
    2. In the popup window click the + icon and select Kotlin.\
    ![add kotlin config](images/add_kotlin_config.png)
    3. Find main.kt by clicking on the ellipse next to the main class field. You should see the following popup:\
   ![img.png](images/add_main.png)
   Intellij should automatically find the main funciton for you. If not just type "MainKt" in the Main class field.
5. To select a JDK open file>>"project structure" window and select project. Select open jdk 16 in the SDK dropdown:\
![img.png](images/project_struct.png)
6. To add kotlin libraries select tools>>kotlin>>"configure kotlin in project".
7. Now press the green-triangular-run button:\
![img.png](images/run.png)



## Requirements to Play
* [Java Run Time Environment](https://www.java.com/en/)
* A keyboard

## Choose your Fantasy Race
When you start the program you will be greeted with your first option:
What fantasy race you would like to play as. Select a race by typing the 
first letter of its name. For example, if you want to be a human 
you can type "h" without the quotations and press enter.

## Map
You will then be shown the dungeon map. You are the @ symbol in the
center of the image below. Above the map are your characters stats.

![picture alt](images/map.png)

"Action (h for help):" is where you type in your commands

## Goal
Survive all levels 5 levels of the dungeon. Each level has a staircase
represented by a slash (/). Walking over the slash will bring you to 
the next level. 

The fifth and final level has a dollar sign ($) instead of a slash. 
Walking over this will win you the game.

## Move
You can move by using these commands:
* no -- Walk **north** one character
* ne -- Walk **north-east** one character
* ea -- Walk **east** one character
* se -- Walk **south-east** one character
* so -- Walk **south** one character
* sw -- Walk **south-west** one character
* we -- Walk **west** one character
* nw -- Walk **north-west** one character

**Important Note: you can repeat the last action by pressing enter without
typing anything. This is very useful for travelling long distances in one
direction.**

## Symbol Legend
Each character on the map represents a different game piece. The meaning of
each character is described below.

### Walkable Tiles
* "." -- Room Tile
* "+" -- Doorway
* "#" -- Hallway Tile

### Non-Walkable Tiles
* " " -- Empty Space
* "-", "|" -- Wall Tiles

### Enemies
* "D" -- Dragon
* "X" -- Phoenix
* "M" -- Merchant
* "N" -- Goblin
* "T" -- Troll
* "W" -- Werwolf
* "V" -- Vampire

### Misc
* " \ " -- Staircase
* "@" -- Player Character (You)
* "P" -- Potion
* "G" -- Gold

## Attack
To attack an enemy they must be within a one character radius of you.
Use the following command:

a *direction*

For example:
* a no -- attack something north of you
* a se -- attack something south-east of you

To repeat an attack at the same location press enter without typing 
anything.

## Use Potions
To use a potion, type:

u *direction*

For exmaple:
* u we -- use a potion west of you
* u ne -- use a potion north-east of you
