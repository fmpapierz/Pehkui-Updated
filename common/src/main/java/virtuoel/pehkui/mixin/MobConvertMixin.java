package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import virtuoel.pehkui.util.ScaleUtils;

/**
 * Every conversion - a cured zombie villager, a pig struck by lightning, a slime splitting -
 * funnels through here, and the new mob inherits the old one's scales.
 */
@Mixin(Mob.class)
public class MobConvertMixin
{
	@Inject(at = @At("RETURN"), method = "convertTo(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/ConversionParams;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/ConversionParams$AfterConversion;)Lnet/minecraft/world/entity/Mob;")
	private <T extends Mob> void pehkui$convertTo(EntityType<T> entityType, ConversionParams params, EntitySpawnReason spawnReason, ConversionParams.AfterConversion<T> afterConversion, CallbackInfoReturnable<T> info)
	{
		final Mob converted = info.getReturnValue();
		
		if (converted != null)
		{
			ScaleUtils.loadScale(converted, (Entity) (Object) this);
		}
	}
}
