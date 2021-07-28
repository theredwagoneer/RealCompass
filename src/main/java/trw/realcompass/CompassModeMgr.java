package trw.realcompass;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.ibm.icu.text.MessageFormat;

import net.minecraft.client.Minecraft;

// Angle and axis translator for mincraft (From F3 screen)
// N = 180 = -Z;
// E = -90 = +X;
// S = 0   = +Z;
// W = 90  = -X;

/** 
 * Object to manage which mode the compass is in and how it computes
 * direction while it is in it.
 * @author theredwagoneer
 *
 */
class CompassModeMgr {
	/** Client Minecraft instance */
	private static final Minecraft mc = Minecraft.getInstance();
	
	/** Gson instance for reading and writing the location file */
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	/** Queue of modes.  The leading mode is the current mode.  It gets moved
	 * to the back when the mode is changed.
	 */
	private final Queue<ICompassMode> modeQueue = new ArrayDeque<ICompassMode>();
	
	/** List of the modes that are saved to the location file. */
	private ArrayList<SavedLocationMode> modeSaveList;
	
	/** Name of the json file to save the locations to */
	private String saveFileName;
	
	/**
	 * Constructor
	 * @param filename - Name of file to save locations to
	 */
	CompassModeMgr(String filename)		
	{ 
		saveFileName = filename;
		try {
			modeSaveList = gson.fromJson(new FileReader(saveFileName),new TypeToken<ArrayList<SavedLocationMode>>(){}.getType());
		} catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
			// If we can't load it, build it generically
			modeSaveList = new ArrayList<SavedLocationMode>();
			modeSaveList.add(new SavedLocationMode("Location 0"));
			modeSaveList.add(new SavedLocationMode("Location 1"));
			modeSaveList.add(new SavedLocationMode("Location 2"));
			modeSaveList.add(new SavedLocationMode("Location 3"));
			modeSaveList.add(new SavedLocationMode("Location 4"));
		}
		
		// Add the special ones
		modeQueue.add(new NorthMode());
		modeQueue.add(new OffMode());
		
		// Add the save Modes to the Queue
		for ( SavedLocationMode loc : modeSaveList )
		{
			modeQueue.add(loc);
		}
		
	};
	
	/**
	 * Move to the next mode.
	 * @return The message to print when making the switch to the next mode
	 */
	String next()
	{
		ICompassMode oldMode = modeQueue.remove();
		modeQueue.add(oldMode);
		return modeQueue.element().getSwitchString();
	}
	
	/**
	 * Informs if this is the off mode so special action can be taken.
	 * @return true if it is the off mode
	 */
	boolean isOffMode()
	{
		return modeQueue.element().isOffMode();
	}
	
	/**
	 * Computes the direction the compass should point
	 * @return Direction to point, in degrees.
	 */
	float computeDirection()
	{
		return modeQueue.element().computeDirection();
	}
	
	/** 
	 * Saves the current location if it is allowed to be saved in this mode
	 * @return Message to display upon saving.
	 */
	String saveCurrentLocation()
	{
		String retString = modeQueue.element().saveCurrentLocation();
		
		try {
			FileWriter out = new FileWriter(saveFileName);
			String saveString = gson.toJson(modeSaveList);
			out.write(saveString);
			out.close();
			
		} catch (JsonIOException | IOException e) {
			e.printStackTrace();
		}
		
		return retString;
	}
	
	/**
	 * Interface for the modes
	 * @author theredwagoneer
	 *
	 */
	private interface ICompassMode
	{	
		/**
		 * Is this the off mode?
		 * @return true if off mode
		 */
		public boolean isOffMode();
		
		/**
		 * Save the current location
		 * @return message to display
		 */
		public String saveCurrentLocation();
		/**
		 * Get the string that is displayed when we switch to this mode
		 * @return message to display
		 */
		public String getSwitchString();
		
		/** 
		 * Compute the direction to point the compass
		 * @return direction to point the compass in degrees
		 */
	    public float computeDirection();
	}
	
	/**
	 * Compass is turned off and deenergized
	 * @author theredwagoneer
	 *
	 */
	private class OffMode implements ICompassMode
	{	
		public boolean isOffMode()
		{
			return true;
		}
		public String saveCurrentLocation()
		{
			return "Cannot save location while Compass is off";
		}
		public String getSwitchString()
		{
			return "Compass Off";
		}
	    public float computeDirection()
	    {
	    	return (float)0;	
	    }
	}
	
	/**
	 * Compass is pointing North
	 * @author theredwagoneer
	 *
	 */
	private class NorthMode implements ICompassMode
	{
		public boolean isOffMode()
		{
			return false;
		}
		public String saveCurrentLocation()
		{
			return "Cannot save location to North Pointing Compass";
		}
		public String getSwitchString()
		{
			return "Compass Pointing North";
		}
	    public float computeDirection()
	    {
	    	return (float)(180 - mc.player.yRot);	
	    }
	}
	
	/**
	 * Compass points to a previously saved location
	 * @author theredwagoneer
	 *
	 */
	private class SavedLocationMode implements ICompassMode
	{
		private double xsaved = 0;
		private double zsaved = 0;
		private boolean hasSavedLocation = false;
		private boolean writeProtect = true;
		private String locationText;
	    
		SavedLocationMode(String locationText)
		{
			this.locationText = locationText;
		}
		public boolean isOffMode()
		{
			return false;
		}
		public String saveCurrentLocation()
		{
			xsaved = mc.player.xo;
			zsaved = mc.player.zo;
			hasSavedLocation = true;
			
			if (writeProtect == false)
			{
				return MessageFormat.format("({0},{1} saved to {2}",xsaved,zsaved,locationText);
			}
			else
			{
				return MessageFormat.format("({0} has been write protected.  You cannot save to this location",locationText);
			}
		}
		
		public String getSwitchString()
		{
			if (hasSavedLocation)
			{
				return MessageFormat.format("Compass Pointing to {0}: ({1},{2})",locationText,xsaved,zsaved);
			}
			else
			{
				return MessageFormat.format("{0} selected, but no location saved",locationText);
			}
		}
		
	    public float computeDirection()
	    {
	    	double tanTheta;
	    	double theta = 0;
	    	double deltaZ = zsaved - mc.player.zo ;
	    	double deltaX = mc.player.xo - xsaved;
	    	
	    	if (!hasSavedLocation)
	    	{
	    		return 0;
	    	}
	    	
	    	if (deltaZ == 0) {
	    		// Deal with divide by 0
	    		if (deltaX < 0)
	    		{
	    			if (deltaX > 0)
	    			{
	    				theta = 90;
	    			}
	    			else
	    			{
	    				theta = -90;
	    			}
	    		}
	    	}
	    	else
	    	{
	    		tanTheta = deltaX / deltaZ;
	    		theta = Math.toDegrees(Math.atan(tanTheta));
	    	}
	    	
	    	if(deltaZ < 0)
	    	{
	    		theta += 180;
	    	}
	    	
	    	return (float)(theta - mc.player.yRot);	
	    }	
	}
}