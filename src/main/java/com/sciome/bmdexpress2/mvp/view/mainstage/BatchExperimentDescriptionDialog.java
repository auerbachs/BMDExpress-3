package com.sciome.bmdexpress2.mvp.view.mainstage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sciome.bmdexpress2.mvp.model.DoseResponseExperiment;
import com.sciome.bmdexpress2.mvp.model.info.ExperimentDescription;
import com.sciome.bmdexpress2.mvp.model.info.TestArticleIdentifier;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Window;

/**
 * Dialog for batch editing experimental metadata for multiple experiments.
 * Shows all experiments with their individual fields in a scrollable view.
 * Uses the unified ExperimentDescription class.
 */
public class BatchExperimentDescriptionDialog extends Dialog<Map<DoseResponseExperiment, ExperimentDescription>> {

	private List<DoseResponseExperiment> experiments;
	private Map<DoseResponseExperiment, ExperimentFields> fieldMap;

	/**
	 * Inner class to hold fields for one experiment
	 */
	private static class ExperimentFields {
		TextField testArticleField;
		ComboBox<String> speciesField;
		ComboBox<String> strainField;
		TextField sexField;
		ComboBox<String> organField;
		ExperimentDescription initialDescription;
	}

	/**
	 * Create batch edit dialog
	 */
	public BatchExperimentDescriptionDialog(Window owner, List<DoseResponseExperiment> experiments) {
		this.experiments = experiments;
		this.fieldMap = new HashMap<>();

		initOwner(owner);
		initModality(Modality.APPLICATION_MODAL);
		setTitle("Edit Experiment Metadata (Batch)");
		setHeaderText("Editing metadata for " + experiments.size() + " experiments");

		// Create scrollable content
		VBox mainContent = new VBox(15);
		mainContent.setPadding(new Insets(10));

		// Add fields for each experiment
		for (DoseResponseExperiment experiment : experiments) {
			GridPane expGrid = createExperimentFields(experiment);
			mainContent.getChildren().add(expGrid);
		}

		// Wrap in scroll pane
		ScrollPane scrollPane = new ScrollPane(mainContent);
		scrollPane.setFitToWidth(true);
		scrollPane.setPrefHeight(600);
		scrollPane.setMaxHeight(600);

		getDialogPane().setContent(scrollPane);
		getDialogPane().setPrefWidth(700);

		// Add OK and Cancel buttons
		ButtonType okButtonType = new ButtonType("OK", ButtonData.OK_DONE);
		ButtonType cancelButtonType = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);

		// Disable OK button initially if any experiment has missing required fields
		javafx.scene.Node okButton = getDialogPane().lookupButton(okButtonType);
		okButton.setDisable(!areAllRequiredFieldsFilled());

		// Add listeners to enable/disable OK button based on field validation for all experiments
		for (ExperimentFields fields : fieldMap.values()) {
			fields.speciesField.valueProperty().addListener((obs, old, newVal) -> {
				okButton.setDisable(!areAllRequiredFieldsFilled());
			});
			fields.strainField.valueProperty().addListener((obs, old, newVal) -> {
				okButton.setDisable(!areAllRequiredFieldsFilled());
			});
			fields.sexField.textProperty().addListener((obs, old, newVal) -> {
				okButton.setDisable(!areAllRequiredFieldsFilled());
			});
			fields.organField.valueProperty().addListener((obs, old, newVal) -> {
				okButton.setDisable(!areAllRequiredFieldsFilled());
			});
		}

