package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.DataCommands;

@Mixin(DataCommands.class)
public interface DataCommandInvoker
{
	@Invoker("getData")
	public static int callGetData(CommandSourceStack source, DataAccessor accessor)
	{
		throw new NoSuchMethodError();
	}
	
	@Invoker("getData")
	public static int callGetData(CommandSourceStack source, DataAccessor accessor, NbtPathArgument.NbtPath path)
	{
		throw new NoSuchMethodError();
	}
	
	@Invoker("getNumeric")
	public static int callGetNumeric(CommandSourceStack source, DataAccessor accessor, NbtPathArgument.NbtPath path, double scale)
	{
		throw new NoSuchMethodError();
	}
}
