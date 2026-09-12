package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import virtuoel.pehkui.api.PehkuiConfig;
import virtuoel.pehkui.util.PehkuiBlockStateExtensions;

@Mixin(NetherPortalBlock.class)
public abstract class NetherPortalBlockMixin
{
	/**
	 * Vanilla treats the whole block as the portal, which lets an oversized entity teleport while
	 * only its bounding box corner clips the frame.
	 */
	@Inject(at = @At("HEAD"), method = "entityInside", cancellable = true)
	private void pehkui$entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise, CallbackInfo info)
	{
		if (PehkuiConfig.COMMON.accurateNetherPortals.get())
		{
			if (!entity.getBoundingBox().intersects(((PehkuiBlockStateExtensions) state).pehkui_getOutlineShape(level, pos).bounds().move(pos)))
			{
				info.cancel();
			}
		}
	}
}
