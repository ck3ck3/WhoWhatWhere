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

import com.sun.javafx.scene.control.skin.TableViewSkin;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
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
import javafx.scene.text.Font;
import javafx.stage.PopupWindow.AnchorLocation;
import javafx.util.Callback;
import numbertextfield.NumberTextField;
import whowhatwhere.Main;
import whowhatwhere.controller.GUIController;
import whowhatwhere.controller.ToolTipUtilities;
import whowhatwhere.model.geoipresolver.GeoIPResolver;

public class VisualTraceController implements Initializable
{
	private final static String traceIconLocation = "/buttonGraphics/Globe-Earth.png";
	
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

	/**
	 * Exposes an originally protected method to auto-resize columns to fit to
	 * width.<br>
	 * Will not be needed once this is solved:
	 * {@link https://bugs.openjdk.java.net/browse/JDK-8092235}
	 */
	private class TableViewSkinWithAutoFitColumns extends TableViewSkin<TraceLineInfo>
	{
		public TableViewSkinWithAutoFitColumns(TableView<TraceLineInfo> tableView)
		{
			super(tableView);
		}

		@Override
		public void resizeColumnToFitContent(TableColumn<TraceLineInfo, ?> tc, int maxRows)
		{
			super.resizeColumnToFitContent(tc, maxRows);
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		initColumns();
		initWdithAndHeightConstraints();
		setTableToAutoFitColumns();
		setGraphics();
		setTableContextMenu();
		setMapContextMenu();
		
		textTrace.setOnKeyPressed(keyEvent ->
		{
			if (keyEvent.getCode() == KeyCode.ENTER)
				btnTrace.fire();
		});
	}
	
	private void initColumns()
	{
		setColumnHeadersTooltips();
		
		columnHop.setCellValueFactory(new PropertyValueFactory<>(TraceLineInfo.Columns.HOP.getColumnName()));
		columnPing.setCellValueFactory(new PropertyValueFactory<>(TraceLineInfo.Columns.PINGS.getColumnName()));
		columnHostname.setCellValueFactory(new PropertyValueFactory<>(TraceLineInfo.Columns.HOSTNAME.getColumnName()));
		columnIPAddress.setCellValueFactory(new PropertyValueFactory<>(TraceLineInfo.Columns.IP.getColumnName()));
		columnLocation.setCellValueFactory(new PropertyValueFactory<>(TraceLineInfo.Columns.LOCATION.getColumnName()));
	}
	
	private void setColumnHeadersTooltips()
	{
		Label labelForMapPin = new Label("Map pin");
		GUIController.setCommonGraphicOnLabeled(labelForMapPin, GUIController.CommonGraphicImages.TOOLTIP);
		Tooltip tooltipForMapPin = new Tooltip("Show/hide hops on the map");
		tooltipForMapPin.setFont(new Font(12));
		labelForMapPin.setTooltip(tooltipForMapPin);
		labelForMapPin.setMaxWidth(Double.MAX_VALUE); //so the entire header width gives the tooltip
		columnMapPin.setGraphic(labelForMapPin);
		columnMapPin.setText("");
		
		Label labelForLocation = new Label("Location");
		GUIController.setCommonGraphicOnLabeled(labelForLocation, GUIController.CommonGraphicImages.TOOLTIP);
		Tooltip tooltipForLocation = new Tooltip("GeoIP isn't always accurate. Right click on any row to see more GeoIP results in your browser");
		ToolTipUtilities.setTooltipProperties(tooltipForLocation, true, 400.0, 12.0, AnchorLocation.CONTENT_TOP_LEFT);
		labelForLocation.setTooltip(tooltipForLocation);
		labelForLocation.setMaxWidth(Double.MAX_VALUE); //so the entire header width gives the tooltip
		columnLocation.setGraphic(labelForLocation);
		columnLocation.setText("");
		
		Label labelForZoom = new Label("Zoom in");
		GUIController.setCommonGraphicOnLabeled(labelForZoom, GUIController.CommonGraphicImages.TOOLTIP);
		Tooltip tooltipForZoom = new Tooltip("Toggle the buton to zoom in on the location. Untoggle to see the full route again. Note that this does not give extra accuracy to the location of the IP, it just zooms in to the center of the city.");
		ToolTipUtilities.setTooltipProperties(tooltipForZoom, true, 400.0, 12.0, AnchorLocation.CONTENT_TOP_LEFT);
		labelForZoom.setTooltip(tooltipForZoom);
		labelForZoom.setMaxWidth(Double.MAX_VALUE); //so the entire header width gives the tooltip
		columnZoomButton.setGraphic(labelForZoom);
		columnZoomButton.setText("");
	}

