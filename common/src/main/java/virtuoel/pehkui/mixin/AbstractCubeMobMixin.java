package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.cubemob.AbstractCubeMob;
import virtuoel.pehkui.util.ScaleUtils;

/**
 * A splitting cube places its halves half a block up. Scales are carried over by the conversion
 * itself; only the spawn offset needs to follow the parent's size.
 */
@Mixin(AbstractCubeMob.class)
public class AbstractCubeMobMixin
{
	@ModifyExpressionValue(method = "setUpSplitCube", at = @At(value = "CONSTANT", args = "doubleValue=0.5D"))
	private double pehkui$setUpSplitCube$verticalOffset(double value)
	{
		final float scale = ScaleUtils.getBoundingBoxHeightScale((Entity) (Object) this);
		
		return scale != 1.0F ? value * scale : value;
	}
}
