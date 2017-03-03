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

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

/**
 * This class represents a controller for a secondary FXML screen that has a CRUD table with add/remove buttons and editable cells for data input, and a close button.
 * This class must be used together with a screen class that inherits from {@code SecondaryFXMLWithEditableCRUDTableScreen<T>}.
 * @param <T> - the type of the table's data model.
 */
public abstract class SecondaryFXMLWithEditableCRUDTableController<T> implements Initializable
{
	public abstract void initialize(URL location, ResourceBundle resources);
	
	public abstract TableView<T> getTable();
	
	public abstract Button getBtnAddRow();

	public abstract Button getBtnRemoveRow();

	public abstract Button getCloseButton();

}
