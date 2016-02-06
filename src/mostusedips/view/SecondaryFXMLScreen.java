package mostusedips.view;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public abstract class SecondaryFXMLScreen
{
	private Stage postCloseStage;
	private Scene postCloseScene;
	private Parent loadedFXML;
	private FXMLLoader loader;

	/**
	 * 
	 * @param fxmlLocation
	 *            - location of the fxml file relative to the resources dir
	 * @param stage
	 *            - the stage to come back to after this window is closed
	 * @param scene
	 *            - the scene to come back to after this window is closed
	 * @throws IOException
	 *             - if an error occurred while trying to load the fxml
	 */
	public SecondaryFXMLScreen(String fxmlLocation, Stage stage, Scene scene) throws IOException
	{
		this.postCloseStage = stage;
		this.postCloseScene = scene;

		loader = new FXMLLoader(getClass().getResource(fxmlLocation));
		setLoadedFXML(loader.load());
	}

	/**
	 * Shows the screen on the current stage
	 * 
	 * @param btnClose
	 *            - the button that closes this window
	 */
	public void showScreenOnCurrentStage(Button btnClose)
	{
		Scene scene = new Scene(getLoadedFXML());
		Stage stage = getPostCloseStage();

		EventHandler<ActionEvent> onAction = btnClose.getOnAction();
		
		btnClose.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				try
				{
					if (onAction != null) //if there was already a button handler, run it, and then close the screen
						onAction.handle(event);
				}
				catch(IllegalArgumentException iae) //some error occurred (like invalid data entry), don't close the window 
				{
					return;
				}
				
				postCloseStage.setScene(postCloseScene);
				postCloseStage.show();
			}
		});

		stage.setScene(scene);
		stage.show();
	}

	/**
	 * Shows the screen on a new stage
	 * 
	 * @param btnClose
	 *            - the button that closes this window
	 * @param title
	 *            - Title for the new stage
	 */
	public void showScreenOnNewStage(Button btnClose, String title)
	{
		Scene scene = new Scene(getLoadedFXML());
		Stage stage = new Stage();

		stage.setTitle(title);
		stage.getIcons().addAll(postCloseStage.getIcons());

		EventHandler<ActionEvent> onAction = btnClose.getOnAction();
		
		btnClose.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				try
				{
					if (onAction != null) //if there was already a button handler, run it, and then close the screen
						onAction.handle(event);
				}
				catch(IllegalArgumentException iae) //some error occurred (like invalid data entry), don't close the window
				{
					return;
				}
				
				stage.close();
			}
		});

		stage.setScene(scene);
		stage.show();
	}

	public Scene getPostCloseScene()
	{
		return postCloseScene;
	}

	public void setPostCloseScene(Scene postCloseScene)
	{
		this.postCloseScene = postCloseScene;
	}

	public Stage getPostCloseStage()
	{
		return postCloseStage;
	}

	public void setPostCloseStage(Stage stage)
	{
		this.postCloseStage = stage;
	}

	public Parent getLoadedFXML()
	{
		return loadedFXML;
	}

	public void setLoadedFXML(Parent loadedFXML)
	{
		this.loadedFXML = loadedFXML;
	}

	public FXMLLoader getLoader()
	{
		return loader;
	}
}
