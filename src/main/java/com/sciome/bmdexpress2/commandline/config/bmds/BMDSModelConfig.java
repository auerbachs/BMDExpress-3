package com.sciome.bmdexpress2.commandline.config.bmds;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.sciome.bmdexpress2.commandline.config.nonparametric.GCurvePConfig;

@JsonTypeInfo(use = Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@JsonSubTypes({ @Type(value = PolyConfig.class, name = "poly"),
		@Type(value = PowerConfig.class, name = "power"),
		@Type(value = ExponentialConfig.class, name = "exp"), @Type(value = HillConfig.class, name = "hill"),
		@Type(value = GCurvePConfig.class, name = "gcurvep") })
public abstract class BMDSModelConfig
{

}
