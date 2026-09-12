package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(ThrownEgg.class)
public class ThrownEggMixin
{
	@ModifyArg(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
	private Entity pehkui$onHit$addFreshEntity(Entity entity)
	{
		ScaleUtils.loadScale(entity, (Entity) (Object) this);
		
		return entity;
	}
}
