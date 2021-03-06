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
package whowhatwhere;

import java.awt.Desktop;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
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
import whowhatwhere.controller.GUIController;
import whowhatwhere.controller.ToolTipUtilities;

public class Main extends Application
{
	public final static String releaseVersion = "1.00";
	private final static String urlForLatestRelease = "http://snip.li/WhoWhatWhereUpdate";

	public static final String website = "http://ck3ck3.github.io/WhoWhatWhere";
	public static final String copyrightNotice = "Copyright (C) 2017  ck3ck3 ";
	public static final String email = "WhoWhatWhereInfo@gmail.com";

	public final static String iconResource16 = "/appIcons/www16.jpg";
	public final static String iconResource32 = "/appIcons/www32.jpg";
	public final static String iconResource48 = "/appIcons/www48.jpg";
	public final static String iconResource256 = "/appIcons/www256.jpg";
	public final static List<Image> appIconList = Arrays.asList(new Image(Main.class.getResourceAsStream(iconResource16)), new Image(Main.class.getResourceAsStream(iconResource32)),
			new Image(Main.class.getResourceAsStream(iconResource48)), new Image(Main.class.getResourceAsStream(iconResource256)));
	
	public final static String attributionHTMLLocation = website + "/attribution.html";

	public final static String appTitle = "Who What Where";
	public final static String executableFilename = "WhoWhatWhere.exe";
	public final static String scheduledTaskName = "Who What Where launcher " + releaseVersion;
	private final static String mainFormLocation = "/whowhatwhere/view/fxmls/maingui/MainForm.fxml";
	
	public final static String appFilesLocation = System.getenv("APPDATA") + "\\" + appTitle + "\\";
	
	private final static int tooltipOpenDelay = 250;
	private final static int tooltipVisibleDuration = 15000;
	private final static int tooltipCloseDelay = 200;

	private static Logger logger = Logger.getLogger(Main.class.getPackage().getName());

	@Override
	public void start(Stage primaryStage)
	{
		try
		{
			initLogger();

			ToolTipUtilities.setTooltipTimers(tooltipOpenDelay, tooltipVisibleDuration, tooltipCloseDelay);
			FXMLLoader loader = new FXMLLoader(getClass().getResource(mainFormLocation));
			Parent root = (Parent) loader.load();
			GUIController gui = (GUIController) loader.getController();

			Scene scene = new Scene(root);
			primaryStage.setTitle(appTitle);
			primaryStage.setScene(scene);
			gui.setStage(primaryStage);
			gui.init();
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
		FileHandler fh = new FileHandler(appTitle + ".log");
		fh.setFormatter(new SimpleFormatter());
		logger.addHandler(fh);
		
		System.setErr(new PrintStream(new FileOutputStream("stderr.log")));
	}
	
	public static CheckForUpdateResult checkForUpdate()
	{
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		
		HttpGet getRequest = new HttpGet(urlForLatestRelease);
		getRequest.addHeader("User-Agent", appTitle);
	    try
		{
	    	CloseableHttpResponse getResponse = httpClient.execute(getRequest);
			String output = IOUtils.toString(getResponse.getEntity().getContent());
			httpClient.close();
			
			JSONObject jsonObject = new JSONObject(output);
			String version = (String) jsonObject.get("tag_name");
			String releaseNotes = (String) jsonObject.get("body");
			
			return new CheckForUpdateResult(!version.equals(releaseVersion), version, releaseNotes);
		}
		catch (IOException ioe)
		{
			logger.log(Level.WARNING, "Failed to check for updates", ioe);
			return new CheckForUpdateResult(ioe.getMessage());
		}
	}
	
	public static void openInBrowser(String link)
	{
		boolean fail = false;
		String errorMsg = "Unable to open " + link + " in the browser";
		
		if (Desktop.isDesktopSupported())
		{
			try
			{
				URI uri = new URI(link);
				Desktop.getDesktop().browse(uri);
			}
			catch (IOException | URISyntaxException e)
			{
				fail = true;
				logger.log(Level.SEVERE, errorMsg, e);
			}
		}
		else
			fail = true;
		
		if (fail)
			Platform.runLater(() ->	new Alert(AlertType.ERROR, errorMsg).showAndWait());
	}

	public static void main(String[] args)
	{
		launch(args);
	}
}
