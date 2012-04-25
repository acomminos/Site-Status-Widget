package com.morlunk.webstatus;

import java.net.HttpURLConnection;
import java.net.URL;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

public class SiteStatusService extends IntentService {
	
	public SiteStatusService() {
		super("SiteStatusService");
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		// Get remote views for widget
		AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
		RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.widget);

		// Get passed widget ID
		int mAppWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
		
		// Get values
		String siteName = SiteStatusPreferences.getSiteName(this, mAppWidgetId);
		String siteUrl = SiteStatusPreferences.getSiteUrl(this, mAppWidgetId);
		
		// Set 'refreshing' icon while loading
		remoteViews.setImageViewResource(R.id.status_refresh, R.drawable.navigation_refresh);
		widgetManager.updateAppWidget(mAppWidgetId, remoteViews);
		
		int code;
		URL u;
		HttpURLConnection huc;
		
		// Get time before transfer
		long time = SystemClock.elapsedRealtime();
		
		try {
			u = new URL(siteUrl);
			huc = (HttpURLConnection)u.openConnection (); 
			huc.setRequestMethod("GET"); 
			huc.connect();
			code = huc.getResponseCode();
			huc.disconnect();
		} catch (Exception e) {
			code = 404; // TODO provide more detailed error codes
		} 
		
		// Get time after
		time = SystemClock.elapsedRealtime()-time;
		
		// Set name and URL, and image
		remoteViews.setTextViewText(R.id.site_title, siteName);
		remoteViews.setTextViewText(R.id.site_url, siteUrl);
		remoteViews.setTextViewText(R.id.latency_text, time+"ms");
		remoteViews.setImageViewResource(R.id.status_refresh, code == 200 ? R.drawable.navigation_accept : R.drawable.navigation_cancel);
		// Set on click action to start the service
		remoteViews.setOnClickPendingIntent(R.id.widget_layout, PendingIntent.getService(this, mAppWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT));
		widgetManager.updateAppWidget(mAppWidgetId, remoteViews);
		
		// Log update
		Log.i("Site Status", "Widget "+mAppWidgetId+" updated.");
	}

}
