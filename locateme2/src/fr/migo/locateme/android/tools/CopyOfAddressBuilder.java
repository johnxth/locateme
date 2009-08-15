package fr.migo.locateme.android.tools;

import java.io.IOException;
import java.util.List;

import fr.migo.locateme.android.R;
import fr.migo.locateme.android.R.string;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

/**
 * {@link CopyOfAddressBuilder} permet de constuire une adresse en fonction d'un locallisation {@link Location}. Elle utilise pour cela la classe {@link Geocoder}.
 *
 */
public class CopyOfAddressBuilder {

	/**
	 * 
	 */
	private final Geocoder geocoder;

	/**
	 * 
	 */
	private final Context context;

	/**
	 * Template affichant "num�ro, rue \n code postal, ville"
	 */
	public final static AddressInfo[] DEFAULT_ADDRESS_TEMPLATE_WITHOUT_COUNTRY=new AddressInfo[]{
		AddressInfo.FEATURE_NAME,AddressInfo.COMMA_CHAR,AddressInfo.THOROUGHFARE,AddressInfo.BACKSPACE_CHAR,
		AddressInfo.POSTAL_CODE,AddressInfo.COMMA_CHAR,AddressInfo.LOCALITY};

	/**
	 * Template affichant "num�ro, rue \n code postal, ville \n pays"
	 */
	public final static AddressInfo[] DEFAULT_ADDRESS_TEMPLATE=new AddressInfo[]{
		AddressInfo.FEATURE_NAME,AddressInfo.COMMA_CHAR,AddressInfo.THOROUGHFARE,AddressInfo.BACKSPACE_CHAR,
		AddressInfo.POSTAL_CODE,AddressInfo.COMMA_CHAR,AddressInfo.LOCALITY,AddressInfo.COMMA_CHAR,
		AddressInfo.COUNTRY_NAME};

	/**
	 * Constructeur
	 * @param context le contexte instanciant {@link CopyOfAddressBuilder}
	 */
	public CopyOfAddressBuilder(Context context) {
		this.context=context;
		this.geocoder=new Geocoder(context);
	}

	/**
	 * Construit dynamiquement l'adresse � partir de l'objet {@link Location}
	 * @param location la localisation
	 * @return l'adresse construite, {@link R.string.address_not_found} sinon
	 */
	public String build (Location location){
		return build(location,DEFAULT_ADDRESS_TEMPLATE_WITHOUT_COUNTRY);
	}

	/**
	 * Construit dynamiquement l'adresse de la forme du template � partir de l'objet {@link Location}. Retourne un exception si l'adresse n'a pu �tre d�termin�e.
	 * @param location la localisation
	 * @param template le format de l'adresse � construire
	 * @return l'adresse construite
	 * @throws IOException si l'adresse n'a pu �tre d�termin�e ou construite
	 */
	public String buildOrThrows (Location location, AddressInfo[] template) throws IOException{
		if(location==null)
			throw new IllegalArgumentException("bad location");

		List<Address> addresses=geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
		if(addresses.isEmpty())
			throw new IllegalArgumentException("Unknow location");

		Address address=addresses.get(0);

		StringBuilder builder=new StringBuilder();
		
		String lastAddressValue=null;
		AddressInfo lastAddressInfo=null;
		
		for(AddressInfo addressInfo:template){
			
			String value=addressInfo.getValue(address);
			
			if(value!=null && value.equals(lastAddressValue)==false){
				
				if(AddressInfo.isSpecialChar(addressInfo)==false || AddressInfo.isSpecialChar(lastAddressInfo)==false)
					builder.append(value);
				if(AddressInfo.isSpecialChar(addressInfo)==false)
					lastAddressValue=value;
				lastAddressInfo=addressInfo;
			}
		}
		return builder.toString();
	}
	
	public String build (Address address, AddressInfo[] template){

		StringBuilder builder=new StringBuilder();
		
		String lastAddressValue=null;
		AddressInfo lastAddressInfo=null;
		
		for(AddressInfo addressInfo:template){
			
			String value=addressInfo.getValue(address);
			
			if(value!=null && value.equals(lastAddressValue)==false){
				
				if(AddressInfo.isSpecialChar(addressInfo)==false || AddressInfo.isSpecialChar(lastAddressInfo)==false)
					builder.append(value);
				if(AddressInfo.isSpecialChar(addressInfo)==false)
					lastAddressValue=value;
				lastAddressInfo=addressInfo;
			}
		}
		return builder.toString();
	}

	/**
	 * Construit dynamiquement l'adresse de la forme du template � partir de l'objet {@link Location}.
	 * @param location la localisation
	 * @param template le format de l'adresse � construire
	 * @return l'adresse construite, {@link R.string.address_not_found} sinon
	 */
	public String build (Location location, AddressInfo[] template){
		
		String textToSet=context.getString(R.string.address_not_found);
		try {
			textToSet=buildOrThrows(location,template);
		} catch (Exception e) {
			textToSet+=" : "+e.getLocalizedMessage();
			e.printStackTrace();
		}
		return textToSet;
	}


	/**
	 * La liste des param�tres possible pour construire une adresse.
	 */
	public enum AddressInfo {

		/**
		 * Num�ro de la rue
		 */
		FEATURE_NAME{
			@Override public String getValue(Address address) { return address.getFeatureName(); }
		},
		/**
		 * Nom de la rue
		 */
		THOROUGHFARE{
			@Override public String getValue(Address address) { return address.getThoroughfare(); }
		},
		/**
		 * Code postal
		 */
		POSTAL_CODE{
			@Override public String getValue(Address address) { return address.getPostalCode(); }
		},
		/**
		 * Ville
		 */
		LOCALITY{
			@Override public String getValue(Address address) { return address.getLocality(); }
		},
		/**
		 * Pays
		 */
		COUNTRY_NAME{
			@Override public String getValue(Address address) { return address.getCountryName(); }
		},
		/**
		 * Caract�re de retour � la ligne
		 */
		BACKSPACE_CHAR{
			@Override public String getValue(Address address) { return "\n"; }
		},
		/**
		 * Caract�re virgule
		 */
		COMMA_CHAR{
			@Override public String getValue(Address address) { return ", "; }
		};
		
		/**
		 * Retourne la valeur du pram�tre de l'adresse
		 * @param address l'adresse
		 * @return la valeur du param�tre
		 */
		public abstract String getValue (Address address); 
		
		/**
		 * V�rifie si l'{@link AddressInfo} n'est pas un caract�re sp�cial de s�paration
		 * @param addressInfo l'{@link AddressInfo} � v�rifier
		 * @return <code>true</code> si la caract�re est sp�cial, <code>false</code> sinon
		 */
		public static boolean isSpecialChar (AddressInfo addressInfo){
			return addressInfo==BACKSPACE_CHAR || addressInfo==COMMA_CHAR;
		}
	}
}