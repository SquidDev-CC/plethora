package org.squiddev.plethora.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;

/**
 * Facade to the basic logger, which also forces debug calls
 * if {@link #DEBUG} is enabled
 */
public class DebugLogger {
	private static final Logger logger = LogManager.getLogger("Plethora");
	public static final boolean DEBUG = true;

	public static void trace(String message, int max) {
		try {
			throw new RuntimeException();
		} catch (RuntimeException e) {
			debug(message + "\n\tat " + StringUtils.join(e.getStackTrace(), "\n\tat ", 1, max + 1));
		}
	}

	public static void trace(String message) {
		try {
			throw new RuntimeException();
		} catch (RuntimeException e) {
			debug(message + "\n\tat " + StringUtils.join(e.getStackTrace(), "\n\tat "));
		}
	}

	public static void trace(String message, Object... params) {
		try {
			throw new RuntimeException();
		} catch (RuntimeException e) {
			debug(String.format(message, params) + "\n\tat " + StringUtils.join(e.getStackTrace(), "\n\tat "));
		}
	}

	public static void debug(Marker marker, String message) {
		if (DEBUG) {
			logger.info(marker, message);
		}
	}

	public static void debug(Marker marker, String message, Object... params) {
		if (DEBUG) {
			logger.info(marker, String.format(message, params));
		}
	}

	public static void debug(Marker marker, String message, Throwable t) {
		if (DEBUG) {
			logger.info(marker, message, t);
		}
	}

	public static void debug(String message) {
		if (DEBUG) {
			logger.info(message);
		} else {
			logger.debug(message);
		}
	}

	public static void debug(String message, Object... params) {
		if (DEBUG) {
			logger.info(String.format(message, params));
		} else {
			logger.debug(String.format(message, params));
		}
	}

	public static void debug(String message, Throwable t) {
		if (DEBUG) {
			logger.info(message, t);
		} else {
			logger.debug(message, t);
		}
	}

	public static void error(Marker marker, String message) {
		logger.error(marker, message);
	}

	public static void error(Marker marker, String message, Object... params) {
		logger.error(marker, String.format(message, params));
	}

	public static void error(Marker marker, String message, Throwable t) {
		logger.error(marker, message, t);
	}

	public static void error(String message) {
		logger.error(message);
	}

	public static void error(String message, Object... params) {
		logger.error(String.format(message, params));
	}

	public static void error(String message, Throwable t) {
		logger.error(message, t);
	}

	public static void info(Marker marker, String message) {
		logger.info(marker, message);
	}

	public static void info(Marker marker, String message, Object... params) {
		logger.info(marker, String.format(message, params));
	}

	public static void info(Marker marker, String message, Throwable t) {
		logger.info(marker, message, t);
	}

	public static void info(String message) {
		logger.info(message);
	}

	public static void info(String message, Object... params) {
		logger.info(String.format(message, params));
	}

	public static void info(String message, Throwable t) {
		logger.info(message, t);
	}

	public static void warn(Marker marker, String message) {
		logger.warn(marker, message);
	}

	public static void warn(Marker marker, String message, Object... params) {
		logger.warn(marker, String.format(message, params));
	}

	public static void warn(Marker marker, String message, Throwable t) {
		logger.warn(marker, message, t);
	}

	public static void warn(String message) {
		logger.warn(message);
	}

	public static void warn(String message, Object... params) {
		logger.warn(String.format(message, params));
	}

	public static void warn(String message, Throwable t) {
		logger.warn(message, t);
	}

	private static final String SEPARATOR = StringUtils.repeat('#', 100);

	public static void beginMajor(Level level) {
		logger.log(level, SEPARATOR);
		logger.log(level, "");
	}

	public static void endMajor(Level level) {
		logger.log(level, "");
		logger.log(level, SEPARATOR);
	}

	public static void major(Level level, String... message) {
		beginMajor(level);
		for (String line : message) {
			logger.log(level, line);
		}
		endMajor(level);
	}
}
