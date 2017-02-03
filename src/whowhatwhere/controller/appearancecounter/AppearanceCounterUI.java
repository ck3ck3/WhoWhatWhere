package whowhatwhere.controller.appearancecounter;

import java.io.File;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import numbertextfield.NumberTextField;
import whowhatwhere.Main;
import whowhatwhere.controller.GUIController;
import whowhatwhere.controller.HotkeyRegistry;
import whowhatwhere.controller.UserNotes;
import whowhatwhere.model.PropertiesByType;
import whowhatwhere.model.TextToSpeech;
import whowhatwhere.model.geoipresolver.GeoIPInfo;
import whowhatwhere.model.geoipresolver.GeoIPResolver;
import whowhatwhere.model.networksniffer.CaptureStartListener;
import whowhatwhere.model.networksniffer.NetworkSniffer;
import whowhatwhere.model.networksniffer.appearancecounter.AppearanceCounterResults;
import whowhatwhere.model.networksniffer.appearancecounter.IpAppearancesCounter;

public class AppearanceCounterUI implements CaptureStartListener
{
	private final static Logger logger = Logger.getLogger(AppearanceCounterUI.class.getPackage().getName());
	
	private final static String propsChkboxFilterProtocols = "chkboxFilterProtocols";
	private final static String propsChkboxUDP = "chkboxUDP";
	private final static String propsChkboxTCP = "chkboxTCP";
	private final static String propsChkboxICMP = "chkboxICMP";
	private final static String propsChkboxHTTP = "chkboxHTTP";
	private final static String propsChkboxTimedCapture = "chkboxTimedCapture";
	private final static String propsNumFieldCaptureTimeout = "numFieldCaptureTimeout";
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
	private final static String captureHotkeyID = "WhoWhatWhere capture hotkey";
	private final static String voiceForTTS = "kevin16";
	
	private final static String emptyNotesString = "(Click to add notes)";

	private AppearanceCounterController controller;
	private GUIController guiController;

	private Button btnStart;
	private Button btnStop;
	private NumberTextField numFieldCaptureTimeout;
	private NumberTextField numFieldPingTimeout;
	private NumberTextField numFieldRowsToRead;
	private CheckBox chkboxTimedCapture;
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
	private CheckBox chkboxFilterProtocols;
	private CheckBox chkboxUDP;
	private CheckBox chkboxTCP;
	private CheckBox chkboxICMP;
	private CheckBox chkboxHTTP;
	private Label labelStatus;
	private CheckBox chkboxUseCaptureHotkey;
	private Button btnConfigCaptureHotkey;
	private AnchorPane paneEnableCaptureHotkey;
	private CheckBox chkboxUseTTS;
	private GridPane gridPaneColumnNames;
	private CheckBox chkboxFilterResults;
	private Pane paneFilterResults;
	private ComboBox<String> comboColumns;
	private TextField textColumnContains;
	private TabPane tabPane;
	private Pane paneProtocolBoxes;
	private HotkeyRegistry hotkeyRegistry;
	
	private Button activeButton;
	private List<CheckBox> chkboxListColumns;
	private Timer timer;
	private TimerTask timerTask;
	private boolean isTimedTaskRunning = false;
	private boolean isAHotkeyResult = false;
	private int protocolBoxesChecked = 0;
	private NetworkSniffer sniffer = new NetworkSniffer();
	private int captureHotkeyKeyCode;
	private int captureHotkeyModifiers;
	private TextToSpeech tts = new TextToSpeech(voiceForTTS);
	private String suggestedPathForCSVFile;
	private UserNotes userNotes;

	private Runnable captureHotkeyPressed = new Runnable()
	{
		@Override
		public void run()
		{
			String line = activeButton == btnStart ? "Starting capture" : "Stopping capture";

			isAHotkeyResult = true;
			tts.speak(line);
			activeButton.fire();
		}
	};

