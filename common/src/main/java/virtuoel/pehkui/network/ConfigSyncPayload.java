package virtuoel.pehkui.network;

import java.util.Collection;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import virtuoel.pehkui.Pehkui;
import virtuoel.pehkui.util.ConfigSyncUtils;
import virtuoel.pehkui.util.ConfigSyncUtils.SyncableConfigEntry;

public class ConfigSyncPayload implements CustomPacketPayload
{
	public static final CustomPacketPayload.Type<ConfigSyncPayload> TYPE = new CustomPacketPayload.Type<>(Pehkui.CONFIG_SYNC_PACKET);
	public static final StreamCodec<RegistryFriendlyByteBuf, ConfigSyncPayload> CODEC = CustomPacketPayload.codec(ConfigSyncPayload::write, ConfigSyncPayload::new);
	
	public Collection<SyncableConfigEntry<?>> configEntries;
	public Runnable action;
	
	public ConfigSyncPayload(final Collection<SyncableConfigEntry<?>> configEntries)
	{
		this.configEntries = configEntries;
	}
	
	public ConfigSyncPayload(final FriendlyByteBuf buf)
	{
		this.action = ConfigSyncUtils.readConfigs(buf);
	}
	
	public void write(final FriendlyByteBuf buf)
	{
		ConfigSyncUtils.write(this.configEntries, buf);
	}
	
	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type()
	{
		return TYPE;
	}
}
