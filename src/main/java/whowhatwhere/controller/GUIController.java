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

import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import javafx.application.Platform;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import numbertextfield.NumberTextField;
import whowhatwhere.Main;
import whowhatwhere.controller.appearancecounter.AppearanceCounterController;
import whowhatwhere.controller.appearancecounter.AppearanceCounterUI;
import whowhatwhere.controller.utilities.QuickPingController;
import whowhatwhere.controller.utilities.QuickPingUI;
import whowhatwhere.controller.utilities.TraceUtilityController;
import whowhatwhere.controller.utilities.TraceUtilityUI;
import whowhatwhere.controller.watchdog.WatchdogController;
import whowhatwhere.controller.watchdog.WatchdogUI;
import whowhatwhere.model.TextToSpeech;
import whowhatwhere.model.networksniffer.NICInfo;
import whowhatwhere.model.networksniffer.NetworkSniffer;

public class GUIController
{
	public enum CommonGraphicImages 
	{
		OK		("/buttonGraphics/Ok.png"),
		CANCEL	("/buttonGraphics/Cancel.png"),
		ADD		("/buttonGraphics/Add.png"),
		EDIT	("/buttonGraphics/Edit.png"),
		REMOVE	("/buttonGraphics/Delete.png"),
		UP		("/buttonGraphics/Up.png"),
		DOWN	("/buttonGraphics/Down.png"),
		LOAD	("/buttonGraphics/Load.png"),
		SAVE	("/buttonGraphics/Save.png"),
		STOP	("/buttonGraphics/Stop.png"),
		TOOLTIP	("/buttonGraphics/Help.png"),
		HOTKEY	("/buttonGraphics/Keyboard.png");
		
		private String imageLocation;
		
		private CommonGraphicImages(String location) { imageLocation = location; }
		
		public String getLocation() { return imageLocation; }
	}
	
	private final static Logger logger = Logger.getLogger(GUIController.class.getPackage().getName());
	
	
	private final static String applicationIcon16Location = "/appIcons/www16.jpg";
	private final static String exitImageLocation = "/buttonGraphics/exit.png";
	private final static String textColorForValidText = "black"; 
	private final static String backgroundColorForValidText = "white";
	private final static String textColorForInvalidText = "#b94a48";
	private final static String backgroundColorForInvalidText = "#f2dede";
	public final static String voiceForTTS = "kevin16";

	@FXML
	private AnchorPane paneRoot;
	@FXML
	private Button btnExit;
	@FXML
	private MenuItem menuItemMinimize;
	@FXML
	private MenuItem menuItemExit;
	@FXML
	private MenuItem menuItemUpdate;
	@FXML
	private MenuItem menuItemAbout;
	@FXML
	private TabPane tabPane;
	@FXML
	private Tab tabWWW;
	@FXML
	private Tab tabWatchdog;
	@FXML
	private Tab tabUtils;
	@FXML
	private MenuItem menuItemSelectNIC;
	@FXML
	private CheckMenuItem menuItemChkCheckUpdateStartup;
	@FXML
	private CheckMenuItem menuItemChkDisplayBalloon;
	@FXML
	private CheckMenuItem menuItemChkStartMinimized;
	@FXML
	private CheckMenuItem menuItemChkThisUserOnly;
	@FXML
	private CheckMenuItem menuItemChkAllUsers;
	@FXML
	private MenuItem menuItemManageNotes;
	@FXML
	private AppearanceCounterController appearanceCounterPaneController;
	@FXML
	private QuickPingController quickPingPaneController;
	@FXML
	private WatchdogController watchdogPaneController;
	@FXML
	private TraceUtilityController tracePaneController;


	private NetworkSniffer sniffer;
	private TextToSpeech tts = new TextToSpeech(voiceForTTS);
	private HotkeyRegistry hotkeyRegistry;
	private boolean isExitAlreadyAddedToSystray = false;
	private IPNotes ipNotes;
	private SettingsHandler settings;
	private List<LoadAndSaveSettings> instancesWithSettingsToHandle = new ArrayList<>();
	private Map<Tab, BooleanExpression> tabToBindExpression = new HashMap<>();
	private WebEngine engine;
	private ChangeListenerForUpdate changeListenerForUpdate;


	
	
