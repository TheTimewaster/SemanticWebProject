package web.crawler;


import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


/**
 * The WebCrawler should run multiple Threads to crawl and parse data to save
 * time. There should be no problem to sent multiple web requests to multiple
 * targets. The targets should come from a configuration file (JSON or config
 * maybe?).
 */
public class WebCrawler
{
	ByteArrayOutputStream _stream;

	List<WebRequestThread> _threads;

	/**
	 * Single uri constructor
	 * 
	 * @param uri
	 */
	public WebCrawler(String uri)
	{
		_stream = new ByteArrayOutputStream();
		_threads = new ArrayList<WebRequestThread>();

		WebRequestThread webRequestThread = new WebRequestThread(uri, _stream);
		_threads.add(webRequestThread);
	}

	/**
	 * Multiple uri constructor
	 * 
	 * @param uris
	 */
	public WebCrawler(List<String> uris)
	{
		for (String uri : uris)
		{
			// WebRequestThread webRequestThread = new WebRequestThread(uri,
			// out);
		}
	}

	/**
	 * This method executes the web request and returns the content of the
	 * response as string
	 * 
	 * @return the content of the response as string
	 * @throws UnsupportedEncodingException
	 */
	public String executeRequest() throws UnsupportedEncodingException
	{
		for (WebRequestThread thread : _threads)
		{
			thread.run();
		}

		return new String(_stream.toByteArray(), "UTF-8");
	}

}
