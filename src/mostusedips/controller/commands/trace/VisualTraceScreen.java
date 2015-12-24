package mostusedips.controller.commands.trace;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import mostusedips.Main;
import mostusedips.controller.GUIController;
import mostusedips.controller.VisualTraceController;
import mostusedips.model.geoipresolver.GeoIPInfo;
import mostusedips.model.geoipresolver.GeoIPResolver;
import mostusedips.view.SecondaryFXMLScreen;

public class VisualTraceScreen extends SecondaryFXMLScreen
{
	private final static String visualTraceFormLocation = "/mostusedips/view/VisualTraceForm.fxml";
	private final static String baseUrl = "https://maps.googleapis.com/maps/api/staticmap?key=AIzaSyCT-QqmWbW7A-N0ywbXTmblZKq5flvtXmE&size=400x400&scale=2&maptype=roadmap";
	private final static Logger logger = Logger.getLogger(Main.getAppName());

	private VisualTraceController visualTraceController;
	private ArrayList<IPHostAndLabel> listOfIPs;
	private ArrayList<CheckBox> listOfChkBoxes = new ArrayList<CheckBox>();
	private GenerateImageFromURLService imgService;

	public VisualTraceScreen(ArrayList<IPHostAndLabel> listOfIPs)
	{
		this.listOfIPs = listOfIPs;

		imgService = new GenerateImageFromURLService(this);

		initScreen();

		imgService.setOnSucceeded(new EventHandler<WorkerStateEvent>()
		{

			@Override
			public void handle(WorkerStateEvent event)
			{
				getVisualTraceController().getLoadingLabel().setVisible(false);
				Image img = imgService.getValue();
				getVisualTraceController().getImgView().setImage(img);
			}
		});
	}

	public void showScreen()
	{
		Scene scene = new Scene(getLoadedFXML());
		Stage stage = getStage();
		stage.setScene(scene);
		stage.show();
	}

	private void initScreen()
	{
		FXMLLoader loader;

		try
		{
			loader = new FXMLLoader(getClass().getResource(visualTraceFormLocation));
			setLoadedFXML(loader.load());

		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "Unable to load resource " + visualTraceFormLocation, e);
			return;
		}

		visualTraceController = loader.<VisualTraceController> getController();

		setCloseButtonStageAndScene(getVisualTraceController().getBtnClose(), getStage(), getPostCloseScene());

		VBox vbox = getVisualTraceController().getVboxChkboxes();

		for (IPHostAndLabel ipInfo : listOfIPs)
		{
			String text = ipInfo.getLabel() + ": " + ipInfo.getIp() + " (" + ipInfo.getHostname() + ")";
			CheckBox box = new CheckBox(text);

			box.setSelected(true);
			box.selectedProperty().addListener(new ChangeListener<Boolean>()
			{

				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
				{
					generateAndShowImage();
				}
			});

			listOfChkBoxes.add(box);
			vbox.getChildren().add(box);

			Hyperlink geoIPLink = new Hyperlink("GeoIP info");
			geoIPLink.setOnAction(new EventHandler<ActionEvent>()
			{

				@Override
				public void handle(ActionEvent event)
				{
					GUIController.openInBrowser(GUIController.getSecondaryGeoIpPrefix() + ipInfo.getIp());
				}
			});

			HBox hbox = new HBox(box, geoIPLink);
			vbox.getChildren().add(hbox);
		}

		generateAndShowImage();

	}

	private void generateAndShowImage()
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				getVisualTraceController().getImgView().setImage(null);
				getVisualTraceController().getLoadingLabel().setVisible(true);
				imgService.restart();
			}
		});
	}

	private String generateURL()
	{
		String url = new String(baseUrl);
		final String pathInit = "&path=";
		String path = pathInit;
		boolean isFirstMarker = true;

		for (CheckBox checkBox : listOfChkBoxes)
		{
			String text = checkBox.getText();
			String ip = text.split(" ")[1];

			if (checkBox.isSelected())
			{
				char label = text.charAt(0);

				ip = text.split(" ")[1];
				GeoIPInfo ipInfo = GeoIPResolver.getIPInfo(ip);

				if (!isPublicIP(ip) || ipInfo == null || !ipInfo.getSuccess()) //not a public ip with a location, skip it
				{
					checkBox.setSelected(false);
					checkBox.setDisable(true);

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

		public GenerateImageFromURLService(VisualTraceScreen traceScreen)
		{
			this.traceScreen = traceScreen;
		}

		@Override
		protected Task<Image> createTask()
		{
			return new Task<Image>()
			{
				@Override
				protected Image call() throws Exception
				{
					String url = traceScreen.generateURL();
					Image image = new Image(url);
					return image;
				}
			};
		}

	}
}
