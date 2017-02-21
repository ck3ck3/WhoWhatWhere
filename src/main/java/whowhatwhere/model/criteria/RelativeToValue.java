package whowhatwhere.model.criteria;

public enum RelativeToValue
{
	LESS_THAN("\u2264"), EQUALS("=="), GREATER_THAN("\u2265");
	
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
