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
package whowhatwhere.view.secondaryfxmlscreen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import whowhatwhere.controller.SecondaryFXMLWithCRUDTableController;


/**
 * This class represents a secondary FXML screen that has a table with CRUD abilities, add/remove/close buttons and editable cells for data input.
 * This class must be used together with a controller class that inherits from {@code SecondaryFXMLWithCRUDTableController<T>}.<br>
 * That controller is defined in this abstract class as<br> 
 * {@code protected SecondaryFXMLWithCRUDTableController<T> controller;}<br>
 *  and assigned in the constructor: {@code controller = initController();}<br>
 *  <b>The inheriting class must call {@code initGUI()} in its constructor </b> in order to initialize the GUI. 
 *  This is not done in this class' constructor in order to allow to initialize the inheriting class' members first, so they can be used in methods like {@code getInitialTableItems(), setOnEditCommit()}.
 * @param <T> - the type of the table's data model.
 */
public abstract class SecondaryFXMLWithCRUDTableScreen<T> extends SecondaryFXMLScreen
{
	protected SecondaryFXMLWithCRUDTableController<T> controller;
	protected TableView<T> table;
	
	/**
	 * 
	 * @param fxmlLocation
	 *            - location of the fxml file relative to the resources dir
	 * @param stage
	 *            - the stage to come back to after this window is closed
	 * @param scene
	 *            - the scene to come back to after this window is closed
	 * @throws IOException
	 *             - if an error occurred while trying to load the fxml
	 */
	public SecondaryFXMLWithCRUDTableScreen(String fxmlLocation, Stage stage, Scene scene) throws IOException
	{
		super(fxmlLocation, stage, scene);
		
		controller = initController();
		table = controller.getTable();
		
		setButtonActionHandlersToCheckGUIInit(); //if initGUI() will be called from the deriving class, it will override these handlers. Otherwise, these handlers will throw an exception about not calling initGUI()
	}
	
	private void setButtonActionHandlersToCheckGUIInit() throws IllegalStateException
	{
		String errorMessage = "The method initGUI() was not called! It must be called by the deriving class' constructor before the GUI can be used.";
		
		controller.getBtnAddRow().setOnAction(event -> {throw new IllegalStateException(errorMessage);});
		controller.getBtnRemoveRow().setOnAction(event -> {throw new IllegalStateException(errorMessage);});
		controller.getCloseButton().setOnAction(event -> {throw new IllegalStateException(errorMessage);});
	}
	
	/**
	 * Typically implemented as follows:<br> {@code return getLoader().<ControllerClass> getController();}<br>
	 * where {@code ControllerClass} is the implemented controller class that inherits from {@code SecondaryFXMLWithCRUDTableController<T>}
	 * <br><b>Note:</b> Since this method is called from the parent class' constructor ({@code super(...)}),  it cannot make use of any of the inheriting class' members, since they haven't been initialized yet.
	 * @return The controller for the implemented class
	 */
	protected abstract SecondaryFXMLWithCRUDTableController<T> initController();
	
	/**
	 * This method sets the initial items in the table.<br>
	 * If the table should be empty on startup, this method should return an empty {@code ObservableList<T>}, for example {@code FXCollections.observableArrayList();}
	 * @return An ObservableList<T> that contains the items that should be displayed in the table when the screen loads
	 */
	protected abstract ObservableList<T> getInitialTableItems();
	
	/**
	 * Deriving classes should implement the required behavior for onEditStart/onEditCancel/onEditCommit for editable columns.
	 */
	protected abstract void setOnEditHandlers();
	
	/**
	 * @return An object of type T that represents an empty row in the table, that the user will later edit to input data
	 */
	protected abstract T newEmptyTableRow();
	
	/**
	 * Given a list of the rows that were selected for deletion, this method may filter these rows by deleting items in {@code initialSelectionOfRowsToDelete}.
	 * Only rows that remain in this list will be deleted from the table.<br>
	 * If this list is empty after this method returns, an error message will be shown to the user with the {@code return}ed String.<br>
	 * <b>This method is "optional".</b> If you do not wish to filter the rows to be deleted, just give this method a trivial implementation like { {@code return null;} }
	 * @param initialSelectionOfRowsToDelete - list of rows that were selected for deletion
	 * @return A string to be shown in an error message in case the list is filtered completely and is now empty. 
	 */
	protected abstract String filterRowsToDelete(ObservableList<T> initialSelectionOfRowsToDelete);
	
	/**
	 * This method should implement any "housekeeping" involved with the deletion of the rows in {@code listOfDeletedRows}.<br>
	 * The rows will be removed from the table's data model (and hence visually as well) by the button handler in the abstract class. <br>
	 * <b>This method is only for anything extra that needs to happen when rows are deleted</b>, for example removing these rows from another data structure.<br>
	 * If only removal from the table is required, this method can have an empty implementation.    
	 * @param listOfDeletedRows - a <b>read-only</b> list of the items that were selected for deletion and passed the filtration by {@link #filterRowsToDelete(ObservableList)}
	 */
	protected abstract void performForDeleteRows(List<T> listOfDeletedRows);
	
	/**
	 * This method will be executed when the user clicks on the Close button. <b>This method doesn't need to implement closing the window</b>, that is done by {@code SecondaryFXMLScreen}.<br>
	 * If the window should not be closed (for example if there's an input error on a form), this method should {@code throw} an {@code IllegalArgumentException}.  
	 */
	protected abstract void performOnCloseButton() throws IllegalArgumentException;
	
	
	public Button getCloseButton()
	{
		return controller.getCloseButton();
	}
	
	/**
	 * <b>Must be called by the deriving class' constructor</b> in order to initialize the GUI (set initial table items, button handlers, onEditCommit etc)
	 */
	protected void initGUI()
	{
		ArrayList<TableColumn<T, ?>> sortOrder = new ArrayList<TableColumn<T, ?>>(table.getSortOrder());
		
		table.setItems(getInitialTableItems());
		table.getSortOrder().addAll(sortOrder); //restore the sort order, if existed (setItems removes that property)

		initButtonHandlers();
		setOnEditHandlers();
	}
	
	/**
	 * Sets the behavior for clicking the Add/Remove/Close buttons.
	 */
	protected void initButtonHandlers()
	{
		controller.getCloseButton().setOnAction(event -> performOnCloseButton());
		controller.getBtnAddRow().setOnAction(event -> table.getItems().add(newEmptyTableRow()));
		controller.getBtnRemoveRow().setOnAction(event ->
		{
			ObservableList<T> selectedItems = table.getSelectionModel().getSelectedItems();

			if (selectedItems.isEmpty())
			{
				new Alert(AlertType.ERROR, "No lines selected.").showAndWait();
				return;
			}
			
			String errorIfEmpty = filterRowsToDelete(selectedItems);
			
			if (selectedItems.isEmpty())
			{
				new Alert(AlertType.ERROR, errorIfEmpty).showAndWait();
				return;				
			}

			performForDeleteRows(Collections.unmodifiableList(selectedItems));
			
			table.getItems().removeAll(selectedItems);
			table.getSelectionModel().clearSelection();
		});
	}
}
