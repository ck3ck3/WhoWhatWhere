package whowhatwhere.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

/**
 * This class represents a controller for a secondary FXML screen that has a CRUD table with add/remove buttons and editable cells for data input, and a close button.
 * This class must be used together with a screen class that inherits from {@code SecondaryFXMLWithCRUDTableScreen<T>}.
 * @param <T> - the type of the table's data model.
 */
public abstract class SecondaryFXMLWithCRUDTableController<T> implements Initializable
{
	public abstract void initialize(URL location, ResourceBundle resources);
	
	public abstract TableView<T> getTable();
	
	public abstract Button getBtnAddRow();

	public abstract Button getBtnRemoveRow();

	public abstract Button getCloseButton();

}
