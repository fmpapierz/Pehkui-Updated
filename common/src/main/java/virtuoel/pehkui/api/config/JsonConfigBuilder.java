package virtuoel.pehkui.api.config;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

	private final Map<String, Declared<?>> declared = new LinkedHashMap<>();

	public JsonConfigBuilder(final String namespace, final Path path)
	{
		this.namespace = namespace;
		this.config = new JsonConfigHandler(path);
	}

	/**
	 * The kinds of entry a config file holds, so that a screen can pick an editor for each one
	 * without having to guess from the value.
	 */
	public enum Kind
	{
		BOOLEAN,
		DOUBLE,
		STRING_LIST
	}

	/**
	 * One declared entry: its flat key, what it holds, and the entry itself.
	 */
	public record Declared<T> (String name, Kind kind, T defaultValue, MutableConfigEntry<T> entry)
	{

	}

	/**
	 * Every entry declared so far, in declaration order.
	 */
	public Collection<Declared<?>> getDeclaredEntries()
	{
		return Collections.unmodifiableCollection(this.declared.values());
	}

	private <T> MutableConfigEntry<T> declare(final String name, final Kind kind, final T defaultValue, final MutableConfigEntry<T> entry)
	{
		this.declared.put(name, new Declared<>(name, kind, defaultValue, entry));

		return entry;
	}

	public MutableConfigEntry<Boolean> booleanConfig(final String name, final boolean defaultValue)
	{
		return declare(name, Kind.BOOLEAN, defaultValue, createConfigEntry(
			name,
			defaultValue,
			() ->
			{
				final JsonElement element = get(name);
				
				return element != null && element.isJsonPrimitive() ? element.getAsBoolean() : defaultValue;
			},
			value -> set(name, new JsonPrimitive(value == null ? defaultValue : value))
		));
	}

	public MutableConfigEntry<Double> doubleConfig(final String name, final double defaultValue)
	{
		return declare(name, Kind.DOUBLE, defaultValue, createConfigEntry(
			name,
			defaultValue,
			() ->
			{
				final JsonElement element = get(name);

				return element != null && element.isJsonPrimitive() ? element.getAsDouble() : defaultValue;
			},
			value -> set(name, new JsonPrimitive(value == null ? defaultValue : value))
		));
	}

	public MutableConfigEntry<List<String>> stringListConfig(final String name)
	{
		return declare(name, Kind.STRING_LIST, Collections.<String>emptyList(), createConfigEntry(
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
		));
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
