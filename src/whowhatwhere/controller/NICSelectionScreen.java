package whowhatwhere.controller;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
	private List<NICInfo> listOfDevices = new NetworkSniffer().getListOfDevices();

	private Process pingProcess;
	private NetworkSniffer[] snifferArray = new NetworkSniffer[listOfDevices.size()];
	private boolean isAutoDetectRunning = false;
	private boolean isFirstRun;
	private Timer autoDetectTimer;

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
			autoDetect();
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
			isAutoDetectRunning = true;
			detecting.setVisible(true);
			btnAutoDetect.setDisable(true);
			comboNIC.setDisable(true);
			final int timeoutInSecs = 30;

			autoDetectTimer = new Timer(true);
			autoDetectTimer.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					Platform.runLater(() -> new Alert(AlertType.ERROR, "Unable to auto-detect, process timed out.").showAndWait());
					cleanupAfterAutoDetect();
				}
			}, timeoutInSecs * 1000);

			String ipToPing = "8.8.8.8";
			pingProcess = Runtime.getRuntime().exec("ping " + ipToPing + " -n " + timeoutInSecs);

			PacketTypeToMatch packetType = new PacketTypeToMatch(ipToPing, "255.255.255.255", PacketTypeToMatch.userNotes_EMPTY, PacketDirection.Outgoing, SupportedProtocols.ICMP, null, null, null,
					"", OutputMethod.POPUP);

			for (int i = 0; i < listOfDevices.size() && isAutoDetectRunning; i++)
			{
				NICInfo nic = listOfDevices.get(i);

				snifferArray[i] = new NetworkSniffer();

				packetType.setMessageText(String.valueOf(i));
				List<PacketTypeToMatch> list = new ArrayList<>(1);
				list.add(packetType);

				if (isAutoDetectRunning) //if one of these threads detected it, this will be false and we won't run another detection for no reason
				{
					final int index = i;
					new Thread(() ->
					{
						try
						{
							snifferArray[index].startWatchdogCapture(nic, list, false, null, this, new StringBuilder());
						}
						catch (UnknownHostException e)
						{
							Platform.runLater(() -> new Alert(AlertType.ERROR, "Unable to auto-detect: " + e.getMessage()).showAndWait());
							cleanupAfterAutoDetect();
						}

					}).start();
				}
			}
		}
		catch (Exception e)
		{
			Platform.runLater(() -> new Alert(AlertType.ERROR, "Unable to auto-detect: " + e.getMessage()).showAndWait());
			cleanupAfterAutoDetect();
		}
	}

	@Override
	public void watchdogFoundMatchingPacket(PcapPacket packet, WatchdogMessage message)
	{
		cleanupAfterAutoDetect();
		int index = Integer.valueOf(message.getMessage());

		Platform.runLater(() -> comboNIC.getSelectionModel().select(index));
	}

	private void cleanupAfterAutoDetect()
	{
		isAutoDetectRunning = false;
		autoDetectTimer.cancel();
		detecting.setVisible(false);

		pingProcess.destroy();

		for (int i = 0; i < listOfDevices.size(); i++)
			if (snifferArray[i] != null)
				snifferArray[i].cleanup();

		comboNIC.setDisable(false);
		btnAutoDetect.setDisable(false);
	}
}
