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

import java.util.Properties;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import whowhatwhere.controller.GUIController;
import whowhatwhere.controller.LoadAndSaveSettings;
import whowhatwhere.controller.commands.Commands;

public class TraceUtilityUI implements LoadAndSaveSettings
{
	private GUIController guiController;
	private TraceUtilityController controller;
	private TextField textTrace;
	private Button btnTrace;
	
	private final static String propsTraceAddress = "traceAddress";
	
	public TraceUtilityUI(GUIController guiController)
	{
		this.guiController = guiController;
		this.guiController.registerForSettingsHandler(this);
//		controller = guiController.getTracePaneController();
		textTrace = controller.getTextTrace();
		btnTrace = controller.getBtnTrace();
		
//		btnTrace.setOnAction(event -> Commands.traceCommand(textTrace.getText(), guiController.getStage()));

		textTrace.setOnKeyPressed(ke ->
		{
			if (ke.getCode().equals(KeyCode.ENTER))
				btnTrace.fire();
		});		
	}
	
	public void saveCurrentRunValuesToProperties(Properties props)
	{
		props.put(propsTraceAddress, textTrace.getText());
	}
	
	public void loadLastRunConfig(Properties props)
	{
		textTrace.setText(props.getProperty(propsTraceAddress));
	}
}
