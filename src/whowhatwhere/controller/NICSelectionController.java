package whowhatwhere.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
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
	private ProgressIndicator progressIndicator;
	@FXML
	private Label labelFirstRun;


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
	
	public ProgressIndicator getProgressIndicator()
	{
		return progressIndicator;
	}
	
	public Label getLabelFirstRun()
	{
		return labelFirstRun;
	}
}
