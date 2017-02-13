package whowhatwhere.controller.watchdog;

public enum NumberRange
{
	EQUALS("Equals"), GREATER_THAN("At least"), LESS_THAN("At most"), RANGE("Between"); 
	
	private String value;
	
	private NumberRange(String value)
	{
		this.value = value;
	}
	
	@Override
	public String toString()
	{
		return value;
	}

	public static String numberRangeStringRepresentation(NumberRange range, Number leftNum, Number rightNum)
	{
		switch(range)
		{
			case EQUALS: return leftNum.toString();
			case GREATER_THAN : return "\u2265 " + leftNum; // >=
			case LESS_THAN : return "\u2264 " + leftNum; // <=
			case RANGE: return leftNum + " - " + rightNum;
			default : return ""; //never gets here
		}
	}
}
