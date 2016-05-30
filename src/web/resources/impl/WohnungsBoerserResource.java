package web.resources.impl;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.VCARD;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import data.ResultMap;
import web.parsing.ResourceParser;
import web.resources.SingleWebResource;
import web.resources.SingleWebResource.WebResourceType;


public class WohnungsBoerserResource extends SingleWebResource
{
	private static final String	REQUEST_URL		= "http://www.wohnungsboerse.net/mietspiegel-Leipzig/7390";

	private static final String	LOCAL_RESOURCE	= "/Users/Tu/Documents/Hochschule/Master/Semantic Web/doc.htm";

	Model						_model;

	public WohnungsBoerserResource(Model model)
	{
		_model = model;
	}

	@Override
	public void startWorkflow() throws Exception
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try
		{
			executeRequest(REQUEST_URL, out);
			executeGeneralWorkflow(out);
		}
		catch (Exception e)
		{
			out = new ByteArrayOutputStream();
			executeFallback();
			executeGeneralWorkflow(out);
		}
		finally
		{
			out.flush();
			out.close();
		}
	}

	private void executeFallback()
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		FileInputStream input;
		try
		{
			input = new FileInputStream(new File(LOCAL_RESOURCE));
			IOUtils.copy(input, out);
			executeGeneralWorkflow(out);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void executeGeneralWorkflow(ByteArrayOutputStream out)
	{
		try
		{
			String contentString = new String(out.toByteArray(), "UTF-8");
			Element rootElement = (Element) ResourceParser.parseResource(contentString, WebResourceType.HTML_DOC);

			_data = new ResultMap();

			Element table = rootElement.select(".rentindexDistrict_table").get(0);
			Elements tableRows = table.select("tr");

			// iterate through table rows
			for ( Element tableRow : tableRows )
			{
				Elements tableColumns = tableRow.getElementsByTag("td");

				if ( (tableColumns.size() % 2) == 0 )
				{
					for ( int i = 0; i < tableColumns.size(); i = i + 2 )
					{
						String district = tableColumns.get(i).text();
						String value = tableColumns.get(i + 1).text();
						List<Object> values = new ArrayList<Object>();
						values.add(value);

						Resource districtResource = _model.createResource(district);
						districtResource.addProperty(ResourceFactory.createProperty("district"), district);
						
					}
				}
			}

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
