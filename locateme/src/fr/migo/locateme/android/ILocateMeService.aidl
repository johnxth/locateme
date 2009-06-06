package fr.migo.locateme.android;

import android.location.Location;


/**
* L'interface permettant de communiquer avec le service LocateMe$LocateMeService
*/
interface ILocateMeService {

	/**
	* R�cup�re la derni�re localisation trouv�e
	*/
    Location getLastLocation();
    
    /**
    * Force la mise � jour de la localisation
    */
    void updateLocation ();
    
}