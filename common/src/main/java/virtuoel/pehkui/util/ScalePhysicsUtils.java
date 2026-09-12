package virtuoel.pehkui.util;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import virtuoel.pehkui.api.PehkuiConfig;

/**
 * Vanilla walks every block an entity's hitbox covers, several times each tick, for collision,
 * block effects and suffocation. That is affordable at vanilla sizes - the largest entity in the
 * game is under sixteen blocks across - and ruinous once a scaled entity is tens of blocks across,
 * because the work grows with the cube of the scale.
 *
 * <p>{@link PehkuiConfig.Common#physicsBoxLimit} is the side of the largest hitbox that still gets
 * all of it. Past that the whole-hitbox scans stop and collision is resolved against a core of that
 * size standing at the entity's feet. Size, eye height, reach and the model are all untouched, and
 * for a player the budget is not reached until roughly twenty-seven times normal size.
 */
public class ScalePhysicsUtils
{
	/**
	 * Side length, in blocks, of the largest hitbox that keeps vanilla's own behaviour.
	 */
	public static double getLimit()
	{
		final double limit = PehkuiConfig.COMMON.physicsBoxLimit.get();

		return limit > 0.0D ? limit : Double.MAX_VALUE;
	}

	/**
	 * Whether the entity's hitbox has outgrown the block scans. Measured by volume, because that is
	 * what the scans actually cost: a tall thin entity is nowhere near as expensive as a cube of
	 * the same height.
	 */
	public static boolean exceedsScanBudget(@Nullable final Entity entity)
	{
		if (entity == null)
		{
			return false;
		}

		final double limit = getLimit();

		if (limit == Double.MAX_VALUE)
		{
			return false;
		}

		final AABB box = entity.getBoundingBox();

		return box.getXsize() * box.getYsize() * box.getZsize() > limit * limit * limit;
	}

	/**
	 * Trims a scan region down to what the entity can afford, or returns it untouched while the
	 * entity is still within budget.
	 */
	public static AABB limitFor(final AABB box, @Nullable final Entity entity)
	{
		return exceedsScanBudget(entity) ? limit(box, entity) : box;
	}

	/**
	 * The horizontal extents close in on the region's centre and the top comes down towards the
	 * entity's feet. Everything below the feet is kept: that part of the region is the path a fall
	 * sweeps through, and dropping it would let a large entity fall straight through the ground.
	 */
	public static AABB limit(final AABB box, final Entity entity)
	{
		final double limit = getLimit();
		final double feet = entity.getBoundingBox().minY;

		double minX = box.minX;
		double maxX = box.maxX;
		double minZ = box.minZ;
		double maxZ = box.maxZ;
		double maxY = box.maxY;

		if (box.getXsize() > limit)
		{
			final double centre = (box.minX + box.maxX) * 0.5D;

			minX = centre - limit * 0.5D;
			maxX = centre + limit * 0.5D;
		}

		if (box.getZsize() > limit)
		{
			final double centre = (box.minZ + box.maxZ) * 0.5D;

			minZ = centre - limit * 0.5D;
			maxZ = centre + limit * 0.5D;
		}

		final double top = Math.max(feet, box.minY) + limit;

		if (maxY > top)
		{
			maxY = Math.max(top, box.minY);
		}

		return new AABB(minX, box.minY, minZ, maxX, maxY, maxZ);
	}

	private ScalePhysicsUtils()
	{

	}
}
