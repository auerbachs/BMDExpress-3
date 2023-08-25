package com.sciome.bmdexpress2.commandline;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sciome.bmdexpress2.commandline.config.RunConfig;
import com.sciome.bmdexpress2.commandline.config.bmds.BMDSConfig;
import com.sciome.bmdexpress2.commandline.config.bmds.BMDSModelConfig;
import com.sciome.bmdexpress2.commandline.config.bmds.ExponentialConfig;
import com.sciome.bmdexpress2.commandline.config.bmds.HillConfig;
import com.sciome.bmdexpress2.commandline.config.bmds.ModelAveragingConfig;
import com.sciome.bmdexpress2.commandline.config.bmds.PolyConfig;
import com.sciome.bmdexpress2.commandline.config.bmds.PowerConfig;
import com.sciome.bmdexpress2.commandline.config.category.CategoryConfig;
import com.sciome.bmdexpress2.commandline.config.category.DefinedConfig;
import com.sciome.bmdexpress2.commandline.config.category.GOConfig;
import com.sciome.bmdexpress2.commandline.config.category.GeneLevelConfig;
import com.sciome.bmdexpress2.commandline.config.category.IVIVEConfig;
import com.sciome.bmdexpress2.commandline.config.category.PathwayConfig;
import com.sciome.bmdexpress2.commandline.config.expression.ExpressionDataConfig;
import com.sciome.bmdexpress2.commandline.config.nonparametric.NonParametricConfig;
import com.sciome.bmdexpress2.commandline.config.prefilter.ANOVAConfig;
import com.sciome.bmdexpress2.commandline.config.prefilter.CurveFitPrefilterConfig;
import com.sciome.bmdexpress2.commandline.config.prefilter.OriogenConfig;
import com.sciome.bmdexpress2.commandline.config.prefilter.PrefilterConfig;
import com.sciome.bmdexpress2.commandline.config.prefilter.WilliamsConfig;
import com.sciome.bmdexpress2.mvp.model.BMDProject;
import com.sciome.bmdexpress2.mvp.model.DoseResponseExperiment;
import com.sciome.bmdexpress2.mvp.model.IStatModelProcessable;
import com.sciome.bmdexpress2.mvp.model.category.CategoryAnalysisResults;
import com.sciome.bmdexpress2.mvp.model.prefilter.CurveFitPrefilterResults;
import com.sciome.bmdexpress2.mvp.model.prefilter.OneWayANOVAResults;
import com.sciome.bmdexpress2.mvp.model.prefilter.OriogenResults;
import com.sciome.bmdexpress2.mvp.model.prefilter.WilliamsTrendResults;
import com.sciome.bmdexpress2.mvp.model.stat.BMDResult;
import com.sciome.bmdexpress2.shared.BMDExpressConstants;
import com.sciome.bmdexpress2.shared.BMDExpressProperties;
import com.sciome.bmdexpress2.shared.CategoryAnalysisEnum;
import com.sciome.bmdexpress2.util.FileIO;
import com.sciome.bmdexpress2.util.MatrixData;
import com.sciome.bmdexpress2.util.bmds.BMD_METHOD;
import com.sciome.bmdexpress2.util.bmds.ModelInputParameters;
import com.sciome.bmdexpress2.util.bmds.ModelSelectionParameters;
import com.sciome.bmdexpress2.util.bmds.shared.BestModelSelectionBMDLandBMDU;
import com.sciome.bmdexpress2.util.bmds.shared.BestModelSelectionWithFlaggedHillModelEnum;
import com.sciome.bmdexpress2.util.bmds.shared.BestPolyModelTestEnum;
import com.sciome.bmdexpress2.util.bmds.shared.ExponentialModel;
import com.sciome.bmdexpress2.util.bmds.shared.FlagHillModelDoseEnum;
import com.sciome.bmdexpress2.util.bmds.shared.HillModel;
import com.sciome.bmdexpress2.util.bmds.shared.PolyModel;
import com.sciome.bmdexpress2.util.bmds.shared.PowerModel;
import com.sciome.bmdexpress2.util.bmds.shared.StatModel;
import com.sciome.bmdexpress2.util.categoryanalysis.CategoryAnalysisParameters;
import com.sciome.bmdexpress2.util.categoryanalysis.GeneLevelUtils;
import com.sciome.bmdexpress2.util.categoryanalysis.IVIVEParameters;
import com.sciome.bmdexpress2.util.categoryanalysis.defined.DefinedCategoryFileParameters;
import com.sciome.bmdexpress2.util.curvep.GCurvePInputParameters;
import com.sciome.commons.math.httk.calc.calc_analytic_css.Model;
import com.sciome.commons.math.httk.model.Compound;
import com.sciome.commons.math.httk.model.CompoundTable;
import com.sciome.commons.math.httk.model.InVitroData;

/*
 * When command line is in "analyze" mode, use this class to run the different analyses
 * specified in the configuration file that is passed in.
 */
public class AnalyzeRunner
{

	BMDProject project = new BMDProject();
	boolean verbose = true;
	boolean veryverbose = false;

