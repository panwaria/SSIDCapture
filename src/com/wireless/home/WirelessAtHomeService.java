package com.wireless.home;

import java.util.ArrayList;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.provider.Settings.Secure;
import android.util.Log;
import android.widget.Toast;

public class WirelessAtHomeService extends Service 
{
  public void onCreate()
  {
	  // Set the Player
	  if(AppConstants.PLAY_MUSIC)
	  {
	      player = MediaPlayer.create(this, R.raw.sample);
	      player.setLooping(false);
	  }
      
      // Display notification in the notification bar
//      showNotification();
      
      Toast.makeText(this, "Service Created", Toast.LENGTH_SHORT).show();
  }
  
  public void onDestroy()
  {
	  // Remove notification from the Notification bar.
//	  nm.cancel(R.string.hello_world);
	  
	  if(AppConstants.PLAY_MUSIC)
	  {
		  // Stop the player
	      player.stop();
	  }
      
      // TODO: Kill the SSID Scan Thread.
      stopWifiInfoRunnable();
      
      Toast.makeText(this, "Service Destroyed", Toast.LENGTH_SHORT).show();
  }
  
  public int mDeviceID = -1;		// Android Device ID
  public void getDeviceID()
  {
      // Get Device ID
      String mDeviceIDStr = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
		mDeviceID = Math.abs(mDeviceIDStr.hashCode());
		 if(AppConstants.SHOW_LOGS)
		 {
			 Log.d("MainActivity", "mDeviceIDStr = " + mDeviceIDStr);
			 Log.d("MainActivity", "mDeviceID (Hashed) = " + mDeviceID);
		 }
  }
  
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) 
  {
	  if(intent == null)
		  return Service.START_STICKY;
		  
	  if(AppConstants.PLAY_MUSIC)
		  player.start();
	  
	  // NOTE: You can add your API here.
	  
      // STEPS: 
      // 1. Perform SSID Scan on another thread
      // 2. After the scan, convert it to json and store it in a file
      // 3. Upload the file to WAH server
      // 4. Stop/Destroy the service.
	  getDeviceID();
	  startWifiInfoRunnable();
	  
	  return Service.START_STICKY;
  	}

  @Override
  public IBinder onBind(Intent arg0) 
  {
	  return mBinder;
  }

  public class MyBinder extends Binder 
  {
	  WirelessAtHomeService getService() 
	  {
		  return WirelessAtHomeService.this;
	  }
  }
//
//  public List<String> getMACIDList() 
//  {
//	  return mMACIDList;
//  }
  
  public ArrayList<WifiData> getWifiDataList()
  {
	  if(mWifiInfoRunnable != null)
	  {
		  return mWifiInfoRunnable.getWifiDataList();
	  }
	  
	  return null;
  }
  
//  private NotificationManager nm;
//  private void showNotification() 
//  {
//      nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
//      
//      // In this sample, we'll use the same text for the ticker and the expanded notification
//      CharSequence text = "Tap to go to application & stop the service.";
//      
//      // Set the icon, scrolling text and timestamp
//      Notification notification = new Notification(R.drawable.icon_40x40, text, System.currentTimeMillis());
//       
//      // The PendingIntent to launch our activity if the user selects this notification
//      Intent intent = new Intent(this, MyListActivity.class);
//      
//      PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, Notification.FLAG_ONGOING_EVENT);
//      
//      notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
//      // Set the info for the views that show in the notification panel.
//      notification.setLatestEventInfo(getApplicationContext(), "Wireless@Home", text, contentIntent);
//      
//      // Send the notification.
//      // We use a layout id because it is a unique number. We use it later to cancel.
//      nm.notify(R.string.hello_world, notification);
//  }
  
  /**
   * Method to start WiFiInfoRunnable.
   */    
  private void startWifiInfoRunnable()
  {
  	if(mWAHLogger == null)
      	mWAHLogger = new WAHLogger(this);
  	
		if(mWifiInfoRunnable == null)
			mWifiInfoRunnable = new WifiInfoRunnable(this, mWAHLogger);
		
		if(mWifiInfoThread == null) 
		{
			mWifiInfoThread = new Thread(mWifiInfoRunnable);
			mWifiInfoThread.start();
		}	
  }
  
  /**
   * Method to stop WiFiInfoRunnable.
   */
  private void stopWifiInfoRunnable()
  {
  	mWifiInfoRunnable.removeReciever();
  	mWifiInfoThread = null;
  	mWifiInfoRunnable = null;
  }
  
  // MEMBER VARIABLES
  
  private final IBinder mBinder = new MyBinder();
//  private ArrayList<String> mMACIDList = new ArrayList<String>();
//  private ArrayList<WifiData> mWifiDataList = new ArrayList<WifiData>();
  MediaPlayer player;

  // AIRSHARK LOGGER Related
  private WAHLogger mWAHLogger = null;
  
  // WIFI INFO RUNNABLE & THREAD
  private WifiInfoRunnable mWifiInfoRunnable = null;
  public volatile Thread mWifiInfoThread = null;
//  private WifiInfoRenderer mWifiInfoRenderer = null;
  
  public final static String MY_ACTION = "MY_ACTION";

} 