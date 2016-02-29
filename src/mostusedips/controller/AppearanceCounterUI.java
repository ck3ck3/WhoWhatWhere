package mostusedips.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import mostusedips.Main;
import mostusedips.controller.commands.ping.PingCommandScreen;
import mostusedips.controller.commands.trace.TraceCommandScreen;
import mostusedips.model.PropertiesByType;
import mostusedips.model.TextToSpeech;
import mostusedips.model.geoipresolver.GeoIPInfo;
import mostusedips.model.geoipresolver.GeoIPResolver;
import mostusedips.model.ipsniffer.CaptureStartListener;
import mostusedips.model.ipsniffer.IPSniffer;
import mostusedips.model.ipsniffer.appearancecounter.AppearanceCounterResults;
import mostusedips.model.ipsniffer.appearancecounter.IpAppearancesCounter;
import mostusedips.view.NumberTextField;

public class AppearanceCounterUI implements CaptureStartListener
{
	private final static Logger logger = Logger.getLogger(AppearanceCounterUI.class.getPackage().getName());

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

	private final static int defaultCaptureTimeout = 10;
	private final static int defaultPingTimeout = 300;
	private final static int defaultRowsToRead = 3;
	private final static int maxPingTimeout = 3000;
	private final static String statusIdle = "Status: Idle";
	private final static String statusGettingReady = "Status: Getting ready to start capture...";
	private final static String statusCapturing = "Status: Capture in progress...";
	private final static String statusStopping = "Status: Stopping capture...";
	private final static String statusResults = "Status: Fetching results...";
	private final static String msgTimerExpired = "Timer expired, stopping capture";
	private final static String captureHotkeyID = "Mosed used IPs capture hotkey";
	private final static String voiceForTTS = "kevin16";

	private GUIController controller;

	private Button btnStart;
	private Button btnStop;
	private NumberTextField numFieldCaptureTimeout;
	private NumberTextField numberFieldPingTimeout;
	private NumberTextField numFieldRowsToRead;
	private RadioButton radioManual;
	private RadioButton radioTimedCapture;
	private AnchorPane paneCaptureOptions;
	private AnchorPane paneUseTTS;
	private CheckBox chkboxPing;
	private Label labelReadFirstRows;
	private CheckBox chkboxGetLocation;
	private TableView<IPInfoRowModel> tableResults;
	private TableColumn<IPInfoRowModel, Integer> columnPacketCount;
	private TableColumn<IPInfoRowModel, String> columnIP;
	private TableColumn<IPInfoRowModel, String> columnOwner;
	private TableColumn<IPInfoRowModel, String> columnPing;
	private TableColumn<IPInfoRowModel, String> columnCountry;
	private TableColumn<IPInfoRowModel, String> columnRegion;
	private TableColumn<IPInfoRowModel, String> columnCity;
	private Label labelCurrCaptureHotkey;
	private CheckBox chkboxAnyProtocol;
	private CheckBox chkboxUDP;
	private CheckBox chkboxTCP;
	private CheckBox chkboxICMP;
	private CheckBox chkboxHTTP;
	private Label labelStatus;
	private CheckBox chkboxUseCaptureHotkey;
	private Button btnConfigCaptureHotkey;
	private AnchorPane paneEnableCaptureHotkey;
	private CheckBox chkboxUseTTS;
	private HBox hboxColumnNames;
	private CheckBox chkboxFilterResults;
	private Pane paneFilterResults;
	private ComboBox<String> comboColumns;
	private TextField textColumnContains;
	private TabPane tabPane;
	private HotkeyRegistry hotkeyRegistry;

	private Button activeButton;
	private ToggleGroup tglGrpCaptureOptions = new ToggleGroup();
	private List<CheckBox> chkboxListColumns;
	private Timer timer;
	private TimerTask timerTask;
	private boolean isTimedTaskRunning = false;
	private boolean isAHotkeyResult = false;
	private int protocolBoxesChecked = 0;
	private Map<RadioButton, String> buttonToIpMap;
	private IPSniffer sniffer = new IPSniffer();
	private int captureHotkeyKeyCode;
	private int captureHotkeyModifiers;
	private TextToSpeech tts = new TextToSpeech(voiceForTTS);

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

