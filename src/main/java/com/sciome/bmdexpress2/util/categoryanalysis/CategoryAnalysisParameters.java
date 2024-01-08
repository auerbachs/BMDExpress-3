package com.sciome.bmdexpress2.util.categoryanalysis;

import com.sciome.bmdexpress2.util.categoryanalysis.defined.DefinedCategoryFileParameters;

public class CategoryAnalysisParameters
{

	private DefinedCategoryFileParameters probeFileParameters;
	private DefinedCategoryFileParameters categoryFileParameters;

	private String goCat;
	private int goTermIdx;

	private String pathwayDB;

	private boolean removeBMDGreaterHighDose = false;
	private boolean removePromiscuousProbes = false;
	private boolean removeBMDPValueLessCuttoff = false;
	private boolean removeBMDBMDLRatio = false;
	private boolean removeNFoldBelowLowestDose = false;
	private boolean removeStepFunction = false;
	private boolean removeStepFunctionWithBMDLower = false;
	private boolean removeAdverseDirection = false;
	private String removeAdverseDirectionValue = "";
	private double bmdBmdlRatio;
	private double nFoldbelowLowestDoseValue;
	private double pValueCutoff;

	private boolean removeBMDUBMDLRatio = false;
	private double bmduBmdlRatio;

	private boolean removeBMDUBMDRatio = false;
	private double bmduBmdRatio;

	private boolean identifyConflictingProbeSets = false;
	private double correlationCutoffConflictingProbeSets;

	private boolean userFoldChangeFilter = false;
	private double maxFoldChange;

	private boolean userPValueFilter = false;
	private double pValue;

	private boolean userAdjustedPValueFilter = false;
	private double adjustedPValue;

	private double minDose;
	private double maxDose;
	private double minPositiveDose;

	private boolean deduplicateGeneSets = false;

	private boolean removeRSquared = false;
	private double rSquared;

	private boolean removeMinGenesInSet = false;
	private int minGenesInSet;

	private boolean removeMaxGenesInSet = false;
	private int maxGenesInSet;

	// IVIVE calculation
	private IVIVEParameters iviveParameters;

	public boolean isRemoveAdverseDirection()
	{
		return removeAdverseDirection;
	}

	public void setRemoveAdverseDirection(boolean removeAdverseDirection)
	{
		this.removeAdverseDirection = removeAdverseDirection;
	}

	public String getRemoveAdverseDirectionValue()
	{
		return removeAdverseDirectionValue;
	}

	public void setRemoveAdverseDirectionValue(String removeAdverseDirectionValue)
	{
		this.removeAdverseDirectionValue = removeAdverseDirectionValue;
	}

	public DefinedCategoryFileParameters getProbeFileParameters()
	{
		return probeFileParameters;
	}

	public boolean isRemoveStepFunction()
	{
		return removeStepFunction;
	}

	public void setRemoveStepFunction(boolean removeStepFunction)
	{
		this.removeStepFunction = removeStepFunction;
	}

	public boolean isRemoveStepFunctionWithBMDLower()
	{
		return removeStepFunctionWithBMDLower;
	}

	public void setRemoveStepFunctionWithBMDLower(boolean removeStepFunctionWithBMDLower)
	{
		this.removeStepFunctionWithBMDLower = removeStepFunctionWithBMDLower;
	}

	public boolean isRemoveMinGenesInSet()
	{
		return removeMinGenesInSet;
	}

	public void setRemoveMinGenesInSet(boolean removeMinGenesInSet)
	{
		this.removeMinGenesInSet = removeMinGenesInSet;
	}

	public int getMinGenesInSet()
	{
		return minGenesInSet;
	}

	public void setMinGenesInSet(int minGenesInSet)
	{
		this.minGenesInSet = minGenesInSet;
	}

	public boolean isRemoveMaxGenesInSet()
	{
		return removeMaxGenesInSet;
	}

