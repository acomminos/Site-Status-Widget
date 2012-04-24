package com.morlunk.webstatus;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

public class SiteStatusWidgetProvider extends AppWidgetProvider {
	
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
			
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
			
			remoteViews.setTextViewText(R.id.site_title, SiteStatusWidgetActivity.getSiteName(context, widgetId));

			Log.i("fu", SiteStatusWidgetActivity.getSiteName(context, widgetId));
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
	}
}
