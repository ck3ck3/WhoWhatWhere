package whowhatwhere.controller.appearancecounter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

import org.apache.commons.io.FileUtils;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import numbertextfield.NumberTextField;
import whowhatwhere.Main;
import whowhatwhere.controller.GUIController;
import whowhatwhere.controller.HotkeyRegistry;
import whowhatwhere.controller.commands.ping.PingCommandScreen;
import whowhatwhere.controller.commands.trace.TraceCommandScreen;
import whowhatwhere.model.PropertiesByType;
import whowhatwhere.model.TextToSpeech;
import whowhatwhere.model.geoipresolver.GeoIPInfo;
import whowhatwhere.model.geoipresolver.GeoIPResolver;
import whowhatwhere.model.networksniffer.CaptureStartListener;
import whowhatwhere.model.networksniffer.DeviceAddressesAndDescription;
import whowhatwhere.model.networksniffer.NetworkSniffer;
import whowhatwhere.model.networksniffer.appearancecounter.AppearanceCounterResults;
import whowhatwhere.model.networksniffer.appearancecounter.IpAppearancesCounter;

public class AppearanceCounterUI implements CaptureStartListener
{
	private final static Logger logger = Logger.getLogger(AppearanceCounterUI.class.getPackage().getName());
	private final static String manageUserNotesFormLocation = "/whowhatwhere/view/ManageUserNotes.fxml";

	private final static String propsChkboxUDP = "chkboxUDP";
	private final static String propsChkboxTCP = "chkboxTCP";
	private final static String propsChkboxICMP = "chkboxICMP";
	private final static String propsChkboxHTTP = "chkboxHTTP";
	private final static String propsRadioTimedCapture = "radioTimedCapture";
	private final static String propsNumFieldCaptureTimeout = "numFieldCaptureTimeout";
	private final static String propsRadioManual = "radioManual";
	private final static String propsChkboxGetLocation = "chkboxGetLocation";
	private final static String propsChkboxPing = "chkboxPing";
	private final static String propsNumFieldPingTimeout = "numFieldPingTimeout";
	private final static String propsChkboxUseCaptureHotkey = "chkboxUseCaptureHotkey";
	private final static String propsCaptureHotkeyKeycode = "captureHotkeyKeycode";
	private final static String propsCaptureHotkeyModifiers = "captureHotkeyModifiers";
	private final static String propsChkboxUseTTS = "chkboxUseTTS";
	private final static String propsNumFieldRowsToRead = "numFieldRowsToRead";
	private final static String propsChkboxFilterResults = "chkboxFilterResults";
	private final static String propsComboColumnsSelection = "comboColumnsSelection";
	private final static String propsTextColumnContains = "textColumnContains";
	private final static String propsTTSCheckBox = "TTSCheckBox ";
	private final static String propsExportCSVPath = "Export to CSV path";

	private final static int maxPingTimeout = 3000;
	private final static String statusIdle = "Status: Idle";
	private final static String statusGettingReady = "Status: Getting ready to start capture...";
	private final static String statusCapturing = "Status: Capture in progress...";
	private final static String statusStopping = "Status: Stopping capture...";
	private final static String statusResults = "Status: Fetching results...";
	private final static String msgTimerExpired = "Timer expired, stopping capture";
	private final static String captureHotkeyID = "Mosed used IPs capture hotkey";
	private final static String voiceForTTS = "kevin16";
	
	private final static String emptyNotesString = "(Click to add notes)";
	private final static String userNotesFilename = "userNotes.properties";

	private GUIController controller;

	private Button btnStart;
	private Button btnStop;
	private NumberTextField numFieldCaptureTimeout;
	private NumberTextField numFieldPingTimeout;
	private NumberTextField numFieldRowsToRead;
	private RadioButton radioManual;
	private RadioButton radioTimedCapture;
	private AnchorPane paneCaptureOptions;
	private AnchorPane paneUseTTS;
	private CheckBox chkboxPing;
	private CheckBox chkboxGetLocation;
	private TableView<IPInfoRowModel> tableResults;
	private Button btnExportTableToCSV;
	private TableColumn<IPInfoRowModel, Integer> columnPacketCount;
	private TableColumn<IPInfoRowModel, String> columnIP;
	private TableColumn<IPInfoRowModel, String> columnNotes;
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
	private List<CheckBox> chkboxListColumns;
	private Timer timer;
	private TimerTask timerTask;
	private boolean isTimedTaskRunning = false;
	private boolean isAHotkeyResult = false;
	private int protocolBoxesChecked = 0;
	private Map<RadioButton, DeviceAddressesAndDescription> buttonToIpMap;
	private NetworkSniffer sniffer = new NetworkSniffer();
	private int captureHotkeyKeyCode;
	private int captureHotkeyModifiers;
	private TextToSpeech tts = new TextToSpeech(voiceForTTS);
	private Properties userNotes;
	private String suggestedPathForCSVFile;

