package whowhatwhere.view;

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
	 * @param existingStage
	 *            - The stage on which to show the new screen.
	 * 
	 * @param buttonsToClose
	 *            - list of buttons that close this window. The buttons'
	 *            onAction doesn't have to implement anything, not even closing
	 *            the screen. If onAction is implemented, it MUST to be done in
	 *            the constructor of the inheriting class, and then that
	 *            implementation will be called before closing the window. In
	 *            case the window shouldn't be closed (if there's an input error
	 *            on a form for example), the inheriting class' onAction method
	 *            implementation should throw an IllegalArgumentException, which
	 *            will then not close the window.
	 */
	public void showScreenOnExistingStage(Stage existingStage, Button... buttonsToClose)
	{
		Scene scene = new Scene(getLoadedFXML());

		for (Button btn : buttonsToClose)
		{
			EventHandler<ActionEvent> onAction = btn.getOnAction();

			btn.setOnAction(event ->
			{
				try
				{
					if (onAction != null) //if there was already a button handler, run it, and then close the screen
						onAction.handle(event);
				}
				catch (IllegalArgumentException iae) //some error occurred (like invalid data entry), don't close the window 
				{
					return;
				}

				postCloseStage.setScene(postCloseScene);
				postCloseStage.show();
			});
		}

		existingStage.setScene(scene);
		existingStage.show();
	}

	/**
	 * Shows the screen on a new stage
	 * 
	 * @param buttonsToClose
	 *            - list of buttons that close this window. The buttons'
	 *            onAction doesn't have to implement anything, not even closing
	 *            the screen. If onAction is implemented, it MUST to be done in
	 *            the constructor of the inheriting class, and then that
	 *            implementation will be called before closing the window. In
	 *            case the window shouldn't be closed (if there's an input error
	 *            on a form for example), the inheriting class' onAction method
	 *            implementation should throw an IllegalArgumentException, which
	 *            will then not close the window.
	 * @param title
	 *            - Title for the new stage
	 */
	public void showScreenOnNewStage(String title, Button... buttonsToClose)
	{
		Scene scene = new Scene(getLoadedFXML());
		Stage stage = new Stage();

		stage.setTitle(title);
		stage.getIcons().addAll(postCloseStage.getIcons());

		for (Button btn : buttonsToClose)
		{
			EventHandler<ActionEvent> onAction = btn.getOnAction();

			btn.setOnAction(event ->
			{
				try
				{
					if (onAction != null) //if there was already a button handler, run it, and then close the screen
						onAction.handle(event);
				}
				catch (IllegalArgumentException iae) //some error occurred (like invalid data entry), don't close the window
				{
					return;
				}

				stage.close();
			});
		}
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
