package fr.migo.locateme.android;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Boîte de dialogue pour les préférences affichant une bar de progression éditable pour l'utilisateur. 
 */
public class SeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener {

	/**
	 * 
	 */
	private SeekBar seekBar;
	
	
	/**
	 * 
	 */
	private ImageView minus;
	
	/**
	 * 
	 */
	private ImageView plus;
	
	/**
	 * 
	 */
	private TextView textView;

	/**
	 * La valeur maximum possible
	 */
	private int max;
	
	/**
	 * La valeur séléectionnée
	 */
	private int progress;
	
	/**
	 * L"échellon entre deux valeurs
	 */
	private int delta;

	/**
	 * Constructeur
	 * @param context le contexte
	 * @param attrs les attributs
	 */
	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	/**
	 * Initialialise les valeurs de la bar de progression
	 * @param attrs les attributs
	 */
	private void init(AttributeSet attrs) {

		TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.SeekBarPreference);
		this.max=typedArray.getInt(R.styleable.SeekBarPreference_max,100);
		this.progress=typedArray.getInt(R.styleable.SeekBarPreference_progress,0);
		this.delta=typedArray.getInt(R.styleable.SeekBarPreference_delta,1);
	}

	/**
	 * 
	 */
	protected void onPrepareDialogBuilder(Builder builder) {
		
		int currentProgress=getPersistedInt(progress);
		
		View view=View.inflate(getContext(), R.layout.seekbar_preference, null);

		seekBar=(SeekBar) view.findViewById(R.id.seek);
		seekBar.setMax(max/delta);
		seekBar.setProgress(currentProgress/delta);
		seekBar.setOnSeekBarChangeListener(this);

		
		minus = (ImageView) view.findViewById(R.id.minus);
		minus.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				seekBar.setProgress(seekBar.getProgress()-1);
			}
		});
		
		plus = (ImageView) view.findViewById(R.id.plus);
		plus.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				seekBar.setProgress(seekBar.getProgress()+1);
			}
		});
		
		textView= (TextView) view.findViewById(R.id.progress);
		textView.setText(currentProgress+"/"+max);

		builder.setView(view);

		super.onPrepareDialogBuilder(builder);
	}

	/**
	 * 
	 */
	protected void onDialogClosed(boolean positiveResult) {
		if(positiveResult)
			persistInt(seekBar.getProgress()*delta);
	}
	
	/**
	 * 
	 */
	@Override
	public void onProgressChanged(SeekBar sb, int arg1, boolean arg2) {
		textView.setText((seekBar.getProgress()*delta)+"/"+max);
	}

	/**
	 * 
	 */
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// DO NOTHING
	}

	/**
	 * 
	 */
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// DO NOTHING
	}
} 