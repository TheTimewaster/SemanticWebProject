package web.resources.impl;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;

import javax.lang.model.element.Element;

import org.apache.commons.io.IOUtils;

import data.ResultMap;
import web.parsing.ResourceParser;
import web.resources.SingleWebResource;
import web.resources.SingleWebResource.WebResourceType;


public class AdressSearchResource extends SingleWebResource
{
	private static final String REQUEST_URL = "http://adressen.leipzig.de/";

	private static final String LOCAL_RESOURCE = "/Users/Tu/Documents/Hochschule/Master/Semantic Web/adress-search.htm";

	private Element _document;

	@Override
	public ResultMap extractData()
	{
		ResultMap results = new ResultMap();

		return null;
	}

	@Override
	public void startWorkflow() throws Exception
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try
		{
			executePostRequest(REQUEST_URL, out, "");
			executeGeneralWorkflow(out);
		}
		catch (Exception e)
		{
			out = new ByteArrayOutputStream();
			executeFallback(out);
			executeGeneralWorkflow(out);
		}
		finally
		{
			out.close();
		}

		String response = new String(out.toByteArray(), "UTF-8");

		_document = (Element) ResourceParser.parseResource(response, WebResourceType.HTML_DOC);

		ResultMap results = extractData();
	}

	private void executeFallback(OutputStream out)
	{
		FileInputStream input;
		try
		{
			input = new FileInputStream(new File(LOCAL_RESOURCE));
			IOUtils.copy(input, out);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void executeGeneralWorkflow(ByteArrayOutputStream out)
	{
		
	}

}
