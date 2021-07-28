# RealCompass

The Real Compass Mod adds a real world accessory to Minecraft, a compass that sits on your desk!

In order to use this client only Mod, you need to build the compass out of a Pololu TIC and a 28BYJ-48 stepper motor.

## Usage

When you first boot the mod, you need to home the motor.  Turn the view until the compass points "north", regardless of where you are actually pointed in game, then press "H."  Repeat this process any time the compass seems to have lost it's way.

Use "Y" to cycle through the various modes:
	- Point North
	- Turn off to save power
	- Point to location 1
	- Point to location 2
	- etc...

When a location is selected, press 'V' to save your current location.  The compass will now point to this location whenever it is selected (like a lodestone).  Pressing 'V' again will overwrite the location.

## Advanced Use

On first saving a location, the mod creates a file "CompassLocations.java" to save all the location.  This can be editted manually to customize the accessible locations.  This should be done with care as you can crash the mod if not careful.
Examples:
	- Edit locationText to give the location a real name instead of "location #"
	- Set the writeProtect to "true" to prevent accidently overwriting this location
	- Expand the array to add locations.

## License

MIT License
