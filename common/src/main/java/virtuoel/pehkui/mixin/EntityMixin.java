package virtuoel.pehkui.mixin;

import java.util.Map;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import virtuoel.pehkui.Pehkui;
import virtuoel.pehkui.api.PehkuiConfig;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.api.ScaleType;
import virtuoel.pehkui.server.command.DebugCommand;
import virtuoel.pehkui.util.PehkuiEntityExtensions;
import virtuoel.pehkui.util.ScalePhysicsUtils;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(Entity.class)
public abstract class EntityMixin implements PehkuiEntityExtensions
{
	@Shadow private boolean onGround;
	@Shadow protected boolean firstTick;
	@Shadow private BlockPos blockPosition;

	private boolean pehkui_shouldSyncScales = false;
	private boolean pehkui_shouldIgnoreScaleNbt = false;
	private ScaleData[] pehkui_scaleCache = null;

	private static final String PEHKUI_SCALE_DATA_KEY = Pehkui.MOD_ID + ":scale_data_types";

	@Override
	public ScaleData pehkui_constructScaleData(ScaleType type)
	{
		return ScaleData.Builder.create().type(type).entity((Entity) (Object) this).build();
	}

	@Override
	public ScaleData[] pehkui_getScaleCache()
	{
		return pehkui_scaleCache;
	}

	@Override
	public void pehkui_setScaleCache(ScaleData[] scaleCache)
	{
		pehkui_scaleCache = scaleCache;
	}

	@Override
	public void pehkui_setShouldSyncScales(boolean sync)
	{
		pehkui_shouldSyncScales = sync;
	}

	@Override
	public boolean pehkui_shouldSyncScales()
	{
		return pehkui_shouldSyncScales;
	}

	@Override
	public boolean pehkui_shouldIgnoreScaleNbt()
	{
		return pehkui_shouldIgnoreScaleNbt;
	}

	@Override
	public void pehkui_setShouldIgnoreScaleNbt(boolean ignore)
	{
		pehkui_shouldIgnoreScaleNbt = ignore;
	}

	@Inject(at = @At("TAIL"), method = "load")
	private void pehkui$load(ValueInput input, CallbackInfo info)
	{
		if (pehkui_shouldIgnoreScaleNbt())
		{
			return;
		}

		final Optional<CompoundTag> stored = input.read(PEHKUI_SCALE_DATA_KEY, CompoundTag.CODEC);

		if (stored.isPresent())
		{
			final CompoundTag wrapper = new CompoundTag();
			wrapper.put(PEHKUI_SCALE_DATA_KEY, stored.get());

			pehkui_readScaleNbt(wrapper);
		}
	}

	@Override
	public void pehkui_readScaleNbt(CompoundTag nbt)
	{
		if (pehkui_shouldIgnoreScaleNbt())
		{
			return;
		}

		final Optional<CompoundTag> stored = nbt.getCompound(PEHKUI_SCALE_DATA_KEY);

		if (stored.isPresent() && !DebugCommand.unmarkEntityForScaleReset((Entity) (Object) this))
		{
			final CompoundTag typeData = stored.get();

			String key;
			ScaleData scaleData;
			for (final Map.Entry<Identifier, ScaleType> entry : ScaleRegistries.SCALE_TYPES.entrySet())
			{
				key = entry.getKey().toString();

				final Optional<CompoundTag> entryData = typeData.getCompound(key);

				if (entryData.isPresent())
				{
					scaleData = pehkui_getScaleData(entry.getValue());
					scaleData.readNbt(entryData.get());
				}
			}
		}
	}

	@Inject(at = @At("TAIL"), method = "saveWithoutId")
	private void pehkui$saveWithoutId(ValueOutput output, CallbackInfo info)
	{
		if (pehkui_shouldIgnoreScaleNbt())
		{
			return;
		}

		final CompoundTag wrapper = pehkui_writeScaleNbt(new CompoundTag());

		wrapper.getCompound(PEHKUI_SCALE_DATA_KEY).ifPresent(typeData -> output.store(PEHKUI_SCALE_DATA_KEY, CompoundTag.CODEC, typeData));
	}

