package com.sciome.bmdexpress2.mvp.model.tpod;

import java.util.Map;

public class TPODInputFilter
{

	Map<FilterCriterion, Number> filterMap;

	public Map<FilterCriterion, Number> getFilterMap()
	{
		return filterMap;
	}

	public void setFilterMap(Map<FilterCriterion, Number> filterMap)
	{
		this.filterMap = filterMap;
	}

}
