package virtuoel.pehkui.mixin;

import java.util.function.BiFunction;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.Cursor3D;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import virtuoel.pehkui.util.ScalePhysicsUtils;

/**
 * Every block collision lookup in the game funnels through this cursor, so it is the one place that
 * can keep a very large entity's physics affordable. Only the region walked is trimmed - the box
 * each candidate shape is tested against is left alone, so whatever is found still resolves exactly
 * as vanilla would resolve it.
 */
@Mixin(BlockCollisions.class)
public class BlockCollisionsMixin
{
	@Shadow @Final @Mutable private Cursor3D cursor;

	@Inject(
		method = "<init>(Lnet/minecraft/world/level/CollisionGetter;Lnet/minecraft/world/phys/shapes/CollisionContext;Lnet/minecraft/world/phys/AABB;ZLjava/util/function/BiFunction;)V",
		at = @At("RETURN")
	)
	@SuppressWarnings("rawtypes")
	private void pehkui$limitScannedRegion(CollisionGetter collisionGetter, CollisionContext context, AABB box, boolean onlySuffocatingBlocks, BiFunction resultProvider, CallbackInfo info)
	{
		final Entity entity = context instanceof EntityCollisionContext entityContext ? entityContext.getEntity() : null;
		final AABB limited = ScalePhysicsUtils.limitFor(box, entity);

		if (limited != box)
		{
			this.cursor = new Cursor3D(
				Mth.floor(limited.minX - 1.0E-7D) - 1,
				Mth.floor(limited.minY - 1.0E-7D) - 1,
				Mth.floor(limited.minZ - 1.0E-7D) - 1,
				Mth.floor(limited.maxX + 1.0E-7D) + 1,
				Mth.floor(limited.maxY + 1.0E-7D) + 1,
				Mth.floor(limited.maxZ + 1.0E-7D) + 1
			);
		}
	}
}
