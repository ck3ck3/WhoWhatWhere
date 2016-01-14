package mostusedips.model.geoipresolver;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import mostusedips.Main;

public class GeoIPResolver
{

	private static final String serviceBaseURI = "http://ip-api.com/xml/";
	private static final String failMsg = "fail";
	private static final Logger logger = Logger.getLogger(Main.getAppName());
	private static final int maxQueriesPerMin = 120;
	private static final int connectionTimeout = 1500;
	private static final int readTimeout = 1500;
	private static int queryCounter = 0;

	public static GeoIPInfo getIPInfo(String ip)
	{

		if (queryCounter == maxQueriesPerMin)
			return generateQuotaReachedInfo();
		else
			addToCounter();

		Document doc;

		try
		{
			URLConnection serviceURL = new URL(serviceBaseURI + ip).openConnection();
			serviceURL.setConnectTimeout(connectionTimeout);
			serviceURL.setReadTimeout(readTimeout);
			
			InputStream serviceInputStream = serviceURL.getInputStream();
			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			doc = docBuilder.parse(serviceInputStream);
			serviceInputStream.close();
		}
		catch (Exception e)
		{
			logger.log(Level.SEVERE, "Unable to get GeoIP info for IP " + ip, e);
			return parseResponse(null);
		}

		return parseResponse(doc);
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
		GeoIPInfo ipInfo = new GeoIPInfo();

		ipInfo.setSuccess(false);
		ipInfo.setMessage("Fail: Reached quota of queries per minute (" + maxQueriesPerMin + "). Right click to use a different GeoIP service or wait a minute and try again");
		return ipInfo;
	}

	private static GeoIPInfo parseResponse(Document doc)
	{
		GeoIPInfo ipInfo = new GeoIPInfo();

		NodeList elements;
		String text;
		
		if (doc == null) //geoIP connection timed out
		{
			ipInfo.setSuccess(false);
			ipInfo.setMessage("Fail: Connection to GeoIP service timed out");
			return ipInfo;			
		}

		elements = doc.getElementsByTagName("status");
		text = elements.item(0).getTextContent();
		ipInfo.setStatus(text);

		if (text.equals(failMsg))
		{
			ipInfo.setSuccess(false);
			ipInfo.setMessage("Fail: " + doc.getElementsByTagName("message").item(0).getTextContent());
			return ipInfo;
		}

		ipInfo.setSuccess(true);
		ipInfo.setStatus(doc.getElementsByTagName("status").item(0).getTextContent());
		ipInfo.setCountry(doc.getElementsByTagName("country").item(0).getTextContent());
		ipInfo.setCountryCode(doc.getElementsByTagName("countryCode").item(0).getTextContent());
		ipInfo.setRegion(doc.getElementsByTagName("region").item(0).getTextContent());
		ipInfo.setRegionName(doc.getElementsByTagName("regionName").item(0).getTextContent());
		ipInfo.setCity(doc.getElementsByTagName("city").item(0).getTextContent());
		ipInfo.setZip(doc.getElementsByTagName("zip").item(0).getTextContent());
		ipInfo.setLat(doc.getElementsByTagName("lat").item(0).getTextContent());
		ipInfo.setLon(doc.getElementsByTagName("lon").item(0).getTextContent());
		ipInfo.setTimezone(doc.getElementsByTagName("timezone").item(0).getTextContent());
		ipInfo.setIsp(doc.getElementsByTagName("isp").item(0).getTextContent());
		ipInfo.setOrg(doc.getElementsByTagName("org").item(0).getTextContent());
		ipInfo.setAs(doc.getElementsByTagName("as").item(0).getTextContent());
		ipInfo.setQuery(doc.getElementsByTagName("query").item(0).getTextContent());

		return ipInfo;
	}

}
