package fr.migo.locateme.android.tools;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Trousse à outil 
 *
 */
public class Utils {


	
	/**
	 * Arrondi un double n chiffre après la virgule
	 * @param number le double à arrondir
	 * @param floatNb le nobre de chiffre apès la vigure à arrondir
	 * @return le double arrondi
	 */
	public static double round (double number, int floatNb){
		return Math.round(number*Math.pow(10, floatNb))/Math.pow(10, floatNb);
	}
	
	
	
	
}
