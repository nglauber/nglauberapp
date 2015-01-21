package ngvl.android.blogger.service;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncInfo;
import android.os.Build;
import android.os.Bundle;

import ngvl.android.blogger.R;
import ngvl.android.blogger.storage.RssReaderProvider;

public class SyncUtil {
    private static Account getAccount(Context context) {
        final String ACCOUNT_TYPE = context.getString(R.string.account_type);
        final String ACCOUNT = "nglauber";

        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        Account account;
        Account[] accounts = accountManager.getAccountsByType(ACCOUNT_TYPE);
        if (accounts != null && accounts.length > 0) {
            account = accounts[0];
        } else {
            account = new Account(ACCOUNT, ACCOUNT_TYPE);

            /*
            * Add the account and account type, no password or user data
            * If successful, return the Account object, otherwise report an error.
            */
            if (!accountManager.addAccountExplicitly(account, null, null)) {
                throw new IllegalArgumentException("Failed to add account.");
            }

        }
        return account;
    }

    public static void requestSync(Context context, String page) {
        Account account = getAccount(context);

        Bundle params = new Bundle();
        params.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        params.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        params.putString(SyncAdapter.EXTRA_PAGE, page);

        ContentResolver.requestSync(account, RssReaderProvider.AUTHORITY, params);
    }

    public static void setSyncAutomatically(Context context){
        Account account = getAccount(context);
        String authority = context.getString(R.string.authority);

        ContentResolver.setSyncAutomatically(account, authority, true);
    }

    public static boolean isSyncing(Context context){
        Account account = getAccount(context);
        String authority = context.getString(R.string.authority);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            for(SyncInfo syncInfo : ContentResolver.getCurrentSyncs())
            {
                if(syncInfo.account.equals(account) &&
                        syncInfo.authority.equals(authority))
                {
                    return true;
                }
            }
            return false;

        } else {
            SyncInfo currentSync = ContentResolver.getCurrentSync();
            return currentSync != null && currentSync.account.equals(account) &&
                    currentSync.authority.equals(authority);
        }
    }
}
