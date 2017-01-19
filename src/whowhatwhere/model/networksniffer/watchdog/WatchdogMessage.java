package whowhatwhere.model.networksniffer.watchdog;

public class WatchdogMessage
{
	private String message;
	private OutputMethod method;
	
	public WatchdogMessage(String message, OutputMethod method)
	{
		this.setMessage(message);
		this.setMethod(method);
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public OutputMethod getMethod()
	{
		return method;
	}

	public void setMethod(OutputMethod method)
	{
		this.method = method;
	}
}
