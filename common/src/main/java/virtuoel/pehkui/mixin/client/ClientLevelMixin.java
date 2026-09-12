package virtuoel.pehkui.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.client.multiplayer.ClientLevel;
import virtuoel.pehkui.util.ScaleRenderUtils;

@Mixin(ClientLevel.class)
public class ClientLevelMixin
{
	@Inject(method = "fillReportDetails", at = @At("RETURN"))
	private void pehkui$fillReportDetails(CrashReport report, CallbackInfoReturnable<CrashReportCategory> info)
	{
		ScaleRenderUtils.addDetailsToCrashReport(info.getReturnValue());
	}
}
