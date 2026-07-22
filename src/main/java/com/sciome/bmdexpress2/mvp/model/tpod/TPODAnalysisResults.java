package com.sciome.bmdexpress2.mvp.model.tpod;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.sciome.bmdexpress2.mvp.model.BMDExpressAnalysisDataSet;
import com.sciome.bmdexpress2.mvp.model.category.CategoryAnalysisResults;
import com.sciome.bmdexpress2.mvp.model.info.AnalysisInfo;

@JsonTypeInfo(use = Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@ref")
public class TPODAnalysisResults extends BMDExpressAnalysisDataSet implements Serializable
{

	private static final long serialVersionUID = 5135935005024600791L;
	private String name;
	private List<TPODAnalysisResult> tpodAnalysisResults;
	private AnalysisInfo analysisInfo;

	private CategoryAnalysisResults categoryAnalysisResults;

	private transient List<String> columnHeader;

	private Long id;

	public List<TPODAnalysisResult> getTpodAnalysisResults()
	{
		return tpodAnalysisResults;
	}

	public void setTpodAnalysisResults(List<TPODAnalysisResult> tpodAnalysisResults)
	{
		this.tpodAnalysisResults = tpodAnalysisResults;
	}

	public AnalysisInfo getAnalysisInfo()
	{
		return analysisInfo;
	}

	public void setAnalysisInfo(AnalysisInfo analysisInfo)
	{
		this.analysisInfo = analysisInfo;
	}

	public CategoryAnalysisResults getCategoryAnalysisResults()
	{
		return categoryAnalysisResults;
	}

	public void setCategoryAnalysisResults(CategoryAnalysisResults categoryAnalysisResults)
	{
		this.categoryAnalysisResults = categoryAnalysisResults;
	}

	private void fillColumnHeader()
	{
		// refresh the doseResponseExperiement so that any transient values are populated
		// the category results are using fold change values that are
		// gotten from the bmdresults. So they need to be avaiable here.
		if (categoryAnalysisResults != null)
			this.categoryAnalysisResults.getColumnHeader();
		columnHeader = new ArrayList<>();
		if (tpodAnalysisResults == null || tpodAnalysisResults.size() == 0)
		{
			return;
		}
		TPODAnalysisResult tpodResult = tpodAnalysisResults.get(0);

		columnHeader = tpodResult.generateColumnHeader();

	}

	@Override
	@JsonIgnore
	public List<String> getColumnHeader()
	{
		if (columnHeader == null || columnHeader.size() == 0)
		{
			fillColumnHeader();
			// refresh all the data rows so all transient properties are availabe
			for (TPODAnalysisResult result : this.tpodAnalysisResults)
				result.createRowData();
		}
		return columnHeader;
	}

	@Override
	public List<Object> getColumnHeader2()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AnalysisInfo> getAnalysisInfo(boolean getParents)
	{
		List<AnalysisInfo> list = new ArrayList<>();
		list.add(analysisInfo);

		if (getParents)
		{
			List<AnalysisInfo> parentList = categoryAnalysisResults.getAnalysisInfo(getParents);
			list.addAll(parentList);
		}

		return list;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void setName(String name)
	{
		this.name = name;

	}

	@Override
	public List getAnalysisRows()
	{
		return tpodAnalysisResults;
	}

	@Override
	public Object getObject()
	{
		return this;
	}

	@Override
	public String toString()
	{
		return name;
	}

}
