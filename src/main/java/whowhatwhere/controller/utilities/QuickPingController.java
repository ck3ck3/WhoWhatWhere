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
package whowhatwhere.controller.utilities;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import whowhatwhere.controller.GUIController;
import whowhatwhere.model.networksniffer.watchdog.OutputMethod;

public class QuickPingController implements Initializable
{
	@FXML
	private ComboBox<String> comboIPToPing;
	@FXML
	private Button btnConfigureHotkey;
	@FXML
	private Label labelCurrentHotkey;
	@FXML
	private CheckBox chkboxHotkey;
	@FXML
	private AnchorPane paneHotkey;
	@FXML
	private ComboBox<OutputMethod> comboOutputMethod;
	@FXML
	private AnchorPane paneAllButHotkeyChkbox;
	@FXML
	private Label labelIP;
	@FXML
	private Label labelTTSTooltip;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		comboOutputMethod.setItems(FXCollections.observableArrayList(OutputMethod.values()));
		
		GUIController.setCommonGraphicOnLabeled(labelIP, GUIController.CommonGraphicImages.TOOLTIP);
		labelIP.setTooltip(new Tooltip("IP or hostname to ping when the hotkey is pressed"));
		
		GUIController.setCommonGraphicOnLabeled(labelTTSTooltip, GUIController.CommonGraphicImages.TOOLTIP);
		labelTTSTooltip.setTooltip(new Tooltip("The voice (and language) can be configured from the Options menu."));
		
		comboOutputMethod.valueProperty().addListener((ChangeListener<OutputMethod>) (observable, oldValue, newValue) -> labelTTSTooltip.setVisible(newValue == OutputMethod.TTS || newValue == OutputMethod.TTS_AND_POPUP));
		
		GUIController.setCommonGraphicOnLabeled(btnConfigureHotkey, GUIController.CommonGraphicImages.HOTKEY);
	}
	
	public ComboBox<String> getComboToPing()
	{
		return comboIPToPing;
	}
	
	public ComboBox<OutputMethod> getComboOutputMethod()
	{
		return comboOutputMethod;
	}

	public Button getBtnConfigureHotkey()
	{
		return btnConfigureHotkey;
	}

	public Label getLabelCurrentHotkey()
	{
		return labelCurrentHotkey;
	}

	public CheckBox getChkboxHotkey()
	{
		return chkboxHotkey;
	}

	public AnchorPane getPaneHotkey()
	{
		return paneHotkey;
	}
	
	public AnchorPane getPaneAllButHotkeyChkbox()
	{
		return paneAllButHotkeyChkbox;
	}
}
