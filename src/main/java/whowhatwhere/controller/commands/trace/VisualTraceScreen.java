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
package whowhatwhere.controller.commands.trace;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.HPos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import whowhatwhere.Main;
import whowhatwhere.model.geoipresolver.GeoIPInfo;
import whowhatwhere.model.geoipresolver.GeoIPResolver;
import whowhatwhere.view.secondaryfxmlscreen.SecondaryFXMLScreen;

public class VisualTraceScreen extends SecondaryFXMLScreen
{
	private final static String visualTraceFormLocation = "/whowhatwhere/view/fxmls/commands/VisualTraceForm.fxml";
	private final static String baseUrl = "https://maps.googleapis.com/maps/api/staticmap?key=" + GoogleStaticMapsAPIKey.key + "&size=400x340&scale=2&maptype=roadmap";
	private final static String geoIPIconLocation = "/buttonGraphics/earth-16.png";
	private final static String zoomInIconLocation = "/buttonGraphics/zoom-16.png";
	private final static int googleMinZoomLevel = 1;
	private final static int googleMaxZoomLevel = 20;
	private final static int googleDefaultZoomLevel = 6;
	private final static int googleZoomLevelStep = 1;
	private final static String propertyHop = "hop#";
	private final static String propertyPings = "pings";
	private final static String propertyHostname = "hostname";
	private final static String propertyIP = "ip";

	private final static Logger logger = Logger.getLogger(VisualTraceScreen.class.getPackage().getName());

	private VisualTraceController visualTraceController;
	private Label labelUnderMap;
	private Pane paneStackColor;
	private Pane paneTraceInfo;
	private List<String> tracertOutput;
	private Map<CheckBox, String> checkboxToIP = new HashMap<>();
	private Map<String, GeoIPInfo> geoIPResults = new HashMap<>();
	private List<CheckBox> listOfChkBoxes = new ArrayList<>();
	private GenerateImageFromURLService imgService;
	private ImageView imgView;
	private ToggleGroup zoomToggleGroup = new ToggleGroup();
	private Map<String, GeoIPInfo> ipToGeoipInfo = new HashMap<>();

	public VisualTraceScreen(List<String> listOfIPs, Stage postCloseStage, Scene postCloseScene) throws IOException
	{
		super(visualTraceFormLocation, postCloseStage, postCloseScene);
		this.tracertOutput = listOfIPs;

		visualTraceController = getLoader().<VisualTraceController> getController();
		imgView = visualTraceController.getImgView();
		labelUnderMap = visualTraceController.getLoadingLabel();
		paneStackColor = visualTraceController.getPaneStackColor();
		paneTraceInfo = visualTraceController.getPaneTraceInfo();

		initImgService();
		populateGeoIPResults();
		generateTraceInfoGUI();
		generateAndShowImage();

		setStageOnShowing(event -> Platform.runLater(() -> resizeIfNeeded()));
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
				labelUnderMap.setText("No data to show");
				labelUnderMap.setVisible(true);
			}
			else
				paneStackColor.setStyle("-fx-background-color: #A3CBFE;"); //ocean color background to hide transparent part of image if exists
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

	private void resizeIfNeeded()
	{
		SplitPane splitPane = visualTraceController.getSplitPane();
		double traceInfoWidth = paneTraceInfo.getWidth();
		double splitPaneDividerPosition = splitPane.getDividerPositions()[0];
		double splitPaneWidth = splitPane.getWidth();

		if (traceInfoWidth > splitPaneDividerPosition * splitPaneWidth) //if trace info is wider than what is visible
		{
			double idealWidth = traceInfoWidth + (1 - splitPaneDividerPosition) * splitPaneWidth + 30;
			Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

			Stage stage = (Stage) imgView.getScene().getWindow();

			if (primaryScreenBounds.getWidth() > idealWidth) //if the resolution is wide enough to contain the view without scrollers
				stage.setWidth(idealWidth);
			else
				stage.setMaximized(true);
		}
	}

	private void generateTraceInfoGUI()
	{
		int row = 1, col = 0;
		GridPane gridPane = new GridPane();
		paneTraceInfo.getChildren().add(gridPane);
		gridPane.setHgap(10);

		createLabelsOnGridPane(gridPane, tracertOutput.get(0).contains("["));

		char label = 'A';
		for (String line : tracertOutput)
		{
			Map<String, String> valueMap = getMapOfValues(line);
			String ip = valueMap.get(propertyIP);

			col = 0;

			GeoIPInfo geoIPInfo = geoIPResults.get(ip);
			ipToGeoipInfo.put(ip, geoIPInfo);

			CheckBox box = generateCheckbox(label, geoIPInfo);
			listOfChkBoxes.add(box);
			gridPane.add(box, col++, row);
			GridPane.setHalignment(box, HPos.CENTER);

			checkboxToIP.put(box, ip);

			col = addPropertiesToGridPane(valueMap, gridPane, col, row);
			addButtonsToGridPane(ip, gridPane, col, row);

			if (label != 'Z')
				label++; //trace is limited to 30 hops, so no need to worry about '9'
			else
				label = '0';

			row++;
		}
	}

