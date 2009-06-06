package fr.migo.locateme.android;

import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.provider.Contacts;
import android.provider.Contacts.ContactMethods;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * Cette classe permet de simplifier la construction de la boîte de dialogue utilisée pour configurer la nouvelle adresse d'un contact
 *
 */
public class AddressContactDialogBuilder {
	
	/**
	 * 
	 */
	private final View parentLayout;
	
	/**
	 * 
	 */
	private final EditText addressEditText;
	
	/**
	 * 
	 */
	private final EditText customAddressTypeEditText;
	
	/**
	 * 
	 */
	private final Spinner addressTypeSpinner;
	
	/**
	 * 
	 */
	private final Activity parentActivity;
	
	/**
	 * Index des types d'adresses {@link ContactMethods} en fonction de la liste  {@link android.R.array#postalAddressTypes}.
	 */
	private final static HashMap<Integer, Integer> ADDRESS_TYPE_INDEXES=new HashMap<Integer, Integer>();
	static {
		ADDRESS_TYPE_INDEXES.put(0, Contacts.ContactMethods.TYPE_HOME);
		ADDRESS_TYPE_INDEXES.put(1, Contacts.ContactMethods.TYPE_WORK);
		ADDRESS_TYPE_INDEXES.put(2, Contacts.ContactMethods.TYPE_OTHER);
		ADDRESS_TYPE_INDEXES.put(3, Contacts.ContactMethods.TYPE_CUSTOM);
	}
	
	/**
	 * Constructeur
	 * @param parent l'activité parente
	 * @param address la nouvelle adresse par défaut 
	 */
	public AddressContactDialogBuilder(Activity parent, String address) {
		
		parentActivity=parent;
		
		// Récupère le layout a intégrer dans la boîte de dialogue
		parentLayout = parentActivity.getLayoutInflater().inflate(R.layout.edit_contact_address, (ViewGroup) parentActivity.findViewById(R.id.edit_contact_address_root));

		// L'éditeur de texte qui doit contenir l'adresse à ajouter
		addressEditText=(EditText)parentLayout.findViewById(R.id.address_edit_text);
		addressEditText.setText(address);
		
		// L'éditeur de texte où l'on doit saisir le type d'adresse customisable.
		customAddressTypeEditText=(EditText)parentLayout.findViewById(R.id.custom_address_type_edit_text);

		// Le spinner permettant de sélectionner le type d'adresse (domicile, bureau, ...)
		// Lorsque l'on clique sur "Custom" le spinner fait apparaître l'éditeur de texte où l'on doit saisir le type d'adresse customisable.
		addressTypeSpinner=(Spinner)parentLayout.findViewById(R.id.address_type_spinner);
		addressTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			/**
			 * 
			 */
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
				if(ADDRESS_TYPE_INDEXES.get(position)==Contacts.ContactMethods.TYPE_CUSTOM)
					customAddressTypeEditText.setVisibility(View.VISIBLE);
				else 
					customAddressTypeEditText.setVisibility(View.GONE);
			}

			/**
			 * 
			 */
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				parent.setSelection(0);
			}
		});
	}
	
	/**
	 * Affiche la boîte de dialogue
	 * @param title le titre de la boîte de dialogue
	 * @param positiveOnClickListener Le listener à positionner sur le bouton de confirmation
	 */
	public void show (String title, DialogInterface.OnClickListener positiveOnClickListener ){
		// Construit la boîte de dialogue pour valider la nouvelle adresse et sélectionner son type.
		new AlertDialog.Builder(parentActivity)
		.setTitle(title)
		.setView(parentLayout)
		.setPositiveButton(android.R.string.ok, positiveOnClickListener)
		.setNegativeButton(android.R.string.cancel,null)
		.create().show();
	}
	
	/**
	 * 
	 * @return l'activité parente de la boîte de dialogue
	 */
	public Activity getParentActivity (){
		return parentActivity;
	}
	
	/**
	 * 
	 * @return l'adresse courante
	 */
	public String getAddress (){
		return addressEditText.getText().toString();
	}
	
	/**
	 * 
	 * @return le type d'adresse saisie
	 */
	public int getAddressType (){
		return ADDRESS_TYPE_INDEXES.get(addressTypeSpinner.getSelectedItemPosition());
	}
	
	/**
	 * 
	 * @return le type d'adresse choisi dans le cas où le type <code>Custom</code> a été choisi
	 */
	public String getCustomAddressTypeValue (){
		return customAddressTypeEditText.getText().toString();
	}
}
