package com.sciome.bmdexpress2.mvp.view.prefilter;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.sciome.bmdexpress2.mvp.model.IStatModelProcessable;
import com.sciome.bmdexpress2.mvp.model.prefilter.CurveFitPrefilterInput;
import com.sciome.bmdexpress2.mvp.presenter.prefilter.CurveFitPrefilterPresenter;
import com.sciome.bmdexpress2.mvp.view.BMDExpressViewBase;
import com.sciome.bmdexpress2.mvp.viewinterface.prefilter.IPrefilterView;
import com.sciome.bmdexpress2.service.PrefilterService;
import com.sciome.bmdexpress2.shared.BMDExpressProperties;
import com.sciome.bmdexpress2.shared.eventbus.BMDExpressEventBus;
import com.sciome.bmdexpress2.util.bmds.shared.BMRFactor;
import com.sciome.bmdexpress2.util.bmds.shared.ExponentialModel;
import com.sciome.bmdexpress2.util.bmds.shared.HillModel;
import com.sciome.bmdexpress2.util.bmds.shared.PolyModel;
import com.sciome.bmdexpress2.util.bmds.shared.PowerModel;
import com.sciome.bmdexpress2.util.bmds.shared.StatModel;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

// williamstrend.fxml
public class CurveFitPrefilterView extends BMDExpressViewBase implements IPrefilterView, Initializable
{

	private final String NON_CONSTANT_VARIANCE = "Non-Constant";
	private final String CONSTANT_VARIANCE = "Constant";

	@FXML
	private CheckBox hillCB;
	@FXML
	private CheckBox powerCB;
	@FXML
	private CheckBox exp3CB;
	@FXML
	private CheckBox exp5CB;
	@FXML
	private CheckBox linearCB;

	@FXML
	private CheckBox poly2CB;

	@FXML
	private ComboBox<BMRFactor> bmrFCombo;

	@FXML
	private ComboBox<BMRFactor> poly2BmrFCombo;
	@FXML
	private ComboBox<String> varianceCombo;

	@FXML
	private ComboBox expressionDataComboBox;

	@FXML
	private CheckBox useFoldChangeCheckBox;
	@FXML
	private TextField foldChangeValueTextField;
	@FXML
	private ProgressBar prefilterProgress;
	@FXML
	private Label progressMessage;
	@FXML
	private Label datasetsCompletedLabel;
	@FXML
	private Button startButton;
	@FXML
	private Button saveSettingsButton;
	@FXML
	private Button stopButton;
	@FXML
	private TextField pValueLoelTextField;
	@FXML
	private TextField foldChangeLoelTextField;
	@FXML
	private TextField numberOfThreadsTextField;
	@FXML
	private RadioButton dunnettsRadioButton;
	@FXML
	private RadioButton tRadioButton;

	private List<IStatModelProcessable> processableData = null;
	private List<IStatModelProcessable> processableDatas = null;

	private CurveFitPrefilterInput input;

	CurveFitPrefilterPresenter presenter;

	public CurveFitPrefilterView()
	{
		this(BMDExpressEventBus.getInstance());
	}