	public void analyze(String configFile, boolean veryverbose) throws Exception
	{

		this.veryverbose = veryverbose;
		// deserialize the config file that was passed on commandline
		RunConfig runConfig = getRunConfig(configFile);

		// load the project if the file exists.
		// if overwrite is set to true then don't open it, but rather start fresh
		// This little bit of code will set the base directory path.
		// sometimes when running command line, you want to copy your base path
		// to another node from home dir and use that so that many simultaneous
		// running instances are not hitting the home dir at the same time.
		if (runConfig.getBasePath() == null || runConfig.getBasePath().equals(""))
			BMDExpressConstants.getInstance();
		else
			BMDExpressConstants.getInstance(runConfig.getBasePath());

		BMDExpressProperties.getInstance().setIsConsole(true);

		if (new File(runConfig.getBm2FileName()).exists() && !runConfig.getOverwrite())
		{
			try
			{
				FileInputStream fileIn = new FileInputStream(new File(runConfig.getBm2FileName()));
				BufferedInputStream bIn = new BufferedInputStream(fileIn, 1024 * 2000);

				ObjectInputStream in = new ObjectInputStream(bIn);
				project = (BMDProject) in.readObject();
				in.close();
				fileIn.close();
				System.out.println("Reading existing project: " + runConfig.getBm2FileName());
				if (veryverbose)
					System.out.println("All results will be added to this existing project");
			}
			catch (IOException i)
			{
				i.printStackTrace();
			}
			catch (ClassNotFoundException c)
			{
				c.printStackTrace();
			}
		}

		if (veryverbose)
			System.out.println(getJsonForObject(runConfig));

		// 1: get all the expression data configs

		List<ExpressionDataConfig> expressionConfigs = runConfig.getExpressionDataConfigs();
		if (expressionConfigs != null)
		{
			System.out.println("Reading expression data");
			for (ExpressionDataConfig expressionConfig : expressionConfigs)
				doExpressionConfig(expressionConfig);
		}

		// 2: get all the anova configs
		List<PrefilterConfig> preFilterConfigs = runConfig.getPreFilterConfigs();

		if (preFilterConfigs != null)
		{
			System.out.println("Performing prefilter step");
			for (PrefilterConfig preFilterConfig : preFilterConfigs)
				doPrefilter(preFilterConfig);
		}

		// 3.1: get all the analysis configs
		List<BMDSConfig> bmdsConfigs = runConfig.getBmdsConfigs();
		if (bmdsConfigs != null)
		{
			System.out.println("Performing MLE BMD Analysis");
			for (BMDSConfig bmdsConfig : bmdsConfigs)
				doBMDSAnalysis(bmdsConfig);
		}

		// 3.2: get all the analysis configs
		List<ModelAveragingConfig> maConfigs = runConfig.getMaConfigs();
		if (maConfigs != null)
		{
			System.out.println("Performing Model Averaging BMD Analysis");
			for (ModelAveragingConfig maConfig : maConfigs)
				doMaAnalysis(maConfig);
		}

		// 4: get all the analysis configs
		List<NonParametricConfig> nonParametricConfigs = runConfig.getNonParametricConfigs();
		if (nonParametricConfigs != null)
		{
			System.out.println("Performing Non-Parametric BMD Analysis");
			for (NonParametricConfig nonPConfig : nonParametricConfigs)
				doNonParametricAnalysis(nonPConfig);
		}

		// 5: get all the category analysis configs
		List<CategoryConfig> catConfigs = runConfig.getCategoryAnalysisConfigs();
		if (catConfigs != null)
		{
			System.out.println("Performing Category Analysis");
			for (CategoryConfig catConfig : catConfigs)
				doCatAnalysis(catConfig);
		}

		// 6. see if this needs exporting to json
		if (runConfig.getJsonExportFileName() != null)
		{
			System.out.println("Exporting as JSON: " + runConfig.getJsonExportFileName());
			doJsonExport(runConfig.getJsonExportFileName());
		}

		try
		{
			System.out.println("Saving project: " + runConfig.getBm2FileName());
			File selectedFile = new File(runConfig.getBm2FileName());
			FileOutputStream fileOut = new FileOutputStream(selectedFile);

			int bufferSize = 2000 * 1024; // make it a 2mb buffer
			BufferedOutputStream bout = new BufferedOutputStream(fileOut, bufferSize);
			ObjectOutputStream out = new ObjectOutputStream(bout);
			project.setName(selectedFile.getName());
			out.writeObject(project);
			out.close();
			fileOut.close();
		}
		catch (IOException i)
		{
			i.printStackTrace();
		}
	}

	// invoke the export to json functionality.
	private void doJsonExport(String jsonExportFileName) throws Exception
	{
		System.out.println("export to json");
		new ExportRunner().exportToJson(project, jsonExportFileName);
	}

