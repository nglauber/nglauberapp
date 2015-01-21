package ngvl.android.blogger.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import ngvl.android.blogger.R;
import ngvl.android.blogger.model.Entry;

public class PostDetailActivity extends ActionBarActivity {

    private static final String TAG = PostDetailActivity.class.getSimpleName();
    
	public static final String EXTRA_ENTRY = "entry";
	
	private Entry entry;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post_detail);
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);
		
		Log.d(TAG, "onCreate");
		
		if (savedInstanceState == null){
			entry = (Entry)getIntent().getParcelableExtra(EXTRA_ENTRY);
			
		} else {
			entry = (Entry)savedInstanceState.getParcelable(EXTRA_ENTRY);
		}
		
		if (entry == null) finish();
		
		PostDetailFragment fragment = PostDetailFragment.newInstance(entry);
		getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.rootDetail, fragment, "detail")
                .commit();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.d(TAG, "onSaveInstanceState");
		
		outState.putParcelable(EXTRA_ENTRY, entry);
	}
}
