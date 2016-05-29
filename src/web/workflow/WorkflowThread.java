package web.workflow;

import java.io.OutputStream;

import web.resources.SingleWebResource;
import web.resources.impl.GooglePlacesResource;
import web.resources.impl.WikipediaTableResource;
import web.resources.impl.WohnungsBoerserResource;

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
//		SingleWebResource wikipediaResource = new WikipediaTableResource();
//		SingleWebResource googlePlacesResource = new GooglePlacesResource();
		SingleWebResource wohnungsBoerseResource = new WohnungsBoerserResource();
		
		try
		{
//			wikipediaResource.startWorkflow();
//			googlePlacesResource.startWorkflow();
			wohnungsBoerseResource.startWorkflow();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
