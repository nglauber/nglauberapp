package ngvl.android.blogger.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SlidingPaneLayout.PanelSlideListener;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import ngvl.android.blogger.R;
import ngvl.android.blogger.http.HttpUtil;
import ngvl.android.blogger.model.Entry;
import ngvl.android.blogger.storage.PostContract;
import ngvl.android.blogger.storage.RssReaderProvider;

public class PostDetailFragment extends Fragment implements PanelSlideListener {

    public static final String TAG = PostDetailFragment.class.getSimpleName();

	private static final String EXTRA_ENTRY = "entry";

	private ShareActionProvider mShareActionProvider;
	private WebView mWebView;
	private Entry mEntry;
	private Toast mToast;
	
	public static PostDetailFragment newInstance(Entry entry) {
		Log.d(TAG, "newInstance");
		PostDetailFragment fragment = new PostDetailFragment();
		fragment.mEntry = entry;

		return fragment;
	}
	
	public static Entry loadEntryFromId(Activity activity, long entryId) {
		Log.d(TAG, "loadEntryFromId");

		Entry entry = null;
		String id = String.valueOf(entryId);
		Cursor cursor = activity.getContentResolver().query(
				RssReaderProvider.CONTENT_URI, null,
				PostContract._ID + " = ?", new String[] { id }, null);
		if (cursor.moveToNext()) {
			entry = new Entry(cursor.getString(cursor
					.getColumnIndex(PostContract.POST_ID)),
					cursor.getString(cursor
							.getColumnIndex(PostContract.TITLE)),
					cursor.getString(cursor
							.getColumnIndex(PostContract.CONTENT)),
					cursor.getString(cursor
							.getColumnIndex(PostContract.LINK)),
					cursor.getString(cursor
							.getColumnIndex(PostContract.THUMBNAIL)),
					cursor.getString(cursor
							.getColumnIndex(PostContract.PUBLISHED)),
					cursor.getString(cursor
							.getColumnIndex(PostContract.LAST_UPDATE)),
					cursor.getInt(cursor
							.getColumnIndex(PostContract.FAVORITE)) == 1);
		}
		return entry;
	}	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			final Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		setHasOptionsMenu(true);
		mToast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);
		
		if (savedInstanceState != null) {
			mEntry = (Entry) savedInstanceState.getParcelable(EXTRA_ENTRY);
		}

		View v = inflater.inflate(R.layout.fragment_post_detail, null);

		if (mEntry != null) {
			mWebView = (WebView) v.findViewById(R.id.webView1);
			mWebView.setSaveEnabled(true);
			mWebView.setWebChromeClient(new WebChromeClient() {});
			mWebView.getSettings().setJavaScriptEnabled(true);
			mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
			mWebView.loadDataWithBaseURL(
					"file:///android_asset/", 
					HttpUtil.formatHtml(getActivity(), mEntry.title, mEntry.content),
					"text/html", "UTF-8", null);
		}
		return v;
	}
	
	@Override
	public void onResume() {
        super.onResume();
        try {
            WebView.class.getMethod("onResume").invoke(mWebView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
	public void onPause() {
        super.onPause();
        try {
            WebView.class.getMethod("onPause").invoke(mWebView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }	
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.d(TAG, "onSaveInstanceState");
		outState.putParcelable(EXTRA_ENTRY, mEntry);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_post_detail, menu);

		// Locate MenuItem with ShareActionProvider
		MenuItem shareItem = menu.findItem(R.id.menu_share);

		if (mEntry == null)
			return;

		// Fetch and store ShareActionProvider
		mShareActionProvider = (ShareActionProvider) MenuItemCompat
				.getActionProvider(shareItem);
		mShareActionProvider.setShareIntent(createShareIntent());

        menu.findItem(R.id.menu_favorite).setIcon(mEntry.favorite ?
                R.drawable.ic_action_favorite :
                R.drawable.ic_action_unfavorite);
	}

    public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_favorite){

            ContentValues values = new ContentValues();
            values.put(PostContract.POST_ID, mEntry.id);
            values.put(PostContract.TITLE, mEntry.title);
            values.put(PostContract.LINK, mEntry.link);
            values.put(PostContract.CONTENT, mEntry.content);
            values.put(PostContract.THUMBNAIL, mEntry.thumbnailURL);
            values.put(PostContract.PUBLISHED, mEntry.published);
            values.put(PostContract.LAST_UPDATE, mEntry.lastUpdate);
            values.put(PostContract.FAVORITE, mEntry.favorite ? 0 : 1);
			Uri rowInserted = getActivity().getContentResolver().insert(
                    RssReaderProvider.CONTENT_URI,
                    values);
			
			mToast.cancel();
			if (rowInserted == null){
				mToast.setText(R.string.failed_add_to_favorites);
				mToast.show();
			} else {
				mEntry.favorite = !mEntry.favorite;
				mToast.setText(mEntry.favorite ? R.string.added_to_favorites : R.string.removed_from_favorites);
				mToast.show();
			}

            item.setIcon(mEntry.favorite ?
                    R.drawable.ic_action_favorite :
                    R.drawable.ic_action_unfavorite);
            return true;

		} else if (item.getItemId() == R.id.menu_browser){
			Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse(mEntry.link));
			startActivity(it);
            return true;
		}
		return false;
	}
	
	// ******************************************************************************************
	// PanelSlideListener methods
	// ******************************************************************************************
	@Override
	public void onPanelClosed(View panel) {
		if (mWebView != null)
			mWebView.requestLayout();
	}

	@Override
	public void onPanelOpened(View panel) {
		if (mWebView != null)
			mWebView.requestLayout();
	}

	@Override
	public void onPanelSlide(View panel, float slideOffset) {
	}
	
	// ******************************************************************************************
	// Private methods
	// ******************************************************************************************
	private Intent createShareIntent() {
		Intent intent = new Intent(android.content.Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

		// Add data to the intent, the receiving app will decide what to do with
		// it.
		intent.putExtra(Intent.EXTRA_SUBJECT, mEntry.title);
		intent.putExtra(Intent.EXTRA_TEXT, "\"" + mEntry.title + "\" "
				+ mEntry.link + " via @nglauber");
		return intent;
	}
}