	public void setRemoveMaxGenesInSet(boolean removeMaxGenesInSet)
	{
		this.removeMaxGenesInSet = removeMaxGenesInSet;
	}

	public int getMaxGenesInSet()
	{
		return maxGenesInSet;
	}

	public void setMaxGenesInSet(int maxGenesInSet)
	{
		this.maxGenesInSet = maxGenesInSet;
	}

	public boolean isRemoveRSquared()
	{
		return removeRSquared;
	}

	public void setRemoveRSquared(boolean removeRSquared)
	{
		this.removeRSquared = removeRSquared;
	}

	public double getrSquared()
	{
		return rSquared;
	}

	public void setrSquared(double rSquared)
	{
		this.rSquared = rSquared;
	}

	public void setProbeFileParameters(DefinedCategoryFileParameters probeFileParameters)
	{
		this.probeFileParameters = probeFileParameters;
	}

	public DefinedCategoryFileParameters getCategoryFileParameters()
	{
		return categoryFileParameters;
	}

	public void setCategoryFileParameters(DefinedCategoryFileParameters categoryFileParameters)
	{
		this.categoryFileParameters = categoryFileParameters;
	}

	public String getGoCat()
	{
		return goCat;
	}

	public void setGoCat(String goCat)
	{
		this.goCat = goCat;
	}

	public int getGoTermIdx()
	{
		return goTermIdx;
	}

	public void setGoTermIdx(int goTermIdx)
	{
		this.goTermIdx = goTermIdx;
	}

	public String getPathwayDB()
	{
		return pathwayDB;
	}

	public void setPathwayDB(String pathwayDB)
	{
		this.pathwayDB = pathwayDB;
	}

	public boolean isRemoveBMDGreaterHighDose()
	{
		return removeBMDGreaterHighDose;
	}

	public void setRemoveBMDGreaterHighDose(boolean removeBMDGreaterHighDose)
	{
		this.removeBMDGreaterHighDose = removeBMDGreaterHighDose;
	}

	public boolean isRemoveBMDPValueLessCuttoff()
	{
		return removeBMDPValueLessCuttoff;
	}

	public void setRemoveBMDPValueLessCuttoff(boolean removeBMDPValueLessCuttoff)
	{
		this.removeBMDPValueLessCuttoff = removeBMDPValueLessCuttoff;
	}

	public double getpValueCutoff()
	{
		return pValueCutoff;
	}

	public void setpValueCutoff(double pValueCutoff)
	{
		this.pValueCutoff = pValueCutoff;
	}

	public boolean isIdentifyConflictingProbeSets()
	{
		return identifyConflictingProbeSets;
	}

	public void setIdentifyConflictingProbeSets(boolean identifyConflictingProbeSets)
	{
		this.identifyConflictingProbeSets = identifyConflictingProbeSets;
	}

	public double getCorrelationCutoffConflictingProbeSets()
	{
		return correlationCutoffConflictingProbeSets;
	}

	public void setCorrelationCutoffConflictingProbeSets(double correlationCutoffConflictingProbeSets)
	{
		this.correlationCutoffConflictingProbeSets = correlationCutoffConflictingProbeSets;
	}

	public double getMinDose()
	{
		return minDose;
	}

	public void setMinDose(double minDose)
	{
		this.minDose = minDose;
	}

	public double getMaxDose()
	{
		return maxDose;
	}

	public void setMaxDose(double maxDose)
	{
		this.maxDose = maxDose;
	}

	public boolean isRemoveBMDBMDLRatio()
	{
		return removeBMDBMDLRatio;
	}

	public void setRemoveBMDBMDLRatio(boolean removeBMDBMDLRatio)
	{
		this.removeBMDBMDLRatio = removeBMDBMDLRatio;
	}

	public boolean isRemoveNFoldBelowLowestDose()
	{
		return removeNFoldBelowLowestDose;
	}

