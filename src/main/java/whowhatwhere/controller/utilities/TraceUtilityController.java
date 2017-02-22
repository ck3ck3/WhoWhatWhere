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
package whowhatwhere.controller.utilities;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TextField;
import whowhatwhere.controller.GUIController;

public class TraceUtilityController implements Initializable
{
	private final static String traceImageLocation = "/buttonGraphics/Globe-Earth.png";
	
	@FXML
	private TextField textTrace;
	@FXML
	private Button btnTrace;
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		GUIController.setGraphicForLabeledControl(btnTrace, traceImageLocation, ContentDisplay.LEFT);
	}
	
	public TextField getTextTrace()
	{
		return textTrace;
	}
	
	public Button getBtnTrace()
	{
		return btnTrace;
	}
}
