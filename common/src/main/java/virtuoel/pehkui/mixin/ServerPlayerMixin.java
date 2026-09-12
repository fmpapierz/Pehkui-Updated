package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerPlayer;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin
{
	@Inject(at = @At("HEAD"), method = "restoreFrom")
	private void pehkui$restoreFrom(ServerPlayer oldPlayer, boolean restoreAll, CallbackInfo info)
	{
		ScaleUtils.loadScaleOnRespawn((ServerPlayer) (Object) this, oldPlayer, restoreAll);
	}
}