	private CheckBox generateCheckbox(char label, GeoIPInfo geoIPInfo)
	{
		CheckBox box = new CheckBox(String.valueOf(label));

		boolean hasLocation = geoIPInfo.getSuccess();
		box.setSelected(hasLocation);
		box.setDisable(!hasLocation);

		box.selectedProperty().addListener((observable, oldValue, newValue) ->
		{
			Toggle selectedZoomBtn = zoomToggleGroup.getSelectedToggle();
			if (selectedZoomBtn != null)
				selectedZoomBtn.setSelected(false);

			generateAndShowImage();
		});

		return box;
	}

	/**
	 * @return the updated col value, after the new property columns were added
	 */
	private int addPropertiesToGridPane(Map<String, String> valueMap, GridPane gridPane, int col, int row)
	{
		String[] orderedProperties = new String[] { propertyHop, propertyPings, propertyHostname, propertyIP };
		for (String key : orderedProperties)
		{
			String value = valueMap.get(key);
			if (value != null)
			{
				Label tempLabel = new Label(value);
				gridPane.add(tempLabel, col++, row);

				if (key.equals(propertyHop))
					GridPane.setHalignment(tempLabel, HPos.CENTER);
			}
		}

		return col;
	}

	/**
	 * @param fullLine
	 *            - full line from tracert output
	 * @return - a map of values with these keys: propertyHop, propertyPings,
	 *         propertyHostname (if available), propertyIP
	 */
	private Map<String, String> getMapOfValues(String fullLine)
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

		Map<String, String> mappedResults = new HashMap<>();
		int index = 0;

		mappedResults.put(propertyHop, values.get(index++));
		mappedResults.put(propertyPings, values.get(index++));
		if (containsHostname)
			mappedResults.put(propertyHostname, values.get(index++));
		mappedResults.put(propertyIP, values.get(index++));

