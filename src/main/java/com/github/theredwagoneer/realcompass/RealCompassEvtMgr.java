package com.github.theredwagoneer.realcompass;

import java.io.File;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * This is where we really get start up the mod.
 * 
 * We instantiate the objects that will control the compass and
 * service key presses. 
 * 
 * @author theredewagoneer
 *
 */
class RealCompassEvtMgr
{
	/** Hook into the client side minecraft so we can get player position */
	private static Minecraft MC;
	
	/** Instance of the compass mode manager for controlling the mode */
    private static CompassModeMgr COMPASS_MODE;
    
    /** Instance of the Compass object which actually interacts with the
     * motor controller in the physical compass.
     */
    private static Compass COMPASS;
    
    /** Key binding for the homing function */
    public static KeyBinding KB_COMPASS_HOME;
    
    /** Key Binding to cycle the compass modes/locations */
	public static KeyBinding KB_COMPASS_FUNCTION;
	
	/** Key Binding to sace locations. */
	public static KeyBinding KB_COMPASS_SAVE;
	
	/**
	 * Called by the main class to register the listeners in this class
	 */
	void register() {
    	// Register the setup method to the Mode Event Bus
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientSetup);
    
		// Register the ClientTick to the Forge Event Bus
		MinecraftForge.EVENT_BUS.addListener(this::ClientTick);
		MinecraftForge.EVENT_BUS.addListener(this::playerLoad);
		
	}
	
	/**
	 * Sets up the Mod by registering the key bindings and instantiating 
	 * the compass interface and mode manager.
	 * @param event - Client Setup Event
	 */
	void doClientSetup(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
		
		KB_COMPASS_HOME = new KeyBinding("Home the Compass", 'H', "Compass Controls");
		KB_COMPASS_FUNCTION = new KeyBinding("Cycle Compass Function", 'Y', "Compass Controls");
		KB_COMPASS_SAVE = new KeyBinding("Save Location", 'V', "Compass Controls");
		
		ClientRegistry.registerKeyBinding(KB_COMPASS_HOME);
		ClientRegistry.registerKeyBinding(KB_COMPASS_FUNCTION);
		ClientRegistry.registerKeyBinding(KB_COMPASS_SAVE);
		
		 MC = Minecraft.getInstance();       
    }

	/**
	 * Instantiates the compass when a player is loaded
	 * @param event - The player load event
	 */
	public void playerLoad(PlayerEvent.LoadFromFile event) {
		File filename = new File(event.getPlayerDirectory(),event .getPlayerUUID()+"-CompassLocations.json");
		COMPASS_MODE = new CompassModeMgr(filename);
		COMPASS = new Compass(COMPASS_MODE);
	}
	
	
	/**
	 * Monitors for key presses and activates compass functions in reaction.
	 * @param event - Client tick event
	 */
	public void ClientTick(TickEvent.ClientTickEvent event) {
		
		if (event.phase == Phase.START) {
			
			if (KB_COMPASS_HOME.consumeClick()) 
			{
				COMPASS.setHome();
				TextComponent msg = new StringTextComponent("Compass Homed...");
				MC.gui.getChat().addMessage(msg);	
			} 
			else if (KB_COMPASS_FUNCTION.consumeClick())
			{
				String resp = COMPASS_MODE.next();
				TextComponent msg = new StringTextComponent(resp);
				MC.gui.getChat().addMessage(msg);
				
				if(COMPASS_MODE.isOffMode())
				{
					COMPASS.deenergize();
				}
				else
				{
					COMPASS.energize();
				}
				
			}
			else if (KB_COMPASS_SAVE.consumeClick())
			{
				String resp = COMPASS_MODE.saveCurrentLocation();
				TextComponent msg = new StringTextComponent(resp);
				MC.gui.getChat().addMessage(msg);
			}	
		}
	}
}


