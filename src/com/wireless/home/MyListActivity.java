package com.wireless.home;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MyListActivity extends ListActivity
{
//    private WirelessAtHomeService mWAHService;
    private ArrayAdapter<String> mAdapter;
    private List<String> mMACIDList;
    
    // WIFI INFO RUNNABLE & THREAD
    private WifiInfoRunnable mWifiInfoRunnable = null;
    public volatile Thread mWifiInfoThread = null;
    
    // AIRSHARK LOGGER Related
    private WAHLogger mWAHLogger = null;
    
//    private ArrayList<WifiData> mWifiDataList;
//    private ArrayAdapter<WifiData> mWifiDataAdapter;
    
    private boolean mIsBound = false;
    
    // DATA UPLOAD SERVICE
    private boolean mIsDataUploadServiceBound = false;
	@SuppressWarnings("unused")
	private DataUploadService mDataUploadService;// = new DataUploadService(this);
    
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_mylist);
    	
//    	mMACIDList = new ArrayList<String>();
//    	mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, mMACIDList);
//    	setListAdapter(mAdapter);
    	
//    	doBindWAHService();
    	
		getDeviceID();
    }
    
    public void onResume()
    {
    	// Setting up the broadcast receiver to receive any signal when to update SSID List.
//    	mMyReceiver = new MyReceiver();
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(WirelessAtHomeService.MY_ACTION);
//        registerReceiver(mMyReceiver, intentFilter);
//        
    	// Stop if Data Upload Service already running
//		stopDataUploadService();
		
        super.onResume();
    }
    
    public void onPause()
    {
//    	unregisterReceiver(mMyReceiver);
    	
    	// Now is the best time to upload recorded data.
//    	uploadRecordedSSIDData();
    	
    	super.onPause();
    }
  
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        
        try 
        {
        	// Just unbind the services from this application, do not stop it.
//            doUnbindDataUploadService();
        	
//            doUnbindWAHService();
        } 
        catch (Throwable t) { }
        
        // Kill the SSID Scan Thread.
        stopWifiInfoRunnable();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
	public boolean onOptionsItemSelected(MenuItem item) 
	{
	    // Handle item selection
	    switch (item.getItemId()) 
	    {
	        case R.id.menu_quit:
	        	
				Toast.makeText(this, "Stopping Wireless@Home Service", Toast.LENGTH_SHORT).show();
				
				// Unbinding from the service and stopping it
//				doUnbindWAHService();
//				stopService(new Intent(MyListActivity.this, WirelessAtHomeService.class));
				
				// Destroying activity
				finish();
				
	            return true;
	            
	        default:
	            return false;
	    }
	}
    
    // WAH SERVICE RELATED
	/*
    private ServiceConnection mWAHServiceConnection = new ServiceConnection() 
    {
    	public void onServiceConnected(ComponentName className, IBinder binder)
    	{
    		mWAHService = ((WirelessAtHomeService.MyBinder) binder).getService();
    		Toast.makeText(MyListActivity.this, "Connected to the service", Toast.LENGTH_SHORT).show();
    	}
    	
    	public void onServiceDisconnected(ComponentName className) 
    	{
    		mWAHService = null;
    		Toast.makeText(MyListActivity.this, "Disconnected from the service", Toast.LENGTH_SHORT).show();
    	}
    };
    
    void doBindWAHService() 
    {
    	mIsBound = true;
    	bindService(new Intent(this, WirelessAtHomeService.class), mWAHServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void showWAHServiceData() 
    {
      if (mWAHService != null) 
      {
//        Toast.makeText(this, "Number of elements = " + mWAHService.getMACIDList().size(), Toast.LENGTH_SHORT).show();
//        
//        mMACIDList.clear();
//        mMACIDList.addAll(mWAHService.getMACIDList());
//        mAdapter.notifyDataSetChanged();
      }
    }
	
	void doUnbindWAHService() 
	{
        if (mIsBound) 
        {
            // Detach our existing connection.
            unbindService(mWAHServiceConnection);
            mIsBound = false;
            
            Toast.makeText(this, "Unbinding Wireless@Home Service", Toast.LENGTH_SHORT).show();
        }
    }
	*/
	
	
	// UI UPDATES RELATED
	
	public void onClick(View v)
	{
		switch(v.getId())
		{
		case R.id.mylist_button_start:
			
			Toast.makeText(this, "Starting WifiInfoRunnable", Toast.LENGTH_SHORT).show();

			if(mWifiInfoRunnable == null)
				startWifiInfoRunnable();
			else
			{
				// Restart WifiInfoRunnable;
				stopWifiInfoRunnable();
				startWifiInfoRunnable();
			}
			
//			Toast.makeText(this, "Starting Wireless@Home Service", Toast.LENGTH_SHORT).show();
//		    startService(new Intent(MyListActivity.this, WirelessAtHomeService.class));
		    
		    break;
		    
		case R.id.mylist_button_stop:
			
			Toast.makeText(this, "Stopping WifiInfoRunnable", Toast.LENGTH_SHORT).show();
			stopWifiInfoRunnable();
			
//			Toast.makeText(this, "Stopping Wireless@Home Service", Toast.LENGTH_SHORT).show();
			
//			doUnbindWAHService();
//			stopService(new Intent(MyListActivity.this, WirelessAtHomeService.class));
			
			break;
			
		case R.id.mylist_button_results:
			
//			showWAHServiceData();
			break;
		}
	}

     @Override
    protected void onListItemClick(ListView l, View v, int position, long id) 
    {
    	// Starting ListItem Activity
    	Intent intent = new Intent(MyListActivity.this, ListItemActivity.class);
    	intent.putExtra("macid", ((TextView)v).getText().toString());
    	startActivity(intent);

      super.onListItemClick(l, v, position, id);
    }
    
     /*
    private MyReceiver mMyReceiver = null;
    private class MyReceiver extends BroadcastReceiver
    {
    	 @Override
    	 public void onReceive(Context arg0, Intent arg1) 
    	 {
		 	int datapassed = arg1.getIntExtra("DATAPASSED", 0);
		 	
		 	if(datapassed == 0)
		 		ssidScanUnsuccessful();
		 	else if (datapassed == 1)
		 		updateWifiData();
		 	else
		 		Toast.makeText(	MyListActivity.this, 
		 					"Triggered by Service!\n" + "Data passed: " + String.valueOf(datapassed),
	 						Toast.LENGTH_LONG).show();
    	 }
	}
	*/
    
    public void ssidScanUnsuccessful()
    {
		TextView noWifiInfoMessage = (TextView) findViewById(R.id.wifiinfo_no_device_message);
		
		if(noWifiInfoMessage != null)
		{
			noWifiInfoMessage.setVisibility(View.VISIBLE);
			noWifiInfoMessage.setText(this.getResources().getString(R.string.ssid_not_avaliable));
		}
    }
    
