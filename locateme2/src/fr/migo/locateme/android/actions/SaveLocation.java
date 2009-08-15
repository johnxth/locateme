package fr.migo.locateme.android.actions;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import fr.migo.locateme.android.ILocateMeService;
import fr.migo.locateme.android.LocateMeService;
import fr.migo.locateme.android.R;
import fr.migo.locateme.android.tools.Utils;

public class SaveLocation extends Activity{

	private final static String PREF_ADDRESS_SAVED="pref.address.saved";

	private final static String PREF_INFOS_SAVED="pref.infos.saved";

	private final static String PREF_LATITUDE_SAVED="pref.latitude.saved";

	private final static String PREF_LONGITUDE_SAVED="pref.longitude.saved";

	private final static String PREF_NOTES_SAVED="pref.notes.saved";

	private SharedPreferences mSharedPreferences;

	private TextView addressToSaveText;
	private TextView infosToSaveText;
	private EditText notesToSaveEditText;

	private TextView addressSavedText;
	private TextView infosSavedText;
	private TextView notesSavedText;


	private Button saveButton;

	private float currentLatitude;
	private float currentLongitude;

	/**
	 * {@link ServiceConnection} utilisé pour récupérer l'interface de communication {@link ILocateMeService}
	 */
	private final ServiceConnection mConnection = new ServiceConnection() {

		/**
		 * L'interface de communication avec le service {@link LocateMeService}
		 */
		private ILocateMeService locateMeService;

		/**
		 * 
		 */
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			locateMeService =  ILocateMeService.Stub.asInterface(service);
			// mise à jour de l'interface graphique et des différentes actions
			updateUI(locateMeService);
		}

		/**
		 * 
		 */
		@Override
		public void onServiceDisconnected(ComponentName name) {
			locateMeService=null;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.save_location);

		mSharedPreferences=PreferenceManager.getDefaultSharedPreferences(this);

		addressSavedText=(TextView)findViewById(R.id.address_saved_text);
		infosSavedText=(TextView)findViewById(R.id.infos_saved_text);
		notesSavedText=(TextView)findViewById(R.id.notes_saved_text);

		addressToSaveText=(TextView)findViewById(R.id.address_to_save_text);
		infosToSaveText=(TextView)findViewById(R.id.infos_to_save_text);
		notesToSaveEditText=(EditText)findViewById(R.id.notes_to_save_edittext);

		saveButton=(Button)findViewById(R.id.save_location);
		saveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				CharSequence addressToSave=addressToSaveText.getText();
				CharSequence accurencyToSave=infosToSaveText.getText();
				String notesToSave=notesToSaveEditText.getText().toString().trim();

				Editor editor=mSharedPreferences.edit();

				if(addressToSave==null || addressToSave.length()==0)
					editor.remove(PREF_ADDRESS_SAVED);
				else
					editor.putString(PREF_ADDRESS_SAVED, (String) addressToSave);

				if(accurencyToSave==null || accurencyToSave.length()==0)
					editor.remove(PREF_INFOS_SAVED);
				else
					editor.putString(PREF_INFOS_SAVED, (String) accurencyToSave);

				if(notesToSave.length()==0)
					editor.remove(PREF_NOTES_SAVED);
				else
					editor.putString(PREF_NOTES_SAVED, notesToSave);

				editor.putFloat(PREF_LATITUDE_SAVED, currentLatitude);
				editor.putFloat(PREF_LONGITUDE_SAVED, currentLongitude);

				editor.commit();

