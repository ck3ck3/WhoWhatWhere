package whowhatwhere.controller.watchdog;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.filefilter.WildcardFileFilter;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;
import javafx.stage.Stage;
import whowhatwhere.model.networksniffer.watchdog.PacketTypeToMatch;
import whowhatwhere.view.secondaryfxmlscreen.SecondaryFXMLScreen;

public class ManageListScreen extends SecondaryFXMLScreen
{
	private final static Logger logger = Logger.getLogger(ManageListScreen.class.getPackage().getName());
	
	private final static String watchdogListAddEditFormLocation = "/whowhatwhere/view/fxmls/watchdog/AddEditEntry.fxml";
	
	private ManageListController watchdogListController;
	private ObservableList<PacketTypeToMatch> entryList;
	private TableView<PacketTypeToMatch> table;
	private Map<String, List<String>> userNotesToIPListMap;

	
	public ManageListScreen(String fxmlLocation, Stage stage, Scene scene, WatchdogUI uiController) throws IOException
	{
		super(fxmlLocation, stage, scene);

		watchdogListController = getLoader().<ManageListController> getController();;
		entryList = uiController.getEntryList();
		
		table = watchdogListController.getTable();
		table.setItems(entryList);
		userNotesToIPListMap = uiController.getUserNotesReverseMap();
		
		initButtonHandlers();
		setTableRowDoubleClickToEdit();
	}
	
	private void setTableRowDoubleClickToEdit()
	{
		table.setRowFactory(param ->
		{
			TableRow<PacketTypeToMatch> row = new TableRow<>();
			row.setOnMouseClicked(event ->
			{
				if (event.getClickCount() == 2 && (!row.isEmpty()))
					watchdogListController.getBtnEditRow().fire();
			});

			return row;
		});
	}

	
	private void initButtonHandlers()
	{
		watchdogListController.getBtnAddRow().setOnAction(generateAddEditEventHandler(false));
		watchdogListController.getBtnEditRow().setOnAction(generateAddEditEventHandler(true));
		initRemoveEntryButton();
		
		initSavePresetButton();
		initLoadPresetButton();
	}
	
	private void initRemoveEntryButton()
	{
		watchdogListController.getBtnRemoveRow().setOnAction(event ->
		{
			ObservableList<PacketTypeToMatch> selectedItems = table.getSelectionModel().getSelectedItems();

			if (selectedItems.isEmpty())
			{
				new Alert(AlertType.ERROR, "No entries selected.").showAndWait();
				return;
			}
			
			table.getItems().removeAll(selectedItems);
			table.getSelectionModel().clearSelection();
		});
	}

	public Button getCloseButton()
	{
		return watchdogListController.getCloseButton();
	}
	
	private EventHandler<ActionEvent> generateAddEditEventHandler(boolean isEdit)
	{
		return new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				ListAddEditScreen watchdogListAddEditScreen;
				Stage stage = getPostCloseStage();
				
				if (isEdit)
				{
					int numOfSelectedRows = table.getSelectionModel().getSelectedIndices().size();
					String errorMsg = null;
					
					if (numOfSelectedRows == 0)
						errorMsg = "Please select an entry to edit";
					else
						if (numOfSelectedRows > 1)
							errorMsg = "Only one entry must be selected for edit";
					
					if (numOfSelectedRows != 1)
					{
						new Alert(AlertType.ERROR, errorMsg).showAndWait();
						return;
					}
				}

				try
				{
					watchdogListAddEditScreen = new ListAddEditScreen(watchdogListAddEditFormLocation, stage, stage.getScene(), table, userNotesToIPListMap, isEdit);
				}
				catch (IOException e)
				{
					logger.log(Level.SEVERE, "Unable to load watchdog list add/edit screen", e);
					return;
				}
				catch (IllegalStateException ise)
				{
					new Alert(AlertType.ERROR, ise.getMessage()).showAndWait();
					return;
				}

				Stage newStage = watchdogListAddEditScreen.showScreenOnNewStage((isEdit ? "Edit" : "Add") + " an entry", Modality.APPLICATION_MODAL, watchdogListAddEditScreen.getBtnDone(), watchdogListAddEditScreen.getBtnCancel());
				
				newStage.setOnCloseRequest(windowEvent ->
				{
					windowEvent.consume();
					watchdogListAddEditScreen.getBtnCancel().fire();
				});
			}
		};
	}

	private void initSavePresetButton()
	{
		watchdogListController.getBtnSavePreset().setOnAction(event ->
		{
			TextInputDialog dialog = new TextInputDialog();
			dialog.setTitle("Save Preset");
			dialog.setHeaderText("Save this preset for future use");
			dialog.setContentText("Please enter preset name:");

			Optional<String> result = dialog.showAndWait();

			result.ifPresent(filename ->
			{
				String fullName = filename + WatchdogUI.presetExtension;
				boolean alreadyExists = false;

				if (new File(fullName).exists()) //if filename already exists
				{
					Alert overwriteDialog = new Alert(AlertType.CONFIRMATION,
							"A preset with that name already exists. Press \"OK\" to overwrite the preset or \"Cancel\" to close this dialog without saving the new preset.");
					overwriteDialog.setTitle("Preset name already exists");
					overwriteDialog.setHeaderText("Overwrite existing preset?");

					Optional<ButtonType> overwriteResult = overwriteDialog.showAndWait();
					if (overwriteResult.get() == ButtonType.CANCEL)
						return;

					alreadyExists = true;
				}

				try
				{
					WatchdogUI.saveListToFile(entryList, fullName);
				}
				catch (IOException ioe)
				{
					new Alert(AlertType.ERROR, "Unable to save preset: " + ioe.getMessage()).showAndWait();
					return;
				}

				MenuItem menuItem = ManageListScreen.createMenuItem(entryList, filename);

				ObservableList<MenuItem> items = watchdogListController.getMenuBtnLoadPreset().getItems();

				if (alreadyExists)
					return;

				if (items.get(0).isDisable()) //it only contains the disabled "none found " item, remove it before adding new one
					items.clear();

				items.add(menuItem);
			});
		});
	}

	private void initLoadPresetButton()
	{
		ObservableList<MenuItem> items = watchdogListController.getMenuBtnLoadPreset().getItems();

		File dir = new File(System.getProperty("user.dir"));
		FileFilter fileFilter = new WildcardFileFilter("*" + WatchdogUI.presetExtension);
		List<File> files = new ArrayList<File>(Arrays.asList(dir.listFiles(fileFilter))); //ArrayList because asList() returns an immutable list

		if (files.removeIf(file -> file.getName().equals(WatchdogUI.lastRunFilename))) //if lastRun exists, remove it from the list and put it on top of the button's list
			items.add(createMenuItem(entryList, WatchdogUI.lastRunFilename.replace(WatchdogUI.presetExtension, "")));

		for (File file : files)
			items.add(createMenuItem(entryList, file.getName().replace(WatchdogUI.presetExtension, "")));

		if (items.isEmpty())
		{
			MenuItem none = new MenuItem("No presets found");

			none.setDisable(true);
			items.add(none);
		}
	}

	public static MenuItem createMenuItem(ObservableList<PacketTypeToMatch> list, String filename)
	{
		MenuItem menuItem = new MenuItem(filename);

		menuItem.setOnAction(event ->
		{
			try
			{
				WatchdogUI.loadListFromFile(list, filename + WatchdogUI.presetExtension);
			}
			catch (ClassNotFoundException | IOException e)
			{
				new Alert(AlertType.ERROR, "Unable to load preset: " + e.getMessage()).showAndWait();
			}
		});

		return menuItem;
	}
}
