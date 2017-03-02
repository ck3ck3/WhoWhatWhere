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

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import numbertextfield.NumberTextField;
import whowhatwhere.controller.GUIController;
import whowhatwhere.model.networksniffer.PacketDirection;
import whowhatwhere.model.networksniffer.SupportedProtocols;
import whowhatwhere.model.networksniffer.watchdog.OutputMethod;

public class ListAddEditController implements Initializable
{
	@FXML
	private ComboBox<OutputMethod> comboOutputMethod;
	@FXML
	private TextField textMessage;
	@FXML
	private Button btnDone;
	@FXML
	private ComboBox<PacketDirection> comboPacketDirection;
	@FXML
	private TextField textIPAddress;
	@FXML
	private TextField textNetmask;
	@FXML
	private ComboBox<String> comboIPNotes;
	@FXML
	private ComboBox<NumberRange> comboPacketSize;
	@FXML
	private NumberTextField numFieldPacketSizeLeft;
	@FXML
	private Label labelPacketSizeRight;
	@FXML
	private NumberTextField numFieldPacketSizeRight;
	@FXML
	private ComboBox<SupportedProtocols> comboProtocol;
	@FXML
	private ComboBox<NumberRange> comboSrcPort;
	@FXML
	private NumberTextField numFieldSrcPortLeft;
	@FXML
	private Label labelSrcPortRight;
	@FXML
	private NumberTextField numFieldDstPortRight;
	@FXML
	private ComboBox<NumberRange> comboDstPort;
	@FXML
	private NumberTextField numFieldDstPortLeft;
	@FXML
	private Label labelDstPortRight;
	@FXML
	private NumberTextField numFieldSrcPortRight;
	@FXML
	private Button btnCancel;
	@FXML
	private CheckBox chkboxIPAddress;
	@FXML
	private CheckBox chkboxIPNotes;
	@FXML
	private CheckBox chkboxPacketDirection;
	@FXML
	private CheckBox chkboxProtocol;
	@FXML
	private CheckBox chkboxSrcPort;
	@FXML
	private CheckBox chkboxDstPort;
	@FXML
	private CheckBox chkboxPacketSize;
	@FXML
	private Label labelIPRange;
	@FXML
	private CheckBox chkboxNetmask;
	@FXML
	private AnchorPane paneWholeForm;
	@FXML
	private Button btnPreview;
	@FXML
	private Label labelNoteCount;
	@FXML
	private Button btnConfigTTS;
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		paneWholeForm.setOnKeyPressed(keyEvent ->
		{
			Button btnToFire;
			
			switch(keyEvent.getCode())
			{
				case ESCAPE:	btnToFire = btnCancel;	break;
				case ENTER:		btnToFire = btnDone;	break;
				default:		btnToFire = null;		break;
			}
			
			if (btnToFire != null)
			{
				btnToFire.requestFocus();
				if (btnToFire.isFocused()) //if there's no validation error
					btnToFire.fire();
			}
		});
		
		labelNoteCount.visibleProperty().bind(chkboxIPNotes.selectedProperty());
		
		labelIPRange.setVisible(false);
		
		labelSrcPortRight.setVisible(false);
		numFieldSrcPortRight.setVisible(false);
		
		labelDstPortRight.setVisible(false);
		numFieldDstPortRight.setVisible(false);
		
		labelPacketSizeRight.setVisible(false);
		numFieldPacketSizeRight.setVisible(false);
		
		btnPreview.setVisible(false);
		GUIController.setCommonGraphicOnLabeled(btnConfigTTS, GUIController.CommonGraphicImages.VOICE_CONFIG);
		GUIController.setCommonGraphicOnLabeled(btnPreview, GUIController.CommonGraphicImages.SPEAKER);
		GUIController.setCommonGraphicOnLabeled(btnDone, GUIController.CommonGraphicImages.OK);
		GUIController.setCommonGraphicOnLabeled(btnCancel, GUIController.CommonGraphicImages.CANCEL);
	}
	
	public ComboBox<OutputMethod> getComboOutputMethod()
	{
		return comboOutputMethod;
	}

	public TextField getTextMessage()
	{
		return textMessage;
	}

	public Button getBtnDone()
	{
		return btnDone;
	}

	public Button getBtnCancel()
	{
		return btnCancel;
	}

	public ComboBox<PacketDirection> getComboPacketDirection()
	{
		return comboPacketDirection;
	}

	public TextField getTextIPAddress()
	{
		return textIPAddress;
	}

	public TextField getTextNetmask()
	{
		return textNetmask;
	}

	public ComboBox<String> getComboIPNotes()
	{
		return comboIPNotes;
	}

	public ComboBox<NumberRange> getComboPacketSize()
	{
		return comboPacketSize;
	}

	public NumberTextField getNumFieldPacketSizeLeft()
	{
		return numFieldPacketSizeLeft;
	}

	public Label getLabelPacketSizeRight()
	{
		return labelPacketSizeRight;
	}

	public NumberTextField getNumFieldPacketSizeRight()
	{
		return numFieldPacketSizeRight;
	}

	public ComboBox<SupportedProtocols> getComboProtocol()
	{
		return comboProtocol;
	}

	public ComboBox<NumberRange> getComboSrcPort()
	{
		return comboSrcPort;
	}

	public NumberTextField getNumFieldSrcPortLeft()
	{
		return numFieldSrcPortLeft;
	}

	public Label getLabelSrcPortRight()
	{
		return labelSrcPortRight;
	}

	public NumberTextField getNumFieldDstPortRight()
	{
		return numFieldDstPortRight;
	}

	public ComboBox<NumberRange> getComboDstPort()
	{
		return comboDstPort;
	}

	public NumberTextField getNumFieldDstPortLeft()
	{
		return numFieldDstPortLeft;
	}

	public Label getLabelDstPortRight()
	{
		return labelDstPortRight;
	}

	public NumberTextField getNumFieldSrcPortRight()
	{
		return numFieldSrcPortRight;
	}

	public CheckBox getChkboxIPAddress()
	{
		return chkboxIPAddress;
	}

	public CheckBox getChkboxIPNotes()
	{
		return chkboxIPNotes;
	}

	public CheckBox getChkboxPacketDirection()
	{
		return chkboxPacketDirection;
	}

	public CheckBox getChkboxProtocol()
	{
		return chkboxProtocol;
	}

	public CheckBox getChkboxSrcPort()
	{
		return chkboxSrcPort;
	}

	public CheckBox getChkboxDstPort()
	{
		return chkboxDstPort;
	}

	public CheckBox getChkboxPacketSize()
	{
		return chkboxPacketSize;
	}


	public Label getLabelIPRange()
	{
		return labelIPRange;
	}

	public CheckBox getChkboxNetmask()
	{
		return chkboxNetmask;
	}

	public Button getBtnPreview()
	{
		return btnPreview;
	}

	public Label getLabelNoteCount()
	{
		return labelNoteCount;
	}
	
	public Button getBtnConfigTTS()
	{
		return btnConfigTTS;
	}
}
