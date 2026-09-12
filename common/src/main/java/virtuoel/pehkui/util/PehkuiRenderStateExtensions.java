package virtuoel.pehkui.util;

/**
 * Rendering in 26.2 runs off an extracted render state rather than off the entity, so the scales
 * that the renderer needs are captured during extraction and read back at submit time.
 */
public interface PehkuiRenderStateExtensions
{
	float pehkui_getModelWidthScale();
	
	float pehkui_getModelHeightScale();
	
	void pehkui_setModelScales(float widthScale, float heightScale);
}
