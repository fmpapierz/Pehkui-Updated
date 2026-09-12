package virtuoel.pehkui.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import virtuoel.pehkui.api.PehkuiConfig;
import virtuoel.pehkui.api.config.JsonConfigBuilder;
import virtuoel.pehkui.api.config.MutableConfigEntry;

/**
 * Edits the entries in {@code config/pehkui/config.json} in place. There are close to a hundred of
 * them once every scale type's clamps are counted, so they are filtered and paged rather than
 * crammed onto one screen, and each row is named with the exact key it writes to the file.
 */
public class PehkuiConfigScreen extends Screen
{
	private static final int ROW_HEIGHT = 24;
	private static final int LIST_TOP = 56;
	private static final int FOOTER_HEIGHT = 56;

	private final @Nullable Screen parent;
	private final List<JsonConfigBuilder.Declared<?>> all;

	private List<JsonConfigBuilder.Declared<?>> shown;
	private String filter = "";
	private int page = 0;
	private boolean rebuildQueued = false;

	public PehkuiConfigScreen(@Nullable final Screen parent)
	{
		super(Component.literal("Pehkui"));

		this.parent = parent;
		this.all = new ArrayList<>(PehkuiConfig.BUILDER.getDeclaredEntries());
		this.shown = this.all;
	}

	@Override
	protected void init()
	{
		final int contentWidth = Math.min(420, this.width - 40);
		final int left = (this.width - contentWidth) / 2;

		final EditBox search = new EditBox(this.font, left, 26, contentWidth, 18, Component.literal("Search"));
		search.setValue(this.filter);
		search.setHint(Component.literal("Search").withStyle(ChatFormatting.DARK_GRAY));
		search.setResponder(value ->
		{
			if (!value.equals(this.filter))
			{
				this.filter = value;
				this.page = 0;
				// Rebuilding mid-keystroke would pull this box out from under the event that is
				// still being delivered to it, so the rows are rebuilt on the next tick instead.
				this.rebuildQueued = true;
			}
		});
		this.addRenderableWidget(search);

		this.shown = filtered();

		final int rows = Math.max(1, (this.height - LIST_TOP - FOOTER_HEIGHT) / ROW_HEIGHT);
		final int pages = Math.max(1, (this.shown.size() + rows - 1) / rows);

		this.page = Math.min(this.page, pages - 1);

		final int labelWidth = contentWidth - 130;
		final int controlX = left + contentWidth - 120;

		for (int row = 0; row < rows; row++)
		{
			final int index = this.page * rows + row;

			if (index >= this.shown.size())
			{
				break;
			}

			final JsonConfigBuilder.Declared<?> declared = this.shown.get(index);
			final int y = LIST_TOP + row * ROW_HEIGHT;

			final StringWidget label = new StringWidget(left, y + 5, labelWidth, 10, Component.literal(declared.name()), this.font);
			label.setMaxWidth(labelWidth);
			this.addRenderableWidget(label);

			this.addRenderableWidget(control(declared, controlX, y, 120));
		}

		final int footerY = this.height - 46;

		final Button previous = Button.builder(Component.literal("<"), button ->
		{
			this.page = Math.max(0, this.page - 1);
			this.rebuildWidgets();
		}).bounds(left, footerY, 24, 20).build();
		previous.active = this.page > 0;
		this.addRenderableWidget(previous);

		final Button next = Button.builder(Component.literal(">"), button ->
		{
			this.page = this.page + 1;
			this.rebuildWidgets();
		}).bounds(left + contentWidth - 24, footerY, 24, 20).build();
		next.active = this.page < pages - 1;
		this.addRenderableWidget(next);

		final StringWidget position = new StringWidget(
			left + 30, footerY + 5, contentWidth - 60, 10,
			Component.literal(this.shown.isEmpty() ? "no matches" : ("page " + (this.page + 1) + " of " + pages)),
			this.font
		);
		this.addRenderableWidget(position);

		this.addRenderableWidget(
			Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
				.bounds((this.width - 150) / 2, this.height - 24, 150, 20)
				.build()
		);
	}

	private List<JsonConfigBuilder.Declared<?>> filtered()
	{
		if (this.filter.isEmpty())
		{
			return this.all;
		}

		final String needle = this.filter.toLowerCase(Locale.ROOT);
		final List<JsonConfigBuilder.Declared<?>> matches = new ArrayList<>();

		for (final JsonConfigBuilder.Declared<?> declared : this.all)
		{
			if (declared.name().toLowerCase(Locale.ROOT).contains(needle))
			{
				matches.add(declared);
			}
		}

		return matches;
	}

	private AbstractWidget control(final JsonConfigBuilder.Declared<?> declared, final int x, final int y, final int width)
	{
		switch (declared.kind())
		{
			case BOOLEAN:
			{
				final MutableConfigEntry<Boolean> entry = cast(declared);

				return Button.builder(CommonComponents.optionStatus(entry.getValue()), button ->
				{
					final boolean value = !entry.getValue();

					entry.setValue(value);
					button.setMessage(CommonComponents.optionStatus(value));
				}).bounds(x, y, width, 20).build();
			}
			case DOUBLE:
			{
				final MutableConfigEntry<Double> entry = cast(declared);
				final EditBox box = new EditBox(this.font, x, y, width, 20, Component.literal(declared.name()));

				box.setMaxLength(32);
				box.setValue(format(entry.getValue()));
				box.setResponder(value ->
				{
					try
					{
						entry.setValue(Double.parseDouble(value.trim()));
						box.setTextColor(0xE0E0E0);
					}
					catch (final NumberFormatException e)
					{
						box.setTextColor(0xFF5555);
					}
				});

				return box;
			}
			default:
			{
				final MutableConfigEntry<List<String>> entry = cast(declared);
				final EditBox box = new EditBox(this.font, x, y, width, 20, Component.literal(declared.name()));

				box.setMaxLength(1024);
				box.setValue(String.join(", ", entry.getValue()));
				box.setResponder(value ->
				{
					final List<String> values = new ArrayList<>();

					for (final String part : value.split(","))
					{
						final String trimmed = part.trim();

						if (!trimmed.isEmpty())
						{
							values.add(trimmed);
						}
					}

					entry.setValue(values);
				});

				return box;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> MutableConfigEntry<T> cast(final JsonConfigBuilder.Declared<?> declared)
	{
		return (MutableConfigEntry<T>) declared.entry();
	}

	/**
	 * Trims the trailing zeroes off whole numbers so a clamp of four reads as {@code 4} rather than
	 * {@code 4.0}, while leaving the very small and very large defaults in a form that round-trips.
	 */
	private static String format(final double value)
	{
		if (value == Math.rint(value) && !Double.isInfinite(value) && Math.abs(value) < 1.0E7D)
		{
			return String.valueOf((long) value);
		}

		return String.valueOf(value);
	}

	@Override
	public void tick()
	{
		super.tick();

		if (this.rebuildQueued)
		{
			this.rebuildQueued = false;
			this.rebuildWidgets();
		}
	}

	@Override
	public void onClose()
	{
		if (this.minecraft != null)
		{
			this.minecraft.gui.setScreen(this.parent);
		}
	}

}