		// Convert the result when OK is clicked
		setResultConverter(dialogButton -> {
			if (dialogButton == okButtonType) {
				Map<DoseResponseExperiment, ExperimentDescription> results = new HashMap<>();
				for (DoseResponseExperiment exp : experiments) {
					ExperimentFields fields = fieldMap.get(exp);
					ExperimentDescription result = new ExperimentDescription();
					result.setSubjectType("in vivo"); // Default to in vivo

					// Set test article
					String testArticleName = getTextOrNull(fields.testArticleField.getText());
					if (testArticleName != null) {
						TestArticleIdentifier testArticle = new TestArticleIdentifier();
						testArticle.setName(testArticleName);
						result.setTestArticle(testArticle);
					}

					result.setSpecies(getTextOrNull(fields.speciesField.getValue()));
					result.setStrain(getTextOrNull(fields.strainField.getValue()));
					result.setSex(getTextOrNull(fields.sexField.getText()));
					result.setOrgan(getTextOrNull(fields.organField.getValue()));
					results.put(exp, result);
				}
				return results;
			}
			return null;
		});
	}

	/**
	 * Create fields for one experiment
	 */
	private GridPane createExperimentFields(DoseResponseExperiment experiment) {
		ExperimentFields fields = new ExperimentFields();
		fieldMap.put(experiment, fields);

		// Get current description or create a new one
		ExperimentDescription currentDesc = experiment.getExperimentDescription();
		if (currentDesc != null) {
			fields.initialDescription = currentDesc;
		} else {
			fields.initialDescription = new ExperimentDescription();
			fields.initialDescription.setSubjectType("in vivo");
		}

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(8);
		grid.setPadding(new Insets(10));
		grid.setStyle("-fx-border-color: lightgray; -fx-border-width: 1;");

		int row = 0;

		// Experiment name header
		Label nameLabel = new Label(experiment.getName());
		nameLabel.setFont(Font.font(null, FontWeight.BOLD, 14));
		grid.add(nameLabel, 0, row++, 2, 1);

		// Test Article
		grid.add(new Label("Test Article:"), 0, row);
		fields.testArticleField = new TextField();
		fields.testArticleField.setPromptText("e.g., Chemical name");
		fields.testArticleField.setPrefWidth(400);
		if (fields.initialDescription.getTestArticle() != null && fields.initialDescription.getTestArticle().getName() != null) {
			fields.testArticleField.setText(fields.initialDescription.getTestArticle().getName());
		}
		grid.add(fields.testArticleField, 1, row++);

		// Species
		grid.add(new Label("Species: *"), 0, row);
		fields.speciesField = new ComboBox<>();
		fields.speciesField.setItems(FXCollections.observableArrayList(ExperimentDescription.SPECIES_VOCABULARY));
		fields.speciesField.setEditable(true);
		fields.speciesField.setPrefWidth(400);
		if (fields.initialDescription.getSpecies() != null) {
			fields.speciesField.setValue(fields.initialDescription.getSpecies());
		}
		// No default value - user must select
		grid.add(fields.speciesField, 1, row++);

		// Strain
		grid.add(new Label("Strain: *"), 0, row);
		fields.strainField = new ComboBox<>();
		fields.strainField.setEditable(true);
		fields.strainField.setPrefWidth(400);

		// Initialize strain dropdown based on selected species
		String selectedSpecies = fields.speciesField.getValue();
		updateStrainOptions(fields.strainField, selectedSpecies);

		// Set initial strain value only if from file
		if (fields.initialDescription.getStrain() != null) {
			fields.strainField.setValue(fields.initialDescription.getStrain());
		}
		// No default value - user must select

		// Add listener to update strain options when species changes
		fields.speciesField.valueProperty().addListener((observable, oldValue, newValue) -> {
			String currentStrain = fields.strainField.getValue();
			updateStrainOptions(fields.strainField, newValue);

			// If current strain is not in the new list and is not a custom value, clear it
			if (currentStrain != null && !fields.strainField.getItems().contains(currentStrain)) {
				if (oldValue != null && ExperimentDescription.getStrainsForSpecies(oldValue).contains(currentStrain)) {
					fields.strainField.setValue(null);
				}
			}
			// No auto-filling of default strain - user must select
		});

		grid.add(fields.strainField, 1, row++);

		// Sex
		grid.add(new Label("Sex: *"), 0, row);
		fields.sexField = new TextField();
		fields.sexField.setPromptText("e.g., Male, Female, Both");
		fields.sexField.setPrefWidth(400);
		if (fields.initialDescription.getSex() != null) {
			fields.sexField.setText(fields.initialDescription.getSex());
		}
		// No default value - user must enter
		grid.add(fields.sexField, 1, row++);

		// Organ
		grid.add(new Label("Organ: *"), 0, row);
		fields.organField = new ComboBox<>();
		fields.organField.setItems(FXCollections.observableArrayList(ExperimentDescription.ORGAN_VOCABULARY));
		fields.organField.setEditable(true);
		fields.organField.setPrefWidth(400);
		if (fields.initialDescription.getOrgan() != null) {
			fields.organField.setValue(fields.initialDescription.getOrgan());
		}
		// No default value - user must select
		grid.add(fields.organField, 1, row++);

		return grid;
	}

	/**
	 * Get text from field or null if empty
	 */
	private String getTextOrNull(String text) {
		if (text == null || text.trim().isEmpty()) {
			return null;
		}
		return text.trim();
	}

	/**
	 * Update strain dropdown options based on selected species
	 */
	private void updateStrainOptions(ComboBox<String> strainField, String species) {
		List<String> strains = ExperimentDescription.getStrainsForSpecies(species);
		strainField.setItems(FXCollections.observableArrayList(strains));
	}

	/**
	 * Check if all required fields are filled for all experiments
	 */
	private boolean areAllRequiredFieldsFilled() {
		for (ExperimentFields fields : fieldMap.values()) {
			if (getTextOrNull(fields.speciesField.getValue()) == null ||
				getTextOrNull(fields.strainField.getValue()) == null ||
				getTextOrNull(fields.sexField.getText()) == null ||
				getTextOrNull(fields.organField.getValue()) == null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Static method to show dialog and get results
	 */
	public static Map<DoseResponseExperiment, ExperimentDescription> showDialog(
			Window owner, List<DoseResponseExperiment> experiments) {
		BatchExperimentDescriptionDialog dialog = new BatchExperimentDescriptionDialog(owner, experiments);
		return dialog.showAndWait().orElse(null);
	}
}
