package com.team4.caucapstone.labeltong;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TagList {
    @SerializedName("ID")
    @Expose
    private String ID;
    @SerializedName("CreatedAt")
    @Expose
    private String CreatedAt;
    @SerializedName("UpdatedAt")
    @Expose
    private String UpdatedAt;
    @SerializedName("DeletedAt")
    @Expose
    private String DeletedAt;
    @SerializedName("TagId")
    @Expose
    private String TagId;
    @SerializedName("TagName")
    @Expose
    private String TagName;
    @SerializedName("TagDescription")
    @Expose
    private String TagDescription;
    @SerializedName("TagThumbnail")
    @Expose
    private String TagThumbnail;

    public String getCreatedAt() {
        return CreatedAt;
    }

    public String getDeletedAt() {
        return DeletedAt;
    }

    public String getID() {
        return ID;
    }

    public String getTagDescription() {
        return TagDescription;
    }

    public String getTagId() {
        return TagId;
    }

    public String getTagName() {
        return TagName;
    }

    public String getTagThumbnail() {
        return TagThumbnail;
    }

    public String getUpdatedAt() {
        return UpdatedAt;
    }

    public void setCreatedAt(String createdAt) {
        CreatedAt = createdAt;
    }

    public void setDeletedAt(String deletedAt) {
        DeletedAt = deletedAt;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public void setTagDescription(String tagDescription) {
        TagDescription = tagDescription;
    }

    public void setTagId(String tagId) {
        TagId = tagId;
    }

    public void setTagName(String tagName) {
        TagName = tagName;
    }

    public void setTagThumbnail(String tagThumbnail) {
        TagThumbnail = tagThumbnail;
    }
    public void setUpdatedAt(String updatedAt) {
        UpdatedAt = updatedAt;
    }

}
