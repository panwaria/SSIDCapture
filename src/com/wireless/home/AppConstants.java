package com.wireless.home;

public class AppConstants
{
	public static final boolean SHOW_LOGS = true;
	public static final boolean PLAY_MUSIC = true;
	
	
	// LOGGING DATA Related
	public static final String WAH_SERVER_URL = "http://128.105.22.232/insert_wah_ssid.php";
    public static final String SSID_FILE_BASENAME = "log_ssid_";
    public static final int DATA_UPLOAD_SERVICE_REFRESH_TIME = 60* 1000;	// 1 minute
    
    // JSON Messages Related
	public static final int JSON_INVALID = -1;
    public static final int SSID_SAMPLING = 4;
    
    // JSON TAGS
    public static final String TAG_SSIDLIST = "ssidlist";
    public static final String TAG_SSID = "ssid";
    public static final String TAG_LEVEL = "level";
    public static final String TAG_FREQ = "freq";
    public static final String TAG_ISAP = "isap";
    public static final String TAG_BSSID = "bssid";
    public static final String TAG_CAPABILITIES = "capabilities";
    
}
