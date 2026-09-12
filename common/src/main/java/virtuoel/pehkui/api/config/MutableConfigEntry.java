package virtuoel.pehkui.api.config;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface MutableConfigEntry<T> extends Supplier<T>, Consumer<T>
{
	T getValue();
	
	void setValue(T value);
}
