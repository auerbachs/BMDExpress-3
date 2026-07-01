package com.sciome.bmdexpress2.mvp.view.tpod;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.sciome.bmdexpress2.mvp.model.category.CategoryAnalysisResults;
import com.sciome.bmdexpress2.mvp.presenter.tpod.TPODPresenter;
import com.sciome.bmdexpress2.mvp.view.BMDExpressViewBase;
import com.sciome.bmdexpress2.mvp.viewinterface.tpod.ITPODView;
import com.sciome.bmdexpress2.service.CategoryAnalysisService;
import com.sciome.bmdexpress2.serviceInterface.ICategoryAnalysisService;
import com.sciome.bmdexpress2.shared.TPODAnalysisEnum;
import com.sciome.bmdexpress2.shared.eventbus.BMDExpressEventBus;
import com.sciome.bmdexpress2.util.categoryanalysis.CategoryAnalysisParameters;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TPODView extends BMDExpressViewBase implements ITPODView, Initializable
{

	TPODPresenter presenter;

	private TPODAnalysisEnum tpodAnalysisEnum;

	@FXML
	private VBox mainVBox;

	// labels
	@FXML
	private Label bMDAnalysisName;

	@FXML
	private ProgressBar progressBar;
	@FXML
	private Label progressLabel;
	@FXML
	private HBox progressHBox;

	@FXML
	private Button startButton;
	@FXML
	private Button closeButton;
	@FXML
	private Button saveSettingsButton;

	TextField stringAutoCompleteSelector;

	private Map<String, CheckBox> labelToNode = new HashMap<>();

	public TPODView()
	{
		this(BMDExpressEventBus.getInstance());
	}

	/*
	 * Event bus is passed as an argument so the unit tests can pass their own custom eventbus
	 */
	public TPODView(BMDExpressEventBus eventBus)
	{
		super();
		ICategoryAnalysisService service = new CategoryAnalysisService();
		presenter = new TPODPresenter(this, service, eventBus);
		// goInput = BMDExpressProperties.getInstance().getGOCategoryInput();
		// definedInput = BMDExpressProperties.getInstance().getDefinedCategoryInput();
		// geneInput = BMDExpressProperties.getInstance().getGeneCategoryInput();
		// pathwayInput = BMDExpressProperties.getInstance().getPathwayCategoryInput();

	}

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{

	}

	private void initializeInputParameterVisibility()
	{
		// TODO Auto-generated method stub

	}

	/*
	 * user clicked close button
	 */
	@Override
	public void handle_close(ActionEvent event)
	{
		closeWindow();
	}

	/*
	 * use clicked start button
	 */
	@Override
	public void handle_start(ActionEvent event)
	{
		CategoryAnalysisParameters params = null;

		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Invalid Input");
		alert.setHeaderText(null);
		try
		{
			params = this.gatherParameters();
		}
		catch (NumberFormatException e)
		{
			// Otherwise give user a message
			alert.setContentText("Invalid input fields");
			alert.showAndWait();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
			if (e.getMessage() != null)
				alert.setContentText(e.getMessage());
			else
				alert.setContentText("Invalid input fields");
			alert.showAndWait();
		}

		if (params != null)
		{
			startButton.setDisable(true);
			closeButton.setDisable(true);
			presenter.startAnalyses(params);
		}
	}

	@Override
	public void handle_saveSettingsButtonPressed(ActionEvent event)
	{

		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Saved Settings");
		alert.setHeaderText(null);
		alert.setContentText("Your settings have been saved");

		alert.showAndWait();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initData(List<CategoryAnalysisResults> catResults, TPODAnalysisEnum tpodAnalysisEnum)
	{
		this.tpodAnalysisEnum = tpodAnalysisEnum;

		presenter.initData(catResults, tpodAnalysisEnum);

	}

	/*
	 * set up the parameters object to send to the presenter.
	 */
	private CategoryAnalysisParameters gatherParameters()
	{
		CategoryAnalysisParameters params = new CategoryAnalysisParameters();

		return params;
	}

	/*
	 * hide certain input parameters
	 * 
	 * 
	 */
	private void hideParameter(String parameterLabel)
	{
		CheckBox node2Hide = labelToNode.get(parameterLabel);
		if (node2Hide == null)
			return;
		node2Hide.setSelected(false);
		node2Hide.getParent().setVisible(false);
		node2Hide.getParent().setManaged(false);
	}

	/*
	 * show certain input parameters
	 * 
	 * 
	 */
	private void showParameter(String parameterLabel)
	{
		Node node2Hide = labelToNode.get(parameterLabel);
		if (node2Hide == null)
			return;
		node2Hide.getParent().setVisible(true);
		node2Hide.getParent().setManaged(true);
	}

	@Override
	public void finishedTPODDetermination()
	{
		progressLabel.setText("Finished Categorization");
		progressBar.setProgress(0.0);

	}

	@Override
	public void closeWindow()
	{
		Stage stage = (Stage) this.progressBar.getScene().getWindow();
		this.close();
		stage.close();

	}

	@Override
	public void startedTPODDetermination()
	{
		progressHBox.setVisible(true);
		progressLabel.setText("Beginning tPOD/CMC Determination");
		progressBar.setProgress(0.0);
	}

	@Override
	public void updateProgressBar(String label, double value)
	{
		progressLabel.setText(label);
		progressBar.setProgress(value);

	}

	@Override
	public void enableButtons()
	{
		startButton.setDisable(false);
		closeButton.setDisable(false);

	}

	@Override
	public void close()
	{
		if (presenter != null)
			presenter.close();
	}

}
