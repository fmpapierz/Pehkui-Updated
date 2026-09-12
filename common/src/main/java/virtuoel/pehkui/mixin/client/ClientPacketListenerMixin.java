package virtuoel.pehkui.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import virtuoel.pehkui.util.ScaleUtils;

/**
 * The client builds a fresh player on respawn, so the scales are carried across before the old
 * one is discarded. The server does the same thing on its side.
 */
@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin
{
	@Inject(method = "handleRespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setCameraEntity(Lnet/minecraft/world/entity/Entity;)V", ordinal = 1))
	private void pehkui$handleRespawn(ClientboundRespawnPacket packet, CallbackInfo info, @Local(ordinal = 0) LocalPlayer oldPlayer, @Local(ordinal = 1) LocalPlayer newPlayer)
	{
		ScaleUtils.loadScaleOnRespawn(newPlayer, oldPlayer, packet.shouldKeep(ClientboundRespawnPacket.KEEP_ATTRIBUTE_MODIFIERS));
	}
}
