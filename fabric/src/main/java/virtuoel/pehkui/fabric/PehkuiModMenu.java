package virtuoel.pehkui.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import virtuoel.pehkui.client.PehkuiConfigScreen;

/**
 * Hands Mod Menu the config screen, which is what turns the greyed-out button on the mod's entry
 * into a working one. Mod Menu itself is optional: nothing else looks this class up, so it is never
 * loaded when Mod Menu is absent.
 */
public class PehkuiModMenu implements ModMenuApi
{
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory()
	{
		return PehkuiConfigScreen::new;
	}
}
