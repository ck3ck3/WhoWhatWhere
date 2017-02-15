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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.jnetpcap.packet.PcapPacket;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import numbertextfield.NumberTextField;
import whowhatwhere.controller.GUIController;
import whowhatwhere.controller.HotkeyRegistry;
import whowhatwhere.controller.LoadAndSaveSettings;
import whowhatwhere.model.PropertiesByType;
import whowhatwhere.model.TextToSpeech;
import whowhatwhere.model.networksniffer.NICInfo;
import whowhatwhere.model.networksniffer.NetworkSniffer;
import whowhatwhere.model.networksniffer.watchdog.PacketTypeToMatch;
import whowhatwhere.model.networksniffer.watchdog.WatchdogListener;
import whowhatwhere.model.networksniffer.watchdog.WatchdogMessage;

public class WatchdogUI implements WatchdogListener, LoadAndSaveSettings
{
	private final static Logger logger = Logger.getLogger(WatchdogUI.class.getPackage().getName());
	private final static String watchdogListAddEditFormLocation = "/whowhatwhere/view/fxmls/watchdog/AddEditEntry.fxml";

	public final static String presetExtension = ".watchdogPreset";
	public final static String lastRunFilename = "Last run" + presetExtension;
	public final static int minCooldownValue = 1;
	public final static int defaultnCooldownValue = 3;
	public final static int maxCooldownValue = 60 * 60 * 24; //24 hours
	
	private final static String hotkeyID = "Watchdog hotkey";
	private final static String voiceForTTS = GUIController.voiceForTTS;

	private final static String propsChkboxHotkey = "chkboxWatchdogHotkey";
	private final static String propsHotkeyKeycode = "watchdogHotkeyKeycode";
	private final static String propsHotkeyModifiers = "watchdogHotkeyModifiers";
	private final static String propsRadioStopAfterMatch = "radioStopAfterMatch";
	private final static String propsRadioKeepLooking = "radioKeepLooking";
	private final static String propsNumFieldCooldown = "numFieldCooldown";

	private GUIController guiController;
	private WatchdogController controller;

	private CheckBox chkboxHotkey;
	private AnchorPane paneHotkeyConfig;
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

	private int hotkeyKeyCode;
	private int hotkeyModifiers;
	private ObservableList<PacketTypeToMatch> entryList = FXCollections.observableArrayList();
	private TextToSpeech tts = new TextToSpeech(voiceForTTS);
	private NetworkSniffer sniffer = new NetworkSniffer();
	private HotkeyRegistry hotkeyRegistry;
	private Map<String, List<String>> userNotesToIPListMap;
	
	private Runnable hotkeyPressed = new Runnable()
	{
		@Override
		public void run()
		{
			String line;
			Button savedActiveButton = activeButton;

			activeButton.fire();

			if (savedActiveButton == btnStart)
			{
				if (entryList.isEmpty())
					return;

				line = "Starting watchdog";
				changeUIAccordingToListeningState(true);
			}
			else
			{
				line = "Stopping watchdog";
				changeUIAccordingToListeningState(false);
			}

			tts.speak(line);
		}
	};

