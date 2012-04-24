package com.morlunk.webstatus;

import java.util.HashMap;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
public class SiteStatusWidgetProvider extends AppWidgetProvider {
	
	HashMap<Integer, PendingIntent> activePendingIntents = new HashMap<Integer, PendingIntent>();
	
	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		
		// Loop through app widgets, updating each one.
		for(int x=0; x<appWidgetIds.length; x++) {
			int widgetId = appWidgetIds[x];
			
			// Get update interval
			long interval = SiteStatusPreferences.getRefreshPeriod(context, widgetId);
			
			// Begin schedule
			final Intent intent = new Intent(context, SiteStatusService.class);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
			final PendingIntent pending = PendingIntent.getService(context, 0, intent, 0);
			final AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			alarm.cancel(pending);
			alarm.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), interval*60000, pending);
			
			// Perform an immediate check
			Intent serviceIntent = new Intent(context, SiteStatusService.class);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
			context.startService(serviceIntent);
			
			// Log update
			Log.i("Site Status", "Widget "+widgetId+" started updates with interval "+interval+".");
		}
	}
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		for(int x=0;x<appWidgetIds.length;x++) {
			int widgetId = appWidgetIds[x];
			// Kill pending intent
			if(activePendingIntents.containsKey(x)) {
				final AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
				m.cancel(activePendingIntents.get(x));
				activePendingIntents.remove(x);
				Log.i("Site Status", "Widget "+widgetId+" stopped.");
			}
			// Remove configuration
			SiteStatusPreferences.removePreferences(context, widgetId);
			// Log deletion
			Log.i("Site Status", "Widget "+widgetId+" deleted.");
			super.onDeleted(context, appWidgetIds);
		}
	}
	
	@Override
	public void onDisabled(Context context) {
		// Kill all pending intents
		final AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		for(PendingIntent intent : activePendingIntents.values()) {
			m.cancel(intent);
			activePendingIntents.remove(intent);
		}
		super.onDisabled(context);
	}
}
