package com.sciome.bmdexpress2.shared.eventbus.project;

import java.io.File;

import com.sciome.bmdexpress2.shared.eventbus.BMDExpressEventBase;

public class SaveProjectAsDuckDBRequestEvent extends BMDExpressEventBase<File>
{

	public SaveProjectAsDuckDBRequestEvent(File payload)
	{
		super(payload);
	}
}