package virtuoel.pehkui.api.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import virtuoel.pehkui.Pehkui;

/**
 * Reads and writes a single flat JSON object on disk, lazily. Replaces the external
 * config library the Fabric-only builds of Pehkui depended on, which has no 26.2 release.
 */
public class JsonConfigHandler
{
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	
	private final Path path;
	private JsonObject cached = null;
	
	public JsonConfigHandler(final Path path)
	{
		this.path = path;
	}
	
	public Path getPath()
	{
		return this.path;
	}
	
	public synchronized JsonObject get()
	{
		if (this.cached == null)
		{
			this.cached = read();
		}
		
		return this.cached;
	}
	
	public synchronized void invalidate()
	{
		this.cached = null;
	}
	
	/**
	 * Called when an entry has been modified in memory. The file is rewritten so the change survives a restart.
	 */
	public synchronized void onConfigChanged()
	{
		save();
	}
	
	public synchronized void save()
	{
		if (this.cached == null)
		{
			return;
		}
		
		try
		{
			final Path parent = this.path.getParent();
			
			if (parent != null)
			{
				Files.createDirectories(parent);
			}
			
			try (BufferedWriter writer = Files.newBufferedWriter(this.path, StandardCharsets.UTF_8))
			{
				GSON.toJson(this.cached, writer);
			}
		}
		catch (final IOException e)
		{
			Pehkui.LOGGER.warn("Failed to write Pehkui's config file at {}: {}", this.path, e.toString());
		}
	}
	
	private JsonObject read()
	{
		if (!Files.isRegularFile(this.path))
		{
			return new JsonObject();
		}
		
		try (BufferedReader reader = Files.newBufferedReader(this.path, StandardCharsets.UTF_8))
		{
			final JsonElement parsed = JsonParser.parseReader(reader);
			
			if (parsed != null && parsed.isJsonObject())
			{
				return parsed.getAsJsonObject();
			}
			
			Pehkui.LOGGER.warn("Pehkui's config file at {} is not a JSON object. Falling back to defaults.", this.path);
		}
		catch (final Exception e)
		{
			Pehkui.LOGGER.warn("Failed to read Pehkui's config file at {}: {}", this.path, e.toString());
		}
		
		return new JsonObject();
	}
}
