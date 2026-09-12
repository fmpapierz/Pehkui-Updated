package virtuoel.pehkui.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import virtuoel.pehkui.util.PehkuiRenderStateExtensions;

/**
 * The inventory portrait draws through the normal entity renderer, so a grown player overflowed
 * its frame and only a sliver of the model stayed visible. Growth is capped at the frame here,
 * exactly as vanilla already caps its own scale attribute a few lines further down; shrinking is
 * left alone so a small player still appears small.
 */
@Mixin(InventoryScreen.class)
public class InventoryScreenMixin
{
	@ModifyExpressionValue(method = "extractEntityInInventoryFollowsMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/InventoryScreen;extractRenderState(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;"))
	private static EntityRenderState pehkui$fitPortraitToFrame(EntityRenderState state)
	{
		final PehkuiRenderStateExtensions scales = (PehkuiRenderStateExtensions) state;

		final float widthScale = scales.pehkui_getModelWidthScale();
		final float heightScale = scales.pehkui_getModelHeightScale();

		// Only oversized portraits are pulled back in. Dividing the bounding box by the same
		// amount keeps the model centred, since the frame derives its offset from that box.
		final float widthFit = Math.max(widthScale, 1.0F);
		final float heightFit = Math.max(heightScale, 1.0F);

		if (widthFit != 1.0F || heightFit != 1.0F)
		{
			scales.pehkui_setModelScales(widthScale / widthFit, heightScale / heightFit);

			state.boundingBoxWidth /= widthFit;
			state.boundingBoxHeight /= heightFit;
		}

		return state;
	}
}
