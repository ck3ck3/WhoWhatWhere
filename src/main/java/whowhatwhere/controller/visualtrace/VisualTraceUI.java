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

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Phaser;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import numbertextfield.NumberTextField;
import whowhatwhere.controller.GUIController;
import whowhatwhere.controller.LoadAndSaveSettings;
import whowhatwhere.controller.ToolTipUtilities;
import whowhatwhere.model.PropertiesByType;
import whowhatwhere.model.command.CommmandLiveOutput;
import whowhatwhere.model.command.trace.TraceLiveOutputListener;
import whowhatwhere.model.command.trace.TraceOutputReceiver;
import whowhatwhere.model.geoipresolver.GeoIPInfo;
import whowhatwhere.model.geoipresolver.GeoIPResolver;
import whowhatwhere.model.networksniffer.NetworkSniffer;

public class VisualTraceUI implements TraceOutputReceiver, LoadAndSaveSettings
{
	private final static Logger logger = Logger.getLogger(VisualTraceUI.class.getPackage().getName());
	
	private enum FinishStatus {SUCCESS, ABORT, FAIL}
	
	private final static String propsSplitterPosition = "traceSplitterPosition";
	private final static String propsResolveHostname = "traceResolveHostnames";
	private final static String propsPingTimeout = "tracePingTimeout";
	private final static String propsStopTraceAfterXTimeouts = "stopTraceAfterXTimeouts";
	
	public final static int googleMapWidth = 300;
	public final static int googleMapHeight= 255;
	private final static String baseUrl = "https://maps.googleapis.com/maps/api/staticmap?key=" + GoogleStaticMapsAPIKey.key + "&size=" + googleMapWidth + "x" + googleMapHeight + "&scale=2&maptype=roadmap";
	private final static String zoomInIconLocation = "/buttonGraphics/zoom-16.png";
	private final static int googleMinZoomLevel = 1;
	private final static int googleMaxZoomLevel = 20;
	private final static int googleDefaultZoomLevel = 6;
	private final static int googleZoomLevelStep = 1;
	private final static String loadingLabelText = "Loading...";
	private final static String noHopsSelectedLabelText = "No hops selected";
	private final static String styleForLoadingLabel = "-fx-background-color : #f4f4f4; -fx-border-color: black; -fx-border-width: 3";
	
	private VisualTraceController controller;
	private SplitPane splitPane;
	private TextField textTrace;
	private CheckBox chkResolveHostnames;
	private NumberTextField numFieldReplyTimeout;
	private NumberTextField numFieldStopTracingAfter;
	private Button btnTrace;
	private Label labelUnderMap;
	private ImageView imgView;
	private ToggleGroup zoomToggleGroup = new ToggleGroup();
	private TableView<TraceLineInfo> tableTrace;	
	private TableColumn<TraceLineInfo, TraceLineInfo> columnMapPin;
	private TableColumn<TraceLineInfo, String> columnZoomButton;
	private TableColumn<TraceLineInfo, String> columnHostname;
	private Pane paneSettings;
	private Label labelStatus;
	private Button btnAbort;

	private Map<String, GeoIPInfo> geoIPResults = new HashMap<>();
	private GenerateImageFromURLService imgService;
	private Map<TraceLineInfo, SimpleBooleanProperty> mapRowToSelectedStatus = new HashMap<>();
	private List<TraceLineInfo> listOfRows = new ArrayList<>();
	private boolean noTraceDoneYet = true;

	private ObservableList<String> observableListOfLines = FXCollections.observableArrayList();
	private char label ='A';
	private Semaphore syncMapImageAndTraceTableSemaphore = new Semaphore(1); //will have either 1 or 0 permits. 1 means that a row was added to the table and we're waiting for a map image to appear, which will then set it back to 0.
	private Phaser pendingLinesPhaser = new Phaser(0); //counts the amount of lines that are pending to be shown on the map. will be used to to inform the UI that the trace has finished only after the final trace line was shown on the map.
	private SimpleBooleanProperty isTraceInProgress = new SimpleBooleanProperty(false);
	private String ipBeingTraced = null;
	private String hostnameBeingTraced = null;
	private int consecutiveRequestTimeOuts = 0;
	private SimpleIntegerProperty hopCounter = new SimpleIntegerProperty(0);
	private CommmandLiveOutput traceCommand;
	private String abortReason;
	private boolean wasTraceAborted = false;

