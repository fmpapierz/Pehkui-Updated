package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.animal.Animal;

/**
 * Only exists so the Fox breed goal mixin can reach the two parents, which live on this superclass.
 */
@Mixin(BreedGoal.class)
public abstract class BreedGoalMixin
{
	@Shadow @Final protected Animal animal;
	@Shadow protected Animal partner;
}
