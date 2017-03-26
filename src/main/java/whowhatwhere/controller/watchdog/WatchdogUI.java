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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.jnetpcap.packet.PcapPacket;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import numbertextfield.NumberTextField;
import whowhatwhere.controller.ConfigurableTTS;
import whowhatwhere.controller.GUIController;
import whowhatwhere.controller.HotkeyRegistry;
import whowhatwhere.controller.LoadAndSaveSettings;
import whowhatwhere.model.PropertiesByType;
import whowhatwhere.model.networksniffer.NICInfo;
import whowhatwhere.model.networksniffer.NetworkSniffer;
import whowhatwhere.model.networksniffer.watchdog.PacketTypeToMatch;
import whowhatwhere.model.networksniffer.watchdog.WatchdogListener;
import whowhatwhere.model.networksniffer.watchdog.WatchdogMessage;
import whowhatwhere.model.tts.MaryTTS;
import whowhatwhere.model.tts.TTSVoice;

public class WatchdogUI implements WatchdogListener, LoadAndSaveSettings, ConfigurableTTS
{
	private enum RowMovementDirection {UP, DOWN}
	
	private final static Logger logger = Logger.getLogger(WatchdogUI.class.getPackage().getName());
	private final static String watchdogListAddEditFormLocation = "/whowhatwhere/view/fxmls/watchdog/AddEditRule.fxml";

	public final static String ruleListExtension = ".watchdogRuleList";
	public final static String lastRunFilename = "Last run" + ruleListExtension;
	public final static int minCooldownValue = 1;
	public final static int defaultnCooldownValue = 3;
	public final static int maxCooldownValue = 60 * 60 * 24; //24 hours
	
	private final static String hotkeyID = "Watchdog hotkey";
	private final static String voiceForTTS = GUIController.defaultTTSVoiceName;

	private final static String propsChkboxHotkey = "chkboxWatchdogHotkey";
	private final static String propsHotkeyKeycode = "watchdogHotkeyKeycode";
	private final static String propsHotkeyModifiers = "watchdogHotkeyModifiers";
	private final static String propsRadioStopAfterMatch = "radioStopAfterMatch";
	private final static String propsRadioKeepLooking = "radioKeepLooking";
	private final static String propsNumFieldCooldown = "numFieldCooldown";
	private final static String propsTTSVoiceName = "watchdogTTSVoice";

	private GUIController guiController;
	private WatchdogController controller;

	private CheckBox chkboxHotkey;
	private HBox paneHotkeyConfig;
	private Button btnConfigureHotkey;
	private Label labelCurrHotkey;
	private Button btnStart;
	private Button btnStop;
	private Button activeButton;
	private RadioButton radioStopAfterMatch;
	private RadioButton radioKeepLooking;
	private NumberTextField numFieldCooldown;
	private AnchorPane paneCooldown;
	private AnchorPane paneWatchdogConfig;
	private TableView<PacketTypeToMatch> table;
	private AnchorPane paneTableAndControls;

	private ObservableList<PacketTypeToMatch> ruleList;
	private MaryTTS tts;
	private NetworkSniffer sniffer = new NetworkSniffer();
	private HotkeyRegistry hotkeyRegistry;
	
	private Runnable hotkeyPressed = () ->
	{
		String line;
		Button savedActiveButton = activeButton;

		activeButton.fire();

		if (savedActiveButton == btnStart)
		{
			if (ruleList.isEmpty())
				return;

			line = "Watchdog started";
			changeUIAccordingToListeningState(true);
		}
		else
		{
			line = "Watchdog stopped";
			changeUIAccordingToListeningState(false);
		}

		tts.speak(line);
	};

	public WatchdogUI(GUIController guiController)
	{
		this.controller = guiController.getWatchdogPaneController();
		this.guiController = guiController;
		this.guiController.registerForSettingsHandler(this);

		initUIElementsFromController();
		ruleList = table.getItems();
		initButtonHandlers();
		
		guiController.setNumberTextFieldsValidationUI(guiController.getTabWatchdog(), numFieldCooldown);
	}

