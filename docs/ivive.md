---
title: IVIVE
nav_order: 10
---

IVIVE
==========================

Introduction
------------

[Video demonstrating how to perform IVIVE analysis](https://www.youtube.com/watch?v=ivive-analysis&t=0s)

BMDExpress 2 provides an IVIVE workflow based on a pure java implementation of HTTK 1.8. [It utilizes both them main project methods](https://cran.r-project.org/web/packages/httk/index.html), as well as those provided by [additional contributors](https://cran.r-project.org/src/contrib/Archive/httk/).


IVIVE values are calculated using [calc_mc_oral_equiv](https://www.rdocumentation.org/packages/httk/versions/1.8/topics/calc_mc_oral_equiv). Default values for input parameters are used, with the exception of those provided by the user:
- MW 
- LogP
- pK<sub>a</sub> Donor
- pK<sub>a</sub> Acceptor
- CL<sub>int</sub>
- FU<sub>p</sub> (Fraction unbound in plasma)
- Dose Units (input in vitro units)
- Output units
- Quantile
- Species


BMDExpress 2 carries with it an embedded database of chemical identifiers, with properties (MW, LogP, pK<sub>a</sub> Donor, pK<sub>a</sub> Acceptor, CL<sub>int</sub>, FU<sub>p</sub>). It is derived from the default table provided by the [HTTK package](https://www.rdocumentation.org/packages/httk/versions/1.8), [Sipes et al. 2017](http://footnote), and [ICE 2.0 OPERA](http://reference).

To perform an IVIVE analysis, type a chemical name and/or CAS# into the search box. If the database contains a match, the remainder of the form will be filled with database values. with that chemicals properties and auto populate input fields that will then be passed to the calc_mc_oral_equiv  function and used to calculate the in-vivo oral equivalent.  

Here is the logic for retrieving a chemical’s properties from the embedded DB.
For a given chemical and a given property we first look for the property value in the default HTTK package’s table.  If the property is not there, we look for the property value in the Sipes et al. 2017 table, and finally if the property value is not there we grab the property value from ICE 2.0 OPERA predictions table.

The user has the option to override these parameters by typing their own into the text fields.
The user also has the option to simply not use the auto populate feature and type in their own custom values for their custom or non-custom chemical.

BMDExpress2 runs 3compartmentss and calculates and oral equivalent for the following summary fields in the category analysis results.

Bmd Mean
BmdL Mean
BmdU Mean
Bmd Median
BmdL Median
BmdU Median
Bmd Minimum
BmdL Minimum
BmdU Minimum
Bmd Fifth Percentile
BmdL Fifth Percentile
BmdU Fifth Percentile
Bmd Tenth Percentile
BmdL Tenth Percentile
BmdU Tenth Percentile
