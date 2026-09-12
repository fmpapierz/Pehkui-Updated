package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.world.entity.animal.fox.Fox;
import virtuoel.pehkui.util.ScaleUtils;

/**
 * Foxes build their kit themselves instead of going through {@code Animal#spawnChildFromBreeding},
 * so the average-of-parents rule has to be applied here too.
 */
@Mixin(targets = "net.minecraft.world.entity.animal.fox.Fox$FoxBreedGoal")
public abstract class FoxBreedGoalMixin extends BreedGoalMixin
{
	@Inject(method = "breed()V", at = @At(value = "INVOKE", shift = Shift.BEFORE, target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"))
	private void pehkui$breed(CallbackInfo info, @Local Fox offspring)
	{
		if (this.partner != null)
		{
			ScaleUtils.loadAverageScales(offspring, this.animal, this.partner);
		}
	}
}
