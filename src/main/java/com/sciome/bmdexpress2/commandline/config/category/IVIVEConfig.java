package com.sciome.bmdexpress2.commandline.config.category;

import java.util.ArrayList;

import com.sciome.bmdexpress2.util.categoryanalysis.IVIVEParameters.ConcentrationUnits;
import com.sciome.commons.math.httk.calc.calc_analytic_css.Units;

public class IVIVEConfig
{
	private Boolean						inVivo;
	private Boolean						oneCompartment;
	private Boolean						pbtk;
	private Boolean						threeCompartment;
	private Boolean						threeCompartmentSS;

	// Auto-populate fields
	private Boolean						useAutoPopulate;
	private String						compoundName;
	private String						compoundCASRN;
	private String						compoundSMILES;
	private String						species;

	private Double						mw;
	private Double						logP;
	private ArrayList<Double>			pkaDonor;
	private ArrayList<Double>			pkaAcceptor;
	private Double						CLint;
	private Double						fractionUnboundPlasma;
	private Double						quantile;
	private Double						doseSpacing;
	private Double						finalTime;

	private ConcentrationUnits			concentrationUnits;
	private Units						doseUnits;

	public Boolean getOneCompartment()
	{
		return oneCompartment;
	}

	public void setOneCompartment(Boolean oneCompartment)
	{
		this.oneCompartment = oneCompartment;
	}

	public Boolean getPbtk()
	{
		return pbtk;
	}

	public void setPbtk(Boolean pbtk)
	{
		this.pbtk = pbtk;
	}

	public Boolean getThreeCompartment()
	{
		return threeCompartment;
	}

	public void setThreeCompartment(Boolean threeCompartment)
	{
		this.threeCompartment = threeCompartment;
	}

	public Boolean getThreeCompartmentSS()
	{
		return threeCompartmentSS;
	}

	public void setThreeCompartmentSS(Boolean threeCompartmentSS)
	{
		this.threeCompartmentSS = threeCompartmentSS;
	}

	public Boolean getUseAutoPopulate()
	{
		return useAutoPopulate;
	}

	public void setUseAutoPopulate(Boolean useAutoPopulate)
	{
		this.useAutoPopulate = useAutoPopulate;
	}

	public String getCompoundName()
	{
		return compoundName;
	}

	public void setCompoundName(String compoundName)
	{
		this.compoundName = compoundName;
	}

	public String getCompoundCASRN()
	{
		return compoundCASRN;
	}

	public void setCompoundCASRN(String compoundCASRN)
	{
		this.compoundCASRN = compoundCASRN;
	}

	public String getCompoundSMILES()
	{
		return compoundSMILES;
	}

	public void setCompoundSMILES(String compoundSMILES)
	{
		this.compoundSMILES = compoundSMILES;
	}

	public Double getMw()
	{
		return mw;
	}

	public void setMw(Double mw)
	{
		this.mw = mw;
	}

	public Double getLogP()
	{
		return logP;
	}

	public void setLogP(Double logP)
	{
		this.logP = logP;
	}

	public ArrayList<Double> getPkaDonor()
	{
		return pkaDonor;
	}

	public void setPkaDonor(ArrayList<Double> pkaDonor)
	{
		this.pkaDonor = pkaDonor;
	}

	public ArrayList<Double> getPkaAcceptor()
	{
		return pkaAcceptor;
	}

	public void setPkaAcceptor(ArrayList<Double> pkaAcceptor)
	{
		this.pkaAcceptor = pkaAcceptor;
	}

	public Double getCLint()
	{
		return CLint;
	}

	public void setCLint(Double cLint)
	{
		CLint = cLint;
	}

	public Double getFractionUnboundPlasma()
	{
		return fractionUnboundPlasma;
	}

	public void setFractionUnboundPlasma(Double fractionUnboundPlamsa)
	{
		this.fractionUnboundPlasma = fractionUnboundPlamsa;
	}

	public String getSpecies()
	{
		return species;
	}

	public void setSpecies(String species)
	{
		this.species = species;
	}

	public Double getQuantile()
	{
		return quantile;
	}

	public void setQuantile(Double quantile)
	{
		this.quantile = quantile;
	}

	public ConcentrationUnits getConcentrationUnits() {
		return concentrationUnits;
	}

	public void setConcentrationUnits(ConcentrationUnits concentrationUnits) {
		this.concentrationUnits = concentrationUnits;
	}

	public Units getDoseUnits() {
		return doseUnits;
	}

	public void setDoseUnits(Units doseUnits) {
		this.doseUnits = doseUnits;
	}

	public Boolean getInVivo() {
		return inVivo;
	}

	public void setInVivo(Boolean inVivo) {
		this.inVivo = inVivo;
	}

	public Double getDoseSpacing() {
		return doseSpacing;
	}

	public void setDoseSpacing(Double doseSpacing) {
		this.doseSpacing = doseSpacing;
	}

	public Double getFinalTime() {
		return finalTime;
	}

	public void setFinalTime(Double finalTime) {
		this.finalTime = finalTime;
	}
}
