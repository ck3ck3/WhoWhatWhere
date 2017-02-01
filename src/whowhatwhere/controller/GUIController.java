package whowhatwhere.controller;

import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import numbertextfield.NumberTextField;
import whowhatwhere.Main;
import whowhatwhere.controller.appearancecounter.AppearanceCounterUI;
import whowhatwhere.controller.appearancecounter.IPInfoRowModel;
import whowhatwhere.controller.utilities.PingToSpeechUI;
import whowhatwhere.controller.watchdog.WatchdogUI;
import whowhatwhere.model.PropertiesByType;
import whowhatwhere.model.TextToSpeech;
import whowhatwhere.model.networksniffer.NICInfo;
import whowhatwhere.model.networksniffer.NetworkSniffer;
import whowhatwhere.model.startwithwindows.StartWithWindowsRegistryUtils;

public class GUIController implements CheckForUpdatesResultHandler
{
	private final static String NICSelectionFormLocation = "/whowhatwhere/view/NICSelectionForm.fxml";
	private final static String propsFileLocation = Main.getAppName() + ".properties";
	private final static String defaultPropsResource = "/defaultLastRun.properties";

	private final static String propsNICDescription = "Selected NIC description";
	private final static String propsTraceAddress = "traceAddress";
	private final static String propsShowMessageOnMinimize = "showMinimizeMessage";
	private final static String propsStartMinimized = "startMinimized";
	private final static String propsIgnoreRunPathDiff = "ignorePathDiff";
	private final static String propsCheckForUpdatesOnStartup = "checkForUpdatesOnStartup";
	private final static String propsWidth = "lastRunWidth";
	private final static String propsHeight = "lastRunHeight";
	private final static String propsX = "lastRunX";
	private final static String propsY = "lastRunY";

	private final static String voiceForTTS = "kevin16";

	private final static Logger logger = Logger.getLogger(GUIController.class.getPackage().getName());

	@FXML
	private ScrollPane scrollPaneMainForm;
	@FXML
	private AnchorPane paneCaptureOptions;
	@FXML
	private CheckBox chkboxFilterProtocols;
	@FXML
	private CheckBox chkboxUDP;
	@FXML
	private CheckBox chkboxTCP;
	@FXML
	private CheckBox chkboxICMP;
	@FXML
	private CheckBox chkboxHTTP;
	@FXML
	private CheckBox chkboxTimedCapture;
	@FXML
	private Button btnStart;
	@FXML
	private Button btnStop;
	@FXML
	private Label labelStatus;
	@FXML
	private Button btnExit;
	@FXML
	private CheckBox chkboxGetLocation;
	@FXML
	private TableView<IPInfoRowModel> tableResults;
	@FXML
	private TableColumn<IPInfoRowModel, Integer> columnPacketCount;
	@FXML
	private TableColumn<IPInfoRowModel, String> columnIP;
	@FXML
	private TableColumn<IPInfoRowModel, String> columnNotes;
	@FXML
	private TableColumn<IPInfoRowModel, String> columnOwner;
	@FXML
	private TableColumn<IPInfoRowModel, String> columnPing;
	@FXML
	private TableColumn<IPInfoRowModel, String> columnCountry;
	@FXML
	private TableColumn<IPInfoRowModel, String> columnRegion;
	@FXML
	private TableColumn<IPInfoRowModel, String> columnCity;
	@FXML
	private Label labelCurrCaptureHotkey;
	@FXML
	private CheckBox chkboxUseCaptureHotkey;
	@FXML
	private Button btnConfigCaptureHotkey;
	@FXML
	private AnchorPane paneEnableCaptureHotkey;
	@FXML
	private CheckBox chkboxUseTTS;
	@FXML
	private AnchorPane paneUseTTS;
	@FXML
	private GridPane gridPaneColumnNames;
	@FXML
	private Label labelReadFirstRows;
	@FXML
	private CheckBox chkboxPing;
	@FXML
	private CheckBox chkboxFilterResults;
	@FXML
	private Pane paneFilterResults;
	@FXML
	private ComboBox<String> comboColumns;
	@FXML
	private TextField textColumnContains;
	@FXML
	private MenuItem menuItemMinimize;
	@FXML
	private MenuItem menuItemExit;
	@FXML
	private MenuItem menuItemUpdate;
	@FXML
	private MenuItem menuItemAbout;
	@FXML
	private TabPane tabPane;
	@FXML
	private ComboBox<String> comboPTSipToPing;
	@FXML
	private Button btnPTSConfigureHotkey;
	@FXML
	private Label labelPTSCurrentHotkey;
	@FXML
	private Tab tabUtils;
	@FXML
	private CheckBox chkboxPTSHotkey;
	@FXML
	private AnchorPane panePTSHotkey;
	@FXML
	private CheckBox chkboxWatchdogHotkey;
	@FXML
	private AnchorPane paneWatchdogHotkeyConfig;
	@FXML
	private Button btnWatchdogConfigureHotkey;
	@FXML
	private Label labelWatchdogCurrHotkey;
	@FXML
	private Button btnWatchdogStart;
	@FXML
	private Button btnWatchdogStop;
	@FXML
	private Button btnWatchdogManageList;
	@FXML
	private Label labelWatchdogEntryCount;
	@FXML
	private TextField textTrace;
	@FXML
	private Button btnTrace;
	@FXML
	private Button btnExportTableToCSV;
	@FXML
	private MenuItem menuItemSelectNIC;
	@FXML
	private CheckMenuItem menuItemChkCheckUpdateStartup;
	@FXML
	private CheckMenuItem menuItemChkDisplayBalloon;
	@FXML
	private CheckMenuItem menuItemChkStartMinimized;
	@FXML
	private CheckMenuItem menuItemChkThisUserOnly;
	@FXML
	private CheckMenuItem menuItemChkAllUsers;
	@FXML
	private MenuItem menuItemManageNotes;
	@FXML
	private RadioButton radioWatchdogStopAfterMatch;
	@FXML
	private ToggleGroup tglWatchdogStopOrContinue;
	@FXML
	private RadioButton radioWatchdogKeepLooking;
	@FXML
	private AnchorPane paneWatchdogCooldown;
	@FXML
	private NumberTextField numFieldWatchdogCooldown;
	@FXML
	private AnchorPane paneWatchdogConfig;
	@FXML
	private NumberTextField numFieldCaptureTimeout;
	@FXML
	private NumberTextField numFieldRowsToRead;
	@FXML
	private NumberTextField numFieldPingTimeout;
	@FXML
	private Pane paneProtocolBoxes;