	@Override
	public CompoundTag pehkui_writeScaleNbt(CompoundTag nbt)
	{
		if (pehkui_shouldIgnoreScaleNbt())
		{
			return nbt;
		}

		final CompoundTag typeData = new CompoundTag();

		CompoundTag compound;
		for (final ScaleData scaleData : pehkui_getScales().values())
		{
			if (scaleData != null)
			{
				compound = scaleData.writeNbt(new CompoundTag());

				if (compound.size() != 0)
				{
					typeData.put(ScaleRegistries.getId(ScaleRegistries.SCALE_TYPES, scaleData.getScaleType()).toString(), compound);
				}
			}
		}

		if (typeData.size() > 0)
		{
			nbt.put(PEHKUI_SCALE_DATA_KEY, typeData);
		}

		return nbt;
	}

	@Inject(at = @At("HEAD"), method = "tick")
	private void pehkui$tick(CallbackInfo info)
	{
		for (ScaleType type : ScaleRegistries.SCALE_TYPES.values())
		{
			ScaleUtils.tickScale(pehkui_getScaleData(type));
		}
	}

	@ModifyReturnValue(method = "getDimensions", at = @At("RETURN"))
	private EntityDimensions pehkui$getDimensions(EntityDimensions original)
	{
		final float widthScale = ScaleUtils.getBoundingBoxWidthScale((Entity) (Object) this);
		final float heightScale = ScaleUtils.getBoundingBoxHeightScale((Entity) (Object) this);

		if (widthScale != 1.0F || heightScale != 1.0F)
		{
			return original.scale(widthScale, heightScale);
		}

		return original;
	}

	@Inject(at = @At("HEAD"), method = "startSeenByPlayer")
	private void pehkui$startSeenByPlayer(ServerPlayer player, CallbackInfo info)
	{
		ScaleUtils.syncScalesOnTrackingStart((Entity) (Object) this, player);
	}

	@ModifyVariable(method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At(value = "STORE"))
	private ItemEntity pehkui$spawnAtLocation(ItemEntity entity)
	{
		ScaleUtils.setScaleOfDrop(entity, (Entity) (Object) this);
		return entity;
	}

	@ModifyExpressionValue(method = "move", at = @At(value = "CONSTANT", args = "doubleValue=1.0E-7D"))
	private double pehkui$move$minVelocity(double value)
	{
		final float scale = ScaleUtils.getMotionScale((Entity) (Object) this);

		return scale < 1.0F ? scale * scale * value : value;
	}

