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
package whowhatwhere.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import whowhatwhere.Main;
import whowhatwhere.model.PropertiesByType;
import whowhatwhere.model.networksniffer.NICInfo;
import whowhatwhere.model.networksniffer.NetworkSniffer;
import whowhatwhere.model.startwithwindows.StartWithWindowsRegistryUtils;
import whowhatwhere.view.secondaryfxmlscreen.SecondaryFXMLScreen;

public class SettingsHandler
{
	private final static String propsFileLocation = Main.getAppName() + ".properties";
	private final static String defaultPropsResource = "/defaultLastRun.properties";

	private final static String propsNICDescription = "Selected NIC description";
	private final static String propsShowMessageOnMinimize = "showMinimizeMessage";
	private final static String propsStartMinimized = "startMinimized";
	private final static String propsIgnoreRunPathDiff = "ignorePathDiff";
	private final static String propsCheckForUpdatesOnStartup = "checkForUpdatesOnStartup";
	private final static String propsMinimizeOnXBtn = "minimizeOnXBtn";
	private final static String propsWidth = "lastRunWidth";
	private final static String propsHeight = "lastRunHeight";
	private final static String propsX = "lastRunX";
	private final static String propsY = "lastRunY";

	private final static Logger logger = Logger.getLogger(SettingsHandler.class.getPackage().getName());

	private boolean showMessageOnMinimize;
	private boolean ignoreRunPathDiff;
	private boolean startMinimized;
	private boolean checkForUpdatesOnStartup;
	private boolean minimizeOnXBtn;
	private NetworkSniffer sniffer;

	private GUIController guiController;

	public SettingsHandler(GUIController guiController)
	{
		this.guiController = guiController;
		sniffer = guiController.getSniffer();
	}

	public void saveCurrentRunValuesToProperties(List<LoadAndSaveSettings> instancesWithSettingsToHandle)
	{
		Stage stage = guiController.getStage();
		Properties props = new Properties();

		for (LoadAndSaveSettings instance : instancesWithSettingsToHandle)
			instance.saveCurrentRunValuesToProperties(props);

		props.put(propsNICDescription, guiController.getSelectedNIC().getDescription());

		props.put(propsCheckForUpdatesOnStartup, String.valueOf(checkForUpdatesOnStartup));
		props.put(propsShowMessageOnMinimize, String.valueOf(showMessageOnMinimize));
		props.put(propsMinimizeOnXBtn, String.valueOf(minimizeOnXBtn));
		props.put(propsStartMinimized, String.valueOf(startMinimized));
		props.put(propsIgnoreRunPathDiff, String.valueOf(ignoreRunPathDiff));
		props.put(propsWidth, String.valueOf(stage.getWidth()));
		props.put(propsHeight, String.valueOf(stage.getHeight()));
		props.put(propsX, String.valueOf(stage.getX()));
		props.put(propsY, String.valueOf(stage.getY()));

		try
		{
			savePropertiesSafely(props, "Last run configuration", propsFileLocation);
		}
		catch (IOException ioe)
		{
			logger.log(Level.SEVERE, "Unable to save properties file " + propsFileLocation, ioe);
		}
	}

	/**
	 * Saves Properties to file, but doesn't overwrite the previous file unless
	 * the save was successful.
	 * 
	 * @param props
	 *            - Properties object to save
	 * @param note
	 *            - the note on top of the saved file
	 * @param filename
	 *            - name of the file to save
	 * @throws IOException
	 *             - if saving failed
	 */
	public static void savePropertiesSafely(Properties props, String note, String filename) throws IOException
	{
		String saveFileAttempt = filename + ".attempt";
		FileOutputStream out = new FileOutputStream(saveFileAttempt); //in case saving goes wrong, we don't lose the previous save file
		props.store(out, note);
		out.close();

		new File(filename).delete();
		new File(saveFileAttempt).renameTo(new File(filename));
	}

	private Properties loadProperties()
	{
		InputStream in;
		File lastRun = new File(propsFileLocation);
		Properties props = new Properties();

		try
		{
			in = ((lastRun.exists() && lastRun.length() > 0) ? new FileInputStream(lastRun) : this.getClass().getResourceAsStream(defaultPropsResource));

			props.load(in);
			in.close();
		}
		catch (IOException ioe)
		{
			logger.log(Level.SEVERE, "Unable to load properties file: " + ioe.getMessage(), ioe);
		}

		return props;
	}

