package whowhatwhere.model.geoipresolver;

public class GeoIPInfo
{
	private String status;
	private String country;
	private String countryCode;
	private String region;
	private String regionName;
	private String city;
	private String zip;
	private String lat;
	private String lon;
	private String timezone;
	private String isp;
	private String org;
	private String as;
	private String query;
	private String message; //if status is "fail", info here

	private boolean success;
	
	public GeoIPInfo() {}
	
	
	/**Create an object with success = false
	 * @param fail - success will be set to false regardless of the value of this parameter
	 */
	public GeoIPInfo(boolean fail) { success = false;}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public String getCountry()
	{
		return country;
	}

	public void setCountry(String country)
	{
		this.country = country;
	}

	public String getCountryCode()
	{
		return countryCode;
	}

	public void setCountryCode(String countryCode)
	{
		this.countryCode = countryCode;
	}

	public String getRegion()
	{
		return region;
	}

	public void setRegion(String region)
	{
		this.region = region;
	}

	public String getRegionName()
	{
		return regionName;
	}

	public void setRegionName(String regionName)
	{
		this.regionName = regionName;
	}

	public String getCity()
	{
		return city;
	}

	public void setCity(String city)
	{
		this.city = city;
	}

	public String getZip()
	{
		return zip;
	}

	public void setZip(String zip)
	{
		this.zip = zip;
	}

	public String getLat()
	{
		return lat;
	}

	public void setLat(String lat)
	{
		this.lat = lat;
	}

	public String getLon()
	{
		return lon;
	}

	public void setLon(String lon)
	{
		this.lon = lon;
	}

	public String getTimezone()
	{
		return timezone;
	}

	public void setTimezone(String timezone)
	{
		this.timezone = timezone;
	}

	public String getIsp()
	{
		return isp;
	}

	public void setIsp(String isp)
	{
		this.isp = isp;
	}

	public String getOrg()
	{
		return org;
	}

	public void setOrg(String org)
	{
		this.org = org;
	}

	public String getAs()
	{
		return as;
	}

	public void setAs(String as)
	{
		this.as = as;
	}

	public String getQuery()
	{
		return query;
	}

	public void setQuery(String query)
	{
		this.query = query;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public boolean getSuccess()
	{
		return success;
	}

	public void setSuccess(boolean success)
	{
		this.success = success;
	}

}