//    public void updateWifiData()
//    {
//    	ArrayList<WifiData> wifiDataList = mWAHService.getWifiDataList();
//    	
//    	showWifiData(wifiDataList);
//    }
    
    public void showWifiData(ArrayList<WifiData> wifiDataList)
    {
//    	mWifiDataList = wifiDataList;
//    	mWifiDataAdapter.notifyDataSetChanged();
    	   	
		if(wifiDataList == null || wifiDataList.size() == 0)
			return;
		
		Log.d("MyListActivity", "showWifiData :: wifiDataList.size() = " + wifiDataList.size());
		
		findViewById(R.id.wifiinfo_no_device_message).setVisibility(View.INVISIBLE);
		
		if(AppConstants.SHOW_LOGS)
			Log.d("WifiInfoRenderer", "DisplayWifiData called.");
		
		Collections.sort(wifiDataList, new Comparator<WifiData>() 
		{
			@Override
			public int compare(WifiData lhsWifiData, WifiData rhsWifiData) 
			{
				return lhsWifiData.getLevel() < rhsWifiData.getLevel() ? 1 : -1;
			}
		});
		
		WifiInfoAdapter adapter = new WifiInfoAdapter(this, R.layout.wifiinfo_list_item, wifiDataList); //new WifiInfoAdapter(mContext, mDataList);
		
		ListView dataListView = (ListView) findViewById(R.id.list_wifiinfo);
		dataListView.setVisibility(View.VISIBLE);
		dataListView.setAdapter(adapter);
    }

    
    // DATA UPLOAD SERVICE RELATED
        
    private void uploadRecordedSSIDData()
    {
    	boolean isWifiConnected = AppUtils.checkWifi(this);
//    	boolean isDeviceCharging = AppUtils.checkBattery(this);

		ArrayList<String> filesToUpload = AppUtils.getFilesToUpload(this);
		if(filesToUpload.size() > 0 && isWifiConnected)// && isDeviceCharging)
			startDataUploadService(filesToUpload);
		else if(AppConstants.SHOW_LOGS)
			Log.d("MainActivity", "No files to upload. So, not starting service");
    }
    
    // Create connection with the service.
    private ServiceConnection mDataUploadServiceConnection = new ServiceConnection() 
    {
    	public void onServiceConnected(ComponentName className, IBinder binder)
    	{
    		mDataUploadService = ((DataUploadService.MyBinder) binder).getService();
    		
    		if(AppConstants.SHOW_LOGS)
    			Log.d("MainActivity", "Connected to the Data Upload service");
    	}
    	
    	public void onServiceDisconnected(ComponentName className) 
    	{
    		mDataUploadService = null;
    		
    		if(AppConstants.SHOW_LOGS)
    			Log.d("MainActivity", "Disconnected from the Data Upload service");
    	}
    };
    
    /**
     * Method to start the data upload service,
     * 
     * @param listOfFiles	List of files to be uploaded.
     */
    private void startDataUploadService(ArrayList<String> listOfFiles)
    {
    	// Bind the application with the service
    	doBindDataUploadService();
    	
    	// Then, start the service.
		Intent startServiceIntent = new Intent(MyListActivity.this, DataUploadService.class);
		startServiceIntent.putExtra("ListOfFiles", listOfFiles);
		
	    startService(startServiceIntent);
    }

    /**
     * Method to stop the data upload service.
     */
    private void stopDataUploadService()
    {
    	// Unbind the application with the service.
    	doUnbindDataUploadService();
    	
    	// Then, stop the service.
		stopService(new Intent(MyListActivity.this, DataUploadService.class));
    }
    
    /**
     * Method to bind the data upload service.
     */
    void doBindDataUploadService() 
    {
    	bindService(new Intent(this, DataUploadService.class), mDataUploadServiceConnection, Context.BIND_AUTO_CREATE);
    	mIsDataUploadServiceBound = true;
    	
    	if(AppConstants.SHOW_LOGS)
    		Log.d("MainActivity", "Binding Data Upload Service");
    }
	
    /**
     * Method to unbind the data upload service.
     */
	void doUnbindDataUploadService() 
	{
        if (mIsDataUploadServiceBound) 
        {
            // Detach our existing connection.
            unbindService(mDataUploadServiceConnection);
            mIsDataUploadServiceBound = false;
            
            if(AppConstants.SHOW_LOGS)
            	Log.d("MainActivity", "Unbinding Data Upload Service");
        }
    }
	
	// WIFI INFO RUNNABLE
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
		if(mWifiInfoRunnable != null)
			mWifiInfoRunnable.removeReciever();
		
	  	mWifiInfoThread = null;
	  	mWifiInfoRunnable = null;
	  }
	  
	  public Integer mDeviceID = -1;		// Android Device ID
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

} 