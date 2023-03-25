package com.ruoyi.comment.listener.event;

import org.springframework.context.ApplicationEvent;

import com.ruoyi.comment.domain.Comment;

import lombok.Getter;

@Getter
public class AfterCommentSubmitEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	private Comment comment;

	public AfterCommentSubmitEvent(Object source, Comment comment) {
		super(source);
		this.comment = comment;
	}
}
