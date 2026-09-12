package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.player.Player;

/**
 * Vanilla refuses to nudge a player out of a wall after it grows, because vanilla players never
 * change size. Pehkui players do, so the same fix-up is applied to them client-side.
 */
@Mixin(Entity.class)
public abstract class EntityRefreshDimensionsMixin
{
	@Shadow public abstract boolean fudgePositionAfterSizeChange(EntityDimensions previousDimensions);
	
	@Inject(method = "refreshDimensions", at = @At("TAIL"))
	private void pehkui$refreshDimensions(CallbackInfo info, @Local(ordinal = 0) EntityDimensions oldDim, @Local(ordinal = 1) EntityDimensions newDim)
	{
		final Entity self = (Entity) (Object) this;
		
		if (self.level().isClientSide() && self instanceof Player && !self.isRemoved() && newDim.width() > oldDim.width())
		{
			fudgePositionAfterSizeChange(oldDim);
		}
	}
}
