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
package whowhatwhere.controller.voiceselection;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import numbertextfield.NumberTextField;
import whowhatwhere.controller.ConfigurableTTS;
import whowhatwhere.controller.GUIController;
import whowhatwhere.controller.MessagesI18n;
import whowhatwhere.model.tts.MaryTTS;
import whowhatwhere.model.tts.TTSVoice;
import whowhatwhere.view.secondaryfxmlscreen.SecondaryFXMLScreen;

public class VoiceSelectionScreen extends SecondaryFXMLScreen
{
	private RadioButton radioSameVoice;
	private RadioButton radioDiffVoices;
	private VoiceSelectionController controller;
	private ComboBox<TTSVoice> comboSameVoice;
	private ComboBox<TTSVoice> comboWWW;
	private Button btnPreviewSame;
	private Button btnPreviewWWW;
	private ComboBox<TTSVoice> comboWatchdog;
	private TextField textSame;
	private TextField textWatchdog;
	private Button btnPreviewWatchdog;
	private ComboBox<TTSVoice> comboQuickPing;
	private Button btnPreviewQuickPing;
	private NumberTextField numFieldQuickPing;
	private Button btnDone;
	private TextField textWWW;

	private MaryTTS tts = new MaryTTS(GUIController.defaultTTSVoiceName);
	
	private ConfigurableTTS www;
	private ConfigurableTTS watchdog;
	private ConfigurableTTS quickPing;

	public VoiceSelectionScreen(String fxmlLocation, Stage stage, Scene scene, ConfigurableTTS www, ConfigurableTTS watchdog, ConfigurableTTS quickPing) throws IOException
	{
		super(fxmlLocation, stage, scene);

		controller = getLoader().getController();
		this.www = www;
		this.watchdog = watchdog;
		this.quickPing = quickPing;

		setControlsFromController();
		setButtonHandlers();
		setComboSelections();
	}

	private void setComboSelections()
	{
		boolean sameVoice = isSameVoice();

		radioSameVoice.setSelected(sameVoice);
		radioDiffVoices.setSelected(!sameVoice);

		if (sameVoice)
			comboSameVoice.getSelectionModel().select(www.getTTSVoice());
		else
		{
			comboWWW.getSelectionModel().select(www.getTTSVoice());
			comboWatchdog.getSelectionModel().select(watchdog.getTTSVoice());
			comboQuickPing.getSelectionModel().select(quickPing.getTTSVoice());
		}
	}

	private boolean isSameVoice()
	{
		return (www.getTTSVoice() == watchdog.getTTSVoice()) && (watchdog.getTTSVoice() == quickPing.getTTSVoice()); //transitive property
	}

	private void setButtonHandlers()
	{
		btnDone.setOnAction(ae -> 
		{
			validateSelections();
			setSelectedVoices();
		});

		btnPreviewSame.setOnAction(generateEventHandlerForTextPreview(comboSameVoice, textSame));
		
		btnPreviewWWW.setOnAction(generateEventHandlerForTextPreview(comboWWW, textWWW));
		
		btnPreviewWatchdog.setOnAction(generateEventHandlerForTextPreview(comboWatchdog, textWatchdog));
		
		btnPreviewQuickPing.setOnAction(ae -> 
		{
			TTSVoice voice = comboQuickPing.getValue();
			if (voice == null)
			{
				new Alert(AlertType.ERROR, "Please select a voice.").showAndWait();
				return;
			}
			
			ResourceBundle bundle = ResourceBundle.getBundle(MessagesI18n.i18nBundleLocation, new Locale(voice.getLanguage().getLanguageCode()));
			String text = numFieldQuickPing.getText() + " " + bundle.getString("milliseconds");
			tts.setVoice(voice);
			tts.speak(text);			
		});
	}
	
	private EventHandler<ActionEvent> generateEventHandlerForTextPreview(ComboBox<TTSVoice> combo, TextField textfield)
	{
		return ae -> 
		{
			String text = textfield.getText();
			if (text.isEmpty())
			{
				new Alert(AlertType.ERROR, "Please enter any text in order to preview the voice.").showAndWait();
				return;
			}
			
			TTSVoice voice = combo.getValue();
			if (voice == null)
			{
				new Alert(AlertType.ERROR, "Please select a voice.").showAndWait();
				return;
			}
			
			tts.setVoice(voice);
			tts.speak(text);
		};
	}
	
	private void validateSelections()
	{
		if (radioDiffVoices.isSelected() && (comboWWW.getValue() == null || comboWatchdog.getValue() == null || comboQuickPing.getValue() == null))
		{
			Alert alert = new Alert(AlertType.ERROR, "Please select voices for all features, or choose the option \"" + radioSameVoice.getText() + "\"");
			alert.setTitle("Missing voice selections");
			alert.setHeaderText("Not all choices were set.");
			
			alert.showAndWait();
			throw new IllegalArgumentException();
		}
		else
			if (radioSameVoice.isSelected() && comboSameVoice.getValue() == null)
			{
				Alert alert = new Alert(AlertType.ERROR, "Please select a voice.");
				alert.setTitle("Missing voice selection");
				alert.setHeaderText("No voice was set.");
				
				alert.showAndWait();
				throw new IllegalArgumentException();					
			}		
	}
	
	private void setSelectedVoices()
	{
		if (radioSameVoice.isSelected())
		{
			TTSVoice voice = comboSameVoice.getValue();

			www.setTTSVoice(voice);
			watchdog.setTTSVoice(voice);
			quickPing.setTTSVoice(voice);
		}
		else
		{
			www.setTTSVoice(comboWWW.getValue());
			watchdog.setTTSVoice(comboWatchdog.getValue());
			quickPing.setTTSVoice(comboQuickPing.getValue());
		}
	}

	private void setControlsFromController()
	{
		radioSameVoice = controller.getRadioSameVoice();
		radioDiffVoices = controller.getRadioDiffVoices();
		comboSameVoice = controller.getComboSameVoice();
		comboWWW = controller.getComboWWW();
		btnPreviewSame = controller.getBtnPreviewSame();
		btnPreviewWWW = controller.getBtnPreviewWWW();
		comboWatchdog = controller.getComboWatchdog();
		textSame = controller.getTextSame();
		textWatchdog = controller.getTextWatchdog();
		btnPreviewWatchdog = controller.getBtnPreviewWatchdog();
		comboQuickPing = controller.getComboQuickPing();
		btnPreviewQuickPing = controller.getBtnPreviewQuickPing();
		numFieldQuickPing = controller.getNumFieldQuickPing();
		btnDone = controller.getBtnDone();
		textWWW = controller.getTextWWW();
	}

	public Button getCloseButton()
	{
		return controller.getBtnDone();
	}
}
