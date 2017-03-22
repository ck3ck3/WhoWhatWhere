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
package whowhatwhere.controller.visualtrace;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.PopupWindow.AnchorLocation;
import numbertextfield.NumberTextField;
import whowhatwhere.Main;
import whowhatwhere.controller.GUIController;
import whowhatwhere.controller.ToolTipUtilities;
import whowhatwhere.model.geoipresolver.GeoIPResolver;


public class VisualTraceController implements Initializable
{
	private final static String traceIconLocation = "/buttonGraphics/Globe-Earth.png";
	public final static String infinitySymbol = "\u221E";
	
	@FXML
	private SplitPane splitPane;
	@FXML
	private TextField textTrace;
	@FXML
	private Button btnTrace;
	@FXML
	private ImageView imgView;
	@FXML
	private Label labelLoading;
	@FXML
	private CheckBox chkResolveHostnames;
	@FXML
	private Pane leftPane;
	@FXML
	private Pane rightPane;
	@FXML
	private Label labelPingTimeout;
	@FXML
	private NumberTextField numFieldPingTimeout;
	@FXML
	private TableView<TraceLineInfo> tableTrace;
	@FXML
	private TableColumn<TraceLineInfo, TraceLineInfo> columnMapPin;
	@FXML
	private TableColumn<TraceLineInfo, String> columnHop;
	@FXML
	private TableColumn<TraceLineInfo, String> columnPing;
	@FXML
	private TableColumn<TraceLineInfo, String> columnHostname;
	@FXML
	private TableColumn<TraceLineInfo, String> columnIPAddress;
	@FXML
	private TableColumn<TraceLineInfo, String> columnLocation;
	@FXML
	private TableColumn<TraceLineInfo, String> columnZoomButton;
	@FXML
	private Label labelVisualTrace;
	@FXML
	private NumberTextField numFieldStopTracingAfter;
	@FXML
	private Button btnAbort;
	@FXML
	private Label labelStatus;
	@FXML
	private Pane paneSettings;
	@FXML
	private ProgressIndicator progressIndicator;
	@FXML
	private Label labelConsecutiveTimeouts;


	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		initColumns();
		initWidthAndHeightConstraints();
		setGraphics();
		setTableContextMenu();
		setMapContextMenu();
		
		textTrace.setOnKeyPressed(keyEvent ->
		{
			if (keyEvent.getCode() == KeyCode.ENTER)
				btnTrace.fire();
		});
		
