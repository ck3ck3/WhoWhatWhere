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

import java.io.IOException;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import whowhatwhere.model.command.CommmandLiveOutput;
import whowhatwhere.model.command.LiveOutputListener;
import whowhatwhere.view.secondaryfxmlscreen.SecondaryFXMLScreen;

public abstract class CommandScreen extends SecondaryFXMLScreen implements LiveOutputListener
{
	protected final static String commandFormLocation = "/whowhatwhere/view/fxmls/commands/CommandForm.fxml";
	protected final static Logger logger = Logger.getLogger(CommandScreen.class.getPackage().getName());

	private String commandStr;

	private CmdGUIController cmdController;
	private CommmandLiveOutput cmdLiveOutput;

	private StringBuilder output = new StringBuilder();
	private boolean outputReady = false;

	public CommandScreen(Stage stage, Scene scene) throws IOException
	{
		this(stage, scene, "");
	}

	public CommandScreen(Stage stage, Scene scene, String commandStr) throws IOException
	{
		super(commandFormLocation, stage, scene);
		this.setCommandStr(commandStr);
		cmdController = getLoader().<CmdGUIController> getController();
		cmdController.getHboxBottom().setSpacing(10);
	}

	public void runCommand()
	{
		cmdLiveOutput = new CommmandLiveOutput(commandStr, this);
		cmdLiveOutput.runCommand();
	}
	
	public void stopCommand()
	{
		cmdLiveOutput.stopCommand();
	}

	@Override
	public void lineReady(String line)
	{
		Platform.runLater(() ->
		{
			cmdController.getTextAreaOutput().appendText(line + "\n");
			output.append(line + "\n");
		});
	}

	@Override
	public void endOfOutput()
	{
		outputReady = true;
	}

	public TextArea getTextArea()
	{
		return cmdController.getTextAreaOutput();
	}

	public HBox getBottomHBox()
	{
		return cmdController.getHboxBottom();
	}

	public Button getCloseButton()
	{
		return cmdController.getBtnClose();
	}

	public String getCommandStr()
	{
		return commandStr;
	}

	public void setCommandStr(String commandStr)
	{
		this.commandStr = commandStr;
	}

	public String getOutput()
	{
		return output.toString();
	}

	public boolean isOutputReady()
	{
		return outputReady;
	}

}
