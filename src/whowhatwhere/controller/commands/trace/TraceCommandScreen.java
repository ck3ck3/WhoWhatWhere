package whowhatwhere.controller.commands.trace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import numbertextfield.NumberTextField;
import whowhatwhere.Main;
import whowhatwhere.controller.commands.CommandScreen;
import whowhatwhere.model.geoipresolver.GeoIPResolver;
import whowhatwhere.model.ipsniffer.IPSniffer;

public class TraceCommandScreen extends CommandScreen
{

	private Button btnStart = new Button("Start trace");
	private CheckBox chkboxResolveNames = new CheckBox("Resolve hostnames");
	private Label labelTimeout = new Label("Ping timeout (in milliseconds)");
	private NumberTextField numFieldTimeout = new NumberTextField("200", 1, 3000);
	private Button btnVisualTrace = new Button("Show visual trace");

	private String ip;

	private final String introMarker = "==================================\n";
	private final String introMsg = "Press the start button to start tracing.\nWhen the trace is complete, you can press the \"" + btnVisualTrace.getText()
			+ "\" button, or select an IP address and right click it to see more GeoIP info about it\n\n" + introMarker;

	public TraceCommandScreen(Stage stage, Scene scene, String ip) throws IOException
	{
		super(stage, scene);
		this.ip = ip;

		initTraceScreen();
	}

	public void initTraceScreen()
	{
		HBox bottomHBox = getBottomHBox();
		TextArea textArea = getTextArea();

		btnStart.setOnAction(event ->
		{
			textArea.setText(introMsg);
			bottomHBox.setDisable(true);
			setCommandStr(generateCommandString());
			runCommand();
		});

		numFieldTimeout.setPrefSize(45, 25);

		btnVisualTrace.setOnAction(event -> openVisualTrace());

		btnVisualTrace.setDisable(true);

		AnchorPane aPane = new AnchorPane(chkboxResolveNames);
		chkboxResolveNames.setLayoutY(4);
		labelTimeout.setPadding(new Insets(4, 0, 0, 0));

		bottomHBox.getChildren().addAll(btnStart, aPane, labelTimeout, numFieldTimeout, btnVisualTrace);

		textArea.setText(introMsg);

		MenuItem moreInfo = new MenuItem("See more GeoIP results for selected IP address in browser");
		moreInfo.setOnAction(event ->
		{
			String selectedText = textArea.getSelectedText();

			if (IPSniffer.isValidIPv4(selectedText))
				Main.openInBrowser(GeoIPResolver.getSecondaryGeoIpPrefix() + selectedText);
			else
				new Alert(AlertType.ERROR, "The selected text \"" + selectedText + "\" is not an IP address").showAndWait();
		});

		MenuItem copyIPtoClipboard = new MenuItem("Copy to clipboard");
		copyIPtoClipboard.setOnAction(event ->
		{
			String selectedText = textArea.getSelectedText();

			final Clipboard clipboard = Clipboard.getSystemClipboard();
			final ClipboardContent content = new ClipboardContent();
			content.putString(selectedText);
			clipboard.setContent(content);
		});

		textArea.setContextMenu(new ContextMenu(moreInfo, copyIPtoClipboard));
	}

	@Override
	public void endOfOutput()
	{
		btnStart.setDisable(false);
		btnVisualTrace.setDisable(false);
		getBottomHBox().setDisable(false);
	}

	private String generateCommandString()
	{
		return "tracert " + (chkboxResolveNames.isSelected() ? " " : "-d ") + ("-w " + numFieldTimeout.getValue() + " ") + ip;
	}

	private void openVisualTrace()
	{
		List<String> listOfIPs = getListOfIPs();
		VisualTraceScreen visualTraceScreen;
		Stage stage = (Stage)btnStart.getScene().getWindow();
		
		try
		{
			visualTraceScreen = new VisualTraceScreen(listOfIPs, stage, stage.getScene());
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "Unable to load Visual Trace screen", e);
			return;
		}

		Button btnClose = visualTraceScreen.getVisualTraceController().getBtnClose();

		visualTraceScreen.showScreenOnExistingStage(stage, btnClose);
	}

	private List<String> getListOfIPs()
	{
		List<String> listOfIPs = new ArrayList<>();
		String lines[] = getTextArea().getText().split(introMarker)[1].split("\n"); //get actual command output, after our intro string

		for (int i = 3; i < lines.length - 2; i++) //first few lines and last line are not relevant
		{
			if (lines[i].isEmpty())
				continue;
			
			String ip = extractIPFromLine(lines[i]);
			char lastChar = ip.charAt(ip.length() - 1);
			
			if (lastChar != ']' && !Character.isDigit(lastChar)) //this means this line is a "request timed out" since it doesn't end with an ip or a hostname
				continue;

			listOfIPs.add(lines[i]);
		}

		return listOfIPs;
	}
	
	static String extractIPFromLine(String ipInfo)
	{
		String spaceSeparated[] = ipInfo.split(" ");
		String tempIP = spaceSeparated[spaceSeparated.length - 1];
		
		if (tempIP.startsWith("[")) //then we have a hostname, not just an ip
			tempIP = tempIP.substring(1, tempIP.length() - 1);
		
		return tempIP;
	}

}
