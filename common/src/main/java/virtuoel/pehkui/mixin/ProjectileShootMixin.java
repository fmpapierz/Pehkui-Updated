package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(Projectile.class)
public abstract class ProjectileShootMixin
{
	@ModifyVariable(method = "shoot(DDDFF)V", ordinal = 0, argsOnly = true, at = @At("HEAD"))
	private float pehkui$shoot$power(float value)
	{
		final float scale = ScaleUtils.getMotionScale((Entity) (Object) this);
		
		return scale != 1.0F ? value * scale : value;
	}
	
	@WrapOperation(method = "isOutsideOwnerCollisionRange", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;inflate(D)Lnet/minecraft/world/phys/AABB;"))
	private AABB pehkui$isOutsideOwnerCollisionRange$inflate(AABB obj, double amount, Operation<AABB> original)
	{
		final float scale = ScaleUtils.getBoundingBoxWidthScale((Entity) (Object) this);
		
		return original.call(obj, scale != 1.0F ? amount * scale : amount);
	}
}
