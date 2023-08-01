package com.ruoyi.comment.controller.front;

import com.ruoyi.comment.domain.vo.CommentVO;
import com.ruoyi.comment.service.ICommentApiService;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.security.web.BaseRestController;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class MemberCommentApiController extends BaseRestController {

    private final ICommentApiService commentApiService;

    @GetMapping("/api/comment/user/{uid}")
    public R<?> getMemberCommentList(@PathVariable Long uid,
                                     @RequestParam String type,
                                     @RequestParam(required = false, defaultValue = "10") @Min(1) Integer limit,
                                     @RequestParam(required = false, defaultValue = "0") @Min(0) Long offset) {
        List<CommentVO> comments = commentApiService.getCommentListByMember(type, uid, limit, offset, false);
        return R.ok(comments);
    }
}
