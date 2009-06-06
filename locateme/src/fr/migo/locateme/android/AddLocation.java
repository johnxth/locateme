package fr.migo.locateme.android;

import android.app.ListActivity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.provider.Contacts;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Cette activité permet d'ajouter une adresse à un contact.
 * TODO : faire un {@link SimpleCursorAdapter} avec l'adresse la plus proche de celle a ajouter, passer {@link Location} dans le {@link Intent}
 */
public class AddLocation extends ListActivity {

	/**
	 * La clé permettant d'ajouter/récupérer l'adresse dans l'{@link Intent} transmis à l'activité {@link AddLocation}. 
	 */
	public static final String ADDRESS_INTENT = "address_intent";

	/**
	 * L'adresse à ajouter
	 * TODO : à remplacer par un {@link Location}
	 */
	private String addressLocation;

	/**
	 * Création de l'activité avec la reception d'un {@link Intent} contenant la paramètre {@link AddLocation#ADDRESS_INTENT}
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		addressLocation = (String)getIntent().getExtras().get(ADDRESS_INTENT);

		// Curseur sur tous les contacts triés par nom
		final Cursor c = getContentResolver().query(Contacts.People.CONTENT_URI, null, null,
				null, Contacts.People.NAME);
		startManagingCursor(c);

		// Construction de la vue
		ListAdapter adapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_1,
				c,
				new String[] { Contacts.People.NAME},
				new int[] { android.R.id.text1 });
		
		setListAdapter(adapter);

		// Listener sur chaque contact de la liste		
		getListView().setOnItemClickListener(new OnItemClickListener(){

			/**
			 * 
			 */
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {

				// Place le curseur à l'élément sélectionné
				c.moveToPosition(position);

				// Le nom du contact
				final String name=c.getString(c.getColumnIndex(Contacts.People.NAME));

				// Son id
				final String contactId=c.getString(c.getColumnIndex(Contacts.People._ID));

				final AddressContactDialogBuilder dialogBuilder=new AddressContactDialogBuilder(AddLocation.this,addressLocation);
				dialogBuilder.show(getString(R.string.address_to_add, name), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						// Récupération du type de l'adresse
						int addressType=dialogBuilder.getAddressType();

						ContentValues cv = new ContentValues(); 
						cv.put(Contacts.ContactMethods.KIND		,Contacts.KIND_POSTAL);
						cv.put(Contacts.ContactMethods.TYPE		,addressType);
						cv.put(Contacts.ContactMethods.DATA		,dialogBuilder.getAddress());
						cv.put(Contacts.ContactMethods.PERSON_ID,contactId);

						// Si le type d'adresse est "Custom", le label saisie est ajouté. 
						if(addressType==Contacts.ContactMethods.TYPE_CUSTOM)	
							cv.put(Contacts.ContactMethods.LABEL,dialogBuilder.getCustomAddressTypeValue());

						// Insertion de la nouvelle adresse pour le contact séléctionné
						getContentResolver().insert(Contacts.ContactMethods.CONTENT_URI, cv);

						// Affichage d'un "Toast" de confimation
						Toast.makeText(AddLocation.this, getString(R.string.address_added, name), Toast.LENGTH_SHORT).show();

						// Ferme l'activité
						dialogBuilder.getParentActivity().finish();
					}
				});
			}
		});
	}

	/**
	 * 
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.address_contact, menu);
		return true;
	}

	/**
	 * 
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		// L'item de préférence
		case R.id.address_contact_menu_new_contact:

			final AddressContactDialogBuilder dialogBuilder=new AddressContactDialogBuilder(this,addressLocation);

			dialogBuilder.show(getString(R.string.address_to_add_new_contact), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

					Intent intent = new Intent(Contacts.Intents.Insert.ACTION, Contacts.People.CONTENT_URI);

					// Récupération de l'adresse
					intent.putExtra(Contacts.Intents.Insert.POSTAL, dialogBuilder.getAddress());

					// Récupération du type de l'adresse
					int addressType=dialogBuilder.getAddressType();

					// Si le type d'adresse est "Custom", le label saisie est ajouté. 
					intent.putExtra(Contacts.Intents.Insert.POSTAL_TYPE, addressType);

					if(addressType==Contacts.ContactMethods.TYPE_CUSTOM)
						intent.putExtra(Contacts.Intents.Insert.POSTAL_TYPE, dialogBuilder.getCustomAddressTypeValue());

					// Lancement de la création du contact
					startActivity(intent);

					// Ferme l'activité
					dialogBuilder.getParentActivity().finish();
				}
			});
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
