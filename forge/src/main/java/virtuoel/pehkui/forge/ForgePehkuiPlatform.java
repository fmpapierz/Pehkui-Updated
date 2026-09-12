package virtuoel.pehkui.forge;

import java.nio.file.Path;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.PacketDistributor;
import virtuoel.pehkui.util.PehkuiPlatform;

public class ForgePehkuiPlatform implements PehkuiPlatform
{
	@Override
	public boolean isModLoaded(final String modId)
	{
		return ModList.isLoaded(modId);
	}
	
	@Override
	public boolean isDevelopmentEnvironment()
	{
		return !FMLEnvironment.production;
	}
	
	@Override
	public Path getConfigDirectory()
	{
		return FMLPaths.CONFIGDIR.get();
	}
	
	@Override
	public String getLoaderName()
	{
		return "Forge";
	}
	
	@Override
	public void sendToTrackingAndSelf(final Entity entity, final CustomPacketPayload payload)
	{
		PehkuiForge.channel().send(payload, PacketDistributor.TRACKING_ENTITY_AND_SELF.with(entity));
	}
	
	@Override
	public void sendToPlayer(final ServerPlayer player, final CustomPacketPayload payload)
	{
		PehkuiForge.channel().send(payload, PacketDistributor.PLAYER.with(player));
	}
}
