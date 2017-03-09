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
import java.util.ResourceBundle;

import com.sun.javafx.scene.control.skin.TableViewSkin;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.PopupWindow.AnchorLocation;
import numbertextfield.NumberTextField;
import whowhatwhere.Main;
import whowhatwhere.controller.GUIController;
import whowhatwhere.controller.ToolTipUtilities;
import whowhatwhere.model.geoipresolver.GeoIPResolver;

public class VisualTraceController implements Initializable
{
	private final static String geoIPIconLocation = "/buttonGraphics/earth-16.png";
	
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
	private TableColumn<TraceLineInfo, String> columnCityCountry;
	@FXML
	private TableColumn<TraceLineInfo, String> columnZoomButton;
	@FXML
	private TableColumn<TraceLineInfo, String> columnGeoIPButton;
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
	}
	
	private void initColumns()
	{
		columnHop.setCellValueFactory(new PropertyValueFactory<>(TraceLineInfo.Columns.HOP.getColumnName()));
		columnPing.setCellValueFactory(new PropertyValueFactory<>(TraceLineInfo.Columns.PINGS.getColumnName()));
		columnHostname.setCellValueFactory(new PropertyValueFactory<>(TraceLineInfo.Columns.HOSTNAME.getColumnName()));
		columnIPAddress.setCellValueFactory(new PropertyValueFactory<>(TraceLineInfo.Columns.IP.getColumnName()));
		columnCityCountry.setCellValueFactory(new PropertyValueFactory<>(TraceLineInfo.Columns.LOCATION.getColumnName()));
		
		columnGeoIPButton.setCellFactory(param -> new TableCell<TraceLineInfo, String>()
		{
			@Override
			public void updateItem(String item, boolean empty)
			{
				super.updateItem(item, empty);
			
				if (empty)
				{
					setGraphic(null);
					setText(null);
				}
				else
				{
					Button btn = new Button();
					
					GUIController.setGraphicForLabeledControl(btn, geoIPIconLocation, ContentDisplay.CENTER);
					btn.setOnAction((ActionEvent event) ->
					{
						TraceLineInfo info = getTableView().getItems().get(getIndex());
						Main.openInBrowser(GeoIPResolver.getSecondaryGeoIpPrefix() + info.ipAddressProperty().get());
					});
					
					setGraphic(btn);
					setText(null);
				}
			}
		});
	}
	
	private void initWdithAndHeightConstraints()
	{
		imgView.fitWidthProperty().bind(rightPane.widthProperty().subtract(10));
		
		DoubleBinding imgViewHeightAccordingToPaneHeight = rightPane.heightProperty().multiply(1);
		DoubleBinding imgViewHeightAccordingToItsWidth = imgView.fitWidthProperty().multiply((double)VisualTraceUI.googleMapHeight / (double)VisualTraceUI.googleMapWidth);
		imgView.fitHeightProperty().bind(Bindings.min(imgViewHeightAccordingToItsWidth, imgViewHeightAccordingToPaneHeight));
		
		rightPane.widthProperty().addListener((ChangeListener<Number>) (observable, oldValue, newValue) -> labelLoading.setLayoutX(newValue.doubleValue() / 2 - labelLoading.getWidth() / 2));
		rightPane.heightProperty().addListener((ChangeListener<Number>) (observable, oldValue, newValue) -> labelLoading.setLayoutY(newValue.doubleValue() / 2 - labelLoading.getHeight() / 2));
		
		tableTrace.prefWidthProperty().bind(leftPane.widthProperty().subtract(14));
		
		DoubleBinding tableHeightAccordingToPaneHeight = leftPane.heightProperty().subtract(tableTrace.layoutYProperty());
		DoubleBinding tableHeightAccordingToAmountOfRows = tableTrace.fixedCellSizeProperty().multiply(Bindings.size(tableTrace.getItems()).add(3));
		tableTrace.prefHeightProperty().bind(Bindings.min(tableHeightAccordingToPaneHeight, tableHeightAccordingToAmountOfRows));
	}
	
	private void setTableToAutoFitColumns()
	{
		tableTrace.setSkin(new TableViewSkinWithAutoFitColumns(tableTrace));
		tableTrace.getItems().addListener(new ListChangeListener<TraceLineInfo>()
		{
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends TraceLineInfo> c)
			{
				TableViewSkinWithAutoFitColumns skin = (TableViewSkinWithAutoFitColumns) tableTrace.getSkin();

				for (TableColumn<TraceLineInfo, ?> column : tableTrace.getColumns())
					if (column.isVisible())
						skin.resizeColumnToFitContent(column, -1);
			}
		});		
	}
	
	private void setGraphics()
	{
		GUIController.setCommonGraphicOnLabeled(labelVisualTrace, GUIController.CommonGraphicImages.TOOLTIP);
		GUIController.setNumberTextFieldValidationUI(numFieldPingTimeout);
		GUIController.setCommonGraphicOnLabeled(labelPingTimeout, GUIController.CommonGraphicImages.TOOLTIP);
		
		Tooltip visualTraceTooltip = new Tooltip("Trace the route from your computer to another host on the internet and see it visually on a map."
				+ "\nNote: GeoIP isn't always accurate. Every line in the table has a button that launches more GeoIP results in the browser, for a \"second opinion\"");
		ToolTipUtilities.setTooltipProperties(visualTraceTooltip, true, 470.0, 12.0, null);
		labelVisualTrace.setTooltip(visualTraceTooltip);
		
		Tooltip pingTimeoutTooltip = new Tooltip("The ping timeout (in milliseconds) for each of the 3 pings for every hop.\nEmpty value means default timeout.");
		ToolTipUtilities.setTooltipProperties(pingTimeoutTooltip, true, 400.0, 12.0, AnchorLocation.WINDOW_TOP_LEFT); 
		labelPingTimeout.setTooltip(pingTimeoutTooltip);
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

	public Pane getLeftPane()
	{
		return leftPane;
	}

	public Pane getRightPane()
	{
		return rightPane;
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

	public TableColumn<TraceLineInfo, String> getColumnCityCountry()
	{
		return columnCityCountry;
	}

	public TableColumn<TraceLineInfo, String> getColumnZoom()
	{
		return columnZoomButton;
	}

	public TableColumn<TraceLineInfo, String> getColumnGeoIPInfo()
	{
		return columnGeoIPButton;
	}
}
