package whowhatwhere.model.criteria;

public enum RelativeToValue
{
	LESS_THAN("<"), EQUALS("=="), GREATER_THAN(">");
	
	private String sign;
	
	RelativeToValue(String sign)
	{
		this.sign = sign;
	}
	
	public String getSign()
	{
		return sign;
	}
}
