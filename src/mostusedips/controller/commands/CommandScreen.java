package mostusedips.controller.commands;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import mostusedips.controller.CmdGUIController;
import mostusedips.model.cmd.CmdLiveOutput;
import mostusedips.model.cmd.LiveOutputListener;

public abstract class CommandScreen implements LiveOutputListener
{
	protected final static String commandFormLocation = "/mostusedips/view/CommandForm.fxml";
	protected final static Logger logger = Logger.getLogger(CommandScreen.class.getPackage().getName());

	private Scene postCloseScene;
	private Stage stage;
	private String commandStr;

	private Parent loadedFXML;
	private CmdGUIController cmdController;
	private CmdLiveOutput cmdLiveOutput;

	private StringBuilder output = new StringBuilder();
	private boolean outputReady = false;

	public CommandScreen(Stage stage, Scene scene)
	{
		this(stage, scene, "");
	}

	public CommandScreen(Stage stage, Scene scene, String commandStr)
	{
		this.setCommandStr(commandStr);
		setPostCloseScene(stage, scene);
		initScreen();
	}

	protected void initScreen()
	{
		FXMLLoader loader;

		try
		{
			loader = new FXMLLoader(getClass().getResource(commandFormLocation));
			setLoadedFXML(loader.load());

		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "Unable to load resource " + commandFormLocation, e);
			return;
		}

		cmdController = loader.<CmdGUIController> getController();

		cmdController.setCloseButtonStageAndScene(cmdController.getBtnClose(), stage, postCloseScene);
	}

	public void showScreen()
	{
		Scene scene = new Scene(getLoadedFXML());
		stage.setScene(scene);
		stage.show();
	}

	public void runCommand()
	{
		cmdLiveOutput = new CmdLiveOutput(commandStr, this);
		cmdLiveOutput.runCommand();
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

	public void setPostCloseScene(Stage stage, Scene scene)
	{
		this.stage = stage;
		this.postCloseScene = scene;
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

	public Scene getPostCloseScene()
	{
		return postCloseScene;
	}

	public Stage getStage()
	{
		return stage;
	}

	public String getCommandStr()
	{
		return commandStr;
	}

	public void setCommandStr(String commandStr)
	{
		this.commandStr = commandStr;
	}

	public Parent getLoadedFXML()
	{
		return loadedFXML;
	}

	public void setLoadedFXML(Parent loadedFXML)
	{
		this.loadedFXML = loadedFXML;
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
