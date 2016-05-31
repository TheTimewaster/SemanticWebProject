package proj.web.resources;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import proj.data.ResultMap;
import proj.web.workflow.WorkflowInterruptedException;


public abstract class SingleWebResource
{
	protected String	_url;
	protected ResultMap	_data;

	protected void executeRequest(String url, OutputStream out) throws ClientProtocolException, IOException
	{
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet getRequest;

		getRequest = new HttpGet(url);

		HttpResponse response = client.execute(getRequest);

		InputStream is = response.getEntity().getContent();
		IOUtils.copy(is, out);
	}

	protected void executePostRequest(String url, OutputStream out, String requestBody)
	        throws ClientProtocolException, IOException
	{
		HttpClient client = HttpClientBuilder.create().build();

		HttpPost postRequest = new HttpPost(url);
		if ( requestBody != null )
		{
			HttpEntity entity = new ByteArrayEntity(requestBody.getBytes(Charset.forName("UTF-8")));
			postRequest.setEntity(entity);
		}
		
		HttpResponse response = client.execute(postRequest);

		InputStream is = response.getEntity().getContent();
		IOUtils.copy(is, out);
	}

	public ResultMap getData()
	{
		return _data;
	}

	public abstract void startWorkflow() throws WorkflowInterruptedException;

	public enum WebResourceType
	{
		HTML_DOC, JSON_OBJ, XML_DOC;
	}
}
