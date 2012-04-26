package com.morlunk.webstatus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

public abstract class SiteStatusWidgetProvider extends AppWidgetProvider {
	
	public static final String SITE_STATUS_WIDGET_TYPE_ID = "siteStatusWidgetType";
	
	public static final int SITE_STATUS_LARGE_WIDGET = 0;
	public static final int SITE_STATUS_SMALL_WIDGET = 1;
	
	/**
	 * To be implemented by subclasses.
	 * @return The widget type.
	 */
	public abstract int getWidgetType();
	
	/**
	 * Forces a widget update, usually called after the configuration activity is run. Basically does what the answer in
	 * http://stackoverflow.com/questions/3818545/how-do-i-force-an-update-in-a-configuration-activity describes.
	 * @param context
	 * @param widgetId
	 */
	public static void messyForceUpdate(Context context, int widgetId) {
		//SiteStatusWidgetProvider.clas
	}
	
	public void updateWidget(Context context, int widgetId) {
		// Get update interval
		long interval = SiteStatusPreferences.getRefreshPeriod(context,
				widgetId);

		// Begin schedule
		final Intent intent = new Intent(context, SiteStatusService.class);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
		intent.putExtra(SITE_STATUS_WIDGET_TYPE_ID, getWidgetType());
		final PendingIntent pending = PendingIntent.getService(context, widgetId,
				intent, PendingIntent.FLAG_CANCEL_CURRENT);
		final AlarmManager alarm = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		alarm.cancel(pending);
		alarm.setRepeating(AlarmManager.ELAPSED_REALTIME,
				SystemClock.elapsedRealtime(), interval*60000, pending);
		
		// Log update
		Log.i("Site Status", "Widget " + widgetId
				+ " started updates with interval " + interval + ".");
	}
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		for(int x=0;x<appWidgetIds.length;x++) {
			SiteStatusPreferences.removePreferences(context, appWidgetIds[x]);
			// Delete pending intent with widget ID
			final AlarmManager alarm = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
			PendingIntent pendingIntent = PendingIntent.getService(context, appWidgetIds[x], new Intent(context, SiteStatusService.class), PendingIntent.FLAG_NO_CREATE);
			if(pendingIntent != null) {
				alarm.cancel(pendingIntent);
			}
			// Log update
			Log.i("Site Status", "Widget " + appWidgetIds[x]
					+ " deleted.");
		}
		super.onDeleted(context, appWidgetIds);
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		
		// Loop through app widgets, updating each one.
		for(int x=0; x<appWidgetIds.length; x++) {
			int widgetId = appWidgetIds[x];
			// Make sure widget is defined before updating
			if(SiteStatusPreferences.widgetExists(context, widgetId)) {
				updateWidget(context, widgetId);
			}
		}
	}
}
