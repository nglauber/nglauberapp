package ngvl.android.blogger.service;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ngvl.android.blogger.http.EntryResult;
import ngvl.android.blogger.http.ParserEntriesJSON;
import ngvl.android.blogger.model.Entry;
import ngvl.android.blogger.storage.PostContract;
import ngvl.android.blogger.storage.RssReaderProvider;

/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    public static final String ACTION_FETCH_COMPLETE = "fetchComplete";
    public static final String EXTRA_PAGE = "page";
    public static final String EXTRA_NEXT_PAGE = "nextPage";
    public static final String EXTRA_FAIL = "fail";
    public static final String EXTRA_END_OF_RESULTS_REACHED = "endOfResults";

    public static final String TAG = SyncAdapter.class.getSimpleName();

    private ContentResolver mContentResolver;

    /**
     * Set up the sync adapter
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
    }

    /*
     * Specify the code you want to run in the sync adapter. The entire
     * sync adapter runs in a background thread, so you don't have to set
     * up your own background processing.
     */
    @Override
    public void onPerformSync(
            Account account,
            Bundle extras,
            String authority,
            ContentProviderClient provider,
            SyncResult syncResult) {

        Log.d(TAG, "onPerformSync");
        sync(extras);
    }

    private void sync(Bundle extras) {
        Intent broadcastIntent = new Intent(ACTION_FETCH_COMPLETE);

        try {
            ArrayList<ContentProviderOperation> operations = new ArrayList<>();

            String page = extras.getString(EXTRA_PAGE);

            //EntryResult result = ParserEntries.retrieveEntries(page); // old API with XML
            EntryResult result = ParserEntriesJSON.retrieveEntries(page);

            List<Entry> entries = result.getEntries();

            // Delete all previous cache data
            if (entries != null && entries.size() > 0 && page == null){
                Log.d(TAG, "Deleting existing");
                ContentProviderOperation.Builder cpo = ContentProviderOperation
                        .newDelete(RssReaderProvider.CONTENT_URI)
                        .withSelection(PostContract.FAVORITE +" = 0", null);
                operations.add(cpo.build());
            }

            Log.d(TAG, "Saving entries on cache");
            // For each entry, save new data
            for (Entry entry : entries) {
                ContentValues values = entryToValues(entry);

                ContentProviderOperation.Builder cpo = ContentProviderOperation
                        .newInsert(RssReaderProvider.CONTENT_URI);
                cpo.withValues(values);
                operations.add(cpo.build());
            }

            mContentResolver.applyBatch(RssReaderProvider.AUTHORITY, operations);

            broadcastIntent.putExtra(EXTRA_PAGE, result.getCurrentPage());
            broadcastIntent.putExtra(EXTRA_NEXT_PAGE, result.getNextPage());
            if (result.getNextPage() == null){
                broadcastIntent.putExtra(EXTRA_END_OF_RESULTS_REACHED, true);
            }

        } catch (Exception e) {
            broadcastIntent.putExtra(EXTRA_FAIL, true);
            e.printStackTrace();
        }
        Log.d(TAG, "Sending broadcast.");
        getContext().sendBroadcast(broadcastIntent);
        Log.d(TAG, "DONE!");
    }

    private ContentValues entryToValues(Entry entry) {
        ContentValues values = new ContentValues();
        values.put(PostContract.POST_ID, entry.id);
        values.put(PostContract.TITLE, entry.title);
        values.put(PostContract.LINK, entry.link);
        values.put(PostContract.CONTENT, entry.content);
        values.put(PostContract.THUMBNAIL, entry.thumbnailURL);
        values.put(PostContract.PUBLISHED, entry.published);
        values.put(PostContract.LAST_UPDATE, entry.lastUpdate);
        return values;
    }
}