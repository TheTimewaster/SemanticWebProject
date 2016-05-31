package web.workflow;


import web.resources.SingleWebResource;


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
