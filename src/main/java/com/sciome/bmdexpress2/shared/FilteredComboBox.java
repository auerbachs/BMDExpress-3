package com.sciome.bmdexpress2.shared;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

/**
 * A ComboBox that filters its items as the user types.
 * 
 * @param <T>
 *            The type of items in the ComboBox
 */
public class FilteredComboBox<T> extends ComboBox<T>
{

	private FilteredList<T> filteredList;
	private boolean isSetupComplete = false;

	public FilteredComboBox()
	{
		super();
		System.out.println("FilteredComboBox created");
		initialize();
	}

	private void initialize()
	{
		setEditable(true);
		System.out.println("FilteredComboBox initialized, editable: " + isEditable());

		// Listen for when items are set from FXML or code
		itemsProperty().addListener((obs, oldItems, newItems) ->
		{
			System.out.println("Items changed. Old: " + (oldItems != null ? oldItems.size() : "null")
					+ ", New: " + (newItems != null ? newItems.size() : "null"));
			if (!isSetupComplete && newItems != null && !(newItems instanceof FilteredList))
			{
				System.out.println("Setting up filtering...");
				setupFiltering(newItems);
				isSetupComplete = true;
			}
		});
	}

	private void setupFiltering(ObservableList<T> sourceList)
	{
		System.out.println("setupFiltering called with " + sourceList.size() + " items");

		// Wrap the items in a FilteredList
		filteredList = new FilteredList<>(sourceList, p -> true);

		// Replace items with filtered list
		itemsProperty().unbind();
		super.setItems(filteredList);

		System.out.println("FilteredList created and set");

		// Filter as user types
		getEditor().textProperty().addListener((obs, oldValue, newValue) ->
		{
			System.out.println("Text changed: '" + oldValue + "' -> '" + newValue + "'");

			final T selected = getSelectionModel().getSelectedItem();

			// Don't filter if user just selected an item
			if (selected != null)
			{
				String selectedText = getConverter() != null ? getConverter().toString(selected)
						: selected.toString();
				if (selectedText.equals(newValue))
				{
					System.out.println("Text matches selected item, not filtering");
					filteredList.setPredicate(p -> true);
					return;
				}
			}

			// Apply filter
			if (newValue == null || newValue.isEmpty())
			{
				System.out.println("Empty text, showing all items");
				filteredList.setPredicate(p -> true);
			}
			else
			{
				String lowerCaseFilter = newValue.toLowerCase();
				filteredList.setPredicate(item ->
				{
					String itemText = getConverter() != null ? getConverter().toString(item)
							: (item != null ? item.toString() : "");
					boolean matches = itemText.toLowerCase().contains(lowerCaseFilter);
					return matches;
				});
				System.out.println("Filtered to " + filteredList.size() + " items");
			}

			// Show dropdown with filtered results
			if (!filteredList.isEmpty())
			{
				if (!isShowing())
				{
					System.out.println("Showing dropdown");
					show();
				}
			}
			else
			{
				System.out.println("No matches, hiding dropdown");
				hide();
			}
		});

		System.out.println("Text listener added");

		// Handle focus loss
		focusedProperty().addListener((obs, wasFocused, isNowFocused) ->
		{
			if (!isNowFocused)
			{
				handleFocusLost();
			}
		});
	}

	private void handleFocusLost()
	{
		if (filteredList == null)
			return;

		String text = getEditor().getText();
		@SuppressWarnings("unchecked")
		ObservableList<T> sourceList = (ObservableList<T>) filteredList.getSource();

		if (text == null || text.trim().isEmpty())
		{
			getSelectionModel().clearSelection();
			filteredList.setPredicate(p -> true);
			return;
		}

		StringConverter<T> converter = getConverter();

		// Try exact match first, then partial match
		T match = sourceList.stream().filter(item ->
		{
			String itemText = converter != null ? converter.toString(item)
					: (item != null ? item.toString() : "");
			return itemText.equalsIgnoreCase(text);
		}).findFirst().orElse(sourceList.stream().filter(item ->
		{
			String itemText = converter != null ? converter.toString(item)
					: (item != null ? item.toString() : "");
			return itemText.toLowerCase().contains(text.toLowerCase());
		}).findFirst().orElse(null));

		if (match != null)
		{
			getSelectionModel().select(match);
		}
		else
		{
			getSelectionModel().clearSelection();
			getEditor().clear();
		}

		// Reset filter to show all
		filteredList.setPredicate(p -> true);
	}
}