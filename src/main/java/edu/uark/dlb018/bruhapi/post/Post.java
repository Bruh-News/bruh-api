package edu.uark.dlb018.bruhapi.post;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;
import java.util.Base64;
import javax.persistence.Lob;

public class Post {
    private long UserId;
    private String PostText;
    private Timestamp DateTime;
    private long ParentId;

    @Lob
    private byte[] Media;

    public Post(long uid, String postText, long secondsSinceEpoch, long pid){
        UserId = uid;
        PostText = postText;
        DateTime = new Timestamp(1000 * secondsSinceEpoch);
        ParentId = pid;
        Media = null;
    }

    public Post(long uid, String postText, long secondsSinceEpoch, long pid, byte[] media){
        UserId = uid;
        PostText = postText;
        DateTime = new Timestamp(1000 * secondsSinceEpoch);
        ParentId = pid;
        Media = media;
    }

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
        Media = Base64.getDecoder().decode(media);
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

    public byte[] getMedia() { return Media; }

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

    public void setMedia(byte[] media){ Media = media; }
}
