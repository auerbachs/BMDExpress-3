package com.sciome.bmdexpress2.mvp.view.visualization;

import java.util.ArrayList;
import java.util.List;

import com.sciome.bmdexpress2.mvp.model.ChartKey;
import com.sciome.bmdexpress2.mvp.model.tpod.TPODAnalysisResults;
import com.sciome.bmdexpress2.mvp.presenter.visualization.TPODAnalysisDataVisualizationPresenter;
import com.sciome.bmdexpress2.mvp.viewinterface.visualization.IDataVisualizationView;
import com.sciome.bmdexpress2.service.VisualizationService;
import com.sciome.bmdexpress2.serviceInterface.IVisualizationService;
import com.sciome.bmdexpress2.shared.eventbus.BMDExpressEventBus;
import com.sciome.charts.SciomeChartBase;
import com.sciome.charts.data.ChartDataPack;
import com.sciome.charts.jfree.SciomeAccumulationPlotJFree;
import com.sciome.charts.jfree.SciomeScatterChartJFree;
import com.sciome.filter.DataFilterPack;

/*
 * take care of charts and special view options for the TPOD Analysis visualizations
 * 
 */
public class TPODAnalysisDataVisualizationView extends DataVisualizationView implements IDataVisualizationView
{

	private static final String BMD_BMDL_SCATTER = "BMD vs BMDL Scatter Plots";

	public TPODAnalysisDataVisualizationView()
	{
		super();
		IVisualizationService service = new VisualizationService();
		presenter = new TPODAnalysisDataVisualizationPresenter(this, service,
				BMDExpressEventBus.getInstance());

		chartCache.put(BMD_BMDL_SCATTER + "-" + TPODAnalysisResults.BMD + TPODAnalysisResults.BMDL,
				new SciomeScatterChartJFree("", new ArrayList<>(),
						new ChartKey(TPODAnalysisResults.BMD, null),
						new ChartKey(TPODAnalysisResults.BMDL, null),
						TPODAnalysisDataVisualizationView.this));

		chartCache.put(BMD_BMDL_SCATTER + "-" + TPODAnalysisResults.BMDU + TPODAnalysisResults.BMD,
				new SciomeScatterChartJFree("", new ArrayList<>(),
						new ChartKey(TPODAnalysisResults.BMDU, null),
						new ChartKey(TPODAnalysisResults.BMD, null), TPODAnalysisDataVisualizationView.this));
		chartCache.put(BMD_BMDL_SCATTER + "-" + TPODAnalysisResults.BMDU + TPODAnalysisResults.BMDL,
				new SciomeScatterChartJFree("", new ArrayList<>(),
						new ChartKey(TPODAnalysisResults.BMDU, null),
						new ChartKey(TPODAnalysisResults.BMDL, null),
						TPODAnalysisDataVisualizationView.this));

		chartCache.put("DEFAULT-Accumulation", new SciomeAccumulationPlotJFree("Accumulation",
				new ArrayList<>(), new ChartKey(TPODAnalysisResults.BMD, null), 0.0, this));

	}

	@Override
	public void redrawCharts(DataFilterPack pack)
	{
		// set this to false by default.
		// but if the user wants to see curve overlay, then we will
		/// set this to true and not view custom charts because
		// we want all the real estate we can get
		ignoreCustomCharts = false;

		defaultDPack = pack;
		String chartKey = cBox.getSelectionModel().getSelectedItem();
		if (results == null || results.size() == 0)
			return;

		chartsList = new ArrayList<>();
		if (chartKey.equals(BMD_BMDL_SCATTER))
		{
			SciomeChartBase chart1 = chartCache
					.get(BMD_BMDL_SCATTER + "-" + TPODAnalysisResults.BMD + TPODAnalysisResults.BMDL);
			chartsList.add(chart1);

			SciomeChartBase chart3 = chartCache
					.get(BMD_BMDL_SCATTER + "-" + TPODAnalysisResults.BMDU + TPODAnalysisResults.BMD);
			chartsList.add(chart3);
			SciomeChartBase chart4 = chartCache
					.get(BMD_BMDL_SCATTER + "-" + TPODAnalysisResults.BMDU + TPODAnalysisResults.BMDL);
			chartsList.add(chart4);

		}
		else if (chartKey.equals(DEFAULT_CHARTS))
		{
			SciomeChartBase chart1 = chartCache.get("DEFAULT-Accumulation");
			chartsList.add(chart1);

		}

		List<ChartDataPack> chartDataPacks = presenter.getBMDAnalysisDataSetChartDataPack(results, pack,
				getUsedChartKeys(), getMathedChartKeys(),
				new ChartKey(TPODAnalysisResults.TPOD_METHOD, null));

		showCharts(chartDataPacks);

	}

	@Override
	public List<String> getCannedCharts()
	{
		List<String> resultList = new ArrayList<>();
		resultList.add(DEFAULT_CHARTS);

		resultList.add(BMD_BMDL_SCATTER);

		return resultList;
	}

}
