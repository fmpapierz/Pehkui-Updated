package virtuoel.pehkui.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin
{
	@ModifyExpressionValue(method = "aiStep", at = @At(value = "CONSTANT", args = "floatValue=3.0F"))
	private float pehkui$aiStep$flightSpeed(float value)
	{
		final float scale = ScaleUtils.getFlightScale((Entity) (Object) this);
		
		return scale != 1.0F ? scale * value : value;
	}
	
	@ModifyExpressionValue(method = "updateAutoJump", at = { @At(value = "CONSTANT", args = "floatValue=1.2F"), @At(value = "CONSTANT", args = "floatValue=0.75F") })
	private float pehkui$updateAutoJump$heightAndBoost(float value)
	{
		final float scale = ScaleUtils.getMotionScale((Entity) (Object) this);
		final float jumpScale = ScaleUtils.getJumpHeightScale((Entity) (Object) this);
		
		return scale != 1.0F || jumpScale != 1.0F ? scale * jumpScale * value : value;
	}
	
	/**
	 * The client only reports a position change once it exceeds this threshold, which a shrunken
	 * player's whole stride can fall under.
	 */
	@ModifyExpressionValue(method = "sendPosition", at = @At(value = "CONSTANT", args = "doubleValue=2.0E-4D"))
	private double pehkui$sendPosition$minVelocity(double value)
	{
		final float scale = ScaleUtils.getMotionScale((Entity) (Object) this);
		
		return scale < 1.0F ? scale * value : value;
	}
}
