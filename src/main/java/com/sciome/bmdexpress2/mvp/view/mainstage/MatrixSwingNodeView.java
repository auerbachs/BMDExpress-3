package com.sciome.bmdexpress2.mvp.view.mainstage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import com.sciome.bmdexpress2.mvp.presenter.mainstage.MatrixSwingNodePresenter;
import com.sciome.bmdexpress2.mvp.view.BMDExpressViewBase;
import com.sciome.bmdexpress2.mvp.viewinterface.mainstage.IMatrixSwingNodeView;
import com.sciome.bmdexpress2.shared.BMDExpressProperties;
import com.sciome.bmdexpress2.shared.eventbus.BMDExpressEventBus;
import com.sciome.bmdexpress2.util.FileIO;
import com.sciome.bmdexpress2.util.MatrixData;
import com.sciome.bmdexpress2.util.categoryanalysis.defined.MatrixDataPreviewer;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MatrixSwingNodeView extends BMDExpressViewBase implements IMatrixSwingNodeView, Initializable
{

	@FXML
	VBox swingNode;

	@FXML
	Button doneButton;
	@FXML
	Label headerLabel;
	@FXML
	VBox vBox;

	MatrixSwingNodePresenter presenter;

	MatrixData matrix;

	public MatrixSwingNodeView()
	{
		this(BMDExpressEventBus.getInstance());
	}

	/*
	 * Event bus is passed as an argument so the unit tests can pass their own custom eventbus
	 */
	public MatrixSwingNodeView(BMDExpressEventBus eventBus)
	{
		super();
		presenter = new MatrixSwingNodePresenter(this, eventBus);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{

	}

	@Override
	public void closeWindow()
	{
		handle_Done();
	}

	public void handle_Done()
	{
		Stage stage = (Stage) doneButton.getScene().getWindow();
		stage.close();
	}

	public void handle_Export()
	{

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Export Gene Mapping");
		File initialDirectory = new File(BMDExpressProperties.getInstance().getExportPath());
		if (initialDirectory.exists())
			fileChooser.setInitialDirectory(initialDirectory);
		fileChooser.setInitialFileName("genemapping.txt");
		File selectedFile = fileChooser.showSaveDialog(doneButton.getScene().getWindow());

		if (selectedFile != null)
		{
			BMDExpressProperties.getInstance().setExportPath(selectedFile.getParent());
			FileIO.writeFileMatrix(matrix, selectedFile);
		}

	}

	public void initData(String headerText, MatrixData matrixData)
	{
		initData(headerText, matrixData, false);
	}

	public void initData(String headerText, MatrixData matrixData, boolean showLimited)
	{
		this.matrix = matrixData;
		headerLabel.setText(headerText + ", " + matrixData.rows() + " rows.");
		swingNode.getChildren().clear();
		MatrixDataPreviewer pane = new MatrixDataPreviewer(matrixData, showLimited);
		swingNode.getChildren().add(pane);

	}

	@Override
	public void close()
	{
		if (presenter != null)
			presenter.destroy();

	}
}