	/*
	 * perform category analysis based on the category configuration.
	 */
	private void doCatAnalysis(CategoryConfig catConfig) throws Exception
	{

		if (veryverbose)
			System.out.println(getJsonForObject(catConfig));

		CategoryAnalysisEnum catAn = null;
		String analysisSpecificMessage = "";
		if (catConfig instanceof GOConfig)
		{
			catAn = CategoryAnalysisEnum.GO;
			analysisSpecificMessage = "GO category = " + ((GOConfig) catConfig).getGoCategory();
		}
		else if (catConfig instanceof DefinedConfig)
		{
			catAn = CategoryAnalysisEnum.DEFINED;

			analysisSpecificMessage = "probe file=" + ((DefinedConfig) catConfig).getProbeFilePath() + "\n"
					+ "category file=" + ((DefinedConfig) catConfig).getCategoryFilePath();
		}
		else if (catConfig instanceof PathwayConfig)
		{
			catAn = CategoryAnalysisEnum.PATHWAY;
			analysisSpecificMessage = "Pathway = " + ((PathwayConfig) catConfig).getSignalingPathway();
		}
		else if (catConfig instanceof GeneLevelConfig)
		{
			catAn = CategoryAnalysisEnum.GENE_LEVEL;
			analysisSpecificMessage = "Gene Level Category Analysis";
		}

		if (catConfig.getInputName() != null)
			System.out.println(catAn.toString() + " analysis on " + catConfig.getInputName());
		else
			System.out.println(catAn.toString() + " analysis");

		System.out.println(analysisSpecificMessage);

		List<BMDResult> bmdResultsToRun = new ArrayList<>();
		for (BMDResult result : project.getbMDResult())
			if (catConfig.getInputName() == null)
				bmdResultsToRun.add(result);
			else if (result.getName().equalsIgnoreCase(catConfig.getInputName()))
				bmdResultsToRun.add(result);

		CategoryAnalysisParameters params = new CategoryAnalysisParameters();

		if (catConfig.getBmdBMDLRatioMin() == null)
			params.setRemoveBMDBMDLRatio(false);
		else
		{
			params.setBmdBmdlRatio(catConfig.getBmdBMDLRatioMin());
			params.setRemoveBMDBMDLRatio(true);
		}

		if (catConfig.getBmduBMDLRatioMin() == null)
			params.setRemoveBMDUBMDLRatio(false);
		else
		{
			params.setBmduBmdlRatio(catConfig.getBmduBMDLRatioMin());
			params.setRemoveBMDUBMDLRatio(true);
		}

		if (catConfig.getBmduBMDRatioMin() == null)
			params.setRemoveBMDUBMDRatio(false);
		else
		{
			params.setBmduBmdRatio(catConfig.getBmduBMDRatioMin());
			params.setRemoveBMDUBMDRatio(true);
		}
		if (catConfig.getBmdPValueCutoff() == null)
			params.setRemoveBMDPValueLessCuttoff(false);
		else
		{
			params.setpValueCutoff(catConfig.getBmdPValueCutoff());
			params.setRemoveBMDPValueLessCuttoff(true);
		}

		if (catConfig.getBmdRSquaredCutoff() == null)
			params.setRemoveRSquared(false);
		else
		{
			params.setrSquared(catConfig.getBmdRSquaredCutoff());
			params.setRemoveRSquared(true);
		}

		if (catConfig.getMinGenesInSet() == null)
			params.setRemoveMinGenesInSet(false);
		else
		{
			params.setMinGenesInSet(catConfig.getMinGenesInSet());
			params.setRemoveMinGenesInSet(true);
		}

		if (catConfig.getMaxGenesInSet() == null)
			params.setRemoveMaxGenesInSet(false);
		else
		{
			params.setMaxGenesInSet(catConfig.getMaxGenesInSet());
			params.setRemoveMaxGenesInSet(true);
		}

		if (catConfig.getMaxFoldChange() == null)
			params.setUserFoldChangeFilter(false);
		else
		{
			params.setMaxFoldChange(catConfig.getMaxFoldChange());
			params.setUserFoldChangeFilter(true);
		}

		if (catConfig.getPrefilterPValueMin() == null)
			params.setUserPValueFilter(false);
		else
		{
			params.setPValue(catConfig.getPrefilterPValueMin());
			params.setUserPValueFilter(true);
		}

		if (catConfig.getPrefilterAdjustedPValueMin() == null)
			params.setUserAdjustedPValueFilter(false);
		else
		{
			params.setAdjustedPValue(catConfig.getPrefilterAdjustedPValueMin());
			params.setUserAdjustedPValueFilter(true);
		}

		if (catConfig.getCorrelationCutoffForConflictingProbeSets() != null)
			params.setCorrelationCutoffConflictingProbeSets(
					catConfig.getCorrelationCutoffForConflictingProbeSets());

		if (catConfig.getIdentifyConflictingProbeSets() == null)
			params.setIdentifyConflictingProbeSets(false);
		else
			params.setIdentifyConflictingProbeSets(catConfig.getIdentifyConflictingProbeSets());

		if (catConfig.getnFoldBelowLowestDose() == null)
			params.setRemoveNFoldBelowLowestDose(false);
		else
		{
			params.setnFoldbelowLowestDoseValue(catConfig.getnFoldBelowLowestDose());
			params.setRemoveNFoldBelowLowestDose(true);
		}

		if (catConfig.getRemoveBMDGreaterHighDose() == null)
			params.setRemoveBMDGreaterHighDose(false);
		else
			params.setRemoveBMDGreaterHighDose(catConfig.getRemoveBMDGreaterHighDose());

		if (catConfig.getRemovePromiscuousProbes() == null)
			params.setRemovePromiscuousProbes(false);
		else
			params.setRemovePromiscuousProbes(catConfig.getRemovePromiscuousProbes());

		if (catConfig.getDeduplicateGeneSets() == null)
			params.setDeduplicateGeneSets(false);
		else
			params.setDeduplicateGeneSets(catConfig.getDeduplicateGeneSets());

		if (catConfig.getRemoveStepFunction() == null)
			params.setRemoveStepFunction(false);
		else
			params.setRemoveStepFunction(catConfig.getRemoveStepFunction());

		if (catConfig.getRemoveStepFunctionWithBMDLower() == null)
			params.setRemoveStepFunctionWithBMDLower(false);
		else
			params.setRemoveStepFunctionWithBMDLower(catConfig.getRemoveStepFunction());

		// Set IVIVE parameters
		if (catConfig.getComputeIVIVE())
		{
			System.out.println("Setting Toxicokinetic Modeling Parameters ");
			IVIVEConfig config = catConfig.getIviveConfig();
			if (veryverbose)
				System.out.println(getJsonForObject(config));

			IVIVEParameters iviveParameters = new IVIVEParameters();
			// Set compound
			Compound compound = null;
			if (config.getUseAutoPopulate())
			{
				// Initialize InVitroData with clint and fub
				InVitroData data = new InVitroData();
				data.setParam("Clint", config.getCLint());
				data.setParam("Funbound.plasma", config.getFractionUnboundPlasma());
				HashMap<String, InVitroData> map = new HashMap<String, InVitroData>();
				map.put(config.getSpecies(), data);

				HashMap<String, Double> rBlood2Plasma = new HashMap<String, Double>();

				compound = new Compound(config.getCompoundName(), config.getCompoundCASRN(),
						config.getCompoundSMILES(), config.getLogP(), config.getMw(), 0.0,
						config.getPkaAcceptor(), config.getPkaDonor(), map, rBlood2Plasma);
			}
			else
			{
				CompoundTable table = CompoundTable.getInstance();
				table.loadDefault();
				if (config.getCompoundName() != null)
				{
					compound = table.getCompoundByName(config.getCompoundName());
				}
				else if (config.getCompoundCASRN() != null)
				{
					compound = table.getCompoundByCAS(config.getCompoundCASRN());
				}
				else if (config.getCompoundSMILES() != null)
				{
					compound = table.getCompoundBySMILES(config.getCompoundSMILES());
				}
			}
			iviveParameters.setCompound(compound);

			// Set models
			List<Model> models = new ArrayList<Model>();
			if (config.getOneCompartment())
				models.add(Model.ONECOMP);
			if (config.getPbtk())
				models.add(Model.PBTK);
			if (config.getThreeCompartment())
				models.add(Model.THREECOMP);
			if (config.getThreeCompartmentSS())
				models.add(Model.THREECOMPSS);
			iviveParameters.setModels(models);

			iviveParameters.setConcentrationUnits(config.getConcentrationUnits());
			iviveParameters.setDoseUnits(config.getDoseUnits());
			iviveParameters.setQuantile(config.getQuantile());
			iviveParameters.setSpecies(config.getSpecies());
			iviveParameters.setInvivo(config.getInVivo());
			iviveParameters.setDoseSpacing(config.getDoseSpacing());
			iviveParameters.setFinalTime(config.getFinalTime());

			params.setIviveParameters(iviveParameters);
		}

		String catPreString = "";

		if (catConfig instanceof DefinedConfig)
		{
			catPreString += "Defined Category Analysis on ";
			DefinedCategoryFileParameters probeFileParameters = new DefinedCategoryFileParameters();

			probeFileParameters.setUsedColumns(new int[] { 0, 1 });
			probeFileParameters.setFileName(((DefinedConfig) catConfig).getProbeFilePath());
			MatrixData idsMatrix = FileIO.readFileMatrix(null,
					new File(((DefinedConfig) catConfig).getProbeFilePath()));
			idsMatrix.setAllString(true);
			probeFileParameters.setMatrixData(idsMatrix);

			DefinedCategoryFileParameters catFileParameters = new DefinedCategoryFileParameters();

			catFileParameters.setUsedColumns(new int[] { 0, 1, 2 });
			catFileParameters.setFileName(((DefinedConfig) catConfig).getProbeFilePath());
			MatrixData idsMatrix1 = FileIO.readFileMatrix(null,
					new File(((DefinedConfig) catConfig).getCategoryFilePath()));
			idsMatrix1.setAllString(true);
			catFileParameters.setMatrixData(idsMatrix1);
			params.setProbeFileParameters(probeFileParameters);
			params.setCategoryFileParameters(catFileParameters);
		}

		if (catConfig instanceof PathwayConfig)
		{
			params.setPathwayDB(((PathwayConfig) catConfig).getSignalingPathway());
			catPreString += "Pathway Analysis on ";
		}

		if (catConfig instanceof GeneLevelConfig)
		{
			catPreString += "Gene Level Analysis on ";

		}

		if (catConfig instanceof GOConfig)
		{
			catPreString += "GO (" + ((GOConfig) catConfig).getGoCategory() + ") Analysis on ";
			params.setGoCat(((GOConfig) catConfig).getGoCategory());

			if (((GOConfig) catConfig).getGoCategory().equalsIgnoreCase("universal"))
				params.setGoTermIdx(0);
			else if (((GOConfig) catConfig).getGoCategory().equalsIgnoreCase("biological_process"))
				params.setGoTermIdx(1);
			else if (((GOConfig) catConfig).getGoCategory().equalsIgnoreCase("molecular_function"))
				params.setGoTermIdx(2);
			else if (((GOConfig) catConfig).getGoCategory().equalsIgnoreCase("cellular_component"))
				params.setGoTermIdx(3);

		}

		for (BMDResult bmdResult : bmdResultsToRun)
		{
			if (catConfig instanceof GeneLevelConfig)
			{
				params.setCategoryFileParameters(
						GeneLevelUtils.getCategoryFileParameters(bmdResult.getDoseResponseExperiment()));
				params.setProbeFileParameters(
						GeneLevelUtils.getProbeFileParameters(bmdResult.getDoseResponseExperiment()));
			}

			System.out.println(catPreString + bmdResult.getName());
			CategoryAnalysisResults catResults = new CategoryAnalysisRunner().runCategoryAnalysis(bmdResult,
					catAn, params);

			if (catConfig.getOutputName() != null)
				catResults.setName(catConfig.getOutputName());
			else
				project.giveBMDAnalysisUniqueName(catResults, catResults.getName());
			project.getCategoryAnalysisResults().add(catResults);
		}
	}

