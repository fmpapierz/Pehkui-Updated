package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin
{
	@ModifyExpressionValue(method = "shouldStopFishing", at = @At(value = "CONSTANT", args = "doubleValue=1024.0D"))
	private double pehkui$shouldStopFishing$distance(double value)
	{
		final Player owner = ((FishingHook) (Object) this).getPlayerOwner();
		
		final float scale = ScaleUtils.getProjectileScale(owner);
		
		return scale != 1.0F ? value * scale * scale : value;
	}
}
