package web.workflow;


import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


/**
 * The WorkflowController should run multiple Threads to crawl and parse data to save
 * time. There should be no problem to sent multiple web requests to multiple
 * targets. The targets should come from a configuration file (JSON or config
 * maybe?).
 */
public class WorkflowController
{
	ByteArrayOutputStream _stream;

	List<WorkflowThread> _threads;

	/**
	 * Single uri constructor
	 * 
	 * @param uri
	 */
	public WorkflowController()
	{
		_stream = new ByteArrayOutputStream();
		_threads = new ArrayList<WorkflowThread>();

		WorkflowThread workflowThread = new WorkflowThread(_stream);
		_threads.add(workflowThread);
	}

	/**
	 * Multiple uri constructor
	 * 
	 * @param uris
	 */
	public WorkflowController(List<String> uris)
	{
		for (String uri : uris)
		{
			
		}
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
		for (WorkflowThread thread : _threads)
		{
			thread.run();
		}
	}

}