	NICInfo selectedNIC;
	private NetworkSniffer sniffer;
	private TextToSpeech tts = new TextToSpeech(voiceForTTS);
	private HotkeyRegistry hotkeyRegistry;
	private boolean isExitAlreadyAddedToSystray = false;
	private boolean showMessageOnMinimize;
	private boolean ignoreRunPathDiff;
	private boolean checkForUpdatesOnStartup;

	private AppearanceCounterUI appearanceCounterUI;
	private PingToSpeechUI pingToSpeechUI;
	private WatchdogUI watchdogUI;

	/**
	 * <b>MUST</b> be called after the stage is shown
	 */
	public void init()
	{
		try
		{
			sniffer = new NetworkSniffer();
		}
		catch (IllegalStateException ise)
		{
			if (ise.getMessage().contains("Can't find dependent libraries"))
				generateLabelAndLinkAlert(AlertType.ERROR, "Application cannot be started", "WinPcap is not installed!", "Please download and install WinPcap from",
						"http://www.winpcap.org/install/default.htm").showAndWait();
			else
				new Alert(AlertType.ERROR, "Critical error, application cannot be started:\n" + ise.getMessage()).showAndWait();

			shutdownApp();
		}

		hotkeyRegistry = new HotkeyRegistry(scrollPaneMainForm);

		appearanceCounterUI = new AppearanceCounterUI(this);
		pingToSpeechUI = new PingToSpeechUI(this);
		watchdogUI = new WatchdogUI(this);

		initButtonHandlers();
		initMenuBar();
		addExitToSystrayIcon();

		loadLastRunConfig();

		if (checkForUpdatesOnStartup)
			checkForUpdates(true); //only show a message if there is a new version
	}

