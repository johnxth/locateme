package fr.migo.locateme.android.tools;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Trousse � outil 
 *
 */
public class Utils {


	
	/**
	 * Arrondi un double n chiffre apr�s la virgule
	 * @param number le double � arrondir
	 * @param floatNb le nobre de chiffre ap�s la vigure � arrondir
	 * @return le double arrondi
	 */
	public static double round (double number, int floatNb){
		return Math.round(number*Math.pow(10, floatNb))/Math.pow(10, floatNb);
	}
	
	
	
	
}
