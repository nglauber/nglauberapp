package ngvl.android.blogger.ui;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ngvl.android.blogger.R;
import ngvl.android.blogger.storage.PostContract;

public class PostsCursorAdapter extends SimpleCursorAdapter {

	private static final int LAYOUT = R.layout.post_item;
	private static String columns[] = new String[] {
            PostContract._ID,
            PostContract.TITLE,
            PostContract.THUMBNAIL,
            PostContract.LAST_UPDATE,
            PostContract.FAVORITE,
	};
	
	public PostsCursorAdapter(Context context, Cursor cursor) {
		super(context, LAYOUT, cursor, columns, null, 0);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ImageView imgThumbnail = (ImageView)view.findViewById(R.id.imgThumbnail);
		ImageView imgFavorite = (ImageView)view.findViewById(R.id.imgFavorite);
        TextView txtTitle = (TextView)view.findViewById(R.id.txtTitle);		
        TextView txtLastUpdate = (TextView)view.findViewById(R.id.txtLastUpdate);
        
        String imgUrl = cursor.getString(cursor.getColumnIndex(columns[2]));
        imgThumbnail.setImageResource(R.drawable.ic_launcher);
        if (!TextUtils.isEmpty(imgUrl)){
        	ImageLoader.getInstance().displayImage(imgUrl, imgThumbnail);
        }
        boolean favorite = cursor.getInt(cursor.getColumnIndex(columns[4])) == 1;
        imgFavorite.setVisibility(favorite ? View.VISIBLE : View.GONE);
        txtTitle.setText(cursor.getString(cursor.getColumnIndex(columns[1])));
        
        
        try{
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        	Date date = sdf.parse(cursor.getString(cursor.getColumnIndex(columns[3])));
        	
        	String lastUpdate = context.getString(R.string.last_update, SimpleDateFormat.getDateTimeInstance().format(date)); 
        	
        	txtLastUpdate.setText(lastUpdate);
        	
        } catch (Exception e){
            e.printStackTrace();
        }
	}	
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
		return LayoutInflater.from(context).inflate(LAYOUT, null);
	}
}
