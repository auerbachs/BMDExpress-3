package com.sciome.bmdexpress2.mvp.model.category;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CategoryAnalysisInputToShowOrHide
{

	private final StringProperty name = new SimpleStringProperty();
	private final BooleanProperty showMe = new SimpleBooleanProperty(false);

	public String getName()
	{
		return name.get();
	}

	public void setName(String name)
	{
		this.name.set(name);
	}

	public StringProperty nameProperty()
	{
		return name;
	}

	public boolean isShowMe()
	{
		return showMe.get();
	}

	public void setShowMe(boolean showMe)
	{
		this.showMe.set(showMe);
	}

	public BooleanProperty showMeProperty()
	{
		return showMe;
	}

	@Override
	public String toString()
	{
		return getName();
	}

}
