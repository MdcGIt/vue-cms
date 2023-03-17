package com.ruoyi.common.utils.file;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.util.HtmlUtils;

import com.ruoyi.common.utils.HttpUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 文件处理工具类
 */

public class FileExUtils {

	private static Map<String, String> EXT_MAP = new HashMap<>();

	static {
		EXT_MAP.put("FFD8FF", "jpg");
		EXT_MAP.put("89504E47", "png");
		EXT_MAP.put("47494638", "gif");
		EXT_MAP.put("49492a00227105008037", "tif");
		EXT_MAP.put("424D", "bmp");
		EXT_MAP.put("41433130", "dwg");
		EXT_MAP.put("38425053", "psd");
		EXT_MAP.put("7b5c727466315c616e73", "rtf");
		EXT_MAP.put("3c3f786d6c2076657273", "xml");
		EXT_MAP.put("3c21444f435459504520", "html");
		EXT_MAP.put("3c68746d6c3e0", "html");
		EXT_MAP.put("3c21646f637479706520", "htm");
		EXT_MAP.put("48544d4c207b0d0a0942", "css");
		EXT_MAP.put("696b2e71623d696b2e71", "js");
		EXT_MAP.put("44656C69766572792D646174653A", "eml");
		EXT_MAP.put("CFAD12FEC5FD746F", "dbx");
		EXT_MAP.put("d0cf11e0a1b11ae10000", "vsd");
		EXT_MAP.put("2142444E", "pst");
		EXT_MAP.put("5374616E64617264204A", "mdb");
		EXT_MAP.put("FF575043", "wpd");
		EXT_MAP.put("255044462D312E", "pdf");
		EXT_MAP.put("AC9EBD8F  ", "qdf");
		EXT_MAP.put("E3828596  ", "pwl");
		EXT_MAP.put("504b0304140000000800  ", "zip");
		EXT_MAP.put("526172211a0700cf9073  ", "rar");
		EXT_MAP.put("235468697320636f6e66  ", "ini");
		EXT_MAP.put("504b03040a0000000000  ", "jar");
		EXT_MAP.put("1f8b0800000000000000  ", "gz");
		EXT_MAP.put("52494646e27807005741  ", "wav");
		EXT_MAP.put("52494646d07d60074156  ", "avi");
		EXT_MAP.put("2e524d46000000120001  ", "rmvb");
		EXT_MAP.put("464c5601050000000900  ", "flv");
		EXT_MAP.put("00000020667479706d70  ", "mp4");
		EXT_MAP.put("49443303000000002176  ", "mp3");
		EXT_MAP.put("000001ba210001000180  ", "mpg");
		EXT_MAP.put("3026b2758e66cf11a6d9  ", "wmv");
		EXT_MAP.put("2E7261FD  ", "ram");
		EXT_MAP.put("000001B3  ", "mpg");
		EXT_MAP.put("6D6F6F76  ", "mov");
		EXT_MAP.put("6D6F6F76  ", "mov");
		EXT_MAP.put("3026B2758E66CF11  ", "asf");
		EXT_MAP.put("4d546864000000060001  ", "mid");
		EXT_MAP.put("4d5a9000030000000400  ", "exe");
		EXT_MAP.put("3c25402070616765206c  ", "mf");
		EXT_MAP.put("4d616e69666573742d56  ", "mov");
		EXT_MAP.put("494e5345525420494e54", "sql");
		EXT_MAP.put("7061636b616765207765  ", "java");
		EXT_MAP.put("406563686f206f66660d  ", "bat");
		EXT_MAP.put("7061636b616765207765  ", "java");
		EXT_MAP.put("cafebabe0000002e0041  ", "class");
		EXT_MAP.put("6c6f67346a2e726f6f74  ", "properties");
		EXT_MAP.put("49545346030000006000  ", "chm");
		EXT_MAP.put("04000000010000001300  ", "mxp");
		EXT_MAP.put("504b0304140006000800  ", "docx");
		EXT_MAP.put("d0cf11e0a1b11ae10000  ", "wps");
		EXT_MAP.put("6431303a637265617465  ", "torrent");
	}

