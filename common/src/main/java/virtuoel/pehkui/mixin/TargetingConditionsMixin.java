package virtuoel.pehkui.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(TargetingConditions.class)
public class TargetingConditionsMixin
{
	@Shadow private boolean testInvisible;
	
	@ModifyExpressionValue(method = "test", at = @At(value = "CONSTANT", args = "doubleValue=2.0D"))
	private double pehkui$test$minVisibilityDistance(double value, ServerLevel level, @Nullable LivingEntity targeter, LivingEntity target)
	{
		if (testInvisible)
		{
			final float scale = ScaleUtils.getVisibilityScale(target);
			
			return scale != 1.0F ? value * scale : value;
		}
		
		return value;
	}
}