	/*
	 * perform bmd analysis on the data.
	 */
	private void doBMDSAnalysis(BMDSConfig bmdsConfig) throws Exception
	{
		String method = "BMDS 3.x.x MLE";
		// if (bmdsConfig.getMethod().equals(1))
		// method = "BMDS 2.7.x MLE";

		if (veryverbose)
			System.out.println(getJsonForObject(bmdsConfig));

		// first set up the model input parameters basedo n
		// bmdsConfig setup
		ModelInputParameters inputParameters = new ModelInputParameters();

		inputParameters.setBmdMethod(BMD_METHOD.TOXICR);
		inputParameters.setBMDSMajorVersion("3.x with shared library/DLL");
		// if (bmdsConfig.getMethod().equals(1))
		// {
		// inputParameters.setBmdMethod(BMD_METHOD.ORIGINAL);
		// inputParameters.setBMDSMajorVersion("2.x");
		// }

		inputParameters.setFast(false);
		if (bmdsConfig.getBmdsInputConfig().getBmdUBmdLEstimationMethod().equals(2))
			inputParameters.setFast(true);

		inputParameters.setStepFunctionThreshold(
				bmdsConfig.getBmdsInputConfig().getStepFunctionThreshold().doubleValue());

		// inputParameters.setIterations(bmdsConfig.getBmdsInputConfig().getMaxIterations());
		// inputParameters.setConfidence(bmdsConfig.getBmdsInputConfig().getConfidenceLevel());
		inputParameters.setBmrLevel(bmdsConfig.getBmdsInputConfig().getBmrFactor());
		inputParameters.setNumThreads(bmdsConfig.getNumberOfThreads());
		// if (bmdsConfig.getKillTime() != null)
		// inputParameters.setKillTime(bmdsConfig.getKillTime().intValue() * 1000);
		// else
		// inputParameters.setKillTime(600000); // default to 10 minute timeout

		inputParameters.setBmdlCalculation(1);
		inputParameters.setBmdCalculation(1);
		inputParameters.setConstantVariance((bmdsConfig.getBmdsInputConfig().getConstantVariance()) ? 1 : 0);
		// for simulation only?
		// inputParameters.setRestirctPower((bmdsConfig.getBmdsInputConfig().getRestrictPower()) ? 1 : 0);

		// in practice bmrtype can only be set to relative deviation for non-log normalized data.
		inputParameters.setBmrType(1);
		if (bmdsConfig.getBmdsInputConfig().getBmrType() != null)
			inputParameters.setBmrType(bmdsConfig.getBmdsInputConfig().getBmrType().intValue());

		inputParameters.setPolyMonotonic(false);
		if (bmdsConfig.getBmdsInputConfig().getRestrictPolyToMonotonic() != null)
			inputParameters.setPolyMonotonic(
					bmdsConfig.getBmdsInputConfig().getRestrictPolyToMonotonic().booleanValue());

		if (inputParameters.getConstantVariance() == 0)
			inputParameters.setRho(inputParameters.getNegative());

		// now set up the model selection parameters.
		ModelSelectionParameters modelSelectionParameters = new ModelSelectionParameters();

		// set up how to use the bmdl and bmdu
		modelSelectionParameters
				.setBestModelSelectionBMDLandBMDU(BestModelSelectionBMDLandBMDU.COMPUTE_AND_UTILIZE);
		if (bmdsConfig.getBmdsBestModelSelection().getBmdlBMDUUse().equals(2))
			modelSelectionParameters
					.setBestModelSelectionBMDLandBMDU(BestModelSelectionBMDLandBMDU.COMPUTE_BUT_IGNORE);
		else if (bmdsConfig.getBmdsBestModelSelection().getBmdlBMDUUse().equals(3))
			modelSelectionParameters
					.setBestModelSelectionBMDLandBMDU(BestModelSelectionBMDLandBMDU.DO_NOT_COMPUTE);
		else if (bmdsConfig.getBmdsBestModelSelection().getBmdlBMDUUse().equals(4))
			modelSelectionParameters.setBestModelSelectionBMDLandBMDU(
					BestModelSelectionBMDLandBMDU.COMPUTE_AND_UTILIZE_BMD_BMDL);
		BestPolyModelTestEnum polyTest = null;
		if (bmdsConfig.getBmdsBestModelSelection().getBestPolyTest().equals(2))
			polyTest = BestPolyModelTestEnum.LOWEST_AIC;
		else if (bmdsConfig.getBmdsBestModelSelection().getBestPolyTest().equals(1))
			polyTest = BestPolyModelTestEnum.NESTED_CHI_SQUARED;
		modelSelectionParameters.setBestPolyModelTest(polyTest);

		// set up the pValue
		modelSelectionParameters.setpValue(bmdsConfig.getBmdsBestModelSelection().getpValueCutoff());

		// set up Flag HIll
		modelSelectionParameters
				.setFlagHillModel(bmdsConfig.getBmdsBestModelSelection().getFlagHillWithKParameter());

		FlagHillModelDoseEnum flagHillDose = null;
		if (bmdsConfig.getBmdsBestModelSelection().getkParameterValue().equals(1))
			flagHillDose = FlagHillModelDoseEnum.LOWEST_DOSE;
		else if (bmdsConfig.getBmdsBestModelSelection().getkParameterValue().equals(2))
			flagHillDose = FlagHillModelDoseEnum.ONE_HALF_OF_LOWEST_DOSE;
		else if (bmdsConfig.getBmdsBestModelSelection().getkParameterValue().equals(3))
			flagHillDose = FlagHillModelDoseEnum.ONE_THIRD_OF_LOWEST_DOSE;

		modelSelectionParameters.setFlagHillModelDose(flagHillDose);

		// best model selection with flagged hill model
		BestModelSelectionWithFlaggedHillModelEnum bestModeSel = null;
		if (bmdsConfig.getBmdsBestModelSelection().getBestModelSelectionWithFlaggedHill().equals(1))
			bestModeSel = BestModelSelectionWithFlaggedHillModelEnum.INCLUDE_FLAGGED_HILL;
		else if (bmdsConfig.getBmdsBestModelSelection().getBestModelSelectionWithFlaggedHill().equals(2))
			bestModeSel = BestModelSelectionWithFlaggedHillModelEnum.EXCLUDE_FLAGGED_HILL_FROM_BEST;
		else if (bmdsConfig.getBmdsBestModelSelection().getBestModelSelectionWithFlaggedHill().equals(3))
			bestModeSel = BestModelSelectionWithFlaggedHillModelEnum.EXCLUDE_ALL_HILL_FROM_BEST;
		else if (bmdsConfig.getBmdsBestModelSelection().getBestModelSelectionWithFlaggedHill().equals(4))
			bestModeSel = BestModelSelectionWithFlaggedHillModelEnum.MODIFY_BMD_IF_FLAGGED_HILL_BEST;
		else if (bmdsConfig.getBmdsBestModelSelection().getBestModelSelectionWithFlaggedHill().equals(5))
			bestModeSel = BestModelSelectionWithFlaggedHillModelEnum.SELECT_NEXT_BEST_PVALUE_GREATER_OO5;

		modelSelectionParameters.setBestModelSelectionWithFlaggedHill(bestModeSel);

		if (bmdsConfig.getBmdsBestModelSelection().getBestModelSelectionWithFlaggedHill().equals(4))
			modelSelectionParameters.setModFlaggedHillBMDFractionMinBMD(
					bmdsConfig.getBmdsBestModelSelection().getModifyFlaggedHillWithFractionMinBMD());
		else
			modelSelectionParameters.setModFlaggedHillBMDFractionMinBMD(0.5);

		// figure out which models are going to be run
		List<StatModel> modelsToRun = new ArrayList<>();
		for (BMDSModelConfig modelConfig : bmdsConfig.getModelConfigs())
		{
			if (modelConfig instanceof HillConfig)
			{
				HillModel hillModel = new HillModel();
				//// if (bmdsConfig.getMethod().equals(1))
				// hillModel.setVersion(BMDExpressProperties.getInstance().getHillVersion());
				// else
				hillModel.setVersion("Hill EPA BMDS MLE ToxicR");
				modelsToRun.add(hillModel);
			}
			if (modelConfig instanceof PowerConfig)
			{
				PowerModel powerModel = new PowerModel();
				// if (bmdsConfig.getMethod().equals(1))
				// powerModel.setVersion(BMDExpressProperties.getInstance().getPowerVersion());
				// else
				powerModel.setVersion("Power EPA BMDS MLE ToxicR");
				modelsToRun.add(powerModel);
			}
			if (modelConfig instanceof PolyConfig)
			{
				PolyModel polymodel = new PolyModel();

				polymodel.setDegree(((PolyConfig) modelConfig).getDegree());
				// if (bmdsConfig.getMethod().equals(1))
				// polymodel.setVersion(BMDExpressProperties.getInstance().getPolyVersion());
				if (polymodel.getDegree() == 1)
					polymodel.setVersion("Linear EPA BMDS MLE ToxicR");
				else
					polymodel.setVersion("Poly " + polymodel.getDegree() + " EPA BMDS MLE ToxicR");
				modelsToRun.add(polymodel);
			}

			if (modelConfig instanceof ExponentialConfig)
			{
				ExponentialModel exponentialModel = new ExponentialModel();

				exponentialModel.setOption(((ExponentialConfig) modelConfig).getExpModel());

				// if (bmdsConfig.getMethod().equals(1))
				// exponentialModel.setVersion(BMDExpressProperties.getInstance().getExponentialVersion());
				// else
				exponentialModel
						.setVersion("Exponential " + exponentialModel.getOption() + " EPA BMDS MLE ToxicR");
				modelsToRun.add(exponentialModel);
			}

		}

		// if inputname is specified then get the analysis that matches name.
		// otherwise get all the analysis based on the given input category.
		// input category can be "anova" or "expression" which means
		// one way anova results or dose response expersssion data.
		List<IStatModelProcessable> processables = new ArrayList<>();
		// get the dataset to run

		for (OneWayANOVAResults ways : project.getOneWayANOVAResults())
			if (bmdsConfig.getInputCategory().equalsIgnoreCase(BMDExpressCommandLine.ONE_WAY_ANOVA))
			{
				System.out.println("Performing MLE BMD Analysis on One-way ANOVA Test Results");
				if (bmdsConfig.getInputName() == null)
					processables.add(ways);
				else if (ways.getName().equalsIgnoreCase(bmdsConfig.getInputName()))
					processables.add(ways);
			}

		for (WilliamsTrendResults will : project.getWilliamsTrendResults())
			if (bmdsConfig.getInputCategory().equalsIgnoreCase(BMDExpressCommandLine.WILLIAMS))
			{
				System.out.println("Performing MLE BMD Analysis on Williams Trend Test Results");
				if (bmdsConfig.getInputName() == null)
					processables.add(will);
				else if (will.getName().equalsIgnoreCase(bmdsConfig.getInputName()))
					processables.add(will);
			}

		for (OriogenResults ori : project.getOriogenResults())
			if (bmdsConfig.getInputCategory().equalsIgnoreCase(BMDExpressCommandLine.ORIOGEN))
			{
				System.out.println("Performing MLE BMD Analysis on Oriogen Test Results");
				if (bmdsConfig.getInputName() == null)
					processables.add(ori);
				else if (ori.getName().equalsIgnoreCase(bmdsConfig.getInputName()))
					processables.add(ori);
			}

		for (CurveFitPrefilterResults curve : project.getCurveFitPrefilterResults())
			if (bmdsConfig.getInputCategory().equalsIgnoreCase(BMDExpressCommandLine.CURVE_FIT_PREFILTER))
			{
				System.out.println("Performing MLE BMD Analysis on Curve Fit Prefilter Test Results");
				if (bmdsConfig.getInputName() == null)
					processables.add(curve);
				else if (curve.getName().equalsIgnoreCase(bmdsConfig.getInputName()))
					processables.add(curve);
			}

		for (DoseResponseExperiment exps : project.getDoseResponseExperiments())
			if (bmdsConfig.getInputCategory().equalsIgnoreCase(BMDExpressCommandLine.EXPRESSION))
			{
				System.out.println("Performing MLE BMD Analysis on Expression Data");
				if (bmdsConfig.getInputName() == null)
					processables.add(exps);
				else if (exps.getName().equalsIgnoreCase(bmdsConfig.getInputName()))
					processables.add(exps);
			}

		// for each processable analysis, run the models and select best models.
		for (IStatModelProcessable processableData : processables)
		{
			System.out.println("Performing MLE BMD Analysis on: " + processableData.getDataSetName());
			BMDResult result = new BMDAnalysisRunner().runBMDAnalysis(processableData,
					modelSelectionParameters, modelsToRun, inputParameters, null);
			if (bmdsConfig.getOutputName() != null)
				result.setName(bmdsConfig.getOutputName());
			else
				project.giveBMDAnalysisUniqueName(result, result.getName());
			project.getbMDResult().add(result);
		}

		System.out.println("Finished MLE BMD Analysis");

	}

