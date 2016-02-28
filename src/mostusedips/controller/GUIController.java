package mostusedips.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
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
import javafx.scene.input.KeyCode;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import mostusedips.Main;
import mostusedips.controller.watchdog.WatchdogUI;
import mostusedips.model.PropertiesByType;
import mostusedips.model.TextToSpeech;
import mostusedips.model.ipsniffer.DeviceIPAndDescription;
import mostusedips.model.ipsniffer.IPSniffer;
import mostusedips.view.NumberTextField;

public class GUIController implements Initializable
{
	private final static String propsFileLocation = Main.getAppName() + ".properties";
	private final static String defaultPropsResource = "/defaultLastRun.properties";
	private final static String propsNICIndex = "Selected NIC index";
	private final static String propsTraceAddress = "traceAddress";
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
	private NumberTextField textFieldTimeout;
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

	private NumberTextField numFieldCaptureTimeout;
	private NumberTextField numFieldRowsToRead;
	private NumberTextField numberFieldPingTimeout;

	private ToggleGroup tglGrpNIC = new ToggleGroup();
	private IPSniffer sniffer;
	private ArrayList<DeviceIPAndDescription> listOfDevices;
	private HashMap<RadioButton, String> buttonToIpMap = new HashMap<RadioButton, String>();
	private TextToSpeech tts = new TextToSpeech(voiceForTTS);
	private HotkeyRegistry hotkeyRegistry = new HotkeyRegistry(tabPane);

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

		loadLastRunConfig();

		checkForUpdates(true); //only show a message if there is a new version
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
		menuItemMinimize.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				Stage stage = (Stage) tabPane.getScene().getWindow();
				Event.fireEvent(stage, new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
			}
		});

		menuItemExit.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				exitButtonPressed();
			}
		});

		menuItemUpdate.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				checkForUpdates(false);
			}

		});

		menuItemAbout.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				showAboutWindow();
			}
		});

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
			btn.setUserData(index++);
			btn.setToggleGroup(tglGrpNIC);
			btn.setPadding(new Insets(0, 0, 0, 10));
			buttonToIpMap.put(btn, deviceInfo.getIP());

			vboxNICs.getChildren().add(btn);
		}

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
			in = (lastRun.exists() ? new FileInputStream(lastRun) : this.getClass().getResourceAsStream(defaultPropsResource));

			props.load(in);
			in.close();
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "Unable to load properties file: " + e.getMessage(), e);
		}

		int nicIndex = PropertiesByType.getIntProperty(props, propsNICIndex);

		Node node = vboxNICs.getChildren().get(nicIndex);
		if (node instanceof RadioButton)
		{
			RadioButton rb = (RadioButton) node;
			rb.setSelected(true);
		}

		textTrace.setText(props.getProperty(propsTraceAddress));

		appearanceCounterUI.loadLastRunConfig(props);
		pingToSpeechUI.loadLastRunConfig(props);
		watchdogUI.loadLastRunConfig(props);
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
		link.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				Main.openInBrowser(link.getText());
				alert.close();
			}
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

	public NumberTextField getTextFieldTimeout()
	{
		return textFieldTimeout;
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
		return numberFieldPingTimeout;
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

	public Label getLabelReadFirstRows()
	{
		return labelReadFirstRows;
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

	public HashMap<RadioButton, String> getButtonToIpMap()
	{
		return buttonToIpMap;
	}

	public ToggleGroup getTglGrpNIC()
	{
		return tglGrpNIC;
	}

	public HotkeyRegistry getHotkeyRegistry()
	{
		return hotkeyRegistry;
	}
}