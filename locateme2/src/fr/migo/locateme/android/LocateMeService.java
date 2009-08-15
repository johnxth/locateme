package fr.migo.locateme.android;

import java.io.IOException;
import java.util.List;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import fr.migo.locateme.android.actions.ActionChoice;
import fr.migo.locateme.android.preferences.PreferencesFormat;
import fr.migo.locateme.android.tools.AddressBuilder;
import fr.migo.locateme.android.tools.Utils;
import fr.migo.locateme.android.tools.AddressBuilder.AddressInfo;

/**
 * {@link LocateMeService} est un service Android qui a la particularité d'intercepter les évenements liés à la localisation grâce à son implémentation de l'interface {@link LocationListener}. 
 * 
 */
public class LocateMeService extends Service implements LocationListener, OnSharedPreferenceChangeListener {

	/**
	 * Le gestionnaire de localisation Android
	 */
	private LocationManager mLocationManager;

	/**
	 * La dernière position connue de l'appareil. Peut-être <code>null</code>
	 */
	private Location mLastLocation;

	/**
	 * La dernière addresse connue de l'appareil. Peut-être <code>null</code>
	 */
	private Address mLastAddress;

	//	/**
	//	 * Les préférences utilisateurs
	//	 */
	//	private SharedPreferences mSharedPreferences;

	private PreferencesFormat mPrefsFormat;
	
	/**
	 * Les vues du widget
	 */
	private RemoteViews updateViews;

	/**
	 * Le gestionnaire du widget
	 */
	private AppWidgetManager appWidgetManager ;

	/**
	 * 
	 */
	private Geocoder geocoder;

	/**
	 * L'implémentation du stub générée à partir de l'IDL.
	 */
	private final ILocateMeService.Stub mBinder = new ILocateMeService.Stub() {

		/**
		 * 
		 */
		@Override
		public Location getLastLocation() throws RemoteException {
			return mLastLocation;
		}

		/**
		 * 
		 */
		@Override
		public void updateLocation() throws RemoteException {
			updateLocationProvider();
		}

		/**
		 * 
		 */
		@Override
		public String getLastAddress() throws RemoteException {
			return getAddress(AddressBuilder.DEFAULT_ADDRESS_TEMPLATE);
		}
	};

	/**
	 * 
	 */
	@Override
	public void onCreate() {

		Log.i(LocateMeService.class.getSimpleName(), "Creation du service de localisation.");

		// Ajout du service comme listener des modifications de préférences
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
		mPrefsFormat=new PreferencesFormat(this);
		
		// Récupération du gestionnaire de localisation
		mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

		// Récupère la dernière position connue
		mLastLocation=mLocationManager.getLastKnownLocation(getMyBestProvider());

		// Mise à jour de service de localisation
		updateLocationProvider();

		// Création du conteneur des vues du widget
		updateViews = new RemoteViews(this.getPackageName(), R.layout.appwidget);

		// Récupération du gestionnaire des widget
		appWidgetManager=AppWidgetManager.getInstance(this);


		geocoder=new Geocoder(this);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		// Mise à jour des composants graphiques du widget
		updateWidgetUI();
	}

	/**
	 * 
	 */
	@Override
	public void onDestroy() {

		Log.i(LocateMeService.class.getSimpleName(), "Suppression du service de localisation.");

		// Supprime notre service comme "receveur" des évenements de modification de la localisation 
		mLocationManager.removeUpdates(this);
	}

	/**
	 * 
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	/**
	 * 
	 */
	@Override
	public void onLocationChanged(Location location) {

		Log.i(LocateMeService.class.getSimpleName(), "Detection d'une nouvelle position : "+location);

		// met à jour la position
		mLastLocation=location;
		mLastAddress=null;

		// Mise à jour des composants graphiques du widget
		updateWidgetUI();
	}

	/**
	 * Met à jour les composants graphiques du widget.
	 * @param context le contexte du service
	 */
	private void updateWidgetUI (){

		// mise en place du lien vers le menu des choix d'actions
		Intent actionChoiceIntent=new Intent(this,ActionChoice.class);
		updateViews.setOnClickPendingIntent(R.id.widget, PendingIntent.getActivity(this,0,actionChoiceIntent,0));


		if(mLastLocation==null){
			// Remplace l'adresse trouvée (ou l'erreur) dans le composant graphique du widget
			updateViews.setTextViewText(R.id.address_textview, getString(R.string.position_not_found));
			// Remplace le texte du champ info
			updateViews.setTextViewText(R.id.more_textview, null);
		}
		else {

			String address=getAddress(AddressBuilder.DEFAULT_ADDRESS_TEMPLATE_WITHOUT_COUNTRY);
			if(address==null){
				address=getString(R.string.address_not_found,
						Utils.round(mLastLocation.getLatitude(),6),
						Utils.round(mLastLocation.getLongitude(),6));
			}
			// Remplace l'adresse trouvée (ou l'erreur) dans le composant graphique du widget
			updateViews.setTextViewText(R.id.address_textview, address);

			// Remplace le texte du champ info
			if(mLastLocation.hasAccuracy())
				updateViews.setTextViewText(R.id.more_textview, mPrefsFormat.printAccuracy(mLastLocation.getAccuracy()));
			else 
				updateViews.setTextViewText(R.id.more_textview, "");
		}

		// Demande la mise à jour des composant graphique du widget
		appWidgetManager.updateAppWidget(new ComponentName(this, LocateMe.class), updateViews);
	}

	/**
	 * Met à jour le service de localisation
	 * @param sharedPreferences les préférences utilisateur
	 */
	private void updateLocationProvider (){

		mLastLocation=null;	

		// Affecte notre service comme "receveur" des évenements de modification de la localisation 

		mLocationManager.removeUpdates(this);

		String bestProvider = getMyBestProvider();

		if(bestProvider!=null)
			mLocationManager.requestLocationUpdates(bestProvider,mPrefsFormat.getRefreshTime(),mPrefsFormat.getRefreshDistance(), this);
	}

	/**
	 * 
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

		Log.i(LocateMeService.class.getName(), "Modification du parametre de preference "+key);

		if(mPrefsFormat.isRequiredLocationUpdate(key))
			updateLocationProvider();
	}


	public String getAddress (AddressInfo[] template){

		if(mLastLocation==null)
			return null;

		if(mLastAddress==null){
			try {
				List<Address> addresses = geocoder.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
				if(addresses.isEmpty())
					return null;
				mLastAddress=addresses.get(0);
			} catch (IOException e) {
				return null;
			}
		}
		return new AddressBuilder().build(mLastAddress, template);
	}

	/**
	 * Séléctionne les meilleurs critères de sélection du "founisseur" de position (ex : GPS, triangulation GSM) pour ce service.
	 * @return les meilleurs service de localisation
	 */
	private String getMyBestProvider (){

		if(mPrefsFormat.canUseGPS())
			return LocationManager.NETWORK_PROVIDER;

		Criteria criteria=new Criteria();
		criteria.setAltitudeRequired(false);
		criteria.setSpeedRequired(false);
		criteria.setBearingRequired(false);
		criteria.setAccuracy(Criteria.ACCURACY_FINE);

		return mLocationManager.getBestProvider(criteria, true);
	}

	/**
	 * 
	 */
	@Override
	public void onProviderDisabled(String provider) {
		// DO NOTHING
	}

	/**
	 * 
	 */
	@Override
	public void onProviderEnabled(String provider) {
		//updateLocationProvider(mSharedPreferences);
	}

	/**
	 * 
	 */
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// DO NOTHING
	}
}