	/**
	 * <b>MUST</b> be called after the stage is shown
	 */
	public void init()
	{
		try
		{
			sniffer = new NetworkSniffer();
		}
		catch (IllegalStateException ise)
		{
			if (ise.getMessage().contains("Can't find dependent libraries"))
			{
				Alert alert = new Alert(AlertType.ERROR, "Application cannot be started");
				alert.setHeaderText("WinPcap is not installed!"); 
				alert.getDialogPane().contentProperty().set(generateLabelAndLinkPane("Please download and install WinPcap from", "http://www.winpcap.org/install/default.htm", Font.getDefault().getSize()));
				alert.showAndWait();
			}
			else
				new Alert(AlertType.ERROR, "Critical error, application cannot be started:\n" + ise.getMessage()).showAndWait();

			shutdownApp();
		}

		hotkeyRegistry = new HotkeyRegistry(paneRoot);

		btnExit.setOnAction(e -> exitButtonPressed());

		addExitToSystrayIcon();

		ipNotes = new IPNotes();
		
		new AppearanceCounterUI(this);
		new QuickPingUI(this);
		new WatchdogUI(this);
		new TraceUtilityUI(this);
		
		settings = new SettingsHandler(this);
		settings.loadLastRunConfig(instancesWithSettingsToHandle);
		
		initMenuBar();
		
		setGraphicForLabeledControl(btnExit, exitImageLocation, ContentDisplay.LEFT);
		btnExit.setGraphicTextGap(8);

		if (settings.getCheckForUpdatesOnStartup())
			checkForUpdates(true); //only show a message if there is a new version
	}
	
	public AppearanceCounterController getAppearanceCounterController()
	{
		return appearanceCounterPaneController;
	}
	
	public QuickPingController getQuickPingController()
	{
		return quickPingPaneController;
	}
	
	public WatchdogController getWatchdogPaneController()
	{
		return watchdogPaneController;
	}
	
	public TraceUtilityController getTracePaneController()
	{
		return tracePaneController;
	}
	
	public void registerForSettingsHandler(LoadAndSaveSettings instace)
	{
		instancesWithSettingsToHandle.add(instace);
	}

	private void addExitToSystrayIcon()
	{
		if (!SystemTray.isSupported())
			return;

		SystemTray tray = SystemTray.getSystemTray();

		tray.addPropertyChangeListener("trayIcons", pce ->
		{
			TrayIcon[] trayIcons = tray.getTrayIcons();

			if (!isExitAlreadyAddedToSystray && trayIcons.length == 1) //only for the first time the systray icon appears in systray
			{
				PopupMenu popup = trayIcons[0].getPopupMenu();
				java.awt.MenuItem exit = new java.awt.MenuItem("Exit");

				exit.addActionListener(al -> exitButtonPressed());

				popup.addSeparator();
				popup.add(exit);

				isExitAlreadyAddedToSystray = true;
			}

			if (trayIcons.length == 1 && settings.getShowMessageOnMinimize())
				trayIcons[0].displayMessage("Minimized to tray", "Still running in the background, double click this icon to restore the window. Use the \"Exit\" button to exit.", MessageType.INFO);
		});
	}

	private void initMenuBar()
	{
		menuItemManageNotes.setOnAction(event -> ipNotes.openManageIPNotesScreen(getStage()));
		menuItemMinimize.setOnAction(event -> minimizeToTray());
		menuItemExit.setOnAction(event -> exitButtonPressed());

		menuItemSelectNIC.setOnAction(ae -> settings.showNICSelectionScreen());
		menuItemChkCheckUpdateStartup.setOnAction(ae -> settings.setCheckForUpdatesOnStartup(((CheckMenuItem) ae.getSource()).isSelected()));
		menuItemChkDisplayBalloon.setOnAction(ae -> settings.setShowMessageOnMinimize(((CheckMenuItem) ae.getSource()).isSelected()));
		menuItemChkAllUsers.setOnAction(settings.handleStartWithWindowsClick(true, menuItemChkThisUserOnly));
		menuItemChkThisUserOnly.setOnAction(settings.handleStartWithWindowsClick(false, menuItemChkAllUsers));

		menuItemUpdate.setOnAction(event -> checkForUpdates(false));
		menuItemAbout.setOnAction(event -> showAboutWindow());
	}
	
