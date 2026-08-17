package com.sciome.bmdexpress2.mvp.view.tpod;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.sciome.bmdexpress2.mvp.model.category.CategoryAnalysisResults;
import com.sciome.bmdexpress2.mvp.model.tpod.BMDEndpointType;
import com.sciome.bmdexpress2.mvp.model.tpod.LCRDParameters;
import com.sciome.bmdexpress2.mvp.model.tpod.NthPercentParameters;
import com.sciome.bmdexpress2.mvp.model.tpod.NthRankParameters;
import com.sciome.bmdexpress2.mvp.model.tpod.TPODAnalysisFilter;
import com.sciome.bmdexpress2.mvp.model.tpod.TPODInputFilter;
import com.sciome.bmdexpress2.mvp.model.tpod.TPODInputParameters;
import com.sciome.bmdexpress2.mvp.model.tpod.TPODMethod;
import com.sciome.bmdexpress2.mvp.model.tpod.TPODMethodParameter;
import com.sciome.bmdexpress2.mvp.presenter.tpod.TPODPresenter;
import com.sciome.bmdexpress2.mvp.view.BMDExpressViewBase;
import com.sciome.bmdexpress2.mvp.viewinterface.tpod.ITPODView;
import com.sciome.bmdexpress2.service.TPODAnalysisService;
import com.sciome.bmdexpress2.serviceInterface.ITPODService;
import com.sciome.bmdexpress2.shared.TPODAnalysisEnum;
import com.sciome.bmdexpress2.shared.eventbus.BMDExpressEventBus;
import com.sciome.bmdexpress2.util.categoryanalysis.CategoryAnalysisParameters;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
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
	private BorderPane bmdMetricsBorderPane;

	@FXML
	private BorderPane filtersBorderPane;

	@FXML
	private BorderPane tpodMethodsBorderPane;

	@FXML
	private FlowPane bmdMetricsLayout;

	@FXML
	private FlowPane filtersLayout;

	@FXML
	private FlowPane methodsLayout;

	@FXML
	private Button startButton;
	@FXML
	private Button closeButton;
	@FXML
	private Button saveSettingsButton;

	TextField stringAutoCompleteSelector;

	private Map<String, CheckBox> labelToNode = new HashMap<>();

	private List<TPODMethodCard> tpodMethods = new ArrayList<>();

	private List<TPODFilterCard> tpodFilters = new ArrayList<>();

	private List<CheckBox> bmdMetrics = new ArrayList<>();

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
		ITPODService service = new TPODAnalysisService();
		presenter = new TPODPresenter(this, service, eventBus);

	}

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{

		initializeBMDMetrics();

		initializeTPODMethods();

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
		TPODInputParameters inputParameters = new TPODInputParameters();

		List<BMDEndpointType> bmdEndpointTypes = new ArrayList<>();
		for (CheckBox cb : bmdMetrics)
		{

			if (cb.isSelected())
				bmdEndpointTypes.add((BMDEndpointType) cb.getUserData());
		}
		inputParameters.setBmdEndpointTypes(bmdEndpointTypes);

		List<TPODMethodParameter> tpodParameters = new ArrayList<>();
		for (TPODMethodCard methodCard : tpodMethods)
		{
			if (!methodCard.isEnabled())
				continue;

			TPODMethodParameter tM = null;

			if (methodCard.method.equals(TPODMethod.LCRD))
			{
				LCRDParameters lp = new LCRDParameters();
				lp.setRunLength(methodCard.getRunLength());
				lp.setSpacingRatio(methodCard.getSpacingRatio());
				tM = lp;
			}

			else if (methodCard.method.equals(TPODMethod.NTH_PERCENTILE))
			{
				NthPercentParameters nthP = new NthPercentParameters();
				nthP.setPercent(methodCard.getPercent());
				tM = nthP;

			}
			else if (methodCard.method.equals(TPODMethod.NTH_RANK))
			{
				NthRankParameters nthR = new NthRankParameters();
				nthR.setRank(methodCard.getRank());
				tM = nthR;
			}
			else
				continue;

			tpodParameters.add(tM);

		}

		inputParameters.setMethodParameters(tpodParameters);

		List<TPODInputFilter> filters = new ArrayList<>();

		for (TPODFilterCard filterCard : tpodFilters)
		{
			if (!filterCard.isEnabled())
				continue;

			TPODInputFilter tf = null;

			if (filterCard.filter.equals(TPODAnalysisFilter.FISHERS_RIGHT_P_VALUE))
			{
				tf = new TPODInputFilter(TPODAnalysisFilter.FISHERS_RIGHT_P_VALUE,
						filterCard.getFishersRightField());
			}

			else if (filterCard.filter.equals(TPODAnalysisFilter.GENES_PASS_ALL_FILTERS))
			{
				tf = new TPODInputFilter(TPODAnalysisFilter.GENES_PASS_ALL_FILTERS,
						filterCard.getGenesThatPassed());
			}
			else if (filterCard.filter.equals(TPODAnalysisFilter.PERCENTAGE))
			{
				tf = new TPODInputFilter(TPODAnalysisFilter.PERCENTAGE, filterCard.getPercent());
			}
			else if (filterCard.filter.equals(TPODAnalysisFilter.OVERALLDIRECTION))
			{
				tf = new TPODInputFilter(TPODAnalysisFilter.OVERALLDIRECTION,
						filterCard.getOverallDirection());
			}
			else
				continue;

			filters.add(tf);

		}

		inputParameters.setInputFilters(filters);

		if (inputParameters != null)
		{
			startButton.setDisable(true);
			closeButton.setDisable(true);
			presenter.startAnalyses(inputParameters);
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

	private void initializeBMDMetrics()
	{

		// styling container
		// bmdMetricsLayout.setSpacing(8);
		bmdMetricsLayout.setPadding(new Insets(10));

		bmdMetricsLayout.setStyle("-fx-border-color: #cfcfcf;" + "-fx-border-radius: 6;"
				+ "-fx-background-radius: 6;" + "-fx-background-color: #fafafa;" + "-fx-border-width: 1;");

		// populate enum values
		for (BMDEndpointType type : BMDEndpointType.values())
		{

			CheckBox cb = new CheckBox(type.getLabel());
			cb.setUserData(type);

			cb.setStyle("-fx-font-size: 12px;");
			cb.setSelected(true);

			bmdMetrics.add(cb);
		}

		bmdMetricsLayout.getChildren().addAll(bmdMetrics);
	}

	private void initializeTPODMethods()
	{

		// methodsLayout.setSpacing(12);
		methodsLayout.setPadding(new Insets(10));

		methodsLayout.setStyle("""
				    -fx-background-color: white;
				""");

		filtersLayout.setPadding(new Insets(10));

		filtersLayout.setStyle("""
				    -fx-background-color: white;
				""");

		for (TPODMethod method : TPODMethod.values())
		{
			if (method.equals(TPODMethod.MAX_CURVATURE) || method.equals(TPODMethod.FIRST_MODE))
				continue;
			tpodMethods.add(new TPODMethodCard(method));
		}

		methodsLayout.getChildren().addAll(tpodMethods);

		for (TPODAnalysisFilter filter : TPODAnalysisFilter.values())
		{

			tpodFilters.add(new TPODFilterCard(filter));
		}

		filtersLayout.getChildren().addAll(tpodFilters);

	}

	private class TPODMethodCard extends VBox
	{

		private final TPODMethod method;
		private final CheckBox enabledCheck = new CheckBox();

		// parameter fields (only some are used per method)
		private final TextField spacingRatioField = new TextField("1.67");
		private final TextField runLengthField = new TextField("10");
		private final TextField rankField = new TextField("25");
		private final TextField percentField = new TextField("5");
		private final TextField minSizeField = new TextField("0.055");

		public TPODMethodCard(TPODMethod method)
		{
			this.method = method;

			this.setMinWidth(300);
			this.setMaxWidth(300);
			setSpacing(8);
			setPadding(new Insets(10));

			setStyle("""
					    -fx-border-color: #cfcfcf;
					    -fx-border-radius: 8;
					    -fx-background-radius: 8;
					    -fx-background-color: #fafafa;
					""");

			build();
		}

		private void build()
		{

			// ===== Top row: checkbox + label =====
			HBox header = new HBox(10);

			enabledCheck.setSelected(true);

			Label title = new Label(method.getLabel());
			title.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

			header.getChildren().addAll(enabledCheck, title);

			getChildren().add(header);

			// ===== dynamic parameter section =====
			switch (method)
			{

				case LCRD -> {
					getChildren().add(labelled("Spacing ratio:", spacingRatioField));
					getChildren().add(labelled("Run Length:", runLengthField));
				}

				case NTH_RANK -> {
					getChildren().add(labelled("Rank:", rankField));
				}

				case NTH_PERCENTILE -> {
					getChildren().add(labelled("Percent:", percentField));
				}
				case FIRST_GENESET -> {
				}
				// case FIRST_MODE -> {
				// getChildren().add(labelled("Min Size:", minSizeField));
				// }

				// case MAX_CURVATURE -> {
				// Label none = new Label("No parameters");
				// none.setStyle("-fx-text-fill: #777;");
				// getChildren().add(none);
				// }
			}
		}

		private HBox labelled(String label, TextField field)
		{

			Label l = new Label(label);
			l.setPrefWidth(140);

			field.setPrefWidth(80);

			HBox box = new HBox(10, l, field);
			box.setAlignment(Pos.CENTER_LEFT);
			return box;
		}

		// ===== getters =====

		public boolean isEnabled()
		{
			return enabledCheck.isSelected();
		}

		public TPODMethod getMethod()
		{
			return method;
		}

		public double getSpacingRatio()
		{
			return Double.parseDouble(spacingRatioField.getText());
		}

		public int getRunLength()
		{
			return Integer.parseInt(runLengthField.getText());
		}

		public int getRank()
		{
			return Integer.parseInt(rankField.getText());
		}

		public double getPercent()
		{
			return Double.parseDouble(percentField.getText());
		}

		public double getMinSize()
		{
			return Double.parseDouble(minSizeField.getText());
		}
	}

	private class TPODFilterCard extends VBox
	{

		private final TPODAnalysisFilter filter;
		private final CheckBox enabledCheck = new CheckBox();

		// parameter fields (only some are used per method)
		private final TextField fishersRightField = new TextField("1.67");
		private final TextField runLengthField = new TextField("10");
		private final TextField genesThatPassedField = new TextField("25");
		private final TextField percentField = new TextField("5");
		private final TextField minSizeField = new TextField("0.055");

		private final ToggleGroup directionGroup = new ToggleGroup();

		private final RadioButton upRadio = new RadioButton("UP");

		private final RadioButton downRadio = new RadioButton("DOWN");

		public TPODFilterCard(TPODAnalysisFilter filter)
		{
			this.filter = filter;

			this.setMinWidth(300);
			this.setMaxWidth(300);
			setSpacing(8);
			setPadding(new Insets(10));

			setStyle("""
					    -fx-border-color: #cfcfcf;
					    -fx-border-radius: 8;
					    -fx-background-radius: 8;
					    -fx-background-color: #fafafa;
					""");

			build();
		}

		private void build()
		{

			// ===== Top row: checkbox + label =====
			HBox header = new HBox(10);

			enabledCheck.setSelected(true);

			Label title = new Label(filter.getLabel());
			title.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

			header.getChildren().addAll(enabledCheck, title);

			getChildren().add(header);

			// ===== dynamic parameter section =====
			switch (filter)
			{

				case FISHERS_RIGHT_P_VALUE -> {
					getChildren().add(labelled("<=", fishersRightField));
				}

				case GENES_PASS_ALL_FILTERS -> {
					getChildren().add(labelled(">=", genesThatPassedField));
				}

				case OVERALLDIRECTION -> {
					upRadio.setToggleGroup(directionGroup);
					upRadio.setSelected(true); // Default selection
					downRadio.setToggleGroup(directionGroup);
					HBox directionBox = new HBox(10, upRadio, downRadio);
					directionBox.setPadding(new Insets(10));
					directionBox.setMinWidth(200);
					getChildren().add(labelled("Direction", directionBox));
				}

				case PERCENTAGE -> {
					getChildren().add(labelled(">=", percentField));
				}

			}
		}

		private HBox labelled(String label, Control field)
		{

			Label l = new Label(label);
			// l.setPrefWidth(140);

			// field.setPrefWidth(380);

			HBox box = new HBox(10, l, field);

			box.setAlignment(Pos.CENTER_LEFT);
			return box;
		}

		private HBox labelled(String label, Pane field)
		{

			Label l = new Label(label);
			l.setPrefWidth(140);

			field.setPrefWidth(80);

			HBox box = new HBox(10, l, field);
			box.setAlignment(Pos.CENTER_LEFT);
			return box;
		}

		// ===== getters =====

		public boolean isEnabled()
		{
			return enabledCheck.isSelected();
		}

		public TPODAnalysisFilter getFilter()
		{
			return filter;
		}

		public double getFishersRightField()
		{
			return Double.parseDouble(fishersRightField.getText());
		}

		public int getGenesThatPassed()
		{
			return Integer.parseInt(genesThatPassedField.getText());
		}

		public double getPercent()
		{
			return Double.parseDouble(percentField.getText());
		}

		public int getOverallDirection()
		{
			if (upRadio.isSelected())
				return 1;

			return -1;
		}

	}

}
