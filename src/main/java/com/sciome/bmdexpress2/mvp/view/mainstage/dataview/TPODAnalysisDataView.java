package com.sciome.bmdexpress2.mvp.view.mainstage.dataview;

import java.util.Map;
import java.util.Set;

import com.sciome.bmdexpress2.mvp.model.BMDExpressAnalysisDataSet;
import com.sciome.bmdexpress2.mvp.model.BMDExpressAnalysisRow;
import com.sciome.bmdexpress2.mvp.model.CombinedDataSet;
import com.sciome.bmdexpress2.mvp.model.tpod.TPODAnalysisResult;
import com.sciome.bmdexpress2.mvp.model.tpod.TPODAnalysisResults;
import com.sciome.bmdexpress2.mvp.presenter.mainstage.dataview.TPODAnalysisDataViewPresenter;
import com.sciome.bmdexpress2.mvp.view.visualization.DataVisualizationView;
import com.sciome.bmdexpress2.mvp.view.visualization.TPODAnalysisDataVisualizationView;
import com.sciome.bmdexpress2.mvp.viewinterface.mainstage.dataview.IBMDExpressDataView;
import com.sciome.bmdexpress2.shared.BMDExpressProperties;
import com.sciome.bmdexpress2.shared.eventbus.BMDExpressEventBus;

import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

public class TPODAnalysisDataView extends BMDExpressDataView<TPODAnalysisResults>
		implements IBMDExpressDataView
{

	private Callback<TableColumn, TableCell> tpodCellFactory;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public TPODAnalysisDataView(BMDExpressAnalysisDataSet tpodAnalysisResults, String viewTypeKey)
	{
		super(TPODAnalysisResult.class, tpodAnalysisResults, viewTypeKey);
		try
		{
			presenter = new TPODAnalysisDataViewPresenter(this, BMDExpressEventBus.getInstance());

			// Add any new columns to the map and list
			columnMap = BMDExpressProperties.getInstance().getTableInformation().getTpodAnalysisMap();
			columnOrder = BMDExpressProperties.getInstance().getTableInformation().getTpodAnalysisOrder();
			for (String header : tpodAnalysisResults.getColumnHeader())
			{
				if (!columnMap.containsKey(header))
				{
					columnMap.put(header, true);
				}
				if (!columnOrder.contains(header))
				{
					if (header.equals("Analysis"))
						columnOrder.add(0, header);
					else
						columnOrder.add(header);
				}
			}

			setUpTableView(tpodAnalysisResults);
			setUpTableListeners();
			if (tpodAnalysisResults.getColumnHeader().size() == 0)
				return;

			setCellFactory();

			presenter.showVisualizations(tpodAnalysisResults);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	protected void setCellFactory()
	{

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void close()
	{
		if (tableView != null && tableView.getColumns().size() > 0)
		{
			int pathwayColumn = 0;
			if (bmdAnalysisDataSet instanceof CombinedDataSet)
				pathwayColumn = 1;
			TableColumn tc = tableView.getColumns().get(pathwayColumn);
			tc.setCellFactory(null);
		}
		super.close();

	}

	@Override
	protected DataVisualizationView getDataVisualizationView()
	{
		return new TPODAnalysisDataVisualizationView();
	}

	private class CategoryTableMousEvent implements EventHandler<MouseEvent>
	{

		@Override
		public void handle(MouseEvent event)
		{
			if (event.getClickCount() != 1)
			{
				return;
			}
			TableCell c = (TableCell) event.getSource();
			BMDExpressAnalysisRow item = (BMDExpressAnalysisRow) c.getTableRow().getItem();

			if (item == null)
				return;

		}

	}

	private class TPODTableCallBack implements Callback<TableColumn, TableCell>
	{

		@Override
		public TableCell call(TableColumn param)
		{
			TableCell cell = new TableCell<BMDExpressAnalysisRow, Object>() {

				// must override drawing the cell so we can color it blue.
				@Override
				public void updateItem(Object item, boolean empty)
				{
					super.updateItem(item, empty);
					setTextFill(javafx.scene.paint.Color.BLUE);
					setText(empty ? null : getString());
					setGraphic(null);
				}

				private String getString()
				{
					return getItem() == null ? "" : getItem().toString();
				}
			};

			return cell;
		}

	}

	@Override
	protected Map<String, Map<String, Set<String>>> fillUpDBToPathwayGeneSymbols()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
