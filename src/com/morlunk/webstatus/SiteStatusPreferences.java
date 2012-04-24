package com.morlunk.webstatus;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;

public class SiteStatusPreferences extends PreferenceActivity {
	
	private static final String PREFS_NAME = "morlunk-webstatus-prefs";
	
	public static final String SITE_NAME_KEY = "site_name";
	public static final String SITE_URL_KEY = "site_url";
	public static final String REFRESH_PERIOD_KEY = "refresh_period";
	
	/**
	 * @return the widgetPreferences
	 */
	public static SharedPreferences getWidgetPreferences(Context context) {
		return context.getSharedPreferences(PREFS_NAME, 0);
	}

	private int mAppWidgetId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Clear values and set defaults
		PreferenceManager.getDefaultSharedPreferences(this).edit().clear().commit();
		PreferenceManager.setDefaultValues(this, R.xml.widgetpreferences, true);
		
		addPreferencesFromResource(R.xml.widgetpreferences);
		setContentView(R.layout.main);
		
		// In case user backs out, set result to cancelled so no widget is created
		setResult(RESULT_CANCELED);
		
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
		    mAppWidgetId = extras.getInt(
		            AppWidgetManager.EXTRA_APPWIDGET_ID, 
		            AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		
		// Configure create button
		Button createButton = (Button) findViewById(R.id.create_button);
		createButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {				
				// Get preferences represented in this activity
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SiteStatusPreferences.this);
				
				// Get preferences where widget configurations are stored
				SharedPreferences widgetPreferences = getWidgetPreferences(SiteStatusPreferences.this);
				
				// Put configuration in shared preferences
				SharedPreferences.Editor editor = widgetPreferences.edit();
				
				String siteName = preferences.getString(SITE_NAME_KEY, "Site");
				String siteUrl = preferences.getString(SITE_URL_KEY, "http://www.example.com/");
				long refreshPeriod;
				try {
					String refreshPeriodString = preferences.getString(REFRESH_PERIOD_KEY, "60");
					refreshPeriod = Integer.parseInt(refreshPeriodString);
				} catch (NumberFormatException e) {
					refreshPeriod = 60; // Default value
				}
				
				editor.putString(SITE_NAME_KEY+"-"+mAppWidgetId, siteName);
				editor.putString(SITE_URL_KEY+"-"+mAppWidgetId, siteUrl);
				editor.putLong(REFRESH_PERIOD_KEY+"-"+mAppWidgetId, refreshPeriod);
				
				editor.commit();
				
				// Return with widget ID
				Intent resultValue = new Intent();
				resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
				setResult(RESULT_OK, resultValue);
				finish();
			}
		});
	}
	
	
	
	public static String getSiteName(Context context, int widgetId) {
		return getWidgetPreferences(context).getString(SITE_NAME_KEY+"-"+widgetId, "Undefined name");
	}
	
	public static String getSiteUrl(Context context, int widgetId) {
		return getWidgetPreferences(context).getString(SITE_URL_KEY+"-"+widgetId, "Undefined URL");
	}
	
	public static long getRefreshPeriod(Context context, int widgetId) {
		return getWidgetPreferences(context).getLong(REFRESH_PERIOD_KEY+"-"+widgetId, 10000);
	}
	
	/**
	 * Removes all widget preferences with the specified widget ID.
	 * @param context
	 * @param widgetId
	 */
	public static void removePreferences(Context context, int widgetId) {
		SharedPreferences.Editor editor = getWidgetPreferences(context).edit();
		editor.remove(SITE_NAME_KEY+"-"+widgetId);
		editor.remove(SITE_URL_KEY+"-"+widgetId);
		editor.remove(REFRESH_PERIOD_KEY+"-"+widgetId);
		editor.commit();
	}
}