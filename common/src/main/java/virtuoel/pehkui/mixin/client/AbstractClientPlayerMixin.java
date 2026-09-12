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
	 * How deep the bob swings, in both first and third person - they share this one value. Vanilla
	 * measures it from the velocity, which the mod leaves alone, so the bob was the same depth at
	 * every size. It now shrinks along with the entity and is left alone above normal size, where
	 * vanilla's own cap already holds it steady.
	 */
	@ModifyExpressionValue(method = "updateBob", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;horizontalDistance()D"))
	private double pehkui$updateBob$distance(double value)
	{
		final float scale = ScaleUtils.getMotionScale((Entity) (Object) this);

		return scale < 1.0F ? value * scale : value;
	}
}
