package com.wireless.home;

public class WifiData
{	
	private String mSSID;
	private String mBSSID;
	private int mLevel;
	private int mFreq;
	private boolean mAP;
	
	public WifiData(String ssid, String bssid, int level, int freq, boolean isAP) 
	{
		mSSID = ssid;
		mBSSID = bssid;
		mLevel = level;
		mFreq = freq;
		mAP = isAP;
	}
	
	public boolean isAP() {	return mAP; }
	public void setmAP(boolean isAP) {	mAP = isAP; }
	
	public void setSSID(String ssid) {	mSSID = ssid; }
	public String getSSID() {	return mSSID; }
	
	public void setBSSID(String bssid) { mBSSID = bssid; }
	public String getBSSID() {	return mBSSID; }
	
	public void setLevel(int level) { mLevel = level; }
	public int getLevel() {	return mLevel; }
	
	public void setFreq(int freq) {	mFreq = freq; }
	public int getFreq() {	return mFreq; }

}
