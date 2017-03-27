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
package whowhatwhere.model.command.trace;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import whowhatwhere.model.command.LiveOutputListener;

public class TraceLiveOutputListener implements LiveOutputListener
{
	private TraceOutputReceiver outputReceiver;
	private int lineCount = 0;
	private boolean traceFailed = false;
	private boolean traceFinished = false;
	private String ipBeingTraced = null;
	private final static Pattern ipPattern = Pattern.compile("(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])"); //doesn't completely validate iPv4 correctness, but the inputs will contain either a valid address or nothing similar to an IP address.	
	
	
	public TraceLiveOutputListener(TraceOutputReceiver outputReceiver)
	{
		this.outputReceiver = outputReceiver;
	}
	
	@Override
	public void lineReady(String line)
	{
		if (traceFailed || traceFinished)
			return;

		if (!lineValidation(line))
			return;
		
		String ip = extractIPFromLine(line);
		char lastChar = ip.charAt(ip.length() - 1);
		
		if (lastChar != ']' && !Character.isDigit(lastChar)) //this means this line is a "request timed out" since it doesn't end with an ip or a hostname
			outputReceiver.requestTimedOut();
		else
		{
			outputReceiver.lineAvailable(line);
			
			if (ip.equals(ipBeingTraced))
			{
				traceFinished = true;
				outputReceiver.traceFinished();
			}
		}
	}
	
	private boolean lineValidation(String line)
	{
		lineCount++;
		
		if (lineCount == 1 && !line.isEmpty())
		{
			outputReceiver.traceError("Unable to resolve the hostname of the destination");
			traceFailed = true;
			return false;
		}
		
		if (lineCount == 2)
		{
			if (line.endsWith("]")) //has ip and hostname
			{
				ipBeingTraced = extractIPFromLine(line);
				outputReceiver.setIPBeingTraced(ipBeingTraced);
				
				String hostname = extractHostnameFromIntroLine(line);
				outputReceiver.setHostnameBeingTraced(hostname);
			}
			else //line contains just an ip
			{
				Matcher matcher = ipPattern.matcher(line);
				matcher.find();
				ipBeingTraced = matcher.group();
				
				outputReceiver.setIPBeingTraced(ipBeingTraced);				
			}
			
			return false;
		}
		
		if (line.startsWith("  1 ") && !ipPattern.matcher(line).find() && !line.contains("*")) //if the first trace hop doesn't contain an IP and it wasn't a timeout, an error occurred
		{
			outputReceiver.traceError(line.substring(5));
			traceFailed = true;
			return false;
		}
		
		if (line.isEmpty())
			return false;
		
		return true;
	}

	@Override
	public void endOfOutput()
	{
		if (!traceFailed && !traceFinished && outputReceiver != null)
			outputReceiver.traceFinished();
	}
	
	public static String extractIPFromLine(String line)
	{
		String[] spaceSeparated = line.split(" ");
		String tempIP = spaceSeparated[spaceSeparated.length - 1];
		
		if (tempIP.startsWith("[")) //then we have a hostname, not just an ip
			tempIP = tempIP.substring(1, tempIP.length() - 1);
		
		return tempIP;
	}
	
	private static String extractHostnameFromIntroLine(String line)
	{
		String[] spaceSeparated = line.split(" ");
		
		return line.charAt(line.length() - 1) == ']' ? spaceSeparated[spaceSeparated.length - 2] : null;
	}
}
