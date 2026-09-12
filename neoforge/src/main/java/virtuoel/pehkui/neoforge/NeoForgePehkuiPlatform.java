package virtuoel.pehkui.neoforge;

import java.nio.file.Path;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.network.PacketDistributor;
import virtuoel.pehkui.util.PehkuiPlatform;

public class NeoForgePehkuiPlatform implements PehkuiPlatform
{
	@Override
	public boolean isModLoaded(final String modId)
	{
		return ModList.get() != null && ModList.get().isLoaded(modId);
	}
	
	@Override
	public boolean isDevelopmentEnvironment()
	{
		return !FMLLoader.getCurrent().isProduction();
	}
	
	@Override
	public Path getConfigDirectory()
	{
		return FMLPaths.CONFIGDIR.get();
	}
	
	@Override
	public String getLoaderName()
	{
		return "NeoForge";
	}

	@Override
	public void sendToTrackingAndSelf(final Entity entity, final CustomPacketPayload payload)
	{
		PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, payload);
	}

	@Override
	public void sendToPlayer(final ServerPlayer player, final CustomPacketPayload payload)
	{
		PacketDistributor.sendToPlayer(player, payload);
	}
}
