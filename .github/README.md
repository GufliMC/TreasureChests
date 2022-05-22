# TreasureChests

A Minecraft plugin for creating treasure chests that refill after a certain amount of time.

## Platforms
Note: Java 17 is required

* [x] Bukkit / Spigot / Paper (1.16+)


## Usage

Shift + Left-click on a chest or shulker box. You need the `treasurechests.setup` permission.
A gui will open where you can configure the treasure chest.

### Modes

* PLAYER_BOUND: Each player that opens the chest will have their own individual inventory
* SERVER_BOUND: The inventory of the chest will be shared with everyone on the server

### Respawn time

When the chest is opened, it will refill after the given duration. This is calculated individually for each player if the chest is PLAYER_BOUND.

### Chances

The chance is applied to each **item** in the **stack**, a stack of 40 with a chance of 50% will give approximately 20 items each time.

### Database

Data is stored in a flatfile database by default. You can also use mysql by changing editing `config.json`:

```json
{
  "database": {
    "dsn": "jdbc:mysql://<hostname>:<port>/treasurechests",
    "username": "<user>",
    "password": "<password>"
  }
}
```
