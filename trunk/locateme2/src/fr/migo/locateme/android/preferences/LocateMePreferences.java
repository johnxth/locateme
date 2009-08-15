package fr.migo.locateme.android.preferences;

import fr.migo.locateme.android.R;
import fr.migo.locateme.android.R.id;
import fr.migo.locateme.android.R.layout;
import fr.migo.locateme.android.R.string;
import fr.migo.locateme.android.R.xml;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;

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
		
		setContentView(R.layout.preferences);
		addPreferencesFromResource(R.xml.preferences);

		// Bouton OK
		Button okButton=(Button) findViewById(R.id.ok);
		okButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				LocateMePreferences.this.finish();
			}
		});
		
		// Bouton d'aide 
		Button helpButton=(Button) findViewById(R.id.help);
		helpButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				View webViewLayout=LocateMePreferences.this.getLayoutInflater().inflate(R.layout.help, null);
				
				WebView webView=(WebView) webViewLayout.findViewById(R.id.help_web_view);
				webView.loadUrl(getString(R.string.help_url));
				
				new AlertDialog.Builder(LocateMePreferences.this)
				.setView(webViewLayout)
				.setTitle(R.string.help_title)
				.setPositiveButton(android.R.string.ok,null)
				.create().show();
			}
		});
		
		// Permet de placer le widget sur le bureau, en exécurant un Intent
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null && intent.getAction().equals("android.appwidget.action.APPWIDGET_CONFIGURE")) {
			
			int mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
			
			if (mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
				Intent resultValue = new Intent();
				resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
				setResult(RESULT_OK, resultValue);
			}
		}
	}
}
