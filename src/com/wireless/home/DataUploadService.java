package com.wireless.home;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class DataUploadService extends Service 
{
	/**
	 * Constructor
	 * @param context the context of the main activity
	 */
//	public DataUploadService(Context context) {
//		mContext = context;
//	}
	
	public void onCreate()
	{
		if(AppConstants.SHOW_LOGS)
			Log.d("DataUploadService", "onCreate: Data Upload Service Created");
	    
		// Display notification in the notification bar
		if(AppConstants.SHOW_LOGS)
			showNotification();
	}
	
	public void onDestroy()
	{
		if(AppConstants.SHOW_LOGS)
			Log.d("DataUploadService", "onDestroy: Service Destroyed");
		
		// Remove notification from the Notification bar.
		if(nm != null)
			nm.cancel(R.string.app_name);
	}
	
	/**
	 * This method is called when the service is started.
	 * @return constant to tell system that the service is not sticky
	 */
	@SuppressWarnings("unchecked")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		if(AppConstants.SHOW_LOGS)
			Log.d("DataUploadService", "onStartCommand: Starting Data Upload Service");
		
		mFilesToUpload = (ArrayList<String>) intent.getSerializableExtra("ListOfFiles");
		// Need to upload following log files.
		for(String fileName : mFilesToUpload)
		{
			if(AppConstants.SHOW_LOGS)
				Log.d("DataUploadService", "Detected File to Upload: " + fileName);
		}
		
		while (!mIsComplete) 
		{
			if (/*AppUtils.checkBattery(this) &&*/ AppUtils.checkWifi(this)) 
			{
				// Get files to upload.
				sendLogFilesToServer();
				
				mIsComplete = true;
			} 
			else 
			{
				try 
				{
					Thread.sleep(AppConstants.DATA_UPLOAD_SERVICE_REFRESH_TIME);
				}
				catch (InterruptedException e) {}
			}
		}
		
		return Service.START_NOT_STICKY;
	}
	
	/**
	 * Sends a notification to the device's notification center.
	 */
	  private void showNotification() 
	  {
		  if(AppConstants.SHOW_LOGS)
			  Log.d("DataUploadService", "showNotification called");
		
	      nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
	      
	      // The PendingIntent to launch our activity if the user selects this notification
//	      Intent resultIntent = new Intent(this, MainActivity.class);
//	      resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//	      resultIntent.putExtra("KillUploadService", true);

//			PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, 0);//Notification.FLAG_ONGOING_EVENT);
	      

//	      notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
	      // Set the info for the views that show in the notification panel.
	      NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
	         .setContentTitle(getResources().getString(R.string.service_notification_title))
	         .setContentText(getResources().getString(R.string.service_notification_text))
	         .setSmallIcon(R.drawable.server_upload);
	      
//	      notificationBuilder.setContentIntent(resultPendingIntent);
	      
	      
	      // Send the notification.
	      // We use a layout id because it is a unique number. We use it later to cancel.
	      nm.notify(R.string.app_name, notificationBuilder.build());
	  }
	
	/**
	 * Returns the binder object
	 * @return binder object
	 */
	@Override
	public IBinder onBind(Intent intent) {	return mBinder; }
	
	/**
	 * Private object container.
	 */
	public class MyBinder extends Binder 
	{
		/**
		 * Constructor
		 * @return Data upload service object
		 */
		DataUploadService getService() 
		{
			return DataUploadService.this;
		}
	}
	
	/**
	 * Sends the device data to be stored on the server.
	 */
	private void sendLogFilesToServer() 
	{
		if(mFilesToUpload == null || mFilesToUpload.size() == 0)
			return;
		
		// Iterate through all the files
		for (String fileName : mFilesToUpload)
		{
			new UploadDataTask(this, fileName).execute("");
		}
		
		// StopSelf once we are done uploading all the files.
		if(AppConstants.SHOW_LOGS) 
			Log.d("DataUploadService", "stopSelf: Service Suicide!");
		this.stopSelf();
	}
	   
    private class UploadDataTask extends AsyncTask<String, Void, String> //<Params, Progress, Result>
    {
    	private String mFileName;
    	
       	public UploadDataTask(Context cxt, String fileName) 
    	{
            mFileName = fileName;
        }

       	@Override
        protected void onPreExecute() { }
    	
		@Override
		protected String doInBackground(String... params) 
		{
			if(AppConstants.SHOW_LOGS) 
    			Log.d("DataUploadService", "Trying to upload file in Background! filename=" + mFileName);
			
			boolean uploadSuccessful = false;
	    	try 
	    	{	
	    		// See- http://blog.tacticalnuclearstrike.com/2010/01/using-multipartentity-in-android-applications/
	    		String internalDirPath = getFilesDir().getAbsolutePath();
	    		String fileAbsolutePath = internalDirPath + "/" + mFileName;
	    		
	    		File file = new File(fileAbsolutePath);
	    		if (!file.exists()) {
	    			if(AppConstants.SHOW_LOGS) 
	        			Log.d("DataUploadService", "Original file missing: " + fileAbsolutePath);
		    		
	    			return null;
	    		}
	    		
	    		if(AppConstants.SHOW_LOGS) 
	    			Log.d("DataUploadService", "File available filename=" + mFileName);
				
	    		
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
	    		
	    		HttpClient httpclient = new DefaultHttpClient();
	    		HttpPost httppost = new HttpPost(AppConstants.WAH_SERVER_URL);
	    		 
	    		FileBody bin = new FileBody(file, "multipart/form-data");
	    		 
	    		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
	    		reqEntity.addPart("userfile", bin);
	    		 
	    		httppost.setEntity(reqEntity);
	    		 
	    		if(AppConstants.SHOW_LOGS) 
        			Log.d("DataUploadService", "Sending file: " + fileAbsolutePath + ".zip");
	    		
	    		HttpResponse response = httpclient.execute(httppost);
	    		 
        	    BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
    	    	String line = "";
        	    while ((line = rd.readLine()) != null) 
        	    {
        	    	if(AppConstants.SHOW_LOGS) 
	        			Log.d("DataUploadService", "HTTP_Response: " + line);
        	    
        	    	if(line.contains("success"))
        	    	{
        	    		uploadSuccessful = true;
        	    		break;
        	    	}
    	    	}
        	    
        	    if(uploadSuccessful)
        	    {
        	    	if(AppConstants.SHOW_LOGS)
        	     		Log.d("DataUploadService", "Done uploading the file: " + mFileName);

        	    	boolean isZipDeleted = file.delete();
        	    	
        	    	File originalFile = new File(fileAbsolutePath);
        	    	boolean isOriginalDeleted = originalFile.delete();
        	    	
        	    	if(AppConstants.SHOW_LOGS) 
	        			Log.d("DataUploadService", "Deleted the file (" + mFileName + "):" + isOriginalDeleted + " (.zip):" + isZipDeleted);
        	    	
        	    	mFilesToUpload.remove(mFileName);
        	    }
        	    else
        	    {
        	    	boolean isZipDeleted = file.delete();
        	    	
        	    	if(AppConstants.SHOW_LOGS) 
	        			Log.d("DataUploadService", "Deleted the file (" + mFileName+ " (.zip):" + isZipDeleted);
        	    	
        	    	if(AppConstants.SHOW_LOGS)
        	     		Log.d("DataUploadService", "Not able to uploaded the file: " + mFileName);
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
		
	     protected void onPostExecute(String obj) { }
    }

    // Member Variables
    
	private final IBinder mBinder = new MyBinder();
	private boolean mIsComplete;
	private ArrayList<String> mFilesToUpload = null;
    private NotificationManager nm;
}