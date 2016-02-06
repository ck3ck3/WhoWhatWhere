package mostusedips.controller;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Callback;
import mostusedips.model.ipsniffer.IPToMatch;
import mostusedips.view.SecondaryFXMLScreen;

public class WatchdogManageListScreen extends SecondaryFXMLScreen
{
	private final static String watchdogListAddEditFormLocation = "/mostusedips/view/WatchdogListAddEdit.fxml";
	private final static Logger logger = Logger.getLogger(WatchdogManageListScreen.class.getPackage().getName());

	private WatchdogController watchdogController;

	public WatchdogManageListScreen(String fxmlLocation, Stage stage, Scene scene) throws IOException
	{
		super(fxmlLocation, stage, scene);

		watchdogController = getLoader().<WatchdogController> getController();

		initButtonHandlers();

		setTableRowDoubleClickToEdit();
	}

	private void setTableRowDoubleClickToEdit()
	{
		watchdogController.getTable().setRowFactory(new Callback<TableView<IPToMatch>, TableRow<IPToMatch>>()
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
		watchdogController.getBtnRemoveRow().setOnAction(new EventHandler<ActionEvent>(){

			@Override
			public void handle(ActionEvent event)
			{
				TableView<IPToMatch> table = watchdogController.getTable();
				
				ObservableList<IPToMatch> selectedItems = table.getSelectionModel().getSelectedItems();
				
				table.getItems().removeAll(selectedItems);
			}});
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
					watchdogListAddEditScreen = new WatchdogListAddEditScreen(watchdogListAddEditFormLocation, stage, stage.getScene(), watchdogController.getTable(), isEdit);
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

				watchdogListAddEditScreen.showScreenOnNewStage(watchdogListAddEditScreen.getCloseButton(), (isEdit ? "Edit" : "Add") + " an entry");
			}
		};
	}

	public Button getCloseButton()
	{
		return watchdogController.getBtnClose();
	}
}
