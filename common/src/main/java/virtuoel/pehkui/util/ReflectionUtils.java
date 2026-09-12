package virtuoel.pehkui.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import virtuoel.pehkui.Pehkui;

/**
 * Soft-dependency plumbing. Pehkui reaches into a handful of optional mods that it does not
 * compile against; everything here degrades to an empty {@link Optional} when they are absent.
 */
public class ReflectionUtils
{
	public static Optional<Class<?>> getClass(final String... names)
	{
		for (final String name : names)
		{
			try
			{
				return Optional.of(Class.forName(name));
			}
			catch (final ClassNotFoundException e)
			{
				continue;
			}
		}

		return Optional.empty();
	}

	public static Optional<Method> getMethod(final Optional<Class<?>> owner, final String name, final Class<?>... parameterTypes)
	{
		return owner.flatMap(c ->
		{
			try
			{
				final Method method = c.getMethod(name, parameterTypes);
				method.setAccessible(true);

				return Optional.of(method);
			}
			catch (final NoSuchMethodException | RuntimeException e)
			{
				return Optional.empty();
			}
		});
	}

	public static Optional<Field> getField(final Optional<Class<?>> owner, final String name)
	{
		return owner.flatMap(c ->
		{
			try
			{
				final Field field = c.getField(name);
				field.setAccessible(true);

				return Optional.of(field);
			}
			catch (final NoSuchFieldException | RuntimeException e)
			{
				return Optional.empty();
			}
		});
	}

	public static void setField(final Optional<Class<?>> owner, final String name, final Object instance, final Object value)
	{
		getField(owner, name).ifPresent(f ->
		{
			try
			{
				f.set(instance, value);
			}
			catch (final IllegalArgumentException | IllegalAccessException e)
			{
				Pehkui.LOGGER.catching(e);
			}
		});
	}

	private ReflectionUtils()
	{

	}
}
