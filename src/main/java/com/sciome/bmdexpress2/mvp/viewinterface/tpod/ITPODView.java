package com.sciome.bmdexpress2.mvp.viewinterface.tpod;

import java.util.List;

import com.sciome.bmdexpress2.mvp.model.category.CategoryAnalysisResults;
import com.sciome.bmdexpress2.shared.TPODAnalysisEnum;

import javafx.event.ActionEvent;

public interface ITPODView
{

	void handle_start(ActionEvent event);

	void handle_close(ActionEvent event);

	public void handle_saveSettingsButtonPressed(ActionEvent event);

	public void finishedTPODDetermination();

	public void closeWindow();

	public void startedTPODDetermination();

	public void updateProgressBar(String label, double value);

	public void enableButtons();

	void initData(List<CategoryAnalysisResults> catResults, TPODAnalysisEnum catAnalysisEnum);

}
