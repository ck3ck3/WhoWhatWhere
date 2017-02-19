package whowhatwhere.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class IPNotes
{
	private final static String manageIPNotesFormLocation = "/whowhatwhere/view/fxmls/maingui/ManageIPNotes.fxml";
	private final static String ipNotesFilename = "ipNotes.properties";
	
	private final static Logger logger = Logger.getLogger(IPNotes.class.getPackage().getName());
	
	private Properties ipNotes;
	
	
	public IPNotes()
	{
		InputStream in;
		File ipNotesFile = new File(ipNotesFilename);
		ipNotes = new Properties();

		if (ipNotesFile.exists())
		{
			try
			{
				in = new FileInputStream(ipNotesFile);
				
				ipNotes.load(in);
				in.close();
			}
			catch (IOException e)
			{
				Platform.runLater(() -> new Alert(AlertType.ERROR, "Unable to open IP notes file: " + e.getMessage()).showAndWait());
				logger.log(Level.SEVERE, "Unable to open IP notes file " + ipNotesFilename, e);
			}
		}
	}
	
	public void addIPNote(String ip, String note)
	{
		ipNotes.put(ip, note);
	}
	
	public void removeIPNote(String ip)
	{
		ipNotes.remove(ip);
	}
	
	public String getIPNote(String ip)
	{
		return ipNotes.getProperty(ip);
	}
	
	public String getIPNote(String ip, String valueIfKeyNotFound)
	{
		return ipNotes.getProperty(ip, valueIfKeyNotFound);
	}
	
	public boolean containsIP(String ip)
	{
		return ipNotes.containsKey(ip);
	}
	
	public Set<Object> getIPSet()
	{
		return ipNotes.keySet();
	}
	
	public void saveIPNotes()
	{
		try
		{
			SettingsHandler.savePropertiesSafely(ipNotes, "IP notes", ipNotesFilename);
		}
		catch (IOException e)
		{
			Platform.runLater(() -> new Alert(AlertType.ERROR, "Unable to save IP notes file: " + e.getMessage()).showAndWait());
			logger.log(Level.SEVERE, "Unable to save IP notes file " + ipNotesFilename, e);
		}
	}

	/**
	 * @param stage - stage to return to after this screen is closed
	 */
	public void openManageIPNotesScreen(Stage stage)
	{
		ManageIPNotesScreen ipNotesScreen;

		try
		{
			ipNotesScreen = new ManageIPNotesScreen(manageIPNotesFormLocation, stage, stage.getScene(), this);
		}
		catch (IOException e)
		{
			Platform.runLater(() -> new Alert(AlertType.ERROR, "Unable to load IP notes management screen: " + e.getMessage()).showAndWait());
			logger.log(Level.SEVERE, "Unable to load IP notes management screen", e);
			return;
		}

		Stage newStage = ipNotesScreen.showScreenOnNewStage("Manage IP Notes", Modality.APPLICATION_MODAL, ipNotesScreen.getCloseButton());
		newStage.setOnCloseRequest(windowEvent -> 
		{
			windowEvent.consume();
			ipNotesScreen.getCloseButton().fire();
		});
	}

	/**
	 * @return a map that maps IP note to a list of IPs that have that note 
	 */
	public Map<String, List<String>> getIPNotesReverseMap()
	{
		Map<String, List<String>> reverseMap = new HashMap<String, List<String>>();
		
		for (Object ipObj : ipNotes.keySet())
		{
			String ip = (String) ipObj;
			String note = ipNotes.getProperty(ip);
			
			List<String> listOfIPs = reverseMap.get(note);
			
			if (listOfIPs == null) //first ip for that note
			{
				listOfIPs = new ArrayList<String>();
				listOfIPs.add(ip);
				reverseMap.put(note, listOfIPs);
			}
			else //there's already a list of ips for this note
				listOfIPs.add(ip);
		}
		
		return reverseMap;
	}
}
