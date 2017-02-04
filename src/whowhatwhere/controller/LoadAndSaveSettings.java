package whowhatwhere.controller;

import java.util.Properties;

public interface LoadAndSaveSettings
{
	public void saveCurrentRunValuesToProperties(Properties props);
	public void loadLastRunConfig(Properties props);
}
