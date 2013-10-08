package com.wireless.home;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class WifiInfoRunnable implements Runnable
{
	public WifiInfoRunnable(Context c, WAHLogger wahLogger)
	{
		mWAHLogger = wahLogger;// new WAHLogger(mContext);

		mContext = c;
	}

	@Override
	public void run()
	{
		Thread thisThread = Thread.currentThread();
		while (((MyListActivity) mContext).mWifiInfoThread == thisThread)
		{
			boolean scanSuccessful = performSSIDScan();
			if (!scanSuccessful)
			{
				mHandler.post(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							// Intent intent = new Intent();
							// intent.setAction(
							// ((WirelessAtHomeService)mContext).MY_ACTION );
							// intent.putExtra("DATAPASSED", 0);
							// mContext.sendBroadcast(intent);
							//
							((MyListActivity) mContext).ssidScanUnsuccessful();
						} catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				});
			}

			break; // For now, just scanning once.

			// try
			// {
			// Thread.sleep(5000);
			// }
			// catch (InterruptedException e)
			// {
			// e.printStackTrace();
			// }
		}
	}

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
			Log.d("WifiInfoRunnable",
					"displayScanResults() called with scanResultList.size() = "
							+ scanResultList.size());

		WifiManager wifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();

		// String partialLogString = new String("");
		String jsonString = new String("\" { \"" + AppConstants.TAG_SSIDLIST
				+ "\" : [ ");

		int counter = 0;
		mWifiDataList.clear(); // Cleaning the wifi data recorded before.
		for (ScanResult currentResult : scanResultList)
		{
			// concatenate the scanresult.tostring to the current log string
			// partialLogString = partialLogString + " " +
			// currentResult.toString();
			//
			// if (AppConstants.SHOW_LOGS)
			// Log.d("WifiInfoRunnable", "uploaddata-curResult: " +
			// currentResult.toString());

			String ssid = currentResult.SSID;
			String bssid = currentResult.BSSID.toLowerCase();
			int level = currentResult.level;
			int freq = currentResult.frequency;
			boolean isAP = false;
			if (currentResult.BSSID.equalsIgnoreCase(wifiInfo.getBSSID()))
				isAP = true;

			final WifiData wifiData = new WifiData(ssid, bssid, level, freq,
					isAP);
			mWifiDataList.add(wifiData);

			String capabilities = currentResult.capabilities;
			jsonString += "{ " + "\"" + AppConstants.TAG_SSID + "\" : \""
					+ ssid + "\" , " + "\"" + AppConstants.TAG_BSSID
					+ "\" : \"" + bssid + "\" , " + "\""
					+ AppConstants.TAG_LEVEL + "\" : \"" + level + "\" , "
					+ "\"" + AppConstants.TAG_FREQ + "\" : \"" + freq + "\" , "
					+ "\"" + AppConstants.TAG_CAPABILITIES + "\" : \""
					+ capabilities + "\" , " + "\"" + AppConstants.TAG_ISAP
					+ "\" : \"" + isAP + "\" " + " }";

			if (counter++ < scanResultList.size() - 1)
			{
				jsonString += " , ";
			}
		}

		jsonString += " ] } \"";

		if (AppConstants.SHOW_LOGS)
			Log.d("WifiInfoRunnable", "uploaddata-json: " + jsonString);

		final ArrayList<WifiData> finalWifiDataList = mWifiDataList;
		mHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					((MyListActivity) mContext).showWifiData(finalWifiDataList);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});

		// TODO: Start AsyncTask to upload jsonString to the server.
		new UploadDataTask(mContext, jsonString).execute("");
		
