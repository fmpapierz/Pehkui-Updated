package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(Animal.class)
public class AnimalMixin
{
	@Inject(method = "spawnChildFromBreeding", at = @At(value = "INVOKE", shift = Shift.BEFORE, target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"))
	private void pehkui$spawnChildFromBreeding(ServerLevel level, Animal partner, CallbackInfo info, @Local AgeableMob offspring)
	{
		ScaleUtils.loadAverageScales(offspring, (Animal) (Object) this, partner);
	}
}
