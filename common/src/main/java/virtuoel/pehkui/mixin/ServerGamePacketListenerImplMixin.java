package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import virtuoel.pehkui.util.ScaleUtils;

/**
 * The server replays the movement the client reported. Pehkui scales movement inside
 * {@code Entity#move}, so the distance is divided out here to avoid scaling it twice and
 * tripping the "moved wrongly" check.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin
{
	@Shadow public ServerPlayer player;
	
	@ModifyArg(method = "handleMoveVehicle", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"))
	private Vec3 pehkui$handleMoveVehicle$move(MoverType type, Vec3 movement)
	{
		final float scale = ScaleUtils.getMotionScale(player.getRootVehicle());
		
		return scale != 1.0F ? movement.scale(1.0F / scale) : movement;
	}
	
	@ModifyArg(method = "handleMovePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"))
	private Vec3 pehkui$handleMovePlayer$move(MoverType type, Vec3 movement)
	{
		final float scale = ScaleUtils.getMotionScale(player);
		
		return scale != 1.0F ? movement.scale(1.0F / scale) : movement;
	}
}
