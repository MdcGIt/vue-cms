package com.ruoyi.comment.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.comment.domain.CommentLike;
import com.ruoyi.comment.mapper.CommentLikeMapper;
import com.ruoyi.comment.service.ICommentLikeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentLikeServiceImpl extends ServiceImpl<CommentLikeMapper, CommentLike> implements ICommentLikeService {
}