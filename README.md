# DiscordInjector

DiscordInjector is a simple application that extracts Discord, and insert normal JavaScript code or NodeJS code into the application, replacing code as well, and adding live editing CSS.

To use this, you need to place the `inject` folder and its contents in the same directory as the compiled jar file. If you are testing this with an IDE, place this in your `out/production/classes` directory. Using this application will force close Discord, and start it again when it finishes the modifications. To use the translation feature, you must get a [Yandex API](https://translate.yandex.com/developers/keys) (Free) and insert it into line 610 of `inject/inject.js`.

_Note: This only works on Windows_

## Features

There are several examples in the code of how to add more files. The examples included are:
- Adds an item to the right click menu to translate any message to English
- Makes the :thonk: emoji slightly bigger (If your server has the emoji)
- Makes the taco emoji (`:taco:`) over 3x bigger and spinning

## Screenshots:

Translation:

![](https://rubbaboy.me/images/dpl1cl3.gif)

Spinning tacos:

![](https://rubbaboy.me/images/hn9t445.gif)

Thonk emoji:

![](https://rubbaboy.me/images/3qtfdax)

## Removal of Modifications
To remove the moifications, go to your directory `C:\Users\<User>\AppData\Roaming\discord\0.0.300\modules\discord_desktop_core` (Or whatever is the latest version) and remove the *folder* named `core` (Discord must be closed for this). Rename the file `core-original.asar` to `core.asar`. Edit the file `index.js` from saying `require('./core');` to `require('./core.asar');`. After that, start Discord again and everything should be back to normal.

### Credits
Code from the following sources have been used:
- https://github.com/natanbc/asar - ASAR Extracting
- https://github.com/leovoel/BeautifulDiscord - JavaScript for auto reloading CSS
- https://gist.github.com/davidgilbertson/6eae478d9a197bfa1b4dfbef38f787e5 - Colored console output
