package com.wireless.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class ListItemActivity extends Activity
{
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_list_item);
    	
    	Intent intent = getIntent();
    	if (intent != null)
    	{
    		String text = intent.getStringExtra("macid");
    		TextView v = (TextView) findViewById(R.id.list_item_desc);
    		v.setText(text);   		
    	}
	}
	
}
