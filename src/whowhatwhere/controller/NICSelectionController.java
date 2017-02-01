package whowhatwhere.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import whowhatwhere.model.networksniffer.NICInfo;

public class NICSelectionController
{
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
