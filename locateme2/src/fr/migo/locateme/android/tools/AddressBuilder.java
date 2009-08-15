package fr.migo.locateme.android.tools;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

/**
 * {@link AddressBuilder} permet de constuire une adresse en fonction d'un locallisation {@link Location}. Elle utilise pour cela la classe {@link Geocoder}.
 *
 */
public class AddressBuilder {

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