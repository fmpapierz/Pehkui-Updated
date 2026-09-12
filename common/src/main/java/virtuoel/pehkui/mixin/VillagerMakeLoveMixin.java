package virtuoel.pehkui.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.VillagerMakeLove;
import net.minecraft.world.entity.npc.villager.Villager;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(VillagerMakeLove.class)
public class VillagerMakeLoveMixin
{
	@Inject(method = "breed", at = @At(value = "INVOKE", shift = Shift.BEFORE, target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"))
	private void pehkui$breed(ServerLevel level, Villager parent, Villager partner, CallbackInfoReturnable<Optional<Villager>> info, @Local(ordinal = 2) Villager child)
	{
		ScaleUtils.loadAverageScales(child, parent, partner);
	}
}