	private void initUIElementsFromController()
	{
		hotkeyRegistry = guiController.getHotkeyRegistry();

		chkboxHotkey = controller.getChkboxHotkey();
		paneHotkeyConfig = controller.getPaneHotkeyConfig();
		btnConfigureHotkey = controller.getBtnConfigureHotkey();
		labelCurrHotkey = controller.getLabelCurrHotkey();
		btnStart = controller.getBtnStart();
		btnStop = controller.getBtnStop();
		radioKeepLooking = controller.getRadioKeepLooking();
		radioStopAfterMatch = controller.getRadioStopAfterMatch();
		numFieldCooldown = controller.getNumFieldCooldown();
		paneCooldown = controller.getPaneCooldown();
		paneWatchdogConfig = controller.getPaneConfig();
		table = controller.getTable();
		paneTableAndControls = controller.getPaneTableAndControls();
}

	private void initButtonHandlers()
	{
		WatchdogUI thisObj = this;

		btnConfigureHotkey.setOnAction(hotkeyRegistry.generateEventHandlerForHotkeyConfigButton(hotkeyID));
		
		setTableRowDoubleClickToEdit();
		
		controller.getBtnAddRow().setOnAction(generateAddEditEventHandler(false));
		controller.getBtnEditRow().setOnAction(generateAddEditEventHandler(true));
		initRemoveRowButton();
		
		controller.getBtnMoveUp().setOnAction(generateRowMovementButtonHandlers(RowMovementDirection.UP));
		controller.getBtnMoveDown().setOnAction(generateRowMovementButtonHandlers(RowMovementDirection.DOWN));
		
		initSaveRuleListButton();
		initLoadRuleListButton();

		radioStopAfterMatch.setOnAction(event -> paneCooldown.setDisable(true));
		radioKeepLooking.setOnAction(event -> paneCooldown.setDisable(false));

		btnStart.setOnAction(event ->
		{
			if (ruleList.isEmpty())
			{
				new Alert(AlertType.ERROR, "The list must contain at least one rule").showAndWait();
				return;
			}

			NICInfo deviceInfo = guiController.getSelectedNIC();
			new Thread(() ->
			{
				StringBuilder errorBuffer = new StringBuilder();

				try
				{
					sniffer.startWatchdogCapture(deviceInfo, ruleList, radioKeepLooking.isSelected(), numFieldCooldown.getValue(), thisObj, errorBuffer);
				}
				catch (IllegalArgumentException | UnknownHostException e)
				{
					logger.log(Level.SEVERE, "Unable to build Watchdog list", e);
					Platform.runLater(() -> new Alert(AlertType.ERROR, "Unable to build Watchdog list: " + e.getMessage() + "\nError buffer: " + errorBuffer.toString()).showAndWait());
				}
			}).start();

			changeUIAccordingToListeningState(true);
		});

		btnStop.setOnAction(event ->
		{
			changeUIAccordingToListeningState(false);

			sniffer.stopCapture();
		});

		activeButton = btnStart;
	}
	
	/**
	 * @param movementDirection - enum to specify if the row movement is up or down
	 * @return an EventHandler for the row movement buttons
	 */
	private EventHandler<ActionEvent> generateRowMovementButtonHandlers(RowMovementDirection movementDirection)
	{
		int moveToPosition = movementDirection == RowMovementDirection.UP ? -1 : 1;
		
		return event ->
		{
			ObservableList<PacketTypeToMatch> items = table.getItems();
			TableViewSelectionModel<PacketTypeToMatch> selectionModel = table.getSelectionModel();
			List<Integer> modifiableIndices = new ArrayList<>(selectionModel.getSelectedIndices());
			int[] reSelectRows = new int[modifiableIndices.size()];
			int i = 0;
			
			if (movementDirection == RowMovementDirection.DOWN) //if we are moving down, we should start from the last index and go backwards
				Collections.reverse(modifiableIndices);
			
			for (Integer selectedIndex : modifiableIndices)
			{
				if (selectedIndex == (movementDirection == RowMovementDirection.UP ? 0 : items.size() - 1)) //if it's the first or last row (depending on movement direction), don't do anything
				{
					reSelectRows[i++] = selectedIndex;
					continue;
				}
				
				PacketTypeToMatch itemToReplace = items.set(selectedIndex + moveToPosition, items.get(selectedIndex));
				items.set(selectedIndex, itemToReplace);
				reSelectRows[i++] = selectedIndex + moveToPosition;
			}
			
			selectionModel.clearSelection();
			selectionModel.selectIndices(reSelectRows[0], reSelectRows);
			table.refresh();
		};		
	}
	
