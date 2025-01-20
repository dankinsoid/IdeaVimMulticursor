<img src="src/main/resources/pluginIcon.svg" width="80" height="80" alt="icon" align="left"/>

VimMulticursor
===
![Build](https://github.com/dankinsoid/VimMulticursor/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/19162-VimMulticursor.svg)](https://plusins.jetbrains.com/plugin/19162-VimMulticursor)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/19162-VimMulticursor.svg)](https://plugins.jetbrains.com/plugin/19162-VimMulticursor)

<!-- Plugin description -->
This plugin adds multiple cursor and selection capabilities to `IdeaVim`

![Preview](https://github.com/dankinsoid/VimMulticursor/blob/main/preview.gif?raw=true)

## Installation & Setup

1. Install the plugin:
   - Open IntelliJ IDEA
   - Go to Settings/Preferences → Plugins → Marketplace
   - Search for "VimMulticursor"
   - Click Install and restart your IDE

2. Enable the plugin:
   - Add the following line to the top of your `~/.ideavimrc` file:
     ```
     set multicursor
     ```
   - Reload IdeaVim settings (`:action IdeaVim.ReloadVimRc`)

## Commands

### Basic Usage
- `mc` + command: Create multiple cursors
- `ms` + command: Create multiple selections
- All commands work within selected text when there's an active selection

### Available Commands
- `mc/` + search: Add cursors at all occurrences of search regex.
- `mcf`x, `mcF`x: Add cursors at all occurrences of character x.
- `mcw`, `mcW`, `mcb`, `mcB`: Add cursors at words start.
- `mce`, `mcE`: Add cursors at words end.
- `mcaw`: Around word
- `mca` + bracket: Around bracket, like `mca(`/`mcab`, `mca{`/`mcaB`, `mca"`, etc
- `mci` + bracket: Inside bracket, like `mci(`/`mcib`, `mci{`/`mciB`, `mci"`, etc 
- `mcaa`/`mcia`: any bracket

### Cursor Management
- `mcc`: Add or remove a cursor highlight at the current position (preview mode)
- `mcr`: Convert cursor highlights to active editing cursors
- `mcd`: Remove all cursors and highlights
- `mcia`: Place cursors inside any brackets or quotes ((), [], {}, "", '', ``)
- `mcaa`: Place cursors around any brackets or quotes
- `mcaw`: Add cursors at word boundaries (at the start and end of current word)

The `mcia` and `mcaa` commands automatically find the nearest matching pair of delimiters around the cursor, handling proper nesting and matching of brackets/quotes.

## Examples

1. Select all occurrences of "print":
   ```
   ms/print<Enter>
   ```

2. Create cursors at each word start in selection:
   ```
   msw
   ```

3. Add cursors at specific positions:
   1. Move cursor to desired position
   2. Type `mcc` to add a cursor highlight
   3. Repeat for more positions
   4. Type `mcr` to convert highlights into editing cursors

## Optional Key Mappings

Add these to your `~/.ideavimrc` for faster access:
```vim
" Quick search-select
map q <Plug>(multicursor-ms/)

" Quick cursor add/apply
map z <Plug>(multicursor-mcc)
map Z <Plug>(multicursor-mci)

" Word-based selections
map <leader>w <Plug>(multicursor-msw)
map <leader>b <Plug>(multicursor-msb)
```

## Troubleshooting

1. Commands not working?
   - Ensure `set multicursor` is at the top of `.ideavimrc`
   - Restart IdeaVim after changes
   - Check if IdeaVim plugin is enabled

2. Cursors not appearing?
   - Ensure you are in normal mode
   - Try clearing all cursors with `mcd`
   - If issues persist, restart your IDE
<!-- Plugin description end -->

## License

Just as IdeaVim, this plugin is licensed under the terms of the GNU Public License version 3 or any later version.

## Credits

Plugin icon is merged icons of IdeaVim plugin and a random sneaker by FreePic from flaticon.com
## Installation

- Using IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "VimMulticursor"</kbd> >
  <kbd>Install Plugin</kbd>
  
- Manually:

  Download the [latest release](https://github.com/dankinsoid/VimMulticursor/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