			tts.speak(line);
			activeButton.fire();
		}
	};

	public AppearanceCounterUI(GUIController controller)
	{
		this.controller = controller;

		this.hotkeyRegistry = controller.getHotkeyRegistry();
		initUIElementsFromController();

		activeButton = btnStart;

		createNumTextFields();

		radioManual.setToggleGroup(tglGrpCaptureOptions);
		radioTimedCapture.setToggleGroup(tglGrpCaptureOptions);

		btnStop.setDisable(true);

		initTable();
		initColumnListForTTS();
		initButtonHandlers();

	}

	private void initUIElementsFromController()
	{
		btnStart = controller.getBtnStart();
		btnStop = controller.getBtnStop();
		numFieldCaptureTimeout = controller.getNumFieldCaptureTimeout();
		numberFieldPingTimeout = controller.getNumberFieldPingTimeout();
		numFieldRowsToRead = controller.getNumFieldRowsToRead();
		radioManual = controller.getRadioManual();
		radioTimedCapture = controller.getRadioTimedCapture();
		paneCaptureOptions = controller.getPaneCaptureOptions();
		paneUseTTS = controller.getPaneUseTTS();
		chkboxPing = controller.getChkboxPing();
		labelReadFirstRows = controller.getLabelReadFirstRows();
		chkboxGetLocation = controller.getChkboxGetLocation();
		tableResults = controller.getTableResults();
		columnPacketCount = controller.getColumnPacketCount();
		columnIP = controller.getColumnIP();
		columnOwner = controller.getColumnOwner();
		columnPing = controller.getColumnPing();
		columnCountry = controller.getColumnCountry();
		columnRegion = controller.getColumnRegion();
		columnCity = controller.getColumnCity();
		labelCurrCaptureHotkey = controller.getLabelCurrCaptureHotkey();
		chkboxAnyProtocol = controller.getChkboxAnyProtocol();
		chkboxUDP = controller.getChkboxUDP();
		chkboxTCP = controller.getChkboxTCP();
		chkboxICMP = controller.getChkboxICMP();
		chkboxHTTP = controller.getChkboxHTTP();
		labelStatus = controller.getLabelStatus();
		chkboxUseCaptureHotkey = controller.getChkboxUseCaptureHotkey();
		btnConfigCaptureHotkey = controller.getBtnConfigCaptureHotkey();
		paneEnableCaptureHotkey = controller.getPaneEnableCaptureHotkey();
		chkboxUseTTS = controller.getChkboxUseTTS();
		hboxColumnNames = controller.getHboxColumnNames();
		chkboxFilterResults = controller.getChkboxFilterResults();
		paneFilterResults = controller.getPaneFilterResults();
		comboColumns = controller.getComboColumns();
		textColumnContains = controller.getTextColumnContains();
		tabPane = controller.getTabPane();

		buttonToIpMap = controller.getButtonToIpMap();
		hotkeyRegistry = controller.getHotkeyRegistry();
	}

	private void initButtonHandlers()
	{
		btnStart.setOnAction(e -> startButtonPressed());
		btnStop.setOnAction(e -> stopButtonPressed());

		btnConfigCaptureHotkey.setOnAction(hotkeyRegistry.generateEventHandlerForHotkeyConfigButton(captureHotkeyID));

		chkboxFilterResults.selectedProperty().addListener((ChangeListener<Boolean>) (ov, old_val, new_val) -> paneFilterResults.setDisable(!new_val));
		chkboxPing.selectedProperty().addListener((ChangeListener<Boolean>) (ov, old_val, new_val) -> numberFieldPingTimeout.setDisable(!new_val));

		radioManual.setOnAction(event -> numFieldCaptureTimeout.setDisable(true));
		radioTimedCapture.setOnAction(event -> numFieldCaptureTimeout.setDisable(false));

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

		chkboxUseTTS.selectedProperty().addListener((ChangeListener<Boolean>) (ov, old_val, new_val) ->
		{
			tts.setMuted(!new_val);
			paneUseTTS.setDisable(!new_val);
		});

		chkboxUseCaptureHotkey.selectedProperty().addListener(hotkeyRegistry.generateChangeListenerForHotkeyCheckbox(captureHotkeyID, captureHotkeyModifiers, captureHotkeyKeyCode,
				chkboxUseCaptureHotkey, labelCurrCaptureHotkey, paneEnableCaptureHotkey, captureHotkeyPressed));
	}

	private void createNumTextFields()
	{
		numFieldCaptureTimeout = new NumberTextField(String.valueOf(defaultCaptureTimeout), 1);

		numFieldCaptureTimeout.setPrefSize(45, 25);
		numFieldCaptureTimeout.setLayoutX(208);
		numFieldCaptureTimeout.setLayoutY(radioTimedCapture.getLayoutY() - 2);

		numFieldCaptureTimeout.focusedProperty().addListener((ChangeListener<Boolean>) (arg0, oldPropertyValue, newPropertyValue) ->
		{
			if (newPropertyValue)
				radioTimedCapture.setSelected(true);
		});

		numberFieldPingTimeout = new NumberTextField(String.valueOf(defaultPingTimeout), 1, maxPingTimeout);
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

	private void generatePopupMenus()
	{
		tableResults.setRowFactory(tableView ->
		{
			final TableRow<IPInfoRowModel> row = new TableRow<>();

			MenuItem getGeoIPinfo = new MenuItem("See more GeoIP results for this IP in browser");
			getGeoIPinfo.setOnAction(event -> Main.openInBrowser(GeoIPResolver.getSecondaryGeoIpPrefix() + row.getItem().ipAddressProperty()));

			MenuItem copyIPtoClipboard = new MenuItem("Copy IP to clipboard");
			copyIPtoClipboard.setOnAction(event ->
			{
				final Clipboard clipboard = Clipboard.getSystemClipboard();
				final ClipboardContent content = new ClipboardContent();
				content.putString(row.getItem().ipAddressProperty().getValue());
				clipboard.setContent(content);
			});

			MenuItem sendIPToPTS = new MenuItem("Send IP to Ping-to-Speech tab");
			sendIPToPTS.setOnAction(event ->
			{
				controller.getComboPTSipToPing().getEditor().setText(row.getItem().ipAddressProperty().getValue());
				tabPane.getSelectionModel().select(controller.getUtilsTab());
			});

			MenuItem pingIP = new MenuItem("Ping this IP");
			pingIP.setOnAction(event -> pingCommand(row.getItem().ipAddressProperty().getValue()));

			MenuItem traceIP = new MenuItem("Traceroute this IP");
			traceIP.setOnAction(event -> traceCommand(row.getItem().ipAddressProperty().getValue()));

			final ContextMenu rowMenu = new ContextMenu(getGeoIPinfo, copyIPtoClipboard, sendIPToPTS, pingIP, traceIP);

			// only display context menu for non-null items:
			row.contextMenuProperty().bind(Bindings.when(Bindings.isNotNull(row.itemProperty())).then(rowMenu).otherwise((ContextMenu) null));

			return row;
		});
	}

	private void pingCommand(String ip)
	{
		Stage stage = (Stage) tabPane.getScene().getWindow();

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

		cmdScreen.showScreenOnNewStage("Pinging " + ip, cmdScreen.getCloseButton());
		cmdScreen.runCommand();
	}

	public void traceCommand(String ip)
	{
		Stage stage = (Stage) tabPane.getScene().getWindow();
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

		cmdScreen.showScreenOnNewStage("Tracing " + ip, cmdScreen.getCloseButton());
	}

	private void startButtonPressed()
	{
		StringBuilder errbuf = new StringBuilder();
		String deviceIP = buttonToIpMap.get(controller.getTglGrpNIC().getSelectedToggle());
		final CaptureStartListener thisObj = this;

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
					protocols.add(IPSniffer.UDP_PROTOCOL);

				if (chkboxTCP.isSelected())
					protocols.add(IPSniffer.TCP_PROTOCOL);

				if (chkboxICMP.isSelected())
					protocols.add(IPSniffer.ICMP_PROTOCOL);

				if (chkboxHTTP.isSelected())
					protocols.add(IPSniffer.HTTP_PROTOCOL);

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
		ObservableList<IPInfoRowModel> items = tableResults.getItems();
		List<IPInfoRowModel> filteredList;

		if (isNoColumnChecked() || items == null)
			return;

		if (chkboxFilterResults.isSelected())
			filteredList = filterItemsByColValue(items, comboColumns.getValue(), textColumnContains.getText());

		filteredList = (chkboxFilterResults.isSelected() ? filterItemsByColValue(items, comboColumns.getValue(), textColumnContains.getText()) : new ArrayList<>(items));

		int linesToReadInput = numFieldRowsToRead.getValue();
		int totalLines = filteredList.size();
		int rowsToRead = Math.min(linesToReadInput, totalLines);

		StringBuilder[] lines = new StringBuilder[rowsToRead];

		for (int i = 0; i < rowsToRead; i++)
			lines[i] = new StringBuilder();

		Map<Integer, Map<String, String>> rowIDToColMapping = getRowIDToColMapping();

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
					Integer rowID = filteredList.get(i).getRowID();

					Map<String, String> columnMapping = rowIDToColMapping.get(rowID);

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
	private List<IPInfoRowModel> filterItemsByColValue(ObservableList<IPInfoRowModel> items, String colName, String colValue)
	{
		List<IPInfoRowModel> filteredList = new ArrayList<>();
		Map<Integer, Map<String, String>> rowIDToColMapping = getRowIDToColMapping();

		for (IPInfoRowModel row : items)
		{
			Map<String, String> columnMapping = rowIDToColMapping.get(row.getRowID());
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
	private Map<Integer, Map<String, String>> getRowIDToColMapping()
	{
		Map<Integer, Map<String, String>> rowIDToCOlMapping = new HashMap<>();

		ObservableList<IPInfoRowModel> items = tableResults.getItems();

		for (IPInfoRowModel ipInfoRowModel : items)
		{
			Map<String, String> colMapping = new HashMap<>();

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
		controller.getVboxNICs().setDisable(duringCapture);
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
				Platform.runLater(() -> timerExpired());
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

	private void fillTable(List<IpAppearancesCounter> ips)
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
				ping = IPSniffer.pingAsString(ip, numberFieldPingTimeout.getValue().intValue());

			row = new IPInfoRowModel(id, amountOfAppearances, ip, owner, ping, country, region, city);
			data.add(row);
		}

		if (data.isEmpty())
			tableResults.setPlaceholder(new Label("No packets to show"));
		else
			tableResults.setItems(data);
	}

	@Override
	public void captureStartedNotification()
	{
		Platform.runLater(() ->
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
		});
	}

	private void setCaptureHotkeyAndPane(Properties props)
	{
		captureHotkeyModifiers = PropertiesByType.getIntProperty(props, propsCaptureHotkeyModifiers);
		captureHotkeyKeyCode = PropertiesByType.getIntProperty(props, propsCaptureHotkeyKeycode);
		chkboxUseCaptureHotkey.setSelected(PropertiesByType.getBoolProperty(props, propsChkboxUseCaptureHotkey));

		if (chkboxUseCaptureHotkey.isSelected())
			hotkeyRegistry.addHotkey(captureHotkeyID, captureHotkeyModifiers, captureHotkeyKeyCode, labelCurrCaptureHotkey, captureHotkeyPressed);

		chkboxUseTTS.setSelected(PropertiesByType.getBoolProperty(props, propsChkboxUseTTS));
		numFieldRowsToRead.setText(props.getProperty(propsNumFieldRowsToRead));

		chkboxFilterResults.setSelected(PropertiesByType.getBoolProperty(props, propsChkboxFilterResults));
		textColumnContains.setText(props.getProperty(propsTextColumnContains));

		String comboValue = props.getProperty(propsComboColumnsSelection);
		if (!comboValue.isEmpty())
			comboColumns.setValue(comboValue);

		for (CheckBox box : chkboxListColumns)
			box.setSelected(PropertiesByType.getBoolProperty(props, propsTTSCheckBox + box.getText()));
	}

	private void setCaptureOptionsPane(Properties props)
	{
		radioTimedCapture.setSelected(PropertiesByType.getBoolProperty(props, propsRadioTimedCapture));
		numFieldCaptureTimeout.setText(props.getProperty(propsNumFieldCaptureTimeout));
		radioManual.setSelected(PropertiesByType.getBoolProperty(props, propsRadioManual));
		chkboxGetLocation.setSelected(PropertiesByType.getBoolProperty(props, propsChkboxGetLocation));
		chkboxPing.setSelected(PropertiesByType.getBoolProperty(props, propsChkboxPing));
		numberFieldPingTimeout.setText(props.getProperty(propsNumberFieldPingTimeout));
	}

	private void setProtocolCheckboxes(Properties props)
	{
		boolean isChecked;
		protocolBoxesChecked = 0;

		isChecked = PropertiesByType.getBoolProperty(props, propsChkboxUDP);
		if (isChecked)
			protocolBoxesChecked++;

		chkboxUDP.setSelected(isChecked);

		isChecked = PropertiesByType.getBoolProperty(props, propsChkboxTCP);
		if (isChecked)
			protocolBoxesChecked++;

		chkboxTCP.setSelected(isChecked);

		isChecked = PropertiesByType.getBoolProperty(props, propsChkboxICMP);
		if (isChecked)
			protocolBoxesChecked++;

		chkboxICMP.setSelected(isChecked);

		isChecked = PropertiesByType.getBoolProperty(props, propsChkboxHTTP);
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
			tts.setMuted(true);
		}
	}

	public void loadLastRunConfig(Properties props)
	{
		setProtocolCheckboxes(props);
		setCaptureOptionsPane(props);
		setCaptureHotkeyAndPane(props);
		setDisabledPanes();
	}

	public void saveCurrentRunValuesToProperties(Properties props)
	{
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
		props.put(propsCaptureHotkeyKeycode, Integer.toString(hotkeyRegistry.getHotkeyKeycode(captureHotkeyID)));
		props.put(propsCaptureHotkeyModifiers, Integer.toString(hotkeyRegistry.getHotkeyModifiers(captureHotkeyID)));
		props.put(propsChkboxUseTTS, ((Boolean) chkboxUseTTS.isSelected()).toString());
		props.put(propsNumFieldRowsToRead, numFieldRowsToRead.getText());
		props.put(propsChkboxFilterResults, ((Boolean) chkboxFilterResults.isSelected()).toString());
		props.put(propsComboColumnsSelection, comboColumns.getValue());
		props.put(propsTextColumnContains, textColumnContains.getText());

		for (CheckBox box : chkboxListColumns)
			props.put(propsTTSCheckBox + box.getText(), ((Boolean) box.isSelected()).toString());
	}

}