	/*
	 * perform bmd analysis on the data.
	 */
	private void doMaAnalysis(ModelAveragingConfig maConfig) throws Exception
	{

		if (veryverbose)
			System.out.println(getJsonForObject(maConfig));

		String method = "ToxicR MCMC";
		if (maConfig.getMethod().equals(1))
			method = "ToxicR Laplace";

		String outPrefix = "Performing " + method + " Model Averaging on";

		// first set up the model input parameters basedo n
		// bmdsConfig setup
		ModelInputParameters inputParameters = new ModelInputParameters();

		inputParameters.setBMDSMajorVersion("3.x with shared library/DLL");

		inputParameters.setBmdMethod(BMD_METHOD.TOXICR);
		inputParameters.setMAMethod("Laplace Model Averaging");
		if (maConfig.getMethod().equals(2))
		{
			inputParameters.setBmdMethod(BMD_METHOD.TOXICR_MCMC);
			inputParameters.setMAMethod("MCMC Model Averaging");
		}

		inputParameters.setFast(false);
		if (maConfig.getBmdsInputConfig().getBmdUBmdLEstimationMethod().equals(2))
			inputParameters.setFast(true);

		inputParameters.setStepFunctionThreshold(
				maConfig.getBmdsInputConfig().getStepFunctionThreshold().doubleValue());
		inputParameters.setBmrLevel(maConfig.getBmdsInputConfig().getBmrFactor());
		inputParameters.setNumThreads(maConfig.getNumberOfThreads());

		inputParameters.setConstantVariance((maConfig.getBmdsInputConfig().getConstantVariance()) ? 1 : 0);
		// for simulation only?
		// inputParameters.setRestirctPower((maConfig.getBmdsInputConfig().getRestrictPower()) ? 1 : 0);

		// in practice bmrtype can only be set to relative deviation for non-log normalized data.
		inputParameters.setBmrType(1);
		if (maConfig.getBmdsInputConfig().getBmrType() != null)
			inputParameters.setBmrType(maConfig.getBmdsInputConfig().getBmrType().intValue());

		if (inputParameters.getConstantVariance() == 0)
			inputParameters.setRho(inputParameters.getNegative());

		// figure out which models are going to be run
		List<StatModel> modelsToRun = new ArrayList<>();
		for (BMDSModelConfig modelConfig : maConfig.getModelConfigs())
		{
			if (modelConfig instanceof HillConfig)
			{
				HillModel hillModel = new HillModel();
				hillModel.setVersion("ToxicR 3.x.x");
				modelsToRun.add(hillModel);
			}
			if (modelConfig instanceof PowerConfig)
			{
				PowerModel powerModel = new PowerModel();
				powerModel.setVersion("ToxicR 3.x.x");
				modelsToRun.add(powerModel);
			}
			if (modelConfig instanceof PolyConfig)
			{
				PolyModel polymodel = new PolyModel();
				polymodel.setVersion("ToxicR 3.x.x");
				polymodel.setDegree(((PolyConfig) modelConfig).getDegree());
				modelsToRun.add(polymodel);
			}

			if (modelConfig instanceof ExponentialConfig)
			{
				ExponentialModel exponentialModel = new ExponentialModel();
				exponentialModel.setVersion("ToxicR 3.x.x");
				exponentialModel.setOption(((ExponentialConfig) modelConfig).getExpModel());
				modelsToRun.add(exponentialModel);
			}

		}

		// if inputname is specified then get the analysis that matches name.
		// otherwise get all the analysis based on the given input category.
		// input category can be "anova" or "expression" which means
		// one way anova results or dose response expersssion data.
		List<IStatModelProcessable> processables = new ArrayList<>();
		// get the dataset to run

		for (OneWayANOVAResults ways : project.getOneWayANOVAResults())
			if (maConfig.getInputCategory().equalsIgnoreCase(BMDExpressCommandLine.ONE_WAY_ANOVA))
			{
				System.out.println(outPrefix + " on One-way ANOVA Test Results");
				if (maConfig.getInputName() == null)
					processables.add(ways);
				else if (ways.getName().equalsIgnoreCase(maConfig.getInputName()))
					processables.add(ways);
			}

		for (WilliamsTrendResults will : project.getWilliamsTrendResults())
			if (maConfig.getInputCategory().equalsIgnoreCase(BMDExpressCommandLine.WILLIAMS))
			{
				System.out.println(outPrefix + " Williams Trend Test Results");

				if (maConfig.getInputName() == null)
					processables.add(will);
				else if (will.getName().equalsIgnoreCase(maConfig.getInputName()))
					processables.add(will);
			}

		for (OriogenResults ori : project.getOriogenResults())
		{
			if (maConfig.getInputCategory().equalsIgnoreCase(BMDExpressCommandLine.ORIOGEN))
				System.out.println(outPrefix + " Oriogen Test Results");
			if (maConfig.getInputName() == null)
				processables.add(ori);
			else if (ori.getName().equalsIgnoreCase(maConfig.getInputName()))
				processables.add(ori);
		}

		for (CurveFitPrefilterResults curve : project.getCurveFitPrefilterResults())
			if (maConfig.getInputCategory().equalsIgnoreCase(BMDExpressCommandLine.CURVE_FIT_PREFILTER))
			{
				System.out.println(outPrefix + " Curve Fit Prefilter Test Results");
				if (maConfig.getInputName() == null)
					processables.add(curve);
				else if (curve.getName().equalsIgnoreCase(maConfig.getInputName()))
					processables.add(curve);
			}

		for (DoseResponseExperiment exps : project.getDoseResponseExperiments())
			if (maConfig.getInputCategory().equalsIgnoreCase(BMDExpressCommandLine.EXPRESSION))
			{
				System.out.println(outPrefix + " Expression Data");
				if (maConfig.getInputName() == null)
					processables.add(exps);
				else if (exps.getName().equalsIgnoreCase(maConfig.getInputName()))
					processables.add(exps);
			}

		// for each processable analysis, run the models and select best models.
		boolean laplace = true;
		if (maConfig.getMethod().equals(2))
			laplace = false;
		for (IStatModelProcessable processableData : processables)
		{
			System.out.println(outPrefix + ": " + processableData.getDataSetName());
			BMDResult result = new BMDAnalysisRunner().runMAAnalysis(processableData, modelsToRun,
					inputParameters, laplace);
			if (maConfig.getOutputName() != null)
				result.setName(maConfig.getOutputName());
			else
				project.giveBMDAnalysisUniqueName(result, result.getName());
			project.getbMDResult().add(result);
			System.out.println();
		}

		System.out.println("Finished Model Averaging Analysis");

	}

