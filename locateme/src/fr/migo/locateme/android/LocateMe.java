package fr.migo.locateme.android;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

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

	/**
	 * {@link LocateMeService} est un service Android qui a la particularité d'intercepter les évenements liés à la localisation grâce à son implémentation de l'interface {@link LocationListener}. 
	 * 
	 */
	public static class LocateMeService extends Service implements LocationListener, OnSharedPreferenceChangeListener {

		/**
		 * 
		 */
		private final static String PREF_LOCATION_UPDATE_TIME="pref_location_update_time";

		/**
		 * 
		 */
		private final static String PREF_LOCATION_UPDATE_DISTANCE="pref_location_update_distance";
		
		/**
		 * 
		 */
		private final static String PREF_LOCATION_UPDATE_GPS="pref_location_update_gps";

		/**
		 * 
		 */
		private final static String PREF_DISPLAY_MORE_INFO="pref_display_more_info";

		/**
		 * 
		 */
		private final static String PREF_DISPLAY_METER_UNITS="pref_display_measure_units";

		/**
		 * 
		 */
		private final static String PREF_SHORTCUT="pref_shortcut";

		/**
		 * Le gestionnaire de localisation Android
		 */
		private LocationManager mLocationManager;

		/**
		 * La dernière posistion connue de l'appareil. Peut-être <code>null</code>
		 */
		private Location mLastLocation;

		/**
		 * Le composant permettant d'obtenir l'adresse de la dernière position
		 */
		private AddressBuilder mAddressBuilder;

		/**
		 * Les préférences utilisateurs
		 */
		private SharedPreferences mSharedPreferences;

		/**
		 * Les vues du widget
		 */
		private RemoteViews updateViews;

		/**
		 * Le gestionnaire du widget
		 */
		private AppWidgetManager appWidgetManager ;

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
				updateLocationProvider(mSharedPreferences);
			}
		};

		/**
		 * Séléctionne les meilleurs critères de sélection du "founisseur" de position (ex : GPS, triangulation GSM) pour ce service.
		 * @return les meilleurs service de localisation
		 */
		private String getMyBestProvider (){
			
			if(mSharedPreferences.getBoolean(PREF_LOCATION_UPDATE_GPS, true)==false)
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
		public void onCreate() {

			Log.i(LocateMeService.class.getSimpleName(), "Creation du service de localisation.");

			// Ajout du service comme listener des modifications de préférences
			mSharedPreferences=PreferenceManager.getDefaultSharedPreferences(this);
			mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

			// Initialisation du constructeur d'adresse
			mAddressBuilder=new AddressBuilder(this);

			// Récupération du gestionnaire de localisation
			mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

			// Récupère la dernière position connue
			mLastLocation=mLocationManager.getLastKnownLocation(getMyBestProvider());

			// Mise à jour de service de localisation
			updateLocationProvider(mSharedPreferences);

			// Création du conteneur des vues du widget
			updateViews = new RemoteViews(this.getPackageName(), R.layout.appwidget);

			// Récupération du gestionnaire des widget
			appWidgetManager=AppWidgetManager.getInstance(this);
		}

		@Override
		public void onStart(Intent intent, int startId) {
			super.onStart(intent, startId);
			// Mise à jour des composants graphiques du widget
			updateUI(this);
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

			// Mise à jour des composants graphiques du widget
			updateUI(this);
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

		/**
		 * Met à jour les composants graphiques du widget.
		 * @param context le contexte du service
		 */
		private void updateUI (Context context){

			// mise en place du lien vers le menu des choix d'actions
			Intent actionChoiceIntent=new Intent(context,ActionChoice.class);
			updateViews.setOnClickPendingIntent(R.id.icon_choice_action, PendingIntent.getActivity(context,0,actionChoiceIntent,0));
			
			if(mLastLocation==null){
				// Remplace l'adresse trouvée (ou l'erreur) dans le composant graphique du widget
				updateViews.setTextViewText(R.id.address_textview, getString(R.string.position_not_found));
				// Remplace le texte du champ info
				updateViews.setTextViewText(R.id.more_textview, null);
			}
			else {

				String address=null;
				try {
					address=mAddressBuilder.buildOrThrows(mLastLocation,AddressBuilder.DEFAULT_ADDRESS_TEMPLATE_WITHOUT_COUNTRY);
				} catch (Exception e) {
					address=getString(R.string.address_not_found,
							Utils.round(mLastLocation.getLatitude(),6),
							Utils.round(mLastLocation.getLongitude(),6));
				}
				// Remplace l'adresse trouvée (ou l'erreur) dans le composant graphique du widget
				updateViews.setTextViewText(R.id.address_textview, address);

				// Remplace le texte du champ info
				if(mSharedPreferences.getBoolean(PREF_DISPLAY_MORE_INFO, false) && mLastLocation.hasAccuracy()){
					Float accuracy=mLastLocation.getAccuracy();
					if(mSharedPreferences.getBoolean(PREF_DISPLAY_METER_UNITS, true)==false)
						accuracy*=0.000621371192;
					updateViews.setTextViewText(R.id.more_textview, Float.toString(accuracy));
				}
				else 
					updateViews.setTextViewText(R.id.more_textview, "26.0");
			}

			// Demande la mise à jour des composant graphique du widget
			appWidgetManager.updateAppWidget(new ComponentName(context, LocateMe.class), updateViews);
		}


		/**
		 * NON UTILISE
		 * Met à jour le raccourci sur le texte
		 * @param context le contexte
		 * @param sharedPreferences les préférences utilisateur
		 */
		private void updateTextShorcut (Context context, SharedPreferences sharedPreferences){

			int shortcutType=sharedPreferences.getInt(PREF_SHORTCUT, 0);

			switch (shortcutType) {
			case 0:
				updateViews.setOnClickPendingIntent(R.id.address_textview, null);
				break;
			case 1:
				break;
			}
		}

		/**
		 * Met à jour le service de localisation
		 * @param sharedPreferences les préférences utilisateur
		 */
		private void updateLocationProvider (SharedPreferences sharedPreferences){

			// Affecte notre service comme "receveur" des évenements de modification de la localisation 

			mLocationManager.removeUpdates(this);
			
			String bestProvider = getMyBestProvider();

			if(bestProvider!=null){

				int minTime = sharedPreferences.getInt(PREF_LOCATION_UPDATE_TIME, 300)*1000;
				int minDistance = sharedPreferences.getInt(PREF_LOCATION_UPDATE_DISTANCE, 1500);

				mLocationManager.requestLocationUpdates(bestProvider,minTime, minDistance, this);
			}
		}

		/**
		 * 
		 */
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

			Log.i(LocateMeService.class.getName(), "Modification du parametre de preference "+key);

			if(key.equals(PREF_LOCATION_UPDATE_TIME) || 
					key.equals(PREF_LOCATION_UPDATE_DISTANCE) || 
					key.equals(PREF_LOCATION_UPDATE_GPS)){
				updateLocationProvider(sharedPreferences);
			}

			if(key.equals(PREF_SHORTCUT))
				updateTextShorcut(this,mSharedPreferences);
		}
	}
}