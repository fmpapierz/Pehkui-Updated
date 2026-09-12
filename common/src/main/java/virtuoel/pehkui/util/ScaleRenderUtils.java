package virtuoel.pehkui.util;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;

import net.minecraft.CrashReportCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import virtuoel.pehkui.Pehkui;
import virtuoel.pehkui.api.PehkuiConfig;

/**
 * Client-only rendering helpers. Nothing on the common initialisation path touches this class, so
 * it is never loaded on a dedicated server.
 */
public class ScaleRenderUtils
{
	public static float getTickDelta()
	{
		return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
	}
	
	public static final float modifyProjectionMatrixDepthByWidth(float depth, @Nullable Entity entity, float tickDelta)
	{
		return entity == null ? depth : modifyProjectionMatrixDepth(ScaleUtils.getBoundingBoxWidthScale(entity, tickDelta), depth, entity, tickDelta);
	}
	
	public static final float modifyProjectionMatrixDepthByHeight(float depth, @Nullable Entity entity, float tickDelta)
	{
		return entity == null ? depth : modifyProjectionMatrixDepth(ScaleUtils.getEyeHeightScale(entity, tickDelta), depth, entity, tickDelta);
	}
	
	public static final float modifyProjectionMatrixDepth(float depth, @Nullable Entity entity, float tickDelta)
	{
		return entity == null ? depth : modifyProjectionMatrixDepth(Math.min(ScaleUtils.getBoundingBoxWidthScale(entity, tickDelta), ScaleUtils.getEyeHeightScale(entity, tickDelta)), depth, entity, tickDelta);
	}
	
	public static final float modifyProjectionMatrixDepth(float scale, float depth, Entity entity, float tickDelta)
	{
		if (scale < 1.0F)
		{
			return Math.max(depth * scale, (float) PehkuiConfig.CLIENT.minimumCameraDepth.get().doubleValue());
		}
		
		return depth;
	}
	
	private static final Set<Item> LOGGED_ITEMS = ConcurrentHashMap.newKeySet();
	private static ItemStack lastRenderedStack = null;
	
	public static void saveLastRenderedItem(final ItemStack currentStack)
	{
		lastRenderedStack = currentStack;
	}
	
	public static void clearLastRenderedItem()
	{
		lastRenderedStack = null;
	}
	
	private static final Set<EntityType<?>> LOGGED_ENTITY_TYPES = ConcurrentHashMap.newKeySet();
	private static EntityType<?> lastRenderedEntity = null;
	
	public static void saveLastRenderedEntity(final EntityType<?> type)
	{
		lastRenderedEntity = type;
	}
	
	public static void clearLastRenderedEntity()
	{
		lastRenderedEntity = null;
	}
	
	public static void addDetailsToCrashReport(CrashReportCategory section)
	{
		if (lastRenderedStack != null)
		{
			section.setDetail("pehkui:debug/render/item", lastRenderedStack.getItem().getDescriptionId());
			LOGGED_ITEMS.add(lastRenderedStack.getItem());
		}
		
		if (lastRenderedEntity != null)
		{
			final Identifier id = EntityType.getKey(lastRenderedEntity);
			
			section.setDetail("pehkui:debug/render/entity", id);
			LOGGED_ENTITY_TYPES.add(lastRenderedEntity);
		}
	}
	
	static
	{
		Pehkui.LOGGER.debug("Pehkui render helpers initialised.");
	}
	
	private ScaleRenderUtils()
	{
		
	}
}
