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
package whowhatwhere.controller.commands.trace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import whowhatwhere.controller.commands.CommandScreen;
import whowhatwhere.controller.utilities.TraceOutputReceiver;

public class TraceCommandScreen extends CommandScreen
{
	private Button btnStop = new Button("Stop trace");
	private boolean endedGracefully;

	private TraceOutputReceiver outputReceiver;
	
	
	public TraceCommandScreen(Stage stage, Scene scene, String ip, boolean resolveHostnames, Integer pingTimeout, TraceOutputReceiver outputReceiver) throws IOException
	{
		super(stage, scene);
		
		this.outputReceiver = outputReceiver; 
		
		endedGracefully = true;
		
		setCommandStr(generateCommandString(ip, resolveHostnames, pingTimeout));
		initTraceScreen();
	}	

	public void initTraceScreen()
	{
		getCloseButton().setVisible(false);	
		getBottomHBox().getChildren().add(btnStop);

		btnStop.setOnAction(event ->
		{
			endedGracefully = false;
			btnStop.setDisable(true);
			stopCommand();
		});
	}
	
	public void setOnCloseRequestBehavior(Stage thisStage)
	{
		thisStage.setOnCloseRequest(event -> 
		{
			event.consume();
			if (!btnStop.isDisabled())
				btnStop.fire();
		});
	}

	@Override
	public void endOfOutput()
	{
		super.endOfOutput();
		
		Platform.runLater(() -> 
		{
			btnStop.setDisable(true);
						
			if (outputReceiver != null)
				outputReceiver.traceFinished(getOutputAsList());
			
			getCloseButton().fire();	
		});
	}

	private String generateCommandString(String ip, boolean resolveHostnames, Integer pingTimeout)
	{
		return "tracert " + (resolveHostnames ? "" : "-d ") + (pingTimeout == null ? "" : "-w " + pingTimeout + " ") + ip;
	}

	private List<String> getOutputAsList()
	{
		List<String> outputList = new ArrayList<>();
		String lines[] = getTextArea().getText().split("\n");
		int lastLineToRead = lines.length - (endedGracefully ? 2 : 0); //if ended gracefully, the last two lines are not relevant

		for (int i = 3; i < lastLineToRead; i++) //first few lines are not relevant
		{
			if (lines[i].isEmpty())
				continue;
			
			String ip = extractIPFromLine(lines[i]);
			char lastChar = ip.charAt(ip.length() - 1);
			
			if (lastChar != ']' && !Character.isDigit(lastChar)) //this means this line is a "request timed out" since it doesn't end with an ip or a hostname
				continue;

			outputList.add(lines[i]);
		}

		return outputList;
	}
	
	public static String extractIPFromLine(String line)
	{
		String spaceSeparated[] = line.split(" ");
		String tempIP = spaceSeparated[spaceSeparated.length - 1];
		
		if (tempIP.startsWith("[")) //then we have a hostname, not just an ip
			tempIP = tempIP.substring(1, tempIP.length() - 1);
		
		return tempIP;
	}
}
