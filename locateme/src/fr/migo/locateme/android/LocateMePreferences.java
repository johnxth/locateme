package fr.migo.locateme.android;

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

/**
 * L'activité affichant les préférences de l'application. 
 */
public class LocateMePreferences extends PreferenceActivity {

	/**
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);

		// Permet de placer le widget sur le bureau, en exécurant un Intent
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null && intent.getAction().equals("android.appwidget.action.APPWIDGET_CONFIGURE")) {
			
			Toast.makeText(this, R.string.tips_help, Toast.LENGTH_LONG).show();
			
			int mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
			
			if (mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
				Intent resultValue = new Intent();
				resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
				setResult(RESULT_OK, resultValue);
			}
		}
	}
	
	/**
	 * 
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.pref, menu);
		return true;
	}

	/**
	 * 
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		
		case R.id.pref_menu_ok:
			this.finish();
			
			break;
			// L'item de l'aide	
		case R.id.pref_menu_help:
			
			View webViewLayout=getLayoutInflater().inflate(R.layout.help, null);
			
			WebView webView=(WebView) webViewLayout.findViewById(R.id.help_web_view);
			webView.loadUrl(getString(R.string.help_url));
			
			
			new AlertDialog.Builder(this)
			.setView(webViewLayout)
			.setTitle(R.string.help_title)
			.setPositiveButton(android.R.string.ok,null)
			.create().show();
			
			break;
		}

		return super.onOptionsItemSelected(item);
	}
}
