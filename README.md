# Galaxian Invaders

## 

## Target Environment

* Any Android device with Android 10 or higher
* Fullscreen, Portrait-Mode
* In Landscape mode only the center of the screen will be used

## Tested with

* Samsung S20 FE 
* Samsung S9
* Raspberry PI4 (LineageOS)

## Joystick support

| Control    | Function         |
|------------|------------------|
| DPAD Left  | Move left        |
| DPAD Right | Move right       |
| START      | Restart the game |


# Hotkeys Android


| Key      | Function     |
|----------|--------------|
| CTRL+ESC | Menu Key     |
| ALT+ESC  | Home Key     |
| ALT+TAB  | Task Manager |



# Control with external Keyboard

Connecting an external keyboard to your Android devices simplifies testing and debugging.

The following keys are used to control the normal game play:

| Key        | Description  |
|------------|--------------|
| DPAD_LEFT  | Move left    |
| DPAD_RIGHT | Move right   |
| BUTTON_A   | Fire         |    
| R          | Restart Game |    


| Key | Description                                |                                        |
|-----|--------------------------------------------|:---------------------------------------|
| A   | Chat Code AUTOKILL - auto kill one invader | Kill one invader automatically         |
| D   | Toggle Debug Mode                          | View collision grid and traces         |
| G   | Toggle God Mode                            | Player-ship gets indestructible        |
| L   | Cheat Code 'LAZY'                          | Invader swarm gets very slow           |
| M   | Cheat Code 'MYSTERY'                       | Mystery Ufo starts at once             |
| P   | Cheat Code 'PEACE'                         | Invaders stop shooting                 |
| S   | Cheat Code 'SINK'                          | Invaders sink down fast                |
| T   | Cheat Code 'TEXT'                          | Test the text-2-pixel rendering        |
| W   | Cheat Code WAVE COMPLETED                  | Forwards to the next level immediately |
| Y   | Cheat Code KILL YOURSELF                   | Destroy player ship                    |


## Debug with USB-C 

Make a connection from an USB-C port of yourPC to the USB-C port of the Raspberry PI 4.

The PC will power the Raspberry PI4. Android Studio will automatically detect the Raspberry PI 4 as an 'physical device'.


## Pairing for Wifi remote debugging

On Android
* Activate the Developer mode 
* Activate the Wifi pairing

On PC (Android Studio)
* Activate the Wifi pairing

## Re-connect 

Start a command line window


    cd %LOCALAPPDATA%\Android\sdk\platform-tools
    
    adb connect <ip-address>:<port>

## Copyrights

Background Image: https://pixabay.com/de/photos/planet-mond-raum-weltraum-w%C3%BCste-5966336/

