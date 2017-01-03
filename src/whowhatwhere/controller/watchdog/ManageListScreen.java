package whowhatwhere.controller.watchdog;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
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
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import whowhatwhere.model.ipsniffer.firstsight.IPToMatch;
import whowhatwhere.view.SecondaryFXMLScreen;

public class ManageListScreen extends SecondaryFXMLScreen
{
	private final static String watchdogListAddEditFormLocation = "/whowhatwhere/view/WatchdogListAddEdit.fxml";
	private final static Logger logger = Logger.getLogger(ManageListScreen.class.getPackage().getName());

	private ListController watchdogListController;
	private WatchdogUI watchdogUIController;
	private TableView<IPToMatch> table;
	private ObservableList<IPToMatch> list;
	private TextField textToSay;

	public ManageListScreen(String fxmlLocation, Stage stage, Scene scene, WatchdogUI uiController) throws IOException
	{
		super(fxmlLocation, stage, scene);

		this.list = uiController.getEntryList();
		this.textToSay = uiController.getTextMessage();

		watchdogListController = getLoader().<ListController> getController();
		this.watchdogUIController = uiController;
		table = watchdogListController.getTable();

		table.setItems(list);

		initButtonHandlers();

		setTableRowDoubleClickToEdit();
	}

	private void setTableRowDoubleClickToEdit()
	{
		table.setRowFactory(param ->
		{
			TableRow<IPToMatch> row = new TableRow<>();
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
		watchdogListController.getBtnRemoveRow().setOnAction(event ->
		{
			ObservableList<IPToMatch> selectedItems = table.getSelectionModel().getSelectedItems();

			list.removeAll(selectedItems);
		});

		watchdogListController.getBtnSavePreset().setOnAction(event ->
		{
			TextInputDialog dialog = new TextInputDialog();
			dialog.setTitle("Save Preset");
			dialog.setHeaderText("Save this preset for future use");
			dialog.setContentText("Please enter preset name:");

			Optional<String> result = dialog.showAndWait();

			result.ifPresent(filename -> 
			{
	           	TextField textMessage = watchdogUIController.getTextMessage();
            	ObservableList<IPToMatch> entryList = watchdogUIController.getEntryList();
            	
				try
				{
					WatchdogUI.saveListToFile(entryList, textMessage.getText(), filename + WatchdogUI.presetExtension);
				}
				catch (IOException ioe)
				{
					new Alert(AlertType.ERROR, "Unable to save preset: " + ioe.getMessage()).showAndWait();
					return;
				}

				MenuItem menuItem = ManageListScreen.createMenuItem(entryList, textMessage, filename);
				
				ObservableList<MenuItem> items = watchdogListController.getMenuBtnLoadPreset().getItems();
				
				if (items.get(0).isDisable()) //it only contains the disabled "none found " item, remove it before adding new one
					items.clear();
					
				items.add(menuItem);
			});
		});

		initMenuButton();
	}

	private void initMenuButton()
	{
		ObservableList<MenuItem> items = watchdogListController.getMenuBtnLoadPreset().getItems();
		items.clear();

		File dir = new File(System.getProperty("user.dir"));
		FileFilter fileFilter = new WildcardFileFilter("*" + WatchdogUI.presetExtension);
		File[] files = dir.listFiles(fileFilter);

		for (File file : files)
			items.add(createMenuItem(list, textToSay, file.getName().replace(WatchdogUI.presetExtension, "")));
		
		if (items.isEmpty())
		{
			MenuItem none = new MenuItem("No presets found");
			
			none.setDisable(true);
			items.add(none);
		}
	}

	public static MenuItem createMenuItem(ObservableList<IPToMatch> list, TextField textToSay, String filename)
	{
		MenuItem menuItem = new MenuItem(filename);

		menuItem.setOnAction(event ->
		{
			try
			{
				WatchdogUI.loadListFromFile(list, textToSay, filename + WatchdogUI.presetExtension);
			}
			catch (ClassNotFoundException | IOException e)
			{
				new Alert(AlertType.ERROR, "Unable to load preset: " + e.getMessage()).showAndWait();
			}
		});
		
		return menuItem;
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

				try
				{
					watchdogListAddEditScreen = new ListAddEditScreen(watchdogListAddEditFormLocation, stage, stage.getScene(), table, isEdit);
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

				watchdogListAddEditScreen.showScreenOnNewStage((isEdit ? "Edit" : "Add") + " an entry", watchdogListAddEditScreen.getCloseButton());
			}
		};
	}

	public Button getCloseButton()
	{
		return watchdogListController.getBtnClose();
	}
}
