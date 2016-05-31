package proj.web.workflow;

public class WorkflowInterruptedException extends Throwable
{
	private static final long	serialVersionUID	= 1L;

	Exception					_cause;

	public WorkflowInterruptedException(Exception ex)
	{
		this._cause = ex;
	}

	public Exception getCause()
	{
		return _cause;
	}

}
