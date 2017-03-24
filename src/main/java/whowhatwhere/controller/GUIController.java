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
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import numbertextfield.NumberTextField;
import whowhatwhere.CheckForUpdateResult;
import whowhatwhere.Main;
import whowhatwhere.controller.appearancecounter.AppearanceCounterController;
import whowhatwhere.controller.appearancecounter.AppearanceCounterUI;
import whowhatwhere.controller.quickping.QuickPingController;
import whowhatwhere.controller.quickping.QuickPingUI;
import whowhatwhere.controller.visualtrace.VisualTraceController;
import whowhatwhere.controller.visualtrace.VisualTraceUI;
import whowhatwhere.controller.watchdog.WatchdogController;
import whowhatwhere.controller.watchdog.WatchdogUI;
import whowhatwhere.model.networksniffer.NICInfo;
import whowhatwhere.model.networksniffer.NetworkSniffer;
import whowhatwhere.model.tts.TTSVoice;

public class GUIController
{
	public enum CommonGraphicImages 
	{
		OK				("/buttonGraphics/Ok.png"),
		CANCEL			("/buttonGraphics/Cancel.png"),
		ADD				("/buttonGraphics/Add.png"),
		EDIT			("/buttonGraphics/Edit.png"),
		REMOVE			("/buttonGraphics/Delete.png"),
		UP				("/buttonGraphics/Up.png"),
		DOWN			("/buttonGraphics/Down.png"),
		LOAD			("/buttonGraphics/Load.png"),
		SAVE			("/buttonGraphics/Save.png"),
		STOP			("/buttonGraphics/Stop.png"),
		TOOLTIP			("/buttonGraphics/Help.png"),
		HOTKEY			("/buttonGraphics/Keyboard.png"),
		SPEAKER			("/buttonGraphics/Speaker.png"),
		VOICE_CONFIG	("/buttonGraphics/Voice Presentation.png");
		
		private String imageLocation;
		
		private CommonGraphicImages(String location) { imageLocation = location; }
		
		public String getLocation() { return imageLocation; }
	}
	
	private final static Logger logger = Logger.getLogger(GUIController.class.getPackage().getName());
	private final static String NICSelectionFormLocation = "/whowhatwhere/view/fxmls/maingui/NICSelectionForm.fxml";
	private final static String TTSSelectionFormLocation = "/whowhatwhere/view/fxmls/maingui/VoiceSelectionForm.fxml";
	
	private final static String applicationIcon16Location = "/appIcons/www16.jpg";
	private final static String textColorForValidText = "black"; 
	private final static String backgroundColorForValidText = "white";
	private final static String textColorForInvalidText = "#b94a48";
	private final static String backgroundColorForInvalidText = "#f2dede";
	
	public final static double defaultTooltipMaxWidth = 320.0;
	public final static double defaultFontSize = 12.0;
	public final static String defaultTTSVoiceName = TTSVoice.cmu_bdl_hsmm.getVoiceName();
	
	@FXML
	private AnchorPane paneRoot;
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
	private Tab tabQuickPing;
	@FXML
	private Tab tabVisualTrace;
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
	private VisualTraceController visualTracePaneController;
	@FXML
	private MenuItem menuItemTTSSelection;
	@FXML
	private CheckMenuItem menuItemChkMinimizeOnXBtn;


