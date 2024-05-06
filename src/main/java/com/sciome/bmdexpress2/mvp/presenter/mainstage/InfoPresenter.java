package com.sciome.bmdexpress2.mvp.presenter.mainstage;

import com.google.common.eventbus.Subscribe;
import com.sciome.bmdexpress2.mvp.presenter.presenterbases.PresenterBase;
import com.sciome.bmdexpress2.mvp.viewinterface.mainstage.IInfoView;
import com.sciome.bmdexpress2.shared.eventbus.BMDExpressEventBus;
import com.sciome.bmdexpress2.shared.eventbus.analysis.BMDAnalysisDataCombinedSelectedEvent;
import com.sciome.bmdexpress2.shared.eventbus.analysis.BMDAnalysisDataSelectedEvent;
import com.sciome.bmdexpress2.shared.eventbus.analysis.CategoryAnalysisDataCombinedSelectedEvent;
import com.sciome.bmdexpress2.shared.eventbus.analysis.CategoryAnalysisDataSelectedEvent;
import com.sciome.bmdexpress2.shared.eventbus.analysis.CurveFitPrefilterDataCombinedSelectedEvent;
import com.sciome.bmdexpress2.shared.eventbus.analysis.CurveFitPrefilterDataSelectedEvent;
import com.sciome.bmdexpress2.shared.eventbus.analysis.ExpressionDataCombinedSelectedEvent;
import com.sciome.bmdexpress2.shared.eventbus.analysis.ExpressionDataSelectedEvent;
import com.sciome.bmdexpress2.shared.eventbus.analysis.NoDataSelectedEvent;
import com.sciome.bmdexpress2.shared.eventbus.analysis.OneWayANOVADataCombinedSelectedEvent;
import com.sciome.bmdexpress2.shared.eventbus.analysis.OneWayANOVADataSelectedEvent;
import com.sciome.bmdexpress2.shared.eventbus.analysis.OriogenDataCombinedSelectedEvent;
import com.sciome.bmdexpress2.shared.eventbus.analysis.OriogenDataSelectedEvent;
import com.sciome.bmdexpress2.shared.eventbus.analysis.WilliamsTrendDataCombinedSelectedEvent;
import com.sciome.bmdexpress2.shared.eventbus.analysis.WilliamsTrendDataSelectedEvent;
import com.sciome.bmdexpress2.shared.eventbus.project.BMDProjectLoadedEvent;
import com.sciome.bmdexpress2.shared.eventbus.project.CloseProjectRequestEvent;

public class InfoPresenter extends PresenterBase<IInfoView>
{

	public InfoPresenter(IInfoView view, BMDExpressEventBus eventBus)
	{
		super(view, eventBus);
		init();
	}

	/*
	 * listen for loading an experiment so we can add it to the project.
	 */
	@Subscribe
	public void onLoadExperiement(ExpressionDataSelectedEvent event)
	{
		if (event.GetPayload().getAnalysisInfo(false) != null
				&& event.GetPayload().getAnalysisInfo(false).size() > 0)
			getView().showAnalysisInfo(event.GetPayload().getAnalysisInfo(false).get(0));
	}

	/*
	 * listen for loading oneway anova results so we can add it to the project
	 */
	@Subscribe
	public void onLoadOneWayAnova(OneWayANOVADataSelectedEvent event)
	{
		if (event.GetPayload().getAnalysisInfo(false) != null
				&& event.GetPayload().getAnalysisInfo(false).size() > 0)
			getView().showAnalysisInfo(event.GetPayload().getAnalysisInfo(false).get(0));
	}

	/*
	 * listen for loading william's trend results so we can add it to the project
	 */
	@Subscribe
	public void onLoadWilliamsTrend(WilliamsTrendDataSelectedEvent event)
	{
		if (event.GetPayload().getAnalysisInfo(false) != null
				&& event.GetPayload().getAnalysisInfo(false).size() > 0)
			getView().showAnalysisInfo(event.GetPayload().getAnalysisInfo(false).get(0));
	}

	/*
	 * listen for loading william's trend results so we can add it to the project
	 */
	@Subscribe
	public void onLoadCurveFitPrefilter(CurveFitPrefilterDataSelectedEvent event)
	{
		if (event.GetPayload().getAnalysisInfo(false) != null
				&& event.GetPayload().getAnalysisInfo(false).size() > 0)
			getView().showAnalysisInfo(event.GetPayload().getAnalysisInfo(false).get(0));
	}

