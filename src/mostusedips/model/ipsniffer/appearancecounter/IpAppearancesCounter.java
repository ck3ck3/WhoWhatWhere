package mostusedips.model.ipsniffer.appearancecounter;

public class IpAppearancesCounter implements Comparable<IpAppearancesCounter>
{
	private String ip;
	private int amountOfAppearances;

	public IpAppearancesCounter()
	{
	}

	public IpAppearancesCounter(String ip, int amount)
	{
		this.ip = ip;
		amountOfAppearances = amount;
	}

	public String getIp()
	{
		return ip;
	}

	public void setIp(String ip)
	{
		this.ip = ip;
	}

	public int getAmountOfAppearances()
	{
		return amountOfAppearances;
	}

	public void setAmountOfAppearances(int amountOfAppearances)
	{
		this.amountOfAppearances = amountOfAppearances;
	}

	@Override
	public int compareTo(IpAppearancesCounter o) //REVERSE ORDER, bigger numbers first
	{
		return o.getAmountOfAppearances() - this.amountOfAppearances;
	}
}
