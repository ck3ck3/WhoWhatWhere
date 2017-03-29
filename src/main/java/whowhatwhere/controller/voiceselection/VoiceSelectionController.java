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

import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import numbertextfield.NumberTextField;
import whowhatwhere.controller.GUIController;
import whowhatwhere.controller.MessagesI18n;
import whowhatwhere.model.tts.TTSVoice;

public class VoiceSelectionController implements Initializable
{
	@FXML
	private RadioButton radioSameVoice;
	@FXML
	private ToggleGroup oneVoiceOrMultiple;
	@FXML
	private RadioButton radioDiffVoices;
	@FXML
	private ComboBox<TTSVoice> comboSameVoice;
	@FXML
	private ComboBox<TTSVoice> comboWWW;
	@FXML
	private Button btnPreviewSame;
	@FXML
	private Button btnPreviewWWW;
	@FXML
	private ComboBox<TTSVoice> comboWatchdog;
	@FXML
	private TextField textSame;
	@FXML
	private TextField textWatchdog;
	@FXML
	private Button btnPreviewWatchdog;
	@FXML
	private ComboBox<TTSVoice> comboQuickPing;
	@FXML
	private Button btnPreviewQuickPing;
	@FXML
	private NumberTextField numFieldQuickPing;
	@FXML
	private Label labelLocalizedMS;
	@FXML
	private Button btnDone;
	@FXML
	private AnchorPane paneSameVoice;
	@FXML
	private AnchorPane paneDiffVoices;
	@FXML 
	private AnchorPane paneSettings;
	@FXML
	private TextField textWWW;
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		setGraphics();
		setCombosContent();
		setControlsBehavior();
		GUIController.setNumberTextFieldValidationUI(numFieldQuickPing);
	}

	private void setGraphics()
	{
		GUIController.setCommonGraphicOnLabeled(btnDone, GUIController.CommonGraphicImages.OK);
		GUIController.setCommonGraphicOnLabeled(btnPreviewSame, GUIController.CommonGraphicImages.SPEAKER);
		GUIController.setCommonGraphicOnLabeled(btnPreviewWWW, GUIController.CommonGraphicImages.SPEAKER);
		GUIController.setCommonGraphicOnLabeled(btnPreviewWatchdog, GUIController.CommonGraphicImages.SPEAKER);
		GUIController.setCommonGraphicOnLabeled(btnPreviewQuickPing, GUIController.CommonGraphicImages.SPEAKER);		
	}
	
	private void setCombosContent()
	{
		List<TTSVoice> voices = Arrays.asList(TTSVoice.values());
		Comparator<TTSVoice> comperator = (o1, o2) -> o1.toString().compareTo(o2.toString());  
		voices.sort(comperator);
		Predicate<TTSVoice> onlyEnglishVoices = voice -> voice.getLanguage().toString().contains("English");
		
		comboSameVoice.setItems(FXCollections.observableArrayList(voices).filtered(onlyEnglishVoices));
		comboWWW.setItems(FXCollections.observableArrayList(voices).filtered(onlyEnglishVoices));
		comboWatchdog.setItems(FXCollections.observableArrayList(voices));
		comboQuickPing.setItems(FXCollections.observableArrayList(voices));
		
		numFieldQuickPing.setMinValue(0);
	}
	
	private void setControlsBehavior()
	{
		radioSameVoice.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) ->	paneDiffVoices.setDisable(newValue));
		radioDiffVoices.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> paneSameVoice.setDisable(newValue));
		
		comboWatchdog.valueProperty().addListener((ChangeListener<TTSVoice>) (observable, oldValue, newValue) ->
		{
			String langCode = newValue.getLanguage().getLanguageCode();
			ResourceBundle bundle = ResourceBundle.getBundle(MessagesI18n.i18nBundleLocation, new Locale(langCode));
			textWatchdog.setText(bundle.getString(MessagesI18n.sampleText));
		});
		
		comboQuickPing.valueProperty().addListener((ChangeListener<TTSVoice>) (observable, oldValue, newValue) -> 
		{
			String langCode = newValue.getLanguage().getLanguageCode();
			ResourceBundle bundle = ResourceBundle.getBundle(MessagesI18n.i18nBundleLocation, new Locale(langCode));
			labelLocalizedMS.setText(bundle.getString(MessagesI18n.milliseconds_display));
		});
	}

	public RadioButton getRadioSameVoice()
	{
		return radioSameVoice;
	}

	public RadioButton getRadioDiffVoices()
	{
		return radioDiffVoices;
	}

	public ComboBox<TTSVoice> getComboSameVoice()
	{
		return comboSameVoice;
	}

	public ComboBox<TTSVoice> getComboWWW()
	{
		return comboWWW;
	}

	public Button getBtnPreviewSame()
	{
		return btnPreviewSame;
	}

	public Button getBtnPreviewWWW()
	{
		return btnPreviewWWW;
	}

	public ComboBox<TTSVoice> getComboWatchdog()
	{
		return comboWatchdog;
	}

	public TextField getTextWatchdog()
	{
		return textWatchdog;
	}

	public Button getBtnPreviewWatchdog()
	{
		return btnPreviewWatchdog;
	}

	public ComboBox<TTSVoice> getComboQuickPing()
	{
		return comboQuickPing;
	}

	public Button getBtnPreviewQuickPing()
	{
		return btnPreviewQuickPing;
	}

	public NumberTextField getNumFieldQuickPing()
	{
		return numFieldQuickPing;
	}

	public Button getBtnDone()
	{
		return btnDone;
	}

	public TextField getTextSame()
	{
		return textSame;
	}

	public AnchorPane getPaneSettings()
	{
		return paneSettings;
	}
	
	public TextField getTextWWW()
	{
		return textWWW;
	}
}
