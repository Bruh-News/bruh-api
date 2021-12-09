package edu.uark.dlb018.bruhapi.post;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;

public class Post {
    private long UserId;
    private String PostText;
    private Timestamp DateTime;
    private long ParentId;
    private String Media;

    //minimal constructor
    @JsonCreator
    public Post(@JsonProperty("uid") long uid,
                @JsonProperty("postText") String postText,
                @JsonProperty("secondsSinceEpoch") long secondsSinceEpoch){
        UserId = uid;
        PostText = postText;
        DateTime = new Timestamp(1000 * secondsSinceEpoch);
        ParentId = 0;
        Media = null;
    }

    //constructor with parent and no media
    @JsonCreator
    public Post(@JsonProperty("uid") long uid,
                @JsonProperty("postText") String postText,
                @JsonProperty("secondsSinceEpoch") long secondsSinceEpoch,
                @JsonProperty("pid") long pid){
        UserId = uid;
        PostText = postText;
        DateTime = new Timestamp(1000 * secondsSinceEpoch);
        ParentId = pid;
        Media = null;
    }

    //constructor with media and no parent
    @JsonCreator
    public Post(@JsonProperty("uid") long uid,
                @JsonProperty("postText") String postText,
                @JsonProperty("secondsSinceEpoch") long secondsSinceEpoch,
                @JsonProperty("media") String media){
        UserId = uid;
        PostText = postText;
        DateTime = new Timestamp(1000 * secondsSinceEpoch);
        ParentId = 0;
        Media = media;
    }

    //constructor with parent and media
    @JsonCreator
    public Post(@JsonProperty("uid") long uid,
                @JsonProperty("postText") String postText,
                @JsonProperty("secondsSinceEpoch") long secondsSinceEpoch,
                @JsonProperty("pid") long pid,
                @JsonProperty("media") String media){
        UserId = uid;
        PostText = postText;
        DateTime = new Timestamp(1000 * secondsSinceEpoch);
        ParentId = pid;
        Media = media;
    }

    //getters
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

    public String getMedia() { return Media; }

    //setters
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

    public void setMedia(String media){ Media = media; }
}
