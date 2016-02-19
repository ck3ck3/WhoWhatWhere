package mostusedips.controller.watchdog;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
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
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Callback;
import mostusedips.model.ipsniffer.IPToMatch;
import mostusedips.view.SecondaryFXMLScreen;

public class WatchdogManageListScreen extends SecondaryFXMLScreen
{
	private final static String watchdogListAddEditFormLocation = "/mostusedips/view/WatchdogListAddEdit.fxml";
	private final static String watchdogSavePresetFormLocation = "/mostusedips/view/WatchdogSavePreset.fxml";
	private final static Logger logger = Logger.getLogger(WatchdogManageListScreen.class.getPackage().getName());

	private WatchdogController watchdogController;
	private TableView<IPToMatch> table;
	private ObservableList<IPToMatch> list;
	private TextField textToSay;
	private Label labelCounter;

	public WatchdogManageListScreen(String fxmlLocation, Stage stage, Scene scene, ObservableList<IPToMatch> list, TextField textToSay, Label labelCounter) throws IOException
	{
		super(fxmlLocation, stage, scene);

		this.list = list;
		this.textToSay = textToSay;
		this.labelCounter = labelCounter;

		watchdogController = getLoader().<WatchdogController> getController();
		table = watchdogController.getTable();

		table.setItems(list);

		initButtonHandlers();

		setTableRowDoubleClickToEdit();
	}

	private void setTableRowDoubleClickToEdit()
	{
		table.setRowFactory(new Callback<TableView<IPToMatch>, TableRow<IPToMatch>>()
		{
			@Override
			public TableRow<IPToMatch> call(TableView<IPToMatch> param)
			{
				TableRow<IPToMatch> row = new TableRow<IPToMatch>();
				row.setOnMouseClicked(new EventHandler<MouseEvent>()
				{
					@Override
					public void handle(MouseEvent event)
					{
						if (event.getClickCount() == 2 && (!row.isEmpty()))
							watchdogController.getBtnEditRow().fire();
					}
				});

				return row;
			}
		});
	}

	private void initButtonHandlers()
	{
		watchdogController.getBtnAddRow().setOnAction(generateAddEditEventHandler(false));
		watchdogController.getBtnEditRow().setOnAction(generateAddEditEventHandler(true));
		watchdogController.getBtnRemoveRow().setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				ObservableList<IPToMatch> selectedItems = table.getSelectionModel().getSelectedItems();

				list.removeAll(selectedItems);
			}
		});

		watchdogController.getBtnClose().setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				labelCounter.setText("Match list contains " + list.size() + " entries");
			}
		});

		watchdogController.getBtnSavePreset().setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				WatchdogSavePresetScreen screen;
				Stage stage = getPostCloseStage();

				try
				{
					screen = new WatchdogSavePresetScreen(watchdogSavePresetFormLocation, stage, stage.getScene(), list, textToSay, labelCounter, watchdogController.getMenuBtnLoadPreset());
				}
				catch (IOException e)
				{
					logger.log(Level.SEVERE, "Unable to load watchdog save preset screen", e);
					return;
				}

				screen.showScreenOnNewStage("Save preset", screen.getCloseButton());
			}
		});

		initMenuButton();
	}

	private void initMenuButton()
	{
		ObservableList<MenuItem> items = watchdogController.getMenuBtnLoadPreset().getItems();
		items.clear();

		File dir = new File(System.getProperty("user.dir"));
		FileFilter fileFilter = new WildcardFileFilter("*.watchdogPreset");
		File[] files = dir.listFiles(fileFilter);

		for (File file : files)
			items.add(createMenuItem(list, textToSay, labelCounter, file.getName().replace(".watchdogPreset", "")));
		
		if (items.isEmpty())
		{
			MenuItem none = new MenuItem("No presets found");
			
			none.setDisable(true);
			items.add(none);
		}
	}

	public static MenuItem createMenuItem(ObservableList<IPToMatch> list, TextField textToSay, Label labelCounter, String filename)
	{
		MenuItem menuItem = new MenuItem(filename);

		menuItem.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				try
				{
					WatchdogUI.watchdogLoadListFromFile(list, textToSay, labelCounter, filename + ".watchdogPreset");
				}
				catch (ClassNotFoundException | IOException e)
				{
					new Alert(AlertType.ERROR, "Unable to load preset: " + e.getMessage()).showAndWait();
				}
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
				WatchdogListAddEditScreen watchdogListAddEditScreen;
				Stage stage = getPostCloseStage();

				try
				{
					watchdogListAddEditScreen = new WatchdogListAddEditScreen(watchdogListAddEditFormLocation, stage, stage.getScene(), table, isEdit);
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
		return watchdogController.getBtnClose();
	}
}
