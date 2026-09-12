package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(ServerEntity.class)
public abstract class ServerEntityMixin
{
	@Shadow @Final private Entity entity;
	
	@Inject(at = @At("TAIL"), method = "sendChanges")
	private void pehkui$sendChanges(CallbackInfo info)
	{
		ScaleUtils.syncScalesIfNeeded(entity);
	}
	
	/**
	 * Position updates below this threshold are dropped. A shrunken entity's whole stride can fall
	 * under it, which would freeze it in place for everyone else.
	 */
	@ModifyExpressionValue(method = "sendChanges", at = @At(value = "CONSTANT", args = "doubleValue=7.62939453125E-6D"))
	private double pehkui$sendChanges$minimumSquaredDistance(double value)
	{
		final float scale = ScaleUtils.getMotionScale(entity);
		
		return scale < 1.0F ? value * scale * scale : value;
	}
}
