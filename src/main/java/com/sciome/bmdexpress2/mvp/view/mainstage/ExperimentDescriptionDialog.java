package com.sciome.bmdexpress2.mvp.view.mainstage;

import java.util.List;

import com.sciome.bmdexpress2.mvp.model.info.ExperimentDescription;
import com.sciome.bmdexpress2.util.ExperimentDescriptionParser;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Window;

/**
 * Dialog for entering or editing experimental metadata.
 * Displays auto-parsed values if available and allows user editing.
 */
public class ExperimentDescriptionDialog extends Dialog<ExperimentDescription> {

	private TextField testArticleField;
	private ComboBox<String> speciesField;
	private ComboBox<String> strainField;
	private TextField sexField;
	private ComboBox<String> organField;

	private ExperimentDescription initialDescription;

	/**
	 * Create dialog with auto-parsed description
	 */
	public ExperimentDescriptionDialog(Window owner, ExperimentDescription parsedDescription, String filename) {
		this.initialDescription = parsedDescription != null ? parsedDescription : new ExperimentDescription();

		// Parse filename to get defaults for any missing fields
		ExperimentDescription parsedFromFilename = ExperimentDescriptionParser.parseFromString(filename);

		// Merge: use initialDescription if available, otherwise use parsed values
		if (initialDescription.getTestArticle() == null && parsedFromFilename.getTestArticle() != null) {
			initialDescription.setTestArticle(parsedFromFilename.getTestArticle());
		}
		if (initialDescription.getSpecies() == null && parsedFromFilename.getSpecies() != null) {
			initialDescription.setSpecies(parsedFromFilename.getSpecies());
		}
		if (initialDescription.getStrain() == null && parsedFromFilename.getStrain() != null) {
			initialDescription.setStrain(parsedFromFilename.getStrain());
		}
		if (initialDescription.getSex() == null && parsedFromFilename.getSex() != null) {
			initialDescription.setSex(parsedFromFilename.getSex());
		}
		if (initialDescription.getOrgan() == null && parsedFromFilename.getOrgan() != null) {
			initialDescription.setOrgan(parsedFromFilename.getOrgan());
		}

		initOwner(owner);
		initModality(Modality.APPLICATION_MODAL);
		setTitle("Experiment Description");
		setHeaderText("Enter experimental metadata for: " + filename);

		// Create the custom dialog pane
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		// Instructions label
		Label instructions = new Label("Review and edit the auto-parsed metadata below:");
		instructions.setFont(Font.font(null, FontWeight.NORMAL, 12));
		grid.add(instructions, 0, 0, 2, 1);

		// Test Article
		Label testArticleLabel = new Label("Test Article:");
		testArticleField = new TextField();
		testArticleField.setPromptText("e.g., Perfluoro-3-methoxypropanoic acid");
		testArticleField.setPrefWidth(300);
		if (initialDescription.getTestArticle() != null) {
			testArticleField.setText(initialDescription.getTestArticle());
		}
		grid.add(testArticleLabel, 0, 1);
		grid.add(testArticleField, 1, 1);

		// Species
		Label speciesLabel = new Label("Species:");
		speciesField = new ComboBox<>();
		speciesField.setItems(FXCollections.observableArrayList(ExperimentDescription.SPECIES_VOCABULARY));
		speciesField.setEditable(true);
		speciesField.setPromptText("Select or enter species");
		speciesField.setPrefWidth(300);
		if (initialDescription.getSpecies() != null) {
			speciesField.setValue(initialDescription.getSpecies());
		} else {
			speciesField.setValue("Rat");  // Default to Rat
		}
		grid.add(speciesLabel, 0, 2);
		grid.add(speciesField, 1, 2);

		// Strain
		Label strainLabel = new Label("Strain:");
		strainField = new ComboBox<>();
		strainField.setEditable(true);
		strainField.setPromptText("Select or enter strain");
		strainField.setPrefWidth(300);

		// Initialize strain dropdown based on selected species
		String selectedSpecies = speciesField.getValue();
		updateStrainOptions(selectedSpecies);

		// Set initial strain value
		if (initialDescription.getStrain() != null) {
			strainField.setValue(initialDescription.getStrain());
		} else if ("Rat".equals(selectedSpecies)) {
			strainField.setValue("Sprague-Dawley");  // Default to Sprague-Dawley for Rat
		}

		// Add listener to update strain options when species changes
		speciesField.valueProperty().addListener((observable, oldValue, newValue) -> {
			String currentStrain = strainField.getValue();
			updateStrainOptions(newValue);

			// If current strain is not in the new list and is not a custom value, clear it
			if (currentStrain != null && !strainField.getItems().contains(currentStrain)) {
				// Keep custom values, but if it was from old species vocabulary, clear it
				if (oldValue != null && ExperimentDescription.getStrainsForSpecies(oldValue).contains(currentStrain)) {
					strainField.setValue(null);
				}
			}

			// Set default strain for new species if nothing is selected
			if (strainField.getValue() == null || strainField.getValue().isEmpty()) {
				List<String> strains = ExperimentDescription.getStrainsForSpecies(newValue);
				if (!strains.isEmpty()) {
					strainField.setValue(strains.get(0));
				}
			}
		});

		grid.add(strainLabel, 0, 3);
		grid.add(strainField, 1, 3);

		// Sex
		Label sexLabel = new Label("Sex:");
		sexField = new TextField();
		sexField.setPromptText("e.g., Male, Female, Both");
		sexField.setPrefWidth(300);
		if (initialDescription.getSex() != null) {
			sexField.setText(initialDescription.getSex());
		} else {
			sexField.setText("Male");  // Default to Male
		}
		grid.add(sexLabel, 0, 4);
		grid.add(sexField, 1, 4);

		// Organ
		Label organLabel = new Label("Organ:");
		organField = new ComboBox<>();
		organField.setItems(FXCollections.observableArrayList(ExperimentDescription.ORGAN_VOCABULARY));
		organField.setEditable(true);
		organField.setPromptText("Select or enter organ");
		organField.setPrefWidth(300);
		if (initialDescription.getOrgan() != null) {
			organField.setValue(initialDescription.getOrgan());
		} else {
			organField.setValue("Liver");  // Default to Liver
		}
		grid.add(organLabel, 0, 5);
		grid.add(organField, 1, 5);

		// Note about optional fields
		Label note = new Label("Note: All fields are optional. Leave blank if not applicable.");
		note.setFont(Font.font(null, FontWeight.NORMAL, 11));
		note.setStyle("-fx-text-fill: gray;");
		grid.add(note, 0, 6, 2, 1);
		GridPane.setMargin(note, new Insets(10, 0, 0, 0));

		getDialogPane().setContent(grid);

		// Add OK and Cancel buttons
		ButtonType okButtonType = new ButtonType("OK", ButtonData.OK_DONE);
		ButtonType cancelButtonType = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);

		// Convert the result when OK is clicked
		setResultConverter(dialogButton -> {
			if (dialogButton == okButtonType) {
				ExperimentDescription result = new ExperimentDescription();
				result.setTestArticle(getTextOrNull(testArticleField.getText()));
				result.setSpecies(getTextOrNull(speciesField.getValue()));
				result.setStrain(getTextOrNull(strainField.getValue()));
				result.setSex(getTextOrNull(sexField.getText()));
				result.setOrgan(getTextOrNull(organField.getValue()));
				return result;
			}
			return null;
		});

		// Request focus on first field
		testArticleField.requestFocus();
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
	private void updateStrainOptions(String species) {
		List<String> strains = ExperimentDescription.getStrainsForSpecies(species);
		strainField.setItems(FXCollections.observableArrayList(strains));
	}

	/**
	 * Static method to show dialog and get result
	 */
	public static ExperimentDescription showDialog(Window owner, ExperimentDescription parsedDescription, String filename) {
		ExperimentDescriptionDialog dialog = new ExperimentDescriptionDialog(owner, parsedDescription, filename);
		return dialog.showAndWait().orElse(null);
	}
}
