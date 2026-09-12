package virtuoel.pehkui.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.phys.Vec3;
import virtuoel.pehkui.util.PehkuiRenderStateExtensions;

/**
 * The sneak crouch offset is expressed in model space, so it has to follow the model's height.
 */
@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin
{
	@ModifyReturnValue(method = "getRenderOffset", at = @At("RETURN"))
	private Vec3 pehkui$getRenderOffset(Vec3 original, AvatarRenderState state)
	{
		if (original != Vec3.ZERO)
		{
			return original.scale(((PehkuiRenderStateExtensions) state).pehkui_getModelHeightScale());
		}
		
		return original;
	}
}
