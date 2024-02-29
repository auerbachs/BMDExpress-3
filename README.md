# BMDExpress-3

Software uses the dose-response models from the EPA BMDS software and ToxicR (developed by Matt Wheeler (matt.wheeler@nih.gov) to analyze high dimensional dose-response data, in particular gene expression data. The outputs of the software are gene and gene set level benchmark dose values.

Jason Phillips (jason.phillips@sciome.com) was the software engineer on BMDExpress 3 and Dan Svoboda (daniel.svoboda@sciome.com) was the primary design engineer. Scott Auerbach (auerbachs@niehs.nih.gov) led the project team.

The software is derivation of efforts by Longlong Yang (longlong.yang@nih.gov) that was originally released in 2007. (https://bmcgenomics.biomedcentral.com/articles/10.1186/1471-2164-8-387). It was updated in 2018 through a collaboration involving NTP, Sciome, Health Canada and the US EPA (https://www.ncbi.nlm.nih.gov/pmc/articles/PMC6513160/). 

[BMDExpress 1.0 is available here.](https://sourceforge.net/projects/bmdexpress/)

[BMDExpress 2.0 is available here.](https://github.com/auerbachs/BMDExpress-2/wiki)

[ToxicR is available here.](https://github.com/ToxicR)

[Details on the US EPA BMDS software can be found here.](https://www.epa.gov/bmds)

[BMDExpress 3 installation packages are generated with a multi-platform installer builder from install4j.](https://www.ej-technologies.com/products/install4j/overview.html)

Updates in BMDExpress-3
=========================
- Curve fit-based prefilter to identify dose responsive features more effectively

- New maximum likelihood estimate models implimented (i.e., those used in the best model approach). These are the validated models that currently employed [EPA's BMDS software](https://www.epa.gov/bmds).

- Alternative confidence interval estimate method (Wald) that allows for more rapid curve fitting

- Laplace and MCMC Bayesian model averaging (based the approach used in ToxicR package -- https://github.com/NIEHS/ToxicR). The methods in ToxicR have undergone peer review and publsiehd [here](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC9997717/). The statistical details of the ToxicR model averaging approach has been publsihed [here](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC9799099/)

- Forward toxicokinetic modeling to estimate internal dose levels based upon external dose BMD value


Questions and Contact Information
=================================

If you still have further questions, concerns, comments or suggestions, please contact Scott Auerbach (auerbachs@niehs.nih.gov) or Jason Phillips (jason.phillips@sciome.com).

Contributors
============

-   Scott S Auerbach, National Toxicology Program (Project Lead)
-   Jason Phillips, Sciome (Lead Software Engineer)
-   Matt Wheeler, NIEHS (Developer of ToxicR and statistical modelling consultant)
-   Fred Parham, National Toxicology Program (Statistical and Modeling Consultant)
-   Dan Svoboda, Sciome (Lead Designer)
-   Shyam Patel, Sciome (Software Developer)
-   Louis Olszyk, CSRA via USEPA (Updated BMD models)
-   Arpit Tandon, Sciome (Platform Annotations)
-   Trey Saddler, National Toxicology Program (Documentation)
-   Deepak Mav, Sciome (Statistical and Modelling Consultant)
-   Carole Yauk, Health Canada (Design Consultant)
-   Byron Kuo, Health Canada (Design Consultant)
-   Alex Sedyh, Sciome (Statistical and Modeling Consultant)
-   Russell Thomas, US EPA, originally The Hamner Institute (BMDExpress v1 Project Lead)
-   Longlong Yang, Vistronix, originally The Hamner Institute (BMDExpress v1 Lead Software Engineer)
-   Mel Anderson, Scitovation, orignally The Hamner Institute (BMDExpress v1 contributor)
-   Jeff Gift, US EPA (Updated BMD models)
-   Allen Davis, US EPA (Updated BMD models)
-   Bruce Allen, Bruce Allen Consulting (BMDExpress v1 Modeling and Statistical Consultant)
-   B. Alex Merrick, National Toxicology Program (Contract Officer for the NTP Bioinformatics Contract)
-   Jessica Ewald (Methods consultant)
-   Testers
    -   Julia Rager (ToxStrategies)
    -   Stephen Ferguson (National Toxicology Program)
    -   Joshua Harrill (US EPA)

BMDExpress License Agreements
=============================

BMDExpress Copyright © 2015-2022 by National Institute of Environmental Health Sciences. All rights reserved.

BMDExpress Copyright © 2007-2015 by The Hamner Institutes. All rights reserved.

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