		labelStatus.visibleProperty().bind(btnTrace.disabledProperty());
		progressIndicator.visibleProperty().bind(labelStatus.visibleProperty());
	}
	
	private void initColumns()
	{
		columnHop.setCellValueFactory(new PropertyValueFactory<>(TraceLineInfo.Columns.HOP.getColumnName()));
		columnPing.setCellValueFactory(new PropertyValueFactory<>(TraceLineInfo.Columns.PINGS.getColumnName()));
		columnHostname.setCellValueFactory(new PropertyValueFactory<>(TraceLineInfo.Columns.HOSTNAME.getColumnName()));
		columnIPAddress.setCellValueFactory(new PropertyValueFactory<>(TraceLineInfo.Columns.IP.getColumnName()));
		columnLocation.setCellValueFactory(new PropertyValueFactory<>(TraceLineInfo.Columns.LOCATION.getColumnName()));
		columnZoomButton.setCellValueFactory(new PropertyValueFactory<>(TraceLineInfo.Columns.IP.getColumnName())); //ip is required to create zoom control
		
		setColumnHeaderTooltip(columnMapPin, "Map pin", "Show/hide hops on the map");
		setColumnHeaderTooltip(columnHop, "Hop", "Hops which don't respond to trace requests aren't shown in the table, which may result in hop numbers that aren't sequential. Moreover, when the final destination doesn't respond, or when the trace ends before reaching the destination, the destination is added to the table and marked as " + infinitySymbol + ".");		
		setColumnHeaderTooltip(columnLocation, "Location", "GeoIP isn't always accurate. Right click on any row to see more GeoIP results in your browser");
		setColumnHeaderTooltip(columnZoomButton, "Zoom in", "Toggle the buton to zoom in on the location. Untoggle to see the full route again. Note that this does not give extra accuracy to the location of the IP, it just zooms in to the center of the city.");
	}
	
	private void setColumnHeaderTooltip(TableColumn<TraceLineInfo, ?> column, String headerText, String tooltipText)
	{
		Label label = new Label(headerText);
		GUIController.setCommonGraphicOnLabeled(label, GUIController.CommonGraphicImages.TOOLTIP);
		Tooltip tooltip = new Tooltip(tooltipText);
		ToolTipUtilities.setTooltipProperties(tooltip, true, GUIController.defaultTooltipMaxWidth, GUIController.defaultFontSize, AnchorLocation.CONTENT_TOP_LEFT);
		label.setTooltip(tooltip);
		label.setMaxWidth(Double.MAX_VALUE); //so the entire header width gives the tooltip
		column.setGraphic(label);
		column.setText("");
	}

	private void initWidthAndHeightConstraints()
	{
		imgView.fitWidthProperty().bind(rightPane.widthProperty().subtract(10));
		
		DoubleBinding imgViewHeightAccordingToPaneHeight = rightPane.heightProperty().multiply(1);
		DoubleBinding imgViewHeightAccordingToItsWidth = imgView.fitWidthProperty().multiply((double)VisualTraceUI.googleMapHeight / (double)VisualTraceUI.googleMapWidth);
		imgView.fitHeightProperty().bind(Bindings.min(imgViewHeightAccordingToItsWidth, imgViewHeightAccordingToPaneHeight));
		
		rightPane.widthProperty().addListener((ChangeListener<Number>) (observable, oldValue, newValue) -> 
		{
			labelLoading.autosize();
			labelLoading.setLayoutX(imgView.getFitWidth() / 2 - labelLoading.getWidth() / 2);
		});
		rightPane.heightProperty().addListener((ChangeListener<Number>) (observable, oldValue, newValue) ->
		{
			labelLoading.autosize();
			labelLoading.setLayoutY(imgView.getFitHeight() / 2 - labelLoading.getHeight() / 2);	
		});
		
		splitPane.getDividers().get(0).positionProperty().addListener((ChangeListener<Number>) (observable, oldValue, newValue) ->
		{
			if (btnTrace.getScene() == null) //the stage isn't initialized yet, we don't have real sizes at this point
				return;
			
			double endOfTraceTextfield = paneSettings.getLayoutX() + textTrace.getLayoutX() + textTrace.getWidth() + 10;
			double minX = endOfTraceTextfield / splitPane.getWidth();
			
			if (newValue.doubleValue() * splitPane.getWidth() < endOfTraceTextfield) //don't let the splitter go past the end of the trace button
				splitPane.setDividerPosition(0, minX);
		});

		DoubleBinding maxTableWidth = columnMapPin.widthProperty().add(columnHop.widthProperty().add(columnPing.widthProperty().add(columnIPAddress.widthProperty().
										add(Bindings.when(columnHostname.visibleProperty()).then(columnHostname.widthProperty()).otherwise(0).add(columnLocation.widthProperty().
												add(columnZoomButton.widthProperty().add(15)))))));
		DoubleBinding splitterLocation = leftPane.widthProperty().subtract(30);
		tableTrace.prefWidthProperty().bind(Bindings.min(maxTableWidth, splitterLocation));

		refreshTableWhenNewColumnBecomesVisible();		
		
		DoubleBinding tableHeightAccordingToPaneHeight = leftPane.heightProperty().subtract(tableTrace.layoutYProperty()).subtract(10);
		DoubleBinding tableHeightAccordingToAmountOfRows = tableTrace.fixedCellSizeProperty().multiply(Bindings.size(tableTrace.getItems()).add(2.7));
		tableTrace.prefHeightProperty().bind(Bindings.min(tableHeightAccordingToPaneHeight, tableHeightAccordingToAmountOfRows));
	}

	
	/**
	 * This hack is needed because when the splitter is dragged to the right, more of the table becomes visible but the table doesn't get automatically redrawn.
	 * As a result, the columns in the newly-visible cells remain empty (not drawn) even though they have content.
	 */
	private void refreshTableWhenNewColumnBecomesVisible()
	{
		SimpleDoubleProperty[] columnsStartX = new SimpleDoubleProperty[tableTrace.getColumns().size()];
		ObservableList<TableColumn<TraceLineInfo, ?>> columns = tableTrace.getColumns();
		
		columnsStartX[0] = new SimpleDoubleProperty(0);
		
		for (int i = 1; i < columns.size(); i++)
		{
			columnsStartX[i] = new SimpleDoubleProperty();
			TableColumn<TraceLineInfo, ?> previousColumn = columns.get(i - 1);
			SimpleDoubleProperty previousColumnStartX = columnsStartX[i - 1];
			
			columnsStartX[i].bind(Bindings.when(previousColumn.visibleProperty()).then(previousColumnStartX.add(previousColumn.widthProperty())).otherwise(previousColumnStartX));
		}
	
		tableTrace.prefWidthProperty().addListener((ChangeListener<Number>) (observable, oldValue, newValue) ->
		{
			//if the width is increasing and a new column is now visible
			if (newValue.doubleValue() > oldValue.doubleValue() && isAnyColumnBecameVisible(oldValue.doubleValue(), newValue.doubleValue(), columnsStartX))
				tableTrace.refresh(); //TODO only works well when scroller is at 0 position, otherwise refreshes too late
		});		
	}
	
	private boolean isAnyColumnBecameVisible(double oldWidth, double newWidth, SimpleDoubleProperty[] columnsStartX)
	{
		for (int i = 0; i < columnsStartX.length; i++)
			if (oldWidth <= columnsStartX[i].get() && newWidth >= columnsStartX[i].get()) //if a column starts between the old width and new width (ie it just became visible)
				return true;
		
		return false;
	}

	public void setInitialTableColumnsWidth()
	{
		columnMapPin.setPrefWidth(84);		columnHop.setPrefWidth(55);
		columnPing.setPrefWidth(140);
		columnHostname.setPrefWidth(200);
		columnIPAddress.setPrefWidth(100);
		columnLocation.setPrefWidth(200);
		columnZoomButton.setPrefWidth(100);	
	}
	
	private void setGraphics()
	{
		GUIController.setNumberTextFieldValidationUI(numFieldPingTimeout, numFieldStopTracingAfter);
		
		GUIController.setGraphicForLabeledControl(btnTrace, traceIconLocation, ContentDisplay.LEFT);
		GUIController.setCommonGraphicOnLabeled(btnAbort, GUIController.CommonGraphicImages.CANCEL);
		
		GUIController.setCommonGraphicOnLabeled(labelVisualTrace, GUIController.CommonGraphicImages.TOOLTIP);
		Tooltip visualTraceTooltip = new Tooltip("Trace the route from your computer to another host on the internet and see it visually on a map.");
		ToolTipUtilities.setTooltipProperties(visualTraceTooltip, true, GUIController.defaultTooltipMaxWidth, GUIController.defaultFontSize, null);
		labelVisualTrace.setTooltip(visualTraceTooltip);
		
		GUIController.setCommonGraphicOnLabeled(labelPingTimeout, GUIController.CommonGraphicImages.TOOLTIP);
		Tooltip pingTimeoutTooltip = new Tooltip("The ping timeout (in milliseconds) for each of the 3 pings for every hop. Default value is 3000.");
		ToolTipUtilities.setTooltipProperties(pingTimeoutTooltip, true, GUIController.defaultTooltipMaxWidth, GUIController.defaultFontSize, AnchorLocation.WINDOW_TOP_LEFT); 
		labelPingTimeout.setTooltip(pingTimeoutTooltip);
		
		GUIController.setCommonGraphicOnLabeled(chkResolveHostnames, GUIController.CommonGraphicImages.TOOLTIP);
		Tooltip resolveHostnamesTooltip = new Tooltip("Try to resolve each IP's hostname. This might slow down the trace.");
		ToolTipUtilities.setTooltipProperties(resolveHostnamesTooltip, true, GUIController.defaultTooltipMaxWidth, GUIController.defaultFontSize, AnchorLocation.WINDOW_TOP_LEFT); 
		chkResolveHostnames.setTooltip(resolveHostnamesTooltip);
		
		GUIController.setCommonGraphicOnLabeled(labelConsecutiveTimeouts, GUIController.CommonGraphicImages.TOOLTIP);
		Tooltip timeouts = new Tooltip("A few time-outs in a row usually mean that the final destination was reached but it ignores trace requests. In order to avoid excessive waiting when this happens, it's recommended to set this value to about 5.");
		ToolTipUtilities.setTooltipProperties(timeouts, true, GUIController.defaultTooltipMaxWidth, GUIController.defaultFontSize, AnchorLocation.WINDOW_TOP_LEFT); 
		labelConsecutiveTimeouts.setTooltip(timeouts);
	}
	
	private void setTableContextMenu()
	{
		tableTrace.setRowFactory(param ->
		{
			TableRow<TraceLineInfo> row = new TableRow<TraceLineInfo>();

			MenuItem moreGeoIPInfo = new MenuItem("  Show GeoIP results from multiple sources");
			moreGeoIPInfo.setOnAction(event9 -> Main.openInBrowser(GeoIPResolver.getSecondaryGeoIpPrefix() + row.getItem().ipAddressProperty().get()));
			
			Menu copyMenu = generateCopyMenu(row);
			
			ContextMenu rowMenu = new ContextMenu(moreGeoIPInfo, copyMenu);
			
			row.contextMenuProperty().bind(Bindings.when(Bindings.isNotNull(row.itemProperty())).then(rowMenu).otherwise((ContextMenu) null));
			
			return row;
		});
	}
	
	private Menu generateCopyMenu(TableRow<TraceLineInfo> row)
	{
		Clipboard clipboard = Clipboard.getSystemClipboard();
		ClipboardContent content = new ClipboardContent();
		
		MenuItem copyMapImage = new MenuItem("  Map image");
		copyMapImage.setOnAction(event1 ->
		{
			content.putImage(imgView.getImage());
			clipboard.setContent(content);
		});
		
		MenuItem copyWholeTable = new MenuItem("  Whole table");
		copyWholeTable.setOnAction(event2 ->
		{
			StringBuilder result = new StringBuilder();
			
			for (TraceLineInfo line : tableTrace.getItems())
				result.append(line.toString() + "\n");
			
			content.putString(result.toString().trim());
			clipboard.setContent(content);
		});
		
		MenuItem copyRow = new MenuItem("  Whole row");
		copyRow.setOnAction(event3 ->
		{
			content.putString(row.getItem().toString().trim());
			clipboard.setContent(content);
		});
		
		MenuItem copyPingResults = new MenuItem("  Ping results");
		copyPingResults.setOnAction(event4 ->
		{
			content.putString(row.getItem().pingResultsProperty().get().trim());
			clipboard.setContent(content);
		});
		
		MenuItem copyHostname = new MenuItem("  Hostname");
		copyHostname.setOnAction(event5 ->
		{
			content.putString(row.getItem().hostnameProperty().get().trim());
			clipboard.setContent(content);
		});
		
		MenuItem copyIPAddress = new MenuItem("  IP address");
		copyIPAddress.setOnAction(event6 ->
		{
			content.putString(row.getItem().ipAddressProperty().get().trim());
			clipboard.setContent(content);
		});
		
		MenuItem copyLocation = new MenuItem("  Location");
		copyLocation.setOnAction(event7 ->
		{
			content.putString(row.getItem().locationProperty().get().trim());
			clipboard.setContent(content);
		});				
		
		List<MenuItem> copyMenuItems = Arrays.asList(copyPingResults, copyIPAddress, copyLocation, new SeparatorMenuItem(), copyRow, copyWholeTable, copyMapImage);
		
		Menu copyMenu = new Menu("  Copy", null, (MenuItem[]) copyMenuItems.toArray());
		copyMenu.setOnShowing(event8 ->
		{
			ObservableList<MenuItem> items = copyMenu.getItems();
			if (row.getItem().hostnameProperty().isNotEmpty().get() && !items.contains(copyHostname))
				items.add(1, copyHostname);
		});
		
		return copyMenu;
	}
	
	private void setMapContextMenu()
	{
		imgView.setOnContextMenuRequested(event ->
		{
			MenuItem copyMap = new MenuItem("  Copy map image");
			copyMap.setOnAction(mapEvent ->
			{
				Clipboard clipboard = Clipboard.getSystemClipboard();
				ClipboardContent content = new ClipboardContent();
				
				content.putImage(imgView.getImage());
				clipboard.setContent(content);
			});
			
			ContextMenu menu = new ContextMenu(copyMap);
			menu.show(imgView.getScene().getWindow(), event.getScreenX(), event.getScreenY());
		});
	}
	
	
	public SplitPane getSplitPane()
	{
		return splitPane;
	}

	public TextField getTextTrace()
	{
		return textTrace;
	}

	public Button getBtnTrace()
	{
		return btnTrace;
	}

	public ImageView getImgView()
	{
		return imgView;
	}

	public Label getLoadingLabel()
	{
		return labelLoading;
	}

	public CheckBox getChkResolveHostnames()
	{
		return chkResolveHostnames;
	}
	
	public NumberTextField getNumFieldPingTimeout()
	{
		return numFieldPingTimeout;
	}

	public TableView<TraceLineInfo> getTableTrace()
	{
		return tableTrace;
	}
	
	public TableColumn<TraceLineInfo, TraceLineInfo> getColumnMapPin()
	{
		return columnMapPin;
	}

	public TableColumn<TraceLineInfo, String> getColumnHop()
	{
		return columnHop;
	}

	public TableColumn<TraceLineInfo, String> getColumnPing()
	{
		return columnPing;
	}

	public TableColumn<TraceLineInfo, String> getColumnHostname()
	{
		return columnHostname;
	}

	public TableColumn<TraceLineInfo, String> getColumnIPAddress()
	{
		return columnIPAddress;
	}

	public TableColumn<TraceLineInfo, String> getColumnLocation()
	{
		return columnLocation;
	}

	public TableColumn<TraceLineInfo, String> getColumnZoom()
	{
		return columnZoomButton;
	}

	public NumberTextField getNumFieldStopTracingAfter()
	{
		return numFieldStopTracingAfter;
	}

	public Pane getPaneSettings()
	{
		return paneSettings;
	}
	
	public Label getLabelStatus()
	{
		return labelStatus;
	}

	public Button getBtnAbort()
	{
		return btnAbort;
	}
}
