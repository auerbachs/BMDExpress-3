package com.sciome.bmdexpress2.mvp.view.visualization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sciome.bmdexpress2.mvp.model.BMDExpressAnalysisDataSet;
import com.sciome.bmdexpress2.mvp.model.BMDExpressAnalysisRow;
import com.sciome.bmdexpress2.mvp.model.ChartKey;
import com.sciome.bmdexpress2.mvp.model.category.CategoryAnalysisResult;
import com.sciome.bmdexpress2.mvp.model.category.CategoryAnalysisResults;
import com.sciome.bmdexpress2.mvp.model.category.ReferenceGeneProbeStatResult;
import com.sciome.bmdexpress2.mvp.model.stat.ProbeStatResult;
import com.sciome.bmdexpress2.mvp.model.stat.StatResult;
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
 * take care of charts and special view options for the Category Analysis visualizations
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

		chartCache.put(
				BMD_BMDL_SCATTER + "-" + CategoryAnalysisResults.BMD_MEDIAN
						+ CategoryAnalysisResults.BMDL_MEDIAN,
				new SciomeScatterChartJFree("", new ArrayList<>(),
						new ChartKey(CategoryAnalysisResults.BMD_MEDIAN, null),
						new ChartKey(CategoryAnalysisResults.BMDL_MEDIAN, null),
						TPODAnalysisDataVisualizationView.this));
		chartCache.put(
				BMD_BMDL_SCATTER + "-" + CategoryAnalysisResults.BMD_MEAN + CategoryAnalysisResults.BMDL_MEAN,
				new SciomeScatterChartJFree("", new ArrayList<>(),
						new ChartKey(CategoryAnalysisResults.BMD_MEAN, null),
						new ChartKey(CategoryAnalysisResults.BMDL_MEAN, null),
						TPODAnalysisDataVisualizationView.this));
		chartCache.put(
				BMD_BMDL_SCATTER + "-" + CategoryAnalysisResults.BMDU_MEAN + CategoryAnalysisResults.BMD_MEAN,
				new SciomeScatterChartJFree("", new ArrayList<>(),
						new ChartKey(CategoryAnalysisResults.BMDU_MEAN, null),
						new ChartKey(CategoryAnalysisResults.BMD_MEAN, null),
						TPODAnalysisDataVisualizationView.this));
		chartCache.put(
				BMD_BMDL_SCATTER + "-" + CategoryAnalysisResults.BMDU_MEAN
						+ CategoryAnalysisResults.BMDL_MEAN,
				new SciomeScatterChartJFree("", new ArrayList<>(),
						new ChartKey(CategoryAnalysisResults.BMDU_MEAN, null),
						new ChartKey(CategoryAnalysisResults.BMDL_MEAN, null),
						TPODAnalysisDataVisualizationView.this));

		chartCache.put("DEFAULT-Accumulation", new SciomeAccumulationPlotJFree("Accumulation",
				new ArrayList<>(), new ChartKey(CategoryAnalysisResults.BMD_MEDIAN, null), 0.0, this));

		chartCache.put("DEFAULT-" + CategoryAnalysisResults.BMD_MEDIAN + CategoryAnalysisResults.BMDL_MEDIAN,
				new SciomeScatterChartJFree("", new ArrayList<>(),
						new ChartKey(CategoryAnalysisResults.BMD_MEDIAN, null),
						new ChartKey(CategoryAnalysisResults.BMDL_MEDIAN, null),
						TPODAnalysisDataVisualizationView.this));

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
			SciomeChartBase chart1 = chartCache.get(BMD_BMDL_SCATTER + "-"
					+ CategoryAnalysisResults.BMD_MEDIAN + CategoryAnalysisResults.BMDL_MEDIAN);
			chartsList.add(chart1);
			SciomeChartBase chart2 = chartCache.get(BMD_BMDL_SCATTER + "-" + CategoryAnalysisResults.BMD_MEAN
					+ CategoryAnalysisResults.BMDL_MEAN);
			chartsList.add(chart2);
			SciomeChartBase chart3 = chartCache.get(BMD_BMDL_SCATTER + "-" + CategoryAnalysisResults.BMDU_MEAN
					+ CategoryAnalysisResults.BMD_MEAN);
			chartsList.add(chart3);
			SciomeChartBase chart4 = chartCache.get(BMD_BMDL_SCATTER + "-" + CategoryAnalysisResults.BMDU_MEAN
					+ CategoryAnalysisResults.BMDL_MEAN);
			chartsList.add(chart4);

		}
		else if (chartKey.equals(DEFAULT_CHARTS))
		{
			SciomeChartBase chart1 = chartCache.get("DEFAULT-Accumulation");
			chartsList.add(chart1);

			SciomeChartBase chart3 = chartCache.get(
					"DEFAULT-" + CategoryAnalysisResults.BMD_MEDIAN + CategoryAnalysisResults.BMDL_MEDIAN);
			chartsList.add(chart3);

		}

		List<ChartDataPack> chartDataPacks = presenter.getBMDAnalysisDataSetChartDataPack(results, pack,
				getUsedChartKeys(), getMathedChartKeys(),
				new ChartKey(CategoryAnalysisResults.CATEGORY_DESCRIPTION, null));

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

	private Map<String, Double> getBMDStatResultCountsFromCatAnalysis(
			List<BMDExpressAnalysisDataSet> catResultss, DataFilterPack pack, boolean uniqueBMDCount)
	{
		Map<String, Double> mapCount = new HashMap<>();
		if (catResultss == null)
			return mapCount;
		Set<ProbeStatResult> probeIdSet = new HashSet<>();
		for (BMDExpressAnalysisDataSet results : catResultss)
		{
			for (BMDExpressAnalysisRow row : results.getAnalysisRows())
			{
				CategoryAnalysisResult catResult = (CategoryAnalysisResult) row.getObject();
				if (pack != null && !pack.passesFilter(row))
					continue;

				if (catResult.getReferenceGeneProbeStatResults() == null)
					continue;
				for (ReferenceGeneProbeStatResult geneProbeStat : catResult
						.getReferenceGeneProbeStatResults())
				{
					for (ProbeStatResult probeStatResult : geneProbeStat.getProbeStatResults())
					{
						if (uniqueBMDCount && probeIdSet.contains(probeStatResult))
							continue;

						StatResult result = probeStatResult.getBestStatResult();
						if (result == null)
							continue;
						if (mapCount.containsKey(result.toString()))
						{
							mapCount.put(result.toString(), mapCount.get(result.toString()) + 1.0);
						}
						else
						{
							mapCount.put(result.toString(), 1.0);
						}
						probeIdSet.add(probeStatResult);
					}
				}
			}
		}

		return mapCount;
	}

}
