package com.sciome.bmdexpress2.mvp.model.info;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AnalysisInfo implements Serializable
{

	private static final long serialVersionUID = 6852936561184606211L;

	private List<String> notes;

	public AnalysisInfo()
	{

	}

	public AnalysisInfo(AnalysisInfo analysisInfo)
	{
		notes = new ArrayList<>();
		for (String note : analysisInfo.getNotes())
			notes.add(note);
	}

	public List<String> getNotes()
	{
		return notes;
	}

	public void setNotes(List<String> notes)
	{
		this.notes = notes;
	}

}
