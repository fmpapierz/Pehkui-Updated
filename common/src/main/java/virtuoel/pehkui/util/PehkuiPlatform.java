package virtuoel.pehkui.util;

import java.nio.file.Path;
import java.util.ServiceLoader;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * The handful of things Pehkui needs that every mod loader spells differently.
 * Each loader project ships exactly one implementation, found through {@link ServiceLoader}.
 */
public interface PehkuiPlatform
{
	boolean isModLoaded(String modId);
	
	boolean isDevelopmentEnvironment();
	
	Path getConfigDirectory();
	
	String getLoaderName();
	
	/**
	 * Sends to everyone who can see the entity, and to the entity itself when it is a player.
	 */
	void sendToTrackingAndSelf(Entity entity, CustomPacketPayload payload);
	
	void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);
	
	static PehkuiPlatform get()
	{
		return Holder.INSTANCE;
	}
	
	final class Holder
	{
		private static final PehkuiPlatform INSTANCE = load();
		
		private static PehkuiPlatform load()
		{
			return ServiceLoader.load(PehkuiPlatform.class)
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("No PehkuiPlatform implementation was found on the classpath."));
		}
		
		private Holder()
		{
			
		}
	}
}
