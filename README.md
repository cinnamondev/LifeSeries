# Generic Life Series Plugin

## base features

### revival clock

clock that can teleport a dead player and set their score

### custom recipes

custom recipes for standard minecraft recipes


## Timed

PvP gamemode with boogeymen rolled by GM.

All players have 24 hours, lose 1 hour on death (2 on boogey), gain 30 minutes on kill (1 hour for boogeys)

1-3 boogeymen can be chosen per session

revival clock can be crafted 

### 'Limited Life' configuration

```

```

## Wildcards


## Secret Life

Available tasks:
umm

- gamemaster

### commands:

#### Players

- `/secretTask`: Get your secret task book again (if you have one) 
- `/guess <player> <task description>`: Guess a players task!
    - IF `gamemaster-enabled: true`
        - all players with `life.gamemaster` permission will be given a message to accept or deny a guess.
            - (these player should also have the ability to use minecraft:tp, and permission `life.admin.game` to be able to pass or fail tasks)
    - `life.game.can-always.guess` - player can always guess a task irregadless of team


### explode-another-player

Explode a player (any), then run the task complete command.

## Cat Life

Secret Life for silly kittys!! Less focus on PVP (see example configurations for catlife for a deck of tasks)

### customizing your kttty!

For players:
`/catsumize <cat type> <collar colour>`
Required permission: `life.player.cat-customize`

For admins:
`/lf meowmeowmeowmeow <player selector/random (TODO)> <enable/disable/customize> <type (customize)> <collar colour (customize)>
Required permission: `life.admin.game`

rerolling tasks:

- tasks do not have to belong to a difficulty that that team can normally recieve
- tasks will always be of the harder category W.R.T original task
    - if player has EASY task, reroll will give them a NORMAL or HARD task.
    - if a player has NORMAL task, reroll will give them a HARD TASK
    - otherwise, HARD task.
