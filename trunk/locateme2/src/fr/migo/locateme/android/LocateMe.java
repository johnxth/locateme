package fr.migo.locateme.android;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

/**
 * {@link LocateMe} est le composant permettant de gérer les évenements liés au widget.
 * @see AppWidgetProvider
 */
public class LocateMe extends AppWidgetProvider  {

	/**
	 * Lance, si ce n'est pas déjà fait, le service {@link LocateMeService}.
	 */
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		context.startService(new Intent(context, LocateMeService.class));
	}

	/**
	 * 
	 */
	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		context.startService(new Intent(context, LocateMeService.class));
	}

	/**
	 * 
	 */
	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		context.stopService(new Intent(context, LocateMeService.class));
	}
}