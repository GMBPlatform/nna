package Util;

import org.apache.logging.log4j.LogManager;

// use apache.log4j2 library

enum Log_Level{
	LEVEL_INFO, LEVEL_WARNING, LEVEL_ERROR, LEVEL_DEBUG, LEVEL_TRACE
}

public class Logger {

	private org.apache.logging.log4j.Logger logger = LogManager.getLogger(Logger.class.getName());
	
	public final int LOG_INFO = Log_Level.LEVEL_INFO.ordinal();
	public final int LOG_WARNING = Log_Level.LEVEL_WARNING.ordinal();
	public final int LOG_ERROR = Log_Level.LEVEL_ERROR.ordinal();
	public final int LOG_DEBUG = Log_Level.LEVEL_DEBUG.ordinal();
	public final int LOG_TRACE = Log_Level.LEVEL_TRACE.ordinal();
	
	public void OutConsole(int level, String msg) {
		StackTraceElement[] stacks = new Throwable().getStackTrace();
		StackTraceElement beforeStack = stacks[1];
		
		// branch according to Log_Level
		if(level == this.LOG_INFO) {
			this.logger.info("[{} ({} : {})] - {}", beforeStack.getClassName(), beforeStack.getMethodName(), beforeStack.getLineNumber(), msg);
		} else if(level == this.LOG_WARNING) {
			this.logger.warn("[{} ({} : {})] - {}", beforeStack.getClassName(), beforeStack.getMethodName(), beforeStack.getLineNumber(), msg);
		} else if(level == this.LOG_ERROR) {
			this.logger.error("[{} ({} : {})] - {}", beforeStack.getClassName(), beforeStack.getMethodName(), beforeStack.getLineNumber(), msg);
		} else if(level == this.LOG_DEBUG) {
			this.logger.debug("[{} ({} : {})] - {}", beforeStack.getClassName(), beforeStack.getMethodName(), beforeStack.getLineNumber(), msg);
		} else if(level == this.LOG_TRACE) {
			this.logger.trace("[{} ({} : {})] - {}", beforeStack.getClassName(), beforeStack.getMethodName(), beforeStack.getLineNumber(), msg);
		} else {
			this.logger.warn("[{} ({} : {})] - {}", "This Case is not a LogType");
		}
		stacks = null;
		beforeStack = null;
	}
	
}
