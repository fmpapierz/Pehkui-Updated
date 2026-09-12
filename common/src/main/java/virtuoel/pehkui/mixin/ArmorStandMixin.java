package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(ArmorStand.class)
public abstract class ArmorStandMixin
{
	@ModifyVariable(method = "getClickedSlot", at = @At(value = "STORE"))
	private double pehkui$getClickedSlot(double value)
	{
		final float scale = ScaleUtils.getBoundingBoxHeightScale((Entity) (Object) this);
		
		return scale != 1.0F ? value / scale : value;
	}
}
