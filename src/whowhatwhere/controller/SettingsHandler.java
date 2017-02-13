package whowhatwhere.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import whowhatwhere.Main;
import whowhatwhere.model.PropertiesByType;
import whowhatwhere.model.networksniffer.NICInfo;
import whowhatwhere.model.networksniffer.NetworkSniffer;
import whowhatwhere.model.startwithwindows.StartWithWindowsRegistryUtils;

public class SettingsHandler
{
	private final static String propsFileLocation = Main.getAppName() + ".properties";
	private final static String defaultPropsResource = "/defaultLastRun.properties";
	private final static String NICSelectionFormLocation = "/whowhatwhere/view/fxmls/maingui/NICSelectionForm.fxml";

	private final static String propsNICDescription = "Selected NIC description";
	private final static String propsShowMessageOnMinimize = "showMinimizeMessage";
	private final static String propsStartMinimized = "startMinimized";
	private final static String propsIgnoreRunPathDiff = "ignorePathDiff";
	private final static String propsCheckForUpdatesOnStartup = "checkForUpdatesOnStartup";
	private final static String propsWidth = "lastRunWidth";
	private final static String propsHeight = "lastRunHeight";
	private final static String propsX = "lastRunX";
	private final static String propsY = "lastRunY";

	private final static Logger logger = Logger.getLogger(SettingsHandler.class.getPackage().getName());

	private boolean showMessageOnMinimize;
	private boolean ignoreRunPathDiff;
	private boolean checkForUpdatesOnStartup;
	private NICInfo selectedNIC;
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

		props.put(propsNICDescription, selectedNIC.getDescription());

		props.put(propsCheckForUpdatesOnStartup, String.valueOf(checkForUpdatesOnStartup));
		props.put(propsShowMessageOnMinimize, String.valueOf(showMessageOnMinimize));
		props.put(propsStartMinimized, String.valueOf(guiController.getMenuItemChkStartMinimized().isSelected()));
		props.put(propsIgnoreRunPathDiff, String.valueOf(ignoreRunPathDiff));
		props.put(propsWidth, String.valueOf(stage.getWidth()));
		props.put(propsHeight, String.valueOf(stage.getHeight()));
		props.put(propsX, String.valueOf(stage.getX()));
		props.put(propsY, String.valueOf(stage.getY()));

