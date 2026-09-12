package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.api.ScaleType;

@Mixin(PlayerList.class)
public class PlayerListMixin
{
	/**
	 * A joining player has not been told about its own scales yet, so everything is queued for
	 * the first sync tick.
	 */
	@Inject(method = "placeNewPlayer", at = @At("RETURN"))
	private void pehkui$placeNewPlayer(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo info)
	{
		for (ScaleType type : ScaleRegistries.SCALE_TYPES.values())
		{
			type.getScaleData(player).markForSync(true);
		}
	}
}
