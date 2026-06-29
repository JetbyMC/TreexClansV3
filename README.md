## [Docs](https://wiki.jetby.space/plugins/treexclans) | [Discord](https://dsc.gg/jmdev) | [Download .jar](https://modrinth.com/plugin/treexclans)

| **Dependencies**| |
|:-|-|
| **Libb** | [Download](https://modrinth.com/plugin/libb) |
| Vault | [Download](https://www.spigotmc.org/resources/vault.34315/) |
| Java 21 or higher | 
| Paper or forks |
| Minecraft Version 1.20 or higher |

# TreexClans - This plugin is different. 

This plugin offers the user complete freedom; compared to other plugins, you get total control over customization. The entire frontend of the plugin can be rewritten from scratch using only YAML configurations.

For instance, you can change the `/clan` command to `/team` or anything else. If you don't like the `/clan home` argument, you can change it to `/clan base` or `/clan house`—in fact, you can modify arguments and even add multiple aliases for them.
```yaml
clan:
  - clan
  - team
args:
  create:
    - create # /clan create
  invite:
    - invite # /clan invite
  balance:
    - balance # /clan balance
    - bal # /clan bal
# etc...
```
You also have complete freedom regarding GUIs; if you’ve used DeluxeMenus before, you’ll quickly figure out how to create any GUI for any purpose. We use Libb for GUI handling.
Furthermore, you can customize every single message—and it goes beyond simple text changes; you can add sounds, action bars, effects, titles, commands, and more. Here is a practical example:
```yaml
commands:
  create:
  - "[message] &#FC835C• /{cmd} {arg} [tag] &7- &fCreate a clan"
  # want to add a sound? NO PROBLEM
  - "[sound] ENTITY_VILLAGER_YES;1;1"
  # need MiniMessage? No problem—it's supported everywhere in the plugin, for example:
  - "[message] Hello <rainbow>world</rainbow>"
```

# Benefits
- Support — active bug fixing and community-driven improvements.
- Flexible API
- Separate folder for add-ons
- Customizable — fully configurable with custom functionality support.
- GUIs — create unlimited custom interfaces.
- Technology — uses modern solutions such as MiniMessage and Libby.
- Economy — includes a clan bank system for deposits and upgrades.
- Clan Coins — earn and spend coins on perks and rewards.
- Levels — level up clans to unlock more members, storage, and bank capacity.
- MiniMessage - We use mini message everywhere, guis, messages, logs...


# What addons do you have?
- [Leaderboard](https://modrinth.com/plugin/clanleaderboard)
- [Quests](https://modrinth.com/plugin/clanquest)
- [Glow](https://modrinth.com/plugin/clanglow)
- [Events](https://modrinth.com/plugin/clanevents)
- War(soon)
- Discord Integration (soon)


[![](https://bstats.org/signatures/bukkit/TreexClans.svg)](https://bstats.org/plugin/bukkit/TreexClans/27749)
