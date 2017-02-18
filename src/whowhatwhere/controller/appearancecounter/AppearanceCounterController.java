package whowhatwhere.controller.appearancecounter;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import numbertextfield.NumberTextField;

public class AppearanceCounterController
{
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
	private Button btnExportTableToCSV;
	@FXML
	private NumberTextField numFieldCaptureTimeout;
	@FXML
	private NumberTextField numFieldRowsToRead;
	@FXML
	private NumberTextField numFieldPingTimeout;
	@FXML
	private Pane paneProtocolBoxes;
	@FXML
	private Label labelWWW;
	
	
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
	
	public Pane getPaneProtocolBoxes()
	{
		return paneProtocolBoxes;
	}
	
	public Button getBtnExportTableToCSV()
	{
		return btnExportTableToCSV;
	}

	public Label getLabelWWW()
	{
		return labelWWW;
	}
}