	public void setRemoveNFoldBelowLowestDose(boolean removeNFoldBelowLowestDose)
	{
		this.removeNFoldBelowLowestDose = removeNFoldBelowLowestDose;
	}

	public double getBmdBmdlRatio()
	{
		return bmdBmdlRatio;
	}

	public void setBmdBmdlRatio(double bmdBmdlRatio)
	{
		this.bmdBmdlRatio = bmdBmdlRatio;
	}

	public double getnFoldbelowLowestDoseValue()
	{
		return nFoldbelowLowestDoseValue;
	}

	public void setnFoldbelowLowestDoseValue(double nFoldbelowLowestDoseValue)
	{
		this.nFoldbelowLowestDoseValue = nFoldbelowLowestDoseValue;
	}

	public double getMinPositiveDose()
	{
		return minPositiveDose;
	}

	public void setMinPositiveDose(double minPositiveDose)
	{
		this.minPositiveDose = minPositiveDose;
	}

	public void setRemovePromiscuousProbes(boolean selected)
	{
		removePromiscuousProbes = selected;

	}

	public boolean getRemovePromiscuousProbes()
	{
		return removePromiscuousProbes;

	}

	public boolean isRemoveBMDUBMDLRatio()
	{
		return removeBMDUBMDLRatio;
	}

	public void setRemoveBMDUBMDLRatio(boolean removeBMDUBMDLRatio)
	{
		this.removeBMDUBMDLRatio = removeBMDUBMDLRatio;
	}

	public double getBmduBmdlRatio()
	{
		return bmduBmdlRatio;
	}

	public void setBmduBmdlRatio(double bmduBmdlRatio)
	{
		this.bmduBmdlRatio = bmduBmdlRatio;
	}

	public boolean isRemoveBMDUBMDRatio()
	{
		return removeBMDUBMDRatio;
	}

	public void setRemoveBMDUBMDRatio(boolean removeBMDUBMDRatio)
	{
		this.removeBMDUBMDRatio = removeBMDUBMDRatio;
	}

	public double getBmduBmdRatio()
	{
		return bmduBmdRatio;
	}

	public void setBmduBmdRatio(double bmduBmdRatio)
	{
		this.bmduBmdRatio = bmduBmdRatio;
	}

	public boolean isUserFoldChangeFilter()
	{
		return userFoldChangeFilter;
	}

	public void setUserFoldChangeFilter(boolean userFoldChangeFilter)
	{
		this.userFoldChangeFilter = userFoldChangeFilter;
	}

	public double getMaxFoldChange()
	{
		return maxFoldChange;
	}

	public void setMaxFoldChange(double maxFoldChange)
	{
		this.maxFoldChange = maxFoldChange;
	}

	public boolean isUserPValueFilter()
	{
		return userPValueFilter;
	}

	public void setUserPValueFilter(boolean userPValueFilter)
	{
		this.userPValueFilter = userPValueFilter;
	}

	public double getPValue()
	{
		return pValue;
	}

	public void setPValue(double pValue)
	{
		this.pValue = pValue;
	}

	public boolean isUserAdjustedPValueFilter()
	{
		return userAdjustedPValueFilter;
	}

	public void setUserAdjustedPValueFilter(boolean userAdjustedPValueFilter)
	{
		this.userAdjustedPValueFilter = userAdjustedPValueFilter;
	}

	public double getAdjustedPValue()
	{
		return adjustedPValue;
	}

	public void setAdjustedPValue(double adjustedPValue)
	{
		this.adjustedPValue = adjustedPValue;
	}

	public void setDeduplicateGeneSets(boolean selected)
	{
		deduplicateGeneSets = selected;

	}

	public boolean getDeduplicateGeneSets()
	{
		return deduplicateGeneSets;

	}

	public IVIVEParameters getIviveParameters()
	{
		return iviveParameters;
	}

	public void setIviveParameters(IVIVEParameters iviveParameters)
	{
		this.iviveParameters = iviveParameters;
	}
}
