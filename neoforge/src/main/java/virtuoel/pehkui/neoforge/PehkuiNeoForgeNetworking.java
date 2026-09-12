package virtuoel.pehkui.neoforge;

import org.spongepowered.asm.mixin.MixinEnvironment;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.network.ConfigSyncPayload;
import virtuoel.pehkui.network.DebugPayload;
import virtuoel.pehkui.network.ScalePayload;
import virtuoel.pehkui.server.command.DebugCommand;
import virtuoel.pehkui.util.I18nUtils;

/**
 * Payload handlers run on the main thread, which is where the entity lookups below need to be.
 */
public final class PehkuiNeoForgeNetworking
{
	static void handleScale(final ScalePayload payload, final IPayloadContext context)
	{
		final Entity e = context.player().level().getEntity(payload.entityId);
		
		if (e != null)
		{
			payload.syncedScales.forEach((typeId, scaleData) ->
			{
				if (ScaleRegistries.SCALE_TYPES.containsKey(typeId))
				{
					ScaleRegistries.getEntry(ScaleRegistries.SCALE_TYPES, typeId).getScaleData(e).readNbt(scaleData);
				}
			});
		}
	}
	
	static void handleConfigSync(final ConfigSyncPayload payload, final IPayloadContext context)
	{
		if (payload.action != null)
		{
			payload.action.run();
		}
	}
	
	static void handleDebug(final DebugPayload payload, final IPayloadContext context)
	{
		final Player player = context.player();
		
		if (payload.packetType == null)
		{
			return;
		}
		
		switch (payload.packetType)
		{
			case MIXIN_AUDIT:
				player.sendSystemMessage(I18nUtils.translate("commands.pehkui.debug.audit.start.client", "Starting Mixin environment audit (client)..."));
				MixinEnvironment.getCurrentEnvironment().audit();
				player.sendSystemMessage(I18nUtils.translate("commands.pehkui.debug.audit.end.client", "Mixin environment audit (client) complete!"));
				break;
			case GARBAGE_COLLECT:
				System.gc();
				break;
			default:
				break;
		}
	}
	
	private PehkuiNeoForgeNetworking()
	{
		
	}
}
