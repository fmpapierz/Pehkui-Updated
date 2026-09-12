package virtuoel.pehkui.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import virtuoel.pehkui.Pehkui;
import virtuoel.pehkui.client.PehkuiConfigScreen;

/**
 * Hands NeoForge the config screen, which is what turns the greyed-out button on the mod's entry in
 * the mod list into a working one. Restricted to the client, since the screen is client-only.
 */
@Mod(value = Pehkui.MOD_ID, dist = Dist.CLIENT)
public final class PehkuiNeoForgeClient
{
	public PehkuiNeoForgeClient(final ModContainer container)
	{
		container.registerExtensionPoint(IConfigScreenFactory.class, (mod, parent) -> new PehkuiConfigScreen(parent));
	}
}
