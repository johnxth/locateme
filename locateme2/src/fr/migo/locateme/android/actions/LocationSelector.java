package fr.migo.locateme.android.actions;

import com.google.android.maps.MapActivity;

import android.app.Activity;
import android.os.Bundle;
import fr.migo.locateme.android.R;


public class LocationSelector extends MapActivity{
	
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.location_selector);
		
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}



}
