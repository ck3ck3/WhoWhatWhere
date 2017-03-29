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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

import whowhatwhere.model.networksniffer.NetworkSniffer;

public class PingConsumer implements Consumer<IPInfoRowModel>, Runnable
{
	private BlockingQueue<IPInfoRowModel> queue;
	private int pingTimeout;
	private int amountOfPings;
	private Semaphore runningSemaphore = new Semaphore(0);
	
	public PingConsumer(BlockingQueue<IPInfoRowModel> queue, int pingTimeout, int amountOfPings)
	{
		this.queue = queue;
		this.pingTimeout = pingTimeout;
		this.amountOfPings = amountOfPings;
	}

	@Override
	public void run()
	{
		int i = 0;
		
		try
		{
			while (i++ < amountOfPings)
				accept(queue.take());
		}
		catch (InterruptedException e) //not a problem, just move on
		{
		}
		
		runningSemaphore.release();
	}
	
	@Override
	public void accept(IPInfoRowModel row)
	{
		String ip = row.ipAddressProperty().get();
		
		String ping = NetworkSniffer.pingAsString(ip, pingTimeout);
		row.setPing(ping);
	}
	
	public Semaphore getRunningSemaphore()
	{
		return runningSemaphore;
	}
}