	/**
	 * 从字节流获取文件类型后缀名
	 * 
	 * @param bytes
	 * @return
	 */
	public static String getExtension(byte[] bytes) {
		if (bytes.length > 20) {
			byte[] first20 = Arrays.copyOf(bytes, 20);
			String hexStr = Hex.encodeHexString(first20, false);
			for (Iterator<Entry<String, String>> iterator = EXT_MAP.entrySet().iterator(); iterator.hasNext();) {
				Entry<String, String> e = iterator.next();
				if (hexStr.startsWith(e.getKey())) {
					return e.getValue();
				}
			}
		}
		return StringUtils.EMPTY;
	}

	/**
	 * 读取指定字符串的后缀名
	 * 
	 * @param path
	 * @return
	 */
	public static String getExtension(String path) {
		if (path.indexOf("://") > -1) {
			path = HtmlUtils.htmlUnescape(path);
			String queryString = StringUtils.substringAfter(path, "?");
			if (queryString.indexOf("wx_fmt=") > -1) {
				// 微信图片路径处理
				String ext = StringUtils.substringAfter(queryString, "wx_fmt=");
				if (ext.indexOf("&") > -1) {
					ext = StringUtils.substringBefore(ext, "&");
				}
				if (StringUtils.isNotEmpty(ext)) {
					return ext;
				}
			}
		}
		if (path.indexOf("?") > -1) {
			path = StringUtils.substringBefore(path, "?");
		}
		return FilenameUtils.getExtension(path);
	}
	
	/**
	 * 从url获取图片后缀名
	 * 
	 * @param url
	 * @return
	 */
	public static String getImageSuffix(String url) {
		String extension = getExtension(url);
		if (StringUtils.isEmpty(extension)) {
			// 尝试从response.header.content-type获取
			String contentType = HttpUtils.getDiscardingContentType(url);
			extension = getImageSuffixByContentType(contentType);
		}
		return extension;
	}	
	
	private static String getImageSuffixByContentType(String headerContentType) {
		if (StringUtils.isEmpty(headerContentType) || !headerContentType.startsWith("image/")) {
			return null;
		}
		if (headerContentType.indexOf(";") > -1) {
			headerContentType = StringUtils.substringBefore(headerContentType, ";");
		}
		if (headerContentType.equalsIgnoreCase("image/png")) {
			return "png";
		} else if (headerContentType.equalsIgnoreCase("image/jpeg")) {
			return "jepg";
		} else if (headerContentType.equalsIgnoreCase("image/jpg")) {
			return "jpg";
		} else if (headerContentType.equalsIgnoreCase("image/x-icon")) {
			return "ico";
		} else if (headerContentType.equalsIgnoreCase("image/gif")) {
			return "gif";
		} else if (headerContentType.equalsIgnoreCase("image/webp")) {
			return "webp";
		}
		return null;
	}
	
	/**
	 * 获取符合条件的所有文件，包含子目录
	 * 
	 * @param templateDirectory
	 * @param fileFilter
	 * @return
	 */
	public static List<File> loopFiles(String templateDirectory, FileFilter fileFilter) {
		File file;
		if (StringUtils.isEmpty(templateDirectory) || !(file = new File(templateDirectory)).exists()) {
			return Collections.emptyList();
		}
		List<File> filterFiles = new ArrayList<>();
		loopFiles(file, fileFilter, filterFiles);
		return filterFiles;
	}

	private static void loopFiles(File file, FileFilter fileFilter, List<File> filterFiles) {
		if (file.isFile()) {
			if (fileFilter.accept(file)) {
				filterFiles.add(file);
			}
		} else {
			File[] listFiles = file.listFiles();
			for (File child : listFiles) {
				loopFiles(child, fileFilter, filterFiles);
			}
		}
	}

	/**
	 * 格式化路径，去掉../、./等类似路径避免文件泄露
	 * 
	 * @param path
	 * @return
	 */
	public static String normalizePath(String path) {
		path = path.replace('\\', '/');

		path = StringUtils.replaceEx(path, "../", "/");
		path = StringUtils.replaceEx(path, "./", "/");
		path = StringUtils.replaceEx(path, "~/", "/");
		if (path.endsWith("..")) {
			path = path.substring(0, path.length() - 2);
		}
		path = path.replaceAll("/+", "/");
		return path;
	}

	/**
	 * 创建指定路径的目录，包括所有上级目录
	 * 
	 * @param path
	 */
	public static void mkdirs(String... paths) {
		if (StringUtils.isEmpty(paths)) {
			return;
		}
		for (String path : paths) {
			new File(path).mkdirs();
		}
	}
}
