package ngvl.android.blogger.ui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ngvl.android.blogger.R;
import ngvl.android.blogger.model.Entry;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

public class PostsAdapter extends ArrayAdapter<Entry> {

	public PostsAdapter(Context context, List<Entry> entries) {
		super(context, R.layout.post_item, entries);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		Entry entry = getItem(position);
		
		ViewHolder holder;
		if (convertView == null){
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.post_item, null);
			
			holder = new ViewHolder();
			holder.imgThumbnail = (ImageView)convertView.findViewById(R.id.imgThumbnail);
	        holder.txtTitle = (TextView)convertView.findViewById(R.id.txtTitle);		
	        holder.txtLastUpdate = (TextView)convertView.findViewById(R.id.txtLastUpdate);
	        
	        convertView.setTag(holder);
		} else {
			holder = (ViewHolder)convertView.getTag();
		}
        
        String imgUrl = entry.thumbnailURL;
        holder.imgThumbnail.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(imgUrl)){
        	ImageLoader.getInstance().displayImage(imgUrl, holder.imgThumbnail);
        }
        holder.txtTitle.setText(entry.title);
        
        try{
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        	Date date = sdf.parse(entry.lastUpdate);
        	
        	String lastUpdate = getContext().getString(R.string.last_update, SimpleDateFormat.getDateTimeInstance().format(date)); 
        	
        	holder.txtLastUpdate.setText(lastUpdate);
        } catch (Exception e){}		
		
		return convertView;
	}
	
	static class ViewHolder {
		ImageView imgThumbnail;
		TextView txtTitle;
		TextView txtLastUpdate;
	}
}