	public AppearanceCounterUI(GUIController guiController)
	{
		
		this.controller = guiController.getAppearanceCounterController();
		this.guiController = guiController;
		userNotes = guiController.getUserNotes();

		this.hotkeyRegistry = guiController.getHotkeyRegistry();
		initUIElementsFromController();

		activeButton = btnStart;

		numFieldPingTimeout.setMaxValue(maxPingTimeout);

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
		numFieldPingTimeout = controller.getNumberFieldPingTimeout();
		numFieldRowsToRead = controller.getNumFieldRowsToRead();
		chkboxTimedCapture = controller.getChkboxTimedCapture();
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
		chkboxFilterProtocols = controller.getChkboxFilterProtocols();
		chkboxUDP = controller.getChkboxUDP();
		chkboxTCP = controller.getChkboxTCP();
		chkboxICMP = controller.getChkboxICMP();
		chkboxHTTP = controller.getChkboxHTTP();
		labelStatus = controller.getLabelStatus();
		chkboxUseCaptureHotkey = controller.getChkboxUseCaptureHotkey();
		btnConfigCaptureHotkey = controller.getBtnConfigCaptureHotkey();
		paneEnableCaptureHotkey = controller.getPaneEnableCaptureHotkey();
		chkboxUseTTS = controller.getChkboxUseTTS();
		gridPaneColumnNames = controller.getGridPaneColumnNames();
		chkboxFilterResults = controller.getChkboxFilterResults();
		paneFilterResults = controller.getPaneFilterResults();
		comboColumns = controller.getComboColumns();
		textColumnContains = controller.getTextColumnContains();
		paneProtocolBoxes = controller.getPaneProtocolBoxes();

		tabPane = guiController.getTabPane();
		hotkeyRegistry = guiController.getHotkeyRegistry();
	}

	private void initButtonHandlers()
	{
		btnStart.setOnAction(e -> startButtonPressed());
		btnStop.setOnAction(e -> stopButtonPressed());

		btnConfigCaptureHotkey.setOnAction(hotkeyRegistry.generateEventHandlerForHotkeyConfigButton(captureHotkeyID));

		chkboxFilterResults.selectedProperty().addListener((ov, old_val, new_val) -> paneFilterResults.setDisable(!new_val));
		chkboxPing.selectedProperty().addListener((ov, old_val, new_val) -> numFieldPingTimeout.setDisable(!new_val));

		chkboxTimedCapture.selectedProperty().addListener((ov, old_val, new_val) -> numFieldCaptureTimeout.setDisable(!new_val));

		chkboxFilterProtocols.selectedProperty().addListener((ov, old_val, new_val) -> paneProtocolBoxes.setDisable(!new_val));
		
		ChangeListener<Boolean> protocolBoxes = (observable, oldValue, newValue) ->
		{
			if (newValue)
				protocolBoxesChecked++;
			else
				if (--protocolBoxesChecked == 0) //we are unchecking the last checkbox
				{
					chkboxFilterProtocols.setSelected(false);
					paneProtocolBoxes.setDisable(true);
				}
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
			
			Stage stage = guiController.getStage();
			
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
				userNotes.removeUserNote(ipAddress); //doesn't do anything if the key didn't exist
			else
				userNotes.addUserNote(ipAddress, newContent);
			
			userNotes.saveUserNotes();
		});

