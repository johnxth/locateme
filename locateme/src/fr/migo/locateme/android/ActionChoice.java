package fr.migo.locateme.android;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import fr.migo.locateme.android.LocateMe.LocateMeService;

/**
 * Activit� affichant les diff�rentes actions possible avec l'adresse localis� courante. 
 *
 */
public class ActionChoice extends Activity {

	/**
	 * 
	 */
	private final static String PREF_DISPLAY_METER_UNITS="pref_display_measure_units";

	/**
	 * 
	 */
	private final static String PREF_DISPLAY_FR_DATE_FORMAT="pref_display_date_format_title";
	
	/**
	 * 
	 */
	private final static SimpleDateFormat US_DATE_FORMAT=new SimpleDateFormat("MM/dd hh:mm:ss a");

	/**
	 * 
	 */
	private final static SimpleDateFormat FR_DATE_FORMAT=new SimpleDateFormat("dd/MM HH:mm:ss");

	/**
	 * Le titre affichant l'adresse
	 */
	private TextView addressTextView;

	/**
	 * Le titre affichant les infos suppl�mentaire
	 */
	private TextView extraInfoTextView;

	/**
	 *  Le bouton pour localiser l'adresse sur une carte 
	 */
	private Button mapsAction;

	/**
	 * Le bouton pour ajouter l'addresse � un contact
	 */
	private Button addressContactAction;

	/**
	 * Le bouton pour effectuer un itin�raire au d�part de l'adresse courante
	 */
	private Button directionAction;

	/**
	 * Le bouton pour forcer la mise � jour de la position
	 */
	private Button refreshAction;

	/**
	 * Pr�f�rence utilisateurs
	 */
	private SharedPreferences sharedPreferences;

	/**
	 * Constructeur d'{@link Intent} 
	 */
	private ActionIntentBuilder intentBuilder;

