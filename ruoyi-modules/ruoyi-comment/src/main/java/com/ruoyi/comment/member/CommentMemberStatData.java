package com.ruoyi.comment.member;

import com.ruoyi.member.core.IMemberStatData;
import org.springframework.stereotype.Component;

/**
 * 会员评论数统计
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMemberStatData.BEAN_PREFIX + CommentMemberStatData.TYPE)
public class CommentMemberStatData implements IMemberStatData {

    public static final String TYPE = "comment";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getField() {
        return "intValue3";
    }
}
