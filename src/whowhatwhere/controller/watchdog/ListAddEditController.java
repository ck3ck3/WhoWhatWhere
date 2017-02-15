package whowhatwhere.controller.watchdog;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import numbertextfield.NumberTextField;
import whowhatwhere.model.networksniffer.PacketDirection;
import whowhatwhere.model.networksniffer.SupportedProtocols;
import whowhatwhere.model.networksniffer.watchdog.OutputMethod;

public class ListAddEditController implements Initializable
{
	private final static String speakerImageLocation = "/speaker.png";
	
	@FXML
	private ComboBox<OutputMethod> comboOutputMethod;
	@FXML
	private TextField textMessage;
	@FXML
	private Button btnDone;
	@FXML
	private ComboBox<PacketDirection> comboPacketDirection;
	@FXML
	private TextField textIPAddress;
	@FXML
	private TextField textNetmask;
	@FXML
	private ComboBox<String> comboUserNotes;
	@FXML
	private ComboBox<NumberRange> comboPacketSize;
	@FXML
	private NumberTextField numFieldPacketSizeLeft;
	@FXML
	private Label labelPacketSizeRight;
	@FXML
	private NumberTextField numFieldPacketSizeRight;
	@FXML
	private ComboBox<SupportedProtocols> comboProtocol;
	@FXML
	private ComboBox<NumberRange> comboSrcPort;
	@FXML
	private NumberTextField numFieldSrcPortLeft;
	@FXML
	private Label labelSrcPortRight;
	@FXML
	private NumberTextField numFieldDstPortRight;
	@FXML
	private ComboBox<NumberRange> comboDstPort;
	@FXML
	private NumberTextField numFieldDstPortLeft;
	@FXML
	private Label labelDstPortRight;
	@FXML
	private NumberTextField numFieldSrcPortRight;
	@FXML
	private Button btnCancel;
	@FXML
	private CheckBox chkboxIPAddress;
	@FXML
	private CheckBox chkboxUserNotes;
	@FXML
	private CheckBox chkboxPacketDirection;
	@FXML
	private CheckBox chkboxProtocol;
	@FXML
	private CheckBox chkboxSrcPort;
	@FXML
	private CheckBox chkboxDstPort;
	@FXML
	private CheckBox chkboxPacketSize;
	@FXML
	private Label labelIPRange;
	@FXML
	private CheckBox chkboxNetmask;
	@FXML
	private AnchorPane paneWholeForm;
	@FXML
	private Button btnPreview;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		paneWholeForm.setOnKeyPressed(keyEvent ->
		{
			if (keyEvent.getCode().equals(KeyCode.ESCAPE))
			{
				btnCancel.requestFocus();
				if (btnCancel.isFocused()) //if there's no validation error, to keep consistent with trying to click on Cancel normally
					btnCancel.fire();
			}
		});
		
		labelIPRange.setVisible(false);
		
		labelSrcPortRight.setVisible(false);
		numFieldSrcPortRight.setVisible(false);
		
		labelDstPortRight.setVisible(false);
		numFieldDstPortRight.setVisible(false);
		
		labelPacketSizeRight.setVisible(false);
		numFieldPacketSizeRight.setVisible(false);
		
		btnPreview.setVisible(false);
		btnPreview.setContentDisplay(ContentDisplay.RIGHT);
		btnPreview.setGraphic(new ImageView(new Image(speakerImageLocation)));
	}

	public ComboBox<OutputMethod> getComboOutputMethod()
	{
		return comboOutputMethod;
	}

	public TextField getTextMessage()
	{
		return textMessage;
	}

	public Button getBtnDone()
	{
		return btnDone;
	}

	public Button getBtnCancel()
	{
		return btnCancel;
	}

	public ComboBox<PacketDirection> getComboPacketDirection()
	{
		return comboPacketDirection;
	}

	public TextField getTextIPAddress()
	{
		return textIPAddress;
	}

	public TextField getTextNetmask()
	{
		return textNetmask;
	}

	public ComboBox<String> getComboUserNotes()
	{
		return comboUserNotes;
	}

	public ComboBox<NumberRange> getComboPacketSize()
	{
		return comboPacketSize;
	}

	public NumberTextField getNumFieldPacketSizeLeft()
	{
		return numFieldPacketSizeLeft;
	}

	public Label getLabelPacketSizeRight()
	{
		return labelPacketSizeRight;
	}

	public NumberTextField getNumFieldPacketSizeRight()
	{
		return numFieldPacketSizeRight;
	}

	public ComboBox<SupportedProtocols> getComboProtocol()
	{
		return comboProtocol;
	}

	public ComboBox<NumberRange> getComboSrcPort()
	{
		return comboSrcPort;
	}

	public NumberTextField getNumFieldSrcPortLeft()
	{
		return numFieldSrcPortLeft;
	}

	public Label getLabelSrcPortRight()
	{
		return labelSrcPortRight;
	}

	public NumberTextField getNumFieldDstPortRight()
	{
		return numFieldDstPortRight;
	}

	public ComboBox<NumberRange> getComboDstPort()
	{
		return comboDstPort;
	}

	public NumberTextField getNumFieldDstPortLeft()
	{
		return numFieldDstPortLeft;
	}

	public Label getLabelDstPortRight()
	{
		return labelDstPortRight;
	}

	public NumberTextField getNumFieldSrcPortRight()
	{
		return numFieldSrcPortRight;
	}

	public CheckBox getChkboxIPAddress()
	{
		return chkboxIPAddress;
	}

	public CheckBox getChkboxUserNotes()
	{
		return chkboxUserNotes;
	}

	public CheckBox getChkboxPacketDirection()
	{
		return chkboxPacketDirection;
	}

	public CheckBox getChkboxProtocol()
	{
		return chkboxProtocol;
	}

	public CheckBox getChkboxSrcPort()
	{
		return chkboxSrcPort;
	}

	public CheckBox getChkboxDstPort()
	{
		return chkboxDstPort;
	}

	public CheckBox getChkboxPacketSize()
	{
		return chkboxPacketSize;
	}


	public Label getLabelIPRange()
	{
		return labelIPRange;
	}

	public CheckBox getChkboxNetmask()
	{
		return chkboxNetmask;
	}

	public Button getBtnPreview()
	{
		return btnPreview;
	}
}
