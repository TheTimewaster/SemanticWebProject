package web.pages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WikipediaParser extends Parser
{

	public WikipediaParser(Element rootElement)
	{
		super(rootElement);
	}

	@Override
	public Map<String, List<Object>> extractData()
	{
		Map<String, List<Object>> resultMap = new HashMap<String, List<Object>>();
		
		Element document = super._document;
		
		Elements elements = document.select("table.wikitable");
		// first table is the table we want;
		Element tmp = elements.get(1);
		Elements tableRows = tmp.select("tbody tr");
		
		for(Element tableRow : tableRows)
		{
			// skip header row
			if(!(tableRow.getElementsByTag("th").size() > 0))
			{
				Elements tableColumns = tableRow.getElementsByTag("td");
				String key = tableColumns.get(0).text().replaceAll("\\d", "");
				String value = tableColumns.get(9).textNodes().get(0).text().replaceAll("\\.", "");
				
				System.out.println(key + "\t" + value);
			}
		}
		
		return null;
	}
}
