# AVT-5140 Scratch
Java web server that allow to program AVT-5140 in Scratch

![Screenshot](https://raw.githubusercontent.com/SebastianCelejewski/AVT-5140-Scratch/master/documentation/AVT-5140%20in%20Scratch.PNG)

## Links

- Drivers and simple diagnostics program: http://serwis.avt.pl/files/AVT5140.zip
- AVT-5140 kit documentation: http://serwis.avt.pl/manuals/AVT5140.pdf

## Installation

- Drivers for AVT-5140
  - Download and unzip drivers
  - Restart Windows and keep pressing F8 while it boots again to enter Advanced Startup Options
  - Select Disable Driver Signature Enforcement
  - Connect AVT-5140 controller to an USB port. Windows will try to download and install driver automatically and will fail.
  - Open Control Panel/Devices Manager and locate the controller
  - Install driver from unzipped folder.
- RxTx Library
  - Download RxTx library from http://rxtx.qbang.org/wiki/index.php/Download
  - Unzip downloaded package
  - Follow readme to copy dll files to appropriate locations
 
## Build

- cd Avt5140Driver
- mvn clean install
- cd ../Avt5140Server
- mvn clean install assembly:assembly

## Run and test

- Run AVT-5140 Server
  - cd target
  - java -jar AVT-5140-server-1.0.0-jar-with-dependencies.jar
- Download Scratch plugin configuration file
  - localhost:8000/scratch
  - Save Scratch plugin definition to your hard drive
  
## Configure Scratch to use AVT-5140 plugin
- Import Scratch plugin definition file
  -  Launch Scratch 2
  -  Left-click File menu with left Shift button pressed
  -  Select Import Experimental HTTP Extension
  -  Select downloaded Scratch plugin definition file
- Go to Script/More Blocks in Scratch
  - You should see AVT-5140 section
