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
package whowhatwhere.model.networksniffer.watchdog;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;

import whowhatwhere.controller.watchdog.NumberRange;
import whowhatwhere.controller.watchdog.NumberRangeValues;
import whowhatwhere.model.criteria.AndCriteria;
import whowhatwhere.model.criteria.Criteria;
import whowhatwhere.model.criteria.CriteriaIP;
import whowhatwhere.model.criteria.CriteriaPacketDirection;
import whowhatwhere.model.criteria.CriteriaPacketSize;
import whowhatwhere.model.criteria.CriteriaPort;
import whowhatwhere.model.criteria.CriteriaPort.PortType;
import whowhatwhere.model.criteria.CriteriaProtocol;
import whowhatwhere.model.criteria.OrCriteria;
import whowhatwhere.model.criteria.RelativeToValue;
import whowhatwhere.model.networksniffer.NetworkSniffer;
import whowhatwhere.model.networksniffer.PacketDirection;
import whowhatwhere.model.networksniffer.SupportedProtocols;

public class WatchdogPacketHandler implements PcapPacketHandler<Void>
{
	private enum NumberRangeBasedCriteria {PORT, PACKETSIZE}
	
	private boolean isRepeated;
	private Integer cooldownInSecs;
	private boolean isCooldownPeriod = false;
	private ScheduledThreadPoolExecutor timer;
	private WatchdogListener listener;
	private NetworkSniffer sniffer;
	private byte[] ownMACAddress;
	
	private List<Criteria<PcapPacket, Boolean>> criteriaList;
	private Map<Criteria<PcapPacket, Boolean>, WatchdogMessage> criteriaToMsgMap = new HashMap<>();

	public WatchdogPacketHandler(List<PacketTypeToMatch> packetTypeList, boolean isRepeated, Integer cooldownInSecs, WatchdogListener listener, NetworkSniffer sniffer, byte[] ownMACAddress)
			throws IllegalArgumentException, UnknownHostException
	{
		this.isRepeated = isRepeated;
		this.cooldownInSecs = cooldownInSecs;
		this.listener = listener;
		this.sniffer = sniffer;
		this.ownMACAddress = ownMACAddress;

		if (isRepeated && cooldownInSecs == null)
			throw new IllegalArgumentException("A repeated task cannot have a null cooldownInSecs");

		timer = new ScheduledThreadPoolExecutor(1);

		criteriaList = convertPacketTypeToMatchListToCriteria(packetTypeList);

		if (criteriaList.size() == 0)
			throw new IllegalArgumentException("No criteria was set");
	}

	@Override
	public void nextPacket(PcapPacket packet, Void nothing)
	{
		if (!isCooldownPeriod)
		{
			for (Criteria<PcapPacket, Boolean> criteria : criteriaList)
			{
				if (criteria.meetCriteria(packet))
				{
					if (isRepeated)
					{
						isCooldownPeriod = true;

						timer.schedule(() -> isCooldownPeriod = false, cooldownInSecs, TimeUnit.SECONDS);
					}
					else
						sniffer.stopCapture();

					listener.watchdogFoundMatchingPacket(packet, criteriaToMsgMap.get(criteria));
				}
			}
		}
	}

	private List<Criteria<PcapPacket, Boolean>> convertPacketTypeToMatchListToCriteria(List<PacketTypeToMatch> packetTypeList)
	{
		List<Criteria<PcapPacket, Boolean>> criterias = new ArrayList<>();

		for (PacketTypeToMatch item : packetTypeList)
		{
			Criteria<PcapPacket, Boolean> andCriteria = null;
			List<Criteria<PcapPacket, Boolean>> criteriasToAND = generateCriteriasToAND(item);

			if (criteriasToAND.size() > 0)
				andCriteria = criteriasToAND.get(0);

			for (int i = 1; i < criteriasToAND.size(); i++)
				andCriteria = new AndCriteria<PcapPacket>(andCriteria, criteriasToAND.get(i));

			if (criteriasToAND.size() > 0)
			{
				criterias.add(andCriteria);
				criteriaToMsgMap.put(andCriteria, new WatchdogMessage(item.getMessageTextValue(), item.getMessageOutputMethodValue()));
			}
		}

		return criterias;
	}

