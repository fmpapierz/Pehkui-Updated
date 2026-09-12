package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import virtuoel.pehkui.util.PehkuiBlockStateExtensions;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends EntityMixin
{
	@Unique private BlockPos pehkui$initialClimbingPos = null;

	@ModifyExpressionValue(method = "aiStep", at = @At(value = "CONSTANT", args = "doubleValue=0.003D"))
	private double pehkui$aiStep$minVelocity(double value)
	{
		final float scale = ScaleUtils.getMotionScale((Entity) (Object) this);

		return scale < 1.0F ? value * scale : value;
	}

	@ModifyVariable(method = "getDamageAfterArmorAbsorb", at = @At("HEAD"), argsOnly = true)
	private float pehkui$getDamageAfterArmorAbsorb(float value, DamageSource source, float amount)
	{
		final Entity attacker = source.getEntity();
		final float attackScale = attacker == null ? 1.0F : ScaleUtils.getAttackScale(attacker);
		final float defenseScale = ScaleUtils.getDefenseScale((Entity) (Object) this);

		if (attackScale != 1.0F || defenseScale != 1.0F)
		{
			value = attackScale * value / defenseScale;
		}

		return value;
	}

	@ModifyReturnValue(method = "getMaxHealth", at = @At("RETURN"))
	private float pehkui$getMaxHealth(float original)
	{
		final float scale = ScaleUtils.getHealthScale((Entity) (Object) this);

		return scale != 1.0F ? original * scale : original;
	}

	@ModifyReturnValue(method = "getVisibilityPercent", at = @At("RETURN"))
	private double pehkui$getVisibilityPercent(double original)
	{
		final float scale = ScaleUtils.getVisibilityScale((Entity) (Object) this);

		return scale != 1.0F ? original * scale : original;
	}

	/**
	 * Knockback dealt out is scaled by the attacker rather than by the entity receiving it, so a
	 * giant sends things flying and a tiny one barely nudges them.
	 */
	@ModifyVariable(method = "knockback(DDDLnet/minecraft/world/damagesource/DamageSource;FZ)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private double pehkui$knockback$power(double value, double power, double xd, double zd, DamageSource source, float damage, boolean comesFromEffect)
	{
		final Entity attacker = source == null ? null : source.getEntity();

		if (attacker == null)
		{
			return value;
		}

		final float scale = ScaleUtils.getKnockbackScale(attacker);

		return scale != 1.0F ? value * scale : value;
	}

	/**
	 * Both climb checks below sweep every block under the hitbox, so their cost grows with the
	 * square of the entity's width and they run several times a tick. Past a 16x16 footprint the
	 * sweep is abandoned: an entity that wide is not meaningfully interacting with a ladder, and
	 * the scan is what makes the game stall rather than the climbing.
	 */
	@Unique
	private static boolean pehkui$climbScanTooLarge(final int minX, final int maxX, final int minZ, final int maxZ)
	{
		return (long) (maxX - minX + 1) * (maxZ - minZ + 1) > 256L;
	}

	@ModifyReturnValue(method = "handleOnClimbable", at = @At("RETURN"))
	private Vec3 pehkui$handleOnClimbable(Vec3 original)
	{
		final LivingEntity self = (LivingEntity) (Object) this;

		if (!self.onClimbable())
		{
			return original;
		}

		final float width = ScaleUtils.getBoundingBoxWidthScale(self);

		if (width > 1.0F)
		{
			final AABB bounds = self.getBoundingBox();

			final double halfUnscaledXLength = (bounds.getXsize() / width) / 2.0D;
			final int minX = Mth.floor(bounds.minX + halfUnscaledXLength);
			final int maxX = Mth.floor(bounds.maxX - halfUnscaledXLength);

			final int minY = Mth.floor(bounds.minY);

			final double halfUnscaledZLength = (bounds.getZsize() / width) / 2.0D;
			final int minZ = Mth.floor(bounds.minZ + halfUnscaledZLength);
			final int maxZ = Mth.floor(bounds.maxZ - halfUnscaledZLength);

			if (pehkui$climbScanTooLarge(minX, maxX, minZ, maxZ))
			{
				return original;
			}

			final Level level = self.level();

			for (final BlockPos pos : BlockPos.betweenClosed(minX, minY, minZ, maxX, minY, maxZ))
			{
				if (((PehkuiBlockStateExtensions) level.getBlockState(pos)).pehkui_getBlock() instanceof ScaffoldingBlock)
				{
					return new Vec3(original.x, Math.max(self.getDeltaMovement().y, -0.15D), original.z);
				}
			}
		}

		return original;
	}

	/**
	 * A wide entity should be able to grab a ladder that its centre point misses, so the check is
	 * repeated across the blocks the hitbox actually covers.
	 */
	@ModifyReturnValue(method = "onClimbable", at = @At("RETURN"))
	private boolean pehkui$onClimbable(boolean original)
	{
		final LivingEntity self = (LivingEntity) (Object) this;

		if (pehkui$initialClimbingPos != null || original || self.isSpectator())
		{
			return original;
		}

		final float width = ScaleUtils.getBoundingBoxWidthScale(self);

		if (width > 1.0F)
		{
			final AABB bounds = self.getBoundingBox();

			final double halfUnscaledXLength = (bounds.getXsize() / width) / 2.0D;
			final int minX = Mth.floor(bounds.minX + halfUnscaledXLength);
			final int maxX = Mth.floor(bounds.maxX - halfUnscaledXLength);

			final int minY = Mth.floor(bounds.minY);

			final double halfUnscaledZLength = (bounds.getZsize() / width) / 2.0D;
			final int minZ = Mth.floor(bounds.minZ + halfUnscaledZLength);
			final int maxZ = Mth.floor(bounds.maxZ - halfUnscaledZLength);

			if (pehkui$climbScanTooLarge(minX, maxX, minZ, maxZ))
			{
				return original;
			}

			pehkui$initialClimbingPos = self.blockPosition();

			try
			{
				for (final BlockPos pos : BlockPos.betweenClosed(minX, minY, minZ, maxX, minY, maxZ))
				{
					pehkui_setBlockPosDirectly(pos.immutable());

					if (self.onClimbable())
					{
						return true;
					}
				}
			}
			finally
			{
				pehkui_setBlockPosDirectly(pehkui$initialClimbingPos);
				pehkui$initialClimbingPos = null;
			}
		}

		return original;
	}

	@WrapOperation(method = "pushEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getBoundingBox()Lnet/minecraft/world/phys/AABB;"))
	private AABB pehkui$pushEntities$getBoundingBox(LivingEntity obj, Operation<AABB> original)
	{
		final AABB bounds = original.call(obj);

		final float interactionWidth = ScaleUtils.getInteractionBoxWidthScale(obj);
		final float interactionHeight = ScaleUtils.getInteractionBoxHeightScale(obj);

		if (interactionWidth != 1.0F || interactionHeight != 1.0F)
		{
			final double scaledXLength = bounds.getXsize() * 0.5D * (interactionWidth - 1.0F);
			final double scaledYLength = bounds.getYsize() * 0.5D * (interactionHeight - 1.0F);
			final double scaledZLength = bounds.getZsize() * 0.5D * (interactionWidth - 1.0F);

			return bounds.inflate(scaledXLength, scaledYLength, scaledZLength);
		}

		return bounds;
	}

	@ModifyArg(method = "createWitherRose", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
	private Entity pehkui$createWitherRose$addFreshEntity(Entity entity)
	{
		ScaleUtils.setScaleOfDrop(entity, (Entity) (Object) this);

		return entity;
	}

	@ModifyArg(method = "calculateEntityAnimation", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;updateWalkAnimation(F)V"))
	private float pehkui$calculateEntityAnimation$updateWalkAnimation(float value)
	{
		return ScaleUtils.modifyLimbDistance(value, (LivingEntity) (Object) this);
	}

	@ModifyArg(method = "getPassengerRidingPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getPassengerAttachmentPoint(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/EntityDimensions;F)Lnet/minecraft/world/phys/Vec3;"))
	private float pehkui$getPassengerRidingPosition$attachmentScale(float value)
	{
		final float scale = ScaleUtils.getBoundingBoxHeightScale((Entity) (Object) this);

		return scale == 1.0F ? value : value * scale;
	}

	@ModifyReturnValue(method = "getDimensions", at = @At("RETURN"))
	private EntityDimensions pehkui$livingGetDimensions(EntityDimensions original)
	{
		final float widthScale = ScaleUtils.getBoundingBoxWidthScale((Entity) (Object) this);
		final float heightScale = ScaleUtils.getBoundingBoxHeightScale((Entity) (Object) this);

		if (widthScale != 1.0F || heightScale != 1.0F)
		{
			return original.scale(widthScale, heightScale);
		}

		return original;
	}

	@ModifyReturnValue(method = "maxUpStep", at = @At("RETURN"))
	private float pehkui$livingMaxUpStep(float original)
	{
		final float scale = ScaleUtils.getStepHeightScale((Entity) (Object) this);

		return scale != 1.0F ? original * scale : original;
	}
}
