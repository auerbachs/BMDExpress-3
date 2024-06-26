package com.sciome.bmdexpress2.mvp.model.stat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.sciome.bmdexpress2.mvp.model.BMDExpressAnalysisDataSet;
import com.sciome.bmdexpress2.mvp.model.DoseResponseExperiment;
import com.sciome.bmdexpress2.mvp.model.IStatModelProcessable;
import com.sciome.bmdexpress2.mvp.model.LogTransformationEnum;
import com.sciome.bmdexpress2.mvp.model.info.AnalysisInfo;
import com.sciome.bmdexpress2.mvp.model.prefilter.PrefilterResult;
import com.sciome.bmdexpress2.mvp.model.prefilter.PrefilterResults;
import com.sciome.bmdexpress2.mvp.model.probe.ProbeResponse;
import com.sciome.bmdexpress2.mvp.model.refgene.ReferenceGeneAnnotation;
import com.sciome.bmdexpress2.util.bmds.BMD_METHOD;
import com.sciome.bmdexpress2.util.prefilter.FoldChange;

@JsonTypeInfo(use = Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@ref")
public class BMDResult extends BMDExpressAnalysisDataSet implements Serializable, IStatModelProcessable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4821688005886618518L;

	private String name;
	private List<ProbeStatResult> probeStatResults;

	private DoseResponseExperiment doseResponseExperiment;
	private AnalysisInfo analysisInfo;
	private BMD_METHOD bmdMethod;

	private PrefilterResults prefilterResults;

	private List<Float> wAUCList;
	private List<Float> logwAUCList;

	private transient List<String> columnHeader;

	private Long id;

	/* define chartabble key values */
	public static final String BMD = "Best BMD";
	public static final String BMDL = "Best BMDL";
	public static final String BMDU = "Best BMDU";
	public static final String BMD_BMDL_RATIO = "Best BMD/BMDL";
	public static final String BMDU_BMDL_RATIO = "Best BMDU/BMDL";
	public static final String BMDU_BMD_RATIO = "Best BMDU/BMD";
	public static final String PREFILTER_PVALUE = "Prefilter P-Value";
	public static final String PREFILTER_ADJUSTEDPVALUE = "Prefilter Adjusted P-Value";
	public static final String BEST_FOLDCHANGE = "Max Fold Change";
	public static final String BEST_ABSFOLDCHANGE = "Max Fold Change Absolute Value";
	public static final String PROBE_ID = "Probe ID";
	public static final String GENE_IDS = "Entrez Gene IDs";
	public static final String GENE_SYMBOLS = "Genes Symbols";
	public static final String BEST_MODEL = "Best Model";
	public static final String BEST_BMD = "Best BMD";
	public static final String BEST_BMDL = "Best BMDL";
	public static final String BEST_BMDU = "Best BMDU";
	public static final String BEST_FITPVALUE = "Best fitPValue";
	public static final String BEST_LOGLIKLIHOOD = "Best fitLogLikelihood";
	public static final String BEST_AIC = "Best AIC";
	public static final String BEST_ADVERSE_DIRECTION = "Best adverseDirection";
	public static final String BEST_BMD_BMDL_RATIO = "Best BMD/BMDL";
	public static final String BEST_BMDU_BMDL_RATIO = "Best BMDU/BMDL";
	public static final String BEST_BMDU_BMD_RATIO = "Best BMDU/BMD";
	public static final String BEST_RSQUARED = "Best RSquared";

	public static final String BEST_ISSTEPFUNCTION = "Best Is Step Function";
	public static final String BEST_ISSTEPFUNCTION_WITH_BMD_LESS_THAN_LOWEST = "Best Is Step Function Less Than Lowest Dose";
	public static final String BEST_POLY = "Best Poly";
	public static final String WAUC = "wAUC";
	public static final String LOG_WAUC = "Log 2 wAUC";
	public static final String LOEL_VALUE = "LOTEL";
	public static final String NOEL_VALUE = "NOTEL";

	// clone a bmdexpress result
	public BMDResult(BMDResult bmdResult)
	{
		this.setName(bmdResult.getName());
		this.setDoseResponseExperiment(bmdResult.getDoseResponseExperiment());
		this.setLogwAUC(bmdResult.getLogwAUC());
		this.setwAUC(bmdResult.getwAUC());
		if (bmdResult.getAnalysisInfo(false) != null && bmdResult.getAnalysisInfo(false).size() > 0)
			this.setAnalysisInfo(new AnalysisInfo(bmdResult.getAnalysisInfo(false).get(0)));

		this.setPrefilterResults(bmdResult.getPrefilterResults());
		probeStatResults = new ArrayList<>();
		for (ProbeStatResult probeStatResult : bmdResult.getProbeStatResults())
		{
			// clone probestatResults
			probeStatResults.add(new ProbeStatResult(probeStatResult));
		}

	}

	public BMDResult()
	{
		// TODO Auto-generated constructor stub
	}

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

	public List<ProbeStatResult> getProbeStatResults()
	{
		return probeStatResults;
	}

	public void setProbeStatResults(List<ProbeStatResult> probeStatResults)
	{
		this.probeStatResults = probeStatResults;
	}

	public DoseResponseExperiment getDoseResponseExperiment()
	{
		return doseResponseExperiment;
	}

	public void setDoseResponseExperiment(DoseResponseExperiment doseResponseExperiment)
	{
		this.doseResponseExperiment = doseResponseExperiment;
	}

	public PrefilterResults getPrefilterResults()
	{
		return prefilterResults;
	}

	public void setPrefilterResults(PrefilterResults prefilterResults)
	{
		this.prefilterResults = prefilterResults;
	}

	public List<Float> getwAUC()
	{
		return wAUCList;
	}

	public void setwAUC(List<Float> wAUC)
	{
		this.wAUCList = wAUC;
	}

	public List<Float> getLogwAUC()
	{
		return logwAUCList;
	}

	public void setLogwAUC(List<Float> logwAUCList)
	{
		this.logwAUCList = logwAUCList;
	}

	public BMD_METHOD getBmdMethod()
	{
		if (bmdMethod == null)
			return BMD_METHOD.ORIGINAL;
		return bmdMethod;
	}

	public void setBmdMethod(BMD_METHOD bmdMethod)
	{

		this.bmdMethod = bmdMethod;
	}

	/*
	 * fill the column header for table display or file export purposes.
	 */
	private void fillColumnHeader()
	{
		columnHeader = new ArrayList<>();
		if (probeStatResults == null || probeStatResults.size() == 0)
		{
			return;
		}
		ProbeStatResult probStatResult = probeStatResults.get(0);

		columnHeader = probStatResult.generateColumnHeader();

		// Add Curve P Header
		if (this.getwAUC() != null)
			columnHeader.add(WAUC);
		// Commenting out for now
		// columnHeader.add(LOG_WAUC);
		columnHeader.add(PREFILTER_PVALUE);
		columnHeader.add(PREFILTER_ADJUSTEDPVALUE);
		columnHeader.add(BEST_FOLDCHANGE);
		columnHeader.add(BEST_ABSFOLDCHANGE);

		columnHeader.add(NOEL_VALUE);
		columnHeader.add(LOEL_VALUE);

		// now we want to add the columns for all the
		// individual fold change values.
		if (this.prefilterResults != null && this.prefilterResults.getPrefilterResults() != null
				&& this.prefilterResults.getPrefilterResults().size() > 0
				&& this.prefilterResults.getPrefilterResults().get(0) != null)
		{
			int i = 1;
			for (Float foldChange : this.prefilterResults.getPrefilterResults().get(0).getFoldChanges())
			{
				columnHeader.add("FC Dose Level " + i);
				i++;
			}
		}

	}

	@Override
	@JsonIgnore
	public List<String> getColumnHeader()
	{
		if (columnHeader == null || columnHeader.size() == 0)
			fillTableData();
		return columnHeader;
	}

	@Override
	public List<AnalysisInfo> getAnalysisInfo(boolean getParents)
	{
		List<AnalysisInfo> list = new ArrayList<>();
		list.add(analysisInfo);

		if (getParents)
		{

			List<AnalysisInfo> parentList = new ArrayList<>();
			if (prefilterResults != null)
			{
				parentList.addAll(prefilterResults.getPrefilterAnalysisInfo(getParents));
			}
			else if (doseResponseExperiment != null)
			{
				parentList.addAll(doseResponseExperiment.getAnalysisInfo(getParents));
			}
			list.addAll(parentList);
		}

		return list;
	}

	public void setAnalysisInfo(AnalysisInfo analysisInfo)
	{
		this.analysisInfo = analysisInfo;
	}

	// This is called in order to generate data for each probe stat result fo viewing
	// data in a table or exporting it.
	private void fillRowData()
	{

		// if there is a prefilter associate with this, then fill up this map
		// so the bmdresults can be associated with prefilter results
		// so that fold change/p-value/adjusted p-value can be shown in the grid.
		Map<String, PrefilterResult> probeToPrefilterMap = new HashMap<>();

		if (this.prefilterResults != null && prefilterResults.getPrefilterResults() != null)
			for (PrefilterResult prefilterResult : prefilterResults.getPrefilterResults())
				probeToPrefilterMap.put(prefilterResult.getProbeID(), prefilterResult);

		Map<String, ReferenceGeneAnnotation> probeToGeneMap = new HashMap<>();

		if (this.doseResponseExperiment.getReferenceGeneAnnotations() != null)
		{
			for (ReferenceGeneAnnotation refGeneAnnotation : this.doseResponseExperiment
					.getReferenceGeneAnnotations())
			{
				probeToGeneMap.put(refGeneAnnotation.getProbe().getId(), refGeneAnnotation);
			}
		}
		int index = 0;
		for (ProbeStatResult probeStatResult : probeStatResults)
		{
			Double adjustedPValue = null;
			Double pValue = null;
			Double bestFoldChange = null;
			List<Float> foldChanges = new ArrayList<>();
			Float loel = null;
			Float noel = null;

			PrefilterResult prefilter = probeToPrefilterMap
					.get(probeStatResult.getProbeResponse().getProbe().getId());

			// if the prefilter is not null, then add the prefilter metrics to allow user
			// to more easily sort and view the bmdresults
			if (prefilter != null)
			{
				adjustedPValue = prefilter.getAdjustedPValue();
				pValue = prefilter.getpValue();
				bestFoldChange = prefilter.getBestFoldChange().doubleValue();
				foldChanges = prefilter.getFoldChanges();
				loel = prefilter.getLoelDose();
				noel = prefilter.getNoelDose();
			}

			// if we are working of an earlier version of, let's go ahead and calcualte fold change data.
			// it's not too expensive.
			if (bestFoldChange == null)
			{
				FoldChange fc = null;
				if (doseResponseExperiment.getLogTransformation().equals(LogTransformationEnum.BASE10))
					fc = new FoldChange(doseResponseExperiment.getTreatments(), true, 10.0);
				else if (doseResponseExperiment.getLogTransformation().equals(LogTransformationEnum.BASE2))
					fc = new FoldChange(doseResponseExperiment.getTreatments(), true, 2.0);
				else if (doseResponseExperiment.getLogTransformation().equals(LogTransformationEnum.NATURAL))
					fc = new FoldChange(doseResponseExperiment.getTreatments(), true, Math.E);
				else
					fc = new FoldChange(doseResponseExperiment.getTreatments(), false, 10.0);

				bestFoldChange = fc.getBestFoldChangeValue(probeStatResult.getProbeResponse().getResponses())
						.doubleValue();
				foldChanges = fc.getFoldChanges();

				if (index == 0)
				{
					// at this point, the header doesn't know about individual fold changes
					// so we can add it here to the header row
					int i = 1;
					for (Float foldChange : foldChanges)
					{
						columnHeader.add("FC Dose Level " + i);
						i++;
					}
				}
			}

			Float wAUC = null;
			if (wAUCList != null)
				wAUC = wAUCList.get(index);

			// Comment out for now
			// Float logwAUC = null;
			// if(logwAUCList != null)
			// logwAUC = logwAUCList.get(index);

			probeStatResult.createRowData(probeToGeneMap, adjustedPValue, pValue, bestFoldChange, foldChanges,
					loel, noel, wAUC);
			index++;

		}

	}

	@Override
	public String toString()
	{
		return name;
	}

	private void fillTableData()
	{
		if (columnHeader == null)
		{
			for (ProbeStatResult probeStatResult : probeStatResults)
				probeStatResult.refreshRowData();
			fillColumnHeader();
			fillRowData();
		}

	}

	@Override
	@JsonIgnore
	public DoseResponseExperiment getProcessableDoseResponseExperiment()
	{
		return doseResponseExperiment;
	}

	@Override
	@JsonIgnore
	public List<ProbeResponse> getProcessableProbeResponses()
	{

		List<ProbeResponse> probeResponse = new ArrayList<>();
		for (ProbeStatResult probeStatResult : this.probeStatResults)
		{
			probeResponse.add(probeStatResult.getProbeResponse());
		}

		return probeResponse;
	}

	@Override
	@JsonIgnore
	public String getParentDataSetName()
	{
		return doseResponseExperiment.getName();
	}

	@SuppressWarnings("unchecked")
	@Override
	@JsonIgnore
	public List getAnalysisRows()
	{
		return probeStatResults;
	}

	@Override
	@JsonIgnore
	public List<Object> getColumnHeader2()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@JsonIgnore
	public LogTransformationEnum getLogTransformation()
	{
		return this.getDoseResponseExperiment().getLogTransformation();
	}

	@JsonIgnore
	@Override
	public Object getObject()
	{
		return this;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BMDResult other = (BMDResult) obj;
		if (name == null)
		{
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		return true;
	}

	@JsonIgnore
	@Override
	public String getDataSetName()
	{
		return getName();
	}

}
