package com.example.petheart.models;

import java.util.Date;
import java.util.UUID;

public class Memory {
    private UUID mId;
    private String mTitle;
    private Date mDate;
    private boolean mFavorited;
    private String mDescription;

    public Memory()
    {
        this(UUID.randomUUID());
    }

    public Memory(UUID id)
    {
        mId = id;
        mDate = new Date();
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public boolean isFavorited() {
        return mFavorited;
    }

    public void setFavorited(boolean favorited) {
        mFavorited = favorited;
    }

    public UUID getId() {
        return mId;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getPhotoFilename()
    {
        return "IMG_" + getId().toString() + ".jpg";
    }
}
