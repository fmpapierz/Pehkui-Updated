package virtuoel.pehkui.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import virtuoel.pehkui.util.ScaleUtils;

/**
 * The walk bob drives both the camera and the first person hand. Its speed comes from the distance
 * walked and its depth from the distance moved in a tick, both measured in blocks, so at any size
 * but one the bob ran at the wrong pace: frantic when large, absent when small.
 */
@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin
{
	/**
	 * How far the bob has advanced, and so how fast it swings.
	 */
	@ModifyVariable(method = "addWalkedDistance", at = @At("HEAD"), argsOnly = true)
	private float pehkui$addWalkedDistance(float distance)
	{
		return ScaleUtils.modifyLimbDistance(distance, (Entity) (Object) this);
	}

	/**
	 * How deep the bob swings. Vanilla caps this a moment later, so only shrunken players notice.
	 */
	@ModifyExpressionValue(method = "updateBob", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;horizontalDistance()D"))
	private double pehkui$updateBob$distance(double value)
	{
		final float scale = ScaleUtils.getMotionScale((Entity) (Object) this);

		return scale != 1.0F ? value / scale : value;
	}
}
