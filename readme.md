# Bone Eater
## Top down adventure reverse-horror game
This game is a work in progress; it has many missing features and bugs.

You play as Smith the knave turned ghoul.  He was thrown in the oubliette under the castle for plotting trechery against the king.  Having sacrificed himself to the old gods with whom he is well acquainted, he is afforded another chance at glory.  He returns from the underworld as a hungry dead.  He was never much of a fighter, and must sneak-attack his foes.  His ability to reanimate the dead and infect the living with mind control will prove invaluable.  Spear enemies with zombie blood on ranged weapons to infect them. And of course, feast on fallen enemies to replenish health like the savage you are.

The game uses an oblique camera projection.  Collision class handles invisible tiles used for wall collision detection.  Tilegrid is for background tiles. Brain class handles scripted events.  Entity class handles enemies and NPCs.  Decal class handles temporary images drawn on the ground in world coordinate plane like blood splatters. Decor class draws some images above, and some below the player based on player's screen position.

### Controls
* WASD and Arrow keys = Movement
* E = Activate
* F = Attack
* Shift = Sprint
* I = Inventory
* Q = Toolbar

### Level Editor
* t = edit tile
* n = normal mode
* o = edit triggers
* p = edit pickups
* ] = save edits
* [ = reload map data from file
* Numpad decimal = latch paint mode
* Numpad + = delete decor and other sprites
* Mouse MC/RC = change asset
* Mouse LC = paint
* ` In-game console

## Building
The project can be built using Eclipse. Unzip the project folder archive, or clone the repo using git.  File >> Open Projects from FileSystem >> select the project folder.  Finish