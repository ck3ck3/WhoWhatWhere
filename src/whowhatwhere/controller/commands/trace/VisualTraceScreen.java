package whowhatwhere.controller.commands.trace;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import whowhatwhere.Main;
import whowhatwhere.controller.utilities.VisualTraceController;
import whowhatwhere.model.geoipresolver.GeoIPInfo;
import whowhatwhere.model.geoipresolver.GeoIPResolver;
import whowhatwhere.view.SecondaryFXMLScreen;

public class VisualTraceScreen extends SecondaryFXMLScreen
{
	private final static String visualTraceFormLocation = "/whowhatwhere/view/VisualTraceForm.fxml";
	private final static String baseUrl = "https://maps.googleapis.com/maps/api/staticmap?key=AIzaSyCT-QqmWbW7A-N0ywbXTmblZKq5flvtXmE&size=400x400&scale=2&maptype=roadmap";
	private final static Logger logger = Logger.getLogger(VisualTraceScreen.class.getPackage().getName());

	private VisualTraceController visualTraceController;
	private List<String> listOfIPs;
	private Map<String, GeoIPInfo> geoIPResults = new HashMap<>();
	private List<CheckBox> listOfChkBoxes = new ArrayList<>();
	private GenerateImageFromURLService imgService;

	public VisualTraceScreen(List<String> listOfIPs, Stage postCloseStage, Scene postCloseScene) throws IOException
	{
		super(visualTraceFormLocation, postCloseStage, postCloseScene);
		this.listOfIPs = listOfIPs;

		imgService = new GenerateImageFromURLService(this);

		for (String line : listOfIPs)
		{
			String ip = TraceCommandScreen.extractIPFromLine(line);
			GeoIPInfo ipInfo = GeoIPResolver.getIPInfo(ip);

			geoIPResults.put(ip, ipInfo);
		}

		visualTraceController = getLoader().<VisualTraceController> getController();
		generateTraceInfoGUI();
		generateAndShowImage();

		imgService.setOnSucceeded(event ->
		{
			getVisualTraceController().getLoadingLabel().setVisible(false);
			Image img = imgService.getValue();
			getVisualTraceController().getImgView().setImage(img);
		});
	}

	private void generateTraceInfoGUI()
	{
		VBox vbox = getVisualTraceController().getVboxChkboxes();

		char label = 'A';
		for (String ipInfo : listOfIPs)
		{
			String text = label + ": " + ipInfo;
			CheckBox box = new CheckBox(text);

			box.setSelected(true);
			box.selectedProperty().addListener((observable, oldValue, newValue) -> generateAndShowImage());

			listOfChkBoxes.add(box);
			vbox.getChildren().add(box);

			final String ip = TraceCommandScreen.extractIPFromLine(ipInfo);

			Hyperlink geoIPLink = new Hyperlink("GeoIP info");
			geoIPLink.setOnAction(event -> Main.openInBrowser(GeoIPResolver.getSecondaryGeoIpPrefix() + ip));

			Hyperlink focusMapLink = new Hyperlink("Focus map here");
			focusMapLink.setOnAction(event ->
			{
				imgService.setCenterOnIP(ip);
				generateAndShowImage();
			});

			HBox hbox = new HBox(box, focusMapLink, geoIPLink);
			vbox.getChildren().add(hbox);

			if (label != 'Z')
				label++; //trace is limited to 30 hops, so no need to worry about '9'
			else
				label = '0';
		}
	}

	private void generateAndShowImage()
	{
		Platform.runLater(() ->
		{
			getVisualTraceController().getImgView().setImage(null);
			getVisualTraceController().getLoadingLabel().setVisible(true);
			imgService.restart();
		});
	}

	private String generateURL(String centerOnIP)
	{
		String url = baseUrl;
		final String pathInit = "&path=";
		String path = pathInit;
		boolean isFirstMarker = true;

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

			return url + "&center=" + location + "&zoom=10";
		}

		for (CheckBox checkBox : listOfChkBoxes)
		{
			String text = checkBox.getText();
			String ip;

			if (checkBox.isSelected())
			{
				char label = text.charAt(0);

				ip = TraceCommandScreen.extractIPFromLine(text);
				GeoIPInfo ipInfo = geoIPResults.get(ip);

				if (!isPublicIP(ip) || ipInfo == null || !ipInfo.getSuccess()) //not a public ip with a location, skip it
				{
					checkBox.setSelected(false);
					checkBox.setDisable(true);
					checkBox.getParent().getChildrenUnmodifiable().get(1).setDisable(true); //also disable the hyperlink "focus map", since there's no location

					continue;
				}

				String markers = "&markers=color:" + (isFirstMarker ? "red" : "blue") + "%7Clabel:" + label;
				isFirstMarker = false;

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
				url += markers + "%7C" + location;

				if (!path.equals(pathInit)) //if it's not the first part of the path
					location = "%7C" + location;

				path += location;
			}
		}

		//change the last marker from blue to red
		String lastMarker = "markers=color:blue";
		if (url.contains(lastMarker))
		{
			int lastIndexOfMarker = url.lastIndexOf(lastMarker);
			url = url.substring(0, lastIndexOfMarker) + "markers=color:red" + url.substring(lastIndexOfMarker + lastMarker.length());
		}

		return url + path;
	}

	private boolean isPublicIP(String ip)
	{
		Inet4Address address;

		try
		{
			address = (Inet4Address) InetAddress.getByName(ip);
		}
		catch (UnknownHostException exception)
		{
			return false; // not a real error, just not a valid IP.
		}

		return !(address.isSiteLocalAddress() || address.isAnyLocalAddress() || address.isLinkLocalAddress() || address.isLoopbackAddress() || address.isMulticastAddress());
	}

	public VisualTraceController getVisualTraceController()
	{
		return visualTraceController;
	}

	private static class GenerateImageFromURLService extends Service<Image>
	{
		private VisualTraceScreen traceScreen;
		private String centerOnIP; //IP location to center on, only relevant ONCE for the next use. Ignored when null.

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
					String url = traceScreen.generateURL(centerOnIP);
					centerOnIP = null; //reset for next use
					return new Image(url);
				}
			};
		}

		public void setCenterOnIP(String centerOnIP)
		{
			this.centerOnIP = centerOnIP;
		}
	}
}
