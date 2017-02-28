/*******************************************************************************
 * Who What Where
 * Copyright (C) 2017  ck3ck3
 * https://github.com/ck3ck3/WhoWhatWhere
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package whowhatwhere.controller.watchdog;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import numbertextfield.NumberTextField;
import whowhatwhere.controller.GUIController;
import whowhatwhere.model.networksniffer.NetworkSniffer;
import whowhatwhere.model.networksniffer.PacketDirection;
import whowhatwhere.model.networksniffer.SupportedProtocols;
import whowhatwhere.model.networksniffer.watchdog.OutputMethod;
import whowhatwhere.model.networksniffer.watchdog.PacketTypeToMatch;
import whowhatwhere.model.tts.TextToSpeech;
import whowhatwhere.view.secondaryfxmlscreen.SecondaryFXMLScreen;

public class ListAddEditScreen extends SecondaryFXMLScreen
{
	private ListAddEditController watchdogListAddEditController;
	private TableView<PacketTypeToMatch> table;

	private ComboBox<OutputMethod> comboOutputMethod;
	private TextField textMessage;
	private Button btnDone;
	private ComboBox<PacketDirection> comboPacketDirection;
	private TextField textIPAddress;
	private TextField textNetmask;
	private ComboBox<String> comboIPNotes;
	private ComboBox<NumberRange> comboPacketSize;
	private NumberTextField numFieldPacketSizeLeft;
	private Label labelPacketSizeRight;
	private NumberTextField numFieldPacketSizeRight;
	private ComboBox<SupportedProtocols> comboProtocol;
	private ComboBox<NumberRange> comboSrcPort;
	private NumberTextField numFieldSrcPortLeft;
	private Label labelSrcPortRight;
	private NumberTextField numFieldDstPortRight;
	private ComboBox<NumberRange> comboDstPort;
	private NumberTextField numFieldDstPortLeft;
	private Label labelDstPortRight;
	private NumberTextField numFieldSrcPortRight;
	private CheckBox chkboxIPAddress;
	private CheckBox chkboxIPNotes;
	private CheckBox chkboxPacketDirection;
	private CheckBox chkboxProtocol;
	private CheckBox chkboxSrcPort;
	private CheckBox chkboxDstPort;
	private CheckBox chkboxPacketSize;
	private CheckBox chkboxNetmask;
	private Label labelIPRange;
	private Button btnPreview;
	private Label labelNoteCount;

	private Map<String, List<String>> ipNotesToIPListMap;
	private boolean isIPFieldValid = false;
	private TextToSpeech tts;

	public ListAddEditScreen(String fxmlLocation, Stage stage, Scene scene, TableView<PacketTypeToMatch> table, Map<String, List<String>> ipNotes, boolean isEdit, TextToSpeech tts) throws IOException
	{
		super(fxmlLocation, stage, scene);

		watchdogListAddEditController = getLoader().<ListAddEditController> getController();
		this.table = table;
		this.tts = tts;
		ipNotesToIPListMap = ipNotes;
		
		assignControlsFromController();
		initControlsBehavior(isEdit);

		if (isEdit)
			setContentForEdit(table.getSelectionModel().getSelectedItem());
		else
			comboOutputMethod.getSelectionModel().select(OutputMethod.TTS);
	}

	private void assignControlsFromController()
	{
		comboOutputMethod = watchdogListAddEditController.getComboOutputMethod();
		textMessage = watchdogListAddEditController.getTextMessage();
		btnDone = watchdogListAddEditController.getBtnDone();
		comboPacketDirection = watchdogListAddEditController.getComboPacketDirection();
		textIPAddress = watchdogListAddEditController.getTextIPAddress();
		textNetmask = watchdogListAddEditController.getTextNetmask();
		comboIPNotes = watchdogListAddEditController.getComboIPNotes();
		comboPacketSize = watchdogListAddEditController.getComboPacketSize();
		numFieldPacketSizeLeft = watchdogListAddEditController.getNumFieldPacketSizeLeft();
		labelPacketSizeRight = watchdogListAddEditController.getLabelPacketSizeRight();
		numFieldPacketSizeRight = watchdogListAddEditController.getNumFieldPacketSizeRight();
		comboProtocol = watchdogListAddEditController.getComboProtocol();
		comboSrcPort = watchdogListAddEditController.getComboSrcPort();
		numFieldSrcPortLeft = watchdogListAddEditController.getNumFieldSrcPortLeft();
		labelSrcPortRight = watchdogListAddEditController.getLabelSrcPortRight();
		numFieldDstPortRight = watchdogListAddEditController.getNumFieldDstPortRight();
		comboDstPort = watchdogListAddEditController.getComboDstPort();
		numFieldDstPortLeft = watchdogListAddEditController.getNumFieldDstPortLeft();
		labelDstPortRight = watchdogListAddEditController.getLabelDstPortRight();
		numFieldSrcPortRight = watchdogListAddEditController.getNumFieldSrcPortRight();
		chkboxIPAddress = watchdogListAddEditController.getChkboxIPAddress();
		chkboxIPNotes = watchdogListAddEditController.getChkboxIPNotes();
		chkboxPacketDirection = watchdogListAddEditController.getChkboxPacketDirection();
		chkboxProtocol = watchdogListAddEditController.getChkboxProtocol();
		chkboxSrcPort = watchdogListAddEditController.getChkboxSrcPort();
		chkboxDstPort = watchdogListAddEditController.getChkboxDstPort();
		chkboxPacketSize = watchdogListAddEditController.getChkboxPacketSize();
		chkboxNetmask = watchdogListAddEditController.getChkboxNetmask();
		labelIPRange = watchdogListAddEditController.getLabelIPRange();
		btnPreview = watchdogListAddEditController.getBtnPreview();
		labelNoteCount = watchdogListAddEditController.getLabelNoteCount();
	}

	private void initControlsBehavior(boolean isEdit)
	{
		populateCombos();
		setChangeListeners();
		setTooltipsForControls();
		setValidatorsForControls();
		setButtonHandlers(isEdit);
		GUIController.setNumberTextFieldValidationUI(numFieldDstPortLeft, numFieldDstPortRight, numFieldPacketSizeLeft, numFieldPacketSizeRight, numFieldSrcPortLeft, numFieldSrcPortRight);
	}
	
	private String getValueFromTextField(CheckBox chkbox, TextField field)
	{
		if (!chkbox.isSelected())
			return null;
	
		String value = field.getText();
					
		if (value.isEmpty())
			throw new IllegalArgumentException(chkbox.getText() + " checkbox is checked, but no value was entered. Either uncheck the checkbox or enter a value for that condition.");
		else
			return value;
	}
	
	private <T> T getValueFromComboBox(CheckBox chkbox, ComboBox<T> combo)
	{
		if (!chkbox.isSelected())
			return null;
	
		T value = combo.getValue();
					
		if (value == null)
			throw new IllegalArgumentException(chkbox.getText() + " checkbox is checked, but no value was selected for it. Either uncheck the checkbox or select a value for that condition.");
		else
			return value;
	}
	
	private NumberRangeValues getValuesFromNumberRangeControls(CheckBox chkbox, ComboBox<NumberRange> combo, NumberTextField leftField, NumberTextField rightField)
	{
		NumberRange comboValue = getValueFromComboBox(chkbox, combo);
		
		if (comboValue == null) //unchecked checkbox
			return null;
		
		Integer leftValue = leftField.getValue();
		Integer rightValue = null;

		if (leftValue == null)
			throw new IllegalArgumentException(chkbox.getText() + ": Missing value to compare to.");
				
		if (comboValue == NumberRange.RANGE)
		{
			rightValue = rightField.getValue();
			
			if (rightValue == null)
				throw new IllegalArgumentException(chkbox.getText() + ": Both values must be entered.");
			
			if (!(leftValue < rightValue))
				throw new IllegalArgumentException(chkbox.getText() + ": The left value must be less than the right value (for example, in between 500 and 1000).");
		}
		
		return new NumberRangeValues(comboValue, leftValue, rightValue);			
	}

	private void setButtonHandlers(boolean isEdit)
	{
		btnPreview.setOnAction(actionEvent ->
		{
			String text = textMessage.getText();
			
			if (text.isEmpty())
				new Alert(AlertType.ERROR, "No text to preview").showAndWait();
			else
				tts.speak(text);
		});
		
		btnDone.setOnAction(actionEvent ->
		{
			try
			{
				String ip = getValueFromTextField(chkboxIPAddress, textIPAddress);
				String netmask = getValueFromTextField(chkboxNetmask, textNetmask);
				String ipNotes = getValueFromComboBox(chkboxIPNotes, comboIPNotes);
				PacketDirection packetDirection = getValueFromComboBox(chkboxPacketDirection, comboPacketDirection);
				SupportedProtocols protocol = getValueFromComboBox(chkboxProtocol, comboProtocol);
				NumberRangeValues srcPort = getValuesFromNumberRangeControls(chkboxSrcPort, comboSrcPort, numFieldSrcPortLeft, numFieldSrcPortRight);
				NumberRangeValues dstPort = getValuesFromNumberRangeControls(chkboxDstPort, comboDstPort, numFieldDstPortLeft, numFieldDstPortRight);
				NumberRangeValues packetSize = getValuesFromNumberRangeControls(chkboxPacketSize, comboPacketSize, numFieldPacketSizeLeft, numFieldPacketSizeRight);
				
				if (isAllNull(ip, ipNotes, packetDirection, protocol, srcPort, dstPort, packetSize)) //not checking netmask since netmask without ip is meaningless
					throw new IllegalArgumentException("At least one condition must be set.");
				
				if (textMessage.getText().isEmpty())
					throw new IllegalArgumentException("Please enter a text to be used when a match is found.");
				
				List<String> ipsFromipNotes = ipNotes == null ? null : ipNotesToIPListMap.get(ipNotes);
				
				PacketTypeToMatch newItem = new PacketTypeToMatch(ip, netmask, ipNotes, ipsFromipNotes, packetDirection, protocol, srcPort, dstPort, packetSize, textMessage.getText(), comboOutputMethod.getValue());
				
				
				if (isEdit)
				{
					int selectedIndex = table.getSelectionModel().getSelectedIndex();
					table.getItems().set(selectedIndex, newItem);
					table.getSelectionModel().select(selectedIndex);
				}
				else //add
					table.getItems().add(newItem);
			}
			catch (IllegalArgumentException iae)
			{
				new Alert(AlertType.ERROR, iae.getMessage()).showAndWait();
				throw iae; //so the window won't be closed
			}
		});
	}
	
	private boolean isAllNull(Object... objectList)
	{
		for (Object control : objectList)
			if (control != null)
				return false;
		
		return true;
	}

	private void setValidatorsForControls()
	{
		String deleteToUncheck = "If you want to ignore this field, delete its contents first.";

		textIPAddress.focusedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) ->
		{
			if (!newValue) //on focus out
			{
				isIPFieldValid = false;
				String input = textIPAddress.getText();

				if (input.isEmpty())
				{
					chkboxNetmask.setSelected(false);
					chkboxNetmask.setDisable(true);
					return;
				}

				if (!NetworkSniffer.isValidIPv4(input))
				{
					new Alert(AlertType.ERROR, "Invalid IP address.\n" + deleteToUncheck).showAndWait();
					textIPAddress.requestFocus();
					return;
				}

				isIPFieldValid = true;
				if (chkboxNetmask.isSelected())
				{
					String netmask = textNetmask.getText();

					if (labelIPRange.isVisible()) //if we just updated the ip and there's a valid netmask, recalulate and display it
						calculateAndShowIPRange(input, netmask);
				}
			}
			else //on focus in
				chkboxNetmask.setDisable(false); //in case it was disabled earlier when ip was empty
		});

		textNetmask.focusedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) ->
		{
			if (!isIPFieldValid)
				return;

			if (!newValue)
			{
				String input = textNetmask.getText();

				if (input.isEmpty())
				{
					labelIPRange.setVisible(false);
					return;
				}

				String ip = textIPAddress.getText();

				if (!NetworkSniffer.isValidNetmask(ip, input))
				{
					new Alert(AlertType.ERROR, "Invalid netmask.\n" + deleteToUncheck).showAndWait();
					textNetmask.requestFocus();
				}
				else //valid netmask
					calculateAndShowIPRange(ip, input);
			}
		});
	}

	private void calculateAndShowIPRange(String ip, String netmask)
	{
		String subnetRange = NetworkSniffer.getSubnetRange(ip, netmask);

		if (subnetRange != null)
		{
			labelIPRange.setText("IP address range: " + subnetRange);
			labelIPRange.setVisible(true);
		}
	}

	private void setTooltipsForControls()
	{
		setTooltip(chkboxNetmask, "A netmask defines a range of IP addresses.");
		setTooltip(chkboxIPNotes,
				"An IP note can be used instead of IP address/netmask. An IP note is mapped to one or more IP addresses. IP notes can be managed from the menu File -> Manage IP notes");
	}

	private void setTooltip(Labeled control, String tooltipMsg)
	{
		GUIController.setCommonGraphicOnLabeled(control, GUIController.CommonGraphicImages.TOOLTIP);

		Tooltip tooltip = new Tooltip(tooltipMsg);
		tooltip.setWrapText(true);
		tooltip.setMaxWidth(400);
		control.setTooltip(tooltip);
	}

	private void setChangeListeners()
	{
		chkboxIPAddress.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) ->
		{
			if (newValue)
				chkboxIPNotes.setSelected(false);
			else
				chkboxNetmask.setSelected(false);

			chkboxNetmask.setDisable(!newValue);
			textIPAddress.setDisable(!newValue);
			labelIPRange.setDisable(!newValue);
		});

		chkboxNetmask.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) ->
		{
			boolean ipFieldIsEmpty = textIPAddress.getText().isEmpty();
			boolean netmaskFieldIsEmpty = textNetmask.getText().isEmpty();

			if (newValue && ipFieldIsEmpty)
			{
				new Alert(AlertType.ERROR, "An IP address must be set before setting a netmask.").showAndWait();
				chkboxNetmask.setSelected(false);
				return;
			}

			if (newValue && !ipFieldIsEmpty && !netmaskFieldIsEmpty) //if we're clicking on the checkbox while we already have values in ip/netmask from before (they must be valid)
				labelIPRange.setVisible(true);

			if (!newValue)
				labelIPRange.setVisible(false);

			textNetmask.setDisable(!newValue);
		});

		chkboxIPNotes.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) ->
		{
			if (newValue)
			{
				chkboxIPAddress.setSelected(false);
				chkboxNetmask.setSelected(false);
			}

			comboIPNotes.setDisable(!newValue);
		});

		chkboxPacketDirection.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> comboPacketDirection.setDisable(!newValue));
		chkboxProtocol.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) ->
		{
			comboProtocol.setDisable(!newValue);

			boolean disablePorts = newValue && (comboProtocol.getValue() == SupportedProtocols.ICMP);
			chkboxSrcPort.setDisable(disablePorts);
			chkboxDstPort.setDisable(disablePorts);
			if (disablePorts)
			{
				chkboxSrcPort.setSelected(false);
				chkboxDstPort.setSelected(false);
			}
		});

		chkboxSrcPort.selectedProperty().addListener(generateNumberRangeChangeListenerForCheckboxSelected(comboSrcPort, numFieldSrcPortLeft, numFieldSrcPortRight));
		chkboxDstPort.selectedProperty().addListener(generateNumberRangeChangeListenerForCheckboxSelected(comboDstPort, numFieldDstPortLeft, numFieldDstPortRight));
		chkboxPacketSize.selectedProperty().addListener(generateNumberRangeChangeListenerForCheckboxSelected(comboPacketSize, numFieldPacketSizeLeft, numFieldPacketSizeRight));

		comboProtocol.valueProperty().addListener((ChangeListener<SupportedProtocols>) (observable, oldValue, newValue) ->
		{
			boolean needToDisablePorts = newValue == SupportedProtocols.ICMP;

			if (needToDisablePorts)
			{
				chkboxSrcPort.setSelected(false);
				chkboxDstPort.setSelected(false);
			}

			chkboxSrcPort.setDisable(needToDisablePorts);
			chkboxDstPort.setDisable(needToDisablePorts);
		});

		comboIPNotes.valueProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) ->
		{
			int addressCount = ipNotesToIPListMap.get(newValue).size();
			
			labelNoteCount.setText("Mapped to " + addressCount + " IP address" + (addressCount > 1 ? "es" : ""));
		});
		
		comboSrcPort.valueProperty().addListener(generateNumberRangeChangeListenerForComboValue(numFieldSrcPortLeft, labelSrcPortRight, numFieldSrcPortRight));
		comboDstPort.valueProperty().addListener(generateNumberRangeChangeListenerForComboValue(numFieldDstPortLeft, labelDstPortRight, numFieldDstPortRight));
		comboPacketSize.valueProperty().addListener(generateNumberRangeChangeListenerForComboValue(numFieldPacketSizeLeft, labelPacketSizeRight, numFieldPacketSizeRight));
		
		comboOutputMethod.valueProperty().addListener((ChangeListener<OutputMethod>) (observable, oldValue, newValue) -> btnPreview.setVisible(newValue ==  OutputMethod.TTS || newValue == OutputMethod.TTS_AND_POPUP));
	}

	/**
	 * @param comboOfNumberRange
	 *            - the comboBox that shows the NumberRange values
	 * @param numFieldLeft
	 *            - the left NumberField
	 * @param numFieldRight
	 *            - the right NumberField
	 * @return
	 */
	private ChangeListener<Boolean> generateNumberRangeChangeListenerForCheckboxSelected(ComboBox<NumberRange> comboOfNumberRange, NumberTextField numFieldLeft, NumberTextField numFieldRight)
	{
		return new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				comboOfNumberRange.setDisable(!newValue);

				if (comboOfNumberRange.getValue() != null)
					numFieldLeft.setDisable(!newValue);

				numFieldRight.setDisable(!newValue);
			}
		};
	}

	/**
	 * @param numFieldLeft
	 *            - the left NumberField
	 * @param labelForRightField
	 *            - the label for the right NumberField
	 * @param numFieldRight
	 *            - the right NumberField
	 * @return
	 */
	private ChangeListener<NumberRange> generateNumberRangeChangeListenerForComboValue(NumberTextField numFieldLeft, Label labelForRightField, NumberTextField numFieldRight)
	{
		return new ChangeListener<NumberRange>()
		{
			@Override
			public void changed(ObservableValue<? extends NumberRange> observable, NumberRange oldValue, NumberRange newValue)
			{
				boolean isRangeSelected = newValue == NumberRange.RANGE;

				numFieldLeft.setDisable(false);
				labelForRightField.setVisible(isRangeSelected);
				numFieldRight.setVisible(isRangeSelected);
			}
		};
	}

	private void setContentForEdit(PacketTypeToMatch selectedItem)
	{
		String ipAddress = selectedItem.getIpAddressValue();
		if (ipAddress != null && !ipAddress.equals(PacketTypeToMatch.IPAddress_EMPTY))
		{
			chkboxIPAddress.setSelected(true);
			textIPAddress.setText(ipAddress);
		}

		String netmask = selectedItem.getNetmaskValue();
		if (netmask != null && !netmask.equals(PacketTypeToMatch.netmask_EMPTY))
		{
			chkboxNetmask.setSelected(true);
			textNetmask.setText(netmask);
			calculateAndShowIPRange(ipAddress, netmask);
		}

		String ipNotes = selectedItem.getIPNotesValue();
		if (ipNotes != null && !ipNotes.equals(PacketTypeToMatch.ipNotes_EMPTY))
		{
			chkboxIPNotes.setSelected(true);
			comboIPNotes.getSelectionModel().select(ipNotes);
		}

		PacketDirection packetDirection = selectedItem.getPacketDirectionValue();
		if (packetDirection != null)
		{
			chkboxPacketDirection.setSelected(true);
			comboPacketDirection.getSelectionModel().select(packetDirection);
		}

		SupportedProtocols protocol = selectedItem.getProtocolValue();
		if (protocol != null)
		{
			chkboxProtocol.setSelected(true);
			comboProtocol.getSelectionModel().select(protocol);
		}

		NumberRangeValues srcPortValues = selectedItem.getSrcPortValues();
		if (srcPortValues != null)
		{
			NumberRange srcPortRange = srcPortValues.getRange();
			
			chkboxSrcPort.setSelected(true);
			comboSrcPort.getSelectionModel().select(srcPortRange);
			numFieldSrcPortLeft.setText(srcPortValues.getLeftValue().toString());

			if (srcPortRange == NumberRange.RANGE)
			{
				labelSrcPortRight.setVisible(true);
				numFieldSrcPortRight.setVisible(true);
				numFieldSrcPortRight.setText(srcPortValues.getRightValue().toString());
			}
		}

		NumberRangeValues dstPortValues = selectedItem.getDstPortValues();
		if (dstPortValues != null)
		{
			NumberRange dstPort = dstPortValues.getRange();
			
			chkboxDstPort.setSelected(true);
			comboDstPort.getSelectionModel().select(dstPort);
			numFieldDstPortLeft.setText(dstPortValues.getLeftValue().toString());

			if (dstPort == NumberRange.RANGE)
			{
				labelDstPortRight.setVisible(true);
				numFieldDstPortRight.setVisible(true);
				numFieldDstPortRight.setText(dstPortValues.getRightValue().toString());
			}
		}

		NumberRangeValues packetSizeValues = selectedItem.getPacketSizeValues();
		if (packetSizeValues != null)
		{
			NumberRange packetSize = packetSizeValues.getRange();
			
			chkboxPacketSize.setSelected(true);
			comboPacketSize.getSelectionModel().select(packetSize);
			numFieldPacketSizeLeft.setText(packetSizeValues.getLeftValue().toString());

			if (packetSize == NumberRange.RANGE)
			{
				labelPacketSizeRight.setVisible(true);
				numFieldPacketSizeRight.setVisible(true);
				numFieldPacketSizeRight.setText(packetSizeValues.getRightValue().toString());
			}
		}

		textMessage.setText(selectedItem.getMessageTextValue());
		comboOutputMethod.getSelectionModel().select(selectedItem.getMessageOutputMethodValue());
	}

	private void populateCombos()
	{
		String[] notes = Arrays.copyOf(ipNotesToIPListMap.keySet().toArray(), ipNotesToIPListMap.size(), String[].class);
		comboIPNotes.setItems(FXCollections.observableArrayList(notes));

		comboPacketDirection.setItems(FXCollections.observableArrayList(PacketDirection.values()));
		comboProtocol.setItems(FXCollections.observableArrayList(SupportedProtocols.values()));
		comboOutputMethod.setItems(FXCollections.observableArrayList(OutputMethod.values()));

		ObservableList<NumberRange> obsListNumberRange = FXCollections.observableArrayList(NumberRange.values());
		comboSrcPort.setItems(obsListNumberRange);
		comboDstPort.setItems(FXCollections.observableArrayList(obsListNumberRange));
		comboPacketSize.setItems(obsListNumberRange);
	}

	public Button getBtnDone()
	{
		return watchdogListAddEditController.getBtnDone();
	}

	public Button getBtnCancel()
	{
		return watchdogListAddEditController.getBtnCancel();
	}
}
