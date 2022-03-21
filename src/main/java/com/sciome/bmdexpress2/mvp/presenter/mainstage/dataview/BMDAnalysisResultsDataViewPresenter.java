package com.sciome.bmdexpress2.mvp.presenter.mainstage.dataview;

import com.sciome.bmdexpress2.mvp.viewinterface.mainstage.dataview.IBMDExpressDataView;
import com.sciome.bmdexpress2.service.ProjectNavigationService;
import com.sciome.bmdexpress2.shared.eventbus.BMDExpressEventBus;

public class BMDAnalysisResultsDataViewPresenter extends BMDExpressDataViewPresenter<IBMDExpressDataView>
{

	public BMDAnalysisResultsDataViewPresenter(IBMDExpressDataView view, BMDExpressEventBus eventBus)
	{
		super(view, new ProjectNavigationService(), eventBus);
	}
}
