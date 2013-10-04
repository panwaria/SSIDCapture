package com.wireless.home;

public class AppConstants
{
	public static final boolean SHOW_LOGS = true;
	
	// LOGGING DATA Related
	public static final String WAH_SERVER_URL = "http://128.105.22.232/posthere.php";
    public static final String SSID_FILE_BASENAME = "log_ssid_";
    public static final int DATA_UPLOAD_SERVICE_REFRESH_TIME = 60* 1000;	// 1 minute
    
    // JSON Messages Related
	public static final int JSON_INVALID = -1;
    public static final int SSID_SAMPLING = 4;
}