	/*
	 * perform bmd analysis on the data.
	 */
	private void doNonParametricAnalysis(NonParametricConfig config)
	{
		if (config.getInputName() != null)
			System.out.println(
					"bmd analysis on " + config.getInputName() + " from group " + config.getInputCategory());
		else
			System.out.println("non parametric bmd analysis on group " + config.getInputCategory());
		// first set up the model input parameters basedo n
		// bmdsConfig setup
		GCurvePInputParameters inputParameters = new GCurvePInputParameters();
		inputParameters.setBootStraps(config.getBootStraps());
		inputParameters.setBMR(config.getBmrFactor().floatValue());
		inputParameters.setpValueCutoff(config.getpValueConfidence().floatValue());

		// if inputname is specified then get the analysis that matches name.
		// otherwise get all the analysis based on the given input category.
		// input category can be "anova" or "expression" which means
		// one way anova results or dose response expersssion data.
		List<IStatModelProcessable> processables = new ArrayList<>();
		// get the dataset to run

		for (OneWayANOVAResults ways : project.getOneWayANOVAResults())
			if (config.getInputCategory().equalsIgnoreCase(BMDExpressCommandLine.ONE_WAY_ANOVA))
				if (config.getInputName() == null)
					processables.add(ways);
				else if (ways.getName().equalsIgnoreCase(config.getInputName()))
					processables.add(ways);

		for (WilliamsTrendResults will : project.getWilliamsTrendResults())
			if (config.getInputCategory().equalsIgnoreCase(BMDExpressCommandLine.WILLIAMS))
				if (config.getInputName() == null)
					processables.add(will);
				else if (will.getName().equalsIgnoreCase(config.getInputName()))
					processables.add(will);

		for (OriogenResults ori : project.getOriogenResults())
			if (config.getInputCategory().equalsIgnoreCase(BMDExpressCommandLine.ORIOGEN))
				if (config.getInputName() == null)
					processables.add(ori);
				else if (ori.getName().equalsIgnoreCase(config.getInputName()))
					processables.add(ori);

		for (CurveFitPrefilterResults curve : project.getCurveFitPrefilterResults())
			if (config.getInputCategory().equalsIgnoreCase(BMDExpressCommandLine.CURVE_FIT_PREFILTER))
				if (config.getInputName() == null)
					processables.add(curve);
				else if (curve.getName().equalsIgnoreCase(config.getInputName()))
					processables.add(curve);

		for (DoseResponseExperiment exps : project.getDoseResponseExperiments())
			if (config.getInputCategory().equalsIgnoreCase(BMDExpressCommandLine.EXPRESSION))
				if (config.getInputName() == null)
					processables.add(exps);
				else if (exps.getName().equalsIgnoreCase(config.getInputName()))
					processables.add(exps);

		// for each processable analysis, run the models and select best models.
		for (IStatModelProcessable processableData : processables)
		{
			BMDResult result = new NonParametricAnalysisRunner().runBMDAnalysis(processableData,
					inputParameters);

			if (config.getOutputName() != null)
				result.setName(config.getOutputName());
			else
				project.giveBMDAnalysisUniqueName(result, result.getName());
			project.getbMDResult().add(result);
		}

	}