	public void minimizeToTray()
	{
		Stage stage = getStage();

		Event.fireEvent(stage, new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
	}

	private void exitButtonPressed()
	{
		try
		{
			settings.saveCurrentRunValuesToProperties(instancesWithSettingsToHandle);

			hotkeyRegistry.cleanup();
			sniffer.cleanup();
			tts.cleanup();
		}
		catch (Exception e) //just in case
		{
			logger.log(Level.SEVERE, "Exception while trying to close the program: ", e);
		}
		finally
		{
			shutdownApp();
		}
	}

	public void shutdownApp()
	{
		Platform.setImplicitExit(true); //was initially set to false when initializing the systray
		Platform.exit();
		System.exit(0); //needed because of the AWT SysTray		
	}

	private void showAboutWindow()
	{
		String appName = Main.getAppName();
		String version = Main.getReleaseVersion();
		String website = Main.getWebsite();

		Alert about = new Alert(AlertType.INFORMATION, "About " + appName);
		about.initOwner(getStage());
		about.setTitle("About " + appName);
		about.setHeaderText(appName + " version " + version);
		
		FlowPane infoPane = generateLabelAndLinkPane("For more information visit", website, Font.getDefault().getSize() + 2);
		infoPane.setPadding(new Insets(0, 0, 15, 0));
		
		FlowPane copyright = generateLabelAndLinkPane("Copyright (C) 2017  ck3ck3 ", "mailto:WhoWhatWhereInfo@gmail.com", Font.getDefault().getSize());
		Hyperlink mailLink = (Hyperlink) copyright.getChildren().get(1);
		mailLink.setText("WhoWhatWhereInfo@gmail.com");
		copyright.setPadding(new Insets(0, 0, 15, 0));
		
		VBox aboutVBox = new VBox();
		aboutVBox.getChildren().addAll(infoPane, copyright, getAttributionLinksForAboutDialog());
		about.getDialogPane().contentProperty().set(aboutVBox);
		about.getDialogPane().setPrefWidth(440);
		
		about.showAndWait();
	}
	
	private class ChangeListenerForUpdate implements ChangeListener<State>
	{
		private boolean silent;
		
		public ChangeListenerForUpdate(boolean silent)
		{
			this.silent = silent;
		}
		
		@Override
		public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue)
		{
			if (newValue == State.SUCCEEDED)
			{
				JSONObject jsonObject = new JSONObject(engine.getDocument().getDocumentElement().getTextContent());
				String version = (String) jsonObject.get("tag_name");
				String releaseNotes = (String) jsonObject.get("body");
				checkForUpdatesResult(silent, !version.equals(Main.getReleaseVersion()), version, releaseNotes);
			}
			else
				if (newValue == State.FAILED)
				{
					Throwable throwable = engine.getLoadWorker().getException();
					String errorMsg = throwable != null ? throwable.getMessage() : "Failed to check for updates";
					
					if (!silent)
					{
						Alert alert = new Alert(AlertType.ERROR);
						alert.setHeaderText("Unable to check for updates");
						alert.setContentText(errorMsg);
						alert.showAndWait();
					}

					logger.log(Level.SEVERE, "Failed to check for updates", throwable != null ? throwable : errorMsg);
				}
		}
		
		public void setSilent(boolean silent)
		{
			this.silent = silent;
		}
	}

	/**
	 * @param silent
	 *            - if true, a message will be shown only if there's a new
	 *            update. if false, a message will be shown regardless.
	 */
	private void checkForUpdates(boolean silent)
	{
		if (engine == null)
		{
			engine = new WebEngine();
			changeListenerForUpdate = new ChangeListenerForUpdate(silent);
			engine.getLoadWorker().stateProperty().addListener(changeListenerForUpdate);
		}
		else
			changeListenerForUpdate.setSilent(silent);
		
		engine.load(Main.getURLForLatestRelease());
	}

	private void checkForUpdatesResult(boolean silent, boolean newVersionExists, String latestVersion, String latestReleaseNotes)
	{
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Check for updates");
		alert.initOwner(getStage());

		if (newVersionExists)
		{
			alert.setHeaderText("New version available!");
			alert.getDialogPane().contentProperty().set(generateLabelAndLinkPane("Download the latest version (" + latestVersion + ") at", Main.getWebsite(), Font.getDefault().getSize() + 2));
			FlowPane flowPane = (FlowPane) alert.getDialogPane().getContent();
			flowPane.getChildren().add(new Label("\nLatest release notes:\n\n" + latestReleaseNotes));
		}
		else
		{
			alert.setHeaderText("No new updates available.");
			alert.setContentText("You are running the latest version.");
		}

		if (newVersionExists || !silent)
			alert.showAndWait();
	}
	
	private FlowPane generateLabelAndLinkPane(String text, String url, double fontSize)
	{
		Font font = new Font(fontSize);

		Label label = new Label(text);
		label.setFont(font);
		
		Hyperlink link = new Hyperlink(url);
		link.setFont(font);
		link.setOnAction(event -> Main.openInBrowser(url));

		FlowPane flowPane = new FlowPane(label, link);

		return flowPane;
	}
	
