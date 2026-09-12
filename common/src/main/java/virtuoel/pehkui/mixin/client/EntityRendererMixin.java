package virtuoel.pehkui.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import virtuoel.pehkui.util.PehkuiRenderStateExtensions;
import virtuoel.pehkui.util.ScaleUtils;

/**
 * Rendering in 26.2 works off an extracted render state rather than off the entity, so the model
 * scales are captured here and read back by {@code EntityRenderDispatcherMixin} at submit time.
 */
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState>
{
	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void pehkui$extractRenderState(T entity, S state, float partialTicks, CallbackInfo info)
	{
		((PehkuiRenderStateExtensions) state).pehkui_setModelScales(
			ScaleUtils.getModelWidthScale(entity, partialTicks),
			ScaleUtils.getModelHeightScale(entity, partialTicks)
		);
	}
	
	/**
	 * A grown entity is drawn well outside its own bounding box, so culling has to consider the
	 * rendered size instead.
	 */
	@ModifyReturnValue(method = "getBoundingBoxForCulling", at = @At("RETURN"))
	private AABB pehkui$getBoundingBoxForCulling(AABB original, T entity)
	{
		final float width = ScaleUtils.getModelWidthScale(entity);
		final float height = ScaleUtils.getModelHeightScale(entity);
		
		if (width > 1.0F || height > 1.0F)
		{
			final double dX = original.getXsize() * 0.5D * (Math.max(width, 1.0F) - 1.0F);
			final double dY = original.getYsize() * 0.5D * (Math.max(height, 1.0F) - 1.0F);
			final double dZ = original.getZsize() * 0.5D * (Math.max(width, 1.0F) - 1.0F);
			
			return original.inflate(dX, dY, dZ);
		}
		
		return original;
	}
	
	@ModifyReturnValue(method = "getShadowRadius", at = @At("RETURN"))
	private float pehkui$getShadowRadius(float original, S state)
	{
		final float width = ((PehkuiRenderStateExtensions) state).pehkui_getModelWidthScale();
		
		return width != 1.0F ? original * width : original;
	}
}
