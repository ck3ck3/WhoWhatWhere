package whowhatwhere;

import java.awt.Desktop;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import whowhatwhere.controller.CheckForUpdatesResultHandler;

public class Main extends Application
{
	private final static String releaseVersion = "1.10";
	private final static String urlForLatestRelease = "https://api.github.com/repos/ck3ck3/WhoWhatWhere/releases/latest";

	private static final String website = "http://ck3ck3.github.io/WhoWhatWhere/";

	private final static String iconResource16 = "/www16.jpg";
	private final static String iconResource32 = "/www32.jpg";
	private final static String iconResource48 = "/www48.jpg";
	private final static String iconResource256 = "/www256.jpg";
	public final static String jnetpcapDLLx86Location = "/native/windows/x86/jnetpcap.dll";
	public final static String jnetpcapDLLx64Location = "/native/windows/x86_64/jnetpcap.dll";

	private final static String appTitle = "Who What Where";
	private final static String executableFilename = "WhoWhatWhere.exe";
	private final static String mainFormLocation = "/whowhatwhere/view/MainForm.fxml";
	private final static int windowSizeX = 1190;
	private final static int windowSizeY = 790;

	private static Logger logger = Logger.getLogger(Main.class.getPackage().getName());

	@Override
	public void start(Stage primaryStage)
	{
		try
		{
			initLogger();

			if (!initSysTray(primaryStage))
				logger.log(Level.WARNING, "Unable to initialize system tray");

			URL fxmlLocation = Main.class.getResource(mainFormLocation);
			Parent root = FXMLLoader.load(fxmlLocation);

			Scene scene = new Scene(root, windowSizeX, windowSizeY);
			primaryStage.setTitle(appTitle);
			primaryStage.setScene(scene);
			primaryStage.show();
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "Unable to open file", e);
		}
	}

	private void initLogger() throws IOException
	{
		logger = Logger.getLogger(this.getClass().getPackage().getName());
		FileHandler fh = new FileHandler(getAppName() + ".log");
		fh.setFormatter(new SimpleFormatter());
		logger.addHandler(fh);
	}

	private boolean initSysTray(Stage primaryStage)
	{
		TrayIcon trayIcon;
		SystemTray tray;

		if (!SystemTray.isSupported())
			return false;
		else
		{
			primaryStage.getIcons().addAll(new Image(Main.class.getResourceAsStream(iconResource16)), new Image(Main.class.getResourceAsStream(iconResource32)),
					new Image(Main.class.getResourceAsStream(iconResource48)), new Image(Main.class.getResourceAsStream(iconResource256)));

			tray = SystemTray.getSystemTray();
			java.awt.Image image = Toolkit.getDefaultToolkit().getImage(Main.class.getResource(iconResource16));

			Platform.setImplicitExit(false); //needed to keep the app running while minimized to tray

			trayIcon = new TrayIcon(image, appTitle);

			Runnable restoreApplication = () ->
			{
				primaryStage.show();
				tray.remove(trayIcon);
			};

			trayIcon.addActionListener(ae -> Platform.runLater(restoreApplication));

			PopupMenu popupMenu = new PopupMenu();
			MenuItem restore = new MenuItem("Restore");

			restore.addActionListener(al -> Platform.runLater(restoreApplication));

			popupMenu.add(restore);
			trayIcon.setPopupMenu(popupMenu);

			primaryStage.setOnCloseRequest(we ->
			{
				we.consume(); //ignore the application's title window exit button, instead minimize to systray
				Platform.runLater(() ->
				{
					try
					{
						tray.add(trayIcon);
						primaryStage.hide();
					}
					catch (Exception e)
					{
						logger.log(Level.WARNING, "Unable to minimize to tray", e);
					}
				});
			});

			return true;
		}
	}

	public static void isUpdateAvailable(CheckForUpdatesResultHandler resultHandler, boolean silent) throws IOException
	{
		InputStream inputStream = new URL(urlForLatestRelease).openStream();
		String response = IOUtils.toString(inputStream);
		IOUtils.closeQuietly(inputStream);

		JSONObject jsonObject = new JSONObject(response);
		String version = (String) jsonObject.get("tag_name");

		resultHandler.checkForUpdatesResult(!version.equals(getReleaseVersion()), silent);
	}

	public static void openInBrowser(String link)
	{
		if (Desktop.isDesktopSupported())
		{
			try
			{
				URI uri = new URI(link);
				Desktop.getDesktop().browse(uri);
			}
			catch (IOException | URISyntaxException e)
			{
				String msg = "Unable to open \"" + link + "\" in the browser";
				new Alert(AlertType.ERROR, msg).showAndWait();
				logger.log(Level.SEVERE, msg, e);
			}
		}
	}

	public static void main(String[] args)
	{
		launch(args);
	}

	public static String getAppName()
	{
		return appTitle;
	}

	public static String getReleaseVersion()
	{
		return releaseVersion;
	}

	public static String getWebsite()
	{
		return website;
	}

	public static String getExecutablefilename()
	{
		return executableFilename;
	}
}
