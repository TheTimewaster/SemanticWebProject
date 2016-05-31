package web.resources.impl;


import java.io.ByteArrayOutputStream;

import web.resources.SingleWebResource;
import web.workflow.WorkflowInterruptedException;


public class OsmOverpassResource extends SingleWebResource
{
	private final static String		URL			= "http://overpass-api.de/api/interpreter?data=area[name=Leipzig];node(area)%s;out;";

	private final static String[]	KEYWORDS	=
	{ "[shop=supermarket]", "[shop=supermarket][organic=yes]", "[shop=supermarket][organic=only]",
	        "[shop=convenience][organic=yes]", "[shop=convenience][organic=only]" };

	@Override
	public void startWorkflow() throws WorkflowInterruptedException
	{
		for ( String keyword : KEYWORDS )
		{
			try
			{
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				executePostRequest(String.format(URL, keyword), out, null);
			}
			catch (Exception e)
			{
				_data.clear();
				throw new WorkflowInterruptedException(e);
			}
			
		}
		
	}

}
