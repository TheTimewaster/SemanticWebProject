package main;

import java.io.UnsupportedEncodingException;

import org.jsoup.nodes.Element;

import web.parsing.HtmlParser;
import web.resources.impl.WikipediaTableResource;
import web.workflow.WorkflowController;

public class Main
{
	private static final String TEST_URI = "https://de.wikipedia.org/wiki/Liste_der_Gro%C3%9Fst%C3%A4dte_in_Deutschland";
	
	public static void main(String[] args)
	{
		// TODO get uris from configuration
		WorkflowController crawler = new WorkflowController();
		crawler.executeWorkflows();
		
	}
}
