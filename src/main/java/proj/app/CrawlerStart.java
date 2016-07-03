package proj.app;

import proj.web.workflow.WorkflowController;

public class CrawlerStart
{	
	public static void main(String[] args)
	{
		// TODO get uris from configuration
		WorkflowController crawler = new WorkflowController();
		crawler.executeWorkflows();	
	}
}
