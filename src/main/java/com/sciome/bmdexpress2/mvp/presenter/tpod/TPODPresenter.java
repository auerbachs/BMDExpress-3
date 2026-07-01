package com.sciome.bmdexpress2.mvp.presenter.tpod;

import java.util.List;

import com.google.common.eventbus.Subscribe;
import com.sciome.bmdexpress2.mvp.model.category.CategoryAnalysisResults;
import com.sciome.bmdexpress2.mvp.model.tpod.TPODAnalysisResults;
import com.sciome.bmdexpress2.mvp.model.tpod.TPODInputParameters;
import com.sciome.bmdexpress2.mvp.presenter.presenterbases.ServicePresenterBase;
import com.sciome.bmdexpress2.mvp.viewinterface.tpod.ITPODView;
import com.sciome.bmdexpress2.serviceInterface.ITPODService;
import com.sciome.bmdexpress2.shared.TPODAnalysisEnum;
import com.sciome.bmdexpress2.shared.eventbus.BMDExpressEventBus;
import com.sciome.bmdexpress2.shared.eventbus.project.BMDProjectLoadedEvent;
import com.sciome.bmdexpress2.shared.eventbus.project.CloseProjectRequestEvent;
import com.sciome.bmdexpress2.shared.eventbus.project.ShowErrorEvent;
import com.sciome.bmdexpress2.util.bmds.IBMDSToolProgress;

import javafx.application.Platform;
import javafx.concurrent.Task;

public class TPODPresenter extends ServicePresenterBase<ITPODView, ITPODService> implements IBMDSToolProgress
{
	private List<CategoryAnalysisResults> catResults;

	private TPODAnalysisEnum tpodAnalysisEnum;

	/*
	 * Constructors
	 */

	public TPODPresenter(ITPODView view, ITPODService service, BMDExpressEventBus eventBus)
	{
		super(view, service, eventBus);
		init();
	}

	/*
	 * Private Methods
	 */
	private void init()
	{
	}

	public void initData(List<CategoryAnalysisResults> c, TPODAnalysisEnum tpodAnalysisEnum)
	{
		this.catResults = c;
		this.tpodAnalysisEnum = tpodAnalysisEnum;

	}

	@SuppressWarnings("restriction")
	public void startAnalyses(TPODInputParameters params)
	{

		// send this to the bmdanalysis tool so some progress can be updated.
		IBMDSToolProgress me = this;

		Task<Integer> task = new Task<Integer>() {
			@Override
			protected Integer call() throws Exception
			{
				try
				{
					for (CategoryAnalysisResults catResult : catResults)
					{

						// for gene level analysis, just use the genes as categories
						// basically recreate the defined pathway
						if (tpodAnalysisEnum == TPODAnalysisEnum.GENE_LEVEL)
						{
							// params.setCategoryFileParameters(GeneLevelUtils
							// .getCategoryFileParameters(bmdResult.getDoseResponseExperiment()));
							// params.setProbeFileParameters(GeneLevelUtils
							// .getProbeFileParameters(bmdResult.getDoseResponseExperiment()));
						}
						Platform.runLater(() ->
						{
							getView().startedTPODDetermination();
						});
						try
						{
							// CategoryAnalysisResults categoryAnalysisResults = getService()
							// .categoryAnalysis(params, bmdResult, tpodAnalysisEnum, me);

							TPODAnalysisResults tpodResults = getService().tpodAnalysis(catResult, params,
									me);

							Platform.runLater(() ->
							{

								getView().finishedTPODDetermination();
								// if (categoryAnalysisResults != null)
								// {

								// getEventBus().post(
								// new CategoryAnalysisDataLoadedEvent(categoryAnalysisResults));

								// }

							});

						}
						catch (Exception exception)
						{
							Platform.runLater(() ->
							{
								TPODPresenter.this.getEventBus().post(
										new ShowErrorEvent("tPOD Analysis Failure: " + exception.toString()));
								getView().enableButtons();
							});
							exception.printStackTrace();
						}
					}

					Platform.runLater(() ->
					{
						getView().closeWindow();
					});
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				return 0;
			}
		};

		new Thread(task).start();

	}

	@Override
	public void updateProgress(String label, double value)
	{
		Platform.runLater(() ->
		{
			getView().updateProgressBar(label, value);

		});

	}

	@Subscribe
	public void onProjectLoadedEvent(BMDProjectLoadedEvent event)
	{

		getView().closeWindow();
	}

	@Subscribe
	public void onProjectClosedEvent(CloseProjectRequestEvent event)
	{

		getView().closeWindow();
	}

	@Override
	public void clearProgress()
	{
		// TODO Auto-generated method stub

	}

}