				updatePrefUI();
			}
		});

		updatePrefUI();
	}

	/**
	 * Bind le service de localisation {@link LocateMeService}
	 */
	@Override
	protected void onResume() {
		super.onResume();
		bindService(new Intent(this,LocateMeService.class), mConnection, BIND_AUTO_CREATE);
	}

	/**
	 * Unbind le service de localisation {@link LocateMeService}
	 */
	@Override
	protected void onStop() {
		super.onStop();
		unbindService(mConnection);
	}

	void updateUI (ILocateMeService locateMeService){

		/**
		 * RECUPERATION DE LA POSITION ET INITIALISATION DES COMPOSANTS GRAPHIQUES IMPACTES
		 */
		Location currentLocation=null;
		String currentAddress=null;
		try {
			currentLocation=locateMeService.getLastLocation();
			currentAddress=locateMeService.getLastAddress();
		} catch (Exception e) {
			// DO NOTHING
		}

		/**
		 * RECUPERATION DE L'ADRESSE ET INITIALISATION DES COMPOSANTS GRAPHIQUES IMPACTES
		 */
		if(currentLocation==null){

			String notFoundMessage=getString(R.string.share_not_found);

			addressToSaveText.setText(notFoundMessage);
			infosToSaveText.setText(notFoundMessage);
		}
		// Si possible, la localisation et l'adresse courante sont récupérés auprès du service
		else {

			addressToSaveText.setText(currentAddress);

			Float accurency=currentLocation.getAccuracy();
			currentLatitude=(float) currentLocation.getLatitude();
			currentLongitude=(float) currentLocation.getLongitude();

			StringBuilder builder=new StringBuilder("Accurency : ")
			.append((accurency==null)?"0":Float.toString(accurency))
			.append(" (").append(currentLocation.getProvider().toUpperCase())
			.append(")\nlatitude : ").append(Utils.round(currentLatitude,6))
			.append(" longitude : ").append(Utils.round(currentLongitude,6));

			infosToSaveText.setText(builder.toString());
		}
	}

	public void updatePrefUI (){
		// TODO ; à changer
		String notFoundString=getString(R.string.no_data_saved);

		String addressSaved=mSharedPreferences.getString(PREF_ADDRESS_SAVED, notFoundString);
		String infosSaved=mSharedPreferences.getString(PREF_INFOS_SAVED, notFoundString);
		String notesSaved=mSharedPreferences.getString(PREF_NOTES_SAVED, notFoundString);

		addressSavedText.setText(addressSaved);
		infosSavedText.setText(infosSaved);
		notesSavedText.setText(notesSaved);
	}

	/**
	 * 
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.save_location, menu);
		return true;
	}

	/**
	 * 
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.erase_action:
			Editor editor=mSharedPreferences.edit();
			editor.remove(PREF_ADDRESS_SAVED);
			editor.remove(PREF_INFOS_SAVED);
			editor.remove(PREF_NOTES_SAVED);
			editor.remove(PREF_LATITUDE_SAVED);
			editor.remove(PREF_LONGITUDE_SAVED);
			editor.commit();
			updatePrefUI();
			break;
		case R.id.direction_action:
			String addressSaved=mSharedPreferences.getString(PREF_ADDRESS_SAVED, null);
			if(addressSaved==null)
				Toast.makeText(this, R.string.no_data_saved, Toast.LENGTH_SHORT).show();
			else {
				Intent directionActionIntent=new Intent(Intent.ACTION_VIEW,Uri.parse("http://maps.google.com/maps?daddr="+Uri.encode(addressSaved)));
				startActivity(directionActionIntent);
			}
			break;
		case R.id.maps_action:

			float badValue=1000;

			float latitudeSaved=mSharedPreferences.getFloat(PREF_LATITUDE_SAVED, badValue);
			float longitudeSaved=mSharedPreferences.getFloat(PREF_LONGITUDE_SAVED, badValue);

			if(latitudeSaved==badValue || longitudeSaved==badValue)
				Toast.makeText(this, R.string.no_data_saved, Toast.LENGTH_SHORT).show();
			else {
				Intent mapsActionIntent=new Intent(Intent.ACTION_VIEW,Uri.parse("geo:"+latitudeSaved+","+longitudeSaved));
				startActivity(mapsActionIntent);
			}				
			break;
		case R.id.notes_edition_action:

			View view=getLayoutInflater().inflate(R.layout.edit_saved_notes, null);
			final EditText notesSavedEditText=(EditText) view.findViewById(android.R.id.edit);

			String notesSaved=mSharedPreferences.getString(PREF_NOTES_SAVED, null);
			notesSavedEditText.setText(notesSaved);

			new AlertDialog.Builder(this)
			.setIcon(R.drawable.locateme)
			.setTitle(R.string.notes_to_save)
			.setView(view)
			.setNegativeButton(android.R.string.cancel, null)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

					String notesToSave=notesSavedEditText.getText().toString().trim();

					Editor editor=mSharedPreferences.edit();

					if(notesToSave.length()==0)
						editor.remove(PREF_NOTES_SAVED);
					else
						editor.putString(PREF_NOTES_SAVED, notesToSave);

					editor.commit();

					updatePrefUI();
				}
			}).create().show();
			break;
		}
		return super.onOptionsItemSelected(item);
	}


}
