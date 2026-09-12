package virtuoel.pehkui.forge;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.MixinEnvironment;

import com.mojang.brigadier.arguments.ArgumentType;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.registries.DeferredRegister;
import virtuoel.pehkui.Pehkui;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.network.ConfigSyncPayload;
import virtuoel.pehkui.network.DebugPayload;
import virtuoel.pehkui.network.ScalePayload;
import virtuoel.pehkui.util.CommandUtils;
import virtuoel.pehkui.util.ConfigSyncUtils;
import virtuoel.pehkui.util.I18nUtils;

@Mod(Pehkui.MOD_ID)
public final class PehkuiForge
{
	private static Channel<CustomPacketPayload> channel;
	
	static Channel<CustomPacketPayload> channel()
	{
		return channel;
	}
	
	public PehkuiForge(FMLJavaModLoadingContext context)
	{
		Pehkui.init();
		
		ForgeClientHooks.registerConfigScreen(context);
		
		final var modBus = context.getModBusGroup();
		
		final DeferredRegister<ArgumentTypeInfo<?, ?>> argumentTypes = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, Pehkui.MOD_ID);
		
		CommandUtils.registerArgumentTypes(new CommandUtils.ArgumentTypeConsumer()
		{
			@Override
			public <T extends ArgumentType<?>> void register(Identifier id, Class<T> argClass, Supplier<T> supplier)
			{
				argumentTypes.register(id.getPath(), () -> ArgumentTypeInfos.registerByClass(argClass, SingletonArgumentInfo.contextFree(supplier)));
			}
		});
		
		argumentTypes.register(modBus);
		
		channel = ChannelBuilder.named(Pehkui.id("main"))
			.networkProtocolVersion(1)
			.optional()
			.payloadChannel()
			.play()
			.clientbound()
			.addMain(ScalePayload.TYPE, ScalePayload.CODEC, PehkuiForge::handleScale)
			.addMain(ConfigSyncPayload.TYPE, ConfigSyncPayload.CODEC, PehkuiForge::handleConfigSync)
			.addMain(DebugPayload.TYPE, DebugPayload.CODEC, PehkuiForge::handleDebug)
			.build();
		
		RegisterCommandsEvent.BUS.addListener(event -> CommandUtils.registerCommands(event.getDispatcher()));
		PlayerEvent.PlayerLoggedInEvent.BUS.addListener(PehkuiForge::onPlayerLoggedIn);
	}
	
	private static void onPlayerLoggedIn(final PlayerEvent.PlayerLoggedInEvent event)
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
	
	private static void handleScale(final ScalePayload payload, final net.minecraftforge.event.network.CustomPayloadEvent.Context context)
	{
		final Player player = ForgeClientHooks.localPlayer();
		
		if (player == null)
		{
			return;
		}
		
		final Entity e = player.level().getEntity(payload.entityId);
		
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
	
	private static void handleConfigSync(final ConfigSyncPayload payload, final net.minecraftforge.event.network.CustomPayloadEvent.Context context)
	{
		if (payload.action != null)
		{
			payload.action.run();
		}
	}
	
	private static void handleDebug(final DebugPayload payload, final net.minecraftforge.event.network.CustomPayloadEvent.Context context)
	{
		final Player player = ForgeClientHooks.localPlayer();
		
		if (player == null || payload.packetType == null)
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
}
