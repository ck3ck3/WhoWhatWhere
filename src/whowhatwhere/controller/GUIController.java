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
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
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
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
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
import whowhatwhere.model.ipsniffer.DeviceIPAndDescription;
import whowhatwhere.model.ipsniffer.IPSniffer;
import whowhatwhere.model.startwithwindows.StartWithWindowsRegistryUtils;

public class GUIController implements Initializable
{
	private final static String propsFileLocation = Main.getAppName() + ".properties";
	private final static String defaultPropsResource = "/defaultLastRun.properties";
	private final static String propsNICIndex = "Selected NIC index";
	private final static String propsTraceAddress = "traceAddress";
	private final static String propsShowMessageOnMinimize = "showMinimizeMessage";
	private final static String propsStartMinimized = "startMinimized";
	private final static String propsIgnoreRunPathDiff = "ignorePathDiff";
	private final static String voiceForTTS = "kevin16";

	private final static Logger logger = Logger.getLogger(GUIController.class.getPackage().getName());

	@FXML
	private VBox vboxNICs;
	@FXML
	private AnchorPane paneCaptureOptions;
	@FXML
	private CheckBox chkboxAnyProtocol;
	@FXML
	private CheckBox chkboxUDP;
	@FXML
	private CheckBox chkboxTCP;
	@FXML
	private CheckBox chkboxICMP;
	@FXML
	private CheckBox chkboxHTTP;
	@FXML
	private RadioButton radioManual;
	@FXML
	private RadioButton radioTimedCapture;
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
	private HBox hboxColumnNames;
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
	private TextField textWatchdogMessage;
	@FXML
	private Button btnWatchdogManageList;
	@FXML
	private Button btnWatchdogPreview;
	@FXML
	private Label labelWatchdogEntryCount;
	@FXML
	private TextField textTrace;
	@FXML
	private Button btnTrace;
	@FXML
	private Button btnExportTableToCSV;
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
	private CheckBox chkboxWatchdogUseTTS;
	@FXML
	private CheckBox chkboxWatchdogUseAlert;
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
	private ToggleGroup tglGrpCaptureOptions;
	

	private ToggleGroup tglGrpNIC = new ToggleGroup();
	private IPSniffer sniffer;
	private List<DeviceIPAndDescription> listOfDevices;
	private Map<RadioButton, String> buttonToIpMap = new HashMap<>();
	private TextToSpeech tts = new TextToSpeech(voiceForTTS);
	private HotkeyRegistry hotkeyRegistry = new HotkeyRegistry(tabPane);
	private boolean isExitAlreadyAddedToSystray = false;
	private boolean showMessageOnMinimize;
	private boolean ignoreRunPathDiff;

	private AppearanceCounterUI appearanceCounterUI;
	private PingToSpeechUI pingToSpeechUI;
	private WatchdogUI watchdogUI;

	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		sniffer = new IPSniffer();

		createNICRadioButtons();
		vboxNICs.setSpacing(10);

		appearanceCounterUI = new AppearanceCounterUI(this);
		pingToSpeechUI = new PingToSpeechUI(this);
		watchdogUI = new WatchdogUI(this);

		initButtonHandlers();
		initMenuBar();
		addExitToSystrayIcon();

		loadLastRunConfig();

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

