package virtuoel.pehkui.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import virtuoel.pehkui.util.PehkuiRenderStateExtensions;

/**
 * The whole renderer call is wrapped rather than {@code EntityRenderer#submit} itself: subclasses
 * such as {@code LivingEntityRenderer} draw their model first and only call {@code super.submit}
 * afterwards for the name tag, so injecting into the base method would scale the label alone.
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin
{
	@WrapOperation(method = "submit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"))
	private void pehkui$submit(EntityRenderer<?, ?> renderer, EntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, Operation<Void> original)
	{
		final PehkuiRenderStateExtensions scales = (PehkuiRenderStateExtensions) state;
		
		final float width = scales.pehkui_getModelWidthScale();
		final float height = scales.pehkui_getModelHeightScale();
		
		if (width == 1.0F && height == 1.0F)
		{
			original.call(renderer, state, poseStack, submitNodeCollector, camera);
			
			return;
		}
		
		poseStack.pushPose();
		poseStack.scale(width, height, width);
		
		try
		{
			original.call(renderer, state, poseStack, submitNodeCollector, camera);
		}
		finally
		{
			poseStack.popPose();
		}
	}
}
