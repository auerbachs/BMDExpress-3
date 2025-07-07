package com.sciome.bmdexpress2.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sciome.bmdexpress2.serviceInterface.ITissueContaminationService;

public class TissueContaminationService implements ITissueContaminationService
{
	// splash.png
	private Map<String, List<String>> tissueGenesMap = new HashMap<>();

	public TissueContaminationService()
	{

		loadTissueGenes();

	}

	private void loadTissueGenes()
	{

		URL url = this.getClass().getResource("/tissues/tissues.txt");
		System.out.println("Resolved URL: " + url);

		ClassLoader cl = TissueContaminationService.class.getClassLoader();
		try (InputStream indexStream = this.getClass().getResourceAsStream("/tissues/tissues.txt"))
		{
			if (indexStream == null)
			{
				System.out.println("tissues.txt not found in resources");
				return;
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(indexStream));
			List<String> fileNames = reader.lines().toList();

			for (String fileName : fileNames)
			{

				try (InputStream fileStream = this.getClass().getResourceAsStream("/tissues/" + fileName))
				{

					BufferedReader geneReader = new BufferedReader(new InputStreamReader(fileStream));
					List<String> genes = geneReader.lines().toList();
					this.tissueGenesMap.put(fileName, genes);
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public List<String> getSortedListOfTissues()
	{
		List<String> tissues = new ArrayList<>();

		tissues.addAll(this.tissueGenesMap.keySet());

		Collections.sort(tissues);
		return tissues;
	}

	public List<String> getListOfGenesForTissue(String tissue)
	{
		List<String> genes = new ArrayList<>();
		if (this.tissueGenesMap.get(tissue) != null)
			genes.addAll(this.tissueGenesMap.get(tissue));
		return genes;

	}

}
