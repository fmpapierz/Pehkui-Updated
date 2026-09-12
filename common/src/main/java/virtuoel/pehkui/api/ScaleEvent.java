package virtuoel.pehkui.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A minimal array-backed event, mirroring the shape Pehkui's API has always exposed
 * ({@code getScaleChangedEvent().register(callback)} / {@code ...invoker().onEvent(data)})
 * without depending on any one loader's event system.
 */
public final class ScaleEvent
{
	private final List<ScaleEventCallback> callbacks = new ArrayList<>();
	private volatile ScaleEventCallback invoker = data -> {};
	
	public void register(final ScaleEventCallback callback)
	{
		if (callback == null)
		{
			throw new NullPointerException("Tried to register a null scale event callback.");
		}
		
		synchronized (this.callbacks)
		{
			this.callbacks.add(callback);
			this.invoker = createInvoker(new ArrayList<>(this.callbacks));
		}
	}
	
	public ScaleEventCallback invoker()
	{
		return this.invoker;
	}
	
	public List<ScaleEventCallback> getCallbacks()
	{
		synchronized (this.callbacks)
		{
			return Collections.unmodifiableList(new ArrayList<>(this.callbacks));
		}
	}
	
	private static ScaleEventCallback createInvoker(final List<ScaleEventCallback> callbacks)
	{
		if (callbacks.isEmpty())
		{
			return data -> {};
		}
		
		if (callbacks.size() == 1)
		{
			return callbacks.get(0);
		}
		
		final ScaleEventCallback[] array = callbacks.toArray(new ScaleEventCallback[0]);
		
		return data ->
		{
			for (final ScaleEventCallback callback : array)
			{
				callback.onEvent(data);
			}
		};
	}
}