	private TrayIcon trayIcon;
	private Stage stage;
	private NetworkSniffer sniffer;
	private HotkeyRegistry hotkeyRegistry;
	private IPNotes ipNotes;
	private SettingsHandler settings;
	private List<LoadAndSaveSettings> instancesWithSettingsToHandle = new ArrayList<>();
	private Map<Tab, BooleanExpression> tabToBindExpression = new HashMap<>();
	private NICInfo selectedNIC;
	private boolean minimizeRequestCameFromXBtn = false;
	private ConfigurableTTS www;
	private ConfigurableTTS watchdog;
	private ConfigurableTTS quickPing;
	private VisualTraceUI visualTraceUI;
		
	
	/**
	 * <b>MUST</b> be called after the stage and scene have been set
	 */
	public void init()
	{
		try
		{
			sniffer = new NetworkSniffer();
		}
		catch (IllegalStateException ise)
		{
			String exceptionMsg = ise.getMessage();
			
			if (exceptionMsg != null && exceptionMsg.contains("Can't find dependent libraries"))
			{
				Alert alert = new Alert(AlertType.ERROR, "Application cannot be started");
				alert.setHeaderText("WinPcap is not installed!"); 
				alert.getDialogPane().setContent(generateLabelAndLinkPane("Please download and install WinPcap from", "http://www.winpcap.org/install/default.htm", Font.getDefault().getSize()));
				alert.showAndWait();
			}
			else
				new Alert(AlertType.ERROR, "Critical error, application cannot be started.\n" + (exceptionMsg != null ? exceptionMsg : "")).showAndWait();

			shutdownApp();
		}

		hotkeyRegistry = new HotkeyRegistry(paneRoot);

		initSysTray();

		ipNotes = new IPNotes();
		
		www = new AppearanceCounterUI(this);
		quickPing = new QuickPingUI(this);
		watchdog = new WatchdogUI(this);
		
		visualTraceUI = new VisualTraceUI(this);
		
		settings = new SettingsHandler(this);
		settings.loadLastRunConfig(instancesWithSettingsToHandle);
		
		initMenuBar();
		
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
	
	public void registerForSettingsHandler(LoadAndSaveSettings instace)
	{
		instancesWithSettingsToHandle.add(instace);
	}

	private void initMenuBar()
	{
		menuItemManageNotes.setOnAction(event -> ipNotes.openManageIPNotesScreen(getStage()));
		menuItemMinimize.setOnAction(event -> minimizeToTray());
		menuItemExit.setOnAction(event -> exitRequestedByUser());

		menuItemSelectNIC.setOnAction(ae -> showNICSelectionScreen());
		menuItemTTSSelection.setOnAction(ae -> showTTSSelectionScreen());
		menuItemChkStartMinimized.setOnAction(ae -> settings.setStartMinimized(((CheckMenuItem) ae.getSource()).isSelected()));
		menuItemChkCheckUpdateStartup.setOnAction(ae -> settings.setCheckForUpdatesOnStartup(((CheckMenuItem) ae.getSource()).isSelected()));
		menuItemChkMinimizeOnXBtn.setOnAction(ae -> 
		{
			boolean isSelected = ((CheckMenuItem) ae.getSource()).isSelected();
			setXBtnBehavior(isSelected);
			settings.setMinimizeOnXBtn(isSelected);
		});
		menuItemChkDisplayBalloon.setOnAction(ae -> settings.setShowMessageOnMinimize(((CheckMenuItem) ae.getSource()).isSelected()));
		menuItemChkAllUsers.setOnAction(settings.handleStartWithWindowsClick(true, menuItemChkThisUserOnly));
		menuItemChkThisUserOnly.setOnAction(settings.handleStartWithWindowsClick(false, menuItemChkAllUsers));

		menuItemUpdate.setOnAction(event -> checkForUpdates(false));
		menuItemAbout.setOnAction(event -> showAboutWindow());
	}
	
	public void minimizeToTray()
	{
		performMinimizeToTray.run();
	}

	private void exitRequestedByUser()
	{
		try
		{
			settings.saveCurrentRunValuesToProperties(instancesWithSettingsToHandle);

			hotkeyRegistry.cleanup();
			sniffer.cleanup();
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
		String email = Main.getEmail();
		String copyrightNotice = Main.getCopyrightNotice();

		Alert about = new Alert(AlertType.INFORMATION, "About " + appName);
		about.initOwner(getStage());
		about.setTitle("About " + appName);
		about.setHeaderText(appName + " version " + version);
		
		FlowPane infoPane = generateLabelAndLinkPane("For more information visit", website, Font.getDefault().getSize() + 2);
		infoPane.setPadding(new Insets(0, 0, 15, 0));
		
		FlowPane copyright = generateLabelAndLinkPane(copyrightNotice, "mailto:" + email, Font.getDefault().getSize());
		Hyperlink mailLink = (Hyperlink) copyright.getChildren().get(1);
		mailLink.setText(email);
		copyright.setPadding(new Insets(0, 0, 15, 0));
		
		VBox aboutVBox = new VBox();
		aboutVBox.getChildren().addAll(infoPane, copyright, getAttributionLinksForAboutDialog());
		about.getDialogPane().setContent(aboutVBox);
		about.getDialogPane().setPrefWidth(440);
		
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
			CheckForUpdateResult result = Main.checkForUpdate();
			Platform.runLater(() -> showCheckForUpdateResult(silent, result));
		}).start();
	}
	
