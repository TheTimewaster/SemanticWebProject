package web.crawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

public class WebRequestThread implements Runnable
{
	String _uri;
	
	OutputStream _out;
	
	public WebRequestThread(String uri, OutputStream out)
	{
		_out = out;
		_uri = uri;
		
	}

	@Override
	public void run()
	{
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet getRequest;
		try
		{
			getRequest = new HttpGet(new URI(_uri));
			
			HttpResponse response = client.execute(getRequest);
			
			InputStream is = response.getEntity().getContent();
			IOUtils.copy(is, _out);
		} 
		catch (URISyntaxException e)
		{
			e.printStackTrace();
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

}
