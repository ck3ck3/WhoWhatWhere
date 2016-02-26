package mostusedips.controller.watchdog;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import mostusedips.model.ipsniffer.IPSniffer;
import mostusedips.model.ipsniffer.firstsight.IPToMatch;
import mostusedips.view.NumberTextField;
import mostusedips.view.SecondaryFXMLScreen;

public class ListAddEditScreen extends SecondaryFXMLScreen
{
	private static final String inputRules = "IP must be a non-empty valid IP address.\nA protocol must be selected (choose \"" + IPToMatch.protocol_ANY
			+ "\" to check all protocols).\nPort numbers must be between 0-65535 (use \"" + IPToMatch.port_ANY + "\" to check any port).";

	private ListAddEditController watchdogListAddEditController;
	private TextField textIP;
	private ChoiceBox<String> choiceProtocol;
	private NumberTextField numTextSrcPort;
	private NumberTextField numTextDstPort;

	public ListAddEditScreen(String fxmlLocation, Stage stage, Scene scene, TableView<IPToMatch> table, boolean isEdit) throws IOException
	{
		super(fxmlLocation, stage, scene);

		watchdogListAddEditController = getLoader().<ListAddEditController> getController();

		textIP = watchdogListAddEditController.getTextIP();
		choiceProtocol = watchdogListAddEditController.getChoiceProtocol();
		numTextSrcPort = watchdogListAddEditController.getNumTextSrcPort();
		numTextDstPort = watchdogListAddEditController.getNumTextDstPort();

		final int selectedIndex = table.getSelectionModel().getSelectedIndex();

		if (isEdit)
		{
			IPToMatch selectedItem = table.getSelectionModel().getSelectedItem();

			if (selectedItem == null)
				throw new IllegalStateException("No row selected");

			if (selectedItem.ipProperty() != null)
				textIP.setText(selectedItem.ipProperty().getValue());

			if (selectedItem.protocolProperty() != null)
				choiceProtocol.getSelectionModel().select(selectedItem.protocolProperty().getValue());

			if (selectedItem.srcPortProperty() != null)
				numTextSrcPort.setText(selectedItem.srcPortProperty().getValue());

			if (selectedItem.dstPortProperty() != null)
				numTextDstPort.setText(selectedItem.dstPortProperty().getValue());
		}
		else //add
		{
			choiceProtocol.getSelectionModel().select(IPToMatch.protocol_ANY);
			numTextSrcPort.setText(IPToMatch.port_ANY);
			numTextDstPort.setText(IPToMatch.port_ANY);
		}

		getCloseButton().setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				String ip = textIP.getText();
				String protocol = choiceProtocol.getSelectionModel().getSelectedItem();
				String srcPort = numTextSrcPort.getText();
				String dstPort = numTextDstPort.getText();
				
				if (!validateInput())
				{
					new Alert(AlertType.ERROR, "Invalid input. Input must follow these rules:\n" + inputRules).showAndWait();
					throw new IllegalArgumentException("Invalid input");
				}

				if (!isEdit) //adding new row
					table.getItems().add(new IPToMatch(ip, protocol, srcPort, dstPort));
				else //edit existing row
					table.getItems().get(selectedIndex).init(ip, protocol, srcPort, dstPort);
			}

			private boolean validateInput()
			{
				String ip = textIP.getText();
				return !ip.isEmpty() && IPSniffer.isValidIPv4(ip) && numTextSrcPort.isValidText() && numTextDstPort.isValidText();
			}
		});
	}

	public Button getCloseButton()
	{
		return watchdogListAddEditController.getBtnDone();
	}
}
