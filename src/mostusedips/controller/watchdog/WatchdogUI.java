package mostusedips.controller.watchdog;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import mostusedips.controller.GUIController;
import mostusedips.controller.HotkeyRegistry;
import mostusedips.model.ipsniffer.FirstSightListener;
import mostusedips.model.ipsniffer.IPToMatch;
import mostusedips.model.ipsniffer.IpSniffer;
import mostusedips.model.tts.TextToSpeech;

public class WatchdogUI implements FirstSightListener
{
	private final static Logger logger = Logger.getLogger(WatchdogUI.class.getPackage().getName());
	
	private final static String watchdogHotkeyID = "Watchdog hotkey";
	private final static String watchdogListFormLocation = "/mostusedips/view/WatchdogList.fxml";
	private static final String watchdogLastRunFilename = "Last run.watchdogPreset";
	private final static String voiceForTTS = "kevin16";

	private final static String propsChkboxWatchdogHotkey = "chkboxWatchdogHotkey";
	private final static String propsWatchdogHotkeyKeycode = "watchdogHotkeyKeycode";
	private final static String propsWatchdogHotkeyModifiers = "watchdogHotkeyModifiers";

	
	private GUIController controller;
	
	private CheckBox chkboxWatchdogHotkey;
	private AnchorPane paneWatchdogHotkeyConfig;
	private Button btnWatchdogConfigureHotkey;
	private Label labelWatchdogCurrHotkey;
	private Button btnWatchdogStart;
	private Button btnWatchdogStop;
	private TextField textWatchdogMessage;
	private Button btnWatchdogManageList;
	private Button btnWatchdogPreview;
	private Label labelWatchdogEntryCount;
	private Button watchdogActiveButton;
	
	private int watchdogHotkeyKeyCode;
	private int watchdogHotkeyModifiers;
	private ObservableList<IPToMatch> watchdogList = FXCollections.observableArrayList();
	private TextToSpeech tts = new TextToSpeech(voiceForTTS);
	private IpSniffer sniffer;
	
	private Runnable watchdogHotkeyPressed = new Runnable()
	{
		@Override
		public void run()
		{
			String line;
			Button savedActiveButton = watchdogActiveButton;

			watchdogActiveButton.fire();

			if (savedActiveButton == btnWatchdogStart)
			{
				if (watchdogList.isEmpty())
					return;

				line = "Starting watchdog";
				watchdogActiveButton = btnWatchdogStop;
				btnWatchdogStart.setDisable(true);
				btnWatchdogStop.setDisable(false);
			}
			else
			{
				line = "Stopping watchdog";
				watchdogActiveButton = btnWatchdogStart;
				btnWatchdogStop.setDisable(true);
				btnWatchdogStart.setDisable(false);
			}

			tts.speak(line);
		}
	};

	private HotkeyRegistry hotkeyRegistry;
	
	
	public WatchdogUI(GUIController controller)
	{
		this.controller = controller;
		
		
		initUIElementsFromController();
		
		initButtonHandlers();
		
	}
	
	private void initUIElementsFromController()
	{
		hotkeyRegistry = controller.getHotkeyRegistry();
		sniffer = controller.getIpSniffer();
		
		chkboxWatchdogHotkey = controller.getChkboxWatchdogHotkey();
		paneWatchdogHotkeyConfig = controller.getPaneWatchdogHotkeyConfig();
		btnWatchdogConfigureHotkey = controller.getBtnWatchdogConfigureHotkey();
		labelWatchdogCurrHotkey = controller.getLabelWatchdogCurrHotkey();
		btnWatchdogStart = controller.getBtnWatchdogStart();
		btnWatchdogStop = controller.getBtnWatchdogStop();
		textWatchdogMessage = controller.getTextWatchdogMessage();
		btnWatchdogManageList = controller.getBtnWatchdogManageList();
		btnWatchdogPreview = controller.getBtnWatchdogPreview();
		labelWatchdogEntryCount = controller.getLabelWatchdogEntryCount();
		
	}