	private void initRemoveRowButton()
	{
		controller.getBtnRemoveRow().setOnAction(event ->
		{
			ObservableList<PacketTypeToMatch> selectedItems = table.getSelectionModel().getSelectedItems();

			if (selectedItems.isEmpty())
			{
				new Alert(AlertType.ERROR, "No rules selected.").showAndWait();
				return;
			}
			
			String ruleOrRules = selectedItems.size() > 1 ? "s" : ""; 
			Alert removalConfirmation = new Alert(AlertType.CONFIRMATION, "Are you sure you want to remove the selected rule" + ruleOrRules + "?");
			removalConfirmation.setTitle("Rule Removal Confirmation");
			removalConfirmation.setHeaderText("Remove rule" + ruleOrRules);
			ButtonType btnYes = new ButtonType("Yes", ButtonData.OK_DONE);
			ButtonType btnNo = new ButtonType("No", ButtonData.CANCEL_CLOSE);
			removalConfirmation.getButtonTypes().setAll(btnYes, btnNo);
			Optional<ButtonType> result = removalConfirmation.showAndWait();
			
			if (result.get() == btnYes)
			{
				table.getItems().removeAll(selectedItems);
				table.getSelectionModel().clearSelection();
			}
		});
	}
	
	private EventHandler<ActionEvent> generateAddEditEventHandler(boolean isEdit)
	{
		return new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				ListAddEditScreen watchdogListAddEditScreen;
				Stage stage = guiController.getStage();
				
				if (isEdit)
				{
					int numOfSelectedRows = table.getSelectionModel().getSelectedIndices().size();
					String errorMsg = null;
					
					if (numOfSelectedRows == 0)
						errorMsg = "Please select a rule to edit";
					else
						if (numOfSelectedRows > 1)
							errorMsg = "Only one rule must be selected for edit";
					
					if (numOfSelectedRows != 1)
					{
						new Alert(AlertType.ERROR, errorMsg).showAndWait();
						return;
					}
				}

				try
				{
					watchdogListAddEditScreen = new ListAddEditScreen(watchdogListAddEditFormLocation, stage, stage.getScene(), isEdit, table, tts, guiController);
				}
				catch (IOException e)
				{
					logger.log(Level.SEVERE, "Unable to load watchdog list add/edit screen", e);
					return;
				}
				catch (IllegalStateException ise)
				{
					new Alert(AlertType.ERROR, ise.getMessage()).showAndWait();
					return;
				}

				Stage newStage = watchdogListAddEditScreen.showScreenOnNewStage((isEdit ? "Edit" : "Add") + " a Rule", Modality.APPLICATION_MODAL, watchdogListAddEditScreen.getBtnDone(), watchdogListAddEditScreen.getBtnCancel());
				
				newStage.setOnCloseRequest(windowEvent ->
				{
					windowEvent.consume();
					watchdogListAddEditScreen.getBtnCancel().fire();
				});
			}
		};
	}
	
	private void initSaveRuleListButton()
	{
		controller.getBtnSaveRuleList().setOnAction(event ->
		{
			TextInputDialog dialog = new TextInputDialog();
			dialog.setTitle("Save Rule List");
			dialog.setHeaderText("Save this rule list for future use");
			dialog.setContentText("Please enter rule list name:");

			Optional<String> result = dialog.showAndWait();

			result.ifPresent(filename ->
			{
				String fullName = filename + WatchdogUI.ruleListExtension;
				boolean alreadyExists = false;

				if (new File(fullName).exists()) //if filename already exists
				{
					Alert overwriteDialog = new Alert(AlertType.CONFIRMATION,
							"A rule list with that name already exists. Press \"OK\" to overwrite the rule list or \"Cancel\" to close this dialog without saving the new rule list.");
					overwriteDialog.setTitle("Rule list name already exists");
					overwriteDialog.setHeaderText("Overwrite existing rule list?");

					Optional<ButtonType> overwriteResult = overwriteDialog.showAndWait();
					if (overwriteResult.get() == ButtonType.CANCEL)
						return;

					alreadyExists = true;
				}

				try
				{
					saveListToFile(fullName);
				}
				catch (IOException ioe)
				{
					new Alert(AlertType.ERROR, "Unable to save rule list: " + ioe.getMessage()).showAndWait();
					return;
				}

				MenuItem menuItem = createMenuItem(filename);

				ObservableList<MenuItem> items = controller.getMenuBtnLoadRuleList().getItems();

				if (alreadyExists)
					return;

				if (items.get(0).isDisable()) //it only contains the disabled "none found " item, remove it before adding new one
					items.clear();

				items.add(menuItem);
			});
		});
	}

	private void initLoadRuleListButton()
	{
		ObservableList<MenuItem> items = controller.getMenuBtnLoadRuleList().getItems();

		File dir = new File(System.getProperty("user.dir"));
		FileFilter fileFilter = new WildcardFileFilter("*" + WatchdogUI.ruleListExtension);
		List<File> files = new ArrayList<>(Arrays.asList(dir.listFiles(fileFilter))); //ArrayList because asList() returns an immutable list

		if (files.removeIf(file -> file.getName().equals(WatchdogUI.lastRunFilename))) //if lastRun exists, remove it from the list and put it on top of the button's list
			items.add(createMenuItem(WatchdogUI.lastRunFilename.replace(WatchdogUI.ruleListExtension, "")));

		for (File file : files)
			items.add(createMenuItem(file.getName().replace(WatchdogUI.ruleListExtension, "")));

		if (items.isEmpty())
		{
			MenuItem none = new MenuItem("No rule lists found");

			none.setDisable(true);
			items.add(none);
		}
	}

	public MenuItem createMenuItem(String filename)
	{
		MenuItem menuItem = new MenuItem(filename);

		menuItem.setOnAction(event ->
		{
			try
			{
				loadListFromFile(filename + WatchdogUI.ruleListExtension);
			}
			catch (ClassNotFoundException | IOException e)
			{
				new Alert(AlertType.ERROR, "Unable to load rule list: " + e.getMessage()).showAndWait();
			}
		});

		return menuItem;
	}
	
	private void setTableRowDoubleClickToEdit()
	{
		table.setRowFactory(param ->
		{
			TableRow<PacketTypeToMatch> row = new TableRow<>();
			row.setOnMouseClicked(event ->
			{
				if (event.getClickCount() == 2 && (!row.isEmpty()))
					controller.getBtnEditRow().fire();
			});

			return row;
		});
	}

	@Override
	public void watchdogFoundMatchingPacket(PcapPacket packetThatMatched, WatchdogMessage message)
	{
		outputMessage(message);

		if (radioStopAfterMatch.isSelected())
			changeUIAccordingToListeningState(false);
	}

	private void changeUIAccordingToListeningState(boolean listening)
	{
		activeButton = (listening ? btnStop : btnStart);

		btnStop.setDisable(!listening);
		btnStart.setDisable(listening);
		paneWatchdogConfig.setDisable(listening);
		paneTableAndControls.setDisable(listening);
	}

	private void outputMessage(WatchdogMessage message)
	{
		String msg = message.getMessage();

		switch (message.getMethod())
		{
			case TTS:
				tts.speak(msg);
				break;
			case POPUP:
				Platform.runLater(() -> new Alert(AlertType.INFORMATION, msg).showAndWait());
				break;
			case TTS_AND_POPUP:
				tts.speak(msg);
				Platform.runLater(() -> new Alert(AlertType.INFORMATION, msg).showAndWait());
				break;
		}
	}

	public void saveListToFile(String filename) throws IOException
	{
		FileOutputStream fout = new FileOutputStream(filename);
		ObjectOutputStream oos = new ObjectOutputStream(fout);

		oos.writeObject(new ArrayList<>(ruleList));

		oos.close();
		fout.close();
	}

	@SuppressWarnings("unchecked")
	public void loadListFromFile(String filename) throws IOException, ClassNotFoundException
	{
		FileInputStream fin = new FileInputStream(filename);
		ObjectInputStream ois = new ObjectInputStream(fin);

		ArrayList<PacketTypeToMatch> temp = (ArrayList<PacketTypeToMatch>) ois.readObject();

		ruleList.clear();
		ruleList.addAll(temp);

		ois.close();
		fin.close();

		for (PacketTypeToMatch row : ruleList)
			row.initAfterSerialization();
	}

	private void setWatchdogHotkey(Properties props)
	{
		int hotkeyModifiers = PropertiesByType.getIntProperty(props, propsHotkeyModifiers);
		int hotkeyKeyCode = PropertiesByType.getIntProperty(props, propsHotkeyKeycode);
		chkboxHotkey.selectedProperty()
				.addListener(hotkeyRegistry.generateChangeListenerForHotkeyCheckbox(hotkeyID, hotkeyModifiers, hotkeyKeyCode, chkboxHotkey, labelCurrHotkey, paneHotkeyConfig, hotkeyPressed));

		chkboxHotkey.setSelected(PropertiesByType.getBoolProperty(props, propsChkboxHotkey, false));

		if (!chkboxHotkey.isSelected())
			paneHotkeyConfig.setDisable(true);
	}

	public void saveCurrentRunValuesToProperties(Properties props)
	{
		props.put(propsChkboxHotkey, ((Boolean) chkboxHotkey.isSelected()).toString());
		props.put(propsHotkeyKeycode, Integer.toString(hotkeyRegistry.getHotkeyKeycode(hotkeyID)));
		props.put(propsHotkeyModifiers, Integer.toString(hotkeyRegistry.getHotkeyModifiers(hotkeyID)));
		props.put(propsRadioStopAfterMatch, ((Boolean) radioStopAfterMatch.isSelected()).toString());
		props.put(propsRadioKeepLooking, ((Boolean) radioKeepLooking.isSelected()).toString());
		props.put(propsNumFieldCooldown, Integer.toString(numFieldCooldown.getValue()));
		props.put(propsTTSVoiceName, tts.getCurrentVoice().getVoiceName());

		try
		{
			saveListToFile(lastRunFilename);
		}
		catch (IOException ioe)
		{
			logger.log(Level.SEVERE, "Unable to save Watchdog list: " + ioe.getMessage(), ioe);
		}
	}
	
	private void loadTTS(Properties props)
	{
		String voiceName = PropertiesByType.getStringProperty(props, propsTTSVoiceName, voiceForTTS);
		tts = new MaryTTS(voiceName);
	}

	public void loadLastRunConfig(Properties props)
	{
		setWatchdogHotkey(props);
		loadTTS(props);

		numFieldCooldown.setText(PropertiesByType.getStringProperty(props, propsNumFieldCooldown, String.valueOf(defaultnCooldownValue)));

		if (PropertiesByType.getBoolProperty(props, propsRadioStopAfterMatch, true))
			radioStopAfterMatch.fire(); //this way it activates the button handler
		if (PropertiesByType.getBoolProperty(props, propsRadioKeepLooking, false))
			radioKeepLooking.fire(); //this way it activates the button handler

		try
		{
			loadListFromFile(lastRunFilename);
		}
		catch (IOException | ClassNotFoundException ioe) //ignore, don't load
		{
		}
	}

	public ObservableList<PacketTypeToMatch> getRuleList()
	{
		return ruleList;
	}

	@Override
	public void setTTSVoice(TTSVoice voice)
	{
		tts.setVoice(voice);
	}

	@Override
	public TTSVoice getTTSVoice()
	{
		return tts.getCurrentVoice();
	}
}
