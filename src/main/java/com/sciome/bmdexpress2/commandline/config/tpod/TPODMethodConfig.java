package com.sciome.bmdexpress2.commandline.config.tpod;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use = Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@JsonSubTypes({ @Type(value = LCRDConfig.class, name = "lcrd"), @Type(value = NthRankConfig.class, name = "nthrank"),
		@Type(value = LowestConfig.class, name = "lowest"),
		@Type(value = NthPercentileConfig.class, name = "nthpercentile") })
public abstract class TPODMethodConfig
{

}
