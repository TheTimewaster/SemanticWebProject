package web.parser;

/**
 * This Exception is thrown when no elements are found on parsing process.
 * The message can contain the selector which returns no elements.
 *
 */
public class ElementNotFoundException extends Throwable
{
	private static final long serialVersionUID = 1L;
	
	private String _message;
	
	ElementNotFoundException(String message)
	{
		_message = message;
	}
	
	@Override
	public String getMessage()
	{
		return _message;
	}

}
