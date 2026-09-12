package virtuoel.pehkui.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Clientbound payloads only ever arrive on a client, but the handler methods themselves live in a
 * class the dedicated server loads, so the client lookup is kept behind this side-gated helper.
 */
public final class ForgeClientHooks
{
	static Player localPlayer()
	{
		return net.minecraftforge.fml.loading.FMLEnvironment.dist == Dist.CLIENT ? clientPlayer() : null;
	}
	
	@OnlyIn(Dist.CLIENT)
	private static Player clientPlayer()
	{
		return Minecraft.getInstance().player;
	}
	
	private ForgeClientHooks()
	{
		
	}
}
