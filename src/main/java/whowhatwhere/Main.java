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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javafx.application.Application;
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
	private final static String releaseVersion = "1.10";
	private final static String urlForLatestRelease = "http://bit.ly/WhoWhatWhereUpdate";

	private static final String website = "http://ck3ck3.github.io/WhoWhatWhere";

	public final static String iconResource16 = "/appIcons/www16.jpg";
	public final static String iconResource32 = "/appIcons/www32.jpg";
	public final static String iconResource48 = "/appIcons/www48.jpg";
	public final static String iconResource256 = "/appIcons/www256.jpg";
	public final static List<Image> appIconList = Arrays.asList(new Image(Main.class.getResourceAsStream(iconResource16)), new Image(Main.class.getResourceAsStream(iconResource32)),
			new Image(Main.class.getResourceAsStream(iconResource48)), new Image(Main.class.getResourceAsStream(iconResource256)));
	
	public final static String attributionHTMLLocation = "/attribution.html";
	public final static String jnetpcapDLLx86Location = "/native/windows/x86/jnetpcap.dll";
	public final static String jnetpcapDLLx64Location = "/native/windows/x86_64/jnetpcap.dll";

	private final static String appTitle = "Who What Where";
	private final static String executableFilename = "WhoWhatWhere.exe";
	private final static String mainFormLocation = "/whowhatwhere/view/fxmls/maingui/MainForm.fxml";
	
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
		FileHandler fh = new FileHandler(getAppName() + ".log");
		fh.setFormatter(new SimpleFormatter());
		logger.addHandler(fh);
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
	
	public static String getURLForLatestRelease()
	{
		return urlForLatestRelease;
	}
}
