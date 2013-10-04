package com.wireless.home;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import android.content.Context;
import android.util.Log;

public class WAHLogger
{
	/**
	 * Default Constructor
	 * 
	 * @param context	Activity Context
	 */
	public WAHLogger(Context context)
	{
		mContext = context;
		
		initializeFilesToUpload();
	}
	
    /**
     * Method to write text to internal storage.
     */
    public boolean writeToInternalFile(int jsonType, String text)
    {
    	if(text == null || text.length() == 0 || jsonType == AppConstants.JSON_INVALID)	return false;
    	
    	String fileName = "";
    	
    	boolean canUploadCurrentLogFile = updateCurrentLogCount(jsonType);
    	if(canUploadCurrentLogFile)
    	{
    		// Push current file to List of Files that can be uploaded.
    		allowCurrentFileToUpload(jsonType);
    		
    		// Create a new file.
    		fileName = createNewLogFile(jsonType);
			if(AppConstants.SHOW_LOGS)
				Log.d("WAHLogger", "writeToInternalFile :: FileName=" + fileName);
    	}
    	else
    		fileName = getCurrentLogFileName(jsonType);
    	
    	if(fileName == null || fileName.equals(""))	 return false;
    	
    	boolean result = false;
    	FileOutputStream fos = null;
    	
		try
		{
			// Creates a file at '/data/data/com.wisense/files/<fileName>'
			fos = mContext.openFileOutput(fileName, Context.MODE_PRIVATE | Context.MODE_APPEND);
		} 
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return false;
		}
		
		if(fos == null) return false;
		
    	try
		{
    		text += "\n";
			fos.write(text.getBytes());
	    	fos.close();
	    	
	    	result = true;
		} 
    	catch (IOException e)
		{
			e.printStackTrace();
		}
    	
    	if(AppConstants.SHOW_LOGS)
    		Log.d("WAHLogger", "Filename=" + fileName + " update with Content='" + text + "'");
    	
    	return result;
    }
    
    /**
     * Method to read from a file in internal storage.
     * @param context
     * @param fileName
     */
    public void readFromInternalFile(int jsonType)
    {
    	String fileName = "";
    	if((fileName = getCurrentLogFileName(jsonType)) == null)	return;
    		
    	FileInputStream fis = null;
    	try
		{
			fis = mContext.openFileInput(fileName);
		}
    	catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return;
		}
    	
    	if(fis == null) return;
    	
    	if(AppConstants.SHOW_LOGS)
    	{
	    	try
	    	{
	            BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
	            String line = null;
	            while ((line = reader.readLine()) != null) 
					Log.d("WAHLogger", "readFromInternalFile:: " + fileName + ">> " + line.substring(0, line.length()));
	        } 
	    	catch(OutOfMemoryError om)
	    	{	om.printStackTrace();  } 
	    	catch(Exception ex)
	    	{   ex.printStackTrace();  }
    	}
    	
    	// Close FileInputStream
    	try
		{
			fis.close();
		} 
    	catch (IOException e)
		{	e.printStackTrace();  }
    }
    
    public ArrayList<String> getFilesToUpload()
    {
    	return mFilesToUpload;
    }
    
    private String createNewLogFile(int jsonType)
    {
    	String baseName = "";
    	switch(jsonType)
    	{
    	case AppConstants.SSID_SAMPLING: 
			baseName = AppConstants.SSID_FILE_BASENAME;
			break;
    		
    	default:
			return null;
    	}
    	
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yy_MM_dd_HH_mm_ss", Locale.ENGLISH);
    	String fileName = baseName + ((WirelessAtHomeService)mContext).mDeviceID + "_" + dateFormat.format(new Date());
    	
    	switch(jsonType)
    	{
    	case AppConstants.SSID_SAMPLING:
    		mCurrentSSIDLogFileName = fileName;
    		break;
    	}
    	
    	return fileName;
    }
    
    private String getCurrentLogFileName(int jsonType)
    {
    	String fileName = null;
    	switch(jsonType)
    	{
    	case AppConstants.SSID_SAMPLING:
    		
    		fileName = mCurrentSSIDLogFileName;
    		break;
    		
    	default:
			return null;
    	}
    	
		if(fileName == null)
		{
			fileName = createNewLogFile(jsonType);
			
			if(AppConstants.SHOW_LOGS)
				Log.d("WAHLogger", "getCurrentLogFileName :: jsonType=" + jsonType + " FileName=" + fileName);
		}
		
		return fileName;
    }
	
    /**
     * Method to initialize the list of files pending to be uploaded to the server.
     */
	private void initializeFilesToUpload()
	{
		mFilesToUpload = new ArrayList<String>();
		
		// Get internal storage directory
		File internalDir = mContext.getFilesDir();
		
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
			mFilesToUpload.add(fileName);
			
			if(AppConstants.SHOW_LOGS)
				Log.d("WAHLogger", "Added File: " + fileName);
		}
	}
	
	/**
	 * Add the current log file to the list of files to be uploaded. This list of files will 
	 * then to picked by the DataUploadService to upload the files to the Airshark Server.
	 * 
	 * @param jsonType	Type of message
	 */
	private void allowCurrentFileToUpload(int jsonType)
	{
		String fileName = getCurrentLogFileName(jsonType);
		mFilesToUpload.add(fileName);
		
		resetCurrentLogFileName(jsonType);
	}
	
	/**
	 * Method to reset current log files name
	 * 
	 * @param jsonType	Type of message
	 */
	private void resetCurrentLogFileName(int jsonType)
	{
    	switch(jsonType)
    	{
    	case AppConstants.SSID_SAMPLING:
    		
    		mCurrentSSIDLogFileName = null;
    		break;
    	}
	}
	
    private boolean updateCurrentLogCount(int jsonType)
    {
    	boolean canUploadCurrentLogFile = false;
    	switch(jsonType)
    	{
    	case AppConstants.SSID_SAMPLING:
    		mCurrentSSIDLogCount++;
    		
    		if(mCurrentSSIDLogCount > MAX_MESSAGES_LIMIT_IN_FILE)
    		{
    			canUploadCurrentLogFile = true;
    			mCurrentSSIDLogCount = 0;
    		}
    		break;
    	}
    	
    	return canUploadCurrentLogFile;
    }
    
    private Context mContext = null;
    
    private int mCurrentSSIDLogCount = 0;
    
    private String mCurrentSSIDLogFileName = null;
    
    private ArrayList<String> mFilesToUpload = null;
    
    private static final int MAX_MESSAGES_LIMIT_IN_FILE = 300;
    
}