	public VisualTraceUI(GUIController guiController)
	{
		controller = guiController.getVisualTraceController();
		initControls();
		guiController.registerForSettingsHandler(this);
		
		initImgService();
		showOwnLocationOnMap();
		
		setBindingsAndListeners();
		setButtonHandlers();
		setSpecialColumns();
	}
	
	private void setBindingsAndListeners()
	{
		paneSettings.disableProperty().bind(isTraceInProgress);
		labelStatus.visibleProperty().bind(isTraceInProgress);
		hopCounter.addListener((ChangeListener<Number>) (observable, oldValue, newValue) -> Platform.runLater(() -> 
		{
			if (isTraceInProgress.get() && newValue.intValue() > 0) //to avoid situation where user presses abort, but then a new hop overrides the 'aborting now' label. Or when hop was reset.
				labelStatus.setText("Trace in progress, current hop: " + newValue.intValue());	
		}));
		labelUnderMap.styleProperty().bind(Bindings.when(labelUnderMap.textProperty().isEqualTo(loadingLabelText)).then(styleForLoadingLabel).otherwise(""));
		
		observableListOfLines.addListener((ListChangeListener<String>) c ->
		{
			c.next();
			
			if (c.wasAdded())
			{
				String newLine = c.getAddedSubList().get(0);
				
				addSingleGeoIPInfoFromLine(newLine);
				addRowToTableAndShowImage(newLine);
			}
		});
	}
	
	private void setButtonHandlers()
	{
		btnTrace.setOnAction(actionEvent ->
		{
			String addressToTrace = textTrace.getText();
							
			if (addressToTrace.isEmpty())
			{
				new Alert(AlertType.ERROR, "Please enter an IP or hostname to trace").showAndWait();
				return;
			}
			
			noTraceDoneYet = false;
			resetScreen();
			btnTrace.setDisable(true);
			btnAbort.setDisable(false);
			
			traceCommand = new CommmandLiveOutput(generateCommandString(), new TraceLiveOutputListener(this));
			traceCommand.runCommand();
		});
		
		btnAbort.setOnAction(actionEVent ->
		{
			Platform.runLater(() ->
			{
				wasTraceAborted = true;
				btnAbort.setDisable(true);
				labelStatus.setText("Aborting trace, this might take a moment...");
				if (abortReason == null)
					abortReason = "User request";
				
				traceCommand.stopCommand();
			});
		});		
	}
	
	private String generateCommandString()
	{
		Integer pingTimeout = numFieldReplyTimeout.getValue();
		
		return "tracert " + (chkResolveHostnames.isSelected() ? "" : "-d ") + (pingTimeout == null ? "" : "-w " + pingTimeout + " ") + textTrace.getText();
	}
	
	public void addRowToTableAndShowImage(String newLine)
	{
		try
		{
			syncMapImageAndTraceTableSemaphore.acquire(); //a trace line is pending to be shown on the map
		}
		catch (InterruptedException ie)
		{
			return;
		}

		Platform.runLater(() ->
		{
			singleGenerateTraceInfoGUI(newLine);				
			generateAndShowImage();
		});
	}
	
	public boolean isTraceInProgress()
	{
		return isTraceInProgress.get();
	}
	
	public void setAddressAndStartTrace(String address)
	{
		textTrace.setText(address);
		btnTrace.fire();
	}
	
	private void initControls()
	{
		splitPane = controller.getSplitPane();
		textTrace = controller.getTextTrace();
		chkResolveHostnames = controller.getChkResolveHostnames();
		numFieldReplyTimeout = controller.getNumFieldPingTimeout();
		numFieldStopTracingAfter = controller.getNumFieldStopTracingAfter();
		btnTrace = controller.getBtnTrace();
		imgView = controller.getImgView();
		labelUnderMap = controller.getLoadingLabel();
		tableTrace = controller.getTableTrace();
		columnMapPin = controller.getColumnMapPin();
		columnHostname = controller.getColumnHostname();
		columnZoomButton = controller.getColumnZoom();		
		paneSettings = controller.getPaneSettings();
		labelStatus = controller.getLabelStatus();
		btnAbort = controller.getBtnAbort();
	}
	
