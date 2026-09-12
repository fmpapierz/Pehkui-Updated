package virtuoel.pehkui.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import virtuoel.pehkui.util.PehkuiRenderStateExtensions;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements PehkuiRenderStateExtensions
{
	@Unique private float pehkui$modelWidthScale = 1.0F;
	@Unique private float pehkui$modelHeightScale = 1.0F;
	
	@Override
	public float pehkui_getModelWidthScale()
	{
		return this.pehkui$modelWidthScale;
	}
	
	@Override
	public float pehkui_getModelHeightScale()
	{
		return this.pehkui$modelHeightScale;
	}
	
	@Override
	public void pehkui_setModelScales(final float widthScale, final float heightScale)
	{
		this.pehkui$modelWidthScale = widthScale;
		this.pehkui$modelHeightScale = heightScale;
	}
}
