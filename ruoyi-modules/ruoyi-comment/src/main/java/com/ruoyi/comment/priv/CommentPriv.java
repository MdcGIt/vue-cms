package com.ruoyi.comment.priv;

public interface CommentPriv {

	String PRIV_PREFIX = "comment:mgr:";

	public String VIEW = PRIV_PREFIX + "list";

	public String AUDIT = PRIV_PREFIX + "audit";

	public String DELETE = PRIV_PREFIX + "delete";
}
