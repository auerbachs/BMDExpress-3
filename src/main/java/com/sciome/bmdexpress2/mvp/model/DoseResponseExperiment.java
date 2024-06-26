package com.sciome.bmdexpress2.mvp.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.sciome.bmdexpress2.mvp.model.chip.ChipInfo;
import com.sciome.bmdexpress2.mvp.model.info.AnalysisInfo;
import com.sciome.bmdexpress2.mvp.model.probe.ProbeResponse;
import com.sciome.bmdexpress2.mvp.model.probe.Treatment;
import com.sciome.bmdexpress2.mvp.model.refgene.ReferenceGeneAnnotation;

@JsonTypeInfo(use = Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@ref")
public class DoseResponseExperiment extends BMDExpressAnalysisDataSet
		implements Serializable, IStatModelProcessable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6106646178862193241L;
	private String name;

	// this will contain doses.
	private List<Treatment> treatments;

	// this is your dose response matrix
	private List<ProbeResponse> probeResponses;
	private List<ReferenceGeneAnnotation> referenceGeneAnnotations;
	private ChipInfo chip;
	private Long chipCreationDate;
	private AnalysisInfo analysisInfo;

	// default to logTransformation of base2
	// this defines how the data was log transformed before being input into bmdexpress
	// this information is important to know for correctly calculating the fold change
	private LogTransformationEnum logTransformation = LogTransformationEnum.BASE2;

	private transient List<String> columnHeader;
	private transient List<Object> columnHeader2;
	private Long id;

	private transient List<DoseGroup> doseGroups;

	public static final String EXPRESSION_VALUES = "Expression Value";

	@JsonIgnore
	public Long getID()
	{
		return id;
	}

	public void setID(Long id)
	{
		this.id = id;
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

	public List<Treatment> getTreatments()
	{
		return treatments;
	}

	public void setTreatments(List<Treatment> treatments)
	{
		this.treatments = treatments;
	}

	public List<ProbeResponse> getProbeResponses()
	{
		return probeResponses;
	}

	public void setProbeResponses(List<ProbeResponse> probeResponses)
	{
		this.probeResponses = probeResponses;
	}

	public List<ReferenceGeneAnnotation> getReferenceGeneAnnotations()
	{
		return referenceGeneAnnotations;
	}

	public void setReferenceGeneAnnotations(List<ReferenceGeneAnnotation> referenceGeneAnnotations)
	{
		this.referenceGeneAnnotations = referenceGeneAnnotations;
	}

	public ChipInfo getChip()
	{
		return chip;
	}

	public void setChip(ChipInfo chip)
	{
		this.chip = chip;
	}

	public Long getChipCreationDate()
	{
		return chipCreationDate;
	}

	public void setChipCreationDate(Long chipCreationDate)
	{
		this.chipCreationDate = chipCreationDate;
	}

	@Override
	public List<AnalysisInfo> getAnalysisInfo(boolean getParent)
	{
		List<AnalysisInfo> list = new ArrayList<>();
		list.add(analysisInfo);
		return list;
	}

	public void setAnalysisInfo(AnalysisInfo analysisInfo)
	{
		this.analysisInfo = analysisInfo;
	}

	@Override
	public LogTransformationEnum getLogTransformation()
	{
		return logTransformation;
	}

	public void setLogTransformation(LogTransformationEnum logTransformation)
	{
		this.logTransformation = logTransformation;
	}

	@Override
	public String toString()
	{
		return name;
	}

	@JsonIgnore
	@Override
	public DoseResponseExperiment getProcessableDoseResponseExperiment()
	{
		return this;
	}

	@JsonIgnore
	@Override
	public List<ProbeResponse> getProcessableProbeResponses()
	{
		return this.probeResponses;
	}

	@JsonIgnore
	@Override
	public String getParentDataSetName()
	{
		return null;
	}

	/*
	 * treatments are known to be sorted low to high when stored
	 */
	@JsonIgnore
	public Double getMinDose()
	{
		if (treatments != null && treatments.size() > 0)
			return treatments.get(0).getDose().doubleValue();

		return null;
	}

	@JsonIgnore
	public Double getMaxDose()
	{
		if (treatments != null && treatments.size() > 0)
			return treatments.get(treatments.size() - 1).getDose().doubleValue();

		return null;
	}

	@Override
	@JsonIgnore
	public List<String> getColumnHeader()
	{
		if (columnHeader == null)
		{
			columnHeader = new ArrayList<>();

			// add a blank because this goes over the probeset id
			columnHeader.add("");
			for (Treatment treatment : treatments)
			{
				columnHeader.add(treatment.getName());
			}
		}
		return columnHeader;
	}

	@Override
	@JsonIgnore
	public List getAnalysisRows()
	{
		return probeResponses;
	}

	@Override
	public List<Object> getColumnHeader2()
	{
		if (columnHeader2 == null)
		{
			columnHeader2 = new ArrayList<>();
			// add a blank because this goes over the probeset id
			columnHeader2.add("Probe ID");
			for (Treatment treatment : treatments)
			{
				columnHeader2.add(treatment.getDose());
			}
		}
		return columnHeader2;
	}

	/*
	 * loop through each treatment and collapse to form
	 * dosegroups. throughout the application dosegroups are used
	 * to calcuate various metrics and define the x points for
	 * dose response analysis.
	 */
	public List<DoseGroup> getDoseGroups()
	{
		if (doseGroups != null)
			return doseGroups;
		doseGroups = new ArrayList<>();
		Float prevDose = null;
		DoseGroup currDg = null;
		// create the dosegroups list so we can use it to store corresponding dose response values.
		for (Treatment t : getTreatments())
		{
			if (!t.getDose().equals(prevDose))
			{
				DoseGroup dg = new DoseGroup();
				dg.setDose(t.getDose().doubleValue());
				doseGroups.add(dg);
				dg.incrementCount();
				currDg = dg;
			}
			else
				currDg.incrementCount();

			prevDose = t.getDose();
		}

		int j = 0;

		return doseGroups;
	}

	/*
	 * get the dose groups while also calcualting the mean values for
	 * correpsonding values in each dose group
	 */
	public List<DoseGroup> getDoseGroups(List<Float> responses)
	{
		List<DoseGroup> doseGroups = getDoseGroups();

		int j = 0;
		if (responses != null)
		{
			for (DoseGroup dg : doseGroups)
			{
				double sum = 0;
				for (int i = 0; i < dg.getCount(); i++)
				{
					sum += responses.get(j).doubleValue();
					j++;

				}
				dg.setResponseMean(sum / dg.getCount());
			}
		}

		return doseGroups;
	}

	/*
	 * perform post deserialization logic
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();

		// logTransformation is a later addition. So if we deserialize this object
		// and it is null, default it to BASE2.
		if (this.logTransformation == null)
		{
			logTransformation = LogTransformationEnum.BASE2;
			analysisInfo.getNotes()
					.add("Logtransformation set to default of: " + LogTransformationEnum.BASE2);
		}

	}

	@Override
	public Object getObject()
	{
		return this;
	}

	@JsonIgnore
	@Override
	public String getDataSetName()
	{
		return getName();
	}

}
