package ngvl.android.blogger.storage;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class RssReaderProvider extends ContentProvider {

    // this authority must be the same declared in AndroidManifest.xml
	public static final String AUTHORITY = "ngvl.android.blogger.provider";

	private static final int ALL_ENTRIES = 10;
	private static final int ENTRY_ID = 20;
	private static final int ALL_2 = 30;

	private static final String BASE_PATH = "posts";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
	
	private static final UriMatcher sUriMatcher;

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, BASE_PATH, ALL_ENTRIES);
		sUriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", ENTRY_ID);
		sUriMatcher.addURI(AUTHORITY, BASE_PATH + "/all", ALL_2);
	}

	private DBHelper mOpenHelper;

	@Override
	public boolean onCreate() {
		mOpenHelper = new DBHelper(getContext());
		return false;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sUriMatcher.match(uri);
		SQLiteDatabase sqlDB = mOpenHelper.getWritableDatabase();
		long id = 0;
		switch (uriType) {
		case ALL_ENTRIES:
			String postId = String.valueOf(values.get(PostContract.POST_ID));
			String whereClause = PostContract.POST_ID +" = ?";
			String[] whereValue = new String[]{ postId };

			Cursor c = sqlDB.query(
                    PostContract.TABLE_NAME, new String[]{PostContract._ID},
					whereClause,
					whereValue, null, null, null);

			if (c != null && c.moveToNext()){
				c.moveToFirst();

				id = c.getInt(c.getColumnIndex(PostContract._ID));

				sqlDB.update(PostContract.TABLE_NAME, values, whereClause, whereValue);

			} else {
				id = sqlDB.insert(PostContract.TABLE_NAME, null, values);
			}
			break;

		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(BASE_PATH + "/" + id);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {

		int uriType = sUriMatcher.match(uri);
		SQLiteDatabase sqlDB = mOpenHelper.getWritableDatabase();
		int rowsUpdated;

		switch (uriType) {
		case ALL_ENTRIES:
			rowsUpdated = sqlDB.update(PostContract.TABLE_NAME, values, selection,
					selectionArgs);
			break;
		case ENTRY_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = sqlDB.update(PostContract.TABLE_NAME, values,
                        PostContract._ID + "=" + id, null);
			} else {
				rowsUpdated = sqlDB.update(PostContract.TABLE_NAME, values,
                        PostContract._ID + "=" + id + " and " + selection,
						selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sUriMatcher.match(uri);
		SQLiteDatabase sqlDB = mOpenHelper.getWritableDatabase();
		int rowsDeleted = 0;
		switch (uriType) {
		case ALL_ENTRIES:
			rowsDeleted = sqlDB.delete(PostContract.TABLE_NAME, selection,
					selectionArgs);
			break;
		case ENTRY_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = sqlDB.delete(PostContract.TABLE_NAME,
                        PostContract._ID + "=" + id, null);
			} else {
				rowsDeleted = sqlDB.delete(PostContract.TABLE_NAME,
                        PostContract._ID + "=" + id + " and " + selection,
						selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		// Check if the caller has requested a column which does not exists
		checkColumns(projection);

		// Set the table
		queryBuilder.setTables(PostContract.TABLE_NAME);

		int uriType = sUriMatcher.match(uri);
		Cursor cursor = null;
		SQLiteDatabase db;

		switch (uriType) {
		case ALL_ENTRIES:
			db = mOpenHelper.getWritableDatabase();
			cursor = queryBuilder.query(db, projection, selection,
					selectionArgs, null, null, sortOrder);			
			break;

		case ALL_2:
			db = mOpenHelper.getWritableDatabase();
			cursor = db.rawQuery(
					"select * from "+ PostContract.TABLE_NAME +
					" where "+ PostContract.PUBLISHED +" >= "+ DBHelper.SUB_SELECT_MIN_PUBLISHED +
					" order by "+ PostContract.PUBLISHED +" DESC", null);
			break;
			
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		// Make sure that potential listeners are getting notified
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	private void checkColumns(String[] projection) {

		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(
					Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(
					Arrays.asList(DBHelper.ALL_COLUMNS));

			// Check if all columns which are requested are available
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException(
						"Unknown columns in projection");
			}
		}
	}
}
