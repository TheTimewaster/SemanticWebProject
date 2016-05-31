package proj.web.resources.imp;


import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import proj.data.ResultMap;
import proj.web.parsing.ResourceParser;
import proj.web.resources.SingleWebResource;
import proj.web.resources.SingleWebResource.WebResourceType;
import proj.web.workflow.WorkflowInterruptedException;


public class WikipediaTableResource extends SingleWebResource
{
	private static final String	URL	= "https://de.wikipedia.org/wiki/Liste_der_Gro%C3%9Fst%C3%A4dte_in_Deutschland";

	private Element				_document;

	public WikipediaTableResource(Model model)
	{
		super(model);
	}

	@Override
	public void startWorkflow() throws WorkflowInterruptedException
	{

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try
		{
			executeRequest(URL, out);

			String response = new String(out.toByteArray(), "UTF-8");

			_document = (Element) ResourceParser.parseResource(response, WebResourceType.HTML_DOC);

			_data = new ResultMap();

			Element document = _document;

			Elements elements = document.select("table.wikitable");
			// first table is the table we want;
			Element tmp = elements.get(1);
			Elements tableRows = tmp.select("tbody tr");

			for ( Element tableRow : tableRows )
			{
				List<Object> valueList = new ArrayList<Object>();

				// skip header row
				if ( !(tableRow.getElementsByTag("th").size() > 0) )
				{
					Elements tableColumns = tableRow.getElementsByTag("td");
					String key = tableColumns.get(0).text().replaceAll("\\d", "");
					String value = tableColumns.get(9).textNodes().get(0).text().replaceAll("\\.", "");

					valueList.add(value);

					_data.put(key, valueList);
				}
			}
		}
		catch (Exception e)
		{
			_data.clear();
			throw new WorkflowInterruptedException(e);
		}
	}
}
