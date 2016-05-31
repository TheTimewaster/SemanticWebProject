package web.workflow;


import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import web.resources.impl.GooglePlacesResource;
import web.resources.impl.WohnungsBoerserResource;


/**
 * The WorkflowController should run multiple Threads to crawl and parse data to
 * save time. There should be no problem to sent multiple web requests to
 * multiple targets. The targets should come from a configuration file (JSON or
 * config maybe?).
 */
public class WorkflowController
{
	ByteArrayOutputStream	_stream;

	List<WorkflowThread>	_threads;

	/**
	 * Single uri constructor
	 * 
	 * @param uri
	 */
	public WorkflowController()
	{
		_stream = new ByteArrayOutputStream();
		_threads = new ArrayList<WorkflowThread>();

		Model model = ModelFactory.createDefaultModel();

		WorkflowThread workflowThread1 = new WorkflowThread(new WohnungsBoerserResource(model));
//		WorkflowThread workflowThread2 = new WorkflowThread(new GooglePlacesResource());

		_threads.add(workflowThread1);
//		_threads.add(workflowThread2);
	}

	/**
	 * Multiple uri constructor
	 * 
	 * @param uris
	 */
	public WorkflowController(List<String> uris)
	{

	}

	/**
	 * This method executes the web request and returns the content of the
	 * response as string
	 * 
	 * @return the content of the response as string
	 * @throws UnsupportedEncodingException
	 */
	public void executeWorkflows()
	{
		for ( WorkflowThread thread : _threads )
		{
			thread.run();
		}
	}

}
