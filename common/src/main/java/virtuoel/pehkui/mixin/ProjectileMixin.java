package virtuoel.pehkui.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import virtuoel.pehkui.util.ScaleUtils;

/**
 * Every projectile learns who fired it through {@code setOwner}, so scaling here covers arrows,
 * thrown items, fireballs, llama spit and evoker fangs alike.
 */
@Mixin(Projectile.class)
public abstract class ProjectileMixin
{
	@Inject(at = @At("TAIL"), method = "setOwner(Lnet/minecraft/world/entity/Entity;)V")
	private void pehkui$setOwner(@Nullable Entity owner, CallbackInfo info)
	{
		if (owner != null)
		{
			ScaleUtils.setScaleOfProjectile((Entity) (Object) this, owner);
		}
	}
	
	/**
	 * A projectile leaves the shooter's hand, so the spawn height follows the shooter's eye level.
	 */
	@Inject(at = @At("TAIL"), method = "shootFromRotation")
	private void pehkui$shootFromRotation(Entity source, float xRot, float yRot, float yOffset, float pow, float uncertainty, CallbackInfo info)
	{
		final float heightScale = ScaleUtils.getEyeHeightScale(source);
		
		if (heightScale != 1.0F)
		{
			final Entity self = (Entity) (Object) this;
			
			self.setPos(self.getX(), self.getY() + ((1.0F - heightScale) * 0.1D), self.getZ());
		}
	}
}
