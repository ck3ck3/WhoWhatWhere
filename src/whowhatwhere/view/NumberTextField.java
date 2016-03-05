package whowhatwhere.view;

import java.util.ArrayList;
import java.util.List;

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
	private List<String> allowedWords;
	private ChangeListener<Boolean> focusedPropertyListenr;

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

		focusedPropertyListenr = generateFocusedPropertyListenr();
		this.focusedProperty().addListener(focusedPropertyListenr);
	}
	
	public void removeFocusValidator()
	{
		this.focusedProperty().removeListener(focusedPropertyListenr);
	}
	
	public void setFocusValidator()
	{
		this.focusedProperty().addListener(focusedPropertyListenr);
	}
	
	protected ChangeListener<Boolean> generateFocusedPropertyListenr()
	{
		return new ChangeListener<Boolean>()
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

						String msg = "Input \"" + getText() + "\" is not a number " + validRange;
						if (allowedWords != null)
							msg += ", or one of the allowed values " + getStringOfAllowedWords();
						
						Alert alert = new Alert(AlertType.ERROR, msg);
						alert.showAndWait();
						requestFocus();
					}
				}
			}

			private String getStringOfAllowedWords()
			{
				StringBuilder builder = new StringBuilder("(");
				
				for (String word : allowedWords)
					builder.append("\"" + word + "\", ");
				
				builder.delete(builder.lastIndexOf(", "), builder.length());
				builder.append("}");
				
				return builder.toString();
			}
		};
	}

	protected boolean validate(String text)
	{
		if (text.isEmpty())
			return allowEmpty;

		if (allowedWords != null && allowedWords.contains(text))
			return true;
		
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
	
	public boolean isValidText()
	{
		return validate(getText());
	}
	
	public void setAllowedWords(List<String> words)
	{
		allowedWords = new ArrayList<>(words);
	}
	
	public List<String> getAllowedWords()
	{
		return allowedWords;
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