//		final String finalJSONString = jsonString; // partialLogString;
//		mHandler.post(new Runnable()
//		{
//			@Override
//			public void run()
//			{
//				// Upload the data to the server.
//				if (AppConstants.SHOW_LOGS)
//					Log.d("WifiInfoRunnable", "uploaddata: " + finalJSONString);
//			}
//		});
	}

	public boolean performSSIDScan()
	{
		if (AppConstants.SHOW_LOGS)
			Log.d("WifiInfoRunnable", "performSSIDScan called");

		try
		{
			WifiManager wm = (WifiManager) mContext
					.getSystemService(Context.WIFI_SERVICE);
			boolean isWifiEnabled = wm.isWifiEnabled();

			if (!isWifiEnabled)
				return false;

			scanResultsReceiver = new ScanResultsReceiver(this);// , this.apID);
			IntentFilter i = new IntentFilter();
			i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
			mContext.registerReceiver(scanResultsReceiver, i);

			if (AppConstants.SHOW_LOGS)
				Log.d("WifiInfoRunnable",
						"scanResultsReceiver has been registered");

			// Now you can call this and it should execute the
			// broadcastReceiver's onReceive()
			// wm.reassociate();

			wm.reconnect();

			if (AppConstants.SHOW_LOGS)
				Log.d("WifiInfoRunnable", "Requesting the start of a scan");

			boolean a = wm.startScan();

			if (AppConstants.SHOW_LOGS)
				Log.d("WifiInfoRunnable", "Request reply: " + a);
		} catch (Exception e)
		{
			Log.e("WifiInfoRunnable",
					"Error while starting SSID scan request... "
							+ e.getMessage());
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
				Log.d("WifiInfoRunnable",
						"scanResultsReceiver has been unregistered");
		} catch (IllegalArgumentException e)
		{
			if (AppConstants.SHOW_LOGS)
				Log.d("WifiInfoRunnable",
						"Warning: Unable to unregister scanResultsReceiver");
		}
	}

	class ScanResultsReceiver extends BroadcastReceiver
	{
		private WifiInfoRunnable mWifiInfoRunnable;

		public ScanResultsReceiver(WifiInfoRunnable wifiInfoRunnable)
		{
			mWifiInfoRunnable = wifiInfoRunnable;
		}

		@Override
		public void onReceive(Context context, Intent intent)
		{
			// Code to execute when SCAN_RESULTS_AVAILABLE_ACTION event occurs
			try
			{
				WifiManager w = (WifiManager) context
						.getSystemService(Context.WIFI_SERVICE);
				mWifiInfoRunnable.displayScanResults(w.getScanResults());

				context.unregisterReceiver(this);

				if (AppConstants.SHOW_LOGS)
					Log.d("WifiInfoRunnable",
							"scanResultsReceiver (this) has been unregistered");

				// Looper.loop();
			} catch (Exception e)
			{
				if (AppConstants.SHOW_LOGS)
					Log.e("WifiInfoRunnable", "ScanResultsReceiver :: Exception while processing received WiFi scan: "
									+ e.getMessage());
			}

		}
	}

	private class UploadDataTask extends AsyncTask<String, Void, String> // <Params, Progress, Result>
	{
		private String mSSIDString;

		public UploadDataTask(Context cxt, String ssidString)
		{
			mSSIDString = ssidString;
		}

		@Override
		protected void onPreExecute()
		{
		}

		@Override
		protected String doInBackground(String... params) 
		{
			if(AppConstants.SHOW_LOGS) 
				Log.d("UploadDataTask", "Trying to upload ssidstring in Background! ssidstring=" + mSSIDString);
			
			boolean uploadSuccessful = false;
	    	try 
	    	{	
	    		// See- http://blog.tacticalnuclearstrike.com/2010/01/using-multipartentity-in-android-applications/
	    		
	    		/*
	    		File file = new File(fileAbsolutePath);
	    		if (!file.exists()) {
	    			if(AppConstants.SHOW_LOGS) 
	        			Log.d("DataUploadService", "Original file missing: " + fileAbsolutePath);
		    		
	    			return null;
	    		}
	    		
	    		if(AppConstants.SHOW_LOGS) 
	    			Log.d("DataUploadService", "File available filename=" + mSSIDString);
				
	    		
	    		try 
	    		{
	    			BufferedInputStream origin = null;
	    			FileOutputStream dest = new FileOutputStream(fileAbsolutePath + ".zip");
	    			
	    			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
	    			
	    			byte data[] = new byte[2048];
	    			
	    			Log.v("Compress", "Adding: " + fileAbsolutePath);
	    			FileInputStream fi = new FileInputStream(fileAbsolutePath);
	    			origin = new BufferedInputStream(fi, 2048);
	    			ZipEntry entry = new ZipEntry(fileAbsolutePath.substring(fileAbsolutePath.lastIndexOf("/") + 1));
	    			out.putNextEntry(entry);
	    			
	    			int count;
	    			while ((count = origin.read(data, 0, 2048)) != -1) 
	    			{
	    				out.write(data, 0, count);
 				}
	    			
	    			origin.close();
	    			out.close();
	    		}
	    		catch(Exception e) 
	    		{
	    			e.printStackTrace();
	    		}
	    		
	    		file = new File(fileAbsolutePath + ".zip");
	    		if (!file.exists()) {
	    			if(AppConstants.SHOW_LOGS) 
	        			Log.d("DataUploadService", "Compressed file missing: " + fileAbsolutePath + ".zip");
		    		
	    			return null;
	    		}
	    		*/
	    		
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(AppConstants.WAH_SERVER_URL);
				 
				// http://stackoverflow.com/questions/2938502/sending-post-data-in-android
				// Add your data
				String deviceIDStr = ((MyListActivity)mContext).mDeviceID.toString();
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		        nameValuePairs.add(new BasicNameValuePair("clientid", deviceIDStr));
		        nameValuePairs.add(new BasicNameValuePair("ssidstring", mSSIDString));
		        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	
		        // Execute HTTP Post Request
		        HttpResponse response = httpclient.execute(httppost);
	    		 
	    		 /*
	    		FileBody bin = new FileBody(file, "multipart/form-data");
	    		 
	    		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
	    		reqEntity.addPart("userfile", bin);
	    		 
	    		httppost.setEntity(reqEntity);
	    		 
	    		if(AppConstants.SHOW_LOGS) 
     			Log.d("DataUploadService", "Sending file: " + fileAbsolutePath + ".zip");
	    		
	    		HttpResponse response = httpclient.execute(httppost);
	    		*/
	    		 
				
     	    BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
 	    	String line = "";
     	    while ((line = rd.readLine()) != null) 
     	    {
     	    	if(AppConstants.SHOW_LOGS) 
	        			Log.d("UploadDataTask", "HTTP_Response: " + line);
     	    
     	    	if(line.contains("success"))
     	    	{
     	    		uploadSuccessful = true;
     	    		break;
     	    	}
 	    	}
     	    
     	    if(uploadSuccessful)
     	    {
     	    	if(AppConstants.SHOW_LOGS)
     	     		Log.d("UploadDataTask", "SUCCESSFULLY UPLOADED the deviceid: " + deviceIDStr  + " ssidstring: " + mSSIDString);
     	    	
     	    	displayToastInUIThread("Database Updated.");
     	    }
     	    else
     	    {
     	    	if(AppConstants.SHOW_LOGS)
     	     		Log.d("UploadDataTask", "NOT ABLE TO UPLOAD the deviceid: " + deviceIDStr  + " ssidstring: " + mSSIDString);
     	    }
        }
     	catch (Exception e)
     	{
     	    // Show error
     		e.printStackTrace();
     		return null;
     	}
	        
	    	return "";
		}

		protected void onPostExecute(String obj)
		{
		}
	}

	private ScanResultsReceiver scanResultsReceiver;

	private Context mContext = null;
	private Handler mHandler = new Handler(); // Handler to display UI updates
	private WAHLogger mWAHLogger = null; // Logger used to log the messages in
											// different files

	private ArrayList<WifiData> mWifiDataList = new ArrayList<WifiData>();
}
