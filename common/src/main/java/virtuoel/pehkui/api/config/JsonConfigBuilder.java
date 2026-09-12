package virtuoel.pehkui.api.config;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Declares config entries backed by a {@link JsonConfigHandler}. Entries are flat keys, so a
 * scale clamp reads as {@code "width.minimum"} in the file rather than as nested objects.
 */
public class JsonConfigBuilder
{
	public final String namespace;
	public final JsonConfigHandler config;
	
	public JsonConfigBuilder(final String namespace, final Path path)
	{
		this.namespace = namespace;
		this.config = new JsonConfigHandler(path);
	}
	
	public MutableConfigEntry<Boolean> booleanConfig(final String name, final boolean defaultValue)
	{
		return createConfigEntry(
			name,
			defaultValue,
			() ->
			{
				final JsonElement element = get(name);
				
				return element != null && element.isJsonPrimitive() ? element.getAsBoolean() : defaultValue;
			},
			value -> set(name, new JsonPrimitive(value == null ? defaultValue : value))
		);
	}
	
	public MutableConfigEntry<Double> doubleConfig(final String name, final double defaultValue)
	{
		return createConfigEntry(
			name,
			defaultValue,
			() ->
			{
				final JsonElement element = get(name);
				
				return element != null && element.isJsonPrimitive() ? element.getAsDouble() : defaultValue;
			},
			value -> set(name, new JsonPrimitive(value == null ? defaultValue : value))
		);
	}
	
	public MutableConfigEntry<List<String>> stringListConfig(final String name)
	{
		return createConfigEntry(
			name,
			Collections.<String>emptyList(),
			() ->
			{
				final JsonElement element = get(name);
				
				if (element == null || !element.isJsonArray())
				{
					return Collections.<String>emptyList();
				}
				
				final List<String> values = new ArrayList<>();
				
				for (final JsonElement entry : element.getAsJsonArray())
				{
					if (entry.isJsonPrimitive())
					{
						values.add(entry.getAsString());
					}
				}
				
				return Collections.unmodifiableList(values);
			},
			value ->
			{
				final JsonArray array = new JsonArray();
				
				if (value != null)
				{
					for (final String entry : value)
					{
						array.add(entry);
					}
				}
				
				set(name, array);
			}
		);
	}
	
	/**
	 * Overridden by {@code PehkuiConfig} so that every entry it declares can also be server-synced.
	 */
	public <T> MutableConfigEntry<T> createConfigEntry(final String name, final T defaultValue, final Supplier<T> supplier, final Consumer<T> consumer)
	{
		return new SimpleConfigEntry<>(defaultValue, supplier, consumer);
	}
	
	private JsonElement get(final String name)
	{
		final JsonObject object = this.config.get();
		
		return object.has(name) ? object.get(name) : null;
	}
	
	private void set(final String name, final JsonElement value)
	{
		this.config.get().add(name, value);
		this.config.onConfigChanged();
	}
	
	private static final class SimpleConfigEntry<T> implements MutableConfigEntry<T>
	{
		private final T defaultValue;
		private final Supplier<T> supplier;
		private final Consumer<T> consumer;
		
		private SimpleConfigEntry(final T defaultValue, final Supplier<T> supplier, final Consumer<T> consumer)
		{
			this.defaultValue = defaultValue;
			this.supplier = supplier;
			this.consumer = consumer;
		}
		
		@Override
		public T get()
		{
			final T value = this.supplier.get();
			
			return value == null ? this.defaultValue : value;
		}
		
		@Override
		public void accept(final T value)
		{
			this.consumer.accept(value);
		}
		
		@Override
		public T getValue()
		{
			return get();
		}
		
		@Override
		public void setValue(final T value)
		{
			accept(value);
		}
	}
}
