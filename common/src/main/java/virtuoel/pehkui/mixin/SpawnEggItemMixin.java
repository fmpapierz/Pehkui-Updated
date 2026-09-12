package virtuoel.pehkui.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.phys.Vec3;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(SpawnEggItem.class)
public class SpawnEggItemMixin
{
	@Inject(at = @At("RETURN"), method = "spawnOffspringFromSpawnEgg")
	private static void pehkui$spawnOffspringFromSpawnEgg(Player user, Mob parent, EntityType<? extends Mob> entityType, ServerLevel level, Vec3 pos, ItemStack itemStack, CallbackInfoReturnable<Optional<Mob>> info)
	{
		info.getReturnValue().ifPresent(e -> ScaleUtils.loadScale(e, parent));
	}
}
