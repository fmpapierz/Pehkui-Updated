package virtuoel.pehkui.fabric;

import org.spongepowered.asm.mixin.MixinEnvironment;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.network.ConfigSyncPayload;
import virtuoel.pehkui.network.DebugPayload;
import virtuoel.pehkui.network.ScalePayload;
import virtuoel.pehkui.server.command.DebugCommand;
import virtuoel.pehkui.util.I18nUtils;

public class PehkuiFabricClient implements ClientModInitializer
{
	@Override
	public void onInitializeClient()
	{
		ClientPlayNetworking.registerGlobalReceiver(ScalePayload.TYPE, (payload, context) ->
		{
			final Minecraft client = context.client();
			
			client.execute(() ->
			{
				final Entity e = client.level == null ? null : client.level.getEntity(payload.entityId);
				
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
			});
		});
		
		ClientPlayNetworking.registerGlobalReceiver(ConfigSyncPayload.TYPE, (payload, context) -> context.client().execute(payload.action));
		
		ClientPlayNetworking.registerGlobalReceiver(DebugPayload.TYPE, (payload, context) ->
		{
			final Minecraft client = context.client();
			
			client.execute(() -> handleDebugPacket(client, payload.packetType));
		});
	}
	
	private static void handleDebugPacket(final Minecraft client, final DebugCommand.PacketType type)
	{
		if (type == null || client.player == null)
		{
			return;
		}
		
		switch (type)
		{
			case MIXIN_AUDIT:
				client.player.sendSystemMessage(I18nUtils.translate("commands.pehkui.debug.audit.start.client", "Starting Mixin environment audit (client)..."));
				MixinEnvironment.getCurrentEnvironment().audit();
				client.player.sendSystemMessage(I18nUtils.translate("commands.pehkui.debug.audit.end.client", "Mixin environment audit (client) complete!"));
				break;
			case GARBAGE_COLLECT:
				System.gc();
				break;
			default:
				break;
		}
	}
}
