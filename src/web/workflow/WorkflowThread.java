package web.workflow;

import java.io.OutputStream;

import web.resources.SingleWebResource;
import web.resources.impl.GooglePlacesResource;
import web.resources.impl.WikipediaTableResource;

public class WorkflowThread implements Runnable
{
	String _uri;
	
	OutputStream _out;
	
	public WorkflowThread(OutputStream out)
	{
		_out = out;
	}

	@Override
	public void run()
	{
		SingleWebResource wikipediaResource = new WikipediaTableResource();
		SingleWebResource googlePlacesResource = new GooglePlacesResource();
		
		try
		{
			wikipediaResource.startWorkflow();
			googlePlacesResource.startWorkflow();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
