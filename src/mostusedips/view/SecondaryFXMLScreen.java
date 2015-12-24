package mostusedips.view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * Extending classes should set the close button, and stage and scene to come
 * back to after the close button has been clicked.
 *
 */
public abstract class SecondaryFXMLScreen
{
	private Scene postCloseScene;
	private Stage stage;
	private Parent loadedFXML;

	/**
	 * @param btnClose
	 *            - the close button
	 * @param stage
	 *            - stage to come back to after the close button has been
	 *            clicked
	 * @param scene
	 *            - scene to come back to after the close button has been
	 *            clicked
	 */
	public void setCloseButtonStageAndScene(Button btnClose, Stage stage, Scene scene)
	{
		btnClose.setOnAction(new EventHandler<ActionEvent>()
		{

			@Override
			public void handle(ActionEvent event)
			{
				getStage().setScene(getPostCloseScene());
				getStage().show();
			}
		});
		this.stage = stage;
		this.postCloseScene = scene;
	}

	public Scene getPostCloseScene()
	{
		return postCloseScene;
	}

	public void setPostCloseScene(Scene postCloseScene)
	{
		this.postCloseScene = postCloseScene;
	}

	public Stage getStage()
	{
		return stage;
	}

	public void setStage(Stage stage)
	{
		this.stage = stage;
	}

	public Parent getLoadedFXML()
	{
		return loadedFXML;
	}

	public void setLoadedFXML(Parent loadedFXML)
	{
		this.loadedFXML = loadedFXML;
	}

}
