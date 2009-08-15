package fr.migo.locateme.android.actions;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
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
import fr.migo.locateme.android.ILocateMeService;
import fr.migo.locateme.android.LocateMeService;
import fr.migo.locateme.android.R;
import fr.migo.locateme.android.preferences.LocateMePreferences;
import fr.migo.locateme.android.preferences.PreferencesFormat;
import fr.migo.locateme.android.tools.Utils;

/**
 * Activit� affichant les diff�rentes actions possible avec l'adresse localis� courante. 
 *
 */
public class ActionChoice extends Activity {

	
	private PreferencesFormat prefsFormat;
	
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
	 * Le bouton pour partager sa position
	 */
	private Button shareAction;
	
	/**
	 * Le bouton pour enregistrer sa position
	 */
	private Button saveAction;

	/**
	 * Le bouton pour forcer la mise � jour de la position
	 */
	private Button refreshAction;

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
		String currentAddress=null;
		try {
			currentLocation=locateMeService.getLastLocation();
			currentAddress=locateMeService.getLastAddress();
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
			int extraInfoMsg=R.string.current_extra_info_in_meter;

			extraInfoTextView.setText(getString(extraInfoMsg,
					prefsFormat.printTime(	currentLocation.getTime()),
					currentLocation.getProvider(),
					prefsFormat.printAccuracy(currentLocation.getAccuracy()),
					Utils.round(currentLocation.getLatitude(),6),
					Utils.round(currentLocation.getLongitude(),6)));

			/**
			 * Action pour localiser l'adresse sur une carte
			 */
			Intent mapsActionIntent=new Intent(Intent.ACTION_VIEW,Uri.parse("geo:"+currentLocation.getLatitude()+","+currentLocation.getLongitude()));
			mapsAction.setEnabled(true);
			mapsAction.setOnClickListener(new OnActionClickListener(this,mapsActionIntent));
			
			/**
			 * Action pour partager sa localisation
			 */
			shareAction.setEnabled(true);
			shareAction.setOnClickListener(new OnActionClickListener(this,new Intent(this,ShareLocation.class)));
			
			/**
			 * Action pour sauvegarder sa localisation
			 */
			saveAction.setEnabled(true);
			saveAction.setOnClickListener(new OnActionClickListener(this,new Intent(this,SaveLocation.class)));
		}

		/**
		 * RECUPERATION DE L'ADRESSE ET INITIALISATION DES COMPOSANTS GRAPHIQUES IMPACTES
		 */
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
			// TODO : mettre � false apr�s test

		}
		else {

			/**
			 * Ajout du titre de l'activit� avec l'adresse courante
			 */
			addressTextView.setText(currentAddress);

			/**
			 * Action pour ajouter l'adresse � un contact
			 */
			Intent addLocationIntent=new Intent(this,AddLocation.class);
			addLocationIntent.putExtra(AddLocation.ADDRESS_INTENT, currentAddress);
			addressContactAction.setEnabled(true);
			addressContactAction.setOnClickListener(new OnActionClickListener(this,addLocationIntent));

			/**
			 * Action pour effectuer un itin�raire au d�part de l'adresse courante
			 */
			Intent directionActionIntent=new Intent(Intent.ACTION_VIEW,Uri.parse("http://maps.google.com/maps?saddr="+Uri.encode(currentAddress)));
			directionAction.setEnabled(true);
			directionAction.setOnClickListener(new OnActionClickListener(this,directionActionIntent));
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
		prefsFormat=new PreferencesFormat(this);

		// R�cup�re les diff�rents �l�ments graphique
		addressTextView=(TextView) findViewById(R.id.address_action_choice);
		extraInfoTextView=(TextView) findViewById(R.id.extra_info_action_choice);
		mapsAction=(Button) findViewById(R.id.maps_action);
		addressContactAction=(Button) findViewById(R.id.contact_address_action);
		directionAction=(Button) findViewById(R.id.direction_action);
		shareAction = (Button)findViewById(R.id.share_action);
		saveAction = (Button)findViewById(R.id.save_location);
		refreshAction = (Button)findViewById(R.id.refresh_action);
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
		}
	}
}
