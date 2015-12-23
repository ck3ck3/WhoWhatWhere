package mostusedips;

import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.io.IOUtils;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import mostusedips.controller.GUIController;

public class Main extends Application
{
    private final static String iconResource16 = "/ip16.jpg";
    private final static String iconResource32 = "/ip32.jpg";
    private final static String appTitle = "Most used IPs";

    private Logger logger;
    private TrayIcon trayIcon;
    private SystemTray tray;

    @Override
    public void start(Stage primaryStage)
    {

	try
	{
	    initLogger();

	    if (!loadJNetPCapDll())
	    {
		System.err.println("Unable to load jnetpcap native dll. See log file for details. Unable to continue, aborting.");
		return;
	    }
	    
	    primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream(iconResource16)));
	    primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream(iconResource32)));

	    if (!initSysTray(primaryStage))
		logger.log(Level.WARNING, "Unable to initialize system tray");

	    primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>()
	    {
		public void handle(WindowEvent we)
		{
		    we.consume(); //ignore the application's exit button, instead minimize to systray
		    Platform.runLater(new Runnable()
		    {

			@Override
			public void run()
			{
			    try
			    {
				tray.add(trayIcon);
				trayIcon.displayMessage("Minimized to tray", "Still running in the background, double click this icon to restore the window. Use the \"Exit\" button to exit.",
					MessageType.INFO);
				primaryStage.hide();
			    }
			    catch (Exception e)
			    {
				logger.log(Level.WARNING, "Unable to minimize to tray", e);
			    }

			}
		    });
		}
	    });

	    Parent root = FXMLLoader.load(getClass().getResource(GUIController.getMainformlocation()));

	    Scene scene = new Scene(root, 1024, 768);
	    primaryStage.setTitle(appTitle);
	    primaryStage.setScene(scene);
	    primaryStage.show();
	}
	catch (IOException e)
	{
	    logger.log(Level.SEVERE, "Unable to open file", e);
	}
    }

    /**
     * @return true if successfully loaded, false otherwise
     */
    private boolean loadJNetPCapDll()
    {
	try
	{
	    System.loadLibrary("jnetpcap"); //expected to throw exception on first run only
	}
	catch (UnsatisfiedLinkError ule)
	{
	    try
	    {
		UnsatisfiedLinkError exception32 = tryLoadingDll("/x86/jnetpcap.dll", "jnetpcap");

		if (exception32 != null) //we did get an error, try x64 instead
		{
		    UnsatisfiedLinkError exception64 = tryLoadingDll("/x64/jnetpcap.dll", "jnetpcap");

		    if (exception64 != null) //we failed again, nothing we can do now
		    {
			logger.log(Level.SEVERE, "Unable to load jnetpcap.dll x86 version", exception32);
			logger.log(Level.SEVERE, "Unable to load jnetpcap.dll x64 version", exception64);
			return false;
		    }
		}
	    }
	    catch (IOException ioe)
	    {
		logger.log(Level.SEVERE, "Unable to copy dll from resources", ioe);
		return false;
	    }
	}

	return true;
    }

    /**
     * @param copyDllFrom
     *            - name of the dll resource
     * @param libName
     *            - name of the library
     * @return null on success, an UnsatisfiedLinkError otherwise
     * @throws IOException
     *             if copying the file from resources to current dir fails
     */
    private UnsatisfiedLinkError tryLoadingDll(String copyDllFrom, String libName) throws IOException
    {
	try
	{
	    String currDir = System.getProperty("user.dir");
	    InputStream dll = Main.class.getResourceAsStream(copyDllFrom);
	    FileOutputStream dstFile = new FileOutputStream(currDir + "/jnetpcap.dll");

	    IOUtils.copy(dll, dstFile);
	    IOUtils.closeQuietly(dstFile);

	    System.loadLibrary(libName);
	}
	catch (UnsatisfiedLinkError ule)
	{
	    return ule;
	}

	return null;
    }

    private void initLogger() throws IOException
    {
	logger = Logger.getLogger(getAppName());
	FileHandler fh = new FileHandler(getAppName() + ".log");
	fh.setFormatter(new SimpleFormatter());
	logger.addHandler(fh);
    }

    private boolean initSysTray(Stage primaryStage)
    {
	if (SystemTray.isSupported())
	{
	    tray = SystemTray.getSystemTray();
	    java.awt.Image image = Toolkit.getDefaultToolkit().getImage(Main.class.getResource(iconResource16));
	    PopupMenu popup = new PopupMenu();

	    Platform.setImplicitExit(false); //needed to keep the app running while minimized to tray

	    trayIcon = new TrayIcon(image, appTitle, popup);

	    ActionListener listenerTray = new ActionListener()
	    {
		@Override
		public void actionPerformed(java.awt.event.ActionEvent arg0)
		{
		    Platform.runLater(new Runnable()
		    {

			@Override
			public void run()
			{
			    primaryStage.show();
			    tray.remove(trayIcon);
			}
		    });
		}
	    };

	    trayIcon.addActionListener(listenerTray);

	    return true;
	}
	else
	    return false;
    }

    public static void main(String[] args)
    {
	launch(args);
    }

    public static String getAppName()
    {
	return appTitle;
    }
}