	/*
	 * do prefilter
	 */
	private void doPrefilter(PrefilterConfig preFilterConfig) throws Exception
	{
		// if the user specifies a dose experiment name, then find it and add it.
		// if the inputname is null, then add all dose response experiments
		// to receive the pre filter.
		List<IStatModelProcessable> processables = new ArrayList<>();
		boolean doOnlyDoseResponseExperimentInput = true;
		if (veryverbose)
			System.out.println(getJsonForObject(preFilterConfig));
		if (preFilterConfig instanceof CurveFitPrefilterConfig)
		{
			CurveFitPrefilterConfig cfpreCfg = (CurveFitPrefilterConfig) preFilterConfig;

			if (cfpreCfg.getInputCategory() == null || cfpreCfg.getInputCategory().equals("")
					|| cfpreCfg.getInputCategory().equals(BMDExpressCommandLine.EXPRESSION))
			{
				System.out.println("Performing Curve Fit Prefilter on Expression Data");
				doOnlyDoseResponseExperimentInput = true;
			}
			else
			{
				// we found out that we are running curve fit prefilter on
				// another prefilter set.
				if (BMDExpressCommandLine.WILLIAMS.equalsIgnoreCase(cfpreCfg.getInputCategory()))
				{
					System.out.println("Performing Curve Fit Prefilter on Williams Trend Test Results");

					for (WilliamsTrendResults exp : project.getWilliamsTrendResults())
					{
						if (preFilterConfig.getInputName() == null
								|| preFilterConfig.getInputName().equals(""))
						{
							processables.add(exp);
						}
						else if (exp.getName().equalsIgnoreCase(preFilterConfig.getInputName()))
						{
							processables.add(exp);
						}
					}
				}
				else if (BMDExpressCommandLine.ONE_WAY_ANOVA.equalsIgnoreCase(cfpreCfg.getInputCategory()))
				{
					System.out.println("Performing Curve Fit Prefilter on One-way ANOVA Test Results");
					for (OneWayANOVAResults exp : project.getOneWayANOVAResults())
						if (preFilterConfig.getInputName() == null
								|| preFilterConfig.getInputName().equals(""))
							processables.add(exp);
						else if (exp.getName().equalsIgnoreCase(preFilterConfig.getInputName()))
							processables.add(exp);
				}
				else if (BMDExpressCommandLine.ORIOGEN.equalsIgnoreCase(cfpreCfg.getInputCategory()))
				{
					System.out.println("Performing Curve Fit Prefilter on Oriogen Test Results");
					for (OriogenResults exp : project.getOriogenResults())
						if (preFilterConfig.getInputName() == null
								|| preFilterConfig.getInputName().equals(""))
							processables.add(exp);
						else if (exp.getName().equalsIgnoreCase(preFilterConfig.getInputName()))
							processables.add(exp);
				}

				doOnlyDoseResponseExperimentInput = false;

			}
		}

		if (doOnlyDoseResponseExperimentInput)
		{
			for (DoseResponseExperiment exp : project.getDoseResponseExperiments())
				if (preFilterConfig.getInputName() == null || preFilterConfig.getInputName().equals(""))
					processables.add(exp);
				else if (exp.getName().equalsIgnoreCase(preFilterConfig.getInputName()))
					processables.add(exp);
		}

		String stdoutInfo = "";
		if (preFilterConfig instanceof ANOVAConfig)
		{
			ANOVARunner anovaRunner = new ANOVARunner();
			if (preFilterConfig.getInputName() != null)
				stdoutInfo = "One-way ANOVA Test";
			else
				stdoutInfo = "One-way ANOVA Test";

			for (IStatModelProcessable processable : processables)
			{
				System.out.println("Starting " + stdoutInfo + ": " + processable.getDataSetName());
				project.getOneWayANOVAResults()
						.add(anovaRunner.runANOVAFilter(processable, preFilterConfig.getpValueCutoff(),
								preFilterConfig.getUseMultipleTestingCorrection(),
								preFilterConfig.getFilterOutControlGenes(),
								preFilterConfig.getUseFoldChange(), preFilterConfig.getFoldChange(),
								preFilterConfig.getpValueLotel(), preFilterConfig.getFoldChangeLotel(),
								preFilterConfig.getOutputName(), preFilterConfig.getNumberOfThreads(),
								preFilterConfig.getlotelTest().equals(2), project));
			}
		}
		else if (preFilterConfig instanceof WilliamsConfig)
		{
			WilliamsTrendRunner williamsRunner = new WilliamsTrendRunner();

			if (preFilterConfig.getInputName() != null)
				stdoutInfo = "Williams Trend Test";
			else
				stdoutInfo = "Williams Trend Test";

			for (IStatModelProcessable processable : processables)
			{
				System.out.println("Starting " + stdoutInfo + ": " + processable.getDataSetName());
				project.getWilliamsTrendResults().add(williamsRunner.runWilliamsTrendFilter(processable,
						preFilterConfig.getpValueCutoff(), preFilterConfig.getUseMultipleTestingCorrection(),
						preFilterConfig.getFilterOutControlGenes(), preFilterConfig.getUseFoldChange(),
						preFilterConfig.getFoldChange(),
						((WilliamsConfig) preFilterConfig).getNumberOfPermutations(),
						preFilterConfig.getpValueLotel(), preFilterConfig.getFoldChangeLotel(),
						preFilterConfig.getOutputName(), preFilterConfig.getNumberOfThreads(),
						preFilterConfig.getlotelTest().equals(2), project));
			}
		}
		else if (preFilterConfig instanceof CurveFitPrefilterConfig)
		{
			CurveFitPrefilterConfig cfpreCfg = (CurveFitPrefilterConfig) preFilterConfig;
			CurveFitPrefilterRunner curveFitRunner = new CurveFitPrefilterRunner();

			// figure out which models are going to be run
			List<StatModel> modelsToRun = new ArrayList<>();
			for (BMDSModelConfig modelConfig : cfpreCfg.getModelConfigs())
			{
				if (modelConfig instanceof HillConfig)
				{
					HillModel hillModel = new HillModel();
					hillModel.setVersion("Hill EPA BMDS MLE ToxicR");
					modelsToRun.add(hillModel);
				}
				if (modelConfig instanceof PowerConfig)
				{
					PowerModel powerModel = new PowerModel();
					powerModel.setVersion("Power EPA BMDS MLE ToxicR");
					modelsToRun.add(powerModel);
				}
				if (modelConfig instanceof PolyConfig)
				{
					PolyModel polymodel = new PolyModel();
					polymodel.setDegree(((PolyConfig) modelConfig).getDegree());
					polymodel.setVersion("Poly " + polymodel.getDegree() + " EPA BMDS MLE ToxicR");
					modelsToRun.add(polymodel);
				}

				if (modelConfig instanceof ExponentialConfig)
				{
					ExponentialModel exponentialModel = new ExponentialModel();

					exponentialModel.setOption(((ExponentialConfig) modelConfig).getExpModel());
					exponentialModel.setVersion(
							"Exponential " + exponentialModel.getOption() + " EPA BMDS MLE ToxicR");
					modelsToRun.add(exponentialModel);
				}

			}

			if (preFilterConfig.getInputName() != null)
				stdoutInfo = "Curve Fit Prefilter Test";
			else
				stdoutInfo = "Curve Fit Prefilter Test";

			for (IStatModelProcessable processable : processables)
			{
				System.out.println("Starting " + stdoutInfo + ": " + processable.getDataSetName());
				int constV = 1;
				if (!cfpreCfg.getConstantVariance())
					constV = 0;
				project.getCurveFitPrefilterResults()
						.add(curveFitRunner.runCurveFitPrefilter(processable,
								preFilterConfig.getUseFoldChange(), preFilterConfig.getFoldChange(),
								preFilterConfig.getpValueLotel(), preFilterConfig.getFoldChangeLotel(),
								preFilterConfig.getOutputName(), preFilterConfig.getNumberOfThreads(),
								preFilterConfig.getlotelTest().equals(2), modelsToRun,
								cfpreCfg.getBmrFactor(), cfpreCfg.getPoly2BmrFactor(), constV, project));
				System.out.println();
			}
		}
		else if (preFilterConfig instanceof OriogenConfig)
		{
			OriogenRunner oriogenRunner = new OriogenRunner();

			if (preFilterConfig.getInputName() != null)
				stdoutInfo = "Oriogen Test";
			else
				stdoutInfo = "Oriogen Test";

			System.out.println("Starting " + stdoutInfo);
			for (IStatModelProcessable processable : processables)
			{
				System.out.println("Starting " + stdoutInfo + ": " + processable.getDataSetName());
				project.getOriogenResults()
						.add(oriogenRunner.runOriogenFilter(processable, preFilterConfig.getpValueCutoff(),
								preFilterConfig.getUseMultipleTestingCorrection(),
								((OriogenConfig) preFilterConfig).getMpc(),
								((OriogenConfig) preFilterConfig).getInitialBootstraps(),
								((OriogenConfig) preFilterConfig).getMaxBootstraps(),
								((OriogenConfig) preFilterConfig).getS0Adjustment(),
								preFilterConfig.getFilterOutControlGenes(),
								preFilterConfig.getUseFoldChange(), preFilterConfig.getFoldChange(),
								preFilterConfig.getpValueLotel(), preFilterConfig.getFoldChangeLotel(),
								preFilterConfig.getOutputName(), preFilterConfig.getNumberOfThreads(),
								preFilterConfig.getlotelTest().equals(2), project));
			}
		}
		System.out.println("Finished " + stdoutInfo);
	}

