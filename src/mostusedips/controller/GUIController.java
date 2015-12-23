package mostusedips.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnetpcap.PcapAddr;
import org.jnetpcap.PcapIf;
import org.jnetpcap.protocol.network.Icmp;
import org.jnetpcap.protocol.tcpip.Http;
import org.jnetpcap.protocol.tcpip.Tcp;
import org.jnetpcap.protocol.tcpip.Udp;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import mostusedips.Main;
import mostusedips.controller.commands.ping.PingCommandScreen;
import mostusedips.controller.commands.trace.TraceCommandScreen;
import mostusedips.model.geoipresolver.GeoIPInfo;
import mostusedips.model.geoipresolver.GeoIPResolver;
import mostusedips.model.hotkey.HotKeyExecuter;
import mostusedips.model.hotkey.HotKeyManager;
import mostusedips.model.ipsniffer.CaptureStartListener;
import mostusedips.model.ipsniffer.IpAppearancesCounter;
import mostusedips.model.ipsniffer.IpSniffer;
import mostusedips.model.tts.TextToSpeech;
import mostusedips.view.NumberTextField;
import javafx.scene.layout.Pane;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class GUIController implements Initializable, CaptureStartListener
{
    private final static String mainFormLocation = "/mostusedips/view/MainForm.fxml";
    private final static String propsFileLocation = Main.getAppName() + ".properties";
    private final static String defaultPropsResource = "/defaultLastRun.properties";
    private final static String voiceForTTS = "kevin16";
    private final static String Ipv4Prefix = "INET4:";
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

    private final static Logger logger = Logger.getLogger(Main.getAppName());

    @FXML
    ScrollPane scrollPane;
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
    CheckBox chkboxGetLocation;
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
    TableColumn<IPInfoRowModel, String> columnPing;
    @FXML
    private TableColumn<IPInfoRowModel, String> columnCountry;
    @FXML
    private TableColumn<IPInfoRowModel, String> columnRegion;
    @FXML
    private TableColumn<IPInfoRowModel, String> columnCity;
    @FXML
    private Label labelCurrHotkey;

    private NumberTextField numFieldCaptureTimeout;
    private NumberTextField numFieldRowsToRead;
    private NumberTextField numberFieldPingTimeout;
    private Alert alertChangeHotkey;

    private ToggleGroup tglGrpNIC = new ToggleGroup();
    private ToggleGroup tglGrpCaptureOptions = new ToggleGroup();
    private IpSniffer sniffer = new IpSniffer();
    private List<PcapIf> listOfDevices;
    private HashMap<RadioButton, PcapIf> buttonToNicMap = new HashMap<RadioButton, PcapIf>();
    private TextToSpeech tts = new TextToSpeech(voiceForTTS);
    private HotKeyManager hotKey;
    private Timer timer;
    private TimerTask timerTask;
    private boolean isTimedTaskRunning = false;
    private Button activeButton;
    private boolean isAHotkeyResult = false;
    private ArrayList<CheckBox> chkboxListColumns;
    private int protocolBoxesChecked = 0;

    @FXML
    CheckBox chkboxUseHotkey;
    @FXML
    Button btnConfigHotkey;
    @FXML
    AnchorPane paneEnableHotkey;
    @FXML
    CheckBox chkboxUseTTS;
    @FXML
    AnchorPane paneUseTTS;
    @FXML
    HBox hboxColumnNames;
    @FXML
    Label labelReadFirstRows;
    @FXML
    TextArea textAreaOutput;
    @FXML
    ButtonBar buttonBar;
    @FXML
    Button btnCloseCmd;
    @FXML
    SplitPane splitPaneRoot;
    @FXML
    CheckBox chkboxPing;
    @FXML
    CheckBox chkboxFilterResults;
    @FXML
    Pane paneFilterResults;
    @FXML
    ComboBox<String> comboColumns;
    @FXML
    TextField textColumnContains;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
	sniffer.setCapureStartListener(this);
	activeButton = btnStart;

	if (!createNICRadioButtons())
	    return;

	vboxNICs.setSpacing(10);

	createNumTextFields();

	radioManual.setToggleGroup(tglGrpCaptureOptions);
	radioTimedCapture.setToggleGroup(tglGrpCaptureOptions);

	btnStop.setDisable(true);

	initHotkeyChangeAlert();
	initTable();
	initColumnListForTTS();

	loadLastRunConfig();
	initButtonHandlers();
    }

    private void initHotkeyChangeAlert()
    {
	alertChangeHotkey = new Alert(AlertType.INFORMATION);

	alertChangeHotkey.setTitle("Change hotkey");
	alertChangeHotkey.setHeaderText("Choose a new hotkey");
	alertChangeHotkey.setContentText("Press the new hotkey");
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

	btnConfigHotkey.setOnAction(new EventHandler<ActionEvent>()
	{
	    @Override
	    public void handle(ActionEvent e)
	    {
		hotKey.setKeySelection(true);
		splitPaneRoot.setDisable(true);
		alertChangeHotkey.showAndWait();
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

	chkboxUseHotkey.selectedProperty().addListener(new ChangeListener<Boolean>()
	{
	    @Override
	    public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val)
	    {
		if (new_val)
		{
		    if (!GlobalScreen.isNativeHookRegistered())
			hotKey.registerNativeHook();
		}
		else
		{
		    if (GlobalScreen.isNativeHookRegistered())
			hotKey.unregisterNativeHook();
		}

		paneEnableHotkey.setDisable(!new_val);
	    }
	});

	chkboxUseTTS.selectedProperty().addListener(new ChangeListener<Boolean>()
	{
	    @Override
	    public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val)
	    {
		tts.setMuted(!new_val);
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
    }

    private void initHotKey(int modifiers, int key)
    {
	HotKeyExecuter executer = new HotKeyExecuter()
	{
	    public void keyPressed(Integer modifiers, Integer keyCode, boolean isNewKey)
	    {
		if (isNewKey)
		{
		    String keyText = NativeKeyEvent.getKeyText(keyCode);
		    String modifiersText = NativeKeyEvent.getModifiersText(modifiers);

		    hotKey.setModifiers(modifiers);
		    hotKey.setHotkeyToCatch(keyCode);
		    hotKey.setKeySelection(false);

		    Platform.runLater(new Runnable()
		    {

			@Override
			public void run()
			{
			    String line = "Current hotkey: " + (modifiersText.isEmpty() ? "" : modifiersText + "+");
			    labelCurrHotkey.setText(line + keyText);
			    alertChangeHotkey.close();
			    splitPaneRoot.setDisable(false);
			}
		    });

		}
		else //hotkey pressed
		{
		    Platform.runLater(new Runnable()
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

			    activeButton.fire();
			    tts.speak(line);
			}
		    });
		}
	    }
	};

	hotKey = new HotKeyManager(executer, modifiers, key);

	String modifiersText = NativeKeyEvent.getModifiersText(modifiers);
	String keyText = NativeKeyEvent.getKeyText(key);
	String line = "Current hotkey: " + (modifiersText.isEmpty() ? "" : modifiersText + "+");

	labelCurrHotkey.setText(line + keyText);

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
			openInBrowser(getSecondaryGeoIpPrefix() + row.getItem().getIpAddress());
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
			content.putString(row.getItem().getIpAddress());
			clipboard.setContent(content);
		    }
		});

		MenuItem pingIP = new MenuItem("Ping this IP");
		pingIP.setOnAction(new EventHandler<ActionEvent>()
		{
		    @Override
		    public void handle(ActionEvent event)
		    {
			pingCommand(row.getItem().getIpAddress());
		    }
		});

		MenuItem traceIP = new MenuItem("Traceroute this IP");
		traceIP.setOnAction(new EventHandler<ActionEvent>()
		{
		    @Override
		    public void handle(ActionEvent event)
		    {
			traceCommand(row.getItem().getIpAddress());
		    }
		});

		final ContextMenu rowMenu = new ContextMenu(getGeoIPinfo, copyIPtoClipboard, pingIP, traceIP);

		// only display context menu for non-null items:
		row.contextMenuProperty().bind(Bindings.when(Bindings.isNotNull(row.itemProperty())).then(rowMenu).otherwise((ContextMenu) null));

		return row;
	    }
	});
    }

    private void pingCommand(String ip)
    {
	Stage stage = (Stage) splitPaneRoot.getScene().getWindow();

	PingCommandScreen cmdScreen = new PingCommandScreen(stage, stage.getScene(), ip);

	cmdScreen.showScreen();
	cmdScreen.runCommand();
    }

    private void traceCommand(String ip)
    {
	Stage stage = (Stage) splitPaneRoot.getScene().getWindow();
	TraceCommandScreen cmdScreen = new TraceCommandScreen(stage, stage.getScene(), ip);

	cmdScreen.showScreen();
    }

    private void startButtonPressed()
    {
	StringBuilder errbuf = new StringBuilder();
	PcapIf device = buttonToNicMap.get(tglGrpNIC.getSelectedToggle());

	changeGuiTemplate(true);

	Task<Void> workerThreadTask = new Task<Void>()
	{
	    @Override
	    protected Void call() throws Exception
	    {
		ArrayList<Integer> list = getSelectedProtocols();
		sniffer.startCapture(device, list, errbuf);
		return null;
	    }

	    @Override
	    protected void succeeded() //capture finished
	    {
		labelStatus.setText(statusResults);

		fillTable(sniffer.getResults());

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
		ArrayList<Integer> list = new ArrayList<Integer>();

		if (chkboxUDP.isSelected())
		    list.add(Udp.ID);

		if (chkboxTCP.isSelected())
		    list.add(Tcp.ID);

		if (chkboxICMP.isSelected())
		    list.add(Icmp.ID);

		if (chkboxHTTP.isSelected())
		    list.add(Http.ID);

		return list;
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

	tts.speak(result);
    }

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

	    colMapping.put("Packet Count", ipInfoRowModel.getPacketCount().toString());
	    colMapping.put("IP Address", ipInfoRowModel.getIpAddress());
	    colMapping.put("Owner", ipInfoRowModel.getOwner());
	    colMapping.put("Ping", ipInfoRowModel.getPing());
	    colMapping.put("Country", ipInfoRowModel.getCountry());
	    colMapping.put("Region", ipInfoRowModel.getRegion());
	    colMapping.put("City", ipInfoRowModel.getCity());

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
	    tts.speak(msgTimerExpired);
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

	tableResults.requestFocus(); //otherwise table contents only shown when hovering over the table. JavaFX bug?
    }

    private String getPingForIP(String ip, int timeout)
    {
	String ping;
	PingCommandScreen pingCmd = new PingCommandScreen(null, null, ip, "-n 1 -w " + timeout);

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
	    ping = pingResult.replace("ms\n", " milliseconds");

	return ping;
    }

    /**
     * @return false on failure, true otherwise
     */
    private boolean createNICRadioButtons()
    {
	StringBuilder errbuf = new StringBuilder();

	listOfDevices = sniffer.getListOfDevices(errbuf);

	if (listOfDevices == null)
	{
	    Label label = new Label("Unable to find any network interfaces");
	    vboxNICs.getChildren().add(label);
	    btnStart.setDisable(true);
	    logger.log(Level.SEVERE, "Unable to find any network interfaces. More info: " + errbuf);
	    return false;
	}

	int index = 1; //index of radio button in the vbox. starts at 1 because we already added a label earlier

	for (PcapIf device : listOfDevices)
	{
	    String description = (device.getDescription() != null) ? device.getDescription() : "No description available";
	    String IP = null;
	    for (PcapAddr pcapAddr : device.getAddresses())
	    {
		String temp = pcapAddr.getAddr().toString();

		if (temp.contains(Ipv4Prefix))
		{
		    IP = temp.replace(Ipv4Prefix, "");
		    break;
		}
	    }

	    if (IP == null)
		continue;

	    RadioButton btn = new RadioButton(description + " " + IP);
	    btn.setUserData(index++);
	    btn.setToggleGroup(tglGrpNIC);
	    btn.setPadding(new Insets(0, 0, 0, 10));
	    buttonToNicMap.put(btn, device);

	    vboxNICs.getChildren().add(btn);
	}

	tglGrpNIC.selectToggle(tglGrpNIC.getToggles().get(0)); //select the first button

	return true;
    }

    private void exitButtonPressed()
    {
	saveCurrentValuesToProperties();

	hotKey.unregisterNativeHook();
	sniffer.cleanup();
	tts.cleanup();

	Platform.setImplicitExit(true); //was initially set to false when initializing the systray
	Platform.exit();
	System.exit(0); //needed because of the AWT SysTray
    }

    private void saveCurrentValuesToProperties()
    {
	Properties props = new Properties();

	Integer selectedNic = (Integer) (tglGrpNIC.getSelectedToggle().getUserData());

	props.put("Selected NIC index", selectedNic.toString());
	props.put("chkboxUDP", ((Boolean) chkboxUDP.isSelected()).toString());
	props.put("chkboxTCP", ((Boolean) chkboxTCP.isSelected()).toString());
	props.put("chkboxICMP", ((Boolean) chkboxICMP.isSelected()).toString());
	props.put("chkboxHTTP", ((Boolean) chkboxHTTP.isSelected()).toString());
	props.put("radioTimedCapture", ((Boolean) radioTimedCapture.isSelected()).toString());
	props.put("numFieldCaptureTimeout", numFieldCaptureTimeout.getText());
	props.put("radioManual", ((Boolean) radioManual.isSelected()).toString());
	props.put("chkboxGetLocation", ((Boolean) chkboxGetLocation.isSelected()).toString());
	props.put("chkboxPing", ((Boolean) chkboxPing.isSelected()).toString());
	props.put("numberFieldPingTimeout", numberFieldPingTimeout.getText());
	props.put("chkboxUseHotkey", ((Boolean) chkboxUseHotkey.isSelected()).toString());
	props.put("hotkey_key", hotKey.getHotkeyToCatch().toString());
	props.put("hotkey_modifiers", hotKey.getModifiers().toString());
	props.put("chkboxUseTTS", ((Boolean) chkboxUseTTS.isSelected()).toString());
	props.put("numFieldRowsToRead", numFieldRowsToRead.getText());
	props.put("chkboxFilterResults", ((Boolean) chkboxFilterResults.isSelected()).toString());
	props.put("comboColumnsSelection", comboColumns.getValue());
	props.put("textColumnContains", textColumnContains.getText());

	for (CheckBox box : chkboxListColumns)
	    props.put("TTSCheckBox " + box.getText(), ((Boolean) box.isSelected()).toString());

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
	int intVal;
	boolean boolVal;

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

	intVal = getIntProperty(props, "Selected NIC index");
	((RadioButton) (vboxNICs.getChildren().get(intVal))).setSelected(true);

	protocolBoxesChecked = 0;

	boolVal = getBoolProperty(props, "chkboxUDP");
	if (boolVal)
	    protocolBoxesChecked++;

	chkboxUDP.setSelected(boolVal);

	boolVal = getBoolProperty(props, "chkboxTCP");
	if (boolVal)
	    protocolBoxesChecked++;

	chkboxTCP.setSelected(boolVal);

	boolVal = getBoolProperty(props, "chkboxICMP");
	if (boolVal)
	    protocolBoxesChecked++;

	chkboxICMP.setSelected(boolVal);

	boolVal = getBoolProperty(props, "chkboxHTTP");
	if (boolVal)
	    protocolBoxesChecked++;

	chkboxHTTP.setSelected(boolVal);

	if (protocolBoxesChecked == 0)
	    chkboxAnyProtocol.setSelected(true);

	radioTimedCapture.setSelected(getBoolProperty(props, "radioTimedCapture"));
	numFieldCaptureTimeout.setText(props.getProperty("numFieldCaptureTimeout"));
	radioManual.setSelected(getBoolProperty(props, "radioManual"));
	chkboxGetLocation.setSelected(getBoolProperty(props, "chkboxGetLocation"));
	chkboxPing.setSelected(getBoolProperty(props, "chkboxPing"));
	numberFieldPingTimeout.setText(props.getProperty("numberFieldPingTimeout"));
	chkboxUseHotkey.setSelected(getBoolProperty(props, "chkboxUseHotkey"));

	int modifiers = getIntProperty(props, "hotkey_modifiers");
	int hotkey = getIntProperty(props, "hotkey_key");

	initHotKey(modifiers, hotkey);
	if (!chkboxUseHotkey.isSelected())
	    hotKey.unregisterNativeHook();

	chkboxUseTTS.setSelected(getBoolProperty(props, "chkboxUseTTS"));
	numFieldRowsToRead.setText(props.getProperty("numFieldRowsToRead"));

	chkboxFilterResults.setSelected(getBoolProperty(props, "chkboxFilterResults"));
	textColumnContains.setText(props.getProperty("textColumnContains"));

	String comboValue = props.getProperty("comboColumnsSelection");
	if (!comboValue.isEmpty())
	    comboColumns.setValue(comboValue);

	for (CheckBox box : chkboxListColumns)
	    box.setSelected(getBoolProperty(props, "TTSCheckBox " + box.getText()));

	applyGUILogic();
    }

    /**
     * Makes sure all the panes and controls are enabled/disabled according to
     * the gui logic
     */
    private void applyGUILogic()
    {
	if (!radioTimedCapture.isSelected())
	    numFieldCaptureTimeout.setDisable(true);

	if (!chkboxPing.isSelected())
	    numberFieldPingTimeout.setDisable(true);

	if (!chkboxUseHotkey.isSelected())
	    paneEnableHotkey.setDisable(true);

	if (!chkboxFilterResults.isSelected())
	    paneFilterResults.setDisable(true);

	boolean ttsEnabled = chkboxUseTTS.isSelected();
	if (!ttsEnabled)
	{
	    paneUseTTS.setDisable(true);
	    tts.setMuted(true);
	}

    }

    private boolean getBoolProperty(Properties props, String key)
    {
	String value = props.getProperty(key);

	if (value == null)
	    throw new IllegalArgumentException("The key \"" + key + "\" doesn't exist");

	return (value.equals("true") ? true : false);

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
		    tts.speak("Capture started");
	    }
	});

    }

    public static String getMainformlocation()
    {
	return mainFormLocation;
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
}
