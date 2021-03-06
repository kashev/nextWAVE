nextWAVE
=========

    nextWAVE
    A project for HackIllinois 2014

    Dario Aranguiz :: aranguizdario@gmail.com
    Kashev Dalmia  :: kashev.dalmia@gmail.com
    Brady Salz     :: brady.salz@gmail.com
    Ahmed Suhyl    :: sulaimn2@illinois.edu

## About

[**nextWAVE**](http://kashev.github.io/nextWAVE/) is a smart microwave built for the hardware hackathon section of [HackIllinois](http://www.hackillinois.org/).

nextWAVE solves the 'problem' of not knowing how long to microwave your food. We built an Android app that allows you to scan barcodes, and look up cook times in a [Firebase Database](https://www.firebase.com/). The app can be launched using an NFC tag. Then, the app can turn on the microwave via wifi using a [Spark Core Microcontroller](https://www.spark.io/). While your food is cooking, the cook time is displayed on a [Pebble Smartwatch App](https://getpebble.com/). Finally, when the food is done cooking, it will open itself and play ["Funky Town"](https://www.youtube.com/watch?v=HRDc31Co8sI).

You can view all the source code [here](https://github.com/kashev/nextWAVE) and see a video of the working microwave [here](https://www.youtube.com/watch?v=uaeWA7mdfUo).

## Thanks

Team Brady Rocks built nextWAVE in 36 hours.

- **Dario Aranguiz** - Android App, DB Interaction
- **Kashev Dalmia** - Pebble App, Website, Android Layout
- **Brady Salz** - Hardware, Hardware, Hardware, Embedded Software
- **Ahmed Suhyl** - Embedded Software, Test, Hardware

Special thanks to **Isaac DuPree** for designing the logo.

## Reviews

"This is so dumb you fat kids used sellotape to hold this radioactive machine together you are going to get cancer what do they teach you at that school how to eat your lunch? Use your god given brane for once  #realtalk   #obliterated " - Miles Anderson

"As the "techie culture" crawls ever so further up its own anus I crave the day when the ISIS will celebrate victory over the smoldering ruins of our failed civilization." - kortexsirvasil

"not really more innovative than a normal microwave." - Dragam

## TODO

- [X] Working Microwave
    - [X] Manual Switching
    - [X] Relay Controlled Switching 
    - [X] Add NFC Launching Pads
    - [X] Decorate
- [ ] Working Spark Core Code
    - [X] Switch a pin on & off
    - [ ] Control a Screen
- [ ] Working Android App
    - [X] Send Commands to the Spark
    - [X] Send Remaining Cook Time to the Pebble
    - [X] Scan Barcodes
    - [X] Communicate with FireBase Backend
    - [ ] Facebook Login
- [X] Pebble App
    - [X] Get and Display Remaining Cook Time
- [ ] Website
    - [X] Looks Nice
    - [X] Link to Github Code
    - [ ] Food Stream

