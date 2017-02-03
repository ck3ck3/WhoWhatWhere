package whowhatwhere.controller;

import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import whowhatwhere.Main;
import whowhatwhere.controller.appearancecounter.AppearanceCounterController;
import whowhatwhere.controller.utilities.PingToSpeechController;
import whowhatwhere.controller.utilities.TraceUtilityController;
import whowhatwhere.controller.watchdog.WatchdogController;
import whowhatwhere.model.TextToSpeech;
import whowhatwhere.model.networksniffer.NICInfo;
import whowhatwhere.model.networksniffer.NetworkSniffer;

public class GUIController implements CheckForUpdatesResultHandler
{
	private final static String voiceForTTS = "kevin16";

	private final static Logger logger = Logger.getLogger(GUIController.class.getPackage().getName());

	@FXML
	private ScrollPane scrollPaneMainForm;
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
	private PingToSpeechController ptsPaneController;
	@FXML
	private WatchdogController watchdogPaneController;
	@FXML
	private TraceUtilityController tracePaneController;


	private NetworkSniffer sniffer;
	private TextToSpeech tts = new TextToSpeech(voiceForTTS);
	private HotkeyRegistry hotkeyRegistry;
	private boolean isExitAlreadyAddedToSystray = false;
	private UserNotes userNotes;
	private SettingsHandler settings;
	
	
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
				generateLabelAndLinkAlert(AlertType.ERROR, "Application cannot be started", "WinPcap is not installed!", "Please download and install WinPcap from",
						"http://www.winpcap.org/install/default.htm").showAndWait();
			else
				new Alert(AlertType.ERROR, "Critical error, application cannot be started:\n" + ise.getMessage()).showAndWait();

			shutdownApp();
		}

		hotkeyRegistry = new HotkeyRegistry(scrollPaneMainForm);

		btnExit.setOnAction(e -> exitButtonPressed());

		addExitToSystrayIcon();

		userNotes = new UserNotes();
		
		settings = new SettingsHandler(this);
		settings.loadLastRunConfig();
		
		initMenuBar();

		if (settings.getCheckForUpdatesOnStartup())
			checkForUpdates(true); //only show a message if there is a new version
	}
	
	public AppearanceCounterController getAppearanceCounterController()
	{
		return appearanceCounterPaneController;
	}
	
	public PingToSpeechController getPingToSpeechController()
	{
		return ptsPaneController;
	}
	
	public WatchdogController getWatchdogPaneController()
	{
		return watchdogPaneController;
	}
	
	public TraceUtilityController getTracePaneController()
	{
		return tracePaneController;
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
		menuItemManageNotes.setOnAction(event -> userNotes.openManageUserNotesScreen(getStage()));
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
			settings.saveCurrentRunValuesToProperties();

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

		Alert about = generateLabelAndLinkAlert(AlertType.INFORMATION, "About " + appName, appName + " version " + version, "For more information visit ", website);

		about.showAndWait();
	}

	/**
	 * @param silent
	 *            - if true, a message will be shown only if there's a new
	 *            update. if false, a message will be shown regardless.
	 */
	private void checkForUpdates(boolean silent)
	{
		new Thread(() ->
		{
			try
			{
				Main.isUpdateAvailable(this, silent);
			}
			catch (IOException e)
			{
				if (!silent)
				{
					Platform.runLater(() ->
					{
						Alert alert = new Alert(AlertType.ERROR);
						alert.setHeaderText("Unable to check for updates");
						alert.setContentText(e.getMessage());
						alert.showAndWait();
					});
				}

				logger.log(Level.SEVERE, "Failed to check for updates", e);
			}
		}).start();
	}

	@Override
	public void checkForUpdatesResult(boolean newVersionExists, boolean silent)
	{
		Platform.runLater(() ->
		{
			Alert alert;

			if (newVersionExists)
				alert = generateLabelAndLinkAlert(AlertType.INFORMATION, "Check for updates", "New version available!", "Download the new version at ", Main.getWebsite());
			else
			{
				alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Check for updates");
				alert.setHeaderText("No new updates available.");
				alert.setContentText("You are running the latest version.");
			}

			if (newVersionExists || !silent)
				alert.showAndWait();
		});
	}

	private Alert generateLabelAndLinkAlert(AlertType type, String title, String header, String text, String url)
	{
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(header);

		FlowPane fp = new FlowPane();
		Label lbl = new Label(text);
		Hyperlink link = new Hyperlink(url);

		link.setOnAction(event ->
		{
			Main.openInBrowser(link.getText());
			alert.close();
		});

		fp.getChildren().addAll(lbl, link);

		alert.getDialogPane().contentProperty().set(fp);

		return alert;
	}

	public Button getBtnExit()
	{
		return btnExit;
	}

	public TabPane getTabPane()
	{
		return tabPane;
	}

	public Tab getUtilsTab()
	{
		return tabUtils;
	}

	public HotkeyRegistry getHotkeyRegistry()
	{
		return hotkeyRegistry;
	}

	public Stage getStage()
	{
		return (Stage) scrollPaneMainForm.getScene().getWindow();
	}
	
	public ScrollPane getScrollPane()
	{
		return scrollPaneMainForm;
	}
	
	public UserNotes getUserNotes()
	{
		return userNotes;
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