package virtuoel.pehkui.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import virtuoel.pehkui.util.ScaleUtils;

/**
 * Ray tracing walks each candidate's bounding box, which is where the interaction box scale has
 * to be folded in - both for projectiles in flight and for the crosshair pick.
 */
@Mixin(ProjectileUtil.class)
public class ProjectileUtilMixin
{
	@WrapOperation(method = "getEntityHitResult(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;F)Lnet/minecraft/world/phys/EntityHitResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getBoundingBox()Lnet/minecraft/world/phys/AABB;"))
	private static AABB pehkui$getEntityHitResult$getBoundingBox(Entity obj, Operation<AABB> original, @Local(argsOnly = true) float entityMargin)
	{
		final AABB bounds = original.call(obj);
		
		final float width = ScaleUtils.getBoundingBoxWidthScale(obj);
		final float height = ScaleUtils.getBoundingBoxHeightScale(obj);
		
		final float interactionWidth = ScaleUtils.getInteractionBoxWidthScale(obj);
		final float interactionHeight = ScaleUtils.getInteractionBoxHeightScale(obj);
		
		if (width != 1.0F || height != 1.0F || interactionWidth != 1.0F || interactionHeight != 1.0F)
		{
			final double scaledXLength = entityMargin * ((width * interactionWidth) - 1.0F);
			final double scaledYLength = entityMargin * ((height * interactionHeight) - 1.0F);
			final double scaledZLength = entityMargin * ((width * interactionWidth) - 1.0F);
			
			return bounds.inflate(scaledXLength, scaledYLength, scaledZLength);
		}
		
		return bounds;
	}
	
	@WrapOperation(method = "getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getBoundingBox()Lnet/minecraft/world/phys/AABB;"))
	private static AABB pehkui$pick$getBoundingBox(Entity obj, Operation<AABB> original)
	{
		final AABB bounds = original.call(obj);
		final float margin = obj.getPickRadius();
		
		final float interactionWidth = ScaleUtils.getInteractionBoxWidthScale(obj);
		final float interactionHeight = ScaleUtils.getInteractionBoxHeightScale(obj);
		
		if (interactionWidth != 1.0F || interactionHeight != 1.0F)
		{
			final double scaledXLength = bounds.getXsize() * 0.5D * (interactionWidth - 1.0F);
			final double scaledYLength = bounds.getYsize() * 0.5D * (interactionHeight - 1.0F);
			final double scaledZLength = bounds.getZsize() * 0.5D * (interactionWidth - 1.0F);
			final double scaledMarginWidth = margin * (interactionWidth - 1.0F);
			final double scaledMarginHeight = margin * (interactionHeight - 1.0F);
			
			return bounds.inflate(scaledXLength + scaledMarginWidth, scaledYLength + scaledMarginHeight, scaledZLength + scaledMarginWidth);
		}
		
		return bounds;
	}
}
