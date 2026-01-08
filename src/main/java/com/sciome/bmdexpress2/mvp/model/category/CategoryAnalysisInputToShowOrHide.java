package com.sciome.bmdexpress2.mvp.model.category;

public class CategoryAnalysisInputToShowOrHide
{

	private String name;
	private boolean showMe = false;

	public CategoryAnalysisInputToShowOrHide()
	{
		// TODO Auto-generated constructor stub
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public boolean isShowMe()
	{
		return showMe;
	}

	public void setShowMe(boolean showMe)
	{
		this.showMe = showMe;
	}

	@Override
	public String toString()
	{
		return name;
	}

}