	private void createNICRadioButtons()
	{
		listOfDevices = sniffer.getListOfDevices();

		if (listOfDevices == null)
		{
			Label label = new Label("Unable to find any network interfaces");
			vboxNICs.getChildren().add(label);
			btnStart.setDisable(true);
			logger.log(Level.SEVERE, "Unable to find any network interfaces");
			return;
		}

		int index = 1; //index of radio button in the vbox. starts at 1 because we already added a label earlier

		for (DeviceIPAndDescription deviceInfo : listOfDevices)
		{
			RadioButton btn = new RadioButton(deviceInfo.getDescription() + " " + deviceInfo.getIP());
			btn.setTooltip(new Tooltip(btn.getText())); // so we don't need a horizontal scroller
			btn.setUserData(index++);
			btn.setToggleGroup(tglGrpNIC);
			btn.setPadding(new Insets(0, 0, 0, 10));
			buttonToIpMap.put(btn, deviceInfo.getIP());

			vboxNICs.getChildren().add(btn);
		}

		vboxNICs.setPrefHeight(listOfDevices.size() * 30); //to resize, in case we'll need a vertical scroller

		tglGrpNIC.selectToggle(tglGrpNIC.getToggles().get(0)); //select the first button
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
		Properties props = new Properties();

		appearanceCounterUI.saveCurrentRunValuesToProperties(props);
		pingToSpeechUI.saveCurrentRunValuesToProperties(props);
		watchdogUI.saveCurrentRunValuesToProperties(props);

		Toggle selectedToggle = tglGrpNIC.getSelectedToggle();
		Integer selectedNic = (selectedToggle != null ? (Integer) (selectedToggle.getUserData()) : 1);

		props.put(propsNICIndex, selectedNic.toString());
		props.put(propsTraceAddress, textTrace.getText());
		props.put(propsShowMessageOnMinimize, String.valueOf(showMessageOnMinimize));
		props.put(propsStartMinimized, String.valueOf(menuItemChkStartMinimized.isSelected()));
		props.put(propsIgnoreRunPathDiff, String.valueOf(ignoreRunPathDiff));

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

	private void loadLastRunConfig()
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

		int nicIndex = PropertiesByType.getIntProperty(props, propsNICIndex, 0);

		Node node = vboxNICs.getChildren().get(nicIndex);
		if (node instanceof RadioButton)
		{
			RadioButton rb = (RadioButton) node;
			rb.setSelected(true);
		}

		textTrace.setText(props.getProperty(propsTraceAddress));

		showMessageOnMinimize = PropertiesByType.getBoolProperty(props, propsShowMessageOnMinimize, true);
		menuItemChkDisplayBalloon.setSelected(showMessageOnMinimize);

		boolean startMinimized = PropertiesByType.getBoolProperty(props, propsStartMinimized, false);
		menuItemChkStartMinimized.setSelected(startMinimized);
		if (startMinimized)
			Platform.runLater(() -> menuItemMinimize.fire());

		ignoreRunPathDiff = PropertiesByType.getBoolProperty(props, propsIgnoreRunPathDiff, false);
		loadStartWithWindowsSetting();

		appearanceCounterUI.loadLastRunConfig(props);
		pingToSpeechUI.loadLastRunConfig(props);
		watchdogUI.loadLastRunConfig(props);
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
					String message = Main.getAppName() + " is set to run when Windows starts, but you are currently running it from a different path than the one Windows is set to run it from.\n" + 
															"Current path: " + currentRunLocation + "\nWindows is set to run it from: " + locationToStartFrom + "\n\nPlease choose how to proceed:";
					
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
	 * @param forAllUsers - true if the startup setting is for all users, false if for current user only
	 * @param message - the message to display in the dialog
	 * @return true if the user chose to delete the program from startup, false otherwise.
	 * @throws IOException - if there was a problem modifying the registry
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

		about.show();
	}

	/**
	 * @param silent
	 *            - if true, a message will be shown only if there's a new
	 *            update. if false, a message will be shown regardless.
	 */
	private void checkForUpdates(boolean silent)
	{
		boolean updateAvailable = false;
		Alert alert = null;

		try
		{
			updateAvailable = Main.isUpdateAvailable();

			if (updateAvailable)
				alert = generateLabelAndLinkAlert(AlertType.INFORMATION, "Check for updates", "New version available!", "Download the new version at ", Main.getWebsite());
			else
			{
				alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Check for updates");
				alert.setHeaderText("No new updates available.");
				alert.setContentText("You are running the latest version.");
			}
		}
		catch (Exception e)
		{
			if (!silent)
			{
				alert = new Alert(AlertType.ERROR);
				alert.setHeaderText("Unable to check for updates");
				alert.setHeaderText(e.getMessage());
			}

			logger.log(Level.SEVERE, "Failed to check for updates", e);
		}
		finally
		{
			if (updateAvailable || !silent)
				alert.showAndWait();
		}
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

	public VBox getVboxNICs()
	{
		return vboxNICs;
	}

	public AnchorPane getPaneCaptureOptions()
	{
		return paneCaptureOptions;
	}

	public CheckBox getChkboxAnyProtocol()
	{
		return chkboxAnyProtocol;
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

	public RadioButton getRadioManual()
	{
		return radioManual;
	}

	public RadioButton getRadioTimedCapture()
	{
		return radioTimedCapture;
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

	public HBox getHboxColumnNames()
	{
		return hboxColumnNames;
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

	public TextField getTextWatchdogMessage()
	{
		return textWatchdogMessage;
	}

	public Button getBtnWatchdogManageList()
	{
		return btnWatchdogManageList;
	}

	public Button getBtnWatchdogPreview()
	{
		return btnWatchdogPreview;
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

	public Map<RadioButton, String> getButtonToIpMap()
	{
		return buttonToIpMap;
	}

	public ToggleGroup getTglGrpNIC()
	{
		return tglGrpNIC;
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
		return (Stage) tabPane.getScene().getWindow();
	}
	
	public CheckBox getChkboxWatchdogUseTTS()
	{
		return chkboxWatchdogUseTTS;
	}
	
	public CheckBox getChkboxWatchdogUseAlert()
	{
		return chkboxWatchdogUseAlert;
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
}