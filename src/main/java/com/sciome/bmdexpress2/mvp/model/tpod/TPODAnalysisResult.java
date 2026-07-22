package com.sciome.bmdexpress2.mvp.model.tpod;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sciome.bmdexpress2.mvp.model.BMDExpressAnalysisRow;

public class TPODAnalysisResult extends BMDExpressAnalysisRow implements Serializable
{

	private TPODMethod tpodMethod;
	private BMDEndpointType bmdEndpointType;
	private Double bmd;
	private Double bmdl;
	private Double bmdu;

	// row data for the table view.
	protected transient List<Object> row;

	// converting the object data to row data will require lots of string buffers.
	// let them all use the same object to reduce instantiation
	private transient StringBuffer stringBuffer = new StringBuffer();

	public TPODMethod getTpodMethod()
	{
		return tpodMethod;
	}

	public void setTpodMethod(TPODMethod tpodMethod)
	{
		this.tpodMethod = tpodMethod;
	}

	public BMDEndpointType getBmdEndpointType()
	{
		return bmdEndpointType;
	}

	public void setBmdEndpointType(BMDEndpointType bmdEndpointType)
	{
		this.bmdEndpointType = bmdEndpointType;
	}

	public Double getBmd()
	{
		return bmd;
	}

	public void setBmd(Double bmd)
	{
		this.bmd = bmd;
	}

	public Double getBmdl()
	{
		return bmdl;
	}

	public void setBmdl(Double bmdl)
	{
		this.bmdl = bmdl;
	}

	public Double getBmdu()
	{
		return bmdu;
	}

	public void setBmdu(Double bmdu)
	{
		this.bmdu = bmdu;
	}

	@Override
	public Object getObject()
	{
		return this;
	}

	@Override
	@JsonIgnore
	public List<Object> getRow()
	{
		if (row == null || row.size() == 0)
			createRowData();
		return row;
	}

	protected void createRowData()
	{
		stringBuffer = getStringBuffer();
		if (row != null)
			return;

		row = new ArrayList<>();
		row.add(tpodMethod);
		row.add(bmdEndpointType);
		row.add(bmd);
		row.add(bmdl);
		row.add(bmdu);

	}

	private StringBuffer getStringBuffer()
	{
		if (this.stringBuffer == null)
			stringBuffer = new StringBuffer();

		return stringBuffer;
	}

	public List<String> generateColumnHeader()
	{
		List<String> header = new ArrayList<>();

		header.add("TPOD Method");
		header.add("BMD Endpoint Type");
		header.add("BMD");
		header.add("BMDL");
		header.add("BMDU");
		return header;
	}

}
