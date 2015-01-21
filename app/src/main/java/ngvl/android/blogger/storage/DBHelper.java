package ngvl.android.blogger.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import static ngvl.android.blogger.storage.PostContract.*;

public class DBHelper extends SQLiteOpenHelper {

	public static final String SUB_SELECT_MIN_PUBLISHED =
			"(SELECT MIN(v."+ PUBLISHED +") FROM "+ VIEW_NAME +" v)";
	
	private static final String SCRIPT_CREATE_TABLE = 
			"CREATE TABLE IF NOT EXISTS "+ TABLE_NAME +" ("+
                    _ID +" integer primary key autoincrement, " +
                    POST_ID +" TEXT NOT NULL, " +
                    TITLE +" TEXT NOT NULL, " +
                    LINK +" TEXT NOT NULL, " +
                    CONTENT +" TEXT NOT NULL, " +
                    PUBLISHED +" TEXT NOT NULL, " +
                    LAST_UPDATE +" TEXT NOT NULL, " +
                    THUMBNAIL +" TEXT, "+
                    FAVORITE +" INTEGER DEFAULT 0)";
	
	private static final String SCRIPT_CREATE_VIEW =
			"CREATE VIEW IF NOT EXISTS "+ VIEW_NAME +" AS SELECT * FROM "+ TABLE_NAME +" WHERE "+ FAVORITE +" = 0";
	
	private static final String SCRIPT_CREATE_INDEX = 
			"CREATE UNIQUE INDEX idx_"+ TABLE_NAME +"_"+ POST_ID +
			" ON "+ TABLE_NAME +"("+ POST_ID +")";
			
	
	public static final String[] ALL_COLUMNS = {
            _ID,
            POST_ID,
            TITLE,
            CONTENT,
            THUMBNAIL,
            PUBLISHED,
            LAST_UPDATE,
            FAVORITE};
	
	private static final String DATABASE_NAME = "rssDb";
	private static final int DATABASE_VERSION = 3;
	
	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SCRIPT_CREATE_TABLE);
		db.execSQL(SCRIPT_CREATE_VIEW);
		db.execSQL(SCRIPT_CREATE_INDEX);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion <= 2){
			db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME);
			db.execSQL("DROP VIEW IF EXISTS "+ VIEW_NAME);
			onCreate(db);
		}
	}
}
