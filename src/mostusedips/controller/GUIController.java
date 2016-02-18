package mostusedips.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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
import javafx.scene.control.ButtonBar;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import mostusedips.Main;
import mostusedips.controller.commands.ping.PingCommandScreen;
import mostusedips.controller.commands.trace.TraceCommandScreen;
import mostusedips.model.geoipresolver.GeoIPInfo;
import mostusedips.model.geoipresolver.GeoIPResolver;
import mostusedips.model.hotkey.HotkeyExecuter;
import mostusedips.model.hotkey.HotkeyManager;
import mostusedips.model.ipsniffer.AppearanceCounterResults;
import mostusedips.model.ipsniffer.CaptureStartListener;
import mostusedips.model.ipsniffer.DeviceIPAndDescription;
import mostusedips.model.ipsniffer.FirstSightListener;
import mostusedips.model.ipsniffer.IPToMatch;
import mostusedips.model.ipsniffer.IpAppearancesCounter;
import mostusedips.model.ipsniffer.IpSniffer;
import mostusedips.model.tts.TextToSpeech;
import mostusedips.view.NumberTextField;

public class GUIController implements Initializable, CaptureStartListener, FirstSightListener
{
	private final static String propsFileLocation = Main.getAppName() + ".properties";
	private final static String defaultPropsResource = "/defaultLastRun.properties";
	private final static String voiceForTTS = "kevin16";
	private final static int defaultCaptureTimeout = 10;
	private final static int defaultPingTimeout = 300;
	private final static String statusIdle = "Status: Idle";
	private final static String statusGettingReady = "Status: Getting ready to start capture...";
	private final static String statusCapturing = "Status: Capture in progress...";
	private final static String statusStopping = "Status: Stopping capture...";
	private final static String statusResults = "Status: Fetching results...";
	private final static String secondaryGeoIpPrefix = "https://www.iplocation.net/?query=";
	private final static int defaultRowsToRead = 3;
	private final static String msgTimerExpired = "Timer expired, stopping capture";
	private final static String ptsHistoryFile = "ptsHistory";
	private final static String captureHotkeyID = "Mosed used IPs capture hotkey";
	private final static String ptsHotkeyID = "PTS hotkey";
	private final static String watchdogHotkeyID = "Watchdog hotkey";
	private final static String watchdogListFormLocation = "/mostusedips/view/WatchdogList.fxml";
	private static final String watchdogLastRunFilename = "Last run.watchdogPreset";

	private final static String propsNICIndex = "Selected NIC index";
	private final static String propsChkboxUDP = "chkboxUDP";
	private final static String propsChkboxTCP = "chkboxTCP";
	private final static String propsChkboxICMP = "chkboxICMP";
	private final static String propsChkboxHTTP = "chkboxHTTP";
	private final static String propsRadioTimedCapture = "radioTimedCapture";
	private final static String propsNumFieldCaptureTimeout = "numFieldCaptureTimeout";
	private final static String propsRadioManual = "radioManual";
	private final static String propsChkboxGetLocation = "chkboxGetLocation";
	private final static String propsChkboxPing = "chkboxPing";
	private final static String propsNumberFieldPingTimeout = "numberFieldPingTimeout";
	private final static String propsChkboxUseCaptureHotkey = "chkboxUseCaptureHotkey";
	private final static String propsCaptureHotkeyKeycode = "captureHotkeyKeycode";
	private final static String propsCaptureHotkeyModifiers = "captureHotkeyModifiers";
	private final static String propsChkboxUseTTS = "chkboxUseTTS";
	private final static String propsNumFieldRowsToRead = "numFieldRowsToRead";
	private final static String propsChkboxFilterResults = "chkboxFilterResults";
	private final static String propsComboColumnsSelection = "comboColumnsSelection";
	private final static String propsTextColumnContains = "textColumnContains";
	private final static String propsTTSCheckBox = "TTSCheckBox ";
	private final static String propsChkboxPTSHotkey = "chkboxPTSHotkey";
	private final static String propsPTSHotkey = "PTSHotkey";
	private final static String propsPTSModifiers = "PTSModifiers";
	private final static String propsPTSComboValue = "PTSComboValue";
	private final static String propsChkboxWatchdogHotkey = "chkboxWatchdogHotkey";
	private final static String propsWatchdogHotkeyKeycode = "watchdogHotkeyKeycode";
	private final static String propsWatchdogHotkeyModifiers = "watchdogHotkeyModifiers";

	private final static Logger logger = Logger.getLogger(GUIController.class.getPackage().getName());

	@FXML
	private ScrollPane scrollPane;
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
	private Spinner<Integer> spinnerTimeout;
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

	private NumberTextField numFieldCaptureTimeout;
	private NumberTextField numFieldRowsToRead;
	private NumberTextField numberFieldPingTimeout;
	private Alert alertChangeHotkey;

