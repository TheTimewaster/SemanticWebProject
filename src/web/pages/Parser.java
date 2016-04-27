package web.pages;

import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Element;

public abstract class Parser
{
	protected Element _document;
	
	public Parser(Element rootElement)
	{
		_document = rootElement;
	}
	
	public abstract Map<String, List<Object>> extractData();
}