	/**
	 * Scaling what an entity moves in a tick is what makes a small one take small steps. Falling is
	 * left out of it: a shrunken entity whose fall was scaled down too drifted to the ground like a
	 * feather instead of dropping, so gravity keeps its normal pull no matter how small the entity
	 * gets. Growing still speeds a fall up, which is what keeps a large entity from appearing to
	 * sink in slow motion.
	 */
	@ModifyArg(method = "move", index = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;maybeBackOffFromEdge(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/entity/MoverType;)Lnet/minecraft/world/phys/Vec3;"))
	private Vec3 pehkui$move$maybeBackOffFromEdge(Vec3 movement, MoverType type)
	{
		if (type == MoverType.SELF || type == MoverType.PLAYER)
		{
			final float scale = ScaleUtils.getMotionScale((Entity) (Object) this);

			if (scale != 1.0F)
			{
				final double y = scale < 1.0F && movement.y < 0.0D ? movement.y : movement.y * scale;

				return new Vec3(movement.x * scale, y, movement.z * scale);
			}
		}

		return movement;
	}

	/**
	 * Guards the block effect scan, which visits every block the hitbox covers each tick. A cactus
	 * or a cobweb means nothing to something the size of a hill, and the scan is what stalls the
	 * game rather than the effect.
	 */
	@ModifyReturnValue(method = "isAffectedByBlocks", at = @At("RETURN"))
	private boolean pehkui$isAffectedByBlocks(boolean original)
	{
		return original && !ScalePhysicsUtils.exceedsScanBudget((Entity) (Object) this);
	}

	/**
	 * Suffocation sweeps a slab as wide as the entity, so its cost grows with the square of the
	 * width. Nothing that large can be smothered by the block its eyes happen to be in.
	 */
	@ModifyReturnValue(method = "isInWall", at = @At("RETURN"))
	private boolean pehkui$isInWall(boolean original)
	{
		return original && !ScalePhysicsUtils.exceedsScanBudget((Entity) (Object) this);
	}

	@WrapOperation(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/entity/Entity;push(DDD)V"))
	private void pehkui$pushSelfAwayFrom$self(Entity obj, double x, double y, double z, Operation<Void> original)
	{
		final float ownScale = ScaleUtils.getMotionScale((Entity) (Object) this);

		if (ownScale != 1.0F)
		{
			x *= ownScale;
			z *= ownScale;
		}

		original.call(obj, x, y, z);
	}

	@WrapOperation(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/world/entity/Entity;push(DDD)V"))
	private void pehkui$pushSelfAwayFrom$other(Entity obj, double x, double y, double z, Operation<Void> original, @Local(argsOnly = true) Entity other)
	{
		final float otherScale = ScaleUtils.getMotionScale(other);

		if (otherScale != 1.0F)
		{
			x *= otherScale;
			z *= otherScale;
		}

		original.call(obj, x, y, z);
	}

	@Inject(at = @At("HEAD"), method = "spawnSprintParticle", cancellable = true)
	private void pehkui$spawnSprintParticle(CallbackInfo info)
	{
		if (ScaleUtils.getMotionScale((Entity) (Object) this) < 1.0F)
		{
			info.cancel();
		}
	}

	@ModifyArg(method = "checkFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;fallOn(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;D)V"))
	private double pehkui$checkFallDamage$fallDistance(double distance)
	{
		final float scale = ScaleUtils.getFallingScale((Entity) (Object) this);

		if (scale != 1.0F && PehkuiConfig.COMMON.scaledFallDamage.get())
		{
			return distance * scale;
		}

		return distance;
	}

	/**
	 * Footstep cadence is measured in blocks travelled, so without this a shrunken entity would
	 * take a very long time between steps.
	 */
	@ModifyExpressionValue(method = "applyMovementEmissionAndPlaySound", at = { @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;length()D"), @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;horizontalDistance()D") })
	private double pehkui$applyMovementEmissionAndPlaySound$stepDistance(double value)
	{
		final float scale = ScaleUtils.getMotionScale((Entity) (Object) this);

		return scale != 1.0F ? value / scale : value;
	}

	@ModifyExpressionValue(method = "checkSupportingBlock", at = @At(value = "CONSTANT", args = "doubleValue=1.0E-6"))
	private double pehkui$checkSupportingBlock$offset(double value)
	{
		final float scale = ScaleUtils.getMotionScale((Entity) (Object) this);

		return scale < 1.0F ? value * scale : value;
	}

	@ModifyArg(method = "getPassengerRidingPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getPassengerAttachmentPoint(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/EntityDimensions;F)Lnet/minecraft/world/phys/Vec3;"))
	private float pehkui$getPassengerRidingPosition$attachmentScale(float value)
	{
		final float scale = ScaleUtils.getBoundingBoxHeightScale((Entity) (Object) this);

		return scale == 1.0F ? value : value * scale;
	}

	/**
	 * Lets a mixin on a subclass move the cached block position without triggering a repositioning.
	 */
	protected void pehkui_setBlockPosDirectly(final BlockPos pos)
	{
		this.blockPosition = pos;
	}

	@Override
	public boolean pehkui_isFirstUpdate()
	{
		return this.firstTick;
	}

	@Override
	public boolean pehkui_getOnGround()
	{
		return this.onGround;
	}

	@Override
	public void pehkui_setOnGround(boolean onGround)
	{
		this.onGround = onGround;
	}
}
