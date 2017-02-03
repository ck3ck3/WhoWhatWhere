package whowhatwhere.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

import javafx.stage.Stage;
import whowhatwhere.controller.appearancecounter.ManageUserNotesScreen;

public class UserNotes
{
	private final static String manageUserNotesFormLocation = "/whowhatwhere/view/fxmls/maingui/ManageUserNotes.fxml";
	private final static String userNotesFilename = "userNotes.properties";
	
	private final static Logger logger = Logger.getLogger(UserNotes.class.getPackage().getName());
	
	private Properties userNotes;
	
	
	public UserNotes()
	{
		InputStream in;
		File userNotesFile = new File(userNotesFilename);
		userNotes = new Properties();

		if (userNotesFile.exists())
		{
			try
			{
				in = new FileInputStream(userNotesFile);
				
				userNotes.load(in);
				in.close();
			}
			catch (IOException e)
			{
				logger.log(Level.SEVERE, "Unable to open user notes file " + userNotesFilename, e);
			}
		}
	}
	
	public void addUserNote(String ip, String note)
	{
		userNotes.put(ip, note);
	}
	
	public void removeUserNote(String ip)
	{
		userNotes.remove(ip);
	}
	
	public String getUserNote(String ip)
	{
		return userNotes.getProperty(ip);
	}
	
	public String getUserNote(String ip, String valueIfKeyNotFound)
	{
		return userNotes.getProperty(ip, valueIfKeyNotFound);
	}
	
	public boolean userNotesContainsKey(String ip)
	{
		return userNotes.containsKey(ip);
	}
	
	public Set<Object> getKeySet()
	{
		return userNotes.keySet();
	}
	
	public void saveUserNotes()
	{
		try
		{
			FileOutputStream out = new FileOutputStream(userNotesFilename);
			userNotes.store(out, "User notes");
			out.close();
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "Unable to save user notes file " + userNotesFilename, e);
		}
	}

	/**
	 * @param stage - stage to return to after this screen is closed
	 */
	public void openManageUserNotesScreen(Stage stage)
	{
		ManageUserNotesScreen userNotesScreen;

		try
		{
			userNotesScreen = new ManageUserNotesScreen(manageUserNotesFormLocation, stage, stage.getScene(), this);
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "Unable to load user notes management screen", e);
			return;
		}

		Stage newStage = userNotesScreen.showScreenOnNewStage("Manage User Notes", null, userNotesScreen.getCloseButton());
		newStage.setOnCloseRequest(windowEvent -> 
		{
			windowEvent.consume();
			userNotesScreen.getCloseButton().fire();
		});
	}

	/**
	 * @return a map that maps user note to a list of IPs that have that note 
	 */
	public Map<String, List<String>> getUserNotesReverseMap()
	{
		Map<String, List<String>> reverseMap = new HashMap<String, List<String>>();
		
		for (Object ipObj : userNotes.keySet())
		{
			String ip = (String) ipObj;
			String note = userNotes.getProperty(ip);
			
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
