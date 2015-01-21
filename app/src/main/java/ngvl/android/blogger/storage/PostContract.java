package ngvl.android.blogger.storage;

import android.provider.BaseColumns;

public interface PostContract extends BaseColumns {
    String TABLE_NAME = "posts";
    String VIEW_NAME = TABLE_NAME + "_VIEW";
    String POST_ID = "postId";
    String TITLE = "title";
    String LINK = "link";
    String CONTENT = "content";
    String THUMBNAIL = "thumbail";
    String PUBLISHED = "published";
    String LAST_UPDATE = "lastUpdate";
    String FAVORITE = "favorite";
}
