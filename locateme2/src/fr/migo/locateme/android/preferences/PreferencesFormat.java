package fr.migo.locateme.android.preferences;

import java.text.SimpleDateFormat;
import java.util.Date;

import fr.migo.locateme.android.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferencesFormat {
	
	private final SharedPreferences mSharedPreferences;
	
	/**
	 * 
	 */
	private final static SimpleDateFormat US_DATE_FORMAT=new SimpleDateFormat("MM/dd hh:mm:ss a");

	/**
	 * 
	 */
	private final static SimpleDateFormat FR_DATE_FORMAT=new SimpleDateFormat("dd/MM HH:mm:ss");
	
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

	
	public PreferencesFormat(SharedPreferences sharedPreferences) {
		this.mSharedPreferences=sharedPreferences;
	}
	
	public PreferencesFormat (Context context){
		this(PreferenceManager.getDefaultSharedPreferences(context));
	}
	
	public String printTime (long time){
		
		if(mSharedPreferences.getBoolean(PREF_DISPLAY_FR_DATE_FORMAT, true))
			return US_DATE_FORMAT.format(new Date(time));
		return FR_DATE_FORMAT.format(new Date(time));
	}

	
	public String printAccuracy (Float accuracy){

		Float acc=(accuracy==null || accuracy==0.0)?0:accuracy;
		
		StringBuilder builder=new StringBuilder();

		if(acc>0)
			builder.append("± ");
		
		if(mSharedPreferences.getBoolean(PREF_DISPLAY_METER_UNITS, true)==false)
			builder.append(acc*0.000621371192).append("mi");
		else
			builder.append(acc).append("m");
		return builder.toString(); 
	}
	
	public boolean canUseGPS (){
		return mSharedPreferences.getBoolean(PREF_LOCATION_UPDATE_GPS, true);
	}
	
	public int getRefreshDistance (){
		return mSharedPreferences.getInt(PREF_LOCATION_UPDATE_DISTANCE, 1500);
	}
	
	public int getRefreshTime (){
		return mSharedPreferences.getInt(PREF_LOCATION_UPDATE_TIME, 300)*1000;
	}

	public boolean displayWidgetDetail (){
		return mSharedPreferences.getBoolean(PREF_DISPLAY_MORE_INFO, false);
	}
	
	public boolean isRequiredLocationUpdate (String prefKey){
		return prefKey.equals(PREF_LOCATION_UPDATE_TIME) || 
		prefKey.equals(PREF_LOCATION_UPDATE_DISTANCE) || 
		prefKey.equals(PREF_LOCATION_UPDATE_GPS);
	}
	
	
}
