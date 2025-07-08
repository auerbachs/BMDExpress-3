package com.sciome.bmdexpress2.shared.component.expression;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.sciome.bmdexpress2.mvp.model.DoseResponseExperiment;
import com.sciome.bmdexpress2.mvp.model.probe.Probe;
import com.sciome.bmdexpress2.mvp.model.probe.ProbeResponse;
import com.sciome.bmdexpress2.mvp.model.refgene.ReferenceGene;
import com.sciome.bmdexpress2.mvp.model.refgene.ReferenceGeneAnnotation;
import com.sciome.bmdexpress2.service.TissueContaminationService;
import com.sciome.charts.jfree.SciomeChartViewer;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class TissueContaminationChartComponent extends VBox
{

	DoseResponseExperiment doseResponseExperiment;

	private ComboBox<String> tissueBox;
	private TextArea textArea = new TextArea();

	SciomeChartViewer tissueContaminationViewer;
	private HBox tissueLayout;
	private TissueContaminationService tissueService;

	public TissueContaminationChartComponent(DoseResponseExperiment doseResponseExperiment)
	{
		super();
		this.doseResponseExperiment = doseResponseExperiment;

		tissueService = new TissueContaminationService();

		Label tissueLabel = new Label("Tissue");
		textArea.setPromptText("Add custom genes here...");

		textArea.textProperty().addListener((observable, oldValue, newValue) ->
		{
			updateChart();
		});

		tissueBox = new ComboBox<>();
		List<String> tissues = tissueService.getSortedListOfTissues();
		tissues.add(0, "Custom Genes Only");
		tissueBox.getItems().addAll(tissues);
		tissueBox.setValue(tissues.get(1));
		VBox.setVgrow(textArea, Priority.NEVER);

		tissueBox.setOnAction((event) ->
		{
			updateChart();
		});
		// Lock height and give width
		textArea.setPrefHeight(60);
		textArea.setMinHeight(60);
		textArea.setMaxHeight(60);
		textArea.setPrefWidth(400);
		tissueLayout = new HBox(10, tissueLabel, tissueBox, textArea);
		tissueLayout.setAlignment(Pos.CENTER_LEFT);
		VBox.setMargin(tissueLayout, new Insets(10));
		tissueLayout.setMaxHeight(90);

		this.getChildren().add(tissueLayout);

		updateChart();

	}

	/*
	 * recalculate the bars are redraw the chart
	 */
	private void updateChart()
	{
		List<String> genes = this.tissueService.getListOfGenesForTissue(tissueBox.getValue());

		String[] customgenes = textArea.getText().split("\\s+");
		Set<String> genesSet = new HashSet<>();
		for (String g : genes)
			genesSet.add(g.toLowerCase());

		for (String g : customgenes)
			genesSet.add(g.toLowerCase());

		// get the dataseries
		Map<Probe, String> probesToUse = new HashMap<>();
		for (ReferenceGeneAnnotation ann : doseResponseExperiment.getReferenceGeneAnnotations())
		{
			for (ReferenceGene rg : ann.getReferenceGenes())
			{
				if (genesSet.contains(rg.getGeneSymbol().toLowerCase()))
					probesToUse.put(ann.getProbe(), rg.getGeneSymbol().toLowerCase());
			}
		}

		XYSeriesCollection dataset = new XYSeriesCollection();

		Map<String, Integer> geneCountMap = new HashMap<>();
		for (ProbeResponse pr : doseResponseExperiment.getProbeResponses())
		{
			if (probesToUse.get(pr.getProbe()) == null)
				continue;
			String gene = probesToUse.get(pr.getProbe());

			int count = 1;
			if (geneCountMap.containsKey(gene))
				geneCountMap.put(gene, geneCountMap.get(gene) + 1);
			else
				geneCountMap.put(gene, 0);

			String seriesLabel = gene;
			if (geneCountMap.get(gene) > 0)
				seriesLabel += " " + geneCountMap.get(gene);
			XYSeries series = new XYSeries(seriesLabel);

			for (Float response : pr.getResponses())
			{
				series.add((double) count++, response.doubleValue());

			}

			dataset.addSeries(series);

		}

		JFreeChart chart = createChart(dataset);

		Platform.runLater(() ->
		{
			if (tissueContaminationViewer != null)
				this.getChildren().removeAll(tissueContaminationViewer);
			tissueContaminationViewer = new SciomeChartViewer(chart);
			this.getChildren().add(tissueContaminationViewer);
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
	private JFreeChart createChart(XYSeriesCollection dataset)
	{

		JFreeChart chart = ChartFactory.createXYLineChart("Tissue Contamination", "Sample", "Dose Response",
				dataset);

		// chart.addSubtitle(new TextTitle("qc bar chart"));
		XYPlot plot = (XYPlot) chart.getPlot();

		return chart;

	}

}
