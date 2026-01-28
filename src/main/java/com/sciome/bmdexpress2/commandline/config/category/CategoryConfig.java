package com.sciome.bmdexpress2.commandline.config.category;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use = Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@JsonSubTypes({ @Type(value = DefinedConfig.class, name = "defined"),
		@Type(value = GOConfig.class, name = "go"), @Type(value = PathwayConfig.class, name = "pathway"),
		@Type(value = GeneLevelConfig.class, name = "gene") })
public abstract class CategoryConfig
{
	// name of bmdanalysis to cateogorize
	private String inputName;

	// name of output for the analysis
	private String outputName;

	private Boolean removePromiscuousProbes = true;
	private Boolean removeBMDGreaterHighDose = true;

	private Double bmdPValueCutoff;
	private Double bmdRSquaredCutoff;
	private Double bmdBMDLRatioMin;
	private Double bmduBMDRatioMin;
	private Double bmduBMDLRatioMin;
	private Double nFoldBelowLowestDose;
	private Double maxFoldChange;

	private Double anovaPrefilterPValueMaxValue;
	private Double anovaPrefilterAdjustedPValueMaxValue;
	private Double williamsPrefilterPValueMaxValue;
	private Double williamsPrefilterAdjustedPValueMaxValue;

	private Double oriogenPrefilterPValueMaxValue;
	private Double oriogenPrefilterAdjustedPValueMaxValue;

	private Double curveFitPrefilterGoFMinValue;

	private Boolean identifyConflictingProbeSets;
	private Double correlationCutoffForConflictingProbeSets;

	private Integer minGenesInSet;
	private Integer maxGenesInSet;

	private Boolean deduplicateGeneSets = false;

	private Boolean computeIVIVE = false;
	private IVIVEConfig iviveConfig;

	private Boolean removeStepFunction;
	private Boolean removeStepFunctionWithBMDLower;
	private Integer removeAdverseDirection;
	private Double absZScoreMax;
	private Double absModelFoldChangeMax;

	public String getInputName()
	{
		return inputName;
	}

	public void setInputName(String inputName)
	{
		this.inputName = inputName;
	}

	public String getOutputName()
	{
		return outputName;
	}

	public void setOutputName(String outputName)
	{
		this.outputName = outputName;
	}

	public Boolean getRemoveStepFunction()
	{
		return removeStepFunction;
	}

	public void setRemoveStepFunction(Boolean removeStepFunction)
	{
		this.removeStepFunction = removeStepFunction;
	}

	public Boolean getRemoveStepFunctionWithBMDLower()
	{
		return removeStepFunctionWithBMDLower;
	}

	public void setRemoveStepFunctionWithBMDLower(Boolean removeStepFunctionWithBMDLower)
	{
		this.removeStepFunctionWithBMDLower = removeStepFunctionWithBMDLower;
	}

	public Boolean getRemovePromiscuousProbes()
	{
		return removePromiscuousProbes;
	}

	public void setRemovePromiscuousProbes(Boolean removePromiscuousProbes)
	{
		this.removePromiscuousProbes = removePromiscuousProbes;
	}

	public Boolean getRemoveBMDGreaterHighDose()
	{
		return removeBMDGreaterHighDose;
	}

	public void setRemoveBMDGreaterHighDose(Boolean removeBMDGreaterHighDose)
	{
		this.removeBMDGreaterHighDose = removeBMDGreaterHighDose;
	}

	public Double getBmdPValueCutoff()
	{
		return bmdPValueCutoff;
	}

	public void setBmdPValueCutoff(Double bmdPValueCutoff)
	{
		this.bmdPValueCutoff = bmdPValueCutoff;
	}

	public Double getBmdRSquaredCutoff()
	{
		return bmdRSquaredCutoff;
	}

	public void setBmdRSquaredCutoff(Double bmdRSquaredCutoff)
	{
		this.bmdRSquaredCutoff = bmdRSquaredCutoff;
	}

	public Double getBmdBMDLRatioMin()
	{
		return bmdBMDLRatioMin;
	}

	public void setBmdBMDLRatioMin(Double bmdBMDLRatioMin)
	{
		this.bmdBMDLRatioMin = bmdBMDLRatioMin;
	}

	public Double getBmduBMDRatioMin()
	{
		return bmduBMDRatioMin;
	}

	public void setBmduBMDRatioMin(Double bmduBMDRatioMin)
	{
		this.bmduBMDRatioMin = bmduBMDRatioMin;
	}

	public Double getBmduBMDLRatioMin()
	{
		return bmduBMDLRatioMin;
	}

	public void setBmduBMDLRatioMin(Double bmduBMDLRatioMin)
	{
		this.bmduBMDLRatioMin = bmduBMDLRatioMin;
	}

	public Double getnFoldBelowLowestDose()
	{
		return nFoldBelowLowestDose;
	}

	public void setnFoldBelowLowestDose(Double nFoldBelowLowestDose)
	{
		this.nFoldBelowLowestDose = nFoldBelowLowestDose;
	}

	public Double getMaxFoldChange()
	{
		return maxFoldChange;
	}