	private List<Criteria<PcapPacket, Boolean>> generateCriteriasToAND(PacketTypeToMatch item)
	{
		List<Criteria<PcapPacket, Boolean>> criteriasToAND = new ArrayList<>();

		if (item.getPacketDirectionValue() != null)
			criteriasToAND.add(new CriteriaPacketDirection(item.getPacketDirectionValue(), ownMACAddress));

		String ipAddress = item.getIpAddressValue();
		if (ipAddress != null && !ipAddress.equals(PacketTypeToMatch.IPAddress_EMPTY))
		{
			String temp = item.getNetmaskValue();
			String netmask = temp == null || temp.isEmpty() || temp.equals(PacketTypeToMatch.netmask_EMPTY) ? "255.255.255.255" : temp;
			criteriasToAND.add(new CriteriaIP(ipAddress, netmask, item.getPacketDirectionValue()));
		}

		String ipNotesValue = item.getIPNotesValue();
		if (ipNotesValue != null && !ipNotesValue.equals(PacketTypeToMatch.ipNotes_EMPTY))
		{
			List<String> ipsFromIPNotes = item.getIPsFromIPNotes();
			int ipsToAdd = ipsFromIPNotes.size();

			if (ipsToAdd > 0)
			{
				PacketDirection packetDirectionValue = item.getPacketDirectionValue();
				
				Criteria<PcapPacket, Boolean> orBetweenIPs = new CriteriaIP(ipsFromIPNotes.get(0), "255.255.255.255", packetDirectionValue);

				for (int i = 1; i < ipsToAdd; i++)
					orBetweenIPs = new OrCriteria<PcapPacket>(orBetweenIPs, new CriteriaIP(ipsFromIPNotes.get(i), "255.255.255.255", packetDirectionValue));

				criteriasToAND.add(orBetweenIPs);
			}
		}

		Criteria<PcapPacket, Boolean> packetSizeCriteria = generateNumberRangeBasedCriteria(NumberRangeBasedCriteria.PORT, item.getPacketSizeValues(), null);
		if (packetSizeCriteria != null)
			criteriasToAND.add(packetSizeCriteria);
		
		SupportedProtocols protocol = item.getProtocolValue();
		if (protocol != null)
			criteriasToAND.add(new CriteriaProtocol(protocol));
		
		Criteria<PcapPacket, Boolean> srcPortCriteria = generateNumberRangeBasedCriteria(NumberRangeBasedCriteria.PORT, item.getSrcPortValues(), PortType.SOURCE);
		if (srcPortCriteria != null)
			criteriasToAND.add(srcPortCriteria);
		
		Criteria<PcapPacket, Boolean> dstPortCriteria = generateNumberRangeBasedCriteria(NumberRangeBasedCriteria.PORT, item.getDstPortValues(), PortType.DESTINATION);
		if (dstPortCriteria != null)
			criteriasToAND.add(dstPortCriteria);
		
		return criteriasToAND;
	}
	
	
	/**
	 * @param criteriaType - type of criteria to generate
	 * @param numberRangeValues - the number range values
	 * @param portType - if it's a CriteriaPort, this is the type of port for the criteria. Otherwise, ignored, should be null.
	 * @return - the requested criteria, or null if there's no NumberRange set
	 */
	private Criteria<PcapPacket, Boolean> generateNumberRangeBasedCriteria(NumberRangeBasedCriteria criteriaType, NumberRangeValues numberRangeValues, PortType portType)
	{
		if (numberRangeValues != null)
		{
			NumberRange numberRange = numberRangeValues.getRange();
			Integer leftValue = numberRangeValues.getLeftValue();
			
			if (criteriaType == NumberRangeBasedCriteria.PORT)
				switch (numberRange)
				{
					case EQUALS:
						return new CriteriaPort(leftValue, RelativeToValue.EQUALS, portType);
					case GREATER_THAN:
						return new CriteriaPort(leftValue, RelativeToValue.GREATER_THAN, portType);
					case LESS_THAN:
						return new CriteriaPort(leftValue, RelativeToValue.LESS_THAN, portType);
					case RANGE:
						return new AndCriteria<PcapPacket>(new CriteriaPort(leftValue, RelativeToValue.GREATER_THAN, portType), 
															new CriteriaPort(numberRangeValues.getRightValue(), RelativeToValue.LESS_THAN, portType));
				}
			else
				if (criteriaType == NumberRangeBasedCriteria.PACKETSIZE)
					switch (numberRange)
					{
						case EQUALS:
							return new CriteriaPacketSize(leftValue, RelativeToValue.EQUALS);
						case GREATER_THAN:
							return new CriteriaPacketSize(leftValue, RelativeToValue.GREATER_THAN);
						case LESS_THAN:
							return new CriteriaPacketSize(leftValue, RelativeToValue.LESS_THAN);
						case RANGE:
							return new AndCriteria<PcapPacket>(new CriteriaPacketSize(leftValue, RelativeToValue.GREATER_THAN), 
																new CriteriaPacketSize(numberRangeValues.getRightValue(), RelativeToValue.LESS_THAN));
					}
		}

		return null; //nothing to add, just return null
	}
}
