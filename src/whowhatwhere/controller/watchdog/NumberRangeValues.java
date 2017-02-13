package whowhatwhere.controller.watchdog;

import java.io.Serializable;

public class NumberRangeValues implements Serializable
{
	private static final long serialVersionUID = 7554403736198431309L; //auto-generated, modify if changes to the class are not backwards-compatible
	
	private NumberRange rangeEnum;
	private Integer leftValue;
	private Integer rightValue;
	
	public NumberRangeValues(NumberRange range, Integer left, Integer right)
	{
		rangeEnum = range;
		leftValue = left;
		rightValue = right;
	}

	public NumberRange getRange()
	{
		return rangeEnum;
	}

	public Integer getLeftValue()
	{
		return leftValue;
	}

	public Integer getRightValue()
	{
		return rightValue;
	}
}
