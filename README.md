<img src="src/main/resources/pluginIcon.svg" width="80" height="80" alt="icon" align="left"/>

VimMulticursor
===
![Build](https://github.com/dankinsoid/VimMulticursor/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/19162-VimMulticursor.svg)](https://plusins.jetbrains.com/plugin/19162-VimMulticursor)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/19162-VimMulticursor.svg)](https://plugins.jetbrains.com/plugin/19162-VimMulticursor)

<!-- Plugin description -->
This plugin brings multiple cursors and selections to `IdeaVim`

![Preview](https://github.com/dankinsoid/VimMulticursor/blob/main/preview.gif?raw=true)

## Usage

- Type `mc` (<ins>m</ins>ulti<ins>c</ins>ursor) and a vim command to create cursors
- Type `ms` (<ins>m</ins>ulti<ins>s</ins>elect) and a vim command to select multiple items
- If you previously selected some text, then the commands work only in the selected text
- Supported vim commands: `/`, `f`, `t`, `w`, `W`, `b`, `B`, `e`, `E`


- Type `mca` (<ins>m</ins>ulti<ins>c</ins>ursor <ins>a</ins>dd) to add (or remove) a virtual caret
- Type `mci` (<ins>m</ins>ulti<ins>c</ins>ursor <ins>i</ins>nsert) to insert real carets instead of virtual
- Type `mcd` (<ins>m</ins>ulti<ins>c</ins>ursor <ins>d</ins>elete) to remove all virtual carets

Example:

type `ms/print` to select all `print`s in selected text

## Setup

Install plugin from Intellij Idea Marketplace and add the following option on top of your `./ideavimrc`:

```
set multicursor
```
also you can map commands, for example: 
```
map q <Plug>(multicursor-ms/)
map z <Plug>(multicursor-mca)
map Z <Plug>(multicursor-mci)
```
After IdeaVim reboot you can use this plugin
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
