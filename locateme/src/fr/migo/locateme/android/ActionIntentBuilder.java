package fr.migo.locateme.android;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;

/**
 * Construit les {@link Intent} pour chaque action possible
 */
public class ActionIntentBuilder {

	private final Context mContext;
	
	public ActionIntentBuilder(Context context) {
		mContext=context;
	}
	
	public Intent intentAddAddressContact (String address){
		Intent addLocationIntent=new Intent(mContext,AddLocation.class);
		addLocationIntent.putExtra(AddLocation.ADDRESS_INTENT, address);
		return addLocationIntent;
	}
	
	public Intent intentDisplayOnMap (Location location){
		return new Intent(Intent.ACTION_VIEW,Uri.parse("geo:"+location.getLatitude()+","+location.getLongitude()));
	}
	
	public Intent intentDisplayAsDirection (String address){
		return new Intent(Intent.ACTION_VIEW,Uri.parse("http://maps.google.com/maps?saddr="+Uri.encode(address)));
	}
	
}
