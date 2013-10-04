package com.wireless.home;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Environment;
import android.util.Log;

/**
 * AppUtils: One stop for all the application's utility functions.
 */
public class AppUtils {
	/**
	 * Checks if the battery is charging.
	 * 
	 * @return true if charging, false if not
	 */
	public static boolean checkBattery(Context c) {
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = c.registerReceiver(null, ifilter);
		int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

		return (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL);
	}

	/**
	 * Checks if the device's wifi is connected.
	 * 
	 * @return true if connected, false if not
	 */
	public static boolean checkWifi(Context c) {
		ConnectivityManager connManager = (ConnectivityManager) c
				.getSystemService(MyListActivity.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (mWifi.isConnected())
			return true;
		else
			return false;
	}

	public static double calculateFrequencyFromChannel(int channel, double freq) {
		return freq + ((channel - 28) * 0.312);
	}

	/**
	 * Method to convert ArrayList<ArrayList> to 2-d Array
	 * 
	 * @param colorsArrayList
	 *            ArrayList<ArrayList<Integer>>
	 * @return 2-d Array
	 */
	public static int[][] convertArrayListOfArrayListTo2DArray(
			ArrayList<ArrayList<Integer>> colorsArrayList) {
		int[][] colors = new int[colorsArrayList.size()][colorsArrayList.get(0)
				.size()];

		int i = 0;
		for (ArrayList<Integer> colorArray : colorsArrayList) {
			int j = 0;
			for (Integer colorValue : colorArray) {
				colors[i][j] = colorValue.intValue();
				j++;
			}
			i++;
		}

		return colors;
	}
	
	/**
	 * Method to create JSON Objects out of a JSON String
	 */
	public static void parseStringToJSONObjects() {
		try {
			JSONObject jsonObj = new JSONObject(readJSONFromFile());

			String dev = jsonObj.getString("dev");

			if (AppConstants.SHOW_LOGS) {
				Log.d("MainActivity",
						"MainActivity :: Parsing String to JSON: " + jsonObj);
				Log.d("MainActivity", "MainActivity :: dev=" + dev);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method to read a File with JSON Strings in it.
	 * 
	 * @return String read from the file
	 */
	public static String readJSONFromFile() {
		String jSONString = new String();
		BufferedReader br = null;

		try {
			String fileName = new String(Environment
					.getExternalStorageDirectory().getPath());// + "/DCIM");
			Log.d("MainActivity", "MainActivity :: File Name : " + fileName);
			File tmpdir = new File(fileName);// /home/prakhar/desktop");
			File tmpfile = new File(tmpdir, "test.json");

			InputStreamReader isr = new InputStreamReader(new FileInputStream(
					tmpfile));
			br = new BufferedReader(isr);

			String sCurrentLine;

			// br = new BufferedReader(new
			// FileReader("//home//prakhar//Desktop//test.json"));

			while ((sCurrentLine = br.readLine()) != null) {
				jSONString += sCurrentLine;
				System.out.println(sCurrentLine);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		Log.d("MainActivity", "MainActivity :: JSON String from file: "
				+ jSONString);

		return jSONString;
	}

	/**
	 * Gets the IP address of the device's network
	 * 
	 * @return Local IP Address
	 */
	public static String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("ServerActivity", ex.toString());
		}
		return null;
	}

	/**
	 * Method to write text to internal storage.
	 */
	public static boolean writeToInternalFile(Context context, String fileName,
			String text) {
		boolean result = false;
		FileOutputStream fos = null;

		try {
			fos = context.openFileOutput(fileName, Context.MODE_PRIVATE
					| Context.MODE_APPEND);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}

		if (fos == null)
			return false;
		try {
			text += "\n";
			fos.write(text.getBytes());
			fos.close();

			result = true;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Method to read from a file in internal storage.
	 * 
	 * @param context
	 * @param fileName
	 */
	public static void readFromInternalFile(Context context, String fileName) {
		FileInputStream fis = null;
		try {
			fis = context.openFileInput(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		if (fis == null)
			return;

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					fis, "UTF-8"));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (AppConstants.SHOW_LOGS)
					Log.d("AppUtils", "readFromInternalFile:: " + fileName
							+ ">> " + line.substring(0, line.length()));
			}
			fis.close();
		} catch (OutOfMemoryError om) {
			om.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
    /**
     * Method to initialize the list of files pending to be uploaded to the server.
     */
	public static ArrayList<String> getFilesToUpload(Context c)
	{
		ArrayList<String> filesToUpload = new ArrayList<String>();
		
		// Get internal storage directory
		File internalDir = c.getFilesDir();
		
		if(AppConstants.SHOW_LOGS)
			Log.d("WAHLogger", "Internal Dir Path: " + internalDir.getAbsolutePath());
		
		// Creating a filter that catches "log_device" or "log_energy" files
		// to upload *ONLY LOG* files to the Airshark Server
		FilenameFilter logFileFilter = new FilenameFilter() 
		{
		    public boolean accept(File f, String fileName) 
		    {
		    	return 	fileName.startsWith(AppConstants.SSID_FILE_BASENAME);	
		    }
		};
		
		// Add the log files.
		for(String fileName : internalDir.list(logFileFilter))
		{
			filesToUpload.add(fileName);
			
			if(AppConstants.SHOW_LOGS)
				Log.d("WAHLogger", "Added File: " + fileName);
		}
		
		return filesToUpload;
	}
}
