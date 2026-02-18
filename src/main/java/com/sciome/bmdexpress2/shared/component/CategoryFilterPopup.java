package com.sciome.bmdexpress2.shared.component;

import com.sciome.bmdexpress2.mvp.model.category.CategoryAnalysisInputToShowOrHide;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PopupControl;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

public class CategoryFilterPopup extends PopupControl
{

	private final ObservableList<CategoryAnalysisInputToShowOrHide> items = FXCollections
			.observableArrayList();

	private final ListView<CategoryAnalysisInputToShowOrHide> listView = new ListView<>();

	public CategoryFilterPopup()
	{
		initialize();
	}

	public CategoryFilterPopup(ObservableList<CategoryAnalysisInputToShowOrHide> items)
	{
		this.items.setAll(items);
		initialize();
	}

	private void initialize()
	{

		listView.setItems(items);
		listView.setFixedCellSize(-1);

		listView.setCellFactory(lv -> new ListCell<>() {

			private final CheckBox checkBox = new CheckBox();
			private final HBox container = new HBox(8, checkBox);

			private CategoryAnalysisInputToShowOrHide currentItem;

			{
				container.setPadding(new Insets(4));
			}

			@Override
			protected void updateItem(CategoryAnalysisInputToShowOrHide item, boolean empty)
			{
				super.updateItem(item, empty);

				if (currentItem != null)
				{
					checkBox.textProperty().unbind();
					checkBox.selectedProperty().unbindBidirectional(currentItem.showMeProperty());
				}

				if (empty || item == null)
				{
					currentItem = null;
					setGraphic(null);
				}
				else
				{
					currentItem = item;

					checkBox.textProperty().bind(item.nameProperty());
					checkBox.selectedProperty().bindBidirectional(item.showMeProperty());

					setGraphic(container);
				}
			}
		});

		listView.setPrefHeight(550);

		Button closeButton = new Button("✕");
		closeButton.setOnAction(e -> hide());
		closeButton.setFocusTraversable(false);
		closeButton.setStyle("""
				-fx-background-color: transparent;
				-fx-font-size: 14;
				-fx-text-fill: #666;
				""");

		closeButton.setOnMouseEntered(e -> closeButton.setStyle("""
				-fx-background-color: transparent;
				-fx-font-size: 14;
				-fx-text-fill: black;
				"""));

		closeButton.setOnMouseExited(e -> closeButton.setStyle("""
				-fx-background-color: transparent;
				-fx-font-size: 14;
				-fx-text-fill: #666;
				"""));

		HBox topBar = new HBox();
		Region spacer = new Region();
		HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

		topBar.getChildren().addAll(spacer, closeButton);
		topBar.setAlignment(Pos.CENTER_RIGHT);

		BorderPane root = new BorderPane();
		root.setTop(topBar);
		root.setCenter(listView);

		root.setPadding(new Insets(10));
		root.setStyle("""
				-fx-background-color: white;
				-fx-border-color: #ccc;
				-fx-border-radius: 5;
				-fx-background-radius: 5;
				""");

		root.prefWidthProperty().bind(Bindings.max(listView.prefWidthProperty().add(20), 550));

		getScene().setRoot(root);

		setAutoHide(true);
		setHideOnEscape(true);
	}

	public ObservableList<CategoryAnalysisInputToShowOrHide> getItems()
	{
		return items;
	}

	public void setItems(ObservableList<CategoryAnalysisInputToShowOrHide> items)
	{
		this.items.setAll(items);
	}

	public void show(Node owner)
	{
		if (!isShowing())
		{
			var bounds = owner.localToScreen(owner.getBoundsInLocal());
			show(owner, bounds.getMinX(), bounds.getMaxY());
		}
	}
}
