package virtuoel.pehkui.fabric;

import com.mojang.brigadier.arguments.ArgumentType;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.resources.Identifier;
import virtuoel.pehkui.Pehkui;
import virtuoel.pehkui.network.ConfigSyncPayload;
import virtuoel.pehkui.network.DebugPayload;
import virtuoel.pehkui.network.ScalePayload;
import virtuoel.pehkui.util.CommandUtils;
import virtuoel.pehkui.util.ConfigSyncUtils;

public class PehkuiFabric implements ModInitializer
{
	@Override
	public void onInitialize()
	{
		Pehkui.init();
		
		CommandUtils.registerArgumentTypes(new CommandUtils.ArgumentTypeConsumer()
		{
			@Override
			public <T extends ArgumentType<?>> void register(Identifier id, Class<T> argClass, java.util.function.Supplier<T> supplier)
			{
				ArgumentTypeRegistry.registerArgumentType(id, argClass, SingletonArgumentInfo.contextFree(supplier));
			}
		});
		
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> CommandUtils.registerCommands(dispatcher));
		
		PayloadTypeRegistry.clientboundPlay().register(ScalePayload.TYPE, ScalePayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(ConfigSyncPayload.TYPE, ConfigSyncPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(DebugPayload.TYPE, DebugPayload.CODEC);
		
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
		{
			if (!server.isSingleplayerOwner(handler.player.nameAndId()))
			{
				ConfigSyncUtils.syncConfigs(handler.player);
			}
			else
			{
				ConfigSyncUtils.resetSyncedConfigs();
			}
		});
	}
}
