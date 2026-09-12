package virtuoel.pehkui.fabric;

import java.nio.file.Path;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import virtuoel.pehkui.util.PehkuiPlatform;

public class FabricPehkuiPlatform implements PehkuiPlatform
{
	@Override
	public boolean isModLoaded(final String modId)
	{
		return FabricLoader.getInstance().isModLoaded(modId);
	}
	
	@Override
	public boolean isDevelopmentEnvironment()
	{
		return FabricLoader.getInstance().isDevelopmentEnvironment();
	}
	
	@Override
	public Path getConfigDirectory()
	{
		return FabricLoader.getInstance().getConfigDir();
	}
	
	@Override
	public String getLoaderName()
	{
		return FabricLoader.getInstance().isModLoaded("quilt_loader") ? "Quilt" : "Fabric";
	}
	
	@Override
	public void sendToTrackingAndSelf(final Entity entity, final CustomPacketPayload payload)
	{
		if (!(entity.level() instanceof ServerLevel))
		{
			return;
		}
		
		for (final ServerPlayer viewer : PlayerLookup.tracking(entity))
		{
			sendToPlayer(viewer, payload);
		}
		
		if (entity instanceof ServerPlayer)
		{
			sendToPlayer((ServerPlayer) entity, payload);
		}
	}
	
	@Override
	public void sendToPlayer(final ServerPlayer player, final CustomPacketPayload payload)
	{
		if (ServerPlayNetworking.canSend(player, payload.type()))
		{
			ServerPlayNetworking.send(player, payload);
		}
	}
}
