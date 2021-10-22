package edu.uark.dlb018.bruhapi.user;

public class UserAttribute {
    private long UserId;
    private String AttributeName;
    private String AttributeValue;

    public UserAttribute(long userId, String attributeName, String attributeValue){
        UserId = userId;
        AttributeName = attributeName;
        AttributeValue = attributeValue;
    }

    public long getUserId() {
        return UserId;
    }

    public String getAttributeName() {
        return AttributeName;
    }

    public String getAttributeValue() {
        return AttributeValue;
    }
}
