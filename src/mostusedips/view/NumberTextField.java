package mostusedips.view;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;

public class NumberTextField extends TextField
{
	private Integer minValue = null;
	private Integer maxValue = null;
	private boolean allowEmpty = false;

	public NumberTextField()
	{
		this(null, null, null);
	}

	public NumberTextField(String text)
	{
		this(text, null, null);
	}

	public NumberTextField(String text, Integer minValue)
	{
		this(text, minValue, null);
	}
	
	public NumberTextField(Integer minValue)
	{
		this("", minValue, null);
	}
	
	public NumberTextField(Integer minValue, Integer maxValue)
	{
		this("", minValue, maxValue);
	}

	public NumberTextField(String text, Integer minValue, Integer maxValue)
	{
		super(text);
		this.setMinValue(minValue);
		this.setMaxValue(maxValue);

		this.focusedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
			{
				if (!newPropertyValue) //focus out
				{
					if (!validate(getText()))
					{
						String validRange = "";

						if (minValue != null && maxValue != null)
							validRange = "between " + minValue + " and " + maxValue;
						else
							if (minValue != null)
								validRange = ">= " + minValue;
							else
								if (maxValue != null)
									validRange = "<= " + maxValue;

						Alert alert = new Alert(AlertType.ERROR, "Input \"" + getText() + "\" is not a number " + validRange);
						alert.showAndWait();
						requestFocus();
					}
				}
			}
		});
	}

	private boolean validate(String text)
	{
		if (text.isEmpty())
			return allowEmpty;

		int value;
		try
		{
			value = Integer.valueOf(text);
		}
		catch (NumberFormatException nfe) //not a number
		{
			return false;
		}

		if (getMinValue() != null && value < getMinValue())
			return false;

		if (getMaxValue() != null && value > getMaxValue())
			return false;

		return true;
	}

	public Integer getValue()
	{
		String text = getText();
		
		return (text == null || text.isEmpty() ? null : Integer.valueOf(text));
	}

	public Integer getMinValue()
	{
		return minValue;
	}

	public void setMinValue(Integer minValue)
	{
		this.minValue = minValue;
	}

	public Integer getMaxValue()
	{
		return maxValue;
	}

	public void setMaxValue(Integer maxValue)
	{
		this.maxValue = maxValue;
	}

	public boolean isAllowEmpty()
	{
		return allowEmpty;
	}

	public void setAllowEmpty(boolean allowEmpty)
	{
		this.allowEmpty = allowEmpty;
	}
}