	private Runnable captureHotkeyPressed = new Runnable()
	{
		@Override
		public void run()
		{
			String line = "Pressing " + activeButton.getText() + " button";

			isAHotkeyResult = true;
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

		numFieldPingTimeout.setMaxValue(maxPingTimeout);

		btnStop.setDisable(true);

		initUserNotes();
		initTable();
		initColumnListForTTS();
		initButtonHandlers();
	}

	private void initUserNotes()
	{
		InputStream in;
		File userNotesFile = new File(userNotesFilename);
		userNotes = new Properties();

		if (userNotesFile.exists())
		{
			try
			{
				in = new FileInputStream(userNotesFile);
				
				userNotes.load(in);
				in.close();
			}
			catch (IOException e)
			{
			}
		}
	}

	private void initUIElementsFromController()
	{
		btnStart = controller.getBtnStart();
		btnStop = controller.getBtnStop();
		numFieldCaptureTimeout = controller.getNumFieldCaptureTimeout();
		numFieldPingTimeout = controller.getNumberFieldPingTimeout();
		numFieldRowsToRead = controller.getNumFieldRowsToRead();
		radioManual = controller.getRadioManual();
		radioTimedCapture = controller.getRadioTimedCapture();
		paneCaptureOptions = controller.getPaneCaptureOptions();
		paneUseTTS = controller.getPaneUseTTS();
		chkboxPing = controller.getChkboxPing();
		chkboxGetLocation = controller.getChkboxGetLocation();
		tableResults = controller.getTableResults();
		btnExportTableToCSV = controller.getBtnExportTableToCSV();
		columnPacketCount = controller.getColumnPacketCount();
		columnIP = controller.getColumnIP();
		columnNotes = controller.getColumnNotes();
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

		chkboxFilterResults.selectedProperty().addListener((ov, old_val, new_val) -> paneFilterResults.setDisable(!new_val));
		chkboxPing.selectedProperty().addListener((ov, old_val, new_val) -> numFieldPingTimeout.setDisable(!new_val));

		radioManual.setOnAction(event -> numFieldCaptureTimeout.setDisable(true));
		radioTimedCapture.setOnAction(event -> numFieldCaptureTimeout.setDisable(false));

		ChangeListener<Boolean> protocolBoxes = (observable, oldValue, newValue) ->
		{
			if (newValue)
			{
				chkboxAnyProtocol.setSelected(false);
				protocolBoxesChecked++;
			}
			else
				if (--protocolBoxesChecked == 0) //we are unchecking the last checkbox
					chkboxAnyProtocol.setSelected(true);
		};

		chkboxUDP.selectedProperty().addListener(protocolBoxes);
		chkboxTCP.selectedProperty().addListener(protocolBoxes);
		chkboxICMP.selectedProperty().addListener(protocolBoxes);
		chkboxHTTP.selectedProperty().addListener(protocolBoxes);
		
		btnExportTableToCSV.setOnAction(e ->
		{
			StringBuilder csvData = new StringBuilder();
			
			for (IPInfoRowModel row : tableResults.getItems())
				csvData.append(String.join(",", row.getFullRowDataAsOrderedList()) + "\n");
			
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Save table contents in CSV format");
			fileChooser.setInitialDirectory(new File(suggestedPathForCSVFile));
			fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV Files", "*.csv"), new FileChooser.ExtensionFilter("Text Files", "*.txt"));
			
			Stage stage = controller.getStage();
			
			File file = fileChooser.showSaveDialog(stage);
            if (file != null) 
            {
            	suggestedPathForCSVFile = file.getParentFile().getAbsolutePath();
            	
				try
				{
					FileUtils.writeStringToFile(file, csvData.toString(), (String)null);
				}
				catch (Exception ex)
				{
					new Alert(AlertType.ERROR, "Unable to save CSV file. See log file for details.").showAndWait();
					logger.log(Level.SEVERE, "Unable to save CSV file", ex);
				}
            }
		});

		chkboxUseTTS.selectedProperty().addListener((ov, old_val, new_val) ->
		{
			tts.setMuted(!new_val);
			paneUseTTS.setDisable(!new_val);
		});

		chkboxUseCaptureHotkey.selectedProperty().addListener(hotkeyRegistry.generateChangeListenerForHotkeyCheckbox(captureHotkeyID, captureHotkeyModifiers, captureHotkeyKeyCode,
				chkboxUseCaptureHotkey, labelCurrCaptureHotkey, paneEnableCaptureHotkey, captureHotkeyPressed));
	}

	private void initTable()
	{
		tableResults.setPlaceholder(new Label()); //remove default string on empty table
		tableResults.getSortOrder().add(columnPacketCount);

		columnPacketCount.setCellValueFactory(new PropertyValueFactory<>("packetCount"));
		columnIP.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
		columnNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));
		columnOwner.setCellValueFactory(new PropertyValueFactory<>("owner"));
		columnPing.setCellValueFactory(new PropertyValueFactory<>("ping"));
		columnCountry.setCellValueFactory(new PropertyValueFactory<>("country"));
		columnRegion.setCellValueFactory(new PropertyValueFactory<>("region"));
		columnCity.setCellValueFactory(new PropertyValueFactory<>("city"));

