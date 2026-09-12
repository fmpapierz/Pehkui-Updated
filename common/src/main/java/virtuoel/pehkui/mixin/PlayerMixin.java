package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(Player.class)
public abstract class PlayerMixin
{
	@Inject(at = @At("RETURN"), method = "drop(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/item/ItemEntity;")
	private void pehkui$drop(ItemStack stack, boolean thrownFromHand, CallbackInfoReturnable<ItemEntity> info)
	{
		final ItemEntity entity = info.getReturnValue();

		if (entity != null)
		{
			ScaleUtils.setScaleOfDrop(entity, (Entity) (Object) this);

			final float scale = ScaleUtils.getEyeHeightScale((Entity) (Object) this);

			if (scale != 1.0F)
			{
				final Vec3 pos = entity.position();

				entity.setPos(pos.x, pos.y + ((1.0F - scale) * 0.3D), pos.z);
			}
		}
	}

	@WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;inflate(DDD)Lnet/minecraft/world/phys/AABB;"))
	private AABB pehkui$aiStep$inflate(AABB obj, double x, double y, double z, Operation<AABB> original)
	{
		final float widthScale = ScaleUtils.getBoundingBoxWidthScale((Entity) (Object) this);
		final float heightScale = ScaleUtils.getBoundingBoxHeightScale((Entity) (Object) this);

		if (widthScale != 1.0F)
		{
			x *= widthScale;
			z *= widthScale;
		}

		if (heightScale != 1.0F)
		{
			y *= heightScale;
		}

		return original.call(obj, x, y, z);
	}

	/**
	 * The sweeping edge hits everything within a fixed box around the target, so the box follows
	 * the target's size rather than the attacker's. Forge builds that box through its own
	 * {@code ItemStack#getSweepHitBox} hook instead, so this is allowed to find nothing there.
	 */
	@WrapOperation(method = "doSweepAttack", require = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;inflate(DDD)Lnet/minecraft/world/phys/AABB;"))
	private AABB pehkui$doSweepAttack$inflate(AABB obj, double x, double y, double z, Operation<AABB> original, @Local(argsOnly = true) Entity target)
	{
		final float widthScale = ScaleUtils.getBoundingBoxWidthScale(target);
		final float heightScale = ScaleUtils.getBoundingBoxHeightScale(target);

		if (widthScale != 1.0F)
		{
			x *= widthScale;
			z *= widthScale;
		}

		if (heightScale != 1.0F)
		{
			y *= heightScale;
		}

		return original.call(obj, x, y, z);
	}

	/**
	 * Vanilla hard-codes the sweep radius. NeoForge and Forge already replace that constant with
	 * the entity interaction range attribute, which is scaled elsewhere, so this is allowed to find
	 * nothing on those loaders.
	 */
	@ModifyExpressionValue(method = "doSweepAttack", require = 0, at = @At(value = "CONSTANT", args = "doubleValue=9.0D"))
	private double pehkui$doSweepAttack$sweepDistance(double value)
	{
		final float scale = ScaleUtils.getEntityReachScale((Entity) (Object) this);

		return scale > 1.0F ? scale * scale * value : value;
	}

	@ModifyExpressionValue(method = "getCurrentItemAttackStrengthDelay", at = @At(value = "CONSTANT", args = "doubleValue=20.0D"))
	private double pehkui$getCurrentItemAttackStrengthDelay$multiplier(double value)
	{
		final float scale = ScaleUtils.getAttackSpeedScale((Entity) (Object) this);

		return scale != 1.0F ? value / scale : value;
	}

	@ModifyReturnValue(method = "getDestroySpeed", at = @At("RETURN"))
	private float pehkui$getDestroySpeed(float original)
	{
		final float scale = ScaleUtils.getMiningSpeedScale((Entity) (Object) this);

		return scale != 1.0F ? original * scale : original;
	}

	@ModifyReturnValue(method = "blockInteractionRange", at = @At("RETURN"))
	private double pehkui$blockInteractionRange(double original)
	{
		final float scale = ScaleUtils.getBlockReachScale((Entity) (Object) this);

		return scale != 1.0F ? scale * original : original;
	}

	@ModifyReturnValue(method = "entityInteractionRange", at = @At("RETURN"))
	private double pehkui$entityInteractionRange(double original)
	{
		final float scale = ScaleUtils.getEntityReachScale((Entity) (Object) this);

		return scale != 1.0F ? scale * original : original;
	}

	@ModifyReturnValue(method = "getFlyingSpeed", at = @At("RETURN"))
	private float pehkui$getFlyingSpeed(float original)
	{
		final float scale = ScaleUtils.getFlightScale((Entity) (Object) this);

		return scale != 1.0F ? original * scale : original;
	}
}
