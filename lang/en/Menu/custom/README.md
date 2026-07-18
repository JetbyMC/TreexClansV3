This is the folder for custom menus. You can safely delete this file (README.md) if you want.

To open custom GUIs, you have two options:

1. `open_args`: You specify a list of subcommands through which the player will open it. For example: `/clan example`.
   There can be as many subcommands as needed.
2. `[open]`: Important — if you want to open a model FROM the models folder, use `[open] model:example`, but if you just
   want to open a custom GUI use `[open] example`.

If you need to create a menu that REQUIRES A CLAN, use: `model: clan_only` — then a player without a clan won't be able
to open the GUI, to prevent issues.

For ADDON GUIs use `model: Addon:model`, for example: `Glow:players`