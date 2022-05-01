# BrickPermissions

An extension for [Minestom](https://github.com/Minestom/Minestom) to manage the permissions of players.
Players can have individual permissions or can be assigned to a group with permissions. The data can be stored in a 
database server or a local H2 file database.

## Install

Get the [release](https://github.com/MinestomBrick/BrickPermissions/releases)
and place it in the extension folder of your minestom server.

### Dependencies
* [BrickI18n](https://github.com/MinestomBrick/BrickI18n)

## Commands

Don't worry, console can execute all commands ;)

| Command | Permission |
|---|---|
| **Players** ||
| /bp player info (player) | brickpermissions.player.info |
| /bp player permission add (player) (permission) | brickpermissions.player.permission.add |
| /bp player permission remove (player) (permission) | brickpermissions.player.permission.remove |
| **Groups** ||
| /bp group add (name) | brickpermissions.group.add |
| /bp group remove (group) | brickpermissions.group.remove |
| /bp group list | brickpermissions.group.list | 
| /bp group info (group) | brickpermissions.group.info |
| /bp group permission add (group) (permission) | brickpermissions.group.permission.add |
| /bp group permission remove (group) (permission) | brickpermissions.group.permission.remove |

## Database

You can change the database settings in the `config.json`.

```json
{
  "database": {
    "dsn": "jdbc:h2:file:./extensions/BrickPermissions/data/database.h2",
    "username": "dbuser",
    "password": "dbuser"
  }
}
```

MySQL is supported, use the following format:

````
"dsn": "jdbc:mysql://<hostname>:<ip>/<database>"
````

## API

### Maven
```
repositories {
    maven { url "https://repo.jorisg.com/snapshots" }
}

dependencies {
    implementation 'org.minestombrick.permissions:api:1.0-SNAPSHOT'
}
```

### Usage

Check the [javadocs](https://minestombrick.github.io/BrickPermissions/)

#### Examples

```
// does not persist
player.addPermission("super.cool.permission");

// does persist
PermissionAPI.get().addPermission(player, "super.cool.permission");

PermissionAPI.get().addGroup("admin")
    .thenCompose(group -> PermissionAPI.addPermission(group, "fly.mode"))
    .thenCompose(v -> PermissionAPI.addGroup(player, group))
```

Note that the api returns completable futures. If you need to execute multiple operations
on related objects, you have to wait for the previous operation to finish.