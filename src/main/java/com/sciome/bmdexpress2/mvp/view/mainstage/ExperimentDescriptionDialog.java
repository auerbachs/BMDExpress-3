package com.sciome.bmdexpress2.mvp.view.mainstage;

import java.util.List;

import com.sciome.bmdexpress2.mvp.model.info.ExperimentDescriptionBase;
import com.sciome.bmdexpress2.mvp.model.info.InVivoExperimentDescription;
import com.sciome.bmdexpress2.mvp.model.info.TestArticleIdentifier;

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
 * TODO: Expand to support full hierarchy (In Vivo/In Vitro, routes, etc.)
 */
public class ExperimentDescriptionDialog extends Dialog<ExperimentDescriptionBase> {

	private TextField testArticleField;
	private ComboBox<String> speciesField;
	private ComboBox<String> strainField;
	private TextField sexField;
	private ComboBox<String> organField;

	private InVivoExperimentDescription initialDescription;

	/**
	 * Create dialog with auto-parsed description
	 */
	public ExperimentDescriptionDialog(Window owner, ExperimentDescriptionBase parsedDescription, String filename) {
		// Convert to InVivoExperimentDescription if needed
		if (parsedDescription instanceof InVivoExperimentDescription) {
			this.initialDescription = (InVivoExperimentDescription) parsedDescription;
		} else if (parsedDescription == null) {
			this.initialDescription = new InVivoExperimentDescription();
		} else {
			// If it's InVitro, create a new InVivo for now
			// TODO: Support InVitro in dialog
			this.initialDescription = new InVivoExperimentDescription();
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
		if (initialDescription.getTestArticle() != null && initialDescription.getTestArticle().getName() != null) {
			testArticleField.setText(initialDescription.getTestArticle().getName());
		}
		grid.add(testArticleLabel, 0, 1);
		grid.add(testArticleField, 1, 1);

		// Species
		Label speciesLabel = new Label("Species: *");
		speciesField = new ComboBox<>();
		speciesField.setItems(FXCollections.observableArrayList(InVivoExperimentDescription.SPECIES_VOCABULARY));
		speciesField.setEditable(true);
		speciesField.setPromptText("Select or enter species");
		speciesField.setPrefWidth(300);
		if (initialDescription.getSpecies() != null) {
			speciesField.setValue(initialDescription.getSpecies());
		}
		// No default value - user must select
		grid.add(speciesLabel, 0, 2);
		grid.add(speciesField, 1, 2);

		// Strain
		Label strainLabel = new Label("Strain: *");
		strainField = new ComboBox<>();
		strainField.setEditable(true);
		strainField.setPromptText("Select or enter strain");
		strainField.setPrefWidth(300);

		// Initialize strain dropdown based on selected species
		String selectedSpecies = speciesField.getValue();
		updateStrainOptions(selectedSpecies);

		// Set initial strain value only if from file
		if (initialDescription.getStrain() != null) {
			strainField.setValue(initialDescription.getStrain());
		}
		// No default value - user must select

		// Add listener to update strain options when species changes
		speciesField.valueProperty().addListener((observable, oldValue, newValue) -> {
			String currentStrain = strainField.getValue();
			updateStrainOptions(newValue);

			// If current strain is not in the new list and is not a custom value, clear it
			if (currentStrain != null && !strainField.getItems().contains(currentStrain)) {
				// Keep custom values, but if it was from old species vocabulary, clear it
				if (oldValue != null && InVivoExperimentDescription.getStrainsForSpecies(oldValue).contains(currentStrain)) {
					strainField.setValue(null);
				}
			}
			// No auto-filling of default strain - user must select
		});

		grid.add(strainLabel, 0, 3);
		grid.add(strainField, 1, 3);

		// Sex
		Label sexLabel = new Label("Sex: *");
		sexField = new TextField();
		sexField.setPromptText("e.g., Male, Female, Both");
		sexField.setPrefWidth(300);
		if (initialDescription.getSex() != null) {
			sexField.setText(initialDescription.getSex());
		}
		// No default value - user must enter
		grid.add(sexLabel, 0, 4);
		grid.add(sexField, 1, 4);

		// Organ
		Label organLabel = new Label("Organ: *");
		organField = new ComboBox<>();
		organField.setItems(FXCollections.observableArrayList(InVivoExperimentDescription.ORGAN_VOCABULARY));
		organField.setEditable(true);
		organField.setPromptText("Select or enter organ");
		organField.setPrefWidth(300);
		if (initialDescription.getOrgan() != null) {
			organField.setValue(initialDescription.getOrgan());
		}
		// No default value - user must select
		grid.add(organLabel, 0, 5);
		grid.add(organField, 1, 5);

		// Note about required fields
		Label note = new Label("Note: Fields marked with * are required.");
		note.setFont(Font.font(null, FontWeight.NORMAL, 11));
		note.setStyle("-fx-text-fill: gray;");
		grid.add(note, 0, 6, 2, 1);
		GridPane.setMargin(note, new Insets(10, 0, 0, 0));

		getDialogPane().setContent(grid);

		// Add OK and Cancel buttons
		ButtonType okButtonType = new ButtonType("OK", ButtonData.OK_DONE);
		ButtonType cancelButtonType = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);

		// Disable OK button initially if required fields are empty
		javafx.scene.Node okButton = getDialogPane().lookupButton(okButtonType);
		okButton.setDisable(!areRequiredFieldsFilled());

		// Add listeners to enable/disable OK button based on field validation
		speciesField.valueProperty().addListener((obs, old, newVal) -> {
			okButton.setDisable(!areRequiredFieldsFilled());
		});
		strainField.valueProperty().addListener((obs, old, newVal) -> {
			okButton.setDisable(!areRequiredFieldsFilled());
		});
		sexField.textProperty().addListener((obs, old, newVal) -> {
			okButton.setDisable(!areRequiredFieldsFilled());
		});
		organField.valueProperty().addListener((obs, old, newVal) -> {
			okButton.setDisable(!areRequiredFieldsFilled());
		});

		// Convert the result when OK is clicked
		setResultConverter(dialogButton -> {
			if (dialogButton == okButtonType) {
				InVivoExperimentDescription result = new InVivoExperimentDescription();

				// Set test article
				String testArticleName = getTextOrNull(testArticleField.getText());
				if (testArticleName != null) {
					TestArticleIdentifier testArticle = new TestArticleIdentifier();
					testArticle.setName(testArticleName);
					result.setTestArticle(testArticle);
				}

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
		List<String> strains = InVivoExperimentDescription.getStrainsForSpecies(species);
		strainField.setItems(FXCollections.observableArrayList(strains));
	}

	/**
	 * Check if all required fields are filled
	 */
	private boolean areRequiredFieldsFilled() {
		return getTextOrNull(speciesField.getValue()) != null &&
			   getTextOrNull(strainField.getValue()) != null &&
			   getTextOrNull(sexField.getText()) != null &&
			   getTextOrNull(organField.getValue()) != null;
	}

	/**
	 * Static method to show dialog and get result
	 */
	public static ExperimentDescriptionBase showDialog(Window owner, ExperimentDescriptionBase parsedDescription, String filename) {
		ExperimentDescriptionDialog dialog = new ExperimentDescriptionDialog(owner, parsedDescription, filename);
		return dialog.showAndWait().orElse(null);
	}
}
