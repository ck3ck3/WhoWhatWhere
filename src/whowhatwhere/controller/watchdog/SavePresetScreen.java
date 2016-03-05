package whowhatwhere.controller.watchdog;

import java.io.IOException;

import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import whowhatwhere.model.ipsniffer.firstsight.IPToMatch;
import whowhatwhere.view.SecondaryFXMLScreen;

public class SavePresetScreen extends SecondaryFXMLScreen
{
	private SavePresetController savePresetController;

	public SavePresetScreen(String fxmlLocation, Stage stage, Scene scene, WatchdogUI uiController, ListController listController) throws IOException
	{
		super(fxmlLocation, stage, scene);

		savePresetController = getLoader().<SavePresetController> getController();

		savePresetController.getBtnSave().setOnAction(event ->
		{
			try
			{
				String filename = savePresetController.getTextFilename().getText();
				ObservableList<IPToMatch> entryList = uiController.getEntryList();
				TextField textMessage = uiController.getTextMessage();
				
				if (filename.isEmpty())
				{
					new Alert(AlertType.ERROR, "Please enter a non-empty filename").showAndWait();
					throw new IllegalArgumentException("File name must not be empty");
				}

				WatchdogUI.saveListToFile(entryList, textMessage.getText(), filename + WatchdogUI.presetExtension);

				MenuItem menuItem = ManageListScreen.createMenuItem(entryList, textMessage, filename);
				
				ObservableList<MenuItem> items = listController.getMenuBtnLoadPreset().getItems();
				
				if (items.get(0).isDisable()) //it only contains the disabled "none found " item, remove it before adding new one
					items.clear();
					
				items.add(menuItem);
			}
			catch (IOException e)
			{
				new Alert(AlertType.ERROR, "Unable to save preset: " + e.getMessage()).showAndWait();
			}
		});

		savePresetController.getTextFilename().setOnKeyPressed(ke ->
		{
			if (ke.getCode().equals(KeyCode.ENTER))
				savePresetController.getBtnSave().fire();
		});
	}

	public Button getCloseButton()
	{
		return savePresetController.getBtnSave();
	}
}
