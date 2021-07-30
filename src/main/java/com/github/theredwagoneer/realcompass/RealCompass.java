package com.github.theredwagoneer.realcompass;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.FMLNetworkConstants;

/**
 * Entry point for the Real Compass Mod.
 * 
 * This is a Client side mode, so it doesn't really do anything except 
 * spin up the Evt Manager in a safe call.
 * 
 * @author theredwagoneer
 *
 */
@Mod("realcompass")
public class RealCompass
{
	private static RealCompassEvtMgr evtMgr = new RealCompassEvtMgr();
	
	/**
	 * Constructor.
	 * 
	 * Don't reject server connections that don't have the Mod.
	 * 
	 * Then register the method handling events in the Evt Mgr.
	 * 
	 */
	public RealCompass() {
		//Make sure the mod being absent on the other network side does not cause the client to display the server as incompatible
		ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
		
		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> evtMgr::register );

	}
	
	
}


