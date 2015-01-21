package ngvl.android.blogger.ui;

import java.io.FileNotFoundException;
import java.util.List;

import ngvl.android.blogger.R;
import ngvl.android.blogger.http.HttpUtil;
import ngvl.android.blogger.http.ParserEntriesJSON;
import ngvl.android.blogger.model.Entry;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class PostsSearchFragment extends ListFragment {

    private static final String TAG = PostsSearchFragment.class.getSimpleName();
	private static final String EXTRA_TERM = "term";

	private List<Entry> entries;
	private PostsSearchTask task;
	private boolean mLastSearchFailed;
	private int mLastSelected;
	
	public static PostsSearchFragment newInstance(String term){
		Bundle args = new Bundle();
		args.putString(EXTRA_TERM, term);

		PostsSearchFragment f = new PostsSearchFragment();
		f.setArguments(args);
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_post_list, null);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (task == null){
			task = new PostsSearchTask();
			task.execute(getArguments().getString(EXTRA_TERM));
			
		} else if (task.getStatus() == AsyncTask.Status.FINISHED){
			setListAdapter(new PostsAdapter(getActivity(), entries));
			if (getResources().getBoolean(R.bool.tablet)){
				setSelectedPosition(mLastSelected);
			}
			setInProgress(false);
			
		} else if (task.getStatus() == AsyncTask.Status.RUNNING){
			setInProgress(true);
		}
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		setSelectedPosition(position);
	}

    // ******************************************************************************************
    // Private methods
    // ******************************************************************************************
	private void setSelectedPosition(int position){
		Log.w(TAG, "setSelectedPosition:"+ position);
		mLastSelected = position;
		((OnPostFoundSelectedListener)getActivity()).onPostSelected(entries.get(position));
	}
	
	private void setInProgress(boolean visible) {
		Log.w(TAG, "showProgress: " + visible);

		View progress = getView().findViewById(R.id.progressBarBig);
		TextView text = (TextView) getView().findViewById(R.id.emptyText);

		if (visible) {
			if (getListAdapter() == null || getListAdapter().isEmpty()) {
				progress.setVisibility(View.VISIBLE);
			}
			text.setVisibility(View.GONE);
			getListView().setEmptyView(null);

		} else {
			progress.setVisibility(View.GONE);
			text.setVisibility(View.VISIBLE);
			getListView().setEmptyView(text);

			if (!HttpUtil.hasConnectionAvailable(getActivity())) {
				Log.d(TAG, "No connection");
				text.setText(R.string.no_conneciton);

			} else if (mLastSearchFailed) {
				Log.d(TAG, "Last try failed");
				text.setText(R.string.fail_to_fetch);

			} else {
				Log.d(TAG, "No data");
				text.setText(R.string.no_items);
			}
		}
	}

    // ******************************************************************************************
    // Interface to notify when a post was selected in the list
    // ******************************************************************************************
    interface OnPostFoundSelectedListener {
        void onPostSelected(Entry entryId);
    }

    // ******************************************************************************************
    // AsyncTask to perform the search for posts
    // ******************************************************************************************
    class PostsSearchTask extends AsyncTask<String, Void, List<Entry>>{

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setInProgress(true);
		}
		
		@Override
		protected List<Entry> doInBackground(String... params) {
			try {
				return ParserEntriesJSON.searchForEntries(params[0]);
			} catch (FileNotFoundException e){
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
				mLastSearchFailed = true;
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(List<Entry> result) {
			super.onPostExecute(result);
			entries = result;
			if (entries != null){
				setListAdapter(new PostsAdapter(getActivity(), entries));
				mLastSelected = 0;
                if (getResources().getBoolean(R.bool.tablet)){
					setSelectedPosition(mLastSelected);
				}
			} 
			setInProgress(false);
		}
	}
}
