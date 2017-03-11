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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.text.Font;
import javafx.util.Callback;
import javafx.util.StringConverter;
import numbertextfield.NumberTextField;
import whowhatwhere.controller.GUIController;
import whowhatwhere.controller.LoadAndSaveSettings;
import whowhatwhere.controller.commands.Commands;
import whowhatwhere.controller.commands.trace.TraceCommandScreen;
import whowhatwhere.model.PropertiesByType;
import whowhatwhere.model.geoipresolver.GeoIPInfo;
import whowhatwhere.model.geoipresolver.GeoIPResolver;

public class VisualTraceUI implements TraceOutputReceiver, LoadAndSaveSettings
{
	private final static Logger logger = Logger.getLogger(VisualTraceUI.class.getPackage().getName());
	
	private final static String propsSplitterPosition = "traceSplitterPosition";
	private final static String propsResolveHostname = "traceResolveHostnames";
	private final static String propsPingTimeout = "tracePingTimeout";
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
	private Button btnTrace;
	private Label labelUnderMap;
	private ImageView imgView;
	private ToggleGroup zoomToggleGroup = new ToggleGroup();
	private TableView<TraceLineInfo> tableTrace;	
	private TableColumn<TraceLineInfo, TraceLineInfo> columnMapPin;
	private TableColumn<TraceLineInfo, String> columnZoomButton;
	private TableColumn<TraceLineInfo, String> columnHostname;

	private List<String> tracertOutput;
	private Map<String, GeoIPInfo> geoIPResults = new HashMap<>();
	private GenerateImageFromURLService imgService;
	private Map<TraceLineInfo, SimpleBooleanProperty> mapRowToSelectedStatus = new HashMap<>();
	private List<TraceLineInfo> listOfRows = new ArrayList<>();
	private boolean noTraceDoneYet = true;