	/*
	 * listen for loading william's trend results so we can add it to the project
	 */
	@Subscribe
	public void onLoadOriogen(OriogenDataSelectedEvent event)
	{
		if (event.GetPayload().getAnalysisInfo(false) != null
				&& event.GetPayload().getAnalysisInfo(false).size() > 0)
			getView().showAnalysisInfo(event.GetPayload().getAnalysisInfo(false).get(0));
	}

	/*
	 * listen for getting a new BMDAnalysisResult set so we can add it to the project
	 */
	@Subscribe
	public void onLoadBMDAnalysisResults(BMDAnalysisDataSelectedEvent event)
	{
		if (event.GetPayload().getAnalysisInfo(false) != null
				&& event.GetPayload().getAnalysisInfo(false).size() > 0)
			getView().showAnalysisInfo(event.GetPayload().getAnalysisInfo(false).get(0));
	}

	/*
	 * listen for a new category analysis to add to the project
	 */
	@Subscribe
	public void onSelectCategoryAnalysis(CategoryAnalysisDataSelectedEvent event)
	{
		if (event.GetPayload().getAnalysisInfo(false) != null
				&& event.GetPayload().getAnalysisInfo(false).size() > 0)
			getView().showAnalysisInfo(event.GetPayload().getAnalysisInfo(false).get(0));
	}

	@Subscribe
	public void onLoadCombinedExperiement(ExpressionDataCombinedSelectedEvent event)
	{
		if (event.GetPayload().getAnalysisInfo(false) != null
				&& event.GetPayload().getAnalysisInfo(false).size() > 0)
			getView().showAnalysisInfo(event.GetPayload().getAnalysisInfo(false).get(0));
	}

	@Subscribe
	public void onLoadCombinedOneWayAnova(OneWayANOVADataCombinedSelectedEvent event)
	{
		if (event.GetPayload().getAnalysisInfo(false) != null
				&& event.GetPayload().getAnalysisInfo(false).size() > 0)
			getView().showAnalysisInfo(event.GetPayload().getAnalysisInfo(false).get(0));
	}

	@Subscribe
	public void onLoaCombineddWilliamsTrend(WilliamsTrendDataCombinedSelectedEvent event)
	{
		if (event.GetPayload().getAnalysisInfo(false) != null
				&& event.GetPayload().getAnalysisInfo(false).size() > 0)
			getView().showAnalysisInfo(event.GetPayload().getAnalysisInfo(false).get(0));
	}

	@Subscribe
	public void onLoaCombinedCurveFitPrefilter(CurveFitPrefilterDataCombinedSelectedEvent event)
	{
		if (event.GetPayload().getAnalysisInfo(false) != null
				&& event.GetPayload().getAnalysisInfo(false).size() > 0)
			getView().showAnalysisInfo(event.GetPayload().getAnalysisInfo(false).get(0));
	}

	@Subscribe
	public void onLoadCombinedOriogen(OriogenDataCombinedSelectedEvent event)
	{
		if (event.GetPayload().getAnalysisInfo(false) != null
				&& event.GetPayload().getAnalysisInfo(false).size() > 0)
			getView().showAnalysisInfo(event.GetPayload().getAnalysisInfo(false).get(0));
	}

	@Subscribe
	public void onLoadCombinedBMDAnalysisResults(BMDAnalysisDataCombinedSelectedEvent event)
	{
		if (event.GetPayload().getAnalysisInfo(false) != null
				&& event.GetPayload().getAnalysisInfo(false).size() > 0)
			getView().showAnalysisInfo(event.GetPayload().getAnalysisInfo(false).get(0));
	}

	@Subscribe
	public void onSelectCombinedCategoryAnalysis(CategoryAnalysisDataCombinedSelectedEvent event)
	{
		if (event.GetPayload().getAnalysisInfo(false) != null
				&& event.GetPayload().getAnalysisInfo(false).size() > 0)
			getView().showAnalysisInfo(event.GetPayload().getAnalysisInfo(false).get(0));
	}

	@Subscribe
	public void onProjectLoadedEvent(BMDProjectLoadedEvent event)
	{
		getView().clearList();
	}

	@Subscribe
	public void onProjectClosedEvent(CloseProjectRequestEvent event)
	{
		getView().clearList();
	}

	@Subscribe
	public void noDataSelectedEvent(NoDataSelectedEvent event)
	{
		getView().clearList();
	}

	/*
	 * Private Methods
	 */
	private void init()
	{
	}
}
