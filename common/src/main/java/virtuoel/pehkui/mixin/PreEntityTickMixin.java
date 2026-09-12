package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.api.ScaleType;
import virtuoel.pehkui.util.ScaleUtils;

/**
 * Entities that never call {@code super.tick()} still need their scale data ticked.
 */
@Mixin({
	AbstractMinecart.class,
	BlockAttachedEntity.class,
	EndCrystal.class,
	FallingBlockEntity.class,
	PrimedTnt.class
})
public abstract class PreEntityTickMixin extends EntityMixin
{
	@Inject(at = @At("HEAD"), method = "tick")
	private void pehkui$preEntityTick(CallbackInfo info)
	{
		for (final ScaleType scaleType : ScaleRegistries.SCALE_TYPES.values())
		{
			ScaleUtils.tickScale(pehkui_getScaleData(scaleType));
		}
	}
}
