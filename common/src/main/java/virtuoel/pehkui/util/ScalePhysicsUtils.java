package virtuoel.pehkui.util;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import virtuoel.pehkui.api.PehkuiConfig;

/**
 * Vanilla walks every block an entity's hitbox covers, several times each tick: to resolve
 * collision, to apply the effects of the blocks it is standing in, and to check for suffocation.
 * That is nothing at the sizes the game produces on its own - its largest entity is under sixteen
 * blocks across - and the work grows with the cube of the scale.
 *
 * <p>The two scans that only read the world are dropped once the hitbox passes
 * {@link PehkuiConfig.Common#physicsBoxLimit}; collision itself is left exactly as vanilla resolves
 * it, since cutting it down leaves parts of a large entity overlapping terrain that nothing pushes
 * it out of, and it ends up flung about instead of walking.
 */
public class ScalePhysicsUtils
{
	/**
	 * Side length, in blocks, of the largest hitbox that keeps all of vanilla's block scans.
	 */
	public static double getLimit()
	{
		final double limit = PehkuiConfig.COMMON.physicsBoxLimit.get();

		return limit > 0.0D ? limit : Double.MAX_VALUE;
	}

	/**
	 * Whether the entity's hitbox has outgrown the block scans. Measured by volume, because that is
	 * what the scans actually cost: a tall thin entity is nowhere near as expensive as a cube of
	 * the same height. For a player the budget runs out at roughly twenty-seven times normal size.
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

	private ScalePhysicsUtils()
	{

	}
}
