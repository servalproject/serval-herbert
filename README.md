# Serval Herbert #

The purpose of the Serval Herbert software is to start exploring the use of the [IOIO board](http://www.sparkfun.com/products/10748) with an [Android](http://www.android.com) powered mobile phone. 

Specifically the IOIO board has two buttons attached, one red the other green, this software detects when one of the buttons is pressed and automatically makes a phone call to a predetermined number.

## Compiling the Software ##

To successfully compile the Serval Herbert software you must:

* Download the [IOIOLib](https://github.com/ytai/ioio/tree/master/software/IOIOLib) software project
* Add the IOIOLib project in Eclipse as an [Android Library](http://developer.android.com/tools/projects/projects-eclipse.html#ReferencingLibraryProject) to the Serval Herbert software project
* Ensure that both the IOIOLib and Serval Herbert projects in Eclipse are configured to explicitly use the Java 1.6 compiler

More information on the IOIO library is available in the [IOIO Wiki](https://github.com/ytai/ioio/wiki)

## Using the Software ##

The Serval Herbert software assumes that there are two switches connected to the IOIO bard. The red switch is connected to pin 35, and the green switch is connected to pin 34. If different pins are used these values can be changed in the CoreService.java file.

The two phone numbers that Serval Herbert will call when a button is pressed are configured using the Settings Activity which is available by pressing the Settings button on the Main Activity

## Known Limitations ##

There are some known limitations to the software in its current form

1. Making phone calls is a privileged operation and as such it can't be done programmatically when the screen is off
2. Finishing a call, hanging up, is a privileged operation and can't be undertaken programmatically

## More Information ##

For more information about the Serval Herbert software please contact [admin@servalproject.org](mailto:admin@servalproject.org)

