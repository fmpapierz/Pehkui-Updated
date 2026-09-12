package virtuoel.pehkui.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import virtuoel.pehkui.client.PehkuiConfigScreen;

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

	/**
	 * Hands Forge the config screen, which is what turns the greyed-out button on the mod's entry in
	 * the mod list into a working one.
	 */
	static void registerConfigScreen(final ModLoadingContext context)
	{
		if (net.minecraftforge.fml.loading.FMLEnvironment.dist == Dist.CLIENT)
		{
			registerConfigScreenFactory(context);
		}
	}

	@OnlyIn(Dist.CLIENT)
	private static void registerConfigScreenFactory(final ModLoadingContext context)
	{
		context.registerExtensionPoint(
			ConfigScreenHandler.ConfigScreenFactory.class,
			() -> new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> new PehkuiConfigScreen(parent))
		);
	}
	
	private ForgeClientHooks()
	{
		
	}
}
