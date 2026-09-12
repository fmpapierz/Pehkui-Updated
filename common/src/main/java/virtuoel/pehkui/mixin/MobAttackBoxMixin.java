package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(Mob.class)
public abstract class MobAttackBoxMixin
{
	@ModifyVariable(method = "getAttackBoundingBox", at = @At("HEAD"), argsOnly = true)
	private double pehkui$getAttackBoundingBox$horizontalExpansion(double horizontalExpansion)
	{
		final float scale = ScaleUtils.getEntityReachScale((Entity) (Object) this);
		
		return scale != 1.0F ? horizontalExpansion * scale : horizontalExpansion;
	}
}
