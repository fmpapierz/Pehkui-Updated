package virtuoel.pehkui.server.command;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.UUID;

import org.spongepowered.asm.mixin.MixinEnvironment;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import virtuoel.pehkui.api.PehkuiConfig;
import virtuoel.pehkui.network.DebugPayload;
import virtuoel.pehkui.util.CommandUtils;
import virtuoel.pehkui.util.ConfigSyncUtils;
import virtuoel.pehkui.util.I18nUtils;
import virtuoel.pehkui.util.PehkuiPlatform;

public class DebugCommand
{
	public static void register(final CommandDispatcher<CommandSourceStack> commandDispatcher)
	{
		final LiteralArgumentBuilder<CommandSourceStack> builder =
			Commands.literal("scale")
			.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));

		builder.then(Commands.literal("debug")
			.then(ConfigSyncUtils.registerConfigCommands())
		);

		if (PehkuiPlatform.get().isDevelopmentEnvironment() || PehkuiConfig.COMMON.enableCommands.get())
		{
			builder
				.then(Commands.literal("debug")
					.then(Commands.literal("delete_scale_data")
						.then(Commands.literal("uuid")
							.then(Commands.argument("uuid", StringArgumentType.string())
								.executes(context ->
								{
									final String uuidString = StringArgumentType.getString(context, "uuid");

									try
									{
										MARKED_UUIDS.add(UUID.fromString(uuidString));
									}
									catch (IllegalArgumentException e)
									{
										context.getSource().sendFailure(I18nUtils.translate("commands.pehkui.debug.delete.uuid.invalid", "Invalid UUID \"%s\".", uuidString));
										return 0;
									}

									return 1;
								})
							)
						)
						.then(Commands.literal("username")
							.then(Commands.argument("username", StringArgumentType.string())
								.executes(context ->
								{
									MARKED_USERNAMES.add(StringArgumentType.getString(context, "username").toLowerCase(Locale.ROOT));

									return 1;
								})
							)
						)
					)
					.then(Commands.literal("garbage_collect")
						.executes(context ->
						{
							PehkuiPlatform.get().sendToPlayer(context.getSource().getPlayerOrException(), new DebugPayload(PacketType.GARBAGE_COLLECT));


							System.gc();

							return 1;
						})
					)
				);
		}

		if (PehkuiPlatform.get().isDevelopmentEnvironment() || PehkuiConfig.COMMON.enableDebugCommands.get())
		{
			builder
				.then(Commands.literal("debug")
					.then(Commands.literal("run_mixin_tests")
						.executes(DebugCommand::runMixinTests)
					)
				);
		}

		commandDispatcher.register(builder);
	}

	private static final Collection<UUID> MARKED_UUIDS = new HashSet<>();
	private static final Collection<String> MARKED_USERNAMES = new HashSet<>();

	public static boolean unmarkEntityForScaleReset(final Entity entity)
	{
		if (entity instanceof Player && MARKED_USERNAMES.remove(((Player) entity).getGameProfile().name().toLowerCase(Locale.ROOT)))
		{
			return true;
		}

		return MARKED_UUIDS.remove(entity.getUUID());
	}

	public static enum PacketType
	{
		MIXIN_AUDIT,
		GARBAGE_COLLECT
		;
	}

	private static int runMixinTests(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
	{
		final Entity executor = context.getSource().getEntity();
		if (executor instanceof ServerPlayer)
		{
			PehkuiPlatform.get().sendToPlayer((ServerPlayer) executor, new DebugPayload(PacketType.MIXIN_AUDIT));
		}

		CommandUtils.sendFeedback(context.getSource(), () -> I18nUtils.translate("commands.pehkui.debug.audit.start", "Starting Mixin environment audit..."), false);
		MixinEnvironment.getCurrentEnvironment().audit();
		CommandUtils.sendFeedback(context.getSource(), () -> I18nUtils.translate("commands.pehkui.debug.audit.end", "Mixin environment audit complete!"), false);

		return 1;
	}

	private DebugCommand()
	{

	}
}
