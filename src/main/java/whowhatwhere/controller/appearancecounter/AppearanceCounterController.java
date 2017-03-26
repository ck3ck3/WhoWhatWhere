/*******************************************************************************
 * Who What Where
 * Copyright (C) 2017  ck3ck3
 * https://github.com/ck3ck3/WhoWhatWhere
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package whowhatwhere.controller.appearancecounter;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.PopupWindow.AnchorLocation;
import numbertextfield.NumberTextField;
import whowhatwhere.Main;
import whowhatwhere.controller.GUIController;
import whowhatwhere.controller.ToolTipUtilities;

public class AppearanceCounterController implements Initializable
{
	private final static String startWWWImageLocation = "/buttonGraphics/startWWWIcon.png";
	private final static String exportToCSVImageLocation = "/buttonGraphics/Export-CSV.png";
	
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
	@FXML
	private ScrollPane scrollPane;
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		setTooltipsAndGraphics();
		
		//show scrollbars only when needed. When not needed, allow to stretch GUI (AnchorPane behavior)
		scrollPane.fitToWidthProperty().bind(scrollPane.widthProperty().greaterThan(scrollPane.getPrefWidth()));
		scrollPane.fitToHeightProperty().bind(scrollPane.heightProperty().greaterThan(scrollPane.getPrefHeight()));
	}
	
	private void setTooltipsAndGraphics()
	{
		Tooltip wwwTooltip = new Tooltip("Who What Where listens to network traffic and analyzes IP packets. Analysis includes geographical location, latency and total amount of packets sent and received from each address.");
		ToolTipUtilities.setTooltipProperties(wwwTooltip, true, GUIController.defaultTooltipMaxWidth, GUIController.defaultFontSize, null);
		labelWWW.setTooltip(wwwTooltip);
		GUIController.setCommonGraphicOnLabeled(labelWWW, GUIController.CommonGraphicImages.TOOLTIP);
		
		Tooltip hotkeyTooltip = new Tooltip("The hotkey can be activated even while " + Main.getAppName() + " isn't visible on the screen. "
				+ "The table contents will be read out to you so you don't have to look at the screen. The text to speech voice can be configured from the Options menu.");
		ToolTipUtilities.setTooltipProperties(hotkeyTooltip, true, GUIController.defaultTooltipMaxWidth, GUIController.defaultFontSize, AnchorLocation.WINDOW_TOP_RIGHT); 
		chkboxUseTTS.setTooltip(hotkeyTooltip);
		GUIController.setCommonGraphicOnLabeled(chkboxUseTTS, GUIController.CommonGraphicImages.TOOLTIP);
		
		Tooltip geoIPTooltip = new Tooltip("For each IP address, gets the name of the organization that owns it and its location (country, region and city). GeoIP info isn't always accurate. Right click on any row to see more GeoIP results in the browser.");
		ToolTipUtilities.setTooltipProperties(geoIPTooltip, true, GUIController.defaultTooltipMaxWidth, GUIController.defaultFontSize, null);
		chkboxGetLocation.setTooltip(geoIPTooltip);		
		GUIController.setCommonGraphicOnLabeled(chkboxGetLocation, GUIController.CommonGraphicImages.TOOLTIP);
		
		GUIController.setCommonGraphicOnLabeled(btnConfigCaptureHotkey, GUIController.CommonGraphicImages.HOTKEY);
		GUIController.setGraphicForLabeledControl(btnStart, startWWWImageLocation, ContentDisplay.LEFT);
		GUIController.setCommonGraphicOnLabeled(btnStop, GUIController.CommonGraphicImages.STOP);
		GUIController.setGraphicForLabeledControl(btnExportTableToCSV, exportToCSVImageLocation, ContentDisplay.LEFT);		
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
}