	private VBox getAttributionLinksForAboutDialog()
	{
		FlowPane iconsAttributionPane = generateLabelAndLinkPane("All icons (except for ", "http://icons8.com", Font.getDefault().getSize());
		Label labelToAdd = new Label(") are from");
		labelToAdd.setGraphic(new ImageView(new Image(applicationIcon16Location)));
		labelToAdd.setContentDisplay(ContentDisplay.LEFT);
		labelToAdd.setGraphicTextGap(2);
		iconsAttributionPane.getChildren().add(1, labelToAdd);
		
		FlowPane softwareAttributionPane = generateLabelAndLinkPane("Click", getClass().getResource(Main.attributionHTMLLocation).toString(), Font.getDefault().getSize());
		Hyperlink tempLink = (Hyperlink) softwareAttributionPane.getChildren().get(1);
		tempLink.setText("here");
		softwareAttributionPane.getChildren().add(new Label("to see which software libraries used in Who What Where."));
		
		VBox vbox = new VBox();
		vbox.getChildren().addAll(iconsAttributionPane, softwareAttributionPane);
		
		return vbox;
	}
	
	
	/**Sets the text-color and background-color for valid and invalid text
	 * @param fields - the {@code NumberTextField}s to apply this on
	 */
	public static void setNumberTextFieldValidationUI(NumberTextField... fields)
	{
		for (NumberTextField field: fields)
				field.setColorForText(textColorForValidText, backgroundColorForValidText, textColorForInvalidText, backgroundColorForInvalidText);
	}
	
	/**Sets the text-color and background-color for valid and invalid text, and if {@code parentTab} isn't null, sets all <i>other</i> tabs to be disabled when the {@code fields} are invalid 
	 * @param parentTab - the tab containing these {@code fields}
	 * @param fields - the {@code NumberTextField}s to apply this on
	 */
	public void setNumberTextFieldsValidationUI(Tab parentTab, NumberTextField... fields)
	{
		BooleanExpression andOfAllFields = new SimpleBooleanProperty(true);
		
		setNumberTextFieldValidationUI(fields);
		
		if (parentTab != null)
		{
			for (NumberTextField field: fields)
				andOfAllFields = andOfAllFields.and(field.getValidProperty());
		
			for (Tab tab : tabPane.getTabs())
				if (!tab.equals(parentTab))
				{
					BooleanExpression existingExpression = tabToBindExpression.get(tab);
					
					if (existingExpression != null)
						andOfAllFields = andOfAllFields.and(existingExpression);
					
					tabToBindExpression.put(tab, andOfAllFields);
					tab.disableProperty().bind(andOfAllFields.not());					
				}
		}
	}
	
	public static void setCommonGraphicOnLabeled(Labeled labeled, CommonGraphicImages image)
	{
		setGraphicForLabeledControl(labeled, image.getLocation(), image == CommonGraphicImages.TOOLTIP ? ContentDisplay.RIGHT : ContentDisplay.LEFT);
	}
	
	public static void setGraphicForLabeledControl(Labeled control, String imageLocation, ContentDisplay direction)
	{
		if (direction != null)
			control.setContentDisplay(direction);
		
		control.setGraphic(new ImageView(new Image(GUIController.class.getResourceAsStream(imageLocation))));
	}
	
	public Button getBtnExit()
	{
		return btnExit;
	}

	public TabPane getTabPane()
	{
		return tabPane;
	}

	public Tab getTabUtilities()
	{
		return tabUtils;
	}

	public Tab getTabWWW()
	{
		return tabWWW;
	}

	public Tab getTabWatchdog()
	{
		return tabWatchdog;
	}

	public HotkeyRegistry getHotkeyRegistry()
	{
		return hotkeyRegistry;
	}

	public Stage getStage()
	{
		return (Stage) paneRoot.getScene().getWindow();
	}
	
	public IPNotes getIPNotes()
	{
		return ipNotes;
	}
	
	public NetworkSniffer getSniffer()
	{
		return sniffer;
	}
	
	public NICInfo getSelectedNIC()
	{
		return settings.getSelectedNIC();
	}
	
	public CheckMenuItem getMenuItemChkStartMinimized()
	{
		return menuItemChkStartMinimized;
	}
	
	public CheckMenuItem getMenuItemChkCheckUpdateStartup()
	{
		return menuItemChkCheckUpdateStartup;
	}
	
	public CheckMenuItem getMenuItemChkDisplayBalloon()
	{
		return menuItemChkDisplayBalloon;
	}
	
	public CheckMenuItem getMenuItemChkAllUsers()
	{
		return menuItemChkAllUsers;
	}
	
	public CheckMenuItem getMenuItemChkThisUserOnly()
	{
		return menuItemChkThisUserOnly;
	}
}