	public void setMaxFoldChange(Double maxFoldChange)
	{
		this.maxFoldChange = maxFoldChange;
	}

	public Double getAnovaPrefilterPValueMin()
	{
		return anovaPrefilterPValueMaxValue;
	}

	public void setAnovaPrefilterPValueMaxValue(Double anovaPrefilterPValue)
	{
		this.anovaPrefilterPValueMaxValue = anovaPrefilterPValue;
	}

	public Double getAnovaPrefilterAdjustedPValueMaxValue()
	{
		return anovaPrefilterAdjustedPValueMaxValue;
	}

	public void setAnovaPrefilterAdjustedPValueMaxValue(Double anovaPrefilterAdjustedPValue)
	{
		this.anovaPrefilterAdjustedPValueMaxValue = anovaPrefilterAdjustedPValue;
	}

	public Double getWilliamsPrefilterPValueMaxValue()
	{
		return williamsPrefilterPValueMaxValue;
	}

	public void setWilliamsPrefilterPValueMaxValue(Double williamsPrefilterPValue)
	{
		this.williamsPrefilterPValueMaxValue = williamsPrefilterPValue;
	}

	public Double getWilliamsPrefilterAdjustedPValueMaxValue()
	{
		return williamsPrefilterAdjustedPValueMaxValue;
	}

	public void setWilliamsPrefilterAdjustedPValueMaxValue(Double williamsPrefilterAdjustedPValue)
	{
		this.williamsPrefilterAdjustedPValueMaxValue = williamsPrefilterAdjustedPValue;
	}

	public Double getOriogenPrefilterPValueMaxValue()
	{
		return oriogenPrefilterPValueMaxValue;
	}

	public void setOriogenPrefilterPValueMaxValue(Double oriogenPrefilterPValue)
	{
		this.oriogenPrefilterPValueMaxValue = oriogenPrefilterPValue;
	}

	public Double getOriogenPrefilterAdjustedPValueMaxValue()
	{
		return oriogenPrefilterAdjustedPValueMaxValue;
	}

	public void setOriogenPrefilterAdjustedPValueMaxValue(Double oriogenPrefilterAdjustedPValue)
	{
		this.oriogenPrefilterAdjustedPValueMaxValue = oriogenPrefilterAdjustedPValue;
	}

	public Double getCurveFitPrefilterGoFMinValue()
	{
		return curveFitPrefilterGoFMinValue;
	}

	public void setCurveFitPrefilterGoFMinValue(Double curveFitPrefilterGoF)
	{
		this.curveFitPrefilterGoFMinValue = curveFitPrefilterGoF;
	}

	public Boolean getIdentifyConflictingProbeSets()
	{
		return identifyConflictingProbeSets;
	}

	public void setIdentifyConflictingProbeSets(Boolean identifyConflictingProbeSets)
	{
		this.identifyConflictingProbeSets = identifyConflictingProbeSets;
	}

	public Double getCorrelationCutoffForConflictingProbeSets()
	{
		return correlationCutoffForConflictingProbeSets;
	}

	public void setCorrelationCutoffForConflictingProbeSets(Double correlationCutoffForConflictingProbeSets)
	{
		this.correlationCutoffForConflictingProbeSets = correlationCutoffForConflictingProbeSets;
	}

	public Boolean getDeduplicateGeneSets()
	{
		return deduplicateGeneSets;
	}

	public void setDeduplicateGeneSets(Boolean deduplicateGeneSets)
	{
		this.deduplicateGeneSets = deduplicateGeneSets;
	}

	public Integer getMinGenesInSet()
	{
		return minGenesInSet;
	}

	public void setMinGenesInSet(Integer minGenesInSet)
	{
		this.minGenesInSet = minGenesInSet;
	}

	public Integer getMaxGenesInSet()
	{
		return maxGenesInSet;
	}

	public void setMaxGenesInSet(Integer maxGenesInSet)
	{
		this.maxGenesInSet = maxGenesInSet;
	}

	public Boolean getComputeIVIVE()
	{
		return computeIVIVE;
	}

	public void setComputeIVIVE(Boolean computeIVIVE)
	{
		this.computeIVIVE = computeIVIVE;
	}

	public IVIVEConfig getIviveConfig()
	{
		return iviveConfig;
	}

	public void setIviveConfig(IVIVEConfig iviveConfig)
	{
		this.iviveConfig = iviveConfig;
	}

	public Integer getRemoveAdverseDirection()
	{
		return removeAdverseDirection;
	}

	public void setRemoveAdverseDirection(Integer removeAdverseDirection)
	{
		this.removeAdverseDirection = removeAdverseDirection;
	}

	public Double getAbsZScoreMax()
	{
		return absZScoreMax;
	}

	public void setAbsZScoreMax(Double absZScoreMax)
	{
		this.absZScoreMax = absZScoreMax;
	}

	public Double getAbsModelFoldChangeMax()
	{
		return absModelFoldChangeMax;
	}

	public void setAbsModelFoldChangeMax(Double absModelFoldChangeMax)
	{
		this.absModelFoldChangeMax = absModelFoldChangeMax;
	}

}
