package proj.web.parsing;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author Tu
 *
 */
public class HtmlParser
{
	Element _rootElement;

	public Element parseHtmlString(String rootDocument)
	{
		_rootElement = Jsoup.parse(rootDocument);
		
		return _rootElement;
	}
	
	/**
	 * This method selects elements from the DOM-tree with CSS-selectors
	 * 
	 * @param selector
	 * @return multiple Elements
	 */
	public Elements selectElement(String selector) throws ElementNotFoundException
	{
		if(_rootElement.select(selector).size() == 0)
		{
			throw new ElementNotFoundException("No elements found. Selector: " + selector);
		}
		else
		{
			return _rootElement.select(selector);
		}
	}

}
