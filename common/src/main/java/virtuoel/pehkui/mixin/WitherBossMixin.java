package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(WitherBoss.class)
public class WitherBossMixin
{
	@ModifyExpressionValue(method = "getHeadX", at = @At(value = "CONSTANT", args = "doubleValue=1.3D"))
	private double pehkui$getHeadX$offset(double value)
	{
		final float scale = ScaleUtils.getBoundingBoxWidthScale((Entity) (Object) this);
		
		return scale != 1.0F ? scale * value : value;
	}
	
	@ModifyExpressionValue(method = "getHeadZ", at = @At(value = "CONSTANT", args = "doubleValue=1.3D"))
	private double pehkui$getHeadZ$offset(double value)
	{
		final float scale = ScaleUtils.getBoundingBoxWidthScale((Entity) (Object) this);
		
		return scale != 1.0F ? scale * value : value;
	}
}
