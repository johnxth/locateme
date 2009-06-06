package fr.migo.locateme.android;

import android.location.Location;


/**
* L'interface permettant de communiquer avec le service LocateMe$LocateMeService
*/
interface ILocateMeService {

	/**
	* Récupère la dernière localisation trouvée
	*/
    Location getLastLocation();
    
    /**
    * Force la mise à jour de la localisation
    */
    void updateLocation ();
    
}