package com.sciome.bmdexpress2.mvp.view.visualization;

import java.util.ArrayList;
import java.util.List;

import com.sciome.bmdexpress2.mvp.model.BMDExpressAnalysisDataSet;
import com.sciome.bmdexpress2.mvp.model.ChartKey;
import com.sciome.bmdexpress2.mvp.model.DoseResponseExperiment;
import com.sciome.bmdexpress2.mvp.model.pca.IntensityResults;
import com.sciome.bmdexpress2.mvp.model.pca.PCAResults;
import com.sciome.bmdexpress2.mvp.model.prefilter.OneWayANOVAResults;
import com.sciome.bmdexpress2.mvp.presenter.visualization.ExpressionDataVisualizationPresenter;
import com.sciome.bmdexpress2.mvp.viewinterface.visualization.IDataVisualizationView;
import com.sciome.bmdexpress2.service.VisualizationService;
import com.sciome.bmdexpress2.serviceInterface.IVisualizationService;
import com.sciome.bmdexpress2.shared.component.expression.ExpressionQCBarChartComponent;
import com.sciome.bmdexpress2.shared.eventbus.BMDExpressEventBus;
import com.sciome.charts.SciomeChartBase;
import com.sciome.charts.data.ChartDataPack;
import com.sciome.charts.jfree.SciomeDensityChartJFree;
import com.sciome.charts.jfree.SciomePCAJFree;
import com.sciome.filter.DataFilterPack;

public class ExpressionDataVisualizationView extends DataVisualizationView implements IDataVisualizationView
{
	private static final String INTENSITY = "Density Chart";
	private static final String QC_BAR = "QC Bar";
	private static final String DEFAULT = "DEFAULT";

	public ExpressionDataVisualizationView()
	{
		super();
		IVisualizationService service = new VisualizationService();
		presenter = new ExpressionDataVisualizationPresenter(this, service, BMDExpressEventBus.getInstance());

		chartCache.put(DEFAULT + "-" + "PC1 V PC2", new SciomePCAJFree("", new ArrayList<>(),
				new ChartKey(PCAResults.PC1, null), new ChartKey(PCAResults.PC2, null), this));

		chartCache.put(DEFAULT + "-" + "PC1 V PC3", new SciomePCAJFree("", new ArrayList<>(),
				new ChartKey(PCAResults.PC1, null), new ChartKey(PCAResults.PC3, null), this));

		chartCache.put(DEFAULT + "-" + "PC1 V PC4", new SciomePCAJFree("", new ArrayList<>(),
				new ChartKey(PCAResults.PC1, null), new ChartKey(PCAResults.PC4, null), this));

		chartCache.put(DEFAULT + "-" + "PC2 V PC3", new SciomePCAJFree("", new ArrayList<>(),
				new ChartKey(PCAResults.PC2, null), new ChartKey(PCAResults.PC3, null), this));

		chartCache.put(DEFAULT + "-" + "PC2 V PC4", new SciomePCAJFree("", new ArrayList<>(),
				new ChartKey(PCAResults.PC2, null), new ChartKey(PCAResults.PC4, null), this));

		chartCache.put(DEFAULT + "-" + "PC3 V PC4", new SciomePCAJFree("", new ArrayList<>(),
				new ChartKey(PCAResults.PC3, null), new ChartKey(PCAResults.PC4, null), this));

		chartCache.put(INTENSITY, new SciomeDensityChartJFree("", new ArrayList<>(),
				new ChartKey(IntensityResults.RESPONSE, null), this));
	}

	@Override
	public void redrawCharts(DataFilterPack dataFilterPack)
	{
		String chartKey = cBox.getSelectionModel().getSelectedItem();
		defaultDPack = dataFilterPack;
		if (results == null || results.size() == 0)
			return;

		chartsList = new ArrayList<>();

		if (chartKey.equals(INTENSITY))
		{
			List<BMDExpressAnalysisDataSet> intensityResults = ((ExpressionDataVisualizationPresenter) presenter)
					.calculateIntensity((DoseResponseExperiment) results.get(0));

			SciomeChartBase chart1 = chartCache.get(INTENSITY);
			chartsList.add(chart1);

			List<ChartDataPack> chartDataPacks = presenter.getBMDAnalysisDataSetChartDataPack(
					intensityResults, dataFilterPack, getUsedChartKeys(), getMathedChartKeys(),
					new ChartKey(IntensityResults.RESPONSE, null));
			showCharts(chartDataPacks);
		}
		else if (chartKey.equals(QC_BAR))
		{
			OneWayANOVAResults oneways = ((ExpressionDataVisualizationPresenter) presenter)
					.getOneWayResult((DoseResponseExperiment) results.get(0));

			List<BMDExpressAnalysisDataSet> resultList = new ArrayList<>();
			resultList.add(oneways);
			ExpressionQCBarChartComponent qcBar = new ExpressionQCBarChartComponent(oneways,
					(DoseResponseExperiment) results.get(0));
			chartsList.add(qcBar);

			showCharts(null);
		}
		else
		{
			List<BMDExpressAnalysisDataSet> pcaResults = new ArrayList<BMDExpressAnalysisDataSet>();
			pcaResults.add(((ExpressionDataVisualizationPresenter) presenter)
					.calculatePCA((DoseResponseExperiment) results.get(0)));

			SciomeChartBase chart1 = chartCache.get(DEFAULT + "-" + "PC1 V PC2");
			chartsList.add(chart1);
			SciomeChartBase chart2 = chartCache.get(DEFAULT + "-" + "PC1 V PC3");
			chartsList.add(chart2);
			SciomeChartBase chart3 = chartCache.get(DEFAULT + "-" + "PC1 V PC4");
			chartsList.add(chart3);
			SciomeChartBase chart4 = chartCache.get(DEFAULT + "-" + "PC2 V PC3");
			chartsList.add(chart4);
			SciomeChartBase chart5 = chartCache.get(DEFAULT + "-" + "PC2 V PC4");
			chartsList.add(chart5);
			SciomeChartBase chart6 = chartCache.get(DEFAULT + "-" + "PC3 V PC4");
			chartsList.add(chart6);

			List<ChartDataPack> chartDataPacks = presenter.getBMDAnalysisDataSetChartDataPack(pcaResults,
					dataFilterPack, getUsedChartKeys(), getMathedChartKeys(),
					new ChartKey(PCAResults.DOSAGE, null));
			showCharts(chartDataPacks);
		}
	}

	@Override
	public List<String> getCannedCharts()
	{
		List<String> resultList = new ArrayList<>();
		resultList.add(DEFAULT_CHARTS);
		resultList.add(QC_BAR);
		resultList.add(INTENSITY);

		return resultList;
	}

}
