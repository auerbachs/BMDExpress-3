---
title: Command Line
nav_order: 9
---

## Overview

The BMDExpress 3 Command Line gives access to the full workflow of the BMDExpress application without having to navigate through a GUI. The results produced from the command line version can then be easily imported back into the application if the need arises. The command line can be run in 4 different modes: ``analyze``, ``query``, ``export``, and ``delete``. 

### Video Tutorials

[Accessing the command line version](https://www.youtube.com/watch?v=NZgHnV3ZXaw&index=17&list=PLX2Rd5DjtiTeR84Z4wRSUmKYMoAbilZEc)

[Analyze data with command line version](https://www.youtube.com/watch?v=HJPcigf4dTE&t=0s&list=PLX2Rd5DjtiTeR84Z4wRSUmKYMoAbilZEc&index=18)

[Export, delete and query](https://www.youtube.com/watch?v=9W_Vkc7UJo4&index=19&list=PLX2Rd5DjtiTeR84Z4wRSUmKYMoAbilZEc)


## Analyze


### Example Configuration Files

-   [README](assets/images/files/README)

-   [Example config file that reads expression data from a directory. ](assets/images/files/analyze-config1.json)

-   [Example config file that reads expression data from specified files. ](assets/images/files/analyze-config2.json)

-   [Example config file that reads expression data from specified files. ](assets/images/files/analyze-config3.json)

-   [Example config file representing DTT workflow ](assets/images/files/analyze-DTT.json)

-   [Example config file representing ETAP workflow ](assets/images/files/analyze-ETAP.json)

-   [Defined category analysis files to use with example analyze config files. ](assets/images/files/defined-category-for-example-cli-analyze.zip)

-   [Expression data files to use with example analyze config files. ](assets/images/files/expression-data-for-example-cli-analyze.zip)



``bmdexpress3-cmd analyze --config-file``

``--config-file``

The configuration file has five main configuration sections which are ``expressionDataConfigs``, ``preFilterConfigs``, ``bmdsConfigs``, ``maConfigs`` and ``categoryAnalysisConfigs``.

The ``expressionDataConfigs`` section is used to detail the initial input file containing the expression data. It contains fields to describe the platform the data comes from as well as whether or not the data was log transformed or not. Each instance of this section will create a single expression data output in the final bm2 file. 

The next section is the ``preFilterConfigs`` section. This section is used to describe the different prefilters that should be used on the initial expression data before running the BMD Analysis. There is a choice between three different prefilters which are ``anova``, ``williams``, ``oriogen``, and ``curvefit``.  Each instance of this section will create a single prefilter output corresponding to the type of prefilter chosen. 

The next section is the ``bmdsConfigs`` which outlines the details for the MLE BMDAnalysis. This section has three different subsections which are the ``modelConfigs``, ``bmdsBestModelSelection``, and ``bmdsInputConfigs``.  The ``modelConfigs`` subsection defines the models to run.  The ``bmdsBestModelSelection`` subsection defines the parameters that are used to determine how the best model is selected.  The ``bmdsInputConfigs`` subsection defines input parameters that are fed to the individual models during execution.  Each instance of the overall ``bmdsConfigs`` section will generate a Benchmark Dose Analysis output in the bm2 file.

The next section is the ``maConfigs`` which outlines the details for the Model Averaging BMDAnalysis. This section has two different subsections which are the ``modelConfigs`` and ``bmdsInputConfigs``.  The ``modelConfigs`` subsection defines the models to run.  The ``bmdsInputConfigs`` subsection defines input parameters that are fed to the individual models during execution.  Each instance of the overall ``maConfigs`` section will generate a Benchmark Dose Analysis output in the bm2 file.

The last section is the ``categoryAnalysisConfigs`` which is used to define any category analyses you would like to compute on any previously generated BMD Analysis. There is a choice between three different category analysis types which are ``defined``, ``go``, and ``pathway``. Each instance of this section will take a specified BMD Analysis and generate a single Functional Classification output in the bm2 file.

The full list of fields for each section can be found below:

(* indicate required fields)
* ``bm2FileName`` - The file name of the bm2 file that will be outputted
* ``jsonExportFileName`` - The file name of the json export file
* ``overwrite`` - Whether to overwrite the contents of the specified bm2 files if it already exists, choice between (``true`` | ``false``)
* ``basePath`` – Override the default base path for storing meta data and annotation files for bmdexpress3. The default location is in the user home directory under "bmdexpress3."  This option allows localization of file input/output for running many instances of the application in massive parallel on many compute nodes.
* ``expressionDataConfigs``
  * ``inputFileName`` - The file or directory with files to import
  * ``hasHeaders`` - Whether the input file(s) have a header row, choice between (``true`` | ``false``)
  * ``logTransformation`` - Log transformation that has been applied to the input data, choice between (``NONE`` | ``BASE2`` | ``BASE10`` | ``NATURAL``)
  * ``outputName`` - If not specified, the imported filename will be used
  * ``platform`` - The name of the platform ([Use ID column from annotations table](https://github.com/auerbachs/BMDExpress-3/wiki/Additional-Information#supported-microarrays))
* ``preFilterConfigs``
  * ``@type`` - Choice between (``anova`` | ``williams`` | ``oriogen`` | ``curvefit``)
  * ``inputName`` - If not specified, runs filter on all expression data sets
  * ``outputName`` - If not specified, assigns a default name
  * ``pValueCutoff`` - Numeric value between 0 - 1. Only results meeting this criteria will be outputted.
  * ``useMultipleTestingCorrection`` - Whether the ``pValueCutoff`` should be applied to the adjusted p-value, choice between (``true``=adjusted | ``false``=unadjusted)
  * ``filterOutControlGenes` - Choice between (``true`` | ``false``). Only results meeting this criteria will be outputted.
  * ``useFoldChange`` - Choice between (``true`` | ``false``). Only results meeting this criteria will be outputted.
  * ``foldChange`` - Numeric value
  * ``foldChangeLotel`` - Numeric value
  * ``pValueLotel`` - Numeric value
  * ``lotelTest`` - Choice between (``1`` | ``2``)
      * ``1`` - Dunnett's Test
      * ``2`` - T-Test
  * ``numberOfThreads`` - Numeric value
  * ``numberOfPermutations`` - Integer value [Only for Williams]
  * ``initialBootstraps`` - Integer value [Only for Oriogen]
  * ``maxBootstraps`` - Integer value [Only for Oriogen]
  * ``s0Adjustment`` - Numeric value [Only for Oriogen]


  * ``modelConfigs`` - BMDS models to use [Only for CurveFit]
    * ``@type`` - Choice between (``poly`` | ``power`` | ``exp`` | ``hill``)
    * ``expModel`` - Choice between (``3`` | ``5``) [Only for exponential]
    * ``degree`` - Choice between (``1``) [Only for poly, only linear supported]
  * ``constantVariance`` - Choice between (``true`` | ``false``) [Only for CurveFit]
  * ``bmrFactor`` - Numeric Value [Only for CurveFit]
  * ``poly2BmrFactor`` - Numeric Value [Only for CurveFit]
* ``bmdsConfigs``
  * ``modelConfigs``
    * ``@type`` - Choice between (``poly`` | ``power`` | ``exp`` | ``hill``)
    * ``expModel`` - Choice between ( ``3`` | ``5``) [Only for exponential] 
    * ``degree`` - Choice between (``1`` | ``2`` | ``3`` | ``4``) [Only for poly]
  * ``bmdsBestModelSelection``
    * ``bestPolyTest`` - Choice between (``1`` | ``2``)
      * ``1`` - Nested Chi Square
      * ``2`` - Lowest AIC
    * ``pValueCutOff`` - Numeric value
    * ``flagHillWithKParameter`` - Choice between (``true`` | ``false``)
    * ``kParameterValue`` - Choice between (``1`` | ``2`` | ``3``)
      * ``1`` - Lowest Positive Dose
      * ``2`` - 1/2 of Lowest Positive Dose
      * ``3`` - 1/3 of Lowest Positive Dose
    * ``bestModelSelectionWithFlaggedHill`` - Choice between (``1`` | ``2`` | ``3`` | ``4`` | ``5``)
      * ``1`` - Include Flagged Hill
      * ``2`` - Exclude Flagged Hill from Best Models
      * ``3`` - Exclude All Hill from Best Models 
      * ``4`` - Modify BMD if Flagged Hill as Best Model
      * ``5`` - Select Next Best Model with P-Value > 0.05
    * ``modifyFlaggedHillWtihFractionMinBMD`` - Numeric value
  * ``bmdsInputConfig``
    * ``constantVariance`` - Choice between (``true`` | ``false``)
    * ``bmrFactor`` - Numeric value
    * ``bmdUBmdLEstimationMethod`` Choice between (``1`` | ``2``) [1 for Profile Likelihood; 2 for Wald, Ewald Method] (Default = 1)
    * ``stepFunctionThreshold`` - Numberic value
  * ``inputCategory`` - Choice between (``curvefit_prefilter`` | ``anova`` | ``williams`` | ``oriogen`` | ``expression``)
  * ``inputName`` - If not specified, runs all ``inputCategory`` datasets
  * ``outputName`` - If not specified, outputs default name
  * ``numberOfThreads`` - Integer value
* ``maConfigs``
  * ``method`` - Choice between (``1`` | ``2``) [1 for laplace; 2 for MCMC] (Default 1)
  * ``modelConfigs``
    * ``@type`` - Choice between (``power`` | ``exp`` | ``hill``)
    * ``expModel`` - Choice between ( ``3`` | ``5``) [Only for exponential] 
  * ``bmdsInputConfig``
    * ``constantVariance`` - Choice between (``true`` | ``false``) 
    * ``bmrFactor`` - Numeric value
    * ``stepFunctionThreshold`` - Numberic value
  * ``inputCategory`` - Choice between (``curvefit_prefilter`` | ``anova`` | ``williams`` | ``oriogen`` | ``expression``)
  * ``inputName`` - If not specified, runs all ``inputCategory`` datasets
  * ``outputName`` - If not specified, outputs default name
  * ``numberOfThreads`` - Integer value
* ``categoryAnalysisConfigs``
  * ``@type`` - Choice between (``gene`` | ``defined`` | ``go`` | ``pathway``)
  * ``inputName`` - If not specified, then all bmd analysis is run
  * ``outputName`` - If not specified, then default name is assigned
  * ``removePromiscuousProbes`` - Choice between (``true`` | ``false``). Only probes meeting this criteria will be included in the analysis.
  * ``removeBMDGreaterHighDose`` - Choice between (``true`` | ``false``). Only probes meeting this criteria will be included in the analysis.
  * ``identifyConflictingProbeSets`` - Choice between (``true`` | ``false``)
  * ``correlationCutoffForConflictingProbeSets`` - Numeric value
  * ``bmdPValueCutoff`` - Numeric value
  * ``bmdRSquaredCutoff`` - Numberic value
  * ``bmdBMDLRatioMin`` - Numeric value
  * ``bmduBMDRatioMin`` - Numeric value
  * ``bmduBMDLRatioMin`` - Numeric value
  * ``nFoldBelowLowestDose`` - Numeric value
  * ``minGenesInSet`` - Integer value
  * ``maxGenesInSet`` - Integer value
  * ``removeStepFunction`` - Choice between (``true`` | ``false``)
  * ``removeStepFunctionWithBMDLower``  - Choice between (``true`` | ``false``)
  * ``removeAdverseDirection`` - Choice between (``1`` | ``-1`` | ``0`` | ``null``) [1 for UP; -1 for DOWN; 0 or null both directions;]
  * ``maxFoldChange`` - Numeric value
  * ``prefilterPValueMin`` - Numeric value
  * ``probeFilePath`` - File path to probe to gene mapping [Only for defined]
  * ``categoryFilePath`` - File path to the gene to category mapping [Only for defined]
  * ``deduplicateGeneSets`` - Choice between (``true`` | ``false``)
  * ``goCategory`` - Choice between (``universal`` | ``biological_process`` | ``molecular_function`` | ``cellular components``) [Only for GO]
  * ``signalingPathway`` - Choice between (``REACTOME`` | ``BIOPLANET``) [Only for pathway]




## Query

The query command lets you print the names of all analyses from a given group out to the command line. This command takes two inputs, ``analysis-group``, ``input-bm2`` with the following form:

``bmdexpress3-cmd query --analysis-group <GROUP> --input-bm2 <BM2FILE>``

``input-bm2`` : This is the bm2 file that contains the project you are working on

``analysis-group``: Choice from (``expression`` | ``anova`` | ``williams`` | ``oriogen`` | ``bmd`` | ``categorical``)


## Export

The export command lets you export specific analyses from a given project out to a file. This command takes three inputs, ``analysis-group``, ``input-bm2``, ``output-file``, with an optional fourth, ``analysis-name``. If the ``analysis-name`` is left out, then all the analyses in the group are exported into a single file. The specific command has the following form:

``bmdexpress3-cmd export --analysis-group <GROUP> [--analysis-name <NAME>] --input-bm2 <BM2FILE> --output-file-name <OUTPUT>``

``input-bm2`` : This is the bm2 file that contains the project you are working on

``analysis-group``: Choice from (``expression`` | ``anova`` | ``williams`` | ``oriogen`` | ``bmd`` | ``categorical``)

``analysis-name``: The name of the specific analysis that you want to delete

``output-file-name``: The name of the file that will be output



## Delete

The delete command lets you delete specific analyses from a given project. This command takes three inputs, ``analysis-group``, ``analysis-name``, and ``input-bm2`` in the following form:

``bmdexpress3-cmd delete --analysis-group <GROUP> --analysis-name <NAME> --input-bm2 <BM2FILE>``

``input-bm2`` : This is the bm2 file that contains the project you are working on

``analysis-group``: Choice from (``expression`` | ``anova`` | ``williams`` | ``oriogen`` | ``bmd`` | ``categorical``)

``analysis-name``: The name of the specific analysis that you want to delete



## Combine

The combine command lets you combine all contents of multiple bm2 files into one bm2 file. This command takes two inputs, ``input-bm2-files`` and ``output-file-name`` in the following form:

``input-bm2-files``: List the bm2 files to combine

``output-file-name``: The file name of the combined bm2 file that will be outputted