	private ToggleGroup tglGrpNIC = new ToggleGroup();
	private ToggleGroup tglGrpCaptureOptions = new ToggleGroup();
	private IpSniffer sniffer;
	private ArrayList<DeviceIPAndDescription> listOfDevices;
	private HashMap<RadioButton, String> buttonToIpMap = new HashMap<RadioButton, String>();
	private TextToSpeech tts = new TextToSpeech(voiceForTTS);
	private HotkeyManager hotkeyManager = new HotkeyManager();
	private Timer timer;
	private TimerTask timerTask;
	private boolean isTimedTaskRunning = false;
	private Button activeButton;
	private boolean isAHotkeyResult = false;
	private boolean isMuted = false;
	private ArrayList<CheckBox> chkboxListColumns;
	private int protocolBoxesChecked = 0;
	private int captureHotkeyKeyCode;
	private int captureHotkeyModifiers;
	private int ptsHotkeyKeyCode;
	private int ptsHotkeyModifiers;
	private int watchdogHotkeyKeyCode;
	private int watchdogHotkeyModifiers;
	private ObservableList<IPToMatch> watchdogList = FXCollections.observableArrayList();

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
	private TextArea textAreaOutput;
	@FXML
	private ButtonBar buttonBar;
	@FXML
	private Button btnCloseCmd;
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
	private Button watchdogActiveButton;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		sniffer = new IpSniffer(Main.DLLx86Location, Main.DLLx64Location);
		if (!sniffer.isDllLoaded())
		{
			System.err.println("Unable to load jnetpcap native dll. See log file for details. Unable to continue, aborting.");
			shutdownApp();
		}

		activeButton = btnStart;

		createNICRadioButtons();

		vboxNICs.setSpacing(10);

		createNumTextFields();

		radioManual.setToggleGroup(tglGrpCaptureOptions);
		radioTimedCapture.setToggleGroup(tglGrpCaptureOptions);

		btnStop.setDisable(true);

		initTable();
		initColumnListForTTS();

		initHotkeyChangeAlert();
		loadLastRunConfig();
		initButtonHandlers();
		initMenuBar();

