package proj.web.workflow;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import proj.web.resources.imp.GooglePlacesResource;
import proj.web.resources.imp.OsmOverpassResource;
import proj.web.resources.imp.WohnungsBoerserResource;
import proj.web.workflow.WorkflowThread;


/**
 * The WorkflowController should run multiple Threads to crawl and parse data to
 * save time. There should be no problem to sent multiple web requests to
 * multiple targets. The targets should come from a configuration file (JSON or
 * config maybe?).
 */
public class WorkflowController
{
	List<WorkflowThread>	_threads;

	Model	             _model;

	/**
	 * Single uri constructor
	 * 
	 * @param uri
	 */
	public WorkflowController()
	{
		_threads = new ArrayList<WorkflowThread>();

		_model = ModelFactory.createDefaultModel();

		WorkflowThread workflowThread1 = new WorkflowThread(new WohnungsBoerserResource(_model));
		WorkflowThread workflowThread2 = new WorkflowThread(new GooglePlacesResource(_model));
		WorkflowThread workflowThread3 = new WorkflowThread(new OsmOverpassResource(_model));

		_threads.add(workflowThread1);
		_threads.add(workflowThread2);
		_threads.add(workflowThread3);
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

		String targetFile = System.getProperty("user.home") + "/Desktop/results.xml";
		try
		{
			FileOutputStream out = new FileOutputStream(new File(targetFile));
			_model.write(out);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

}