	private void initButtonHandlers()
	{
		chkboxWatchdogHotkey.selectedProperty().addListener(hotkeyRegistry.generateChangeListenerForHotkeyCheckbox(watchdogHotkeyID, watchdogHotkeyModifiers, watchdogHotkeyKeyCode, chkboxWatchdogHotkey,
				labelWatchdogCurrHotkey, paneWatchdogHotkeyConfig, watchdogHotkeyPressed));
		
		btnWatchdogConfigureHotkey.setOnAction(hotkeyRegistry.generateEventHandlerForHotkeyConfigButton(watchdogHotkeyID));

		btnWatchdogManageList.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				WatchdogManageListScreen watchdogManageListScreen;
				Stage stage = (Stage) controller.getTabPane().getScene().getWindow();

				try
				{
					watchdogManageListScreen = new WatchdogManageListScreen(watchdogListFormLocation, stage, stage.getScene(), watchdogList, textWatchdogMessage, labelWatchdogEntryCount);
				}
				catch (IOException e)
				{
					logger.log(Level.SEVERE, "Unable to load watchdog list screen", e);
					return;
				}

				watchdogManageListScreen.showScreenOnNewStage("Manage Watchdog list", watchdogManageListScreen.getCloseButton());
			}
		});

		btnWatchdogPreview.setOnAction(new EventHandler<ActionEvent>()
		{

			@Override
			public void handle(ActionEvent event)
			{
				tts.speak(textWatchdogMessage.getText());
			}
		});

		FirstSightListener thisObj = this;

		btnWatchdogStart.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				if (sniffer.isCaptureInProgress())
				{
					new Alert(AlertType.ERROR, "There's already a capture in progress. Only one capture at a time is allowed.").showAndWait();
					return;
				}

				if (watchdogList.isEmpty())
				{
					new Alert(AlertType.ERROR, "The list must contain at least one entry").showAndWait();
					return;
				}

				String deviceIP = controller.getButtonToIpMap().get(controller.getTglGrpNIC().getSelectedToggle());
				new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						sniffer.startFirstSightCapture(deviceIP, new ArrayList<IPToMatch>(watchdogList), thisObj, new StringBuilder());
					}
				}).start();

				watchdogActiveButton = btnWatchdogStop;
				btnWatchdogStop.setDisable(false);
				btnWatchdogStart.setDisable(true);
			}
		});

		btnWatchdogStop.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				watchdogActiveButton = btnWatchdogStart;
				btnWatchdogStop.setDisable(true);
				btnWatchdogStart.setDisable(false);

				sniffer.stopCapture();
			}
		});

		watchdogActiveButton = btnWatchdogStart;
	}

	@Override
	public void firstSightOfIP(IPToMatch ipInfo)
	{
		tts.speak(textWatchdogMessage.getText());

		watchdogActiveButton = btnWatchdogStart;
		btnWatchdogStop.setDisable(true);
		btnWatchdogStart.setDisable(false);
	}
	
	public static void watchdogSaveListToFile(ArrayList<IPToMatch> list, String msgToSay, String filename) throws IOException
	{
		FileOutputStream fout = new FileOutputStream(filename);
		ObjectOutputStream oos = new ObjectOutputStream(fout);

		oos.writeObject(list);
		oos.writeUTF(msgToSay);

		oos.close();
		fout.close();
	}

	@SuppressWarnings("unchecked")
	public static void watchdogLoadListFromFile(ObservableList<IPToMatch> listToLoadInto, TextField messageField, Label labelCounter, String filename) throws IOException, ClassNotFoundException
	{
		FileInputStream fin = new FileInputStream(filename);
		ObjectInputStream ois = new ObjectInputStream(fin);

		ArrayList<IPToMatch> temp = (ArrayList<IPToMatch>) ois.readObject();

		listToLoadInto.clear();
		listToLoadInto.addAll(temp);

		messageField.setText(ois.readUTF());

		ois.close();
		fin.close();

		for (IPToMatch entry : listToLoadInto)
			entry.initAfterSerialization();

		labelCounter.setText("Match list contains " + listToLoadInto.size() + " entries");
	}
	
	private void setWatchdogHotkey(Properties props)
	{
		chkboxWatchdogHotkey.setSelected(GUIController.getBoolProperty(props, propsChkboxWatchdogHotkey));
		watchdogHotkeyModifiers = GUIController.getIntProperty(props, propsWatchdogHotkeyModifiers);
		watchdogHotkeyKeyCode = GUIController.getIntProperty(props, propsWatchdogHotkeyKeycode);

		if (chkboxWatchdogHotkey.isSelected())
			hotkeyRegistry.addHotkey(watchdogHotkeyID, watchdogHotkeyModifiers, watchdogHotkeyKeyCode, labelWatchdogCurrHotkey, watchdogHotkeyPressed);
		else
			paneWatchdogHotkeyConfig.setDisable(true);
	}
	
	public void saveCurrentRunValuesToProperties(Properties props)
	{
		props.put(propsChkboxWatchdogHotkey, ((Boolean) chkboxWatchdogHotkey.isSelected()).toString());
		props.put(propsWatchdogHotkeyKeycode, Integer.toString(hotkeyRegistry.getHotkeyKeycode(watchdogHotkeyID)));
		props.put(propsWatchdogHotkeyModifiers, Integer.toString(hotkeyRegistry.getHotkeyModifiers(watchdogHotkeyID)));
		
		try
		{
			watchdogSaveListToFile(new ArrayList<IPToMatch>(watchdogList), textWatchdogMessage.getText(), watchdogLastRunFilename);
		}
		catch (IOException ioe)
		{
			logger.log(Level.SEVERE, "Unable to save Watchdog list: " + ioe.getMessage(), ioe);
		}
	}
	
	public void loadLastRunConfig(Properties props)
	{
		setWatchdogHotkey(props);
		
		try
		{
			watchdogLoadListFromFile(watchdogList, textWatchdogMessage, labelWatchdogEntryCount, watchdogLastRunFilename);
		}
		catch (IOException | ClassNotFoundException ioe) //ignore, don't load
		{
		}
	}
}
