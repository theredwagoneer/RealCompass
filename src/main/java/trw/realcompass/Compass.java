package trw.realcompass;

import java.util.Timer;
import java.util.TimerTask;

import javax.usb.UsbDisconnectedException;
import javax.usb.UsbException;

import net.minecraft.client.Minecraft;
import trw.pololu.tic.*;

/**
 * Controls the actual interaction between the game and the compass hardware
 * 
 * We send commands to the compass HW on a periodic basis.
 * 
 * @author theredwagoneer
 *
 */
class Compass {
	/** Timer to update compass with new commands */
	private static final Timer UPDATE_TIMER = new Timer("CompassUpdateTimer",true);
	
	/** Client Minecraft instance */
    private static final Minecraft MC = Minecraft.getInstance();
    
    /** Interface to the TIC */
    private final TicInterface tic = new TicInterface();
    
    /** Link to the compass mode */
    private final CompassModeMgr COMPASS_MODE;
    
    /** Home compass on next update */
    private boolean setHomeFlag = false;

    /** Denergize compass on next update */
    private boolean deenergizeFlag = false;
    
    /** Energize compass on next update */
    private boolean energizeFlag = false;
    
    /**
     * Task to periodically run when to update the compass position and
     * pass in any pending commands.
     */
    private final TimerTask updateTask = new TimerTask() {
    	public void run() {	
    		try
    		{
	    		if (MC.player != null)
	    		{
	    			if (deenergizeFlag == true)
	        		{
	        			TicCmd.DEENERGIZE.Send(tic);
	        			deenergizeFlag = false;
	        		}
	        		if (energizeFlag == true)
	        		{
	        			TicCmd.ENERGIZE.Send(tic);
	        			energizeFlag = false;
	        		}
	    			if (setHomeFlag == true)
	    			{
	    				tic.setHome();
	    				setHomeFlag = false;
	    			}
	    			setDirection( COMPASS_MODE.computeDirection() );
	    		}
    		}
    		catch (UsbDisconnectedException | UsbException e)
    		{
    			// Swallow.
    		}
        }
    };
	
    /**
     * Constructor
     * 
     * Apply the motor settings and start the periodic updates
     * 
     * @param mode - The instance of the compass mode manager to link to this
     * 			compass instance
     */
    public Compass(CompassModeMgr mode)
	{
    	COMPASS_MODE = mode;
    	   	
		tic.applySettings(CompassConst.MOTOR_SETTINGS);
		
		UPDATE_TIMER.schedule(updateTask, 0, CompassConst.UPDATE_PERIOD_MS);
    }

    /**
     * Home the compass
     */
    public void setHome()
	{
		this.setHomeFlag = true;
	}
    
    /**
     * Energize the compass
     */
    public void energize()
    {
    	this.energizeFlag = true;
    }
    
    /**
     * Deenergize the compass
     */
    public void deenergize()
    {
    	this.deenergizeFlag = true;
    }
	
	/**
	 * Set the direction of the compass.
	 * 
	 * The needed direction is set by the compass mode.  However, because the motor
	 * turns in a circle, it can be at way more than 360 degrees.  This finds the 
	 * instance of the correct direction that is closest to where we are now.
	 * 
	 * @param targetDegrees - Direction to point in degrees.
	 */
	private void setDirection(float targetDegrees)
	{
		final int POSITION_LIMIT = 2000000000;
		try
		{
			long ticPosition =  TicVar.CURRENT_POSITION.get(tic);
			
			// We reset tic position if we have spun so much we will overflow.
			// I've never actually tested this, so it may not work.
			// TODO Test this
			if ( ticPosition > POSITION_LIMIT || ticPosition < -POSITION_LIMIT )
			{
					TicCmd.SET_TARGET_VELOCITY.Send(tic,0);
					while( 0 != TicVar.CURRENT_VELOCITY.get(tic) )
					{
						// Wait until it stops
					}
					// Set new position where ever we are mod full rev
					
					ticPosition = TicVar.CURRENT_POSITION.get(tic);
					ticPosition %= CompassConst.MICROSTEPS_PER_REV;
					TicCmd.HALT_AND_SET_POSITION.Send(tic,(int)ticPosition);
			}
			
			// Now tell the tic to go to the position.  We need to find the closest
			// correct position to get sane behavior.		
			int targetPosition = (int)(targetDegrees * CompassConst.MICROSTEPS_PER_DEG);
			targetPosition %= CompassConst.MICROSTEPS_PER_REV;
			
			// Two possible new positions.  One to the left, one to the right
			int newPosition1 = ((int)ticPosition / CompassConst.MICROSTEPS_PER_REV) * 
								CompassConst.MICROSTEPS_PER_REV + targetPosition;
			int newPosition2;
			
			if (ticPosition < newPosition1 ) 
			{
				newPosition2 = newPosition1 - CompassConst.MICROSTEPS_PER_REV;
			}
			else
			{
				newPosition2 = newPosition1 + CompassConst.MICROSTEPS_PER_REV;
			}

		    if ( Math.abs(newPosition1 - ticPosition) < Math.abs(newPosition2 - ticPosition) )
		    {
		    	TicCmd.SET_TARGET_POSITION.Send(tic,newPosition1);
		    }
		    else
		    {
		    	TicCmd.SET_TARGET_POSITION.Send(tic,newPosition2);
		    }
		}
		catch (UsbDisconnectedException | UsbException e) 
		{
			// Deliberately swallow
		}
			
	}
	
	
}