	private void addExitToSystrayIcon()
	{
		if (!SystemTray.isSupported())
			return;

		SystemTray tray = SystemTray.getSystemTray();

		tray.addPropertyChangeListener("trayIcons", pce ->
		{
			TrayIcon[] trayIcons = tray.getTrayIcons();

			if (!isExitAlreadyAddedToSystray && trayIcons.length == 1) //only for the first time the systray icon appears in systray
			{
				PopupMenu popup = trayIcons[0].getPopupMenu();
				java.awt.MenuItem exit = new java.awt.MenuItem("Exit");

				exit.addActionListener(al -> exitButtonPressed());

				popup.addSeparator();
				popup.add(exit);

				isExitAlreadyAddedToSystray = true;
			}

			if (trayIcons.length == 1 && showMessageOnMinimize)
				trayIcons[0].displayMessage("Minimized to tray", "Still running in the background, double click this icon to restore the window. Use the \"Exit\" button to exit.", MessageType.INFO);
		});
	}

	private void initButtonHandlers()
	{
		btnExit.setOnAction(e -> exitButtonPressed());
		btnTrace.setOnAction(event -> appearanceCounterUI.traceCommand(textTrace.getText()));

		textTrace.setOnKeyPressed(ke ->
		{
			if (ke.getCode().equals(KeyCode.ENTER))
				btnTrace.fire();
		});
	}

	private void initMenuBar()
	{
		menuItemManageNotes.setOnAction(event -> appearanceCounterUI.openManageUserNotesScreen());
		menuItemMinimize.setOnAction(event ->
		{
			Stage stage = getStage();

			Event.fireEvent(stage, new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
		});
		menuItemExit.setOnAction(event -> exitButtonPressed());

		menuItemSelectNIC.setOnAction(ae -> showNICSelectionScreen());
		menuItemChkCheckUpdateStartup.setOnAction(ae -> checkForUpdatesOnStartup = ((CheckMenuItem) ae.getSource()).isSelected());
		menuItemChkDisplayBalloon.setOnAction(ae -> showMessageOnMinimize = ((CheckMenuItem) ae.getSource()).isSelected());
		menuItemChkAllUsers.setOnAction(handleStartWithWindowsClick(true, menuItemChkThisUserOnly));
		menuItemChkThisUserOnly.setOnAction(handleStartWithWindowsClick(false, menuItemChkAllUsers));

		menuItemUpdate.setOnAction(event -> checkForUpdates(false));
		menuItemAbout.setOnAction(event -> showAboutWindow());
	}

	private EventHandler<ActionEvent> handleStartWithWindowsClick(boolean allUsers, CheckMenuItem otherItem)
	{
		return ae ->
		{
			try
			{
				StartWithWindowsRegistryUtils.setRegistryToStartWithWindows(((CheckMenuItem) ae.getSource()).isSelected(), allUsers);
			}
			catch (IOException ioe)
			{
				logger.log(Level.SEVERE, "Unable to modify the registry for the \"start with Windows\" setting", ioe);
				new Alert(AlertType.ERROR, "Unable to modify the registry for the \"start with Windows\" setting").showAndWait();
			}

			otherItem.setSelected(false); //if one is selected, the other one has to be unselected. if the click is to de-select, the other one was already unselected as well anyway.	
		};
	}

	private void exitButtonPressed()
	{
		try
		{
			saveCurrentRunValuesToProperties();

			hotkeyRegistry.cleanup();
			sniffer.cleanup();
			tts.cleanup();
		}
		catch (Exception e) //just in case
		{
			logger.log(Level.SEVERE, "Exception while trying to close the program: ", e);
		}
		finally
		{
			shutdownApp();
		}
	}

	private void saveCurrentRunValuesToProperties()
	{
		Stage stage = getStage();
		Properties props = new Properties();

		appearanceCounterUI.saveCurrentRunValuesToProperties(props);
		pingToSpeechUI.saveCurrentRunValuesToProperties(props);
		watchdogUI.saveCurrentRunValuesToProperties(props);

		props.put(propsNICDescription, selectedNIC.getDescription());

		props.put(propsTraceAddress, textTrace.getText());
		props.put(propsCheckForUpdatesOnStartup, String.valueOf(checkForUpdatesOnStartup));
		props.put(propsShowMessageOnMinimize, String.valueOf(showMessageOnMinimize));
		props.put(propsStartMinimized, String.valueOf(menuItemChkStartMinimized.isSelected()));
		props.put(propsIgnoreRunPathDiff, String.valueOf(ignoreRunPathDiff));
		props.put(propsWidth, String.valueOf(stage.getWidth()));
		props.put(propsHeight, String.valueOf(stage.getHeight()));
		props.put(propsX, String.valueOf(stage.getX()));
		props.put(propsY, String.valueOf(stage.getY()));

		try
		{
			FileOutputStream out = new FileOutputStream(propsFileLocation);
			props.store(out, "Last run configuration");
			out.close();
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "Unable to save properties file " + propsFileLocation, e);
		}
	}

