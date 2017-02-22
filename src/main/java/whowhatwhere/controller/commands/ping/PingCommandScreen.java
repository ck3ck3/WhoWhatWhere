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
package whowhatwhere.controller.commands.ping;

import java.io.IOException;

import javafx.scene.Scene;
import javafx.stage.Stage;
import whowhatwhere.controller.commands.CommandScreen;

public class PingCommandScreen extends CommandScreen
{
	public PingCommandScreen(Stage stage, Scene scene, String ip) throws IOException
	{
		this(stage, scene, ip, "");
	}

	public PingCommandScreen(Stage stage, Scene scene, String ip, String parameters) throws IOException
	{
		super(stage, scene);
		
		setStageOnShowing(windowEvent ->
		{
			Stage currentStage = (Stage) getTextArea().getScene().getWindow();
			
			currentStage.setWidth(400);
			currentStage.setHeight(300);	
		});

		setCommandStr("ping " + parameters + " " + ip);
	}
}
