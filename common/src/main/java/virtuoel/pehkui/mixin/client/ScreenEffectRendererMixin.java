package virtuoel.pehkui.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.world.entity.player.Player;
import virtuoel.pehkui.util.ScaleUtils;

/**
 * The check for whether the camera is inside a block probes a tenth of a block above and below the
 * eyes. That is a fixed distance, and once a player is shrunk past about a thirtieth of normal size
 * their eyes sit closer to the floor than that, so the probe reached into the block they were
 * standing on and the view filled with its texture. The probe now follows the eye height.
 *
 * <p>Forge and NeoForge each split the check in two so it can report the position alongside the
 * state, which moves the loop out of the method vanilla keeps it in, and they picked different
 * names for the half that holds it. All three are listed so that whichever one the loader has gets
 * patched.
 */
@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin
{
	@ModifyExpressionValue(
		method = { "getViewBlockingState", "getViewBlockingStateAndPos", "getOverlayBlock" },
		at = @At(value = "CONSTANT", args = "floatValue=0.1F")
	)
	private static float pehkui$getViewBlockingState$eyeProbe(float value, Player player)
	{
		final float scale = ScaleUtils.getEyeHeightScale(player);

		return scale != 1.0F ? value * scale : value;
	}
}
