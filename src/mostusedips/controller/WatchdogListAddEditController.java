package mostusedips.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import mostusedips.model.ipsniffer.IPToMatch;
import mostusedips.view.NumberTextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Label;

public class WatchdogListAddEditController implements Initializable
{
	@FXML
	private TextField textIP;
	@FXML
	private ChoiceBox<String> choiceProtocol;
	@FXML
	private Button btnDone;
	@FXML
	private NumberTextField numTextSrcPort;
	@FXML
	private NumberTextField numTextDstPort;
	@FXML
	AnchorPane paneRoot;
	@FXML
	Label labelSrcPort;
	@FXML
	Label labelDstPort;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		numTextSrcPort = new NumberTextField(0, 65535);

		numTextSrcPort.setPrefSize(textIP.getPrefWidth(), textIP.getPrefHeight());
		numTextSrcPort.setLayoutX(textIP.getLayoutX());
		numTextSrcPort.setLayoutY(labelSrcPort.getLayoutY());
		numTextSrcPort.setAllowEmpty(true);
		
		numTextDstPort = new NumberTextField(0, 65535);
		
		numTextDstPort.setPrefSize(textIP.getPrefWidth(), textIP.getPrefHeight());
		numTextDstPort.setLayoutX(textIP.getLayoutX());
		numTextDstPort.setLayoutY(labelDstPort.getLayoutY());
		numTextDstPort.setAllowEmpty(true);
		
		paneRoot.getChildren().addAll(numTextSrcPort, numTextDstPort);
		
		choiceProtocol.getItems().addAll(IPToMatch.protocol_ANY, "ICMP", "UDP", "TCP", "HTTP");
	}

	public TextField getTextIP()
	{
		return textIP;
	}

	public ChoiceBox<String> getChoiceProtocol()
	{
		return choiceProtocol;
	}

	public Button getBtnDone()
	{
		return btnDone;
	}

	public NumberTextField getNumTextSrcPort()
	{
		return numTextSrcPort;
	}

	public NumberTextField getNumTextDstPort()
	{
		return numTextDstPort;
	}
}
