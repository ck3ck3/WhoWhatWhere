package mostusedips.controller.commands.trace;

public class IPHostAndLabel
{
    private String ip;
    private String hostname;
    private char label;

    public IPHostAndLabel()
    {
	
    }
    
    public IPHostAndLabel(String ip, String hostname, char label)
    {
	this.ip = ip;
	this.hostname = hostname;
	this.label = label;
    }
    
    
    public String getIp()
    {
	return ip;
    }
    public void setIp(String ip)
    {
	this.ip = ip;
    }
    public String getHostname()
    {
	return hostname;
    }
    public void setHostname(String hostname)
    {
	this.hostname = hostname;
    }
    public char getLabel()
    {
	return label;
    }
    public void setLabel(char label)
    {
	this.label = label;
    }
}
