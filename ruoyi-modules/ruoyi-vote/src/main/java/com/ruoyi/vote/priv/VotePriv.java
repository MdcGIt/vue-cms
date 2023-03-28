package com.ruoyi.vote.priv;

public interface VotePriv {

	String PRIV_PREFIX = "vote:mgr:";

	public String VIEW = PRIV_PREFIX + "list";

	public String ADD = PRIV_PREFIX + "add";

	public String EDIT = PRIV_PREFIX + "edit";

	public String DELETE = PRIV_PREFIX + "delete";
}
