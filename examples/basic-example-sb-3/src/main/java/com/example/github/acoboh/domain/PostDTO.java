package com.example.github.acoboh.domain;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import com.example.github.acoboh.entities.PostBlog.PostType;

public class PostDTO {

	private String tsid;

	private String title;

	private String content;

	private int likes;

	private LocalDateTime createDate;

	private Timestamp lastTimestamp;

	private boolean published;

	private PostType postType;

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
