package virtuoel.pehkui.neoforge;

import java.util.function.Supplier;

import com.mojang.brigadier.arguments.ArgumentType;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.RegisterEvent;
import virtuoel.pehkui.Pehkui;
import virtuoel.pehkui.network.ConfigSyncPayload;
import virtuoel.pehkui.network.DebugPayload;
import virtuoel.pehkui.network.ScalePayload;
import virtuoel.pehkui.util.CommandUtils;
import virtuoel.pehkui.util.ConfigSyncUtils;

@Mod(Pehkui.MOD_ID)
public final class PehkuiNeoForge
{
	public PehkuiNeoForge(IEventBus modBus)
	{
		Pehkui.init();
		
		modBus.addListener(RegisterEvent.class, this::onRegister);
		modBus.addListener(RegisterPayloadHandlersEvent.class, this::onRegisterPayloadHandlers);
		
		NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, event -> CommandUtils.registerCommands(event.getDispatcher()));
		NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, this::onPlayerLoggedIn);
	}
	
	private void onRegister(final RegisterEvent event)
	{
		event.register(Registries.COMMAND_ARGUMENT_TYPE, registry ->
		{
			CommandUtils.registerArgumentTypes(new CommandUtils.ArgumentTypeConsumer()
			{
				@Override
				public <T extends ArgumentType<?>> void register(Identifier id, Class<T> argClass, Supplier<T> supplier)
				{
					final ArgumentTypeInfo<T, ?> info = ArgumentTypeInfos.registerByClass(argClass, SingletonArgumentInfo.contextFree(supplier));
					
					registry.register(id, info);
				}
			});
		});
	}
	
	private void onRegisterPayloadHandlers(final RegisterPayloadHandlersEvent event)
	{
		final PayloadRegistrar registrar = event.registrar("1");
		
		registrar.playToClient(ScalePayload.TYPE, ScalePayload.CODEC, PehkuiNeoForgeNetworking::handleScale);
		registrar.playToClient(ConfigSyncPayload.TYPE, ConfigSyncPayload.CODEC, PehkuiNeoForgeNetworking::handleConfigSync);
		registrar.playToClient(DebugPayload.TYPE, DebugPayload.CODEC, PehkuiNeoForgeNetworking::handleDebug);
	}
	
	private void onPlayerLoggedIn(final PlayerEvent.PlayerLoggedInEvent event)
	{
		if (event.getEntity() instanceof ServerPlayer player)
		{
			final MinecraftServer server = player.level().getServer();
			
			if (server != null && !server.isSingleplayerOwner(player.nameAndId()))
			{
				ConfigSyncUtils.syncConfigs(player);
			}
			else
			{
				ConfigSyncUtils.resetSyncedConfigs();
			}
		}
	}
}
