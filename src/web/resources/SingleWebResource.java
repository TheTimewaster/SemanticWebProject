package web.resources;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.nodes.Element;

import web.parsing.HtmlParser;


public abstract class SingleWebResource
{	
	protected String _url;
	
	protected void executeRequest(String url, OutputStream out)
	{
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet getRequest;
		
		try
		{
			getRequest = new HttpGet(url);
			
			HttpResponse response = client.execute(getRequest);
			
			InputStream is = response.getEntity().getContent();
			IOUtils.copy(is, out);
		} 
		catch (ClientProtocolException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public abstract Map<String, List<Object>> extractData();
	
	public abstract void startWorkflow() throws Exception;

	public enum WebResourceType
	{
		HTML_DOC, JSON_OBJ, XML_DOC;
	}
}
