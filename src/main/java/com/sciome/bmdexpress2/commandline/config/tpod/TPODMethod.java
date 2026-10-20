package com.sciome.bmdexpress2.commandline.config.tpod;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use = Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@JsonSubTypes({ @Type(value = LCRD.class, name = "lcrd"), @Type(value = NthRank.class, name = "nthrank"),
		@Type(value = Lowest.class, name = "lowest"),
		@Type(value = NthPercentile.class, name = "nthpercentile") })
public abstract class TPODMethod
{

}
