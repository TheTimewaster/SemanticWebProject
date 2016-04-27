package main;

import java.io.UnsupportedEncodingException;

import org.jsoup.nodes.Element;

import web.crawler.WebCrawler;
import web.pages.WikipediaParser;
import web.parser.HtmlParser;

public class Main
{
	private static final String TEST_URI = "https://de.wikipedia.org/wiki/Liste_der_Gro%C3%9Fst%C3%A4dte_in_Deutschland";
	
	public static void main(String[] args)
	{
		// TODO get uris from configuration
		WebCrawler crawler = new WebCrawler(TEST_URI);
		
		try
		{
			String responseContent = crawler.executeRequest();
			
			HtmlParser parser = new HtmlParser();
			Element element = parser.parseHtmlString(responseContent);
			
			WikipediaParser wikiparser = new WikipediaParser(element);
			wikiparser.extractData();
		} 
		catch (UnsupportedEncodingException e)
		{
			//TODO: log this exception
			e.printStackTrace();
		}
		
		
	}
}