	/**
	 * {@link ServiceConnection} utilis� pour r�cup�rer l'interface de communication {@link ILocateMeService}
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
			// mise � jour de l'interface graphique et des diff�rentes actions
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


	/**
	 * Met � jour l'interface graphique et les diff�rentes actions en fonction des donn�es de localisation re�u du service de localisation {@link LocateMeService}
	 * @param locateMeService interface de communication avec le service {@link LocateMeService}
	 */
	private void updateUI (final ILocateMeService locateMeService){


		/**
		 * INITIALISATION DES COMPOSANTS GRAPHIQUES IMPACTES
		 */

		/**
		 * Action pour forcer la mise � jour de la localisation
		 */
		refreshAction.setEnabled(true);
		refreshAction.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					locateMeService.updateLocation();
					Toast.makeText(ActionChoice.this, getString(R.string.do_refresh_action), Toast.LENGTH_SHORT).show();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				ActionChoice.this.finish();
			}
		});

		/**
		 * RECUPERATION DE LA POSITION ET INITIALISATION DES COMPOSANTS GRAPHIQUES IMPACTES
		 */
		Location currentLocation=null;
		try {
			currentLocation=locateMeService.getLastLocation();
		} catch (Exception e) {
			// DO NOTHING
		}

		if(currentLocation==null){
			extraInfoTextView.setText(null);
			mapsAction.setEnabled(false);
		}
		else {

			/**
			 * Ajout des infos supl�mentaires
			 */
			String lastUpdate=null;
			if(sharedPreferences.getBoolean(PREF_DISPLAY_FR_DATE_FORMAT, true))
				lastUpdate=FR_DATE_FORMAT.format(new Date(currentLocation.getTime()));
			else
				lastUpdate=US_DATE_FORMAT.format(new Date(currentLocation.getTime()));

			float accuracy=currentLocation.getAccuracy();
			int	  extraInfoMsg=R.string.current_extra_info_in_meter;

			if(sharedPreferences.getBoolean(PREF_DISPLAY_METER_UNITS, true)==false){
				accuracy*=0.000621371192;
				extraInfoMsg=R.string.current_extra_info_in_mile;
			}
			extraInfoTextView.setText(getString(extraInfoMsg,
												lastUpdate,
												currentLocation.getProvider(),
												accuracy,
												Utils.round(currentLocation.getLatitude(),6),
												Utils.round(currentLocation.getLongitude(),6)));

			/**
			 * Action pour localiser l'adresse sur une carte
			 */
			mapsAction.setEnabled(true);
			mapsAction.setOnClickListener(new OnActionClickListener(this,intentBuilder.intentDisplayOnMap(currentLocation)));
		}

		/**
		 * RECUPERATION DE L'ADRESSE ET INITIALISATION DES COMPOSANTS GRAPHIQUES IMPACTES
		 */
		String currentAddress=null;

		// Si possible, la localisation et l'adresse courante sont r�cup�r�s aupr�s du service
		if(locateMeService!=null){
			try {
				currentAddress=new AddressBuilder(this).buildOrThrows(currentLocation, AddressBuilder.DEFAULT_ADDRESS_TEMPLATE);
			} catch (Exception e) {
				// DO NOTHING
			}
		}

		// si aucune adresse et donc localisation n'a �t� trouv�e, les actions sont d�sactiv�es et un message d'erreur est affich�
		if(currentAddress==null){
			
			String message=null;
			if(currentLocation!=null)
				message=getString(R.string.address_not_found,Utils.round(currentLocation.getLatitude(),6),
						Utils.round(currentLocation.getLongitude(),6));
			else
				message=getString(R.string.location_not_found);
			
			addressTextView.setText(message);
			addressContactAction.setEnabled(false);
			directionAction.setEnabled(false);
		}
		else {

			/**
			 * Ajout du titre de l'activit� avec l'adresse courante
			 */
			addressTextView.setText(currentAddress);

			/**
			 * Action pour ajouter l'adresse � un contact
			 */
			addressContactAction.setEnabled(true);
//			final Intent addressContactActionIntent=intentBuilder.intentAddAddressContact(currentAddress);
//			addressContactAction.setOnClickListener(new OnClickListener (){
//
//				@Override
//				public void onClick(View view) {
//
//					new AlertDialog.Builder(ActionChoice.this)
//					.setItems(R.array.add_to_entries, new DialogInterface.OnClickListener() {
//
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							switch (which) {
//							case 0:
//								ActionChoice.this.startActivity(addressContactActionIntent);
//								ActionChoice.this.finish();
//								dialog.dismiss();
//								break;
//
//							default:
//								break;
//							}
//						}
//					})
//					.setTitle(R.string.add_location_to)
//					.create().show();
//					
//				}
//			});
			addressContactAction.setOnClickListener(new OnActionClickListener(this,intentBuilder.intentAddAddressContact(currentAddress)));

			/**
			 * Action pour effectuer un itin�raire au d�part de l'adresse courante
			 */
			directionAction.setEnabled(true);
			directionAction.setOnClickListener(new OnActionClickListener(this,intentBuilder.intentDisplayAsDirection(currentAddress)));
		}
	}

	/**
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.action_choice);

		// R�cup�re les param�tres de l'application
		sharedPreferences=PreferenceManager.getDefaultSharedPreferences(this);

		// Construit le constructeur d'intention
		intentBuilder=new ActionIntentBuilder(this);

		// R�cup�re les diff�rents �l�ments graphique
		addressTextView=(TextView) findViewById(R.id.address_action_choice);
		extraInfoTextView=(TextView) findViewById(R.id.extra_info_action_choice);
		mapsAction=(Button) findViewById(R.id.maps_action);
		addressContactAction=(Button) findViewById(R.id.contact_address_action);
		directionAction=(Button) findViewById(R.id.direction_action);
		refreshAction = (Button)findViewById(R.id.refresh_action);
	}

	/**
	 * Bind le service de localisation {@link LocateMeService}
	 */
	@Override
	protected void onStart() {
		super.onStart();
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
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.action_choice, menu);
		return true;
	}

	/**
	 * 
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		// L'item de pr�f�rence
		case R.id.action_choice_menu_preferences:
			startActivity(new Intent(this,LocateMePreferences.class));
			finish();
			break;
			// L'item de l'aide	
		case R.id.action_choice_menu_help:
			
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


	/**
	 * Listener sp�cialis� pour les boutons de l'activit� {@link ActionChoice}
	 */
	private static class OnActionClickListener implements OnClickListener {

		private final Intent mIntent;
		private final ActionChoice mActivity;

		/**
		 * Constructeur
		 * @param activity l'instance de l'activit� {@link ActionChoice}
		 * @param intent l'intention souhait� lors du clique sur le bouton
		 */
		public OnActionClickListener(ActionChoice activity,Intent intent) {
			mIntent=intent;
			mActivity=activity;
		}

		/**
		 * D�marre une activit� avec l'{@link Intent}, puis termine l'activit� {@link ActionChoice}
		 */
		@Override
		public void onClick(View v) {
			mActivity.startActivity(mIntent);
			mActivity.finish();
		}
	}
}
