package ngvl.android.blogger.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import ngvl.android.blogger.R;
import ngvl.android.blogger.storage.PostContract;
import ngvl.android.blogger.storage.RssReaderProvider;

public class PostsFavoritesListFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = PostsFavoritesListFragment.class.getSimpleName();

	private SimpleCursorAdapter mAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.w(TAG, "onCreateView");

		// We have a menu item to show in action bar.
		setHasOptionsMenu(true);
		getActivity().setProgressBarIndeterminate(true);

		// Create an empty adapter used to display the loaded data.
		mAdapter = new PostsCursorAdapter(getActivity(), null);
		
		// Inflating view...
		View v = inflater.inflate(R.layout.fragment_post_list, null);

		ListView listView = (ListView) v.findViewById(android.R.id.list);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		listView.setEmptyView(v.findViewById(R.id.emptyText));
		listView.setAdapter(mAdapter);

        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout)
                v.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setEnabled(false);
		
		// Prepare the loader. Either re-connect with an existing one, or start a new one.
		getLoaderManager().initLoader(0, null, this);
		return v;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Log.w(TAG, "onListItemClick: ID: " + id + " -- " + l.getAdapter().getItem(position));
		((OnPostSelectedListener)getActivity()).onPostSelected(id);
	}

	// ******************************************************************************************
	// LoaderManager.LoaderCallbacks<Cursor> methods
	// ******************************************************************************************
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Log.w(TAG, "onCreateLoader");
		return new CursorLoader(
				getActivity(),
				RssReaderProvider.CONTENT_URI,
				new String[] {
                        PostContract._ID,
                        PostContract.TITLE,
                        PostContract.THUMBNAIL,
                        PostContract.LAST_UPDATE,
                        PostContract.FAVORITE},
                PostContract.FAVORITE +" = "+ 1,
				null,
                PostContract.PUBLISHED +" DESC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		Log.w(TAG, "onLoadFinished");
		mAdapter.swapCursor(data);

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		Log.w(TAG, "onLoadReset");
		mAdapter.swapCursor(null);
	}
}
