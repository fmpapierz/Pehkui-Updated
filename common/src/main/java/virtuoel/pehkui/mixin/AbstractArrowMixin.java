package virtuoel.pehkui.mixin;

import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin
{
	/**
	 * A larger arrow sweeps a larger volume looking for something to hit.
	 */
	@ModifyArg(method = "findHitEntity", index = 4, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getEntityHitResult(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/projectile/Projectile;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Lnet/minecraft/world/phys/EntityHitResult;"))
	private AABB pehkui$findHitEntity$expand(Level level, Projectile source, Vec3 from, Vec3 to, AABB box, Predicate<Entity> matching)
	{
		final float width = ScaleUtils.getBoundingBoxWidthScale(source);
		final float height = ScaleUtils.getBoundingBoxHeightScale(source);

		if (width != 1.0F || height != 1.0F)
		{
			return box.inflate(width - 1.0D, height - 1.0D, width - 1.0D);
		}

		return box;
	}

	@ModifyVariable(method = "onHitEntity", at = @At(value = "STORE"))
	private float pehkui$onHitEntity(float value)
	{
		final float scale = ScaleUtils.getMotionScale((Entity) (Object) this);

		return scale != 1.0F ? ScaleUtils.divideClamped(value, scale) : value;
	}
}
