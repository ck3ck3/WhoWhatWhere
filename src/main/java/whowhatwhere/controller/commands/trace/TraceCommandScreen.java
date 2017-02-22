/*******************************************************************************
 * Who What Where
 * Copyright (C) 2017  ck3ck3
 * https://github.com/ck3ck3/WhoWhatWhere
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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
import javafx.scene.control.ContentDisplay;
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
import whowhatwhere.controller.GUIController;
import whowhatwhere.controller.commands.CommandScreen;
import whowhatwhere.model.geoipresolver.GeoIPResolver;
import whowhatwhere.model.networksniffer.NetworkSniffer;

public class TraceCommandScreen extends CommandScreen
{
	private static final String visualTraceIcon = "/buttonGraphics/earth-16.png";

	private Button btnStart = new Button("Start trace");
	private Button btnStop = new Button("Stop trace");
	private boolean endedGracefully;
	private HBox innerHBox = new HBox();
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
			btnStart.setDisable(true);
			btnStop.setDisable(false);
			innerHBox.setDisable(true);
			setCommandStr(generateCommandString());
			endedGracefully = true;
			runCommand();
		});
		
		btnStop.setOnAction(event ->
		{
			endedGracefully = false;
			btnStop.setDisable(true);
			stopCommand();
		});
		
		btnStop.setDisable(true);

		numFieldTimeout.setPrefSize(45, 25);
		GUIController.setNumberTextFieldValidationUI(numFieldTimeout);

		btnVisualTrace.setOnAction(event -> openVisualTrace());
		btnVisualTrace.setStyle("-fx-font-weight: bold;");
		GUIController.setGraphicForLabeledControl(btnVisualTrace, visualTraceIcon, ContentDisplay.LEFT);
		btnVisualTrace.setDisable(true);

		AnchorPane aPane = new AnchorPane(chkboxResolveNames);
		chkboxResolveNames.setLayoutY(4);
		labelTimeout.setPadding(new Insets(4, 0, 0, 0));
		innerHBox.setSpacing(10);
		
		innerHBox.getChildren().addAll(aPane, labelTimeout, numFieldTimeout, btnVisualTrace);
		bottomHBox.getChildren().addAll(btnStart, btnStop, innerHBox);

		textArea.setText(introMsg);

		MenuItem moreInfo = new MenuItem("See more GeoIP results for selected IP address in browser");
		moreInfo.setOnAction(event ->
		{
			String selectedText = textArea.getSelectedText();

			if (NetworkSniffer.isValidIPv4(selectedText))
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
		btnStop.setDisable(true);
		btnVisualTrace.setDisable(false);
		innerHBox.setDisable(false);
	}

	private String generateCommandString()
	{
		return "tracert " + (chkboxResolveNames.isSelected() ? " " : "-d ") + ("-w " + numFieldTimeout.getValue() + " ") + ip;
	}

	private void openVisualTrace()
	{
		List<String> listOfIPs = getOutputAsList();
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

		visualTraceScreen.showScreenOnNewStage("Visual trace of " + ip, null, btnClose);
	}

	private List<String> getOutputAsList()
	{
		List<String> outputList = new ArrayList<>();
		String lines[] = getTextArea().getText().split(introMarker)[1].split("\n"); //get actual command output, after our intro string
		int lastLineToRead = lines.length - (endedGracefully ? 2 : 0); //if ended gracefully, the last two lines are not relevant

		for (int i = 3; i < lastLineToRead; i++) //first few lines are not relevant
		{
			if (lines[i].isEmpty())
				continue;
			
			String ip = extractIPFromLine(lines[i]);
			char lastChar = ip.charAt(ip.length() - 1);
			
			if (lastChar != ']' && !Character.isDigit(lastChar)) //this means this line is a "request timed out" since it doesn't end with an ip or a hostname
				continue;

			outputList.add(lines[i]);
		}

		return outputList;
	}
	
	static String extractIPFromLine(String line)
	{
		String spaceSeparated[] = line.split(" ");
		String tempIP = spaceSeparated[spaceSeparated.length - 1];
		
		if (tempIP.startsWith("[")) //then we have a hostname, not just an ip
			tempIP = tempIP.substring(1, tempIP.length() - 1);
		
		return tempIP;
	}

}
