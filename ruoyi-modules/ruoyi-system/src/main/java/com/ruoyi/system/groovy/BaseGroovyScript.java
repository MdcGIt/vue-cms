package com.ruoyi.system.groovy;

import java.io.PrintWriter;

import com.xxl.job.core.handler.IJobHandler;

public abstract class BaseGroovyScript extends IJobHandler {

	private PrintWriter writer;

	public void setPrintWriter(PrintWriter writer) {
		this.writer = writer;
	}
	
	@Override
	public void execute() throws Exception {
		try {
			this.run(writer);
		} catch (Exception e) {
			e.printStackTrace(writer);
		}
	}

	protected abstract void run(PrintWriter out) throws Exception;
}