package mostusedips.controller.watchdog;

import java.io.IOException;
import java.util.ArrayList;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import mostusedips.model.ipsniffer.IPToMatch;
import mostusedips.view.SecondaryFXMLScreen;

public class WatchdogSavePresetScreen extends SecondaryFXMLScreen
{
	private WatchdogSavePresetController savePresetController;

	public WatchdogSavePresetScreen(String fxmlLocation, Stage stage, Scene scene, ObservableList<IPToMatch> list, TextField textToSay, Label labelCounter, MenuButton menuButton) throws IOException
	{
		super(fxmlLocation, stage, scene);

		savePresetController = getLoader().<WatchdogSavePresetController> getController();

		savePresetController.getBtnSave().setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				try
				{
					String filename = savePresetController.getTextFilename().getText();

					WatchdogUI.watchdogSaveListToFile(new ArrayList<IPToMatch>(list), textToSay.getText(), filename + ".watchdogPreset");

					MenuItem menuItem = WatchdogManageListScreen.createMenuItem(list, textToSay, labelCounter, filename);
					
					ObservableList<MenuItem> items = menuButton.getItems();
					
					if (items.get(0).isDisable()) //it only contains the disabled "none found " item, remove it before adding new one
						items.clear();
						
					items.add(menuItem);
				}
				catch (IOException e)
				{
					new Alert(AlertType.ERROR, "Unable to save preset: " + e.getMessage()).showAndWait();
				}
			}
		});

		savePresetController.getTextFilename().setOnKeyPressed(new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent ke)
			{
				if (ke.getCode().equals(KeyCode.ENTER))
					savePresetController.getBtnSave().fire();
			}
		});
	}

	public Button getCloseButton()
	{
		return savePresetController.getBtnSave();
	}
}
