---
title: How to Use the Application
nav_order: 3
---

How To Use the Application
==========================

Update Annotations
------------------

[Video tutorial demonstrating how to update annotations](https://www.youtube.com/watch?v=z1nf2GX93uk&index=3&list=PLX2Rd5DjtiTeR84Z4wRSUmKYMoAbilZEc)

<img src="assets/images/workflow/annotations/select-file-update-annotations.png" alt="main-menu/select update annotations" width="50%" align="left" style="margin-right: 15px; margin-bottom: 10px;">

Before beginning an analysis, it is recommended to update any annotation files needed for the analysis. (Click `File > Update Annotations`)

<br clear="both">

<img src="assets/images/workflow/annotations/popup-update-annotations.png" alt="Popup update annotations" width="50%" align="left" style="margin-right: 15px; margin-bottom: 10px;">

Use the checkboxes on the left side of the popup to choose which annotation(s) to update. When finished, click `Update`.

<br clear="both">

<img src="assets/images/workflow/annotations/annotations-directory-structure.png" alt="Annotations directory structure" width="50%" align="left" style="margin-right: 15px; margin-bottom: 10px;">

If you are blocked from downloading the annotations due to IT security do the following:

Step 1.  Download the entire annotation dataset @ https://apps.sciome.com/bmdexpress3/annotations.zip

Step 2. Go to the directory C:\<User>\bmdexpress3

Step 3. If there is not a "data" directory in there, create it.

Step 4. Unzip the contents of annotations.zip into C:\<User>\bmdexpress3\data\

Step 5. Make sure that the directory structure looks correct.  There should be a directory C:\<User>\bmdexpress3\data\annotations\ with contents that look like the following.

<br clear="both">

Import Dose-Response Data
-------------------------

[Video tutorial demonstrating data import into BMDExpress 2](https://www.youtube.com/watch?v=TuF31IGblnQ&list=PLX2Rd5DjtiTeR84Z4wRSUmKYMoAbilZEc&index=4)

The first step in the workflow is to import gene expression data from a tab-delimited plain text file. We recommend using log-transformed data although this is not required. Each column in the data matrix must correspond to an individual expression experiment, and the first row must contain the doses at which the corresponding sample was treated. Subsequent rows contain the data for one probe/gene. An optional header row may also be included, in which case it must be the first row and the doses must be in the second row. You will be prompted when loading the data to indicate if the first row contains sample labels. Example data files are provided in the BMDExpress 2 installation folder.

**Note:** An "off label" use of BMDExpress is to perform dose response modeling on other continuous data types (e.g., clinical chemistry). The data simply needs to be formatted in the same manner as the genomic data and loaded into the software and identified as a "generic" platform. Since the dose response data has no gene labels, functional classification analysis cannot be performed.

<br clear="both">

<img src="assets/images/workflow/expression/select-file-import-expression-data.png" alt="Select import data" width="50%" align="left" style="margin-right: 15px; margin-bottom: 10px;">

Click `File > Import Expression Data`.

<br clear="both">

<img src="assets/images/workflow/expression/popup-import-data.png" alt="Popup import data" width="50%" align="left" style="margin-right: 15px; margin-bottom: 10px;">

Navigate to, and select your data file(s). You may import multiple files at once on this screen. Then click `Open`.

<br clear="both">

<img src="assets/images/workflow/expression/popup-platform-selection.png" alt="Popup platform selection" width="50%" align="left" style="margin-right: 15px; margin-bottom: 10px;">

After the file is read by the program, an array platform will be suggested. If the suggested platform is does not match your data set, select the correct platform from the dropdown list. If your platform is not contained in our [annotation set](how-to-use-the-application#update-annotations), select "generic". Then click `OK`. If "generic" is selected, probe annotations columns will be empty in the subsequent results tables. In order to perform [Functional Classifications](functional-classifications) a [Defined Category Analysis](functional-classifications#defined-category-analysis) will need to be carried out.e

<br clear="both">

<img src="assets/images/workflow/expression/popup-log-transform.png" alt="Popup log transform" width="50%" align="left" style="margin-right: 15px; margin-bottom: 10px;">

Next, select the type of log transformation your data was prepared with.

<br clear="both">

Once the file(s) are loaded into the program, they will be displayed in the lower section of the main window. In the chart area, scatter plots of 6 pairs of principal components are shown. To identify data points in the PCA plot hold shift and click on the point of interest or mouse over the point and the sample name will pop-up.

<img src="assets/images/workflow/expression/main-expression-data-loaded.png" alt="Main expression data loaded" width="100%" style="margin-right: 15px; margin-bottom: 10px;">



<br clear="both">

<img src="assets/images/workflow/expression/popup-density-chart.png" alt="Density chart" width="50%" align="left" style="margin-right: 15px; margin-bottom: 10px;">

Density plots of the expression data can be viewed by clicking the drop down in select chart view in the upper right and selecting "Density Chart". The chart shows a global distribution of intensities/counts for each sample in the selected expression data file.

<br clear="both">

<img src="assets/images/workflow/expression/popup-node-contents.png" alt="Popup node contents" width="50%" align="left" style="margin-right: 15px; margin-bottom: 10px;">

Switch between expression data files using the data selection panel to the left. In this section only one data set at time can be loaded. In other sections of the application multiple data sets can selected and evaluated simultaneously.

<br clear="both">

Once you have loaded your data you should proceed sequentially through [Prefiltering](prefiltering), [Benchmark Dose Analysis](benchmark-dose-analysis), and [Functional Classification](functional-classifications).
