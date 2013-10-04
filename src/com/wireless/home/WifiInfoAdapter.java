package com.wireless.home;

import java.util.List;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WifiInfoAdapter extends ArrayAdapter<WifiData>
{
	public WifiInfoAdapter(Context context, int textViewResourceId, List<WifiData> dataList)
	{
		super(context, textViewResourceId, dataList);
		
		mLayoutResourceId = textViewResourceId;
		mContext = context;
		mData = dataList;
	}
		
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if(AppConstants.SHOW_LOGS)
			Log.d("WifiInfoAdapter", "getView called");
        
		View row = convertView;
		WifiData item = mData.get(position);
		if(item == null) return row;
		
		WifiInfoHolder holder = null;
		
		LayoutInflater inflater = ((MyListActivity)mContext).getLayoutInflater();

        if(row == null) 
        {
            row = inflater.inflate(mLayoutResourceId, null);
            
            holder = new WifiInfoHolder();
            holder.mSSIDTextView = (TextView) row.findViewById(R.id.wifiinfo_ssid_label);
            holder.mBSSIDTextView = (TextView) row.findViewById(R.id.wifiinfo_mac_label);
            holder.mLevelTextView = (TextView) row.findViewById(R.id.wifiinfo_signal_label);
            holder.mFreqTextView = (TextView) row.findViewById(R.id.wifiinfo_freq_label);
            holder.mWifiInfoLayout = (RelativeLayout) row.findViewById(R.id.wifiinfo_list_layout);
            
            row.setTag(holder);
        }
        else
        {
        	holder = (WifiInfoHolder)row.getTag();
        }
            
        String ssid = item.getSSID();
        if(ssid != null)
        	holder.mSSIDTextView.setText(mContext.getResources().getString(R.string.ssid_label) + " " + ssid);
        
        holder.mFreqTextView.setText(mContext.getResources().getString(R.string.freq_label) + " " + item.getFreq() + " MHz");

        String bssid = item.getBSSID();
        if(bssid != null)
        	holder.mBSSIDTextView.setText(mContext.getResources().getString(R.string.mac_label) + " " + bssid);
        
        int level = item.getLevel();
        holder.mLevelTextView.setText(mContext.getResources().getString(R.string.signal_label) + " " + level +" dBm");
       	if (-level < 45)		holder.mLevelTextView.setTextColor(Color.GREEN);
        else if (-level < 75)   holder.mLevelTextView.setTextColor(Color.YELLOW);
        else		            holder.mLevelTextView.setTextColor(Color.RED);
        
    	holder.mWifiInfoLayout.setBackgroundColor(item.isAP() ? Color.DKGRAY : Color.BLACK);
        	
        return row;           
    }

	@Override
	public int getCount()
	{
		return mData.size();
	}

	@Override
	public WifiData getItem(int position)
	{
		return mData.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return 0;
	}
	
    private static class WifiInfoHolder
    {
    	public RelativeLayout mWifiInfoLayout;
    	public TextView mSSIDTextView;
	   	public TextView mBSSIDTextView;
	   	public TextView mLevelTextView;
	   	public TextView mFreqTextView;
    }
    
    // Member Variables
	private int mLayoutResourceId;
	private Context mContext;
	private List<WifiData> mData = null;

}
