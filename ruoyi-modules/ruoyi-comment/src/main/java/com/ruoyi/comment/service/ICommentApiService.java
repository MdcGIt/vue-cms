package com.ruoyi.comment.service;

import java.util.List;

import com.ruoyi.comment.domain.Comment;
import com.ruoyi.comment.domain.dto.SubmitCommentDTO;

public interface ICommentApiService {

	/**
	 * 提交评论
	 * 
	 * @param dto
	 */
	void submitComment(SubmitCommentDTO dto);

	/**
	 * 获取评论列表
	 * 
	 * @param type
	 * @param dataId
	 * @param limit
	 * @param offset
	 * @return
	 */
	List<Comment> getCommentList(String type, Long dataId, Integer limit, Long offset);

	/**
	 * 获取评论回复列表
	 * 
	 * @param commentId
	 * @param limit
	 * @param offset
	 * @return
	 */
	List<Comment> getCommentReplyList(Long commentId, Integer limit, Long offset);

	/**
	 * 删除指定用户评论
	 * 
	 * @param userId
	 * @param commentId
	 */
	void deleteUserComment(Long userId, Long commentId);

	/**
	 * 评论点赞
	 * 
	 * @param commentId
	 * @param uid
	 */
	void likeComment(Long commentId, long uid);

	/**
	 * 取消评论点赞
	 * 
	 * @param comment
	 * @param uid
	 */
	void cancelLikeComment(Long comment, long uid);
}
