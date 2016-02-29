package mostusedips.controller.watchdog;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import mostusedips.model.ipsniffer.firstsight.IPToMatch;
import mostusedips.view.NumberTextField;

public class ListAddEditController implements Initializable
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
	private AnchorPane paneRoot;
	@FXML
	private Label labelSrcPort;
	@FXML
	private Label labelDstPort;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		numTextSrcPort = new NumberTextField(0, 65535);

		numTextSrcPort.setPrefSize(textIP.getPrefWidth(), textIP.getPrefHeight());
		numTextSrcPort.setLayoutX(textIP.getLayoutX());
		numTextSrcPort.setLayoutY(labelSrcPort.getLayoutY());
		numTextSrcPort.setAllowEmpty(true);
		numTextSrcPort.removeFocusValidator();
		
		ArrayList<String> listOfWords = new ArrayList<String>();
		listOfWords.add(IPToMatch.port_ANY);
		
		numTextSrcPort.setAllowedWords(listOfWords);

		numTextDstPort = new NumberTextField(0, 65535);

		numTextDstPort.setPrefSize(textIP.getPrefWidth(), textIP.getPrefHeight());
		numTextDstPort.setLayoutX(textIP.getLayoutX());
		numTextDstPort.setLayoutY(labelDstPort.getLayoutY());
		numTextDstPort.setAllowEmpty(true);
		numTextDstPort.removeFocusValidator();
		numTextDstPort.setAllowedWords(listOfWords);

		paneRoot.getChildren().remove(btnDone); //to get the tab-order right, we remove the button and then add it after the NumTextFields
		paneRoot.getChildren().addAll(numTextSrcPort, numTextDstPort, btnDone);
		paneRoot.setOnKeyPressed(ke ->
		{
			if (ke.getCode().equals(KeyCode.ENTER))
				btnDone.fire();
		});

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
