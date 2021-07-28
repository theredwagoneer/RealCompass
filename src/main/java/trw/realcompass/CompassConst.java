package trw.realcompass;

import java.util.HashMap;
import java.util.Map;

import trw.pololu.tic.TicSet;

/**
 * Holds the constants controlling how the MOD interacts with the motor.
 * 
 * These constants work well with 28BYJ-48.  Different motors would 
 * require modification.
 * 
 * @author theredwagoneer
 *
 */
class CompassConst {
	/** Map of motor settings to be applied to the TIC */
	public static final Map<TicSet, Long> MOTOR_SETTINGS = new HashMap<>();
	
	/** Number of steps per revolution of the motor */
	public static final int FULLSTEPS_PER_REV = 2048;
	
	/** How often to update the motor position in ms */
	public static final int UPDATE_PERIOD_MS = 100;
	
	/** Number of microsteps per revolution */
	public static final int MICROSTEPS_PER_REV;
	
	/** Number of microsteps per degree */
	public static final float MICROSTEPS_PER_DEG;
	
	static 
	{
		MOTOR_SETTINGS.put(TicSet.CONTROL_MODE,               0L);
		MOTOR_SETTINGS.put(TicSet.NEVER_SLEEP,                0L);
		MOTOR_SETTINGS.put(TicSet.DISABLE_SAFE_START,         1L);
		MOTOR_SETTINGS.put(TicSet.IGNORE_ERR_LINE_HIGH,       0L);
		MOTOR_SETTINGS.put(TicSet.AUTO_CLEAR_DRIVER_ERROR,    1L);
		MOTOR_SETTINGS.put(TicSet.SOFT_ERROR_RESPONSE,        2L);
		MOTOR_SETTINGS.put(TicSet.SOFT_ERROR_POSITION,        0L);
		MOTOR_SETTINGS.put(TicSet.COMMAND_TIMEOUT,            0L);
		MOTOR_SETTINGS.put(TicSet.VIN_CALIBRATION,            0L);
		MOTOR_SETTINGS.put(TicSet.CURRENT_LIMIT,              3L);
		MOTOR_SETTINGS.put(TicSet.CURRENT_LIMIT_DURING_ERROR, 255L);
		MOTOR_SETTINGS.put(TicSet.DECAY_MODE,                 1L);
		MOTOR_SETTINGS.put(TicSet.AUTO_HOMING,                0L);
		MOTOR_SETTINGS.put(TicSet.AUTO_HOMING_FORWARD,        0L);
		MOTOR_SETTINGS.put(TicSet.HOMING_SPEED_TOWARDS,       1000000L);
		MOTOR_SETTINGS.put(TicSet.HOMING_SPEED_AWAY,          500000L);
		
		// Fill in these variables below with desired values in full steps.
		// Code will adjust for microstep mode.
		
		// NOTE: This doesn't work right.  In the TIC GUI, all the numbers
		// scale up with more microsteps, but it doesn't appear to happen
		// in the TIC itself?  Or else I'm doing something wrong.
		//
		// TODO: Improve microstep mode.
		// I like how the full step mode works with the 28BYJ-48, so I am 
		// not going to bother figuring out the deal with this yet
		
		long stepMode   = 0; 		 // 0 = 1, 1 = 1/2, 2 = 1/4, 3 = 1/8
		long maxSpeed   = 10000000;
	    long startSpeed = 0;
	    long maxAccel   = 80000;
	    long maxDecel   = 80000;
		
	    int scale = (int) Math.pow(2,stepMode);
		
		MOTOR_SETTINGS.put(TicSet.STEP_MODE,                  stepMode);
		MOTOR_SETTINGS.put(TicSet.MAX_SPEED,                  maxSpeed*scale);
		MOTOR_SETTINGS.put(TicSet.STARTING_SPEED,             startSpeed*scale);
		MOTOR_SETTINGS.put(TicSet.MAX_ACCEL,                  maxAccel*scale);
		MOTOR_SETTINGS.put(TicSet.MAX_DECEL,                  maxDecel*scale);
		
		MICROSTEPS_PER_REV = FULLSTEPS_PER_REV*scale;
		MICROSTEPS_PER_DEG = (float)MICROSTEPS_PER_REV/360;
	
	}
	
}