	/*
	 * Event bus is passed as an argument so the unit tests can pass their own custom eventbus
	 */
	public CurveFitPrefilterView(BMDExpressEventBus eventBus)
	{
		super();
		PrefilterService service = new PrefilterService();
		presenter = new CurveFitPrefilterPresenter(this, service, eventBus);
		input = BMDExpressProperties.getInstance().getCurveFitPrefilterInput();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initData(List<IStatModelProcessable> processableData,
			List<IStatModelProcessable> processableDatas)
	{

		this.processableData = processableData;
		this.processableDatas = processableDatas;

		bmrFCombo.getItems().addAll(initBMRFactorsStandardDeviation());
		poly2BmrFCombo.getItems().addAll(initBMRFactorsStandardDeviation());

		List<String> varianceValues = new ArrayList<>();
		varianceValues.add(CONSTANT_VARIANCE);
		varianceValues.add(NON_CONSTANT_VARIANCE);

		this.varianceCombo.getItems().setAll(varianceValues);

		this.varianceCombo.setValue(CONSTANT_VARIANCE);

		bmrFCombo.getSelectionModel().select(bmrFCombo.getItems().get(bmrFCombo.getItems().size() - 1));

		poly2BmrFCombo.getSelectionModel()
				.select(poly2BmrFCombo.getItems().get(poly2BmrFCombo.getItems().size() - 1));

		for (IStatModelProcessable experiment : processableDatas)
		{
			expressionDataComboBox.getItems().add(experiment);

		}

		expressionDataComboBox.getSelectionModel().select(processableData.get(0));

		if (processableData.size() > 1)
		{
			expressionDataComboBox.setDisable(true);
		}

		ToggleGroup radioGroup = new ToggleGroup();
		tRadioButton.setToggleGroup(radioGroup);
		dunnettsRadioButton.setToggleGroup(radioGroup);

		if (input.istTest())
			tRadioButton.setSelected(true);
		else
			dunnettsRadioButton.setSelected(true);

		if (input.getBMRFactor() != null)
		{
			this.bmrFCombo.setValue(input.getBMRFactor());
		}

		if (input.getPoly2BMRFactor() != null)
		{
			this.poly2BmrFCombo.setValue(input.getPoly2BMRFactor());
		}

		if (input.isConstantVariance() != null)
		{
			if (!input.isConstantVariance())
			{
				this.varianceCombo.setValue(NON_CONSTANT_VARIANCE);
			}
			else
			{
				this.varianceCombo.setValue(CONSTANT_VARIANCE);
			}
		}

		this.hillCB.setSelected(input.getIsHill());
		this.exp3CB.setSelected(input.getIsExp3());
		this.exp5CB.setSelected(input.getIsExp5());
		this.powerCB.setSelected(input.getIsPower());
		this.linearCB.setSelected(input.getIsLinear());
		this.poly2CB.setSelected(input.getIsPoly2());

		useFoldChangeCheckBox.setSelected(input.isUseFoldChange());
		tRadioButton.setSelected(input.istTest());
		foldChangeValueTextField.setText("" + input.getFoldChangeValue());
		pValueLoelTextField.setText("" + input.getLoelPValue());
		foldChangeLoelTextField.setText("" + input.getLoelFoldChangeValue());
		numberOfThreadsTextField.setText("" + input.getNumThreads());
	}

	public void handle_startButtonPressed(ActionEvent event)
	{
		if (!presenter.hasStartedTask())
		{
			List<StatModel> modelsToRun = new ArrayList<>();
			if (!hillCB.isDisabled() && hillCB.isSelected())
			{
				HillModel hillModel = new HillModel();
				hillModel.setVersion("Hill EPA BMDS MLE ToxicR");

				modelsToRun.add(hillModel);
			}
			if (!powerCB.isDisabled() && powerCB.isSelected())
			{
				PowerModel powerModel = new PowerModel();
				powerModel.setVersion("Power EPA BMDS MLE ToxicR");

				modelsToRun.add(powerModel);
			}
			if (!linearCB.isDisabled() && linearCB.isSelected())
			{
				PolyModel linearModel = new PolyModel();
				linearModel.setVersion("Linear EPA BMDS MLE ToxicR");
				linearModel.setDegree(1);
				modelsToRun.add(linearModel);
			}

			if (!poly2CB.isDisabled() && poly2CB.isSelected())
			{
				PolyModel poly2Model = new PolyModel();
				poly2Model.setVersion("Poly 2 EPA BMDS MLE ToxicR");
				poly2Model.setDegree(2);
				modelsToRun.add(poly2Model);
			}

			if (!exp3CB.isDisabled() && exp3CB.isSelected())
			{
				ExponentialModel exponentialModel = new ExponentialModel();
				exponentialModel.setVersion("Exponential 3 EPA BMDS MLE ToxicR");

				modelsToRun.add(exponentialModel);
				exponentialModel.setOption(3);
			}

			if (!exp5CB.isDisabled() && exp5CB.isSelected())
			{
				ExponentialModel exponentialModel = new ExponentialModel();
				exponentialModel.setVersion("Exponential 5 EPA BMDS MLE ToxicR");

				exponentialModel.setOption(5);
				modelsToRun.add(exponentialModel);
			}

			if (processableData.size() > 1)
			{
				presenter.performCurveFitPrefilter(processableData, useFoldChangeCheckBox.isSelected(),
						foldChangeValueTextField.getText(), pValueLoelTextField.getText(),
						foldChangeLoelTextField.getText(), numberOfThreadsTextField.getText(),
						tRadioButton.isSelected(), modelsToRun,
						Double.valueOf(bmrFCombo.getValue().getValue()),
						Double.valueOf(poly2BmrFCombo.getValue().getValue()),
						this.varianceCombo.getValue().equalsIgnoreCase(CONSTANT_VARIANCE) ? 1 : 0);
			}
			else
			{
				presenter.performCurveFitPrefilter(
						(IStatModelProcessable) expressionDataComboBox.getSelectionModel().getSelectedItem(),
						useFoldChangeCheckBox.isSelected(), foldChangeValueTextField.getText(),
						pValueLoelTextField.getText(), foldChangeLoelTextField.getText(),
						numberOfThreadsTextField.getText(), tRadioButton.isSelected(), modelsToRun,
						Double.valueOf(bmrFCombo.getValue().getValue()),
						Double.valueOf(poly2BmrFCombo.getValue().getValue()),
						this.varianceCombo.getValue().equalsIgnoreCase(CONSTANT_VARIANCE) ? 1 : 0);
			}
			startButton.setDisable(true);
		}
	}

	public void handle_cancelButtonPressed(ActionEvent event)
	{
		if (!presenter.hasStartedTask())
		{
			this.closeWindow();
		}
		else
		{
			presenter.cancel();
			startButton.setDisable(false);
		}
	}

	public void handle_saveSettingsButtonPressed(ActionEvent event)
	{

		input.setUseFoldChange(this.useFoldChangeCheckBox.isSelected());
		input.settTest(this.tRadioButton.isSelected());
		input.setFoldChangeValue(Double.parseDouble(this.foldChangeValueTextField.getText()));
		input.setLoelFoldChangeValue(Double.parseDouble(this.foldChangeLoelTextField.getText()));
		input.setLoelPValue(Double.parseDouble(this.pValueLoelTextField.getText()));
		input.setNumThreads(Integer.parseInt(this.numberOfThreadsTextField.getText()));

		input.setIsHill(this.hillCB.isSelected());
		input.setIsPower(this.powerCB.isSelected());
		input.setIsExp3(this.exp3CB.isSelected());
		input.setIsExp5(this.exp5CB.isSelected());
		input.setIsLinear(this.linearCB.isSelected());
		input.setIsPoly2(this.poly2CB.isSelected());

		input.setBMRFactor(this.bmrFCombo.getValue());
		input.setConstantVariance(this.varianceCombo.getValue().equals(CONSTANT_VARIANCE));

		BMDExpressProperties.getInstance().saveCurveFitPrefilterInput(input);

		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Saved Settings");
		alert.setHeaderText(null);
		alert.setContentText("Your settings have been saved");

		alert.showAndWait();
	}

	public void handle_UseFoldChangeFilter()
	{
		if (this.useFoldChangeCheckBox.isSelected())
		{
			this.foldChangeValueTextField.setDisable(false);

		}
		else
		{
			this.foldChangeValueTextField.setDisable(true);

		}

	}

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{

	}

	@Override
	public void closeWindow()
	{
		Stage stage = (Stage) useFoldChangeCheckBox.getScene().getWindow();
		this.close();
		stage.close();
	}

	@Override
	public void updateProgress(double progress)
	{
		prefilterProgress.setProgress(progress);
	}

	@Override
	public void updateMessage(String message)
	{
		progressMessage.setText(message);
	}

	@Override
	public void updateDatasetLabel(String message)
	{
		datasetsCompletedLabel.setText(message);
	}

	@Override
	public void close()
	{
		if (presenter != null)
			presenter.destroy();

	}

	private List<BMRFactor> initBMRFactorsStandardDeviation()
	{
		List<BMRFactor> factors = new ArrayList<>();
		factors.add(new BMRFactor("0.522 (1%)", "0.522"));
		factors.add(new BMRFactor("1 SD", "1.0"));
		factors.add(new BMRFactor("1.021 (5%)", "1.021"));
		factors.add(new BMRFactor("1.349 (10%)", "1.349"));
		factors.add(new BMRFactor("1.581 (15%)", "1.581"));
		factors.add(new BMRFactor("1.932484 (25%)", "1.932484"));
		factors.add(new BMRFactor("2 SD", "2.0"));
		factors.add(new BMRFactor("2.5 SD", "2.5"));
		factors.add(new BMRFactor("2.600898 (50%)", "2.600898"));
		factors.add(new BMRFactor("2.855148 (60%)", "2.855148"));
		factors.add(new BMRFactor("3.282 (75%)", "3.282"));
		factors.add(new BMRFactor("3 SD", "3.0"));
		factors.add(new BMRFactor("3.5 SD", "3.5"));
		factors.add(new BMRFactor("4 SD", "4.0"));
		factors.add(new BMRFactor("4.327 (95%)", "4.327"));
		factors.add(new BMRFactor("5 SD", "5.0"));

		return factors;
	}

}