	private Properties loadProperties()
	{
		InputStream in;
		File lastRun = new File(propsFileLocation);
		Properties props = new Properties();

		try
		{
			in = ((lastRun.exists() && lastRun.length() > 0) ? new FileInputStream(lastRun) : this.getClass().getResourceAsStream(defaultPropsResource));

			props.load(in);
			in.close();
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "Unable to load properties file: " + e.getMessage(), e);
		}

		return props;
	}

	private void loadLastRunConfig()
	{
		Properties props = loadProperties();

		loadNICInfo(PropertiesByType.getStringProperty(props, propsNICDescription, ""));

		textTrace.setText(props.getProperty(propsTraceAddress));

		checkForUpdatesOnStartup = PropertiesByType.getBoolProperty(props, propsCheckForUpdatesOnStartup, true);
		menuItemChkCheckUpdateStartup.setSelected(checkForUpdatesOnStartup);

		showMessageOnMinimize = PropertiesByType.getBoolProperty(props, propsShowMessageOnMinimize, true);
		menuItemChkDisplayBalloon.setSelected(showMessageOnMinimize);

		boolean startMinimized = PropertiesByType.getBoolProperty(props, propsStartMinimized, false);
		menuItemChkStartMinimized.setSelected(startMinimized);
		if (startMinimized)
			Platform.runLater(() -> menuItemMinimize.fire());

		ignoreRunPathDiff = PropertiesByType.getBoolProperty(props, propsIgnoreRunPathDiff, false);
		loadStartWithWindowsSetting();

		loadLastRunDimensions(props);

		appearanceCounterUI.loadLastRunConfig(props);
		pingToSpeechUI.loadLastRunConfig(props);
		watchdogUI.loadLastRunConfig(props);
	}

	private void loadNICInfo(String nicDescription)
	{
		NICInfo nic = null;

		if (!nicDescription.isEmpty()) //we have a previously chosen NIC's description
		{
			nic = getNICByDescription(nicDescription);
			if (nic != null)
				selectedNIC = nic;
		}

		if (nic == null) //couldn't find the NIC
		{
			List<NICInfo> listOfDevices = sniffer.getListOfDevices();

			if (listOfDevices.size() == 1) //if there's only one option
				selectedNIC = listOfDevices.get(0);
			else
				showNICSelectionScreen();
		}
	}

	private void showNICSelectionScreen()
	{
		List<NICInfo> listOfDevices = sniffer.getListOfDevices();

		if (listOfDevices == null || listOfDevices.size() == 0)
		{
			new Alert(AlertType.ERROR, "Unable to find any network interfaces. Terminating application.").showAndWait();
			logger.log(Level.SEVERE, "Unable to find any network interfaces");
			shutdownApp();
		}

		Stage stage = getStage();

		if (selectedNIC == null)
			selectedNIC = new NICInfo();

		NICSelectionScreen selectionScreen = null;

		try
		{
			selectionScreen = new NICSelectionScreen(NICSelectionFormLocation, stage, stage.getScene(), scrollPaneMainForm, selectedNIC);
		}
		catch (Exception e)
		{
			new Alert(AlertType.ERROR, "Unable to load network adapter selection screen. Terminating application.").showAndWait();
			logger.log(Level.SEVERE, "Unable to load network adapter selection screen", e);
			shutdownApp();
		}

		scrollPaneMainForm.setDisable(true);
		Stage newStage = selectionScreen.showScreenOnNewStage("Choose a network adapter", Modality.APPLICATION_MODAL, selectionScreen.getCloseButton());

		newStage.toFront();
		newStage.setOnCloseRequest(windowEvent ->
		{
			if (selectedNIC.getDescription() == null) //if we don't have a NIC set
			{
				windowEvent.consume();
				new Alert(AlertType.ERROR, "You must select a network adapter.").showAndWait();
			}
			else //just close the window and restore gui functionality
				scrollPaneMainForm.setDisable(false);
		});
	}

	private NICInfo getNICByDescription(String description)
	{
		for (NICInfo nic : sniffer.getListOfDevices())
			if (description.equals(nic.getDescription()))
				return nic;

		return null;
	}

	private void loadLastRunDimensions(Properties props)
	{
		Stage stage = getStage();
		Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
		
		if (primaryScreenBounds.getHeight() < stage.getHeight() || primaryScreenBounds.getWidth() < stage.getWidth())
		{
			scrollPaneMainForm.setFitToWidth(false);
			scrollPaneMainForm.setFitToHeight(false);
			stage.setMaximized(true);
		}
		else
		{
			Double value;
			
			value = PropertiesByType.getDoubleProperty(props, propsWidth, Double.NaN);
			if (!value.equals(Double.NaN))
				stage.setWidth(value);
	
			value = PropertiesByType.getDoubleProperty(props, propsHeight, Double.NaN);
			if (!value.equals(Double.NaN))
				stage.setHeight(value);
	
			value = PropertiesByType.getDoubleProperty(props, propsX, Double.NaN);
			if (!value.equals(Double.NaN))
				stage.setX(value);
	
			value = PropertiesByType.getDoubleProperty(props, propsY, Double.NaN);
			if (!value.equals(Double.NaN))
				stage.setY(value);
		}
	}

	private void loadStartWithWindowsSetting()
	{
		try
		{
			String allUsers = StartWithWindowsRegistryUtils.getExecutableLocationToStartWithWindows(true);
			String currentUser = StartWithWindowsRegistryUtils.getExecutableLocationToStartWithWindows(false);

			if (allUsers != null || currentUser != null) //only one or none of these are supposed to be set. Never both.
			{
				String locationToStartFrom = (allUsers == null ? currentUser : allUsers);
				String currentRunLocation = System.getProperty("user.dir") + "\\" + Main.getExecutablefilename();

				if (!ignoreRunPathDiff && !currentRunLocation.equalsIgnoreCase(locationToStartFrom))
				{
					boolean forAllUsers = allUsers != null;
					String message = Main.getAppName() + " is set to run when Windows starts, but you are currently running it from a different path than the one Windows is set to run it from.\n"
							+ "Current path: " + currentRunLocation + "\nWindows is set to run it from: " + locationToStartFrom + "\n\nPlease choose how to proceed:";

					boolean userChoseDelete = showStartupPathConflictDialog(forAllUsers, message);

					if (userChoseDelete)
						return; //don't setSelected
				}
			}

			menuItemChkAllUsers.setSelected(allUsers != null);
			menuItemChkThisUserOnly.setSelected(currentUser != null);
		}
		catch (IOException ioe)
		{
			logger.log(Level.SEVERE, "Failed querying the registry for StartWithWindows values", ioe);
		}
	}

	/**
	 * @param forAllUsers
	 *            - true if the startup setting is for all users, false if for
	 *            current user only
	 * @param message
	 *            - the message to display in the dialog
	 * @return true if the user chose to delete the program from startup, false
	 *         otherwise.
	 * @throws IOException
	 *             - if there was a problem modifying the registry
	 */
	private boolean showStartupPathConflictDialog(boolean forAllUsers, String message) throws IOException
	{
		ButtonType btnModify = new ButtonType("Set to run from current path");
		ButtonType btnDelete = new ButtonType("Don't run when Windows starts");
		ButtonType btnIgnore = new ButtonType("Ignore this in the future");

		Optional<ButtonType> result = new Alert(AlertType.CONFIRMATION, message, btnModify, btnDelete, btnIgnore).showAndWait();
		ButtonType chosenButton = result.get();

		if (chosenButton == btnModify)
			StartWithWindowsRegistryUtils.setRegistryToStartWithWindows(true, forAllUsers);
		else
			if (chosenButton == btnDelete)
			{
				StartWithWindowsRegistryUtils.setRegistryToStartWithWindows(false, forAllUsers);
				return true;
			}
			else
				if (chosenButton == btnIgnore)
					ignoreRunPathDiff = true;

		return false;
	}

	private void shutdownApp()
	{
		Platform.setImplicitExit(true); //was initially set to false when initializing the systray
		Platform.exit();
		System.exit(0); //needed because of the AWT SysTray		
	}

	private void showAboutWindow()
	{
		String appName = Main.getAppName();
		String version = Main.getReleaseVersion();
		String website = Main.getWebsite();

		Alert about = generateLabelAndLinkAlert(AlertType.INFORMATION, "About " + appName, appName + " version " + version, "For more information visit ", website);

		about.showAndWait();
	}

	/**
	 * @param silent
	 *            - if true, a message will be shown only if there's a new
	 *            update. if false, a message will be shown regardless.
	 */
	private void checkForUpdates(boolean silent)
	{
		new Thread(() ->
		{
			try
			{
				Main.isUpdateAvailable(this, silent);
			}
			catch (IOException e)
			{
				if (!silent)
				{
					Platform.runLater(() ->
					{
						Alert alert = new Alert(AlertType.ERROR);
						alert.setHeaderText("Unable to check for updates");
						alert.setContentText(e.getMessage());
						alert.showAndWait();
					});
				}

				logger.log(Level.SEVERE, "Failed to check for updates", e);
			}
		}).start();
	}

	@Override
	public void checkForUpdatesResult(boolean newVersionExists, boolean silent)
	{
		Platform.runLater(() ->
		{
			Alert alert;

			if (newVersionExists)
				alert = generateLabelAndLinkAlert(AlertType.INFORMATION, "Check for updates", "New version available!", "Download the new version at ", Main.getWebsite());
			else
			{
				alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Check for updates");
				alert.setHeaderText("No new updates available.");
				alert.setContentText("You are running the latest version.");
			}

			if (newVersionExists || !silent)
				alert.showAndWait();
		});
	}

	private Alert generateLabelAndLinkAlert(AlertType type, String title, String header, String text, String url)
	{
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(header);

		FlowPane fp = new FlowPane();
		Label lbl = new Label(text);
		Hyperlink link = new Hyperlink(url);

		link.setOnAction(event ->
		{
			Main.openInBrowser(link.getText());
			alert.close();
		});

		fp.getChildren().addAll(lbl, link);

		alert.getDialogPane().contentProperty().set(fp);

		return alert;
	}

	public AnchorPane getPaneCaptureOptions()
	{
		return paneCaptureOptions;
	}

	public CheckBox getChkboxFilterProtocols()
	{
		return chkboxFilterProtocols;
	}

	public CheckBox getChkboxUDP()
	{
		return chkboxUDP;
	}

	public CheckBox getChkboxTCP()
	{
		return chkboxTCP;
	}

	public CheckBox getChkboxICMP()
	{
		return chkboxICMP;
	}

	public CheckBox getChkboxHTTP()
	{
		return chkboxHTTP;
	}

	public CheckBox getChkboxTimedCapture()
	{
		return chkboxTimedCapture;
	}

	public Button getBtnStart()
	{
		return btnStart;
	}

	public Button getBtnStop()
	{
		return btnStop;
	}

	public Label getLabelStatus()
	{
		return labelStatus;
	}

	public Button getBtnExit()
	{
		return btnExit;
	}

	public CheckBox getChkboxGetLocation()
	{
		return chkboxGetLocation;
	}

	public TableView<IPInfoRowModel> getTableResults()
	{
		return tableResults;
	}

	public TableColumn<IPInfoRowModel, Integer> getColumnPacketCount()
	{
		return columnPacketCount;
	}

	public TableColumn<IPInfoRowModel, String> getColumnIP()
	{
		return columnIP;
	}

	public TableColumn<IPInfoRowModel, String> getColumnNotes()
	{
		return columnNotes;
	}

	public TableColumn<IPInfoRowModel, String> getColumnOwner()
	{
		return columnOwner;
	}

	public TableColumn<IPInfoRowModel, String> getColumnPing()
	{
		return columnPing;
	}

	public TableColumn<IPInfoRowModel, String> getColumnCountry()
	{
		return columnCountry;
	}

	public TableColumn<IPInfoRowModel, String> getColumnRegion()
	{
		return columnRegion;
	}

	public TableColumn<IPInfoRowModel, String> getColumnCity()
	{
		return columnCity;
	}

	public Label getLabelCurrCaptureHotkey()
	{
		return labelCurrCaptureHotkey;
	}

	public NumberTextField getNumFieldCaptureTimeout()
	{
		return numFieldCaptureTimeout;
	}

	public NumberTextField getNumFieldRowsToRead()
	{
		return numFieldRowsToRead;
	}

	public NumberTextField getNumberFieldPingTimeout()
	{
		return numFieldPingTimeout;
	}

	public CheckBox getChkboxUseCaptureHotkey()
	{
		return chkboxUseCaptureHotkey;
	}

	public Button getBtnConfigCaptureHotkey()
	{
		return btnConfigCaptureHotkey;
	}

	public AnchorPane getPaneEnableCaptureHotkey()
	{
		return paneEnableCaptureHotkey;
	}

	public CheckBox getChkboxUseTTS()
	{
		return chkboxUseTTS;
	}

	public AnchorPane getPaneUseTTS()
	{
		return paneUseTTS;
	}

	public GridPane getGridPaneColumnNames()
	{
		return gridPaneColumnNames;
	}

	public CheckBox getChkboxPing()
	{
		return chkboxPing;
	}

	public CheckBox getChkboxFilterResults()
	{
		return chkboxFilterResults;
	}

	public Pane getPaneFilterResults()
	{
		return paneFilterResults;
	}

	public ComboBox<String> getComboColumns()
	{
		return comboColumns;
	}

	public TextField getTextColumnContains()
	{
		return textColumnContains;
	}

	public ComboBox<String> getComboPTSipToPing()
	{
		return comboPTSipToPing;
	}

	public Button getBtnPTSConfigureHotkey()
	{
		return btnPTSConfigureHotkey;
	}

	public Label getLabelPTSCurrentHotkey()
	{
		return labelPTSCurrentHotkey;
	}

	public CheckBox getChkboxPTSHotkey()
	{
		return chkboxPTSHotkey;
	}

	public AnchorPane getPanePTSHotkey()
	{
		return panePTSHotkey;
	}

	public CheckBox getChkboxWatchdogHotkey()
	{
		return chkboxWatchdogHotkey;
	}

	public AnchorPane getPaneWatchdogHotkeyConfig()
	{
		return paneWatchdogHotkeyConfig;
	}

	public Button getBtnWatchdogConfigureHotkey()
	{
		return btnWatchdogConfigureHotkey;
	}

	public Label getLabelWatchdogCurrHotkey()
	{
		return labelWatchdogCurrHotkey;
	}

	public Button getBtnWatchdogStart()
	{
		return btnWatchdogStart;
	}

	public Button getBtnWatchdogStop()
	{
		return btnWatchdogStop;
	}

	public Button getBtnWatchdogManageList()
	{
		return btnWatchdogManageList;
	}

	public Label getLabelWatchdogEntryCount()
	{
		return labelWatchdogEntryCount;
	}

	public TabPane getTabPane()
	{
		return tabPane;
	}

	public Tab getUtilsTab()
	{
		return tabUtils;
	}

	public Button getBtnExportTableToCSV()
	{
		return btnExportTableToCSV;
	}

	public HotkeyRegistry getHotkeyRegistry()
	{
		return hotkeyRegistry;
	}

	public Stage getStage()
	{
		return (Stage) scrollPaneMainForm.getScene().getWindow();
	}

	public RadioButton getRadioWatchdogStopAfterMatch()
	{
		return radioWatchdogStopAfterMatch;
	}

	public RadioButton getRadioWatchdogKeepLooking()
	{
		return radioWatchdogKeepLooking;
	}

	public AnchorPane getPaneWatchdogCooldown()
	{
		return paneWatchdogCooldown;
	}

	public NumberTextField getNumFieldWatchdogCooldown()
	{
		return numFieldWatchdogCooldown;
	}

	public AnchorPane getPaneWatchdogConfig()
	{
		return paneWatchdogConfig;
	}

	public Pane getPaneProtocolBoxes()
	{
		return paneProtocolBoxes;
	}

	public NICInfo getSelectedNIC()
	{
		return selectedNIC;
	}

	/**
	 * @return a map that maps user note to a list of IPs that have that note
	 */
	public Map<String, List<String>> getUserNotesReverseMap()
	{
		return appearanceCounterUI.getUserNotesReverseMap();
	}
}