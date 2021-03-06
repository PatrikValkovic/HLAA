package hlaa.tdm.utils;

import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.NavigationGraphBuilder;
import hlaa.tdm.TDMBot;

/**
 * Class containing adjustments for navigation graph of PogamutCup competition maps.
 * 
 * @author Jimmy
 */
public class MapTweaks {

	/**
	 * Called from {@link TDMBot#botInitialized(cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GameInfo, cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ConfigChange, cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.InitedMessage)}.
	 * @param navBuilder
	 */
	public static void tweak(NavigationGraphBuilder navBuilder) {
		if (navBuilder.isMapName("DM-1on1-Roughinery-FPS")) tweakDM1on1RoughineryFPS(navBuilder);
		if (navBuilder.isMapName("DM-DE-Ironic-FE")) tweakDMDEIronicFE(navBuilder);
		if (navBuilder.isMapName("DM-Rankin-FE")) tweakDMRankinFE(navBuilder);
		
	}
	
	// ======================
	// DM-1on1-Roughinery-FPS
	// ======================
	
	private static void tweakDM1on1RoughineryFPS(NavigationGraphBuilder navBuilder) {
		navBuilder.modifyEdge("PathNode105", "JumpSpot10").removeJumpFlag();
		navBuilder.modifyEdge("PathNode105", "JumpSpot10").removeDoubleJump();
		navBuilder.modifyEdge("PathNode105", "PathNode54").removeJumpFlag();
		navBuilder.modifyEdge("PathNode105", "PathNode54").removeDoubleJump();
	}
	
	// ======================
	// DM-DE-Ironic-FE
	// ======================
	
	private static void tweakDMDEIronicFE(NavigationGraphBuilder navBuilder) {
		navBuilder.createSimpleEdge("PathNode47", "PathNode46");
		navBuilder.modifyEdge("PathNode47", "PathNode46").setDoubleJump();
		navBuilder.createSimpleEdge("PathNode49", "PathNode48");
		navBuilder.modifyEdge("PathNode49", "PathNode48").setDoubleJump();
	}

	// ======================
	// DM-Ranking-FE
	// ======================
	
	private static void tweakDMRankinFE(NavigationGraphBuilder navBuilder) {
		navBuilder.removeEdge("PathNode122", "JumpSpot1");
		navBuilder.removeEdge("InventorySpot166", "JumpSpot1");
		navBuilder.removeEdge("InventorySpot165", "JumpSpot1");

	}
	
}
