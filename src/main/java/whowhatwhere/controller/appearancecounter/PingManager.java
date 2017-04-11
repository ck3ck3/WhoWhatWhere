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
package whowhatwhere.controller.appearancecounter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import whowhatwhere.model.networksniffer.NetworkSniffer;

public class PingManager
{
	private int finishedPingsCounter;
	private int amountOfPingTasks;
	private int pingTimeout;
	private ExecutorService pingThreadPool;
	
	public PingManager(int amountOfPingTasks, int pingTimeout, int numOfPingThreads)
	{
		finishedPingsCounter = 0;
		this.amountOfPingTasks = amountOfPingTasks;
		this.pingTimeout = pingTimeout;
		
		pingThreadPool = Executors.newFixedThreadPool(numOfPingThreads);
	}
	
	public boolean isOngoing()
	{
		return !pingThreadPool.isShutdown();
	}
	
	public void shutdown()
	{
		pingThreadPool.shutdownNow();
	}
	
	public boolean awaitTermination(long timeoutSeconds) throws InterruptedException
	{
		return pingThreadPool.awaitTermination(timeoutSeconds, TimeUnit.SECONDS);
	}
	
	public void addPingTask(IPInfoRowModel row)
	{
		pingThreadPool.submit(new PingTask(row));
	}
	
	////////////////////////////////////////////////////////
	
	
	private class PingTask implements Runnable
	{
		private IPInfoRowModel row;
		
		public PingTask(IPInfoRowModel row)
		{
			this.row = row;
		}
		
		@Override
		public void run()
		{
			String ip = row.ipAddressProperty().get();
			
			String ping = NetworkSniffer.pingAsString(ip, pingTimeout);
			row.setPing(ping);
			
			finishedPingsCounter++;
			
			if (finishedPingsCounter == amountOfPingTasks) //we're done with this batch
				pingThreadPool.shutdown();
		}		
	}
}
