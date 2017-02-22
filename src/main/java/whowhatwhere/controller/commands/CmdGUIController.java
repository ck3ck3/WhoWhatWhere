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
package whowhatwhere.controller.commands;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import whowhatwhere.controller.GUIController;

public class CmdGUIController implements Initializable
{
	@FXML
	private TextArea textAreaOutput;
	@FXML
	private Button btnClose;
	@FXML
	private HBox hboxBottom;
	
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		GUIController.setCommonGraphicOnLabeled(btnClose, GUIController.CommonGraphicImages.OK);
	}

	public TextArea getTextAreaOutput()
	{
		return textAreaOutput;
	}

	public HBox getHboxBottom()
	{
		return hboxBottom;
	}

	public Button getBtnClose()
	{
		return btnClose;
	}
}
