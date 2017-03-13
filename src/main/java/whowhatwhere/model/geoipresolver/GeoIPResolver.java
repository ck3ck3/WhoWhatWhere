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
package whowhatwhere.model.geoipresolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

public class GeoIPResolver
{
	private static final Logger logger = Logger.getLogger(GeoIPResolver.class.getPackage().getName());
	
	private static final String secondaryGeoIpPrefix = "https://www.iplocation.net/?query=";
	private static final String serviceBaseURI = "http://ip-api.com/json/";
	private static final String serviceBaseURIForBatch = "http://ip-api.com/batch/";
	private static final String onlyBasicFields = "?fields=country,region,regionName,city,org,query,status,message";
	private static final String failMsg = "fail";
	private static final int maxQueriesPerMin = 120;
	private static final int maxItemsPerBatchRequest = 100;
	private static final int connectionTimeout = 3000;
	private static final int readTimeout = 3000;
	
	private static int queryCounter = 0;

	/**
	 * @param ip - ip to query
	 * @param extended - if true, will return an instance of GeoIPInfoExtended. Otherwise, will return an instance of GeoIPInfo.
	 * @return
	 */
	public static GeoIPInfo getIPInfo(String ip, boolean extended)
	{

		if (queryCounter == maxQueriesPerMin)
			return generateQuotaReachedInfo();
		else
			addToCounter();

		RequestConfig timeouts = RequestConfig.custom().setConnectTimeout(connectionTimeout).setSocketTimeout(readTimeout).build();
		CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(timeouts).build();
		
		HttpGet getRequest = new HttpGet(serviceBaseURI + ip + (extended ? "" : onlyBasicFields));
		
		try
		{
			CloseableHttpResponse response = httpClient.execute(getRequest);
			String responseText = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
			httpClient.close();
			
			return parseResponse(new JSONObject(responseText), extended);
		}
		catch (IOException ioe)
		{
			logger.log(Level.WARNING, "Unable to get GeoIP info for IP " + ip, ioe);
			return parseResponse(null, false);
		}

	}
	
	public static List<GeoIPInfo> getBatchGeoIPInfo(List<String> ips, boolean extended)
	{
		if (ips.size() == 0)
			return new ArrayList<GeoIPInfo>();
		
		RequestConfig timeouts = RequestConfig.custom().setConnectTimeout(connectionTimeout).setSocketTimeout(readTimeout).build();
		CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(timeouts).build();
		
	    HttpPost request = new HttpPost(serviceBaseURIForBatch + (extended ? "" : onlyBasicFields));
	    try
	    {
	    	int listSize = ips.size();
	    	int itemsLeft = listSize;
	    	int firstIndex = 0;
	    	List<GeoIPInfo> resultList = new ArrayList<>();
	    	
	    	while (itemsLeft > 0)
	    	{
	    		int itemsInThisIteration = Math.min(itemsLeft, maxItemsPerBatchRequest);
	    		int lastIndex = firstIndex + itemsInThisIteration;
	    		List<String> subList = ips.subList(firstIndex, lastIndex);
	    		
		    	String jsonArrayInput = buildJSONArrayFromListOfIPs(subList);
		    	request.setEntity(new StringEntity(jsonArrayInput));
		    	CloseableHttpResponse response = httpClient.execute(request);
		    	
		    	String responseText = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
		    	response.close();
		    	resultList.addAll(buildListOfGeoIPInfoFromJSONArray(new JSONArray(responseText), extended));
		    	
		    	itemsLeft -= itemsInThisIteration;
		    	firstIndex = lastIndex;
	    	}
	    	
	    	httpClient.close();
	    	
	    	return resultList;
	    }
	    catch(IOException ioe)
	    {
	    	logger.log(Level.WARNING, "Unable to get batch GeoIP info. ", ioe);
	    	return null;
	    }
	}
	
	private static String buildJSONArrayFromListOfIPs(List<String> ips)
	{
		StringBuilder builder = new StringBuilder("[");
		
		for(String ip : ips)
			builder.append("{\"query\": \"" + ip + "\"}, ");
		
		int length = builder.length();
		builder.replace(length - 2, length, "]"); //replace the last ", " with "]"
		
		return builder.toString();
	}
	
	private static List<GeoIPInfo> buildListOfGeoIPInfoFromJSONArray(JSONArray jsonArray, boolean extended)
	{
		List<GeoIPInfo> list = new ArrayList<>();
		
		jsonArray.forEach(item -> list.add(parseResponse((JSONObject) item, extended)));
		
		return list;
	}

	private static void addToCounter()
	{
		queryCounter++;

		new Timer().schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				queryCounter--;
			}
		}, 60 * 1000); //run in one minute from now

	}

	private static GeoIPInfo generateQuotaReachedInfo()
	{
		GeoIPInfo ipInfo = new GeoIPInfoExtended();

		ipInfo.setSuccess(false);
		ipInfo.setMessage("Fail: Reached quota of queries per minute (" + maxQueriesPerMin + "). Wait a minute and try again");
		return ipInfo;
	}

	private static GeoIPInfo parseResponse(JSONObject jsonObj, boolean extended)
	{
		GeoIPInfo ipInfo = extended ? new GeoIPInfoExtended() : new GeoIPInfo();

		
		if (jsonObj == null) //geoIP connection timed out
		{
			ipInfo.setSuccess(false);
			ipInfo.setMessage("Fail: Connection to GeoIP service timed out");
			return ipInfo;			
		}

		String status = (String) jsonObj.get("status");
		ipInfo.setStatus(status);

		if (status.equals(failMsg))
		{
			ipInfo.setSuccess(false);
			ipInfo.setMessage("Fail: " + (String) jsonObj.get("message"));
			return ipInfo;
		}

		ipInfo.setSuccess(true);
		ipInfo.setCountry((String) jsonObj.get("country"));
		ipInfo.setRegion((String) jsonObj.get("region"));
		ipInfo.setRegionName((String) jsonObj.get("regionName"));
		ipInfo.setCity((String) jsonObj.get("city"));
		ipInfo.setOrg((String) jsonObj.get("org"));
		ipInfo.setQuery((String) jsonObj.get("query"));
		
		if (extended)
		{
			
			((GeoIPInfoExtended)ipInfo).setCountryCode((String) jsonObj.get("countryCode"));
			((GeoIPInfoExtended)ipInfo).setZip((String) jsonObj.get("zip"));
			((GeoIPInfoExtended)ipInfo).setLat(((Double) jsonObj.get("lat")).toString());
			((GeoIPInfoExtended)ipInfo).setLon(((Double) jsonObj.get("lon")).toString());
			((GeoIPInfoExtended)ipInfo).setTimezone((String) jsonObj.get("timezone"));
			((GeoIPInfoExtended)ipInfo).setIsp((String) jsonObj.get("isp"));
			((GeoIPInfoExtended)ipInfo).setAs((String) jsonObj.get("as"));
		}

		return ipInfo;
	}
	
	public static String getSecondaryGeoIpPrefix()
	{
		return secondaryGeoIpPrefix;
	}
}