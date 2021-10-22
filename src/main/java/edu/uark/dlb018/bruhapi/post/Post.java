package edu.uark.dlb018.bruhapi.post;

import java.sql.Timestamp;

public class Post {
    private long UserId;
    private String PostText;
    private Timestamp DateTime;
    private long ParentId;

    public Post(long uid, String postText, long secondsSinceEpoch, long pid){
        UserId = uid;
        PostText = postText;
        DateTime = new Timestamp(1000 * secondsSinceEpoch);
        ParentId = pid;
    }

    public long getUserId() {
        return UserId;
    }

    public String getPostText() {
        return PostText;
    }

    public Timestamp getDateTime() {
        return DateTime;
    }

    public long getParentId() {
        return ParentId;
    }

    public void setUserId(long userId) {
        UserId = userId;
    }

    public void setPostText(String postText) {
        PostText = postText;
    }

    public void setDateTime(long secondsSinceEpoch) {
        DateTime = new Timestamp(1000 * secondsSinceEpoch);
    }

    public void setParentId(long parentId) {
        ParentId = parentId;
    }
}