	private void initWdithAndHeightConstraints()
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
			
			double endOfTraceBtn = btnTrace.getLayoutX() + btnTrace.getWidth();
			double minX = endOfTraceBtn / splitPane.getWidth();
			
			if (newValue.doubleValue() * splitPane.getWidth() < endOfTraceBtn) //don't let the splitter go past the end of the trace button
				splitPane.setDividerPosition(0, minX);
		});

		DoubleBinding maxTableWidth = columnMapPin.widthProperty().add(columnHop.widthProperty().add(columnPing.widthProperty().add(columnIPAddress.widthProperty().
										add(Bindings.when(columnHostname.visibleProperty()).then(columnHostname.widthProperty()).otherwise(0).add(columnLocation.widthProperty().
												add(columnZoomButton.widthProperty().add(10)))))));
		DoubleBinding splitterLocation = leftPane.widthProperty().subtract(14);
		tableTrace.prefWidthProperty().bind(Bindings.min(maxTableWidth, splitterLocation));
		
		DoubleBinding tableHeightAccordingToPaneHeight = leftPane.heightProperty().subtract(tableTrace.layoutYProperty());
		DoubleBinding tableHeightAccordingToAmountOfRows = tableTrace.fixedCellSizeProperty().multiply(Bindings.size(tableTrace.getItems()).add(3));
		tableTrace.prefHeightProperty().bind(Bindings.min(tableHeightAccordingToPaneHeight, tableHeightAccordingToAmountOfRows));
	}
	
	private void setTableToAutoFitColumns()
	{
		tableTrace.setSkin(new TableViewSkinWithAutoFitColumns(tableTrace));
		tableTrace.getItems().addListener((ListChangeListener<TraceLineInfo>) c ->
		{
			TableViewSkinWithAutoFitColumns skin = (TableViewSkinWithAutoFitColumns) tableTrace.getSkin();

			for (TableColumn<TraceLineInfo, ?> column : tableTrace.getColumns())
				if (column.isVisible())
					skin.resizeColumnToFitContent(column, -1);
		});		
	}
	
	private void setGraphics()
	{
		GUIController.setGraphicForLabeledControl(btnTrace, traceIconLocation, ContentDisplay.LEFT);
		GUIController.setNumberTextFieldValidationUI(numFieldPingTimeout);
		
		GUIController.setCommonGraphicOnLabeled(labelVisualTrace, GUIController.CommonGraphicImages.TOOLTIP);
		Tooltip visualTraceTooltip = new Tooltip("Trace the route from your computer to another host on the internet and see it visually on a map.");
		ToolTipUtilities.setTooltipProperties(visualTraceTooltip, true, 470.0, 12.0, null);
		labelVisualTrace.setTooltip(visualTraceTooltip);
		
		GUIController.setCommonGraphicOnLabeled(labelPingTimeout, GUIController.CommonGraphicImages.TOOLTIP);
		Tooltip pingTimeoutTooltip = new Tooltip("The ping timeout (in milliseconds) for each of the 3 pings for every hop.\nDefault value is 3000.");
		ToolTipUtilities.setTooltipProperties(pingTimeoutTooltip, true, 400.0, 12.0, AnchorLocation.WINDOW_TOP_LEFT); 
		labelPingTimeout.setTooltip(pingTimeoutTooltip);
		
		GUIController.setCommonGraphicOnLabeled(chkResolveHostnames, GUIController.CommonGraphicImages.TOOLTIP);
		Tooltip resolveHostnamesTooltip = new Tooltip("Tries to resolve each IP's hostname, but might take longer for the trace to finish or to be canceled.");
		ToolTipUtilities.setTooltipProperties(resolveHostnamesTooltip, true, 400.0, 12.0, AnchorLocation.WINDOW_TOP_LEFT); 
		chkResolveHostnames.setTooltip(resolveHostnamesTooltip);
	}
	
	private void setTableContextMenu()
	{
		tableTrace.setRowFactory(new Callback<TableView<TraceLineInfo>, TableRow<TraceLineInfo>>()
		{
			@Override
			public TableRow<TraceLineInfo> call(TableView<TraceLineInfo> param)
			{
				TableRow<TraceLineInfo> row = new TableRow<TraceLineInfo>();
				Clipboard clipboard = Clipboard.getSystemClipboard();
				ClipboardContent content = new ClipboardContent();
				
				MenuItem copyMapImage = new MenuItem("  Map image");
				copyMapImage.setOnAction(event ->
				{
					content.putImage(imgView.getImage());
					clipboard.setContent(content);
				});
				
				MenuItem copyWholeTable = new MenuItem("  Whole table");
				copyWholeTable.setOnAction(event ->
				{
					StringBuilder result = new StringBuilder();
					
					for (TraceLineInfo line : tableTrace.getItems())
						result.append(line.toString() + "\n");
					
					content.putString(result.toString().trim());
					clipboard.setContent(content);
				});
				
				MenuItem copyRow = new MenuItem("  Whole row");
				copyRow.setOnAction(event ->
				{
					content.putString(row.getItem().toString().trim());
					clipboard.setContent(content);
				});
				
				MenuItem copyPingResults = new MenuItem("  Ping results");
				copyPingResults.setOnAction(event ->
				{
					content.putString(row.getItem().pingResultsProperty().get().trim());
					clipboard.setContent(content);
				});
				
				MenuItem copyHostname = new MenuItem("  Hostname");
				copyHostname.setOnAction(event ->
				{
					content.putString(row.getItem().hostnameProperty().get().trim());
					clipboard.setContent(content);
				});
				
				MenuItem copyIPAddress = new MenuItem("  IP address");
				copyIPAddress.setOnAction(event ->
				{
					content.putString(row.getItem().ipAddressProperty().get().trim());
					clipboard.setContent(content);
				});
				
				MenuItem copyLocation = new MenuItem("  Location");
				copyLocation.setOnAction(event ->
				{
					content.putString(row.getItem().locationProperty().get().trim());
					clipboard.setContent(content);
				});				
				
				List<MenuItem> copyMenuItems = Arrays.asList(copyPingResults, copyIPAddress, copyLocation, new SeparatorMenuItem(), copyRow, copyWholeTable, copyMapImage);
				
				Menu copyMenu = new Menu("  Copy", null, (MenuItem[]) copyMenuItems.toArray());
				copyMenu.setOnShowing(event ->
				{
					ObservableList<MenuItem> items = copyMenu.getItems();
					if (row.getItem().hostnameProperty().isNotEmpty().get() && !items.contains(copyHostname))
						items.add(1, copyHostname);
				});

				MenuItem moreGeoIPInfo = new MenuItem("  Show GeoIP results from multiple sources");
				moreGeoIPInfo.setOnAction(event -> Main.openInBrowser(GeoIPResolver.getSecondaryGeoIpPrefix() + row.getItem().ipAddressProperty().get()));
				
				ContextMenu rowMenu = new ContextMenu(moreGeoIPInfo, copyMenu);
				
				row.contextMenuProperty().bind(Bindings.when(Bindings.isNotNull(row.itemProperty())).then(rowMenu).otherwise((ContextMenu) null));
				
				return row;
			}});
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
}
