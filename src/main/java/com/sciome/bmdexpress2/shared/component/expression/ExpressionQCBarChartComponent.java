package com.sciome.bmdexpress2.shared.component.expression;

import java.awt.Color;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import com.sciome.bmdexpress2.mvp.model.prefilter.OneWayANOVAResult;
import com.sciome.bmdexpress2.mvp.model.prefilter.OneWayANOVAResults;
import com.sciome.charts.jfree.SciomeChartViewer;

import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ExpressionQCBarChartComponent extends VBox
{

	OneWayANOVAResults onewayResults;

	final static String P05 = "0.05";
	final static String P01 = "0.01";
	final static String P1 = "0.1";

	final static String A2 = "2";
	final static String A15 = "1.5";
	final static String A12 = "1.2";

	final static String DOSE_LEVEL = "Dose Level ";

	String[] pValues = { P05, P01, P1 };

	String[] absValues = { A2, A15, A12 };

	private ComboBox<String> pValueBox;

	private ComboBox<String> foldChangeBox;

	SciomeChartViewer barChartViewer;

	public ExpressionQCBarChartComponent(OneWayANOVAResults onewayResults)
	{
		super();
		this.onewayResults = onewayResults;

		Label pLabel = new Label("T-Test p-value <");
		Label absLabel = new Label("Abs fold change >=");

		pValueBox = new ComboBox<>();
		pValueBox.getItems().addAll(pValues);
		pValueBox.setValue(P05);

		pValueBox.setOnAction((event) ->
		{
			updateChart();
		});

		foldChangeBox = new ComboBox<>();
		foldChangeBox.getItems().addAll(absValues);
		foldChangeBox.setValue(A2);
		foldChangeBox.setOnAction((event) ->
		{
			updateChart();
		});

		HBox comboHLayout = new HBox();
		comboHLayout.getChildren().addAll(new HBox(pLabel, pValueBox), new HBox(absLabel, foldChangeBox));
		comboHLayout.setSpacing(50.0);
		this.getChildren().add(comboHLayout);
		updateChart();

	}

	/*
	 * recalculate the bars are redraw the chart
	 */
	private void updateChart()
	{

		double foldChangeCut = 0.0;
		double pvalueCut = 0.0;

		if (pValueBox.getValue().equals(P05))
			pvalueCut = 0.05;
		else if (pValueBox.getValue().equals(P01))
			pvalueCut = 0.01;
		else if (pValueBox.getValue().equals(P1))
			pvalueCut = 0.1;

		if (foldChangeBox.getValue().equals(A2))
			foldChangeCut = 2.0;
		else if (foldChangeBox.getValue().equals(A15))
			foldChangeCut = 1.5;
		else if (foldChangeBox.getValue().equals(A12))
			foldChangeCut = 1.2;

		// now we have the cuttoff values, loop and fill up map for bar chartting

		Map<String, Integer> upMap = new HashMap<>();
		Map<String, Integer> downMap = new HashMap<>();

		for (OneWayANOVAResult result : onewayResults.getOneWayANOVAResults())
		{
			for (int i = 0; i < result.getNoelLoelPValues().size(); i++)
			{
				if (!upMap.containsKey(DOSE_LEVEL + i))
					upMap.put(DOSE_LEVEL + i, 0);

				if (!downMap.containsKey(DOSE_LEVEL + i))
					downMap.put(DOSE_LEVEL + i, 0);

				if (result.getNoelLoelPValues().get(i) < pvalueCut
						&& foldChangeCut >= Math.abs(result.getFoldChanges().get(i)))
				{
					if (result.getFoldChanges().get(i) > 0)
						upMap.put(DOSE_LEVEL + i, upMap.get(DOSE_LEVEL + i) + 1);
					else
						downMap.put(DOSE_LEVEL + i, downMap.get(DOSE_LEVEL + i) + 1);
				}

			}

		}

		List<String> labels = new ArrayList<>();
		labels.addAll(upMap.keySet());

		Collections.sort(labels);

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (String key : labels)
		{

			dataset.addValue(upMap.get(key), "Up", key);
			dataset.addValue(downMap.get(key), "Down", key);

		}

		JFreeChart chart = createChart(dataset);

		Platform.runLater(() ->
		{
			if (barChartViewer != null)
				this.getChildren().removeAll(barChartViewer);
			barChartViewer = new SciomeChartViewer(chart);
			this.getChildren().add(barChartViewer);
		});

	}

	/**
	 * Creates a sample chart.
	 *
	 * @param dataset
	 *            the dataset for the chart.
	 *
	 * @return a sample chart.
	 */
	private JFreeChart createChart(CategoryDataset dataset)
	{

		JFreeChart chart = ChartFactory.createStackedBarChart("UPs and DOWNs", "Dose Levels", "Value",
				dataset);
		chart.addSubtitle(new TextTitle("qc bar chart"));
		CategoryPlot plot = (CategoryPlot) chart.getPlot();

		CategoryAxis xAxis = plot.getDomainAxis();
		xAxis.setLowerMargin(0.01);
		xAxis.setUpperMargin(0.01);
		xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
		AttributedString yLabel = new AttributedString("Count");
		yLabel.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_ULTRABOLD);
		yLabel.addAttribute(TextAttribute.SIZE, 14);
		yLabel.addAttribute(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUPER, 1, 2);
		plot.getRangeAxis().setAttributedLabel(yLabel);
		StackedBarRenderer renderer = (StackedBarRenderer) plot.getRenderer();
		renderer.setDrawBarOutline(false);
		renderer.setBarPainter(new StandardBarPainter());
		// renderer.setBaseItemLabelsVisible(true);
		// renderer.setBaseItemLabelGenerator(
		// new StandardCategoryItemLabelGenerator());
		// renderer.setBaseItemLabelPaint(Color.WHITE);
		renderer.setSeriesPaint(0, new Color(0, 55, 122));
		renderer.setSeriesPaint(1, new Color(24, 123, 58));
		renderer.setSeriesPaint(2, Color.RED);

		return chart;

	}

}