		return mappedResults;
	}

	private void addButtonsToGridPane(String ip, GridPane gridPane, int col, int row)
	{
		ToggleButton btnZoom = new ToggleButton();
		btnZoom.setToggleGroup(zoomToggleGroup);
		btnZoom.setGraphic(new ImageView(new Image(getClass().getResourceAsStream(zoomInIconLocation))));
		btnZoom.setTooltip(new Tooltip("Zoom in on this location (into the center of the city)"));
		btnZoom.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) ->
		{
			HBox hbox = (HBox) btnZoom.getParent();
			@SuppressWarnings("unchecked")
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
		spinnerZoom.setTooltip(spinnerTooltip);
		spinnerZoom.getEditor().setTooltip(spinnerTooltip);
		spinnerZoom.setEditable(false);
		spinnerZoom.setDisable(true);
		HBox zoomControls = new HBox(btnZoom, spinnerZoom);

		Button btnGeoIP = new Button();
		btnGeoIP.setGraphic(new ImageView(new Image(getClass().getResourceAsStream(geoIPIconLocation))));
		btnGeoIP.setTooltip(new Tooltip("Show more detailed GeoIP info online (opens in a browser window)"));
		btnGeoIP.setOnAction(event -> Main.openInBrowser(GeoIPResolver.getSecondaryGeoIpPrefix() + ip));

		gridPane.add(zoomControls, col++, row);
		gridPane.add(btnGeoIP, col++, row);
		GridPane.setHalignment(btnZoom, HPos.CENTER);
		GridPane.setHalignment(btnGeoIP, HPos.CENTER);

		if (!ipToGeoipInfo.get(ip).getSuccess()) //no geoip info for this ip
			btnZoom.setDisable(true);
	}

	private void createLabelsOnGridPane(GridPane gridPane, boolean withHostnames)
	{
		Font defaultFont = Font.getDefault();
		Font font = Font.font(defaultFont.getName(), FontWeight.BOLD, defaultFont.getSize());
		int col = 0;

		Label labelMapNode = new Label("Map label");
		Label labelHopNum = new Label("Hop #");
		Label labelPings = new Label("Ping results");
		Label labelHostname = new Label("Hostname");
		Label labelIPAddress = new Label("IP address");
		Label labelFocusHere = new Label("Zoom in");
		Label labelGeoIPInfo = new Label("GeoIP info");
		labelMapNode.setFont(font);
		labelHopNum.setFont(font);
		labelPings.setFont(font);
		labelHostname.setFont(font);
		labelIPAddress.setFont(font);
		labelFocusHere.setFont(font);
		labelGeoIPInfo.setFont(font);

		gridPane.add(labelMapNode, col++, 0);
		gridPane.add(labelHopNum, col++, 0);
		gridPane.add(labelPings, col++, 0);
		if (withHostnames)
			gridPane.add(labelHostname, col++, 0);
		gridPane.add(labelIPAddress, col++, 0);
		gridPane.add(labelFocusHere, col++, 0);
		gridPane.add(labelGeoIPInfo, col++, 0);
	}

	private void generateAndShowImage()
	{
		Platform.runLater(() ->
		{
			paneStackColor.setStyle("");
			imgView.setImage(null);
			labelUnderMap.setText("Loading...");
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

		List<CheckBox> reorderedListOfCheckBoxes = getReorderedListOfCheckboxes();

		for (CheckBox checkBox : reorderedListOfCheckBoxes)
		{
			String text = checkBox.getText();
			String ip;

			if (checkBox.isSelected())
			{
				char label = text.charAt(0);

				ip = checkboxToIP.get(checkBox);
				GeoIPInfo ipInfo = ipToGeoipInfo.get(ip);

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

		for (int i = 0; i < listOfChkBoxes.size(); i++)
		{
			CheckBox checkBox = listOfChkBoxes.get(i);

			if (!checkBox.isSelected())
				continue;

			String ipForPath = checkboxToIP.get(checkBox);
			String locationForPath = getLocationString(ipToGeoipInfo.get(ipForPath));

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
			GeoIPInfo ipInfo = ipToGeoipInfo.get(centerOnIP);
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
	private List<CheckBox> getReorderedListOfCheckboxes()
	{
		List<CheckBox> reorderedListOfCheckBoxes = new ArrayList<>();

		CheckBox checkedBox;
		int firstSelectedBox = 0;
		int lastSelectedBox = listOfChkBoxes.size() - 1;

		do
		{
			checkedBox = listOfChkBoxes.get(firstSelectedBox++);
		} while (!checkedBox.isSelected() && firstSelectedBox < listOfChkBoxes.size());
		
		reorderedListOfCheckBoxes.add(checkedBox);

		do
		{
			checkedBox = listOfChkBoxes.get(lastSelectedBox--);
		} while (!checkedBox.isSelected() && lastSelectedBox >= 0);
		
		if (!reorderedListOfCheckBoxes.contains(checkedBox))
			reorderedListOfCheckBoxes.add(checkedBox);
		else
			return reorderedListOfCheckBoxes; //if there's just one box to add, don't add it twice.

		for (int i = lastSelectedBox; i >= firstSelectedBox; i--)
		{
			checkedBox = listOfChkBoxes.get(i);
			if (checkedBox.isSelected())
				reorderedListOfCheckBoxes.add(checkedBox);
		}

		return reorderedListOfCheckBoxes;
	}

	private String getLocationString(GeoIPInfo ipInfo)
	{
		String region = ipInfo.getRegion();
		String location = ipInfo.getCountry() + "," + (region.isEmpty() ? "" : ipInfo.getRegion() + ",") + ipInfo.getCity();
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

	public Button getBtnClose()
	{
		return visualTraceController.getBtnClose();
	}
	
	public Label getLabelUnderMap()
	{
		return labelUnderMap;
	}

	private static class GenerateImageFromURLService extends Service<Image>
	{
		private VisualTraceScreen traceScreen;
		private String centerOnIP; //IP location to center on, only relevant ONCE for the next use. Ignored when null.
		private Map<String, Image> urlToImageCache = new HashMap<>();
		private int zoom;

		public GenerateImageFromURLService(VisualTraceScreen traceScreen)
		{
			this.traceScreen = traceScreen;
			this.centerOnIP = null;
		}

		@Override
		protected Task<Image> createTask()
		{
			return new Task<Image>()
			{
				@Override
				protected Image call() throws Exception
				{
					String url = traceScreen.generateURL(centerOnIP, zoom);
					
					if (url.equals(baseUrl))
						return null;
					
					centerOnIP = null; //reset for next use

					Image img = urlToImageCache.get(url);
					if (img == null)
					{
						img = new Image(url);
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
	}
}
