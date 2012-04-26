package com.morlunk.webstatus;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ComponentInfo;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

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
				
				// Perform initial update
				// VERY MESSY! TODO find a better way
				AppWidgetManager widgetManager = AppWidgetManager.getInstance(SiteStatusPreferences.this);
				AppWidgetProviderInfo providerInfo = widgetManager.getAppWidgetInfo(mAppWidgetId);
				ComponentName providerName = providerInfo.provider;
				// Because SiteStatusWidgetProvider is an abstract class (so we can implement the widget type in subclasses) we can't make a static method that fetches the widget types.
				// So, we have to instantiate the provider name of the widget in its provider info.
				try {
					Class<?> providerClass = Class.forName(providerName.getClassName());
					SiteStatusWidgetProvider widgetProvider = (SiteStatusWidgetProvider) providerClass.newInstance();
					widgetProvider.updateWidget(SiteStatusPreferences.this, mAppWidgetId);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// Return with widget ID
				Intent resultValue = new Intent();
				resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
				setResult(RESULT_OK, resultValue);
				finish();
			}
		});
	}
	
	
	
	public static String getSiteName(Context context, int widgetId) {
		return getWidgetPreferences(context).getString(SITE_NAME_KEY+"-"+widgetId, null);
	}
	
	public static String getSiteUrl(Context context, int widgetId) {
		return getWidgetPreferences(context).getString(SITE_URL_KEY+"-"+widgetId, null);
	}
	
	public static long getRefreshPeriod(Context context, int widgetId) {
		return getWidgetPreferences(context).getLong(REFRESH_PERIOD_KEY+"-"+widgetId, 0);
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
	
	/**
	 * Returns true if the passed widget ID is configured in preferences.
	 * @param context
	 * @param widgetId
	 * @return
	 */
	public static boolean widgetExists(Context context, int widgetId) {
		SharedPreferences widgetPreferences = SiteStatusPreferences
				.getWidgetPreferences(context);
		return widgetPreferences.contains(SITE_NAME_KEY + "-" + widgetId)
				&& widgetPreferences.contains(SITE_URL_KEY + "-" + widgetId)
				&& widgetPreferences.contains(REFRESH_PERIOD_KEY + "-"
						+ widgetId);
	}
}