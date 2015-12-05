# AVT-5140-stratch
Java web server that allow to program AVT-5140 in Scratch

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

mvn clean install