	public void loadLastRunConfig(List<LoadAndSaveSettings> instancesWithSettingsToHandle)
	{
		Properties props = loadProperties();
		List<Runnable> needsToRunAfterStageIsShown = new ArrayList<>();
		
		needsToRunAfterStageIsShown.add(() -> loadLastRunDimensions(props));

		if (!loadNICInfo(PropertiesByType.getStringProperty(props, propsNICDescription, "")))
			needsToRunAfterStageIsShown.add(() -> guiController.showNICSelectionScreen());

		checkForUpdatesOnStartup = PropertiesByType.getBoolProperty(props, propsCheckForUpdatesOnStartup, true);
		guiController.getMenuItemChkCheckUpdateStartup().setSelected(checkForUpdatesOnStartup);
		
		minimizeOnXBtn = PropertiesByType.getBoolProperty(props, propsMinimizeOnXBtn, true);
		guiController.getMenuItemChkMinimizeOnXBtn().setSelected(minimizeOnXBtn);
		guiController.setXBtnBehavior(minimizeOnXBtn);

		showMessageOnMinimize = PropertiesByType.getBoolProperty(props, propsShowMessageOnMinimize, true);
		guiController.getMenuItemChkDisplayBalloon().setSelected(showMessageOnMinimize);

		startMinimized = PropertiesByType.getBoolProperty(props, propsStartMinimized, false);
		guiController.getMenuItemChkStartMinimized().setSelected(startMinimized);
		if (startMinimized)
			Platform.runLater(() -> guiController.minimizeToTray());

		ignoreRunPathDiff = PropertiesByType.getBoolProperty(props, propsIgnoreRunPathDiff, false);
		loadStartWithWindowsSetting();

		Stage stage = guiController.getStage();
		EventHandler<WindowEvent> originalOnShown = stage.getOnShown();
		stage.setOnShown(event -> 
		{
			if (originalOnShown != null) //if there was already an event handler, run it
				originalOnShown.handle(event);
			
			for (Runnable runThis : needsToRunAfterStageIsShown)
				Platform.runLater(runThis);

			stage.setOnShown(originalOnShown); //only do this on the first time, after that it's back to the original event handler
		});

		for (LoadAndSaveSettings instance : instancesWithSettingsToHandle)
			instance.loadLastRunConfig(props);
	}

	private void loadLastRunDimensions(Properties props)
	{
		Stage stage = guiController.getStage();
		boolean propsExists = false;

		Double value;

		value = PropertiesByType.getDoubleProperty(props, propsWidth, Double.NaN);
		if (!value.equals(Double.NaN))
		{
			stage.setWidth(value);
			propsExists = true;
		}

		value = PropertiesByType.getDoubleProperty(props, propsHeight, Double.NaN);
		if (!value.equals(Double.NaN))
		{
			stage.setHeight(value);
			propsExists = true;
		}

		value = PropertiesByType.getDoubleProperty(props, propsX, Double.NaN);
		if (!value.equals(Double.NaN))
		{
			stage.setX(value);
			propsExists = true;
		}

		value = PropertiesByType.getDoubleProperty(props, propsY, Double.NaN);
		if (!value.equals(Double.NaN))
		{
			stage.setY(value);
			propsExists = true;
		}
		
		if (!propsExists)
			SecondaryFXMLScreen.fitToVisualBoundsIfTooBig(stage);
	}

	private void loadStartWithWindowsSetting()
	{
		try
		{
			String allUsers = StartWithWindowsRegistryUtils.getExecutableLocationToStartWithWindows(true);
			String currentUser = StartWithWindowsRegistryUtils.getExecutableLocationToStartWithWindows(false);

			boolean userChoseDelete = checkRunPathVsStartWithWindowsPath(allUsers, currentUser);
			
			if (userChoseDelete)
				return; //don't setSelected on either CheckmenuItem

			guiController.getMenuItemChkAllUsers().setSelected(allUsers != null);
			guiController.getMenuItemChkThisUserOnly().setSelected(currentUser != null);
		}
		catch (IOException ioe)
		{
			logger.log(Level.SEVERE, "Failed querying the registry for StartWithWindows values", ioe);
		}
	}
	
	private boolean checkRunPathVsStartWithWindowsPath(String allUsers, String currentUser) throws IOException
	{
		if (allUsers != null || currentUser != null) //only one or none of these are supposed to be set. Never both.
		{
			String locationToStartFrom = (allUsers == null ? currentUser : allUsers);
			String currentRunLocation = System.getProperty("user.dir") + "\\" + Main.getExecutableFilename();

			if (!ignoreRunPathDiff && !currentRunLocation.equalsIgnoreCase(locationToStartFrom))
			{
				boolean forAllUsers = allUsers != null;
				String message = Main.getAppName() + " is set to run when Windows starts, but you are currently running it from a different path than the one Windows is set to run it from.\n"
						+ "Current path: " + currentRunLocation + "\nWindows is set to run it from: " + locationToStartFrom + "\n\nPlease choose how to proceed:";

				boolean userChoseDelete = showStartupPathConflictDialog(forAllUsers, message);

				if (userChoseDelete)
					return true;
			}
		}
		
		return false;
	}