		try
		{
			savePropertiesSafely(props, "Last run configuration", propsFileLocation);
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "Unable to save properties file " + propsFileLocation, e);
		}
	}

	/**Saves Properties to file, but doesn't overwrite the previous file unless the save was successful.
	 * @param props - Properties object to save
	 * @param note - the note on top of the saved file
	 * @param filename - name of the file to save
	 * @throws IOException - if saving failed
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
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "Unable to load properties file: " + e.getMessage(), e);
		}

		return props;
	}

	public void loadLastRunConfig(List<LoadAndSaveSettings> instancesWithSettingsToHandle)
	{
		Properties props = loadProperties();

		loadNICInfo(PropertiesByType.getStringProperty(props, propsNICDescription, ""));

		checkForUpdatesOnStartup = PropertiesByType.getBoolProperty(props, propsCheckForUpdatesOnStartup, true);
		guiController.getMenuItemChkCheckUpdateStartup().setSelected(checkForUpdatesOnStartup);

		showMessageOnMinimize = PropertiesByType.getBoolProperty(props, propsShowMessageOnMinimize, true);
		guiController.getMenuItemChkDisplayBalloon().setSelected(showMessageOnMinimize);

		boolean startMinimized = PropertiesByType.getBoolProperty(props, propsStartMinimized, false);
		guiController.getMenuItemChkStartMinimized().setSelected(startMinimized);
		if (startMinimized)
			Platform.runLater(() -> guiController.minimizeToTray());

		ignoreRunPathDiff = PropertiesByType.getBoolProperty(props, propsIgnoreRunPathDiff, false);
		loadStartWithWindowsSetting();

		loadLastRunDimensions(props);

		for (LoadAndSaveSettings instance : instancesWithSettingsToHandle)
			instance.loadLastRunConfig(props);
	}

	private void loadLastRunDimensions(Properties props)
	{
		Stage stage = guiController.getStage();
		Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

		if (primaryScreenBounds.getHeight() < stage.getHeight() || primaryScreenBounds.getWidth() < stage.getWidth())
			stage.setMaximized(true);
		else
		{
			Double value;

			value = PropertiesByType.getDoubleProperty(props, propsWidth, Double.NaN);
			if (!value.equals(Double.NaN))
				stage.setWidth(value);

			value = PropertiesByType.getDoubleProperty(props, propsHeight, Double.NaN);
			if (!value.equals(Double.NaN))
				stage.setHeight(value);

			value = PropertiesByType.getDoubleProperty(props, propsX, Double.NaN);
			if (!value.equals(Double.NaN))
				stage.setX(value);

			value = PropertiesByType.getDoubleProperty(props, propsY, Double.NaN);
			if (!value.equals(Double.NaN))
				stage.setY(value);
		}
	}

	private void loadStartWithWindowsSetting()
	{
		try
		{
			String allUsers = StartWithWindowsRegistryUtils.getExecutableLocationToStartWithWindows(true);
			String currentUser = StartWithWindowsRegistryUtils.getExecutableLocationToStartWithWindows(false);

			if (allUsers != null || currentUser != null) //only one or none of these are supposed to be set. Never both.
			{
				String locationToStartFrom = (allUsers == null ? currentUser : allUsers);
				String currentRunLocation = System.getProperty("user.dir") + "\\" + Main.getExecutablefilename();

				if (!ignoreRunPathDiff && !currentRunLocation.equalsIgnoreCase(locationToStartFrom))
				{
					boolean forAllUsers = allUsers != null;
					String message = Main.getAppName() + " is set to run when Windows starts, but you are currently running it from a different path than the one Windows is set to run it from.\n"
							+ "Current path: " + currentRunLocation + "\nWindows is set to run it from: " + locationToStartFrom + "\n\nPlease choose how to proceed:";

					boolean userChoseDelete = showStartupPathConflictDialog(forAllUsers, message);

					if (userChoseDelete)
						return; //don't setSelected
				}
			}

			guiController.getMenuItemChkAllUsers().setSelected(allUsers != null);
			guiController.getMenuItemChkThisUserOnly().setSelected(currentUser != null);
		}
		catch (IOException ioe)
		{
			logger.log(Level.SEVERE, "Failed querying the registry for StartWithWindows values", ioe);
		}
	}

	public EventHandler<ActionEvent> handleStartWithWindowsClick(boolean allUsers, CheckMenuItem otherItem)
	{
		return ae ->
		{
			try
			{
				StartWithWindowsRegistryUtils.setRegistryToStartWithWindows(((CheckMenuItem) ae.getSource()).isSelected(), allUsers);
			}
			catch (IOException ioe)
			{
				logger.log(Level.SEVERE, "Unable to modify the registry for the \"start with Windows\" setting", ioe);
				new Alert(AlertType.ERROR, "Unable to modify the registry for the \"start with Windows\" setting").showAndWait();
			}

			otherItem.setSelected(false); //if one is selected, the other one has to be unselected. if the click is to de-select, the other one was already unselected as well anyway.	
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

	private void loadNICInfo(String nicDescription)
	{
		NICInfo nic = null;

		if (!nicDescription.isEmpty()) //we have a previously chosen NIC's description
		{
			nic = getNICByDescription(nicDescription);
			if (nic != null)
				selectedNIC = nic;
		}

		if (nic == null) //couldn't find the NIC
		{
			List<NICInfo> listOfDevices = sniffer.getListOfDevices();

			if (listOfDevices.size() == 1) //if there's only one option
				selectedNIC = listOfDevices.get(0);
			else
				showNICSelectionScreen();
		}
	}

	public void showNICSelectionScreen()
	{
		List<NICInfo> listOfDevices = sniffer.getListOfDevices();

		if (listOfDevices == null || listOfDevices.size() == 0)
		{
			new Alert(AlertType.ERROR, "Unable to find any network interfaces. Terminating application.").showAndWait();
			logger.log(Level.SEVERE, "Unable to find any network interfaces");
			guiController.shutdownApp();
		}

		Stage stage = guiController.getStage();

		if (selectedNIC == null)
			selectedNIC = new NICInfo();

		NICSelectionScreen selectionScreen = null;

		try
		{
			selectionScreen = new NICSelectionScreen(NICSelectionFormLocation, stage, stage.getScene(), selectedNIC);
		}
		catch (Exception e)
		{
			new Alert(AlertType.ERROR, "Unable to load network adapter selection screen. Terminating application.").showAndWait();
			logger.log(Level.SEVERE, "Unable to load network adapter selection screen", e);
			guiController.shutdownApp();
		}

		Stage newStage = selectionScreen.showScreenOnNewStage("Choose a network adapter", Modality.APPLICATION_MODAL, selectionScreen.getCloseButton());

		newStage.setOnCloseRequest(windowEvent ->
		{
			if (selectedNIC.getDescription() == null) //if we don't have a NIC set
			{
				windowEvent.consume();
				new Alert(AlertType.ERROR, "You must select a network adapter.").showAndWait();
			}
		});
	}

	private NICInfo getNICByDescription(String description)
	{
		for (NICInfo nic : sniffer.getListOfDevices())
			if (description.equals(nic.getDescription()))
				return nic;

		return null;
	}

	public NICInfo getSelectedNIC()
	{
		return selectedNIC;
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
}