	private void resetScreen()
	{
		tableTrace.getItems().clear();
		tableTrace.refresh();
		listOfRows.clear();
		mapRowToSelectedStatus.clear();
		
		imgView.setImage(null);
		columnHostname.setVisible(chkResolveHostnames.isSelected());
		
		controller.setInitialTableColumnsWidth();
		observableListOfLines.clear();
		label = 'A';
		isTraceInProgress.set(true);
		consecutiveRequestTimeOuts = 0;
		hopCounter.set(0);
		wasTraceAborted = false;
		abortReason = null;
		ipBeingTraced = null;
		hostnameBeingTraced = null;
		labelStatus.setText("Starting trace...");
		
		syncMapImageAndTraceTableSemaphore = new Semaphore(1); //reset back to 1
		pendingLinesPhaser = new Phaser(0); //reset to 0
	}
	
	private void setSpecialColumns()
	{
		columnMapPin.setCellValueFactory(cdf -> new SimpleObjectProperty<TraceLineInfo>(cdf.getValue()));
		
		columnMapPin.setCellFactory(callback ->
		{
			Callback<Integer, ObservableValue<Boolean>> getSelectedProperty = index -> mapRowToSelectedStatus.get(listOfRows.get(index));
			
			StringConverter<TraceLineInfo> stringConverter = new StringConverter<TraceLineInfo>()
			{
				@Override
				public String toString(TraceLineInfo object)
				{
					return object.getLabel();
				}

				@Override
				public TraceLineInfo fromString(String string)
				{
					logger.log(Level.SEVERE, "Trying to convert String to TraceLineInfo in Visual Trace");
					throw new UnsupportedOperationException("Trying to convert String to TraceLineInfo"); //shouldn't happen
				}}; 
			
			CheckBoxTableCell<TraceLineInfo, TraceLineInfo> checkBoxTableCell = new CheckBoxTableCell<>(getSelectedProperty, stringConverter);
			
			checkBoxTableCell.itemProperty().addListener((ChangeListener<TraceLineInfo>) (observable, oldValue, newValue) ->
			{
				if (newValue != null)
				{
					checkBoxTableCell.disableProperty().bind(newValue.locationProperty().isEmpty().or(isTraceInProgress));
					
					//setting the cell as disabled and 0 opacity instead of invisible, since binding visibleProperty() caused setVisible() to be automatically called on that cell later on (which threw an exception)
					DoubleBinding opacityForTempOrPermDisable = Bindings.when(newValue.locationProperty().isEmpty()).then(0).otherwise(0.4);
					checkBoxTableCell.opacityProperty().bind(Bindings.when(checkBoxTableCell.disableProperty()).then(opacityForTempOrPermDisable).otherwise(1.0));
				}
			});
			
			return checkBoxTableCell;
		});
		
		columnZoomButton.setCellFactory(param -> new TableCell<TraceLineInfo, String>()
		{
			@Override
			public void updateItem(String item, boolean empty)
			{
				super.updateItem(item, empty);
			
				if (item == null || empty)
				{
					setGraphic(null);
					setText(null);
				}
				else
				{
					String ip = item;

					ToggleButton btnZoom = createZoomButton(ip);
					Spinner<Integer> spinnerZoom = createZoomSpinner(btnZoom, ip);
					
					HBox zoomControls = new HBox(btnZoom, spinnerZoom);
					zoomControls.setStyle("-fx-alignment: center;");

					setGraphic(zoomControls);
					setText(null);
				}
			}
			
			private ToggleButton createZoomButton(String ip)
			{
				ToggleButton btnZoom = new ToggleButton();
				btnZoom.setToggleGroup(zoomToggleGroup);
				GUIController.setGraphicForLabeledControl(btnZoom, zoomInIconLocation, ContentDisplay.CENTER);
				Tooltip zoomTooltip = new Tooltip("Zoom in on this location (into the center of the city)");
				ToolTipUtilities.setTooltipProperties(zoomTooltip, true, GUIController.defaultTooltipMaxWidth, GUIController.defaultFontSize, null);
				btnZoom.setTooltip(zoomTooltip);
				btnZoom.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) ->
				{
					HBox hbox = (HBox) btnZoom.getParent();
					Spinner<Integer> spinnerZoom = (Spinner<Integer>) hbox.getChildren().get(1);
					spinnerZoom.setDisable(!newValue);

					if (newValue) //selected
						imgService.setCenterOnIP(ip, spinnerZoom.getValue());

					labelUnderMap.setVisible(true);
					generateAndShowImage();
				});
				
				GeoIPInfo geoIPInfo = geoIPResults.get(ip);
				boolean hasGeoIPInfo = geoIPInfo != null && geoIPInfo.getSuccess();
				
				btnZoom.disableProperty().bind(isTraceInProgress.or(new SimpleBooleanProperty(hasGeoIPInfo).not()));
				
				return btnZoom;
			}
			
			private Spinner<Integer> createZoomSpinner(ToggleButton btnZoom, String ip)
			{
				Spinner<Integer> spinnerZoom = new Spinner<>(googleMinZoomLevel, googleMaxZoomLevel, googleDefaultZoomLevel, googleZoomLevelStep);
				spinnerZoom.setPrefWidth(55);
				spinnerZoom.setPrefHeight(btnZoom.getHeight());
				spinnerZoom.valueProperty().addListener((ChangeListener<Integer>) (observable, oldValue, newValue) ->
				{
					imgService.setCenterOnIP(ip, newValue);
					labelUnderMap.setVisible(true);
					generateAndShowImage();
				});
				Tooltip spinnerTooltip = new Tooltip("Set zoom level (1-20)");
				ToolTipUtilities.setTooltipProperties(spinnerTooltip, true, GUIController.defaultTooltipMaxWidth, GUIController.defaultFontSize, null);
				spinnerZoom.setTooltip(spinnerTooltip);
				spinnerZoom.getEditor().setTooltip(spinnerTooltip);
				spinnerZoom.setEditable(false);
				spinnerZoom.setDisable(true);
				
				return spinnerZoom;
			}
		});
	}
	
	private void showOwnLocationOnMap()
	{
		SimpleStringProperty url = new SimpleStringProperty();
		url.addListener((ChangeListener<String>) (observable, oldValue, newValue) ->
		{
			if (newValue != null && noTraceDoneYet)
				imgService.showExistingURL(newValue);
		});
		
		getURLForOwnLocation(url);
	}
	
	private void getURLForOwnLocation(SimpleStringProperty url)
	{
		new Thread(() ->
		{
			String ip = getOwnExternalIP();
			
			if (ip != null)
			{
				GeoIPInfo ipInfo = GeoIPResolver.getIPInfo(ip, false);
				
				if (ipInfo.getSuccess())
				{
					String encodedLocation = getLocationString(ipInfo);
					url.set(baseUrl + "&center=" + encodedLocation + "&zoom=12");
				}
			}
		}).start();
	}
	
	private String getOwnExternalIP()
	{
		String ip = null;
		
		try
		{
			InputStream stream = new URL("https://api.ipify.org").openStream();
			ip = IOUtils.toString(stream);
			IOUtils.closeQuietly(stream);
		}
		catch (IOException ioe)
		{
			logger.log(Level.WARNING, "Can't get own external IP", ioe);
		}
		
		return ip;
	}
	
	@Override
	public void traceError(String errorMessage)
	{
		abortReason = errorMessage;
		isTraceInProgress.set(false);
		informUserTraceFinishedByAbortOrError(FinishStatus.FAIL, errorMessage);
	}
	
	@Override
	public void setIPBeingTraced(String ip)
	{
		ipBeingTraced = ip;
	}
	
	@Override
	public void setHostnameBeingTraced(String hostname)
	{
		hostnameBeingTraced = hostname;
	}
	
	@Override
	public void lineAvailable(String line)
	{
		pendingLinesPhaser.register(); //a line is now pending to be shown on the map
		consecutiveRequestTimeOuts = 0;
		observableListOfLines.add(line);
		hopCounter.set(hopCounter.get() + 1);
	}
	
	@Override
	public void requestTimedOut()
	{
		hopCounter.set(hopCounter.get() + 1);
		
		if (++consecutiveRequestTimeOuts == numFieldStopTracingAfter.getValue())
		{
			abortReason = consecutiveRequestTimeOuts + " consecutive hops haven't responded";
			btnAbort.fire();
		}
	}

	@Override
	public void traceFinished()
	{
		if (isAtLeastOneCheckboxSelected())
		{
			pendingLinesPhaser.register(); //required for next line
			pendingLinesPhaser.arriveAndAwaitAdvance(); //waint until all lines have been drawn on the map
			
			ensureLastHopIsTheDestination();
			
			imgView.imageProperty().addListener(new ChangeListener<Image>() //perform post-trace stuff only after the last image of the trace is shown 
			{
				@Override
				public void changed(ObservableValue<? extends Image> observable, Image oldValue, Image newValue)
				{
					informUserTraceFinishedByAbortOrError(wasTraceAborted ? FinishStatus.ABORT : FinishStatus.SUCCESS);
					
					imgView.imageProperty().removeListener(this);
				}
			});
			
			generateAndShowImage();
		}
		else
			informUserTraceFinishedByAbortOrError(wasTraceAborted ? FinishStatus.ABORT : FinishStatus.SUCCESS);
		
		isTraceInProgress.set(false);
	}
	
	private void informUserTraceFinishedByAbortOrError(FinishStatus status)
	{
		informUserTraceFinishedByAbortOrError(status, null);
	}
	
	private void informUserTraceFinishedByAbortOrError(FinishStatus status, String failMessage)
	{
		Platform.runLater(() ->
		{
			btnTrace.setDisable(false);
			btnAbort.setDisable(true);
			
			Alert alert = null;
			
			switch (status)
			{
				case SUCCESS:
								alert = new Alert(AlertType.INFORMATION, "Trace completed successfully");
								alert.setHeaderText("Trace finished");
								break;
				case ABORT:
								alert = new Alert(AlertType.WARNING, "Trace aborted: " + abortReason);
								alert.setHeaderText("Trace aborted");
								break;
				case FAIL:
								alert = new Alert(AlertType.ERROR, "Trace failed: " + failMessage);
								alert.setHeaderText("Trace failed");
								break;
			}
			
			Stage stage = (Stage) btnTrace.getScene().getWindow();
			alert.initOwner(stage);
			alert.setTitle("Visual trace");
			alert.getDialogPane().setPrefWidth(360);
			
			alert.showAndWait();
		});
	}
	
	private boolean isAtLeastOneCheckboxSelected()
	{
		for (SimpleBooleanProperty selectedStatus : mapRowToSelectedStatus.values())
			if (selectedStatus.get())
				return true;
		
		return false;
	}
	
	/**
	 *Checks if last row has the IP that is being traced. If not, manually creates a line and adds it 
	 */
	private void ensureLastHopIsTheDestination()
	{
		ObservableList<TraceLineInfo> items = tableTrace.getItems();
		if (items.size() == 0)
			return;
		
		int lastLineIndex = items.size() - 1;
		String lastIPFromTraceResults = items.get(lastLineIndex).ipAddressProperty().get();
		if (!lastIPFromTraceResults.equals(ipBeingTraced))
		{
			String[] pingReply = new String[3];
			for (int i = 0; i < 3 ; i++)
			{
				String tempResult = NetworkSniffer.pingAsString(ipBeingTraced, numFieldReplyTimeout.getValue());
				
				pingReply[i] = tempResult.contains("milliseconds") ? tempResult.replace("milliseconds", "ms") : "*   "; 
			}
			
			addSingleGeoIPInfoFromLine(ipBeingTraced);
			
			String manualLine;
			if (hostnameBeingTraced != null)
				manualLine = String.format("%3s  %7s  %7s  %7s  %s [%s]", VisualTraceController.infinitySymbol, pingReply[0], pingReply[1], pingReply[2], hostnameBeingTraced != null ? hostnameBeingTraced : "", ipBeingTraced);
			else
				manualLine = String.format("%3s  %7s  %7s  %7s  %s", VisualTraceController.infinitySymbol, pingReply[0], pingReply[1], pingReply[2], ipBeingTraced);
			
			singleGenerateTraceInfoGUI(manualLine);
		}		
	}

	private void placeLabelUnderMap()
	{
		labelUnderMap.autosize();
		labelUnderMap.setLayoutX(imgView.getFitWidth() / 2 - labelUnderMap.getWidth() / 2);
		labelUnderMap.setLayoutY(imgView.getFitHeight() / 2 - labelUnderMap.getHeight() / 2);
	}

	private void initImgService()
	{
		imgService = new GenerateImageFromURLService(this);
		imgService.setOnSucceeded(event -> showImage());
	}
	
	private void showImage()
	{
		labelUnderMap.setVisible(false);
		Image img = imgService.getValue();
		imgView.setImage(img);
			
		if (img == null && !isTraceInProgress.get())
		{
			labelUnderMap.setText(noHopsSelectedLabelText);
			placeLabelUnderMap();
			labelUnderMap.setVisible(true);
		}
		
		if (syncMapImageAndTraceTableSemaphore.availablePermits() == 0)
			syncMapImageAndTraceTableSemaphore.release(); //if we were waiting for a line to appear on the map, it's done
		
		if (pendingLinesPhaser.getRegisteredParties() > 0)
			pendingLinesPhaser.arriveAndDeregister(); //if we were waiting for a line to appear on the map, it's done
	}

	private void addSingleGeoIPInfoFromLine(String line)
	{
		String ip = TraceLiveOutputListener.extractIPFromLine(line);
		GeoIPInfo ipInfo = GeoIPResolver.getIPInfo(ip, false);

		if (ipInfo != null)
			geoIPResults.put(ip, ipInfo);		
	}
	
	private void singleGenerateTraceInfoGUI(String line)
	{
		Map<TraceLineInfo.Columns, String> valueMap = getMapOfValues(line);
		String ip = valueMap.get(TraceLineInfo.Columns.IP);
		GeoIPInfo geoIPInfo = geoIPResults.get(ip);
		boolean hasLocation = geoIPInfo.getSuccess();

		valueMap.put(TraceLineInfo.Columns.LABEL, hasLocation ? String.valueOf(label) : "");
		
		TraceLineInfo currentRow = new TraceLineInfo();
		populateTraceResults(valueMap, currentRow);
		
		SimpleBooleanProperty checkboxValue = new SimpleBooleanProperty(hasLocation);
		checkboxValue.addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) ->
		{
			Toggle selectedZoomBtn = zoomToggleGroup.getSelectedToggle();
			if (selectedZoomBtn != null)
				selectedZoomBtn.setSelected(false);
			
			labelUnderMap.setVisible(true);
			generateAndShowImage();
		});
		
		listOfRows.add(currentRow);
		mapRowToSelectedStatus.put(currentRow, checkboxValue);

		tableTrace.getItems().add(currentRow);

		if (hasLocation) //otherwise the label won't be used, so don't advance it
		{
			if (label != 'Z')
				label++; //trace is limited to 30 hops, so no need to worry about '9'
			else
				label = '0';
		}
	}
	
	private void populateTraceResults(Map<TraceLineInfo.Columns, String> valueMap, TraceLineInfo lineInfo)
	{
		for (TraceLineInfo.Columns key : TraceLineInfo.Columns.values())
		{
			String value = valueMap.get(key);
			lineInfo.setColumnValue(key, value != null ? value : "");
		}
	}

	/**
	 * @param fullLine
	 *            - full line from tracert output
	 * @return - a map of values with these keys: propertyHop, propertyPings,
	 *         propertyHostname (if available), propertyIP
	 */
	private Map<TraceLineInfo.Columns, String> getMapOfValues(String fullLine)
	{
		List<String> values = new ArrayList<>();
		boolean containsHostname = false;

		String[] splitBySpace = fullLine.trim().split(" ");
		for (String str : splitBySpace)
		{
			if (!str.isEmpty())
			{
				if (str.equals("ms"))
				{
					int lastIndex = values.size() - 1;
					values.set(lastIndex, values.get(lastIndex) + " ms");
				}
				else
				{
					if (str.contains("["))
					{
						values.add(str.substring(1, str.length() - 1));
						containsHostname = true;
					}
					else
						values.add(str);
				}
			}
		}

		String pings = values.get(1) + "  " + values.get(2) + "  " + values.get(3) + "  ";
		values.set(1, pings);
		values.remove(3); //remove in descending order to prevent lower indices from changing
		values.remove(2);

		Map<TraceLineInfo.Columns, String> mappedResults = new HashMap<>();
		int index = 0;

		mappedResults.put(TraceLineInfo.Columns.HOP, values.get(index++));
		mappedResults.put(TraceLineInfo.Columns.PINGS, values.get(index++));
		
		if (containsHostname)
			mappedResults.put(TraceLineInfo.Columns.HOSTNAME, values.get(index++));
		
		String ip = values.get(index++);
		mappedResults.put(TraceLineInfo.Columns.IP, ip);
		
		GeoIPInfo geoIPInfo = geoIPResults.get(ip);
		String location;
		if (geoIPInfo.getSuccess())
		{
			String region = geoIPInfo.getRegion();
			location = geoIPInfo.getSuccess() ? geoIPInfo.getCity() + (region.isEmpty() ? "" : ", " + region) + ", " + geoIPInfo.getCountry() : "";
			
		}
		else
			location = "";

		mappedResults.put(TraceLineInfo.Columns.LOCATION, location);

		return mappedResults;
	}

	private void generateAndShowImage()
	{
		Platform.runLater(() ->
		{
			labelUnderMap.setText(loadingLabelText);
			imgService.restart();
		});
	}

	private String generateURL(String centerOnIP, int zoom)
	{
		String markersSegment = generateMarkers();
		String pathSegment = generatePath();
		String zoomSegment = generateZoom(centerOnIP, zoom);

		return baseUrl + markersSegment + pathSegment + zoomSegment;
	}

	private String generateMarkers()
	{
		String markers = "";

		List<TraceLineInfo> uniqueHops = removeOverlappingHops();

		for (TraceLineInfo row : uniqueHops)
		{
			if (mapRowToSelectedStatus.get(row).get()) //if the row is selected
			{
				char label = row.getLabel().charAt(0);

				String ip = row.ipAddressProperty().get();
				GeoIPInfo ipInfo = geoIPResults.get(ip);

				int indexOfRow = uniqueHops.indexOf(row);
				boolean isFirstOrLastHop = indexOfRow == 0 || (!isTraceInProgress.get() && indexOfRow == uniqueHops.size() - 1); 
				String currentMarker = "&markers=color:" + (isFirstOrLastHop ? "red" : "blue") + "%7Clabel:" + label;
				
				if (!isFirstOrLastHop) //only the first and last hop are slightly bigger than the other hops, to stick out in the image. 
					currentMarker += "%7Csize:mid";

				String location = getLocationString(ipInfo);
				markers += currentMarker + "%7C" + location;
			}
		}

		return markers;
	}

	private String generatePath()
	{
		String pathInit = "&path=";
		String path = "";
		ObservableList<TraceLineInfo> rows = tableTrace.getItems();

		for (int i = 0; i < rows.size(); i++)
		{
			TraceLineInfo row = rows.get(i);

			if (!mapRowToSelectedStatus.get(row).get())
				continue;

			String ipForPath = row.ipAddressProperty().get();
			String locationForPath = getLocationString(geoIPResults.get(ipForPath));

			if (!path.isEmpty()) //if it's not the first part of the path
				locationForPath = "%7C" + locationForPath;

			path += locationForPath;
		}

		return (path.isEmpty() ? "" : pathInit + path);
	}

	private String generateZoom(String centerOnIP, int zoom)
	{
		String result = "";

		if (centerOnIP != null)
		{
			GeoIPInfo ipInfo = geoIPResults.get(centerOnIP);
			String location = getLocationString(ipInfo);
			
			result = "&center=" + location + "&zoom=" + zoom;
		}

		return result;
	}

	/**
	 * @return returns a list based on tableTrace.getItems() but without duplicate locations.
	 * The first hop is guaranteed to be included, then for every location, only the last hop from that location will be included.
	 */
	private List<TraceLineInfo> removeOverlappingHops()
	{
		List<TraceLineInfo> hopsToShow = new ArrayList<>();
		ObservableList<TraceLineInfo> rows = tableTrace.getItems();
		Map<String, String> mapLocationToLastHopFromIt = new HashMap<>();
		boolean isFirstCheckedRow = true;
		String firstLocation = null;
		
		for(TraceLineInfo hop : rows)
		{
			if (mapRowToSelectedStatus.get(hop).get()) //if row is checked
			{
				String locationString = hop.locationProperty().get();
				
				if (isFirstCheckedRow)
				{
					isFirstCheckedRow = false;
					firstLocation = locationString;
				}
				else
					if (locationString.equals(firstLocation)) //don't overwrite the first hop
						continue;
				
				mapLocationToLastHopFromIt.put(locationString, hop.hopNumberProperty().get());
			}
		}
		
		for(TraceLineInfo hop : rows)
		{
			if (mapRowToSelectedStatus.get(hop).get()) //if row is checked
			{
				String currentHopNumber = hop.hopNumberProperty().get();
				String locationString = hop.locationProperty().get();
				String lastHopFromLocation = mapLocationToLastHopFromIt.get(locationString);
				
				if (currentHopNumber.equals(lastHopFromLocation))
					hopsToShow.add(hop);
			}
		}
		
		return hopsToShow;
	}

	private String getLocationString(GeoIPInfo ipInfo)
	{
		String region = ipInfo.getRegion();
		String location = ipInfo.getCity() + "," + (region.isEmpty() ? "" : ipInfo.getRegionName() + ",") + ipInfo.getCountry();
		try
		{
			location = URLEncoder.encode(location, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			logger.log(Level.SEVERE, "Unable to encode this URL: " + location, e);
		}

		return location;
	}
	
	public void saveCurrentRunValuesToProperties(Properties props)
	{
		props.put(propsSplitterPosition, String.valueOf(splitPane.getDividerPositions()[0]));
		props.put(propsResolveHostname, String.valueOf(chkResolveHostnames.isSelected()));
		props.put(propsPingTimeout, numFieldReplyTimeout.getText());
		props.put(propsStopTraceAfterXTimeouts, numFieldStopTracingAfter.getText());
	}
	
	public void loadLastRunConfig(Properties props)
	{
		splitPane.setDividerPosition(0, PropertiesByType.getDoubleProperty(props, propsSplitterPosition, 0.49));
		chkResolveHostnames.setSelected(PropertiesByType.getBoolProperty(props, propsResolveHostname, false));
		numFieldReplyTimeout.setText(PropertiesByType.getStringProperty(props, propsPingTimeout, "3000"));
		numFieldStopTracingAfter.setText(PropertiesByType.getStringProperty(props, propsStopTraceAfterXTimeouts, "5"));
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	

	private static class GenerateImageFromURLService extends Service<Image>
	{
		private VisualTraceUI traceScreen;
		private String centerOnIP; //IP location to center on, only relevant ONCE for the next use. Ignored when null.
		private Map<String, Image> urlToImageCache = new HashMap<>();
		private int zoom;
		private String existingURL;

		public GenerateImageFromURLService(VisualTraceUI traceScreen)
		{
			this.traceScreen = traceScreen;
			this.centerOnIP = null;
			this.existingURL = null;
		}

		@Override
		protected Task<Image> createTask()
		{
			return new Task<Image>()
			{
				@Override
				protected Image call() throws Exception
				{
					String url;
					
					if (existingURL == null)
					{
						url = traceScreen.generateURL(centerOnIP, zoom);
	
						if (url.equals(baseUrl))
							return null;
	
						centerOnIP = null; //reset for next use
					}
					else
					{
						url = existingURL;
						existingURL = null; //reset for next use
					}
					
					Image img = urlToImageCache.get(url);

					if (img == null)
					{
						img = new Image(url);
						img = cropTransparency(img); //in some cases the image comes with transparent top/bottom
						urlToImageCache.put(url, img);
					}

					return img;
				}
			};
		}

		public void setCenterOnIP(String centerOnIP, int zoom)
		{
			this.centerOnIP = centerOnIP;
			this.zoom = zoom;
		}
		
		public void showExistingURL(String url)
		{
			existingURL = url;
			start();
		}

		private static Image cropTransparency(Image image)
		{
			BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
			BufferedImage croppedImage = trimImage(bufferedImage);
			return SwingFXUtils.toFXImage(croppedImage, null);
		}

		//code taken from http://stackoverflow.com/a/36938923
		//only checking top and bottom, sides won't be transparent
		//only checking first pixel in each line, since the entire line would be either transparent or not
		private static BufferedImage trimImage(BufferedImage image)
		{
			WritableRaster raster = image.getAlphaRaster();

			if (raster == null) //no transparency in this image
				return image;

			int width = raster.getWidth();
			int height = raster.getHeight();
			int top = 0;
			int bottom = height - 1;

			for (; top < bottom; top++)
				if (raster.getSample(0, top, 0) != 0)
					break;

			for (; bottom > top; bottom--)
				if (raster.getSample(0, bottom, 0) != 0)
					break;

			return image.getSubimage(0, top, width, bottom - top + 1);
		}
	}
}
