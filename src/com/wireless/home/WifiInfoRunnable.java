package com.wireless.home;

import java.util.ArrayList;
import java.util.List;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class WifiInfoRunnable implements Runnable
{
	public WifiInfoRunnable(Context c , WAHLogger wahLogger)
	{
		mWAHLogger = wahLogger;//new WAHLogger(mContext);

		mContext = c;
	}
	
	@Override
	public void run()
	{
		Thread thisThread = Thread.currentThread();
		while(((WirelessAtHomeService)mContext).mWifiInfoThread == thisThread)
		{
			boolean scanSuccessful = performSSIDScan();
			if(!scanSuccessful)
			{
				mHandler.post(new Runnable() 
		        {
		            @Override
		            public void run() 
		            { 
		            	try 
		            	{
		            	    Intent intent = new Intent();
		            	    intent.setAction( ((WirelessAtHomeService)mContext).MY_ACTION );
		            	    intent.putExtra("DATAPASSED", 0);
		            	    mContext.sendBroadcast(intent);
		            	}
		            	catch (Exception e) 
		            	{
		            	    e.printStackTrace();
		            	}
		        	}
		        });
			}
			
			try
			{
				Thread.sleep(5000);
			} 
	    	catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

    @SuppressWarnings("unused")
	private void displayToastInUIThread(final String text)
    {
        mHandler.post(new Runnable() 
        {
            @Override
            public void run() 
            { 
            	Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
        	}
        });
    }
    
	@SuppressLint("DefaultLocale")
	public void displayScanResults(List<ScanResult> scanResultList)
	{
		if (AppConstants.SHOW_LOGS) 
			Log.d("WifiInfoRunnable", "displayScanResults() called with scanResultList.size() = " + scanResultList.size());
		
		WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		
		String partialLogString = new String("");
		
//		List<WifiData> wifiDataList = new ArrayList<WifiData>();
		mWifiDataList.clear();	// Cleaning the wifi data recorded before.
		for(ScanResult currentResult : scanResultList)
		{
			//concatenate the scanresult.tostring to the current log string
			partialLogString = partialLogString + " " + currentResult.toString();
			
			String ssid = currentResult.SSID;
			String bssid = currentResult.BSSID.toLowerCase();
			int level = currentResult.level;
			int freq = currentResult.frequency;
			boolean isAP = false;
			if (currentResult.BSSID.equalsIgnoreCase(wifiInfo.getBSSID()))
				isAP = true;
			
			final WifiData wifiData = new WifiData(ssid, bssid, level, freq, isAP);
			mWifiDataList.add(wifiData);
		}
		
		final ArrayList<WifiData> finalWifiDataList = mWifiDataList;
		mHandler.post(new Runnable() 
        {
            @Override
            public void run() 
            { 
            	try 
            	{
            	    Intent intent = new Intent();
            	    intent.setAction( ((WirelessAtHomeService)mContext).MY_ACTION );
            	    intent.putExtra("DATAPASSED", 1);
            	    mContext.sendBroadcast(intent);
            	}
            	catch (Exception e) 
            	{
            	    e.printStackTrace();
            	}
            	
//    			WifiInfoRenderer wifiInfoRenderer = ((MyListActivity)mContext).getWifiInfoRenderer();
//    	    	if(wifiInfoRenderer != null)
//    	    		wifiInfoRenderer.displayWifiData(finalWifiDataList);
            	
//            	((MyListActivity)mContext).showWifiData(finalWifiDataList);
        	}
        });
		
		
		//write out the SSID log to file
		final String logString = partialLogString;
		if(mWAHLogger != null) {
			mHandler.post(new Runnable() 
	        {
	            @Override
	            public void run() 
	            { 
					//send the log string the the logger with the appropriate tag
					if(mWAHLogger.writeToInternalFile(AppConstants.SSID_SAMPLING, logString)) {
						if(AppConstants.SHOW_LOGS) 
							Log.d("WifiInfoRunnable", "SSID Scan results logged to file");
							//mWAHLogger.readFromInternalFile(AppConstants.SSID_SAMPLING);	
					}
	            }
	        });
		}
	}

	public boolean performSSIDScan()
	{
		if(AppConstants.SHOW_LOGS)
			Log.d("WifiInfoRunnable", "performSSIDScan called");
		
		try 
		{
			WifiManager wm = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
			boolean isWifiEnabled = wm.isWifiEnabled();

			if (!isWifiEnabled)
				return false;

			scanResultsReceiver = new ScanResultsReceiver(this);//, this.apID);
			IntentFilter i = new IntentFilter(); 
			i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION); 
			mContext.registerReceiver(scanResultsReceiver, i);
			
			if (AppConstants.SHOW_LOGS) 
				Log.d("WifiInfoRunnable", "scanResultsReceiver has been registered");
			
			// Now you can call this and it should execute the broadcastReceiver's onReceive()
			//wm.reassociate();

			wm.reconnect();

			if(AppConstants.SHOW_LOGS)
				Log.d("WifiInfoRunnable", "Requesting the start of a scan");
			
			boolean a = wm.startScan();
			
			if(AppConstants.SHOW_LOGS)
				Log.d("WifiInfoRunnable", "Request reply: " + a);
		} 
		catch (Exception e) 
		{
			Log.e("WifiInfoRunnable", "Error while starting SSID scan request... " + e.getMessage());
			return false;
		}
		
		return true;
	}

	public ArrayList<WifiData> getWifiDataList()
	{
		return mWifiDataList;
	}
	
	public void removeReciever() 
	{
		try
		{
			mContext.unregisterReceiver(scanResultsReceiver);
			
			scanResultsReceiver = null;
			if (AppConstants.SHOW_LOGS) 
				Log.d("WifiInfoRunnable", "scanResultsReceiver has been unregistered");
		}
		catch (IllegalArgumentException e)
		{
			if (AppConstants.SHOW_LOGS) 
				Log.d("WifiInfoRunnable", "Warning: Unable to unregister scanResultsReceiver");
		}
	}
	
    class ScanResultsReceiver extends BroadcastReceiver 
    {
		private WifiInfoRunnable mWifiInfoRunnable;
		
		public ScanResultsReceiver(WifiInfoRunnable wifiInfoRunnable) //, String apID) 
		{
			mWifiInfoRunnable = wifiInfoRunnable;
		}

		@Override
		public void onReceive(Context context, Intent intent) 
		{
			// Code to execute when SCAN_RESULTS_AVAILABLE_ACTION event occurs
			try
			{
				WifiManager w = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
				mWifiInfoRunnable.displayScanResults(w.getScanResults());

				context.unregisterReceiver(this);
				
				if (AppConstants.SHOW_LOGS) 
					Log.d("WifiInfoRunnable", "scanResultsReceiver (this) has been unregistered");
				
				//Looper.loop();
			} 
			catch (Exception e) 
			{
				if(AppConstants.SHOW_LOGS)
					Log.e("WifiInfoRunnable", "ScanResultsReceiver :: Exception while processing received WiFi scan: " + e.getMessage());
			}
			
		}
	}
    
    private ScanResultsReceiver scanResultsReceiver;
	
    private Context mContext = null;
    private Handler mHandler = new Handler();	// Handler to display UI updates
    private WAHLogger mWAHLogger = null;		// Logger used to log the messages in different files

    private ArrayList<WifiData> mWifiDataList = new ArrayList<WifiData>();
}
