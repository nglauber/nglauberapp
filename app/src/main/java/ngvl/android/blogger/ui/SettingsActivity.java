package ngvl.android.blogger.ui;

import java.util.ArrayList;
import java.util.List;

import ngvl.android.blogger.R;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SettingsActivity extends ActionBarActivity {
	private static final int CLEAR_HISTORIC_SETTINGS = 0;
	private Toolbar mToolbar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        
        this.configureActionBar();
        
		ListView listViewConfiguracoes = (ListView) findViewById(R.id.listSettings);
		
		List<String> list = new ArrayList<String>();
		list.add("Limpar sugestões de buscas");
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list);
		listViewConfiguracoes.setAdapter(adapter);
		
		listViewConfiguracoes.setOnItemClickListener(itemListener());
	}
	
	 
	 // ******************************************************************************************
     // Private methods
     // ******************************************************************************************
	
	
	private OnItemClickListener itemListener(){
		 return new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int position,
						long arg3) {
				switch (position) {
				case CLEAR_HISTORIC_SETTINGS:
					 ClearHistoricDialogFragment aboutDialog = new ClearHistoricDialogFragment();
				     aboutDialog.show(SettingsActivity.this.getSupportFragmentManager(), "clear");
					break;

				default:
					break;
				}
					
				}
				
			};
	 }
	
	
	 private void configureActionBar(){
		 ActionBar actionBar = getSupportActionBar();
	     actionBar.setDisplayHomeAsUpEnabled(true);
	     actionBar.setTitle("Configurações");
	 }
	 

}
