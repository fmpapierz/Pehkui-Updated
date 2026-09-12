package virtuoel.pehkui.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import virtuoel.pehkui.Pehkui;
import virtuoel.pehkui.server.command.DebugCommand;

public class DebugPayload implements CustomPacketPayload
{
	public static final CustomPacketPayload.Type<DebugPayload> TYPE = new CustomPacketPayload.Type<>(Pehkui.DEBUG_PACKET);
	public static final StreamCodec<RegistryFriendlyByteBuf, DebugPayload> CODEC = CustomPacketPayload.codec(DebugPayload::write, DebugPayload::new);
	
	public final DebugCommand.PacketType packetType;
	
	public DebugPayload(final DebugCommand.PacketType packetType)
	{
		this.packetType = packetType;
	}
	
	public DebugPayload(final FriendlyByteBuf buf)
	{
		DebugCommand.PacketType read;
		
		try
		{
			read = buf.readEnum(DebugCommand.PacketType.class);
		}
		catch (Exception e)
		{
			read = null;
		}
		
		this.packetType = read;
	}
	
	public void write(final FriendlyByteBuf buf)
	{
		buf.writeEnum(this.packetType);
	}
	
	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type()
	{
		return TYPE;
	}
}
