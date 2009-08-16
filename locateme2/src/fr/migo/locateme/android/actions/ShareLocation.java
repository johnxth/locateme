package fr.migo.locateme.android.actions;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;


import fr.migo.locateme.android.ILocateMeService;
import fr.migo.locateme.android.LocateMeService;
import fr.migo.locateme.android.R;
import fr.migo.locateme.android.preferences.PreferencesFormat;
import fr.migo.locateme.android.tools.Utils;

public class ShareLocation extends Activity {

	/**
	 * La clé permettant d'ajouter/récupérer l'adresse dans l'{@link Intent} transmis à l'activité {@link CopyOfShareLocation}. 
	 */
	public static final String ADDRESS_INTENT = "address_intent";

	private PreferencesFormat mPrefsFormat;
	
	/**
	 * Le titre affichant les infos supplémentaire
	 */
	private TextView shareAddressText;
	private TextView shareCoordinatesText;
	private TextView shareMapsUrlText;
	private TextView shareAccurencyText;
	private TextView shareTimeText;

	private ToggleButton shareAddressButton;
	private ToggleButton shareCoordinatesButton;
	private ToggleButton shareMapsUrlButton;
	private ToggleButton shareAccurencyButton;
	private ToggleButton shareTimeButton;

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

		setContentView(R.layout.share_location);

		mPrefsFormat=new PreferencesFormat(this);
		
		// Récupération de tout les champs textes
		shareAddressText=(TextView)findViewById(R.id.share_address_text);
		shareCoordinatesText=(TextView)findViewById(R.id.share_coordinates_text);
		shareMapsUrlText=(TextView)findViewById(R.id.share_maps_url_text);
		shareAccurencyText=(TextView)findViewById(R.id.share_accurency_text);
		shareTimeText=(TextView)findViewById(R.id.share_time_text);

		// Récupération de tout les toggle buttons
		shareAddressButton=(ToggleButton)findViewById(R.id.share_address_toggle);
		shareCoordinatesButton=(ToggleButton)findViewById(R.id.share_coordinates_toggle);
		shareMapsUrlButton=(ToggleButton)findViewById(R.id.share_maps_url_toggle);
		shareAccurencyButton=(ToggleButton)findViewById(R.id.share_accurency_toggle);
		shareTimeButton=(ToggleButton)findViewById(R.id.share_time_toggle);

		// Récupération du bouton d'envoi et paramétrage du listener
		Button sendButton=(Button)findViewById(R.id.share_send);
		sendButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent sendIntent = new Intent(Intent.ACTION_SEND);
				sendIntent.setType("text/plain");
				sendIntent.putExtra(Intent.EXTRA_TEXT, buildSharingString().toString());
				sendIntent.putExtra("sms_body", buildSharingString().toString());
				startActivity(Intent.createChooser(sendIntent, "Title:"));
			}
		});
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

			shareAccurencyText.setText(notFoundMessage);
			shareAddressText.setText(notFoundMessage);
			shareMapsUrlText.setText(notFoundMessage);
			shareCoordinatesText.setText(notFoundMessage);
			shareTimeText.setText(notFoundMessage);
		}
		// Si possible, la localisation et l'adresse courante sont récupérés auprès du service
		else {

			Float accurency=currentLocation.getAccuracy();
			Double lat=currentLocation.getLatitude();
			Double lon=currentLocation.getLongitude();

			shareAccurencyText.setText(mPrefsFormat.printAccuracy(accurency)+" ("+currentLocation.getProvider().toUpperCase()+")");
			shareCoordinatesText.setText("lat : "+Utils.round(lat,3)+"\nlon : "+Utils.round(lon,3));
			shareTimeText.setText(mPrefsFormat.printTime(currentLocation.getTime()));
			shareMapsUrlText.setText("http://maps.google.fr/maps?q="+lon+","+lat);
			shareAddressText.setText(currentAddress!=null?currentAddress:getString(R.string.share_address_not_found));
		}
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
	
	/**
	 * 
	 * @return
	 */
	public StringBuilder buildSharingString (){

		StringBuilder stringBuilder=new StringBuilder();

		if(shareAddressButton.isChecked())
			stringBuilder.append(shareAddressText.getText()).append("\n");

		if(shareCoordinatesButton.isChecked())
			stringBuilder.append(shareCoordinatesText.getText()).append("\n");

		if(shareAccurencyButton.isChecked())
			stringBuilder.append(shareAccurencyText.getText()).append("\n");

		if(shareTimeButton.isChecked())
			stringBuilder.append(shareTimeText.getText()).append("\n");

		if(shareMapsUrlButton.isChecked())
			stringBuilder.append(shareMapsUrlText.getText()).append("\n");

		return stringBuilder;
	}
}