	private void showCheckForUpdateResult(boolean silent, CheckForUpdateResult result)
	{
		if (!result.isCheckSuccessful())
		{
			if (!silent)
			{
				Alert alert = new Alert(AlertType.ERROR);
				alert.setHeaderText("Unable to check for updates");
				alert.setContentText(result.getErrorMessage());
				alert.showAndWait();
			}
		}
		else
		{
			boolean updateAvailable = result.isUpdateAvailable();
			
			if (!silent || updateAvailable)
			{
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Check for Updates");
				alert.initOwner(getStage());
				
				if (updateAvailable)
				{
					alert.setHeaderText("New version available!");
					FlowPane flowPane = generateLabelAndLinkPane("Download the latest version (" + result.getNewVersion() + ") at", Main.getWebsite(), Font.getDefault().getSize() + 2);
					flowPane.getChildren().add(new Label("\nLatest release notes:\n\n" + result.getReleaseNotes()));
					alert.getDialogPane().setContent(flowPane);
				}
				else
				{
					alert.setHeaderText("No new updates available.");
					alert.setContentText("You are running the latest version.");
				}
				
				alert.showAndWait();
			}
		}		
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
		iconsAttributionPane.getChildren().add(1, labelToAdd); //add the label in the middle of the message
		
		FlowPane softwareAttributionPane = generateLabelAndLinkPane("Click", getClass().getResource(Main.attributionHTMLLocation).toString(), Font.getDefault().getSize());
		Hyperlink tempLink = (Hyperlink) softwareAttributionPane.getChildren().get(1);
		tempLink.setText("here");
		softwareAttributionPane.getChildren().add(new Label("to see which software libraries are used in Who What Where."));
		
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
	
	public void showNICSelectionScreen()
	{
		List<NICInfo> listOfDevices = sniffer.getListOfDevicesWithIP();

		if (listOfDevices == null || listOfDevices.size() == 0)
		{
			new Alert(AlertType.ERROR, "Unable to find any network interfaces. Terminating application.").showAndWait();
			logger.log(Level.SEVERE, "Unable to find any network interfaces");
			shutdownApp();
		}

		Stage stage = getStage();

		if (selectedNIC == null)
			selectedNIC = new NICInfo(); //the selected NICInfo will be copied into this object

		NICSelectionScreen selectionScreen = null;

		try
		{
			selectionScreen = new NICSelectionScreen(NICSelectionFormLocation, stage, stage.getScene(), selectedNIC);
		}
		catch (Exception e)
		{
			new Alert(AlertType.ERROR, "Unable to load network adapter selection screen. Terminating application.").showAndWait();
			logger.log(Level.SEVERE, "Unable to load network adapter selection screen", e);
			shutdownApp();
		}

		Stage newStage = selectionScreen.showScreenOnNewStage("Choose a Network Adapter", Modality.APPLICATION_MODAL, selectionScreen.getCloseButton());

		newStage.setOnCloseRequest(windowEvent ->
		{
			if (selectedNIC.getDescription() == null) //if we don't have a NIC set
			{
				windowEvent.consume();
				new Alert(AlertType.ERROR, "You must select a network adapter.").showAndWait();
			}
		});
	}
	
	public void showTTSSelectionScreen()
	{
		VoiceSelectionScreen selectionScreen = null;
		Stage stage = getStage();

		try
		{
			selectionScreen = new VoiceSelectionScreen(TTSSelectionFormLocation, stage, stage.getScene(), www, watchdog, quickPing);
		}
		catch (Exception e)
		{
			new Alert(AlertType.ERROR, "Unable to load voice selection screen.").showAndWait();
			logger.log(Level.SEVERE, "Unable to load voice screen", e);
		}
		
		selectionScreen.showScreenOnNewStage("Choose Text to Speech Voices", Modality.APPLICATION_MODAL, selectionScreen.getCloseButton());
	}
	
	public void setXBtnBehavior(boolean minimize)
	{
		if (!minimize)
			stage.setOnCloseRequest(we -> exitRequestedByUser());
		else
			if (!SystemTray.isSupported())
				logger.log(Level.WARNING, "Minimize to tray is not supported.");
			else
			{
				stage.setOnCloseRequest(we ->
				{
					we.consume(); //ignore the application's title window exit button, instead minimize to systray
					minimizeRequestCameFromXBtn = true;
					performMinimizeToTray.run();
				});
			}
		
		menuItemChkDisplayBalloon.setDisable(!minimize);
	}
	
	private void initSysTray()
	{
		stage.getIcons().addAll(Main.appIconList);
		java.awt.Image image = Toolkit.getDefaultToolkit().getImage(Main.class.getResource(Main.iconResource16));

		Platform.setImplicitExit(false); //needed to keep the app running while minimized to tray

		trayIcon = new TrayIcon(image, Main.getAppName());

		Runnable restoreApplication = () ->
		{
			stage.show();
			 SystemTray.getSystemTray().remove(trayIcon);
		};

		trayIcon.addActionListener(ae -> Platform.runLater(restoreApplication));

		java.awt.PopupMenu popupMenu = new PopupMenu();
		
		java.awt.MenuItem restore = new java.awt.MenuItem("Restore");
		restore.addActionListener(al -> Platform.runLater(restoreApplication));
		popupMenu.add(restore);
		
		java.awt.MenuItem exit = new java.awt.MenuItem("Exit");
		exit.addActionListener(al -> exitRequestedByUser());
		popupMenu.addSeparator();
		popupMenu.add(exit);

		trayIcon.setPopupMenu(popupMenu);
	}
	
	private Runnable performMinimizeToTray = () ->
	{
		Platform.runLater(() ->
		{
			try
			{
				SystemTray.getSystemTray().add(trayIcon);
				stage.hide();
				if (minimizeRequestCameFromXBtn && settings.getShowMessageOnMinimize())
					trayIcon.displayMessage(Main.getAppName() + " is running in the background", "Double click this icon to restore the window. To exit the program, right click this icon and choose Exit"
							+ ", or use the File menu. You can configure the behavior of the X button through the Options menu.", MessageType.INFO);
				
				minimizeRequestCameFromXBtn = false;
			}
			catch (Exception e)
			{
				logger.log(Level.WARNING, "Unable to minimize to tray", e);
			}
		});
	};
	
	public TabPane getTabPane()
	{
		return tabPane;
	}

	public Tab getTabQuickPing()
	{
		return tabQuickPing;
	}
	
	public Tab getVisualTraceTab()
	{
		return tabVisualTrace;
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
		return stage;
	}
	
	public void setStage(Stage stage)
	{
		this.stage = stage;
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
		return selectedNIC;
	}
	
	public void setSelectedNIC(NICInfo nic)
	{
		selectedNIC = nic;
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
	
	public CheckMenuItem getMenuItemChkMinimizeOnXBtn()
	{
		return menuItemChkMinimizeOnXBtn;
	}
	
	public VisualTraceController getVisualTraceController()
	{
		return visualTracePaneController;
	}
	
	public VisualTraceUI getVisualTraceUI()
	{
		return visualTraceUI;
	}
}