		checkForUpdates(true); //only show a message if there is a new version
	}

	private void initMenuBar()
	{
		menuItemMinimize.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				Stage stage = (Stage) scrollPane.getScene().getWindow();
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

	private void initHotkeyChangeAlert()
	{
		alertChangeHotkey = new Alert(AlertType.NONE);

		alertChangeHotkey.setTitle("Change hotkey");
		alertChangeHotkey.setHeaderText("Choose a new hotkey");
		alertChangeHotkey.setContentText("Press the new hotkey");
	}

	private void closeHotkeyChangeAlert()
	{
		alertChangeHotkey.setAlertType(AlertType.INFORMATION); //javafx bug? can't close it when it's of type NONE
		alertChangeHotkey.close();
		alertChangeHotkey.setAlertType(AlertType.NONE); //due to the javafx bug, revert back to NONE
	}

	private void initColumnListForTTS()
	{
		chkboxListColumns = new ArrayList<CheckBox>();

		for (TableColumn<IPInfoRowModel, ?> tableColumn : tableResults.getColumns())
		{
			String colName = tableColumn.getText();

			comboColumns.getItems().add(colName);

			CheckBox box = new CheckBox(colName);

			hboxColumnNames.getChildren().add(box);
			chkboxListColumns.add(box);
		}

		comboColumns.setValue(comboColumns.getItems().get(0)); //select the first column
	}

	private void createNumTextFields()
	{
		numFieldCaptureTimeout = new NumberTextField(String.valueOf(defaultCaptureTimeout), 1);

		numFieldCaptureTimeout.setPrefSize(45, 25);
		numFieldCaptureTimeout.setLayoutX(208);
		numFieldCaptureTimeout.setLayoutY(radioTimedCapture.getLayoutY() - 2);

		numFieldCaptureTimeout.focusedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
			{
				if (newPropertyValue)
				{
					radioTimedCapture.setSelected(true);
				}
			}
		});

		numberFieldPingTimeout = new NumberTextField(String.valueOf(defaultPingTimeout), 1, 3000);
		numberFieldPingTimeout.setPrefSize(45, 25);
		numberFieldPingTimeout.setLayoutX(217);
		numberFieldPingTimeout.setLayoutY(chkboxPing.getLayoutY() - 2);

		paneCaptureOptions.getChildren().addAll(numFieldCaptureTimeout, numberFieldPingTimeout);

		numFieldRowsToRead = new NumberTextField(String.valueOf(defaultRowsToRead), 1);

		numFieldRowsToRead.setPrefSize(35, 25);
		numFieldRowsToRead.setLayoutX(77);
		numFieldRowsToRead.setLayoutY(labelReadFirstRows.getLayoutY() - 2);

		paneUseTTS.getChildren().add(numFieldRowsToRead);
	}

	private void initButtonHandlers()
	{
		btnStart.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				startButtonPressed();
			}
		});

		btnStop.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				stopButtonPressed();
			}
		});

		btnConfigCaptureHotkey.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				hotkeyManager.setKeySelection(captureHotkeyID, true);
				tabPane.setDisable(true);
				alertChangeHotkey.show();
			}
		});

		btnExit.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				exitButtonPressed();
			}
		});

		chkboxUseCaptureHotkey.selectedProperty().addListener(generateChangeListenerForHotkeyCheckbox(captureHotkeyID, captureHotkeyModifiers, captureHotkeyKeyCode, chkboxUseCaptureHotkey,
				labelCurrCaptureHotkey, paneEnableCaptureHotkey, captureHotkeyPressed));

		chkboxPTSHotkey.selectedProperty()
				.addListener(generateChangeListenerForHotkeyCheckbox(ptsHotkeyID, ptsHotkeyModifiers, ptsHotkeyKeyCode, chkboxPTSHotkey, labelPTSCurrentHotkey, panePTSHotkey, ptsHotkeyPressed));

		chkboxWatchdogHotkey.selectedProperty().addListener(generateChangeListenerForHotkeyCheckbox(watchdogHotkeyID, watchdogHotkeyModifiers, watchdogHotkeyKeyCode, chkboxWatchdogHotkey,
				labelWatchdogCurrHotkey, paneWatchdogHotkeyConfig, watchdogHotkeyPressed));

		chkboxUseTTS.selectedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val)
			{
				isMuted = !new_val;
				paneUseTTS.setDisable(!new_val);
			}
		});

		chkboxFilterResults.selectedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val)
			{
				paneFilterResults.setDisable(!new_val);
			}
		});

		chkboxPing.selectedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val)
			{
				numberFieldPingTimeout.setDisable(!new_val);
			}
		});

		radioManual.setOnAction(new EventHandler<ActionEvent>()
		{

			@Override
			public void handle(ActionEvent event)
			{
				numFieldCaptureTimeout.setDisable(true);

			}
		});

		radioTimedCapture.setOnAction(new EventHandler<ActionEvent>()
		{

			@Override
			public void handle(ActionEvent event)
			{
				numFieldCaptureTimeout.setDisable(false);

			}
		});

		ChangeListener<Boolean> protocolBoxes = new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				if (newValue)
				{
					chkboxAnyProtocol.setSelected(false);
					protocolBoxesChecked++;
				}
				else
					if (--protocolBoxesChecked == 0) //we are unchecking the last checkbox
						chkboxAnyProtocol.setSelected(true);
			}
		};

		chkboxUDP.selectedProperty().addListener(protocolBoxes);
		chkboxTCP.selectedProperty().addListener(protocolBoxes);
		chkboxICMP.selectedProperty().addListener(protocolBoxes);
		chkboxHTTP.selectedProperty().addListener(protocolBoxes);

		btnPTSConfigureHotkey.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				hotkeyManager.setKeySelection(ptsHotkeyID, true);
				tabPane.setDisable(true);
				alertChangeHotkey.show();
			}
		});

		btnWatchdogConfigureHotkey.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				hotkeyManager.setKeySelection(watchdogHotkeyID, true);
				tabPane.setDisable(true);
				alertChangeHotkey.show();
			}
		});

		btnWatchdogManageList.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				WatchdogManageListScreen watchdogManageListScreen;
				Stage stage = (Stage) scrollPane.getScene().getWindow();

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
				speakIfNotMuted(textWatchdogMessage.getText());
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

				String deviceIP = buttonToIpMap.get(tglGrpNIC.getSelectedToggle());
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

	private ChangeListener<Boolean> generateChangeListenerForHotkeyCheckbox(String hotkeyID, int defaultModifiers, int defaultKeycode, CheckBox chkbox, Label hotkeyLabel, Pane hotkeyPane,
			Runnable runnableKeyPressed)
	{
		return new ChangeListener<Boolean>()
		{
			boolean ignoreUncheck = false; //when we want to programmatically disable the box, without removing the key

			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val)
			{
				hotkeyPane.setDisable(!new_val);

				if (new_val)
				{
					try
					{
						int modifiers, keycode;

						try
						{
							modifiers = hotkeyManager.getHotkeyModifiers(hotkeyID);
							keycode = hotkeyManager.getHotkeyKeycode(hotkeyID);
						}
						catch (IllegalArgumentException iae) //first time we use this key
						{
							addHotkey(hotkeyID, defaultModifiers, defaultKeycode, hotkeyLabel, runnableKeyPressed);
							return;
						}

						addHotkey(hotkeyID, modifiers, keycode, hotkeyLabel, runnableKeyPressed);
					}
					catch (IllegalArgumentException iae)
					{
						new Alert(AlertType.ERROR, "This key combination is already in use. Change the hotkey that uses this combination, then try enabling this hotkey again.").showAndWait();
						hotkeyPane.setDisable(true);

						ignoreUncheck = true; //setSelected(false) will trigger this change listener, we manually ignore it just once
						chkbox.setSelected(false);
					}
				}
				else
				{
					if (!ignoreUncheck)
						hotkeyManager.removeHotkey(hotkeyID);

					ignoreUncheck = false;
				}
			}
		};
	}

	private void addHotkey(String hotkeyID, int modifiers, int keycode, Label hotkeyLabel, Runnable runnableKeyPressed)
	{
		HotkeyExecuter executer = generateHotkeyExecuter(hotkeyID, hotkeyLabel, runnableKeyPressed);

		hotkeyManager.addHotkey(hotkeyID, executer, modifiers, keycode);
		hotkeyLabel.setText("Current hotkey: " + HotkeyManager.hotkeyToString(modifiers, keycode));
	}

	private HotkeyExecuter generateHotkeyExecuter(String hotkeyID, Label hotkeyLabel, Runnable keyPressedRunnable)
	{
		return new HotkeyExecuter()
		{
			public void keyPressed(int modifiers, int keyCode, boolean isNewKey)
			{
				if (!isNewKey) //hotkey pressed
					Platform.runLater(keyPressedRunnable);
				else //new hotkey selection
				{
					try
					{
						hotkeyManager.modifyHotkey(hotkeyID, modifiers, keyCode);
					}
					catch (IllegalArgumentException iae) //failed to change hotkey
					{
						Platform.runLater(new Runnable()
						{
							@Override
							public void run()
							{
								Alert error = new Alert(AlertType.ERROR);
								error.setTitle("Unable to change hotkey");
								error.setHeaderText("Failed to set a new hotkey");
								error.setContentText(iae.getMessage());

								closeHotkeyChangeAlert();
								error.showAndWait();
								tabPane.setDisable(false);
							}
						});

						return;
					}

					Platform.runLater(new Runnable()
					{
						@Override
						public void run()
						{
							hotkeyLabel.setText("Current hotkey: " + HotkeyManager.hotkeyToString(modifiers, keyCode));
							closeHotkeyChangeAlert();
							tabPane.setDisable(false);
						}
					});
				}
			}
		};
	}

	private void initTable()
	{
		tableResults.setPlaceholder(new Label()); //remove default string on empty table

		columnPacketCount.setCellValueFactory(new PropertyValueFactory<IPInfoRowModel, Integer>("packetCount"));
		columnIP.setCellValueFactory(new PropertyValueFactory<IPInfoRowModel, String>("ipAddress"));
		columnOwner.setCellValueFactory(new PropertyValueFactory<IPInfoRowModel, String>("owner"));
		columnPing.setCellValueFactory(new PropertyValueFactory<IPInfoRowModel, String>("ping"));
		columnCountry.setCellValueFactory(new PropertyValueFactory<IPInfoRowModel, String>("country"));
		columnRegion.setCellValueFactory(new PropertyValueFactory<IPInfoRowModel, String>("region"));
		columnCity.setCellValueFactory(new PropertyValueFactory<IPInfoRowModel, String>("city"));

		generatePopupMenus();
	}

	private void generatePopupMenus()
	{
		tableResults.setRowFactory(new Callback<TableView<IPInfoRowModel>, TableRow<IPInfoRowModel>>()
		{
			@Override
			public TableRow<IPInfoRowModel> call(TableView<IPInfoRowModel> tableView)
			{

				final TableRow<IPInfoRowModel> row = new TableRow<>();

				MenuItem getGeoIPinfo = new MenuItem("See more GeoIP results for this IP in browser");
				getGeoIPinfo.setOnAction(new EventHandler<ActionEvent>()
				{

					@Override
					public void handle(ActionEvent event)
					{
						openInBrowser(getSecondaryGeoIpPrefix() + row.getItem().ipAddressProperty());
					}
				});

				MenuItem copyIPtoClipboard = new MenuItem("Copy IP to clipboard");
				copyIPtoClipboard.setOnAction(new EventHandler<ActionEvent>()
				{
					@Override
					public void handle(ActionEvent event)
					{
						final Clipboard clipboard = Clipboard.getSystemClipboard();
						final ClipboardContent content = new ClipboardContent();
						content.putString(row.getItem().ipAddressProperty().getValue());
						clipboard.setContent(content);
					}
				});

				MenuItem sendIPToPTS = new MenuItem("Send IP to Ping-to-Speech tab");
				sendIPToPTS.setOnAction(new EventHandler<ActionEvent>()
				{
					@Override
					public void handle(ActionEvent event)
					{
						comboPTSipToPing.getEditor().setText(row.getItem().ipAddressProperty().getValue());
						tabPane.getSelectionModel().select(tabUtils);
					}
				});

				MenuItem pingIP = new MenuItem("Ping this IP");
				pingIP.setOnAction(new EventHandler<ActionEvent>()
				{
					@Override
					public void handle(ActionEvent event)
					{
						pingCommand(row.getItem().ipAddressProperty().getValue());
					}
				});

				MenuItem traceIP = new MenuItem("Traceroute this IP");
				traceIP.setOnAction(new EventHandler<ActionEvent>()
				{
					@Override
					public void handle(ActionEvent event)
					{
						traceCommand(row.getItem().ipAddressProperty().getValue());
					}
				});

				final ContextMenu rowMenu = new ContextMenu(getGeoIPinfo, copyIPtoClipboard, sendIPToPTS, pingIP, traceIP);

				// only display context menu for non-null items:
				row.contextMenuProperty().bind(Bindings.when(Bindings.isNotNull(row.itemProperty())).then(rowMenu).otherwise((ContextMenu) null));

				return row;
			}
		});
	}

	private void pingCommand(String ip)
	{
		Stage stage = (Stage) scrollPane.getScene().getWindow();

		PingCommandScreen cmdScreen;

		try
		{
			cmdScreen = new PingCommandScreen(stage, stage.getScene(), ip);
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "Unable able to load Ping (command) screen", e);
			return;
		}

		cmdScreen.showScreenOnCurrentStage(cmdScreen.getCloseButton());
		cmdScreen.runCommand();
	}

	private void traceCommand(String ip)
	{
		Stage stage = (Stage) scrollPane.getScene().getWindow();
		TraceCommandScreen cmdScreen;

		try
		{
			cmdScreen = new TraceCommandScreen(stage, stage.getScene(), ip);
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "Unable to load Trace (command) screen", e);
			return;
		}

		cmdScreen.showScreenOnCurrentStage(cmdScreen.getCloseButton());
	}

	private void startButtonPressed()
	{
		StringBuilder errbuf = new StringBuilder();
		String deviceIP = buttonToIpMap.get(tglGrpNIC.getSelectedToggle());
		final CaptureStartListener thisObj = this;

		if (sniffer.isCaptureInProgress())
		{
			new Alert(AlertType.ERROR, "There's already a capture in progress. Only one capture at a time is allowed.").showAndWait();
			return;
		}

		changeGuiTemplate(true);

		Task<Void> workerThreadTask = new Task<Void>()
		{
			private AppearanceCounterResults results;

			@Override
			protected Void call() throws Exception
			{
				results = sniffer.startAppearanceCounterCapture(deviceIP, getSelectedProtocols(), thisObj, errbuf);
				return null;
			}

			@Override
			protected void succeeded() //capture finished
			{
				labelStatus.setText(statusResults);

				fillTable(results.getAppearanceCounterResults());

				if (isAHotkeyResult)
				{
					readResults();
					isAHotkeyResult = false;
				}

				labelStatus.setText(statusIdle);
				changeGuiTemplate(false);
			}

			private ArrayList<Integer> getSelectedProtocols()
			{
				ArrayList<Integer> protocols = new ArrayList<Integer>();

				if (chkboxUDP.isSelected())
					protocols.add(IpSniffer.UDP_PROTOCOL);

				if (chkboxTCP.isSelected())
					protocols.add(IpSniffer.TCP_PROTOCOL);

				if (chkboxICMP.isSelected())
					protocols.add(IpSniffer.ICMP_PROTOCOL);

				if (chkboxHTTP.isSelected())
					protocols.add(IpSniffer.HTTP_PROTOCOL);

				return protocols;
			}
		};

		if (radioTimedCapture.isSelected())
		{
			timerTask = initTimer();
			timer = new Timer(true);
		}

		if (radioTimedCapture.isSelected())
			isTimedTaskRunning = true;

		new Thread(workerThreadTask).start();

	}

	private void readResults()
	{
		List<IPInfoRowModel> items = tableResults.getItems();

		if (isNoColumnChecked() || items == null)
			return;

		if (chkboxFilterResults.isSelected())
			items = filterItemsByColValue(items, comboColumns.getValue(), textColumnContains.getText());

		int linesToReadInput = numFieldRowsToRead.getValue();
		int totalLines = items.size();
		int rowsToRead = Math.min(linesToReadInput, totalLines);

		StringBuilder[] lines = new StringBuilder[rowsToRead];

		for (int i = 0; i < rowsToRead; i++)
			lines[i] = new StringBuilder();

		HashMap<Integer, HashMap<String, String>> rowIDToColMapping = getRowIDToColMapping();

		for (int i = 0; i < rowsToRead; i++)
			lines[i].append("Result number " + (i + 1) + ": ");

		for (Node node : hboxColumnNames.getChildren())
		{
			CheckBox chkbox = (CheckBox) node;

			if (chkbox.isSelected())
			{
				String colName = chkbox.getText();

				for (int i = 0; i < rowsToRead; i++)
				{
					Integer rowID = items.get(i).getRowID();

					HashMap<String, String> columnMapping = rowIDToColMapping.get(rowID);

					String colValue = columnMapping.get(colName);

					if (colValue.isEmpty())
						continue;

					lines[i].append(colName + ": " + colValue + ". ");
				}
			}
		}

		String result = "";

		for (int i = 0; i < rowsToRead; i++)
			result += lines[i].toString();

		speakIfNotMuted(result);
	}

	/**
	 * 
	 * @param items
	 *            - a list of all the items
	 * @param colName
	 *            - the column that we want to filter by
	 * @param colValue
	 *            - the value that we want to filter by. Every column that
	 *            contains that string will be added to the returned list
	 * @return a filtered list, where all the values in the selected column
	 *         contain the selected value
	 */
	private ArrayList<IPInfoRowModel> filterItemsByColValue(List<IPInfoRowModel> items, String colName, String colValue)
	{
		ArrayList<IPInfoRowModel> filteredList = new ArrayList<IPInfoRowModel>();
		HashMap<Integer, HashMap<String, String>> rowIDToColMapping = getRowIDToColMapping();

		for (IPInfoRowModel row : items)
		{
			HashMap<String, String> columnMapping = rowIDToColMapping.get(row.getRowID());
			String value = columnMapping.get(colName);

			if (value.toLowerCase().contains(colValue.toLowerCase()))
				filteredList.add(row);
		}

		return filteredList;
	}

	/**
	 * @return a map that maps each rowID to a map of colName:value pairs in the
	 *         relevant row
	 */
	private HashMap<Integer, HashMap<String, String>> getRowIDToColMapping()
	{
		HashMap<Integer, HashMap<String, String>> rowIDToCOlMapping = new HashMap<Integer, HashMap<String, String>>();

		ObservableList<IPInfoRowModel> items = tableResults.getItems();

		for (IPInfoRowModel ipInfoRowModel : items)
		{
			HashMap<String, String> colMapping = new HashMap<String, String>();

			colMapping.put("Packet Count", ipInfoRowModel.packetCountProperty().getValue().toString());
			colMapping.put("IP Address", ipInfoRowModel.ipAddressProperty().getValue());
			colMapping.put("Owner", ipInfoRowModel.ownerProperty().getValue());
			colMapping.put("Ping", ipInfoRowModel.pingProperty().getValue());
			colMapping.put("Country", ipInfoRowModel.countryProperty().getValue());
			colMapping.put("Region", ipInfoRowModel.regionProperty().getValue());
			colMapping.put("City", ipInfoRowModel.cityProperty().getValue());

			rowIDToCOlMapping.put(ipInfoRowModel.getRowID(), colMapping);
		}

		return rowIDToCOlMapping;
	}

	private boolean isNoColumnChecked()
	{
		for (Node node : hboxColumnNames.getChildren())
		{
			CheckBox box = (CheckBox) node;

			if (box.isSelected())
				return false;
		}

		return true;
	}

	/**
	 * @param duringCapture
	 *            - when true, the GUI will not allow to start a new capture.
	 *            when false, it will allow.
	 */
	private void changeGuiTemplate(boolean duringCapture)
	{
		activeButton = (duringCapture ? btnStop : btnStart);

		btnStart.setDisable(duringCapture);
		btnStop.setDisable(!duringCapture);
		paneCaptureOptions.setDisable(duringCapture);
		vboxNICs.setDisable(duringCapture);
		paneCaptureOptions.setDisable(duringCapture);

		if (duringCapture)
		{
			tableResults.setItems(null);
			tableResults.setPlaceholder(new Label(""));
			labelStatus.setText(statusGettingReady);
		}
	}

	private void stopButtonPressed()
	{
		Task<Void> workerThreadTask = new Task<Void>()
		{
			@Override
			protected Void call() throws Exception
			{
				if (isTimedTaskRunning)
				{
					timerTask.cancel();
					isTimedTaskRunning = false;
				}

				sniffer.stopCapture();

				Platform.runLater(new Runnable()
				{
					@Override
					public void run()
					{
						btnStop.setDisable(true);
						labelStatus.setText(statusStopping);
					}
				});

				return null;
			}
		};

		new Thread(workerThreadTask).start();
	}

	private TimerTask initTimer()
	{
		TimerTask timerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				Platform.runLater(new Runnable()
				{

					@Override
					public void run()
					{
						timerExpired();
					}
				});
			}
		};

		return timerTask;
	}

	private void timerExpired()
	{
		isTimedTaskRunning = false;
		sniffer.stopCapture();

		if (isAHotkeyResult)
			speakIfNotMuted(msgTimerExpired);
	}

	private void fillTable(ArrayList<IpAppearancesCounter> ips)
	{
		ObservableList<IPInfoRowModel> data = FXCollections.observableArrayList();
		IPInfoRowModel row;
		int i = 1;

		for (IpAppearancesCounter ipCounter : ips)
		{

			Integer id = i++;
			Integer amountOfAppearances = ipCounter.getAmountOfAppearances();
			String ip = ipCounter.getIp();
			String owner = "";
			String ping = "";
			String country = "";
			String region = "";
			String city = "";

			if (chkboxGetLocation.isSelected())
			{
				GeoIPInfo ipInfo = GeoIPResolver.getIPInfo(ip);

				if (ipInfo.getSuccess())
				{
					owner = ipInfo.getIsp();
					country = ipInfo.getCountry();
					region = ipInfo.getRegionName();
					city = ipInfo.getCity();
				}
				else
					owner = ipInfo.getMessage();
			}

			if (chkboxPing.isSelected())
				ping = getPingForIP(ip, numberFieldPingTimeout.getValue().intValue());

			row = new IPInfoRowModel(id, amountOfAppearances, ip, owner, ping, country, region, city);
			data.add(row);
		}

		if (data.isEmpty())
			tableResults.setPlaceholder(new Label("No packets to show"));
		else
			tableResults.setItems(data);
	}

	private String getPingForIP(String ip, Integer timeout)
	{
		String ping;
		PingCommandScreen pingCmd;

		try
		{
			pingCmd = new PingCommandScreen(null, null, ip, "-n 1" + (timeout != null ? " -w " + timeout : ""));
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "Unable able to generate ping (failed to load Ping (command) screen)", e);
			return null;
		}

		pingCmd.runCommand();

		try
		{
			while (!pingCmd.isOutputReady())
				Thread.sleep(10); //allow the command to run and finish without creating a deadlock
		}
		catch (InterruptedException e) //required, nothing to do here
		{
		}

		String output = pingCmd.getOutput();
		String pingResult = output.substring(output.lastIndexOf(' '));

		if (pingResult.contains("loss"))
			ping = "Timeout";
		else
			if (pingResult.contains("ms\n"))
				ping = pingResult.replace("ms\n", " milliseconds");
			else
				ping = "Error";

		return ping;
	}

	private void createNICRadioButtons()
	{
		StringBuilder errbuf = new StringBuilder();

		listOfDevices = sniffer.getListOfDevices(errbuf);

		if (listOfDevices == null)
		{
			Label label = new Label("Unable to find any network interfaces");
			vboxNICs.getChildren().add(label);
			btnStart.setDisable(true);
			logger.log(Level.SEVERE, "Unable to find any network interfaces. More info: " + errbuf);
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
			saveCurrentValuesToProperties();

			hotkeyManager.cleanup();
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

	private void shutdownApp()
	{
		Platform.setImplicitExit(true); //was initially set to false when initializing the systray
		Platform.exit();
		System.exit(0); //needed because of the AWT SysTray		
	}

	private void saveCurrentValuesToProperties()
	{
		Properties props = new Properties();

		Toggle selectedToggle = tglGrpNIC.getSelectedToggle();
		Integer selectedNic = (selectedToggle != null ? (Integer) (selectedToggle.getUserData()) : 1);

		props.put(propsNICIndex, selectedNic.toString());
		props.put(propsChkboxUDP, ((Boolean) chkboxUDP.isSelected()).toString());
		props.put(propsChkboxTCP, ((Boolean) chkboxTCP.isSelected()).toString());
		props.put(propsChkboxICMP, ((Boolean) chkboxICMP.isSelected()).toString());
		props.put(propsChkboxHTTP, ((Boolean) chkboxHTTP.isSelected()).toString());
		props.put(propsRadioTimedCapture, ((Boolean) radioTimedCapture.isSelected()).toString());
		props.put(propsNumFieldCaptureTimeout, numFieldCaptureTimeout.getText());
		props.put(propsRadioManual, ((Boolean) radioManual.isSelected()).toString());
		props.put(propsChkboxGetLocation, ((Boolean) chkboxGetLocation.isSelected()).toString());
		props.put(propsChkboxPing, ((Boolean) chkboxPing.isSelected()).toString());
		props.put(propsNumberFieldPingTimeout, numberFieldPingTimeout.getText());
		props.put(propsChkboxUseCaptureHotkey, ((Boolean) chkboxUseCaptureHotkey.isSelected()).toString());
		props.put(propsCaptureHotkeyKeycode, Integer.toString(hotkeyManager.getHotkeyKeycode(captureHotkeyID)));
		props.put(propsCaptureHotkeyModifiers, Integer.toString(hotkeyManager.getHotkeyModifiers(captureHotkeyID)));
		props.put(propsChkboxUseTTS, ((Boolean) chkboxUseTTS.isSelected()).toString());
		props.put(propsNumFieldRowsToRead, numFieldRowsToRead.getText());
		props.put(propsChkboxFilterResults, ((Boolean) chkboxFilterResults.isSelected()).toString());
		props.put(propsComboColumnsSelection, comboColumns.getValue());
		props.put(propsTextColumnContains, textColumnContains.getText());

		for (CheckBox box : chkboxListColumns)
			props.put(propsTTSCheckBox + box.getText(), ((Boolean) box.isSelected()).toString());

		props.put(propsChkboxPTSHotkey, ((Boolean) chkboxPTSHotkey.isSelected()).toString());
		props.put(propsPTSHotkey, Integer.toString(hotkeyManager.getHotkeyKeycode(ptsHotkeyID)));
		props.put(propsPTSModifiers, Integer.toString(hotkeyManager.getHotkeyModifiers(ptsHotkeyID)));
		props.put(propsPTSComboValue, comboPTSipToPing.getEditor().getText());

		props.put(propsChkboxWatchdogHotkey, ((Boolean) chkboxWatchdogHotkey.isSelected()).toString());
		props.put(propsWatchdogHotkeyKeycode, Integer.toString(hotkeyManager.getHotkeyKeycode(watchdogHotkeyID)));
		props.put(propsWatchdogHotkeyModifiers, Integer.toString(hotkeyManager.getHotkeyModifiers(watchdogHotkeyID)));

		StringBuilder ptsHistoryBuilder = new StringBuilder();
		for (String item : comboPTSipToPing.getItems())
			ptsHistoryBuilder.append(item + "\n");

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

		try
		{
			FileUtils.writeStringToFile(new File(ptsHistoryFile), ptsHistoryBuilder.toString(), "UTF-8");
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "Unable to save Ping-to-Speech history: " + e.getMessage(), e);
		}

		try
		{
			watchdogSaveListToFile(new ArrayList<IPToMatch>(watchdogList), textWatchdogMessage.getText(), watchdogLastRunFilename);
		}
		catch (IOException ioe)
		{
			logger.log(Level.SEVERE, "Unable to save Watchdog list: " + ioe.getMessage(), ioe);
		}
	}

	private void loadLastRunConfig()
	{
		InputStream in;

		File lastRun = new File(propsFileLocation);
		Properties props = new Properties();

		try
		{
			if (lastRun.exists())
				in = new FileInputStream(lastRun);
			else
				in = this.getClass().getResourceAsStream(defaultPropsResource);

			props.load(in);
			in.close();
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "Unable to load properties file: " + e.getMessage(), e);
		}

		try
		{
			List<String> lines = FileUtils.readLines(new File(ptsHistoryFile), "UTF-8");
			comboPTSipToPing.getItems().addAll(lines);
		}
		catch (IOException e) //ignore, maybe it's first run
		{
		}

		comboPTSipToPing.getEditor().setText((String) props.get(propsPTSComboValue));
		int nicIndex = getIntProperty(props, propsNICIndex);

		Node node = vboxNICs.getChildren().get(nicIndex);
		if (node instanceof RadioButton)
		{
			RadioButton rb = (RadioButton) node;
			rb.setSelected(true);
		}

		setProtocolCheckboxes(props);
		setCaptureOptionsPane(props);
		setCaptureHotkeyAndPane(props);
		setDisabledPanes();

		setPTSHotkey(props);
		setWatchdogHotkey(props);
		try
		{
			watchdogLoadListFromFile(watchdogList, textWatchdogMessage, labelWatchdogEntryCount, watchdogLastRunFilename);
		}
		catch (IOException | ClassNotFoundException ioe) //ignore, don't load
		{
		}
	}

	private void setWatchdogHotkey(Properties props)
	{
		chkboxWatchdogHotkey.setSelected(getBoolProperty(props, propsChkboxWatchdogHotkey));
		watchdogHotkeyModifiers = getIntProperty(props, propsWatchdogHotkeyModifiers);
		watchdogHotkeyKeyCode = getIntProperty(props, propsWatchdogHotkeyKeycode);

		if (chkboxWatchdogHotkey.isSelected())
			addHotkey(watchdogHotkeyID, watchdogHotkeyModifiers, watchdogHotkeyKeyCode, labelWatchdogCurrHotkey, watchdogHotkeyPressed);
		else
			paneWatchdogHotkeyConfig.setDisable(true);
	}

	private void setPTSHotkey(Properties props)
	{
		chkboxPTSHotkey.setSelected(getBoolProperty(props, propsChkboxPTSHotkey));
		ptsHotkeyModifiers = getIntProperty(props, propsPTSModifiers);
		ptsHotkeyKeyCode = getIntProperty(props, propsPTSHotkey);

		if (chkboxPTSHotkey.isSelected())
			addHotkey(ptsHotkeyID, ptsHotkeyModifiers, ptsHotkeyKeyCode, labelPTSCurrentHotkey, ptsHotkeyPressed);
		else
			panePTSHotkey.setDisable(true);
	}

	private void setCaptureHotkeyAndPane(Properties props)
	{
		captureHotkeyModifiers = getIntProperty(props, propsCaptureHotkeyModifiers);
		captureHotkeyKeyCode = getIntProperty(props, propsCaptureHotkeyKeycode);
		chkboxUseCaptureHotkey.setSelected(getBoolProperty(props, propsChkboxUseCaptureHotkey));

		if (chkboxUseCaptureHotkey.isSelected())
			addHotkey(captureHotkeyID, captureHotkeyModifiers, captureHotkeyKeyCode, labelCurrCaptureHotkey, captureHotkeyPressed);

		chkboxUseTTS.setSelected(getBoolProperty(props, propsChkboxUseTTS));
		numFieldRowsToRead.setText(props.getProperty(propsNumFieldRowsToRead));

		chkboxFilterResults.setSelected(getBoolProperty(props, propsChkboxFilterResults));
		textColumnContains.setText(props.getProperty(propsTextColumnContains));

		String comboValue = props.getProperty(propsComboColumnsSelection);
		if (!comboValue.isEmpty())
			comboColumns.setValue(comboValue);

		for (CheckBox box : chkboxListColumns)
			box.setSelected(getBoolProperty(props, propsTTSCheckBox + box.getText()));
	}

	private void setCaptureOptionsPane(Properties props)
	{
		radioTimedCapture.setSelected(getBoolProperty(props, propsRadioTimedCapture));
		numFieldCaptureTimeout.setText(props.getProperty(propsNumFieldCaptureTimeout));
		radioManual.setSelected(getBoolProperty(props, propsRadioManual));
		chkboxGetLocation.setSelected(getBoolProperty(props, propsChkboxGetLocation));
		chkboxPing.setSelected(getBoolProperty(props, propsChkboxPing));
		numberFieldPingTimeout.setText(props.getProperty(propsNumberFieldPingTimeout));
	}

	private void setProtocolCheckboxes(Properties props)
	{
		boolean isChecked;
		protocolBoxesChecked = 0;

		isChecked = getBoolProperty(props, propsChkboxUDP);
		if (isChecked)
			protocolBoxesChecked++;

		chkboxUDP.setSelected(isChecked);

		isChecked = getBoolProperty(props, propsChkboxTCP);
		if (isChecked)
			protocolBoxesChecked++;

		chkboxTCP.setSelected(isChecked);

		isChecked = getBoolProperty(props, propsChkboxICMP);
		if (isChecked)
			protocolBoxesChecked++;

		chkboxICMP.setSelected(isChecked);

		isChecked = getBoolProperty(props, propsChkboxHTTP);
		if (isChecked)
			protocolBoxesChecked++;

		chkboxHTTP.setSelected(isChecked);

		if (protocolBoxesChecked == 0)
			chkboxAnyProtocol.setSelected(true);
	}

	private void setDisabledPanes()
	{
		if (!radioTimedCapture.isSelected())
			numFieldCaptureTimeout.setDisable(true);

		if (!chkboxPing.isSelected())
			numberFieldPingTimeout.setDisable(true);

		if (!chkboxUseCaptureHotkey.isSelected())
			paneEnableCaptureHotkey.setDisable(true);

		if (!chkboxFilterResults.isSelected())
			paneFilterResults.setDisable(true);

		if (!chkboxUseTTS.isSelected())
		{
			paneUseTTS.setDisable(true);
			isMuted = true;
		}
	}

	private boolean getBoolProperty(Properties props, String key)
	{
		String value = props.getProperty(key);

		if (value == null)
			throw new IllegalArgumentException("The key \"" + key + "\" doesn't exist");

		return value.equals("true");

	}

	private Integer getIntProperty(Properties props, String key)
	{
		String value = props.getProperty(key);

		if (value == null)
			throw new IllegalArgumentException("The key \"" + key + "\" doesn't exist");

		return Integer.valueOf(value);
	}

	@Override
	public void captureStartedNotification()
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				String timerExpires = "";

				if (isTimedTaskRunning)
				{
					timer.schedule(timerTask, numFieldCaptureTimeout.getValue() * 1000);
					timerExpires = " Timer set to expire at " + LocalDateTime.now().toString().split("T")[1].split("\\.")[0];
				}

				labelStatus.setText(statusCapturing + timerExpires);

				if (isAHotkeyResult)
					speakIfNotMuted("Capture started");
			}
		});
	}

	public static void openInBrowser(String link)
	{
		if (Desktop.isDesktopSupported())
		{
			try
			{
				URI uri = new URI(link);
				Desktop.getDesktop().browse(uri);
			}
			catch (IOException | URISyntaxException e)
			{
				String msg = "Unable to open \"" + link + "\" in the browser";
				new Alert(AlertType.ERROR, msg).showAndWait();
				logger.log(Level.SEVERE, msg, e);
			}
		}
	}

	public static String getSecondaryGeoIpPrefix()
	{
		return secondaryGeoIpPrefix;
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
			alert = new Alert(AlertType.ERROR);
			alert.setHeaderText("Unable to check for updates");
			alert.setHeaderText(e.getMessage());
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
				openInBrowser(link.getText());
				alert.close();
			}
		});
		fp.getChildren().addAll(lbl, link);

		alert.getDialogPane().contentProperty().set(fp);

		return alert;
	}

	private void speakIfNotMuted(String line)
	{
		if (!isMuted)
			tts.speak(line);
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

	private Runnable captureHotkeyPressed = new Runnable()
	{
		@Override
		public void run()
		{
			String line;

			if (activeButton == btnStart)
			{
				line = "Pressing start capturing button";
				isAHotkeyResult = true;
			}
			else
				line = "Pressing Stop capturing button";

			speakIfNotMuted(line);
			activeButton.fire();
		}
	};

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

	private Runnable ptsHotkeyPressed = new Runnable()
	{
		@Override
		public void run()
		{
			String address = comboPTSipToPing.getEditor().getText();
			List<String> items = comboPTSipToPing.getItems();

			if (!items.contains(address))
				items.add(address);

			String ping = getPingForIP(address, null); //default timeout

			if (ping.contains("milliseconds"))
				tts.speak(ping);
			else
				if (ping.contains("Timeout"))
					tts.speak("Ping time out");
				else
					tts.speak("Ping failed");
		}
	};
}
