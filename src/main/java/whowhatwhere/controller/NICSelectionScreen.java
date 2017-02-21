package whowhatwhere.controller;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jnetpcap.packet.PcapPacket;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import whowhatwhere.model.networksniffer.NICInfo;
import whowhatwhere.model.networksniffer.NetworkSniffer;
import whowhatwhere.model.networksniffer.PacketDirection;
import whowhatwhere.model.networksniffer.SupportedProtocols;
import whowhatwhere.model.networksniffer.watchdog.OutputMethod;
import whowhatwhere.model.networksniffer.watchdog.PacketTypeToMatch;
import whowhatwhere.model.networksniffer.watchdog.WatchdogListener;
import whowhatwhere.model.networksniffer.watchdog.WatchdogMessage;
import whowhatwhere.view.secondaryfxmlscreen.SecondaryFXMLScreen;

public class NICSelectionScreen extends SecondaryFXMLScreen implements WatchdogListener
{
	private NICSelectionController controller;
	private ComboBox<NICInfo> comboNIC;
	private Button btnAutoDetect;
	private Button btnDone;
	private Pane detecting;
	private final NICInfo nicInfoToCopyResultInto;
	NetworkSniffer sniffer = new NetworkSniffer();
	private List<NICInfo> listOfDevices = sniffer.getListOfDevicesWithIP();

	private Process pingProcess;
	private boolean isAutoDetectRunning = false;
	private boolean isFirstRun;
	private boolean autoDetectSuccess;
	private ScheduledThreadPoolExecutor timer = new ScheduledThreadPoolExecutor(1);
	private ScheduledFuture<?> task;

	/**
	 * 
	 * @param fxmlLocation
	 *            {@link whowhatwhere.view.secondaryfxmlscreen.SecondaryFXMLScreen#SecondaryFXMLScreen(String fxmlLocation, Stage stage, Scene scene)
	 *            see description in super}
	 * @param stage
	 *            {@link whowhatwhere.view.secondaryfxmlscreen.SecondaryFXMLScreen#SecondaryFXMLScreen(String fxmlLocation, Stage stage, Scene scene)
	 *            see description in super}
	 * @param scene
	 *            {@link whowhatwhere.view.secondaryfxmlscreen.SecondaryFXMLScreen#SecondaryFXMLScreen(String fxmlLocation, Stage stage, Scene scene)
	 *            see description in super}
	 * @param nodeToEnableOnClose
	 *            - Node to enable when this screen is closed. Can be null.
	 * @param nicInfoToCopyResultInto
	 *            - a NICInfo object that its contents need to be updated with
	 *            the selected NIC details when this screen closes.
	 * @throws IOException
	 *             {@link whowhatwhere.view.secondaryfxmlscreen.SecondaryFXMLScreen#SecondaryFXMLScreen(String fxmlLocation, Stage stage, Scene scene)
	 *             see description in super}
	 */
	public NICSelectionScreen(String fxmlLocation, Stage stage, Scene scene, NICInfo nicInfoToCopyResultInto) throws IOException
	{
		super(fxmlLocation, stage, scene);
		controller = getLoader().<NICSelectionController> getController();
		comboNIC = controller.getComboNIC();
		btnAutoDetect = controller.getBtnAutoDetect();
		btnDone = controller.getBtnDone();
		detecting = controller.getPaneDetecting();
		this.nicInfoToCopyResultInto = nicInfoToCopyResultInto;

		comboNIC.setItems(FXCollections.observableList(listOfDevices));

		isFirstRun = nicInfoToCopyResultInto.getDescription() == null;
		if (!isFirstRun)
			comboNIC.getSelectionModel().select(nicInfoToCopyResultInto);

		controller.getLabelFirstRun().setVisible(isFirstRun);

		setButtonHandlers();
	}

	private void setButtonHandlers()
	{
		btnAutoDetect.setOnAction(actionEvent ->
		{
			new Thread(() -> autoDetect()).start();
		});

		btnDone.setOnAction(actionEvent ->
		{
			NICInfo selectedItem = comboNIC.getSelectionModel().getSelectedItem();
			if (selectedItem == null)
			{
				new Alert(AlertType.ERROR, "You must choose a network adapter.").showAndWait();
				throw new IllegalArgumentException();
			}
			else
			{
				if (isAutoDetectRunning)
					cleanupAfterAutoDetect();

				nicInfoToCopyResultInto.copyNICInfo(selectedItem);
			}
		});
	}

	public Button getCloseButton()
	{
		return btnDone;
	}

	private void autoDetect()
	{
		try
		{
			autoDetectSuccess = false;
			isAutoDetectRunning = true;
			detecting.setVisible(true);
			btnAutoDetect.setDisable(true);
			comboNIC.setDisable(true);
			final int timeoutInSecs = 5;

			String ipToPing = "8.8.8.8";
			pingProcess = Runtime.getRuntime().exec("ping -t " + ipToPing);

			PacketTypeToMatch detectPing = new PacketTypeToMatch(ipToPing, null, PacketTypeToMatch.ipNotes_EMPTY, null, PacketDirection.Outgoing, SupportedProtocols.ICMP, null, null, null, "",
					OutputMethod.TTS);

			for (int i = 0; i < listOfDevices.size() && isAutoDetectRunning; i++)
			{
				NICInfo nic = listOfDevices.get(i);

				detectPing.setMessageText(String.valueOf(i));
				List<PacketTypeToMatch> list = new ArrayList<>(1);
				list.add(detectPing);

				sniffer = new NetworkSniffer();

				task = timer.schedule(() ->
				{
					try
					{
						sniffer.cleanup();
					}
					catch (Exception e) {} //if it fails, it fails...
				}, timeoutInSecs, TimeUnit.SECONDS);

				try
				{
					sniffer.startWatchdogCapture(nic, list, false, null, this, new StringBuilder());
				}
				catch (UnknownHostException uhe)
				{
					continue; //move on to the next nic
				}
			}
		}
		catch (Exception e)
		{
			Platform.runLater(() -> new Alert(AlertType.ERROR, "Unable to auto-detect: " + e.getMessage()).showAndWait());
		}
		finally
		{
			if (isAutoDetectRunning)
				cleanupAfterAutoDetect();
		}

		Platform.runLater(() ->
		{
			Alert autoDetectStatus = new Alert(AlertType.INFORMATION);
			String result = "Auto detect " + (autoDetectSuccess ? "finished successfuly" : "failed");
			autoDetectStatus.setTitle(result);
			autoDetectStatus.setHeaderText(result);
			autoDetectStatus.setContentText("Auto detect " + (autoDetectSuccess ? "finished successfuly and selected the following adapter:\n" + comboNIC.getSelectionModel().getSelectedItem().getDescription()
																				: "failed to find the appropriate network interface"));
			autoDetectStatus.showAndWait();
			
			if (autoDetectSuccess)
				btnDone.fire();
		});
	}

	@Override
	public void watchdogFoundMatchingPacket(PcapPacket packet, WatchdogMessage message)
	{
		autoDetectSuccess = true;
		cleanupAfterAutoDetect();
		int index = Integer.valueOf(message.getMessage());

		Platform.runLater(() -> comboNIC.getSelectionModel().select(index));
	}

	private void cleanupAfterAutoDetect()
	{
		isAutoDetectRunning = false;
		if (task != null)
			task.cancel(true);
		detecting.setVisible(false);

		pingProcess.destroy();
		try
		{
			sniffer.cleanup();
		}
		catch (Exception e) {} //if it fails, it fails...

		comboNIC.setDisable(false);
		btnAutoDetect.setDisable(false);
	}
}
