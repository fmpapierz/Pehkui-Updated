package virtuoel.pehkui.mixin;

import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions;
import net.minecraft.network.chat.Component;

@Mixin(EntitySelectorOptions.class)
public interface EntitySelectorOptionsInvoker
{
	@Invoker("register")
	public static void callRegister(String id, EntitySelectorOptions.Modifier handler, Predicate<EntitySelectorParser> condition, Component description)
	{
		throw new NoSuchMethodError();
	}
}
