package whowhatwhere.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import whowhatwhere.model.networksniffer.NICInfo;

public class NICSelectionController implements Initializable
{
	private final static String autoDetectImageLocation = "/buttonGraphics/Auto-detect.png";
	
	@FXML
	private ComboBox<NICInfo> comboNIC;
	@FXML
	private Button btnAutoDetect;
	@FXML
	private Button btnDone;
	@FXML
	private Label labelFirstRun;
	@FXML
	private Pane paneDetecting;

	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		GUIController.setGraphicForLabeledControl(btnAutoDetect, autoDetectImageLocation, ContentDisplay.LEFT);
		GUIController.setCommonGraphicOnLabeled(btnDone, GUIController.CommonGraphicImages.OK);
	}

	public ComboBox<NICInfo> getComboNIC()
	{
		return comboNIC;
	}
	
	public Button getBtnAutoDetect()
	{
		return btnAutoDetect;
	}
	
	public Button getBtnDone()
	{
		return btnDone;
	}
	
	public Pane getPaneDetecting()
	{
		return paneDetecting;
	}
	
	public Label getLabelFirstRun()
	{
		return labelFirstRun;
	}
}
