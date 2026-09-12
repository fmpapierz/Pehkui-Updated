package virtuoel.pehkui.mixin.client;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import virtuoel.pehkui.util.ScaleRenderUtils;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(Camera.class)
public abstract class CameraMixin
{
	@Shadow private @Nullable Entity entity;
	
	/**
	 * Third person pulls the camera a fixed distance back, which puts it inside a giant and
	 * miles away from a tiny one.
	 */
	@ModifyVariable(method = "getMaxZoom", at = @At("HEAD"), argsOnly = true)
	private float pehkui$getMaxZoom(float cameraDist)
	{
		return cameraDist * ScaleUtils.getThirdPersonScale(this.entity, ScaleRenderUtils.getTickDelta());
	}
	
	/**
	 * The near clipping plane for the world. A shrunken player's eyes sit closer to the floor than
	 * the fixed five centimetre plane, which put the block underfoot behind the camera and let the
	 * view see straight through it, so the plane comes in with the player.
	 */
	@ModifyExpressionValue(method = "update", at = @At(value = "CONSTANT", args = "floatValue=0.05F"))
	private float pehkui$update$nearPlane(float value)
	{
		return ScaleRenderUtils.modifyProjectionMatrixDepth(value, this.entity, ScaleRenderUtils.getTickDelta());
	}

	@ModifyExpressionValue(method = "getMaxZoom", at = @At(value = "CONSTANT", args = "floatValue=0.1F"))
	private float pehkui$getMaxZoom$offset(float value)
	{
		final float scale = ScaleUtils.getBoundingBoxWidthScale(this.entity);
		
		return scale < 1.0F ? scale * value : value;
	}
}