	public WatchdogUI(GUIController guiController)
	{
		this.controller = guiController.getWatchdogTabController();
		this.guiController = guiController;
		this.guiController.registerForSettingsHandler(this);

		initUIElementsFromController();
		initButtonHandlers();
		
		table.setItems(entryList);
		userNotesToIPListMap = getUserNotesReverseMap();
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
		initRemoveEntryButton();
		
		initSavePresetButton();
		initLoadPresetButton();

		radioStopAfterMatch.setOnAction(event -> paneCooldown.setDisable(true));
		radioKeepLooking.setOnAction(event -> paneCooldown.setDisable(false));

		btnStart.setOnAction(event ->
		{
			if (entryList.isEmpty())
			{
				new Alert(AlertType.ERROR, "The list must contain at least one entry").showAndWait();
				return;
			}

			NICInfo deviceInfo = guiController.getSelectedNIC();
			new Thread(() ->
			{
				StringBuilder errorBuffer = new StringBuilder();

				try
				{
					sniffer.startWatchdogCapture(deviceInfo, entryList, radioKeepLooking.isSelected(), numFieldCooldown.getValue(), thisObj, errorBuffer);
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
	
	private void initRemoveEntryButton()
	{
		controller.getBtnRemoveRow().setOnAction(event ->
		{
			ObservableList<PacketTypeToMatch> selectedItems = table.getSelectionModel().getSelectedItems();

			if (selectedItems.isEmpty())
			{
				new Alert(AlertType.ERROR, "No entries selected.").showAndWait();
				return;
			}
			
			String entryOrEntries = selectedItems.size() == 1 ? "entry" : "entries"; 
			Alert removalConfirmation = new Alert(AlertType.CONFIRMATION, "Are you sure you want to remove the selected " + entryOrEntries + "?");
			removalConfirmation.setTitle("Entry removal confirmation");
			removalConfirmation.setHeaderText("Remove " + entryOrEntries);
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
						errorMsg = "Please select an entry to edit";
					else
						if (numOfSelectedRows > 1)
							errorMsg = "Only one entry must be selected for edit";
					
					if (numOfSelectedRows != 1)
					{
						new Alert(AlertType.ERROR, errorMsg).showAndWait();
						return;
					}
				}

				try
				{
					watchdogListAddEditScreen = new ListAddEditScreen(watchdogListAddEditFormLocation, stage, stage.getScene(), table, userNotesToIPListMap, isEdit);
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

				Stage newStage = watchdogListAddEditScreen.showScreenOnNewStage((isEdit ? "Edit" : "Add") + " an entry", Modality.APPLICATION_MODAL, watchdogListAddEditScreen.getBtnDone(), watchdogListAddEditScreen.getBtnCancel());
				
				newStage.setOnCloseRequest(windowEvent ->
				{
					windowEvent.consume();
					watchdogListAddEditScreen.getBtnCancel().fire();
				});
			}
		};
	}
	
	private void initSavePresetButton()
	{
		controller.getBtnSavePreset().setOnAction(event ->
		{
			TextInputDialog dialog = new TextInputDialog();
			dialog.setTitle("Save Preset");
			dialog.setHeaderText("Save this preset for future use");
			dialog.setContentText("Please enter preset name:");

			Optional<String> result = dialog.showAndWait();

			result.ifPresent(filename ->
			{
				String fullName = filename + WatchdogUI.presetExtension;
				boolean alreadyExists = false;

				if (new File(fullName).exists()) //if filename already exists
				{
					Alert overwriteDialog = new Alert(AlertType.CONFIRMATION,
							"A preset with that name already exists. Press \"OK\" to overwrite the preset or \"Cancel\" to close this dialog without saving the new preset.");
					overwriteDialog.setTitle("Preset name already exists");
					overwriteDialog.setHeaderText("Overwrite existing preset?");

					Optional<ButtonType> overwriteResult = overwriteDialog.showAndWait();
					if (overwriteResult.get() == ButtonType.CANCEL)
						return;

					alreadyExists = true;
				}

				try
				{
					WatchdogUI.saveListToFile(entryList, fullName);
				}
				catch (IOException ioe)
				{
					new Alert(AlertType.ERROR, "Unable to save preset: " + ioe.getMessage()).showAndWait();
					return;
				}

				MenuItem menuItem = createMenuItem(entryList, filename);

				ObservableList<MenuItem> items = controller.getMenuBtnLoadPreset().getItems();

				if (alreadyExists)
					return;

				if (items.get(0).isDisable()) //it only contains the disabled "none found " item, remove it before adding new one
					items.clear();

				items.add(menuItem);
			});
		});
	}

	private void initLoadPresetButton()
	{
		ObservableList<MenuItem> items = controller.getMenuBtnLoadPreset().getItems();

		File dir = new File(System.getProperty("user.dir"));
		FileFilter fileFilter = new WildcardFileFilter("*" + WatchdogUI.presetExtension);
		List<File> files = new ArrayList<File>(Arrays.asList(dir.listFiles(fileFilter))); //ArrayList because asList() returns an immutable list

		if (files.removeIf(file -> file.getName().equals(WatchdogUI.lastRunFilename))) //if lastRun exists, remove it from the list and put it on top of the button's list
			items.add(createMenuItem(entryList, WatchdogUI.lastRunFilename.replace(WatchdogUI.presetExtension, "")));

		for (File file : files)
			items.add(createMenuItem(entryList, file.getName().replace(WatchdogUI.presetExtension, "")));

		if (items.isEmpty())
		{
			MenuItem none = new MenuItem("No presets found");

			none.setDisable(true);
			items.add(none);
		}
	}

	public static MenuItem createMenuItem(ObservableList<PacketTypeToMatch> list, String filename)
	{
		MenuItem menuItem = new MenuItem(filename);

		menuItem.setOnAction(event ->
		{
			try
			{
				WatchdogUI.loadListFromFile(list, filename + WatchdogUI.presetExtension);
			}
			catch (ClassNotFoundException | IOException e)
			{
				new Alert(AlertType.ERROR, "Unable to load preset: " + e.getMessage()).showAndWait();
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

	public static void saveListToFile(List<PacketTypeToMatch> list, String filename) throws IOException
	{
		FileOutputStream fout = new FileOutputStream(filename);
		ObjectOutputStream oos = new ObjectOutputStream(fout);

		oos.writeObject(new ArrayList<>(list));

		oos.close();
		fout.close();
	}

	@SuppressWarnings("unchecked")
	public static void loadListFromFile(ObservableList<PacketTypeToMatch> listToLoadInto, String filename) throws IOException, ClassNotFoundException
	{
		FileInputStream fin = new FileInputStream(filename);
		ObjectInputStream ois = new ObjectInputStream(fin);

		ArrayList<PacketTypeToMatch> temp = (ArrayList<PacketTypeToMatch>) ois.readObject();

		listToLoadInto.clear();
		listToLoadInto.addAll(temp);

		ois.close();
		fin.close();

		for (PacketTypeToMatch entry : listToLoadInto)
			entry.initAfterSerialization();
	}

	private void setWatchdogHotkey(Properties props)
	{
		hotkeyModifiers = PropertiesByType.getIntProperty(props, propsHotkeyModifiers);
		hotkeyKeyCode = PropertiesByType.getIntProperty(props, propsHotkeyKeycode);
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

		try
		{
			saveListToFile(entryList, lastRunFilename);
		}
		catch (IOException ioe)
		{
			logger.log(Level.SEVERE, "Unable to save Watchdog list: " + ioe.getMessage(), ioe);
		}
	}

	public void loadLastRunConfig(Properties props)
	{
		setWatchdogHotkey(props);

		numFieldCooldown.setText(PropertiesByType.getStringProperty(props, propsNumFieldCooldown, String.valueOf(defaultnCooldownValue)));

		if (PropertiesByType.getBoolProperty(props, propsRadioStopAfterMatch, true))
			radioStopAfterMatch.fire(); //this way it activates the button handler
		if (PropertiesByType.getBoolProperty(props, propsRadioKeepLooking, false))
			radioKeepLooking.fire(); //this way it activates the button handler

		try
		{
			loadListFromFile(entryList, lastRunFilename);
		}
		catch (IOException | ClassNotFoundException ioe) //ignore, don't load
		{
		}
	}

	public ObservableList<PacketTypeToMatch> getEntryList()
	{
		return entryList;
	}

	/**
	 * @return a map that maps user note to a list of IPs that have that note
	 */
	public Map<String, List<String>> getUserNotesReverseMap()
	{
		return guiController.getUserNotes().getUserNotesReverseMap();
	}
}
