package ngvl.android.blogger.ui;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import ngvl.android.blogger.R;
import ngvl.android.blogger.http.HttpUtil;
import ngvl.android.blogger.service.SyncAdapter;
import ngvl.android.blogger.service.SyncUtil;
import ngvl.android.blogger.storage.DBHelper;
import ngvl.android.blogger.storage.PostContract;
import ngvl.android.blogger.storage.RssReaderProvider;

public class PostsWebListFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor>, OnScrollListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = PostsWebListFragment.class.getSimpleName();

    // State of this fragment
    private static final String STATE_END_LIST_REACHED = "endListReached";
    private static final String STATE_LAST_REQUEST_FAILED = "lastRetrieveFail";
    private static final String STATE_LAST_SELECTED = "lastSelected";
    private static final String STATE_NEXT_PAGE = "nextPage";

    // Screen state
    private boolean mEndListReached;        // true if all posts already retrieved from the server.
    private boolean mLastRetrieveFailed;    // true if the last request to retrieve data failed.
                                            // broadcast messages from sync service.

    private int mLastSelected = -1;         // store the last selected post position.
    private String mNextPage;               // each request returns a "page" of results, this
                                            // attribute stores the next page id to be requested.

    private SimpleCursorAdapter mAdapter;   // Adapter to display posts into ListView.
    private EventsReceiver mReceiver;       // Receives messages from sync service.

    private View footerView;                // Footer view displayed in the end of the ListView.
    private SwipeRefreshLayout mSwipeLayout;// View used to perform "swipe to refresh" behavior.

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w(TAG, "onCreate");
        onRestoredInstanceState(savedInstanceState);
        if (savedInstanceState == null) {
            SyncUtil.requestSync(getActivity(), null);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.w(TAG, "onCreateView");

        // We have a menu item to show in action bar.
        setHasOptionsMenu(true);

        // Create an empty adapter used to display the loaded data.
        mAdapter = new PostsCursorAdapter(getActivity(), null);

        // Inflating view...
        View v = inflater.inflate(R.layout.fragment_post_list, null);

        ListView listView = (ListView) v.findViewById(android.R.id.list);
        listView.setOnScrollListener(this);

        footerView = inflater.inflate(R.layout.loading_footer, null);
        footerView.findViewById(R.id.btnRetry).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "RetryButton_Click");
                        updateListViewFooter();
                        retrievePostsFromPage(mNextPage);
                    }
                });
        listView.setEmptyView(v.findViewById(R.id.emptyText));
        listView.addFooterView(footerView);
        updateListViewFooter();
        listView.setAdapter(mAdapter);

        mSwipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(
                R.color.accent,
                R.color.accent_selection,
                R.color.primary_selection,
                R.color.primary);

        // Prepare the loader. Either re-connect with an existing one, or start a new one.
        getLoaderManager().initLoader(0, null, this);
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.w(TAG, "onSaveInstanceState");
        outState.putBoolean(STATE_END_LIST_REACHED, mEndListReached);
        outState.putBoolean(STATE_LAST_REQUEST_FAILED, mLastRetrieveFailed);
        outState.putString(STATE_NEXT_PAGE, mNextPage);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.w(TAG, "onResume");
        registerReceiver();
        updateListViewFooter();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.w(TAG, "onPause");
        if (mReceiver != null) {
            getActivity().unregisterReceiver(mReceiver);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Log.w(TAG, "onCreateOptionsMenu");
        inflater.inflate(R.menu.menu_post_list, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        final MenuItem searchItem = menu.findItem(R.id.menu_search);
        final SearchView searchView = (SearchView)
                MenuItemCompat.getActionView(searchItem);
        ComponentName searchableInfo = new ComponentName(
                getActivity(),
                PostsSearchActivity.class);

        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(searchableInfo));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                MenuItemCompat.collapseActionView(searchItem);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.w(TAG, "onOptionsItemSelected");
        switch (item.getItemId()) {
		case R.id.menu_about:
			showAbout();
			break;
		case R.id.menu_settings:
			goSettings();
			break;	
		default:
			break;
		}
      
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Log.w(TAG, "onListItemClick: ID: " + id + " -- "+ l.getAdapter().getItem(position));
        setSelectedPosition(position);
    }


    @Override
    public void onRefresh() {
        retrievePostsFromPage(null);
    }

    // ******************************************************************************************
    // Private methods
    // ******************************************************************************************
    private void onRestoredInstanceState(Bundle savedInstanceState) {
        Log.w(TAG, "onRestoredInstanceState");
        // Restoring fragment state
        mEndListReached = false;
        mLastRetrieveFailed = false;

        if (savedInstanceState != null) {
            mEndListReached = savedInstanceState
                    .getBoolean(STATE_END_LIST_REACHED);
            mLastRetrieveFailed = savedInstanceState
                    .getBoolean(STATE_LAST_REQUEST_FAILED);
            mLastSelected = savedInstanceState
                    .getInt(STATE_LAST_SELECTED);
            mNextPage = savedInstanceState
                    .getString(STATE_NEXT_PAGE);
        }
    }

    private void registerReceiver() {
        Log.w(TAG, "registerReceiver");
        mReceiver = new EventsReceiver();
        getActivity().registerReceiver(mReceiver,
                new IntentFilter(SyncAdapter.ACTION_FETCH_COMPLETE));
    }

    private void showAbout() {
        AboutDialogFragment aboutDialog = new AboutDialogFragment();
        aboutDialog.show(getFragmentManager(), "about");
    }
    
    private void goSettings() {
      Intent it = new Intent(getActivity(),SettingsActivity.class);
      startActivity(it);
    }

    private void retrievePostsFromPage(String page) {

        Log.w(TAG,"retrievePostsFromPage::Retrieve posts from page: "+ page);
        if (!HttpUtil.hasConnectionAvailable(getActivity())) {
            Log.d(TAG,
                    "retrievePostsFromPage::No connection");
            setInProgress(false);
            updateListViewFooter();
            Toast.makeText(getActivity(), R.string.no_conneciton,
                    Toast.LENGTH_SHORT).show();

        } else if (isRetrievingData()) {
            Log.d(TAG, "retrievePostsFromPage::Already loading... Ignoring");
            setInProgress(true);
            updateListViewFooter();

        } else {
            Log.d(TAG, "retrievePostsFromPage::Perform request");
            setInProgress(false);

            SyncUtil.requestSync(getActivity(), page);
        }
    }

    private void setInProgress(boolean visible) {
        Log.w(TAG, "showProgress: " + visible);

        View progress = getView().findViewById(R.id.progressBarBig);
        TextView text = (TextView) getView().findViewById(R.id.emptyText);

        if (mAdapter != null && mAdapter.getCount() > 0) {
            footerView.setVisibility(View.VISIBLE);
        } else {
            footerView.setVisibility(View.GONE);
        }

        if (visible) {
            if (mAdapter == null || mAdapter.isEmpty()) {
                progress.setVisibility(View.VISIBLE);
            }
            text.setVisibility(View.GONE);
            getListView().setEmptyView(null);

        } else {
            mSwipeLayout.setRefreshing(false);
            progress.setVisibility(View.GONE);
            text.setVisibility(View.VISIBLE);
            getListView().setEmptyView(text);

            if (!HttpUtil.hasConnectionAvailable(getActivity())) {
                Log.d(TAG, "No connection");
                text.setText(R.string.no_conneciton);

            } else if (mLastRetrieveFailed) {
                Log.d(TAG, "Last try failed");
                text.setText(R.string.fail_to_fetch);

            } else {
                Log.d(TAG, "No data");
                text.setText(R.string.no_items);
            }
        }
    }

    private void updateListViewFooter() {
        Log.w(TAG, "updateListViewFooter");
        TextView txtLoading = (TextView) footerView.findViewById(R.id.txtLoading);
        ProgressBar progressBar = (ProgressBar)footerView.findViewById(R.id.progressBar);
        Button btnRetry = (Button)footerView.findViewById(R.id.btnRetry);

        if (!HttpUtil.hasConnectionAvailable(getActivity())) {
            Log.d(TAG, "No connection");
            progressBar.setVisibility(View.GONE);
            btnRetry.setVisibility(View.VISIBLE);
            txtLoading.setText(R.string.no_conneciton);

        } else if (isRetrievingData()) {
            Log.d(TAG, "Retrieving data");
            progressBar.setVisibility(View.VISIBLE);
            btnRetry.setVisibility(View.GONE);
            txtLoading.setText(R.string.fetching_more_results);

        } else if (mLastRetrieveFailed) {
            Log.d(TAG, "Last try failed");
            progressBar.setVisibility(View.GONE);
            btnRetry.setVisibility(View.VISIBLE);
            txtLoading.setText(R.string.fail_to_fetch);

        } else if (mEndListReached) {
            Log.d(TAG, "End list reached");
            progressBar.setVisibility(View.GONE);
            btnRetry.setVisibility(View.GONE);
            txtLoading.setText(R.string.end_of_posts_reached);

        } else {
            Log.d(TAG, "Unknow state");
            progressBar.setVisibility(View.VISIBLE);
            btnRetry.setVisibility(View.GONE);
            txtLoading.setText(R.string.fetching_more_results);
        }
    }

    private void selectAfterLoad() {
        Log.w(TAG, "selectAfterLoad");
        if (getResources().getBoolean(R.bool.tablet)
                && mLastSelected == -1
                && mAdapter.getCount() > 0) {
            setSelectedPosition(0);
        }
    }

    private void setSelectedPosition(int position) {
        Log.w(TAG, "setSelectedPosition:" + position);
        mLastSelected = position;
        ((OnPostSelectedListener) getActivity()).onPostSelected(
                getListView().getAdapter().getItemId(position));
    }

    // ******************************************************************************************
    // OnScrollListener methods
    // ******************************************************************************************
    public void onScrollStateChanged(AbsListView listView, int scrollState) {
        int totalVisible = listView.getCount() - 1;
        int lastInScreen = listView.getLastVisiblePosition();

        Log.d(TAG, "onScroll");
        if (totalVisible > 0 && !isRetrievingData() && !mEndListReached
                && !mLastRetrieveFailed && (lastInScreen == totalVisible)) {

            retrievePostsFromPage(mNextPage);
        }
    }

    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        // Do nothing
    }

    // ******************************************************************************************
    // LoaderManager.LoaderCallbacks<Cursor> methods
    // ******************************************************************************************
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.w(TAG, "onCreateLoader");

        return new CursorLoader(
                getActivity(),
                Uri.withAppendedPath(RssReaderProvider.CONTENT_URI, "all"),
                new String[]{
                        PostContract._ID,
                        PostContract.TITLE,
                        PostContract.THUMBNAIL,
                        PostContract.LAST_UPDATE,
                        PostContract.FAVORITE},
                PostContract.PUBLISHED + " >= ?",
                new String[]{DBHelper.SUB_SELECT_MIN_PUBLISHED},
                PostContract.PUBLISHED + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.w(TAG, "onLoadFinished");
        mAdapter.swapCursor(data);

        setInProgress(isRetrievingData());

        // Kids... this is weird, but it was necessary :)
        getListView().postDelayed(new Runnable() {
            @Override
            public void run() {
                selectAfterLoad();
            }
        }, 200);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.w(TAG, "onLoadReset");
        mAdapter.swapCursor(null);
    }

    // ******************************************************************************************
    // Receiver to handle events
    // ******************************************************************************************
    class EventsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Log.w(TAG, "EventsReceiver::onReceive "+ action);
            if (SyncAdapter.ACTION_FETCH_COMPLETE.equals(action)) {

                mLastRetrieveFailed = intent.getBooleanExtra(
                        SyncAdapter.EXTRA_FAIL, false);

                if (mLastRetrieveFailed) {
                    Log.d(TAG, "EventsReceiver::FAIL");
                    Toast.makeText(context, R.string.fail_to_fetch,
                            Toast.LENGTH_SHORT).show();

                } else {
                    Log.d(TAG, "EventsReceiver::DONE");
                    mEndListReached = intent.getBooleanExtra(
                            SyncAdapter.EXTRA_END_OF_RESULTS_REACHED,
                            false);
                }

                mNextPage = intent.getStringExtra(SyncAdapter.EXTRA_NEXT_PAGE);

                selectAfterLoad();

                updateListViewFooter();
                setInProgress(false);
            }
        }
    }

    private boolean isRetrievingData(){
        return SyncUtil.isSyncing(getActivity());
    }
}
