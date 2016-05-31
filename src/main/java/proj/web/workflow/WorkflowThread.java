package proj.web.workflow;


import proj.web.resources.SingleWebResource;
import proj.web.workflow.WorkflowInterruptedException;


public class WorkflowThread implements Runnable
{
	SingleWebResource _source;

	public WorkflowThread(SingleWebResource source)
	{
		_source = source;
	}

	@Override
	public void run()
	{
		try
		{
			_source.startWorkflow();
		}
		catch (WorkflowInterruptedException e)
		{
			e.printStackTrace();
		}
	}

}
