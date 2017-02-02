package whowhatwhere.controller.commands;

import java.io.IOException;
import java.util.logging.Logger;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import whowhatwhere.model.cmd.CmdLiveOutput;
import whowhatwhere.model.cmd.LiveOutputListener;
import whowhatwhere.view.secondaryfxmlscreen.SecondaryFXMLScreen;

public abstract class CommandScreen extends SecondaryFXMLScreen implements LiveOutputListener
{
	protected final static String commandFormLocation = "/whowhatwhere/view/fxmls/commands/CommandForm.fxml";
	protected final static Logger logger = Logger.getLogger(CommandScreen.class.getPackage().getName());

	private String commandStr;

	private CmdGUIController cmdController;
	private CmdLiveOutput cmdLiveOutput;

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
		cmdLiveOutput = new CmdLiveOutput(commandStr, this);
		cmdLiveOutput.runCommand();
	}
	
	public void stopCommand()
	{
		cmdLiveOutput.stopCommand();
	}

	@Override
	public void lineReady(String line)
	{
		cmdController.getTextAreaOutput().appendText(line + "\n");
		output.append(line + "\n");
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