	private void doExpressionConfig(ExpressionDataConfig expressionConfig) throws Exception
	{

		if (veryverbose)
			System.out.println(getJsonForObject(expressionConfig));

		// if the inputfilename is a directory, then loop through each file
		// in the directory and import it as a doseresponse experiment.
		if (new File(expressionConfig.getInputFileName()).isDirectory())
		{
			System.out
					.println("Import Expression Data From Directory: " + expressionConfig.getInputFileName());
			for (final File fileEntry : new File(expressionConfig.getInputFileName()).listFiles())
			{
				System.out.println("Import Expression Data File From Directory: " + fileEntry.getName());
				if (fileEntry.isDirectory())
					continue;
				// the name stored in project bm2 file should be name of file without the extension
				String outname = FilenameUtils.removeExtension(fileEntry.getName());
				project.getDoseResponseExperiments()
						.add((new ExpressionImportRunner()).runExpressionImport(fileEntry,
								expressionConfig.getPlatform(), outname,
								expressionConfig.getLogTransformation(), expressionConfig.getHasHeaders()));
			}

		}
		else
		{
			File inputFile = new File(expressionConfig.getInputFileName());
			System.out.println("Import Expression Data File From Directory: " + inputFile.getName());
			String outname = FilenameUtils.removeExtension(inputFile.getName());

			// if config file outputname is set, then override the default
			if (expressionConfig.getOutputName() != null)
				outname = expressionConfig.getOutputName();
			project.getDoseResponseExperiments()
					.add(new ExpressionImportRunner().runExpressionImport(inputFile,
							expressionConfig.getPlatform(), outname, expressionConfig.getLogTransformation(),
							expressionConfig.getHasHeaders()));
		}

	}

	// deserialize the json file and return the RunConfig object
	private RunConfig getRunConfig(String configFile) throws Exception
	{
		return new ObjectMapper().readValue(new File(configFile), RunConfig.class);

	}

	private String getJsonForObject(Object obj) throws Exception
	{
		return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(obj);
	}
}