		columnNotes.setCellFactory(TextFieldTableCell.forTableColumn()); //make Notes column editable
		columnNotes.setOnEditCommit(rowModel ->
		{
			String newContent = rowModel.getNewValue().isEmpty() ? emptyNotesString : rowModel.getNewValue();
			String ipAddress = rowModel.getRowValue().ipAddressProperty().get();
			
			rowModel.getRowValue().setNotes(newContent);

			if (newContent.equals(emptyNotesString)) //if user deleted an existing note
				userNotes.remove(ipAddress); //doesn't do anything if the key didn't exist
			else
				userNotes.put(ipAddress, newContent);
			
			saveUserNotes(userNotes);
		});

		generatePopupMenus();
	}

	private void initColumnListForTTS()
	{
		chkboxListColumns = new ArrayList<>();

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
			TableRow<IPInfoRowModel> row = new TableRow<>(); //this row will be populated before the event handlers are called.
															 //this reference points to the object that will be used for the actual populated row,
															 //so the event handlers can safely use it.
			
			MenuItem copyRowAsCSV = new MenuItem("Copy whole row in CSV format");
			copyRowAsCSV.setOnAction(event ->
			{
				Clipboard clipboard = Clipboard.getSystemClipboard();
				ClipboardContent content = new ClipboardContent();

				content.putString(String.join(",", row.getItem().getFullRowDataAsOrderedList()));
				
				clipboard.setContent(content);
			});
			
			Menu copyMenu = new Menu("Copy");
			ObservableList<MenuItem> copyMenuItems = copyMenu.getItems();
			copyMenuItems.add(copyRowAsCSV);

			for (TableColumn<IPInfoRowModel, ?> column : tableResults.getColumns()) //build the submenu to copy each column value
			{
				String columnName = column.getText();
				MenuItem itemForcolumn = new MenuItem(columnName);
				itemForcolumn.setOnAction(event ->
				{
					Clipboard clipboard = Clipboard.getSystemClipboard();
					ClipboardContent content = new ClipboardContent();
					
					Map<String, String> mapColumnNameToPropertyValue = mapColumnNameToPropertyValue(row.getItem());
					
					String valueToCopy = mapColumnNameToPropertyValue.get(columnName);
					content.putString(valueToCopy);

					clipboard.setContent(content);					
				});
				
				copyMenuItems.add(itemForcolumn);
			}

			MenuItem getGeoIPinfo = new MenuItem("See more GeoIP results for this IP in browser");
			getGeoIPinfo.setOnAction(event -> Main.openInBrowser(GeoIPResolver.getSecondaryGeoIpPrefix() + row.getItem().ipAddressProperty().getValue()));

			MenuItem sendIPToPTS = new MenuItem("Set this IP in Ping-to-Speech (in Utilities tab)");
			sendIPToPTS.setOnAction(event ->
			{
				controller.getComboPTSipToPing().getEditor().setText(row.getItem().ipAddressProperty().getValue());
				tabPane.getSelectionModel().select(controller.getUtilsTab());
			});

			MenuItem pingIP = new MenuItem("Ping this IP");
			pingIP.setOnAction(event -> pingCommand(row.getItem().ipAddressProperty().getValue()));

			MenuItem traceIP = new MenuItem("Visual trace this IP");
			traceIP.setOnAction(event -> traceCommand(row.getItem().ipAddressProperty().getValue()));

			ContextMenu rowMenu = new ContextMenu(copyMenu, getGeoIPinfo, sendIPToPTS, pingIP, traceIP);

			// only display context menu for non-null items:
			row.contextMenuProperty().bind(Bindings.when(Bindings.isNotNull(row.itemProperty())).then(rowMenu).otherwise((ContextMenu) null));

			return row;
		});
	}

	private void pingCommand(String ip)
	{
		PingCommandScreen cmdScreen;
		Stage stage = controller.getStage();

		try
		{
			cmdScreen = new PingCommandScreen(stage, stage.getScene(), ip);
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "Unable to load Ping (command) screen", e);
			return;
		}

		cmdScreen.showScreenOnNewStage("Pinging " + ip, cmdScreen.getCloseButton());
		cmdScreen.runCommand();
	}

	public void traceCommand(String ip)
	{
		TraceCommandScreen cmdScreen;
		Stage stage = controller.getStage();

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
		String deviceIP = buttonToIpMap.get(controller.getTglGrpNIC().getSelectedToggle()).getIP();
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
				ArrayList<Integer> protocols = new ArrayList<>();

				if (chkboxUDP.isSelected())
					protocols.add(NetworkSniffer.UDP_PROTOCOL);

				if (chkboxTCP.isSelected())
					protocols.add(NetworkSniffer.TCP_PROTOCOL);

				if (chkboxICMP.isSelected())
					protocols.add(NetworkSniffer.ICMP_PROTOCOL);

				if (chkboxHTTP.isSelected())
					protocols.add(NetworkSniffer.HTTP_PROTOCOL);

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
			rowIDToCOlMapping.put(ipInfoRowModel.getRowID(), mapColumnNameToPropertyValue(ipInfoRowModel));

		return rowIDToCOlMapping;
	}
	
	private Map<String, String> mapColumnNameToPropertyValue(IPInfoRowModel ipInfoRowModel)
	{
		Map<String, String> colMapping = new HashMap<>();
		
		colMapping.put("Packet Count", ipInfoRowModel.packetCountProperty().getValue().toString());
		colMapping.put("User Notes", ipInfoRowModel.notesProperty().getValue());
		colMapping.put("IP Address", ipInfoRowModel.ipAddressProperty().getValue());
		colMapping.put("Owner", ipInfoRowModel.ownerProperty().getValue());
		colMapping.put("Ping", ipInfoRowModel.pingProperty().getValue());
		colMapping.put("Country", ipInfoRowModel.countryProperty().getValue());
		colMapping.put("Region", ipInfoRowModel.regionProperty().getValue());
		colMapping.put("City", ipInfoRowModel.cityProperty().getValue());
		
		return colMapping;
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
		btnExportTableToCSV.setDisable(duringCapture);

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

				Platform.runLater(() ->
				{
					btnStop.setDisable(true);
					labelStatus.setText(statusStopping);
				});

				return null;
			}
		};

		new Thread(workerThreadTask).start();
	}

	private TimerTask initTimer()
	{
		return new TimerTask()
		{
			@Override
			public void run()
			{
				Platform.runLater(() -> timerExpired());
			}
		};
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
			String notes = emptyNotesString;
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
				ping = NetworkSniffer.pingAsString(ip, numFieldPingTimeout.getValue());
			
			notes = userNotes.getProperty(ip, emptyNotesString);

			row = new IPInfoRowModel(id, amountOfAppearances, ip, notes, owner, ping, country, region, city);
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
		chkboxUseCaptureHotkey.setSelected(PropertiesByType.getBoolProperty(props, propsChkboxUseCaptureHotkey, false));

		if (chkboxUseCaptureHotkey.isSelected())
			hotkeyRegistry.addHotkey(captureHotkeyID, captureHotkeyModifiers, captureHotkeyKeyCode, labelCurrCaptureHotkey, captureHotkeyPressed);

		chkboxUseTTS.setSelected(PropertiesByType.getBoolProperty(props, propsChkboxUseTTS, false));
		numFieldRowsToRead.setText(PropertiesByType.getProperty(props, propsNumFieldRowsToRead));

		chkboxFilterResults.setSelected(PropertiesByType.getBoolProperty(props, propsChkboxFilterResults, false));
		textColumnContains.setText(PropertiesByType.getProperty(props, propsTextColumnContains, ""));

		String comboValue = PropertiesByType.getProperty(props, propsComboColumnsSelection, "");
		if (!comboValue.isEmpty())
			comboColumns.setValue(comboValue);

		for (CheckBox box : chkboxListColumns)
			box.setSelected(PropertiesByType.getBoolProperty(props, propsTTSCheckBox + box.getText(), false));
	}

	private void setCaptureOptionsPane(Properties props)
	{
		radioTimedCapture.setSelected(PropertiesByType.getBoolProperty(props, propsRadioTimedCapture, false));
		numFieldCaptureTimeout.setText(PropertiesByType.getProperty(props, propsNumFieldCaptureTimeout));
		radioManual.setSelected(PropertiesByType.getBoolProperty(props, propsRadioManual, false));
		chkboxGetLocation.setSelected(PropertiesByType.getBoolProperty(props, propsChkboxGetLocation, false));
		chkboxPing.setSelected(PropertiesByType.getBoolProperty(props, propsChkboxPing, false));
		numFieldPingTimeout.setText(PropertiesByType.getProperty(props, propsNumFieldPingTimeout));
	}

	private void setProtocolCheckboxes(Properties props)
	{
		boolean isChecked;
		protocolBoxesChecked = 0;

		isChecked = PropertiesByType.getBoolProperty(props, propsChkboxUDP, false);
		if (isChecked)
			protocolBoxesChecked++;

		chkboxUDP.setSelected(isChecked);

		isChecked = PropertiesByType.getBoolProperty(props, propsChkboxTCP, false);
		if (isChecked)
			protocolBoxesChecked++;

		chkboxTCP.setSelected(isChecked);

		isChecked = PropertiesByType.getBoolProperty(props, propsChkboxICMP, false);
		if (isChecked)
			protocolBoxesChecked++;

		chkboxICMP.setSelected(isChecked);

		isChecked = PropertiesByType.getBoolProperty(props, propsChkboxHTTP, false);
		if (isChecked)
			protocolBoxesChecked++;

		chkboxHTTP.setSelected(isChecked);

		if (protocolBoxesChecked == 0)
			chkboxAnyProtocol.setSelected(true);
	}

	private void setDisabledPanes()
	{
		numFieldCaptureTimeout.setDisable(!radioTimedCapture.isSelected());
		numFieldPingTimeout.setDisable(!chkboxPing.isSelected());
		paneEnableCaptureHotkey.setDisable(!chkboxUseCaptureHotkey.isSelected());
		paneFilterResults.setDisable(!chkboxFilterResults.isSelected());

		boolean useTTS = chkboxUseTTS.isSelected();
		paneUseTTS.setDisable(!useTTS);
		tts.setMuted(!useTTS);
	}

	public void loadLastRunConfig(Properties props)
	{
		setProtocolCheckboxes(props);
		setCaptureOptionsPane(props);
		setCaptureHotkeyAndPane(props);
		setDisabledPanes();
		
		suggestedPathForCSVFile = PropertiesByType.getProperty(props, propsExportCSVPath, System.getProperty("user.dir"));
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
		props.put(propsNumFieldPingTimeout, numFieldPingTimeout.getText());
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
		
		saveUserNotes(userNotes);
		props.put(propsExportCSVPath, suggestedPathForCSVFile);
	}

	public static void saveUserNotes(Properties props)
	{
		try
		{
			FileOutputStream out = new FileOutputStream(userNotesFilename);
			props.store(out, "User notes");
			out.close();
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "Unable to save user notes file " + userNotesFilename, e);
		}
	}

	public void openManageUserNotesScreen()
	{
		ManageUserNotesScreen userNotesScreen;
		Stage stage = (Stage) controller.getTabPane().getScene().getWindow();

		try
		{
			userNotesScreen = new ManageUserNotesScreen(manageUserNotesFormLocation, stage, stage.getScene(), userNotes);
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "Unable to load user notes management screen", e);
			return;
		}

		userNotesScreen.showScreenOnNewStage("Manage User Notes", userNotesScreen.getCloseButton());
	}

	/**
	 * @return a map that maps user note to a list of IPs that have that note 
	 */
	public Map<String, List<String>> getUserNotesReverseMap()
	{
		Map<String, List<String>> reverseMap = new HashMap<String, List<String>>();
		
		for (Object ipObj : userNotes.keySet())
		{
			String ip = (String) ipObj;
			String note = userNotes.getProperty(ip);
			
			List<String> listOfIPs = reverseMap.get(note);
			
			if (listOfIPs == null) //first ip for that note
			{
				listOfIPs = new ArrayList<String>();
				listOfIPs.add(ip);
				reverseMap.put(note, listOfIPs);
			}
			else //there's already a list of ips for this note
				listOfIPs.add(ip);
		}
		
		return reverseMap;
	}
}
