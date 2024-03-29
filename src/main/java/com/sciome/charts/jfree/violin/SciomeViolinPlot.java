package com.sciome.charts.jfree.violin;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.SlidingCategoryDataset;

import com.sciome.bmdexpress2.mvp.model.ChartKey;
import com.sciome.bmdexpress2.util.ShapeCreator;
import com.sciome.charts.SciomeChartBase;
import com.sciome.charts.SciomeChartListener;
import com.sciome.charts.data.ChartConfiguration;
import com.sciome.charts.data.ChartDataPack;
import com.sciome.charts.jfree.SciomeChartViewer;
import com.sciome.charts.jfree.SciomeNumberAxisGeneratorJFree;
import com.sciome.charts.model.SciomeData;
import com.sciome.charts.model.SciomeSeries;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.util.Callback;

public abstract class SciomeViolinPlot extends SciomeChartBase<String, List<Double>>
{
	private JFreeChart				chart;
	private SlidingCategoryDataset	slidingDataset;
	private ChartKey				key;

	// Adjustable settings
	private Double					bandwidth		= null;
	private Integer					maxNodesShown	= 5;

	public SciomeViolinPlot(String title, List<ChartDataPack> chartDataPacks, ChartKey key,
			boolean allowXAxisSlider, boolean allowYAxisSlider, SciomeChartListener chartListener)
	{
		super(title, chartDataPacks, new ChartKey[] { key }, allowXAxisSlider, allowYAxisSlider,
				chartListener);
		this.key = key;
		this.configurationButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e)
			{
				showConfiguration();
			}
		});

		showLogAxes(false, false, false, false);
		getLogYAxis().selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val)
			{
				showChart();
			}
		});

		getLockYAxis().selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val)
			{
				showChart();
			}
		});
	}

	@Override
	protected Node generateChart(ChartKey[] keys, ChartConfiguration chartConfiguration)
	{
		ViolinCategoryDataset dataset = new ViolinCategoryDataset(bandwidth);

		double minValue = Double.POSITIVE_INFINITY;
		double maxValue = Double.NEGATIVE_INFINITY;

		for (SciomeSeries<String, List<Double>> series : getSeriesData())
		{
			String seriesName = series.getName();
			for (SciomeData<String, List<Double>> chartData : series.getData())
			{
				List value = chartData.getYValue();
				for (int i = 0; i < value.size(); i++)
				{
					double val = (double) value.get(i);
					if (val < minValue)
						minValue = val;
					if (val > maxValue)
						maxValue = val;
				}
				if (value != null)
				{
					dataset.add(value, seriesName, chartData.getXValue());
				}
			}
		}

		slidingDataset = new SlidingCategoryDataset(dataset, 0, maxNodesShown);
		setSliders(dataset.getColumnCount());

		// Set axis
		CategoryAxis xAxis = new CategoryAxis();
		ValueAxis yAxis = SciomeNumberAxisGeneratorJFree.generateAxis(getLogYAxis().isSelected(),
				key.toString());
		yAxis.setVerticalTickLabels(true);

		ViolinRenderer renderer = new ViolinRenderer();

		// Set tooltip string
		CategoryToolTipGenerator tooltipGenerator = new CategoryToolTipGenerator() {
			@Override
			public String generateToolTip(CategoryDataset dataset, int row, int column)
			{
				try
				{
					return getSeriesData().get(row).getData().get(column).getExtraValue().toString();
				}
				catch (Exception e)
				{
					return "";
				}
			}
		};
		renderer.setDefaultToolTipGenerator(tooltipGenerator);
		renderer.setSeriesFillPaint(0, Color.white);
		renderer.setDefaultOutlinePaint(Color.black);

		// Set plot parameters
		CategoryPlot plot = new CategoryPlot(slidingDataset, xAxis, yAxis, renderer);
		plot.setForegroundAlpha(0.8f);
		plot.setRangePannable(false);
		plot.setBackgroundPaint(Color.white);

		if (getLockYAxis().isSelected() || getLogYAxis().isSelected())
		{
			plot.getRangeAxis().setAutoRange(false);
			if (minValue < 0)
				minValue = 0;
			if (maxValue > 0)
				plot.getRangeAxis().setRange(new Range(minValue, maxValue));
			else
				plot.getRangeAxis().setAutoRange(true);
		}
		else
		{
			plot.getRangeAxis().setAutoRange(true);
		}

		// Set default legend items
		LegendItemCollection chartLegend = new LegendItemCollection();
		Shape shape = new Rectangle(10, 10);

		chartLegend.add(new LegendItem("10th Ranked Gene BMD", null, null, null,
				new Ellipse2D.Double(0, 0, 10, 10), Color.black));
		chartLegend.add(
				new LegendItem("25th Ranked Gene BMD", null, null, null, new Rectangle(10, 2), Color.black));
		chartLegend.add(new LegendItem("1th % Gene BMD", null, null, null, shape, Color.black));
		chartLegend.add(new LegendItem("5th % Gene BMD", null, null, null,
				ShapeCreator.createDiamond(10 * Math.sqrt(2), 10 * Math.sqrt(2)), Color.black));
		chartLegend.add(new LegendItem("10th % Gene BMD", null, null, null,
				ShapeCreator.createDiagonalCross(5, 2), Color.black));
		plot.setFixedLegendItems(chartLegend);

		// Create chart
		chart = new JFreeChart(key.getKey() + " Violins", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		chart.getPlot().setForegroundAlpha(.8f);
		plot.setOrientation(PlotOrientation.HORIZONTAL);
		setRange();

		// Create Panel
		SciomeChartViewer chartView = new SciomeChartViewer(chart);

		return chartView;
	}

	@Override
	public void reactToChattingCharts()
	{

	}

	@Override
	public void markData(Set<String> markings)
	{

	}

	@Override
	protected boolean isXAxisDefineable()
	{
		return false;
	}

	@Override
	protected boolean isYAxisDefineable()
	{
		return false;
	}

	@Override
	protected void redrawChart()
	{
		showChart();
	}

	protected void setSliders(double numValues)
	{
		Slider slider = new Slider(0, numValues - maxNodesShown, 0);
		slider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number oldValue, Number newValue)
			{
				slidingDataset.setFirstCategoryIndex(newValue.intValue());
				setRange();
			}
		});

		sethSlider(slider);
	}

	protected void setRange()
	{
		if (getLockYAxis().isSelected())
			return;

		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		ViolinCategoryDataset dataset = (ViolinCategoryDataset) slidingDataset.getUnderlyingDataset();
		for (int i = 0; i < getSeriesData().size(); i++)
		{
			int first = slidingDataset.getFirstCategoryIndex();
			for (int j = 0; j < maxNodesShown; j++)
			{
				ViolinItem item = null;
				try
				{
					item = dataset.getItem(i, first + j);
				}
				catch (Exception e)
				{
					continue;
				}
				if (item == null)
					continue;

				if (item.getMaxOutlier().doubleValue() > max)
					max = item.getMaxOutlier().doubleValue();
				if (item.getMinOutlier().doubleValue() < min)
					min = item.getMinOutlier().doubleValue();
			}
		}
		if (min < max)
			((CategoryPlot) chart.getPlot()).getRangeAxis().setRange(new Range(min, max));
	}

	private void showConfiguration()
	{
		Dialog<Boolean> dialog = new Dialog<>();
		dialog.setTitle("Chart Configuration");
		dialog.setResizable(true);
		dialog.initOwner(this.getScene().getWindow());
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.setResizable(false);
		TextField bandwidthTF = new TextField();
		bandwidthTF.setMaxWidth(100.0);
		TextField maxNodesTF = new TextField();
		maxNodesTF.setMaxWidth(100.0);

		if (bandwidth != null)
			bandwidthTF.setText(bandwidth.toString());
		maxNodesTF.setText(maxNodesShown.toString());

		HBox mainBox = new HBox();
		mainBox.setSpacing(20.0);

		VBox leftBox = new VBox();
		leftBox.setPrefWidth(200.0);
		leftBox.setSpacing(20.0);
		VBox rightBox = new VBox();
		rightBox.setSpacing(20.0);
		rightBox.setPrefWidth(200.0);

		HBox firstLeft = new HBox();
		firstLeft.setAlignment(Pos.CENTER_RIGHT);
		firstLeft.setSpacing(10.0);
		firstLeft.getChildren().addAll(new Label("Bandwidth"));

		HBox secondLeft = new HBox();
		secondLeft.setAlignment(Pos.CENTER_RIGHT);
		secondLeft.setSpacing(10.0);
		secondLeft.getChildren().addAll(new Label("Max Nodes Shown"));

		HBox firstRight = new HBox();
		firstRight.setAlignment(Pos.CENTER_LEFT);
		firstRight.setSpacing(10.0);
		firstRight.getChildren().addAll(bandwidthTF);

		HBox secondRight = new HBox();
		secondRight.setAlignment(Pos.CENTER_LEFT);
		secondRight.setSpacing(10.0);
		secondRight.getChildren().addAll(maxNodesTF);

		leftBox.getChildren().addAll(firstLeft, secondLeft);
		rightBox.getChildren().addAll(firstRight, secondRight);
		mainBox.getChildren().addAll(leftBox, rightBox);

		dialog.getDialogPane().setContent(mainBox);

		ButtonType buttonTypeOk = new ButtonType("Okay", ButtonData.OK_DONE);
		ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
		dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);

		dialog.setResultConverter(new Callback<ButtonType, Boolean>() {
			@Override
			public Boolean call(ButtonType b)
			{

				if (b == buttonTypeOk)
				{
					// If the user keeps the box empty, leave the value the same
					if (!bandwidthTF.getText().equals(""))
						bandwidth = Double.valueOf(bandwidthTF.getText());

					if (!maxNodesTF.getText().equals(""))
						maxNodesShown = Integer.valueOf(maxNodesTF.getText());

					convertChartDataPacksToSciomeSeries(new ChartKey[] { key }, getChartDataPacks());
					return true;
				}

				return false;
			}
		});

		dialog.getDialogPane().setPrefSize(400, 400);
		dialog.getDialogPane().autosize();
		Optional<Boolean> value = dialog.showAndWait();

		if (value.isPresent())
		{
			redrawCharts(getChartDataPacks());
		}
	}
}