	public VisualTraceUI(GUIController guiController)
	{
		controller = guiController.getVisualTraceController();
		initControls();
		guiController.registerForSettingsHandler(this);
		
		initImgService();
		showOwnLocationOnMap();

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
			
			Commands.traceCommand(guiController.getStage(), textTrace.getText(), chkResolveHostnames.isSelected(), numFieldReplyTimeout.getValue(), this);
		});

		setSpecialColumns();
		labelUnderMap.styleProperty().bind(Bindings.when(labelUnderMap.textProperty().isEqualTo(loadingLabelText)).then(styleForLoadingLabel).otherwise(""));
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
		btnTrace = controller.getBtnTrace();
		imgView = controller.getImgView();
		imgView = controller.getImgView();
		labelUnderMap = controller.getLoadingLabel();
		tableTrace = controller.getTableTrace();
		columnMapPin = controller.getColumnMapPin();
		columnHostname = controller.getColumnHostname();
		columnZoomButton = controller.getColumnZoom();		
	}
	
	private void resetScreen()
	{
		tableTrace.getItems().clear();
		listOfRows.clear();
		mapRowToSelectedStatus.clear();
		
		imgView.setImage(null);
		columnHostname.setVisible(chkResolveHostnames.isSelected());
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
					checkBoxTableCell.visibleProperty().bind(newValue.locationProperty().isEmpty().not());
			});
			
			return checkBoxTableCell;
		});
		
		columnZoomButton.setCellFactory(param -> new TableCell<TraceLineInfo, String>()
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
					TraceLineInfo info = getTableView().getItems().get(getIndex());
					String ip = info.ipAddressProperty().get();
					
					ToggleButton btnZoom = new ToggleButton();
					btnZoom.setToggleGroup(zoomToggleGroup);
					GUIController.setGraphicForLabeledControl(btnZoom, zoomInIconLocation, ContentDisplay.CENTER);
					Tooltip zoomTooltip = new Tooltip("Zoom in on this location (into the center of the city)");
					btnZoom.setFont(new Font(12));
					btnZoom.setTooltip(zoomTooltip);
					btnZoom.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) ->
					{
						HBox hbox = (HBox) btnZoom.getParent();
						Spinner<Integer> spinnerZoom = (Spinner<Integer>) hbox.getChildren().get(1);
						spinnerZoom.setDisable(!newValue);

						if (newValue) //selected
							imgService.setCenterOnIP(ip, spinnerZoom.getValue());

						generateAndShowImage();
					});

					Spinner<Integer> spinnerZoom = new Spinner<>(googleMinZoomLevel, googleMaxZoomLevel, googleDefaultZoomLevel, googleZoomLevelStep);
					spinnerZoom.setPrefWidth(55);
					spinnerZoom.setPrefHeight(btnZoom.getHeight());
					spinnerZoom.valueProperty().addListener((ChangeListener<Integer>) (observable, oldValue, newValue) ->
					{
						imgService.setCenterOnIP(ip, newValue);
						generateAndShowImage();
					});
					Tooltip spinnerTooltip = new Tooltip("Set zoom level (1-20)");
					spinnerTooltip.setFont(new Font(12));
					spinnerZoom.setTooltip(spinnerTooltip);
					spinnerZoom.getEditor().setTooltip(spinnerTooltip);
					spinnerZoom.setEditable(false);
					spinnerZoom.setDisable(true);
					
					HBox zoomControls = new HBox(btnZoom, spinnerZoom);
					zoomControls.setStyle("-fx-alignment: center;");

					setGraphic(zoomControls);
					setText(null);
				}
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
				GeoIPInfo ipInfo = GeoIPResolver.getIPInfo(ip);
				
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
	public void traceFinished(List<String> listOfOutputLines)
	{
		tracertOutput = listOfOutputLines;

		Platform.runLater(() -> init());
	}

	private void init()
	{
		populateGeoIPResults();
		generateTraceInfoGUI();
		generateAndShowImage();
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
		imgService.setOnSucceeded(event ->
		{
			labelUnderMap.setVisible(false);
			Image img = imgService.getValue();
			imgView.setImage(img);

			if (img == null)
			{
				labelUnderMap.setText(noHopsSelectedLabelText);
				placeLabelUnderMap();
				labelUnderMap.setVisible(true);
			}
		});
	}

	private void populateGeoIPResults()
	{
		for (String line : tracertOutput)
		{
			String ip = TraceCommandScreen.extractIPFromLine(line);
			GeoIPInfo ipInfo = GeoIPResolver.getIPInfo(ip);

			if (ipInfo != null)
				geoIPResults.put(ip, ipInfo);
		}
	}

	private void generateTraceInfoGUI()
	{
		char label = 'A';
		for (String line : tracertOutput)
		{
			Map<TraceLineInfo.Columns, String> valueMap = getMapOfValues(line);
			valueMap.put(TraceLineInfo.Columns.LABEL, String.valueOf(label));
			String ip = valueMap.get(TraceLineInfo.Columns.IP);
			GeoIPInfo geoIPInfo = geoIPResults.get(ip);
			boolean hasLocation = geoIPInfo.getSuccess();

			TraceLineInfo currentRow = new TraceLineInfo();
			populateTraceResults(valueMap, currentRow);
			
			
			
//			if (ip.contains("109"))
//			{
//				hasLocation = false;
//				currentRow.setLocation("");
//			}
			
			

			SimpleBooleanProperty checkboxValue = new SimpleBooleanProperty(hasLocation);
			checkboxValue.addListener(new ChangeListener<Boolean>()
			{
				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
				{
					Toggle selectedZoomBtn = zoomToggleGroup.getSelectedToggle();
					if (selectedZoomBtn != null)
						selectedZoomBtn.setSelected(false);

					generateAndShowImage();
				}
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
			labelUnderMap.setVisible(true);
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
		int markersLeftToTurnRed = 2; // the first two markers are the first and last markers

		List<TraceLineInfo> reorderedListOfCheckBoxes = getReorderedListOfCheckboxes();

		for (TraceLineInfo row : reorderedListOfCheckBoxes)
		{
			if (mapRowToSelectedStatus.get(row).get())
			{
				char label = row.getLabel().charAt(0);

				String ip = row.ipAddressProperty().get();
				GeoIPInfo ipInfo = geoIPResults.get(ip);

				String currentMarker = "&markers=color:" + (markersLeftToTurnRed-- > 0 ? "red" : "blue") + "%7Clabel:" + label;

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
			String location = ipInfo.getCountry() + "," + ipInfo.getCity();
			try
			{
				location = URLEncoder.encode(location, "UTF-8");
			}
			catch (UnsupportedEncodingException e)
			{
				logger.log(Level.SEVERE, "Unable to encode this URL: " + location, e);
			}
			result = "&center=" + location + "&zoom=" + zoom;
		}

		return result;
	}

	/**
	 * If two markers are set on the same spot in Google static maps, it will
	 * show the first marker set. In order to show the first and last markers in
	 * the route we need them to be set before other markers. The rest of the
	 * markers should come in reverse order so that the last marker set is
	 * visible (style choice).
	 * 
	 * @return returns a list based on listOfChkBoxes but in a different order:
	 *         first, last, {reverse order of first+1 to last-1}
	 */
	private List<TraceLineInfo> getReorderedListOfCheckboxes()
	{
		List<TraceLineInfo> reorderedListOfCheckBoxes = new ArrayList<>();
		ObservableList<TraceLineInfo> rows = tableTrace.getItems();

		TraceLineInfo row;
		int firstSelectedBox = 0;
		int lastSelectedBox = rows.size() - 1;

		do
		{
			row = rows.get(firstSelectedBox++);
		} while (!mapRowToSelectedStatus.get(row).get() && firstSelectedBox < rows.size());

		reorderedListOfCheckBoxes.add(row);

		do
		{
			row = rows.get(lastSelectedBox--);
		} while (!mapRowToSelectedStatus.get(row).get() && lastSelectedBox >= 0);

		if (!reorderedListOfCheckBoxes.contains(row))
			reorderedListOfCheckBoxes.add(row);
		else
			return reorderedListOfCheckBoxes; //if there's just one box to add, don't add it twice.

		for (int i = lastSelectedBox; i >= firstSelectedBox; i--)
		{
			row = rows.get(i);
			if (mapRowToSelectedStatus.get(row).get())
				reorderedListOfCheckBoxes.add(row);
		}

		return reorderedListOfCheckBoxes;
	}

	private String getLocationString(GeoIPInfo ipInfo)
	{
		String region = ipInfo.getRegion();
		String location = ipInfo.getCountry() + "," + (region.isEmpty() ? "" : ipInfo.getRegionName() + ",") + ipInfo.getCity();
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
	}
	
	public void loadLastRunConfig(Properties props)
	{
		splitPane.setDividerPosition(0, PropertiesByType.getDoubleProperty(props, propsSplitterPosition, 0.49));
		chkResolveHostnames.setSelected(PropertiesByType.getBoolProperty(props, propsResolveHostname, false));
		numFieldReplyTimeout.setText(PropertiesByType.getStringProperty(props, propsPingTimeout, ""));
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
