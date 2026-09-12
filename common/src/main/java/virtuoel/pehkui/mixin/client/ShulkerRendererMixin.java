package virtuoel.pehkui.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.entity.ShulkerRenderer;
import net.minecraft.client.renderer.entity.state.ShulkerRenderState;
import net.minecraft.core.Direction;
import virtuoel.pehkui.util.PehkuiRenderStateExtensions;

/**
 * A shulker on a wall or ceiling is drawn from its attachment face, so growing it has to shift
 * the model back against that face.
 */
@Mixin(ShulkerRenderer.class)
public class ShulkerRendererMixin
{
	@Inject(at = @At("RETURN"), method = "setupRotations")
	private void pehkui$setupRotations(ShulkerRenderState state, PoseStack poseStack, float bodyRot, float entityScale, CallbackInfo info)
	{
		final Direction face = state.attachFace;
		
		if (face != Direction.DOWN)
		{
			final PehkuiRenderStateExtensions scales = (PehkuiRenderStateExtensions) state;
			
			final float h = scales.pehkui_getModelHeightScale();
			
			if (face != Direction.UP)
			{
				final float w = scales.pehkui_getModelWidthScale();
				
				if (w != 1.0F || h != 1.0F)
				{
					poseStack.translate(0.0F, -((1.0F - w) * 0.5F) / w, -((1.0F - h) * 0.5F) / h);
				}
			}
			else if (h != 1.0F)
			{
				poseStack.translate(0.0F, -(1.0F - h) / h, 0.0F);
			}
		}
	}
}
