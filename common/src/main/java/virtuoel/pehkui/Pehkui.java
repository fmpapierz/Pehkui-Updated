package virtuoel.pehkui;

import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.service.MixinService;

import net.minecraft.resources.Identifier;
import virtuoel.pehkui.api.PehkuiConfig;
import virtuoel.pehkui.api.ScaleOperations;
import virtuoel.pehkui.api.ScaleTypes;
import virtuoel.pehkui.command.PehkuiEntitySelectorOptions;
import virtuoel.pehkui.util.GravityChangerCompatibility;
import virtuoel.pehkui.util.IdentityCompatibility;
import virtuoel.pehkui.util.ImmersivePortalsCompatibility;
import virtuoel.pehkui.util.ReachEntityAttributesCompatibility;

@ApiStatus.Internal
public final class Pehkui
{
	public static final String MOD_ID = "pehkui";
	
	public static final ILogger LOGGER = MixinService.getService().getLogger(MOD_ID);
	
	public static final Identifier SCALE_PACKET = id("scale");
	public static final Identifier CONFIG_SYNC_PACKET = id("config_sync");
	public static final Identifier DEBUG_PACKET = id("debug");
	
	private static boolean initialized = false;
	
	/**
	 * Loader-independent setup. Every loader entrypoint calls this exactly once, before
	 * registering its own networking and event hooks.
	 */
	public static synchronized void init()
	{
		if (initialized)
		{
			return;
		}
		
		initialized = true;
		
		ScaleTypes.INVALID.getClass();
		ScaleOperations.NOOP.getClass();
		PehkuiConfig.BUILDER.config.get();
		
		PehkuiEntitySelectorOptions.register();
		
		GravityChangerCompatibility.INSTANCE.getClass();
		IdentityCompatibility.INSTANCE.getClass();
		ImmersivePortalsCompatibility.INSTANCE.getClass();
		ReachEntityAttributesCompatibility.INSTANCE.getClass();
	}
	
	public static Identifier id(String path)
	{
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
	
	public static Identifier id(String path, String... paths)
	{
		return id(paths.length == 0 ? path : path + "/" + String.join("/", paths));
	}
	
	private Pehkui()
	{
		
	}
}
