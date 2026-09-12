package virtuoel.pehkui.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import virtuoel.pehkui.Pehkui;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.util.ScaleUtils;

public class ScalePayload implements CustomPacketPayload
{
	public static final CustomPacketPayload.Type<ScalePayload> TYPE = new CustomPacketPayload.Type<>(Pehkui.SCALE_PACKET);
	public static final StreamCodec<RegistryFriendlyByteBuf, ScalePayload> CODEC = CustomPacketPayload.codec(ScalePayload::write, ScalePayload::new);
	
	public final int entityId;
	public final Collection<ScaleData> scales = new ArrayList<>();
	public final Map<Identifier, CompoundTag> syncedScales = new HashMap<>();
	
	public ScalePayload(final Entity entity, final Collection<ScaleData> scales)
	{
		this.entityId = entity.getId();
		this.scales.addAll(scales);
	}
	
	public ScalePayload(final FriendlyByteBuf buf)
	{
		this.entityId = buf.readVarInt();
		
		for (int i = buf.readInt(); i > 0; i--)
		{
			final Identifier typeId = buf.readIdentifier();
			
			final CompoundTag scaleData = ScaleUtils.buildScaleNbtFromByteBuf(buf);
			
			this.syncedScales.put(typeId, scaleData);
		}
	}
	
	public void write(final FriendlyByteBuf buf)
	{
		buf.writeVarInt(this.entityId);
		((ByteBuf) buf).writeInt(this.scales.size());
		
		for (final ScaleData s : this.scales)
		{
			buf.writeIdentifier(ScaleRegistries.getId(ScaleRegistries.SCALE_TYPES, s.getScaleType()));
			s.toPacket(buf);
		}
	}
	
	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type()
	{
		return TYPE;
	}
}
