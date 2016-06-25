package proj.web.resources.imp;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import proj.data.ResultMap;
import proj.data.StaticProperties;
import proj.web.parsing.ResourceParser;
import proj.web.resources.SingleWebResource;
import proj.web.workflow.WorkflowInterruptedException;


public class WohnungsBoerserResource extends SingleWebResource
{
	private static final String	REQUEST_URL		= "http://www.wohnungsboerse.net/mietspiegel-Leipzig/7390";

	private static final String	LOCAL_RESOURCE	= "/Users/Tu/Documents/Hochschule/Master/Semantic Web/doc.htm";

	public WohnungsBoerserResource(Model model)
	{
		super(model);

		_model.setNsPrefix("rdf", RDF.getURI());
		_model.setNsPrefix("geo", StaticProperties.GEO_NAMESPACE_URI);
		_model.setNsPrefix("tht", StaticProperties.NAMESPACE_URI);
	}

	@Override
	public void startWorkflow() throws WorkflowInterruptedException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try
		{
			executeRequest(REQUEST_URL, out);
			executeGeneralWorkflow(out);
		}
		catch (Exception e)
		{
			// out = new ByteArrayOutputStream();
			// executeFallback();
			// executeGeneralWorkflow(out);
			e.printStackTrace();
		}
		finally
		{
			try
			{
				out.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
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
						Resource districtResource = null;
						/*if ( "Zentrum".equals(district) )
						{
							districtResource = _model.createResource(StaticProperties.NAMESPACE_DISTRICT + "-Mitte");

						}
						else */if ( district.startsWith("Anger-") )
						{
							districtResource = _model.createResource(StaticProperties.NAMESPACE_DISTRICT + "-Anger");
						}
						else if ( district.startsWith("Reudnitz-") )
						{
							districtResource = _model.createResource(StaticProperties.NAMESPACE_DISTRICT + "-Reudnitz");
						}
						else if ( district.startsWith("Sellerhausen-") )
						{
							districtResource = _model
							        .createResource(StaticProperties.NAMESPACE_DISTRICT + "-Sellerhausen");
						}
						else if ( district.startsWith("Knauthain-") )
						{
							districtResource = _model
							        .createResource(StaticProperties.NAMESPACE_DISTRICT + "-Knautkleeberg-Knauthain");
						}
						else if ( district.startsWith("Schönefeld-Abtnaundorf") )
						{
							districtResource = _model
							        .createResource(StaticProperties.NAMESPACE_DISTRICT + "-Abtnaundorf");
						}
						else if ( district.startsWith("Neustadt-") )
						{
							districtResource = _model
							        .createResource(StaticProperties.NAMESPACE_DISTRICT + "-Neustadt");
						}
						else if ( district.startsWith("Südvorstadt") )
						{
							districtResource = _model
							        .createResource(StaticProperties.NAMESPACE_DISTRICT + "-Sudvorstadt");
						}
						else
						{
							districtResource = _model
							        .createResource(StaticProperties.NAMESPACE_DISTRICT + "-" + district);
						}

						districtResource.addProperty(_model.createProperty(StaticProperties.NAMESPACE_NAME), district);
						districtResource.addProperty(_model.createProperty(StaticProperties.NAMESPACE_RENT), value);
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
