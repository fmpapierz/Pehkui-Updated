package virtuoel.pehkui.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import virtuoel.pehkui.util.ScaleRenderUtils;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(GameRenderer.class)
public class GameRendererMixin
{
	@WrapOperation(method = "bobView", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"))
	private void pehkui$bobView$translate(PoseStack obj, float x, float y, float z, Operation<Void> original)
	{
		final Minecraft client = Minecraft.getInstance();
		
		final float scale = ScaleUtils.getViewBobbingScale(client.getCameraEntity(), ScaleRenderUtils.getTickDelta());
		
		if (scale != 1.0F)
		{
			x *= scale;
			y *= scale;
			z *= scale;
		}
		
		original.call(obj, x, y, z);
	}
	
	/**
	 * The near clipping plane is what makes a shrunken player see through the world, so it is
	 * pulled in along with the camera.
	 */
	@ModifyExpressionValue(method = "renderLevel", at = @At(value = "CONSTANT", args = "floatValue=0.05F"))
	private float pehkui$renderLevel$nearPlane(float value)
	{
		final Minecraft client = Minecraft.getInstance();
		
		return ScaleRenderUtils.modifyProjectionMatrixDepth(value, client.getCameraEntity(), ScaleRenderUtils.getTickDelta());
	}
}
