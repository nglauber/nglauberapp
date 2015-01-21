package ngvl.android.blogger.http;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import ngvl.android.blogger.model.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


public class ParserEntriesJSON {

    public static final String TAG = ParserEntriesJSON.class.getSimpleName();

	private static final String KEY_TITLE = "title";
	private static final String KEY_NEXT_PAGE_TOKEN = "nextPageToken";
	private static final String KEY_ITEMS = "items";
	private static final String KEY_ID = "id";
	private static final String KEY_CONTENT = "content";
	private static final String KEY_PUBLISHED = "published";
	private static final String KEY_LINK = "url";
	private static final String KEY_THUMBNAIL = "images";
	private static final String KEY_LAST_UPDATE = "updated";
	private static final String KEY_THUMBNAIL_URL = "url";
	
	private static final String API_KEY = "AIzaSyDzVi2wzBtirTOv3iv2OxI42nzoITklYYM";
	private static final String BLOG_ID = "3344721733578072661";
	private static final String BASE_URL = "https://www.googleapis.com/blogger/v3/blogs/"+ BLOG_ID;  
	private static String FETCH_URL = BASE_URL +"/posts?key="+ API_KEY +"&maxResults=20&fetchImages=true";
	private static String SEARCH_URL = BASE_URL +"/posts/search?key="+ API_KEY +"&q=";
	
	public static EntryResult retrieveEntries(String page) throws Exception{
		String url = FETCH_URL;
		if (page != null){
			url += "&pageToken="+ page;
		}
		Log.d(TAG, "RequestURL: "+ url);
		return entryResultFromUrl(url, page);
	}

	public static List<Entry> searchForEntries(String term) throws Exception{
		String url = SEARCH_URL + URLEncoder.encode(term, "UTF-8");
		
		return entryResultFromUrl(url, null).getEntries();
	}

	private static EntryResult entryResultFromUrl(String url, String page) throws Exception{
		
		List<Entry> entries = new ArrayList<Entry>();
		String nextPage = null;
		
		InputStream is = HttpUtil.downloadUrl(url);

		String json = HttpUtil.streamToString(is);
		JSONObject jsonObject = new JSONObject(json);
		if (!jsonObject.isNull(KEY_ITEMS)){
			JSONArray jsonEntries = jsonObject.getJSONArray(KEY_ITEMS);
			
			
			for (int i = 0; i < jsonEntries.length(); i++){
				JSONObject jsonEntry = jsonEntries.getJSONObject(i);
				
				entries.add(entryFromJSON(jsonEntry));
			}
			
			if (!jsonObject.isNull(KEY_NEXT_PAGE_TOKEN)){
				nextPage = jsonObject.getString(KEY_NEXT_PAGE_TOKEN);
			}
		}
		EntryResult result = new EntryResult(entries, page, nextPage);
		return result;		
	}
	
	private static Entry entryFromJSON(JSONObject jsonEntry) throws JSONException {
		String id         = jsonEntry.getString(KEY_ID);
		String title      = jsonEntry.getString(KEY_TITLE);
		String content    = jsonEntry.getString(KEY_CONTENT);
		String link       = jsonEntry.getString(KEY_LINK); 
		String thumbnail  = null;
		if (!jsonEntry.isNull(KEY_THUMBNAIL)){
			thumbnail  = jsonEntry.getJSONArray(KEY_THUMBNAIL).getJSONObject(0).getString(KEY_THUMBNAIL_URL); 
		}
		String published  = jsonEntry.getString(KEY_PUBLISHED); 
		String lastUpdate = jsonEntry.getString(KEY_LAST_UPDATE); 
		
		return new Entry(id, title, content, link, thumbnail, published, lastUpdate, false);
	}
}