	public EventHandler<ActionEvent> handleStartWithWindowsClick(boolean allUsers, CheckMenuItem otherItem)
	{
		return ae ->
		{
			boolean add = ((CheckMenuItem) ae.getSource()).isSelected();
			
			try
			{
				StartWithWindowsRegistryUtils.setRegistryToStartWithWindows(add, allUsers);
			}
			catch (IOException ioe)
			{
				logger.log(Level.SEVERE, "Unable to modify the registry for the \"start with Windows\" setting", ioe);
				new Alert(AlertType.ERROR, "Unable to modify the registry for the \"start with Windows\" setting").showAndWait();
				return;
			}

			otherItem.setSelected(false); //if one is selected, the other one has to be unselected. if the click is to de-select, the other one was already unselected as well anyway.
			
			Alert success = new Alert(AlertType.INFORMATION, Main.getAppName() + (add ? " is now set to " : " will not ") + "start with Windows for " + (allUsers ? "all users" : "this user"));
			success.setTitle("Start with Windows");
			success.setHeaderText("Setting changed");
			success.showAndWait();
		};
	}

	/**
	 * @param forAllUsers
	 *            - true if the startup setting is for all users, false if for
	 *            current user only
	 * @param message
	 *            - the message to display in the dialog
	 * @return true if the user chose to delete the program from startup, false
	 *         otherwise.
	 * @throws IOException
	 *             - if there was a problem modifying the registry
	 */
	private boolean showStartupPathConflictDialog(boolean forAllUsers, String message) throws IOException
	{
		ButtonType btnModify = new ButtonType("Set to run from current path");
		ButtonType btnDelete = new ButtonType("Don't run when Windows starts");
		ButtonType btnIgnore = new ButtonType("Ignore this in the future");

		Optional<ButtonType> result = new Alert(AlertType.CONFIRMATION, message, btnModify, btnDelete, btnIgnore).showAndWait();
		ButtonType chosenButton = result.get();

		if (chosenButton == btnModify)
			StartWithWindowsRegistryUtils.setRegistryToStartWithWindows(true, forAllUsers);
		else
			if (chosenButton == btnDelete)
			{
				StartWithWindowsRegistryUtils.setRegistryToStartWithWindows(false, forAllUsers);
				return true;
			}
			else
				if (chosenButton == btnIgnore)
					ignoreRunPathDiff = true;

		return false;
	}

	/**
	 * @param nicDescription
	 * @return true if the NICInfo was successfully loaded, false if it wasn't, and we need to run guiController.showNICSelectionScreen() after the stage is shown 
	 */
	private boolean loadNICInfo(String nicDescription)
	{
		NICInfo nic = null;

		if (!nicDescription.isEmpty()) //we have a previously chosen NIC's description
		{
			nic = getNICByDescription(nicDescription);
			if (nic != null)
				guiController.setSelectedNIC(nic);
		}

		if (nic == null) //couldn't find the NIC
		{
			List<NICInfo> listOfDevices = sniffer.getListOfDevicesWithIP();

			if (listOfDevices.size() == 1) //if there's only one option
				guiController.setSelectedNIC(listOfDevices.get(0));
			else
				return false;
		}
		
		return true;
	}

	private NICInfo getNICByDescription(String description)
	{
		for (NICInfo nic : sniffer.getListOfDevicesWithIP())
			if (description.equals(nic.getDescription()))
					return nic;

		return null;
	}
	
	public boolean getCheckForUpdatesOnStartup()
	{
		return checkForUpdatesOnStartup;
	}

	public void setCheckForUpdatesOnStartup(boolean value)
	{
		checkForUpdatesOnStartup = value;
	}

	public boolean getShowMessageOnMinimize()
	{
		return showMessageOnMinimize;
	}

	public void setShowMessageOnMinimize(boolean value)
	{
		showMessageOnMinimize = value;
	}
	
	public void setStartMinimized(boolean value)
	{
		startMinimized = value;
	}
	
	public boolean getStartMinimized()
	{
		return startMinimized;
	}
	
	public boolean getMinimizeOnXBtn()
	{
		return minimizeOnXBtn;
	}
	
	public void setMinimizeOnXBtn(boolean value)
	{
		minimizeOnXBtn = value;
	}
}