		generatePopupMenus();
	}

	private void initColumnListForTTS()
	{
		chkboxListColumns = new ArrayList<>();
		
		int col = 0, row = 0, amountOfColumns = gridPaneColumnNames.getColumnConstraints().size();
		ObservableList<TableColumn<IPInfoRowModel, ?>> tableColumns = tableResults.getColumns();
		
		if (tableColumns.size() > amountOfColumns * gridPaneColumnNames.getRowConstraints().size())
			throw new IllegalStateException("There are more table columns that cells in the grid pane");
		
		for (TableColumn<IPInfoRowModel, ?> tableColumn : tableColumns)
		{
			String colName = tableColumn.getText();

			comboColumns.getItems().add(colName);

			CheckBox box = new CheckBox(colName);

			gridPaneColumnNames.add(box, col, row);
			
			if ((col % (amountOfColumns - 1) == 0) && col > 0) 
			{
				row++;
				col = 0;
			}
			else
				col++;
			
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
				guiController.getPingToSpeechController().getComboPTSipToPing().getEditor().setText(row.getItem().ipAddressProperty().getValue());
				tabPane.getSelectionModel().select(guiController.getUtilsTab());
			});

			MenuItem pingIP = new MenuItem("Ping this IP");
			pingIP.setOnAction(event -> guiController.pingCommand(row.getItem().ipAddressProperty().getValue()));

			MenuItem traceIP = new MenuItem("Visual trace this IP");
			traceIP.setOnAction(event -> guiController.traceCommand(row.getItem().ipAddressProperty().getValue()));

			ContextMenu rowMenu = new ContextMenu(copyMenu, getGeoIPinfo, sendIPToPTS, pingIP, traceIP);

			// only display context menu for non-null items:
			row.contextMenuProperty().bind(Bindings.when(Bindings.isNotNull(row.itemProperty())).then(rowMenu).otherwise((ContextMenu) null));

			return row;
		});
	}

	private void startButtonPressed()
	{
		StringBuilder errbuf = new StringBuilder();
		String deviceIP = guiController.getSelectedNIC().getIP();
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

				if (chkboxFilterProtocols.isSelected())
				{
					if (chkboxUDP.isSelected())
						protocols.add(NetworkSniffer.stringProtocolToInt("UDP"));
	
					if (chkboxTCP.isSelected())
						protocols.add(NetworkSniffer.stringProtocolToInt("TCP"));
	
					if (chkboxICMP.isSelected())
						protocols.add(NetworkSniffer.stringProtocolToInt("ICMP"));
	
					if (chkboxHTTP.isSelected())
						protocols.add(NetworkSniffer.stringProtocolToInt("HTTP"));
				}

				return protocols;
			}
		};

		if (chkboxTimedCapture.isSelected())
		{
			timerTask = initTimer();
			timer = new Timer(true);
		}

		if (chkboxTimedCapture.isSelected())
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

		for (Node node : gridPaneColumnNames.getChildren())
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

					if (colValue.isEmpty() || (colName.equals(columnNotes.getText()) && colValue.equals(emptyNotesString)))
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
		for (Node node : gridPaneColumnNames.getChildren())
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
			
			notes = userNotes.getUserNote(ip, emptyNotesString);

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
				Integer secondsToCapture = numFieldCaptureTimeout.getValue();
				timer.schedule(timerTask, secondsToCapture * 1000);
				timerExpires = " Timer set to expire at " + LocalDateTime.now().plusSeconds(secondsToCapture).toString().split("T")[1].split("\\.")[0];
			}

			labelStatus.setText(statusCapturing + timerExpires);
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
		numFieldRowsToRead.setText(PropertiesByType.getStringProperty(props, propsNumFieldRowsToRead));

		chkboxFilterResults.setSelected(PropertiesByType.getBoolProperty(props, propsChkboxFilterResults, false));
		textColumnContains.setText(PropertiesByType.getStringProperty(props, propsTextColumnContains, ""));

		String comboValue = PropertiesByType.getStringProperty(props, propsComboColumnsSelection, "");
		if (!comboValue.isEmpty())
			comboColumns.setValue(comboValue);

		for (CheckBox box : chkboxListColumns)
			box.setSelected(PropertiesByType.getBoolProperty(props, propsTTSCheckBox + box.getText(), false));
	}

	private void setCaptureOptionsPane(Properties props)
	{
		chkboxTimedCapture.setSelected(PropertiesByType.getBoolProperty(props, propsChkboxTimedCapture, false));
		numFieldCaptureTimeout.setText(PropertiesByType.getStringProperty(props, propsNumFieldCaptureTimeout));
		chkboxGetLocation.setSelected(PropertiesByType.getBoolProperty(props, propsChkboxGetLocation, false));
		chkboxPing.setSelected(PropertiesByType.getBoolProperty(props, propsChkboxPing, false));
		numFieldPingTimeout.setText(PropertiesByType.getStringProperty(props, propsNumFieldPingTimeout));
	}

	private void setProtocolCheckboxes(Properties props)
	{
		protocolBoxesChecked = 0;

		chkboxFilterProtocols.setSelected(PropertiesByType.getBoolProperty(props, propsChkboxFilterProtocols, false));
		chkboxUDP.setSelected(PropertiesByType.getBoolProperty(props, propsChkboxUDP, false));
		chkboxTCP.setSelected(PropertiesByType.getBoolProperty(props, propsChkboxTCP, false));
		chkboxICMP.setSelected(PropertiesByType.getBoolProperty(props, propsChkboxICMP, false));
		chkboxHTTP.setSelected(PropertiesByType.getBoolProperty(props, propsChkboxHTTP, false));
		
		paneProtocolBoxes.setDisable(!chkboxFilterProtocols.isSelected());
	}

	private void setDisabledPanes()
	{
		numFieldCaptureTimeout.setDisable(!chkboxTimedCapture.isSelected());
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
		
		suggestedPathForCSVFile = PropertiesByType.getStringProperty(props, propsExportCSVPath, System.getProperty("user.dir"));
	}

	public void saveCurrentRunValuesToProperties(Properties props)
	{
		props.put(propsChkboxFilterProtocols, ((Boolean) chkboxFilterProtocols.isSelected()).toString());
		props.put(propsChkboxUDP, ((Boolean) chkboxUDP.isSelected()).toString());
		props.put(propsChkboxTCP, ((Boolean) chkboxTCP.isSelected()).toString());
		props.put(propsChkboxICMP, ((Boolean) chkboxICMP.isSelected()).toString());
		props.put(propsChkboxHTTP, ((Boolean) chkboxHTTP.isSelected()).toString());
		props.put(propsChkboxTimedCapture, ((Boolean) chkboxTimedCapture.isSelected()).toString());
		props.put(propsNumFieldCaptureTimeout, numFieldCaptureTimeout.getText());
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
		
		userNotes.saveUserNotes();
		props.put(propsExportCSVPath, suggestedPathForCSVFile);
	}
}
