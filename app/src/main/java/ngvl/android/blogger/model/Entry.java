package ngvl.android.blogger.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Entry implements Parcelable {
	public final String id;
	public final String title;
	public final String content;
    public final String link;
    public final String thumbnailURL;
    public final String published;
    public final String lastUpdate;
	public boolean favorite;
    
	public Entry(String id, String title, String content, String link,
			String thumbnailURL, String published, String lastUpdate, boolean favorite) {
		this.id = id;
		this.title = title;
		this.content = content;
		this.link = link;
		this.thumbnailURL = thumbnailURL;
		this.published = published;
		this.lastUpdate = lastUpdate;
		this.favorite = favorite;
	}

	public Entry(Parcel p){
		this(	p.readString(), // id
				p.readString(), // title
				p.readString(), // content
				p.readString(), // link
				p.readString(), // thumbnail
				p.readString(), // published date
				p.readString(), // last update date
				p.readInt() == 1); // is it favorite?
	}
	
	public static final Parcelable.Creator<Entry>  
	   CREATOR = new Parcelable.Creator<Entry>() {  
	  
	   public Entry createFromParcel(Parcel in) {  
	     return new Entry(in);  
	   }  
	  
	   public Entry[] newArray(int size) {  
	     return new Entry[size];  
	   }  
	 };	
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(title);
		dest.writeString(content);
		dest.writeString(link);
		dest.writeString(thumbnailURL);
		dest.writeString(published);
		dest.writeString(lastUpdate);
		dest.writeInt(favorite ? 1 : 0);
	}
}
