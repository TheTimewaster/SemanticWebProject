package web.resources.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import data.ResultMap;
import web.parsing.ResourceParser;
import web.resources.SingleWebResource;

public class WikipediaTableResource extends SingleWebResource
{
	private static final String URL = "https://de.wikipedia.org/wiki/Liste_der_Gro%C3%9Fst%C3%A4dte_in_Deutschland";
	
	private Element _document;

	@Override
	public ResultMap extractData()
	{
		ResultMap resultMap = new ResultMap();
		
		Element document = _document;
		
		Elements elements = document.select("table.wikitable");
		// first table is the table we want;
		Element tmp = elements.get(1);
		Elements tableRows = tmp.select("tbody tr");
		
		for(Element tableRow : tableRows)
		{
			List<Object> valueList = new ArrayList<Object>();
			
			// skip header row
			if(!(tableRow.getElementsByTag("th").size() > 0))
			{
				Elements tableColumns = tableRow.getElementsByTag("td");
				String key = tableColumns.get(0).text().replaceAll("\\d", "");
				String value = tableColumns.get(9).textNodes().get(0).text().replaceAll("\\.", "");
				
				valueList.add(value);
				
				resultMap.put(key, valueList);
			}
		}
		
		return resultMap;
	}

	@Override
	public void startWorkflow() throws Exception
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		executeRequest(URL, out);
		
		String response = new String(out.toByteArray(), "UTF-8");
		
		_document = (Element) ResourceParser.parseResource(response, WebResourceType.HTML_DOC);
		
		Map<String,List<Object>> results = extractData();
		
		File toWriteFile = new File("/Users/Tu/Desktop/test.txt");
		toWriteFile.createNewFile();
		FileWriter writer = new FileWriter(toWriteFile);
		StringBuilder builder = new StringBuilder();
		
		Iterator<String> it = results.keySet().iterator();
		
		while(it.hasNext())
		{
			String key =  it.next();
			String line = key + ";";
			List<Object> values = results.get(key);
			
			for(Object o : values)
			{
				line += o.toString();
			}
			
			builder.append(line + "\r\n");
		}
		
		writer.write(builder.toString());
		
		writer.close();
	}
}