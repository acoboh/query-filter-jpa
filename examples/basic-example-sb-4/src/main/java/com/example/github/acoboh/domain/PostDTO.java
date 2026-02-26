package com.example.github.acoboh.domain;

import com.example.github.acoboh.entities.PostBlog.PostType;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class PostDTO {

    private final String tsid;

    private final String title;

    private final String content;

    private final int likes;

    private final LocalDateTime createDate;

    private final Timestamp lastTimestamp;

    private final boolean published;

    private final PostType postType;

    public PostDTO(String tsid, String title, String content, int likes, LocalDateTime createDate,
            Timestamp lastTimestamp, boolean published, PostType postType) {
        this.tsid = tsid;
        this.title = title;
        this.content = content;
        this.likes = likes;
        this.createDate = createDate;
        this.lastTimestamp = lastTimestamp;
        this.published = published;
        this.postType = postType;
    }

    public String getTsid() {
        return tsid;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public int getLikes() {
        return likes;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public Timestamp getLastTimestamp() {
        return lastTimestamp;
    }

    public boolean isPublished() {
        return published;
    }

    public PostType getPostType() {
        return postType;
    }

}
