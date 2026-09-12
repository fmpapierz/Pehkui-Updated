package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.phys.AABB;
import virtuoel.pehkui.util.ScaleUtils;

/**
 * Area lookups test the stored bounding box, so an entity whose interaction box reaches past its
 * hitbox has to be widened here or it will never be returned.
 */
@Mixin(EntitySection.class)
public class EntitySectionMixin
{
	@WrapOperation(method = "getEntities(Lnet/minecraft/world/phys/AABB;Lnet/minecraft/util/AbortableIterationConsumer;)Lnet/minecraft/util/AbortableIterationConsumer$Continuation;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/entity/EntityAccess;getBoundingBox()Lnet/minecraft/world/phys/AABB;"))
	private AABB pehkui$getEntities$getBoundingBox(EntityAccess obj, Operation<AABB> original)
	{
		return pehkui$expand(obj, original.call(obj));
	}
	
	@WrapOperation(method = "getEntities(Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Lnet/minecraft/util/AbortableIterationConsumer;)Lnet/minecraft/util/AbortableIterationConsumer$Continuation;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/entity/EntityAccess;getBoundingBox()Lnet/minecraft/world/phys/AABB;"))
	private AABB pehkui$getEntitiesFiltered$getBoundingBox(EntityAccess obj, Operation<AABB> original)
	{
		return pehkui$expand(obj, original.call(obj));
	}
	
	private static AABB pehkui$expand(final EntityAccess obj, final AABB bounds)
	{
		if (obj instanceof Entity)
		{
			final Entity entity = (Entity) obj;
			
			final float interactionWidth = ScaleUtils.getInteractionBoxWidthScale(entity);
			final float interactionHeight = ScaleUtils.getInteractionBoxHeightScale(entity);
			
			if (interactionWidth != 1.0F || interactionHeight != 1.0F)
			{
				final double scaledXLength = bounds.getXsize() * 0.5D * (interactionWidth - 1.0F);
				final double scaledYLength = bounds.getYsize() * 0.5D * (interactionHeight - 1.0F);
				final double scaledZLength = bounds.getZsize() * 0.5D * (interactionWidth - 1.0F);
				
				return bounds.inflate(scaledXLength, scaledYLength, scaledZLength);
			}
		}
		
		return bounds;
	}
}
