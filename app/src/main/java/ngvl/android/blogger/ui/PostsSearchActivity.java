package ngvl.android.blogger.ui;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import ngvl.android.blogger.R;
import ngvl.android.blogger.http.HttpUtil;
import ngvl.android.blogger.model.Entry;
import ngvl.android.blogger.ui.PostsSearchFragment.OnPostFoundSelectedListener;

public class PostsSearchActivity extends ActionBarActivity implements OnPostFoundSelectedListener {

    private static final String TAG = PostsSearchActivity.class.getSimpleName();

    private Toolbar mToolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!HttpUtil.hasConnectionAvailable(this)){
			Toast.makeText(getApplicationContext(), R.string.no_conneciton, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		setContentView(R.layout.activity_posts_search);

        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		handleIntent(getIntent());
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handleIntent(intent);
	}

    // ******************************************************************************************
    // OnPostFoundSelectedListener implementation method
    // ******************************************************************************************
    @Override
	public void onPostSelected(Entry entry) {
		FragmentManager fm = getSupportFragmentManager();
		if (entry != null) {
			if (isTablet()) {
					PostDetailFragment fragment = PostDetailFragment
							.newInstance(entry);
					fm.beginTransaction()
							.replace(R.id.rootSearchDetail, fragment, "detail")
							.commit();
	
			} else {
                finish();
                Intent it = new Intent(this, PostDetailActivity.class);
                it.putExtra(PostDetailActivity.EXTRA_ENTRY, entry);
                startActivity(it);
            }
        }
	}

    // ******************************************************************************************
    // Private methods
    // ******************************************************************************************
    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search your data somehow
            Log.d(TAG, "Searching for:"+ query);

            getSupportActionBar().setTitle(getString(R.string.search_results_from, query));

            FragmentManager fm = getSupportFragmentManager();
            PostsSearchFragment fragment = (PostsSearchFragment)
                    fm.findFragmentByTag("search");
            if (fragment == null){
                fragment = PostsSearchFragment.newInstance(query);
            }
            fm.beginTransaction()
                    .replace(R.id.rootSearchList, fragment, "search")
                    .commit();
        }
    }

    private boolean isTablet() {
        return getResources().getBoolean(R.bool.tablet);
    }
}
