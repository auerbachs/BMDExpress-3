package com.sciome.bmdexpress2.shared.component.expression;

import java.awt.Color;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.controlsfx.control.CheckComboBox;
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

import com.sciome.bmdexpress2.mvp.model.DoseResponseExperiment;
import com.sciome.bmdexpress2.mvp.model.prefilter.OneWayANOVAResult;
import com.sciome.bmdexpress2.mvp.model.prefilter.OneWayANOVAResults;
import com.sciome.bmdexpress2.mvp.model.probe.ProbeResponse;
import com.sciome.bmdexpress2.util.annotation.pathway.CategoryMapBase;
import com.sciome.bmdexpress2.util.annotation.pathway.GenesPathways;
import com.sciome.bmdexpress2.util.annotation.pathway.ProbeGeneMaps;
import com.sciome.charts.jfree.SciomeChartViewer;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ExpressionQCBarChartComponent extends VBox
{

	OneWayANOVAResults onewayResults;
	DoseResponseExperiment doseResponseExperiment;

	final static String P05 = "0.05";
	final static String P01 = "0.01";
	final static String P1 = "0.1";

	final static String A2 = "2";
	final static String A15 = "1.5";
	final static String A12 = "1.2";

	final static String DOSE_LEVEL = "Dose Level ";

	String[] pValues = { P01, P05, P1 };

	String[] absValues = { A2, A15, A12 };

	private ComboBox<String> pValueBox;

	private ComboBox<String> foldChangeBox;

	private ComboBox<String> pathwayDBBox;
	private CheckComboBox<String> pathwayBox;

	SciomeChartViewer barChartViewer;
	private Map<String, Set<String>> bioPlanetToProbeMap;
	private Map<String, Set<String>> reactomeToProbeMap;

	public ExpressionQCBarChartComponent(OneWayANOVAResults onewayResults,
			DoseResponseExperiment doseResponseExperiment)
	{
		super();
		this.onewayResults = onewayResults;
		this.doseResponseExperiment = doseResponseExperiment;

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
		setUpPathwayFilter();

		pathwayDBBox = new ComboBox<>();
		pathwayDBBox.getItems().addAll(new String[] { "NONE", "REACTOME", "BioPlanet" });
		pathwayDBBox.setValue("NONE");
		pathwayDBBox.setOnAction((event) ->
		{
			Platform.runLater(() ->
			{
				List<String> pathways = new ArrayList<>();
				if (pathwayDBBox.getValue().equals("REACTOME"))
					pathways.addAll(reactomeToProbeMap.keySet());
				else if (pathwayDBBox.getValue().equals("BioPlanet"))
					pathways.addAll(bioPlanetToProbeMap.keySet());

				Collections.sort(pathways);
				pathwayBox.getItems().clear();
				pathwayBox.getItems().setAll(FXCollections.observableArrayList(pathways));

			});
			updateChart();

		});
		pathwayBox = new CheckComboBox<>();
		pathwayBox.setMaxWidth(200);
		pathwayBox.getItems().addAll(new String[] {});

		pathwayBox.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {

			@Override
			public void onChanged(Change<? extends String> c)
			{
				updateChart();
			}

		});

		HBox comboHLayout = new HBox();
		HBox pLayout = new HBox(pLabel, pValueBox);
		HBox fcLayout = new HBox(absLabel, foldChangeBox);
		HBox pathwayLayout = new HBox(new Label("Pathway DB"), pathwayDBBox, new Label("Pathways"),
				pathwayBox);
		pLayout.setAlignment(Pos.CENTER);
		pLayout.setSpacing(10.0);
		fcLayout.setAlignment(Pos.CENTER);
		fcLayout.setSpacing(10.0);

		pathwayLayout.setAlignment(Pos.CENTER);
		pathwayLayout.setSpacing(10.0);

		comboHLayout.getChildren().addAll(pLayout, fcLayout, pathwayLayout);
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

		Map<Integer, Integer> upMap = new HashMap<>();
		Map<Integer, Integer> downMap = new HashMap<>();

		for (OneWayANOVAResult result : onewayResults.getOneWayANOVAResults())
		{
			if (!isInPathway(result.getProbeID()))
				continue;
			for (int i = 0; i < result.getNoelLoelPValues().size(); i++)
			{
				if (!upMap.containsKey(i + 1))
					upMap.put(i + 1, 0);

				if (!downMap.containsKey(i + 1))
					downMap.put(i + 1, 0);

				if (result.getNoelLoelPValues().get(i) < pvalueCut
						&& foldChangeCut <= Math.abs(result.getFoldChanges().get(i)))
				{
					if (result.getFoldChanges().get(i) > 0)
						upMap.put(i + 1, upMap.get(i + 1) + 1);
					else
						downMap.put(i + 1, downMap.get(i + 1) + 1);
				}

			}

		}

		List<Integer> labels = new ArrayList<>();
		labels.addAll(upMap.keySet());

		Collections.sort(labels);

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (Integer key : labels)
		{

			dataset.addValue(upMap.get(key), "Up", DOSE_LEVEL + " " + key);
			dataset.addValue(downMap.get(key), "Down", DOSE_LEVEL + " " + key);

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

	private boolean isInPathway(String probe)
	{
		if (pathwayDBBox.getValue().equals("NONE"))
			return true;
		Map<String, Set<String>> map = reactomeToProbeMap;
		if (pathwayDBBox.getValue().equals("BioPlanet"))
			map = bioPlanetToProbeMap;

		if (pathwayBox.getCheckModel().getItemCount() == 0)
			return true;
		for (String pathway : pathwayBox.getCheckModel().getCheckedItems())
			if (map.get(pathway).contains(probe))
				return true;
		return false;
	}

	private void setUpPathwayFilter()
	{
		// create a hashtable of probes.
		Hashtable<String, Integer> probeHash = new Hashtable<>();
		for (ProbeResponse probeResponse : doseResponseExperiment.getProbeResponses())
			probeHash.put(probeResponse.getProbe().getId(), 1);

		ProbeGeneMaps probeGeneMaps = new ProbeGeneMaps(doseResponseExperiment);
		probeGeneMaps.readProbes(false);
		// probeGeneMaps.readArraysInfo();
		probeGeneMaps.setProbesHash(probeHash);
		String chip = "Generic";
		if (doseResponseExperiment.getChip() != null)
			chip = doseResponseExperiment.getChip().getGeoID();
		probeGeneMaps.probeGeneMaping(chip, true);

		Set<String> probeSet = new HashSet<>();
		for (OneWayANOVAResult oneway : onewayResults.getOneWayANOVAResults())
			probeSet.add(oneway.getProbeID());

		GenesPathways reactomePathways = new GenesPathways(probeGeneMaps, "REACTOME");
		GenesPathways bioPlanetPathways = new GenesPathways(probeGeneMaps, "BioPlanet");

		reactomeToProbeMap = calculatePathwayStructure(probeGeneMaps, reactomePathways, probeSet);
		bioPlanetToProbeMap = calculatePathwayStructure(probeGeneMaps, bioPlanetPathways, probeSet);

	}

	private Map<String, Set<String>> calculatePathwayStructure(ProbeGeneMaps probeGeneMaps,
			CategoryMapBase reactomePathways, Set<String> probeSet)
	{
		Map<String, Set<String>> pathwayToProbeMap = new HashMap<>();

		Hashtable<String, String> titleHash = reactomePathways.getTitleHash();
		Hashtable<String, Vector<String>> subHash = reactomePathways.subHash();
		Hashtable<String, Vector<String>> subHashG2Ids = probeGeneMaps.subHashG2Ids();

		for (String pathway : titleHash.values())
			pathwayToProbeMap.put(pathway, new HashSet<>());

		for (String pathwayCode : subHash.keySet())
			for (String entrez : subHash.get(pathwayCode))
				for (String p : subHashG2Ids.get(entrez))
					if (titleHash.get(pathwayCode) != null
							&& pathwayToProbeMap.get(titleHash.get(pathwayCode)) != null
							&& probeSet.contains(p))
						pathwayToProbeMap.get(titleHash.get(pathwayCode)).add(p);

		List<String> remove = new ArrayList<>();
		for (String key : pathwayToProbeMap.keySet())
			if (pathwayToProbeMap.get(key).size() == 0)
				remove.add(key);

		for (String key : remove)
			pathwayToProbeMap.remove(key);

		return pathwayToProbeMap;
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
