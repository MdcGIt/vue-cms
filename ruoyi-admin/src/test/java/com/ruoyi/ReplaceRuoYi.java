package com.ruoyi;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class ReplaceRuoYi {

	static final Charset CHARSET = Charset.forName("UTF-8");

	static final String CHANGE_TO = "chestnut";

	public static void main(String[] args) throws IOException {
		String root = "D:/dev/workspace_gitea/RuoYi-Vue-bak/";

		modifyPom(root);
		System.out.println("modify pom.xml done");

		modifyResources(root, "ruoyi", CHANGE_TO);
		System.out.println("modify resources done");

		modifyJavaFileName(root, new String[] { "RuoYi" }, new String[] { "Chestnut" });
		System.out.println("modify java package done");

		modifyJavaPackages(root, "ruoyi", CHANGE_TO);
		System.out.println("modify java package done");

		modifyModules(root, "ruoyi", CHANGE_TO);
		System.out.println("modify java package done");

		modifyProjectName(root, "Chestnut-Vue");
		System.out.println("modify project name done");
	}

	private static void modifyJavaFileName(String root, String[] searchStrs, String[] replacements) throws IOException {
		Map<String, String> result = new HashMap<>();
		modifyFileName(new File(root), new FileFilter() {

			@Override
			public boolean accept(File f) {
				return f.getName().endsWith(".java");
			}
		}, searchStrs, replacements, result);
		
		
	}

	private static void modifyPom(String root) throws IOException {
		modifyFileContent(new File(root), new FileFilter() {

			@Override
			public boolean accept(File f) {
				return f.getName().equals("pom.xml");
			}
		}, "ruoyi", CHANGE_TO);
	}

	/**
	 * 修改src/main/resources下的所有配置文件内容
	 * 
	 * @param root
	 * @param findStr
	 * @param replacement
	 * @throws IOException
	 */
	private static void modifyResources(String root, String findStr, String replacement) throws IOException {
		modifyFileContent(new File(root), new FileFilter() {

			@Override
			public boolean accept(File f) {
				return f.getPath().indexOf("src" + File.separator + "main" + File.separator + "resources") > -1;
			}
		}, findStr, replacement);
	}

	/**
	 * 修改main/java/**下的包名
	 * 
	 * @throws IOException
	 */
	private static void modifyJavaPackages(String root, String from, String target) throws IOException {
		File[] dirs = new File(root).listFiles(f -> f.isDirectory() && f.getName().startsWith("ruoyi-"));
		for (File dir : dirs) {
			Path path = Paths.get(dir.getAbsolutePath(), "pom.xml");
			if (Files.exists(path)) {
				// src/main
				File fromDirectory = Paths.get(dir.getAbsolutePath(), "/src/main/java/com/ruoyi").toFile();
				if (!fromDirectory.exists()) {
					continue;
				}
				File targetDirectory = new File(dir.getAbsolutePath() + "/src/main/java/com/" + target);
				FileUtils.moveDirectory(fromDirectory, targetDirectory);
				System.out.println("modify java package: " + fromDirectory.getAbsolutePath() + " -> "
						+ targetDirectory.getAbsolutePath());
				// 修改java文件注释内容
				modifyFileContent(targetDirectory, new FileFilter() {

					@Override
					public boolean accept(File f) {
						return f.getName().endsWith(".java");
					}
				}, "com.ruoyi.", "com." + CHANGE_TO + ".");

				modifyFileContent(targetDirectory, new FileFilter() {

					@Override
					public boolean accept(File f) {
						return f.getName().endsWith(".java");
					}
				}, "com.ruoyi.", "com." + CHANGE_TO + ".");

				modifyFileContent(targetDirectory, new FileFilter() {

					@Override
					public boolean accept(File f) {
						return f.getName().endsWith(".java");
					}
				}, "@author ruoyi", "@author " + CHANGE_TO);
				// src/test
				File fromDirectory2 = Paths.get(dir.getAbsolutePath(), "/src/test/java/com/ruoyi").toFile();
				if (!fromDirectory2.exists()) {
					continue;
				}
				File targetDirectory2 = new File(dir.getAbsolutePath() + "/src/test/java/com/" + target);
				FileUtils.moveDirectory(fromDirectory2, targetDirectory2);
				System.out.println("modify java package: " + fromDirectory2.getAbsolutePath() + " -> "
						+ targetDirectory2.getAbsolutePath());
				// 修改java文件注释内容
				modifyFileContent(targetDirectory2, new FileFilter() {

					@Override
					public boolean accept(File f) {
						return f.getName().endsWith(".java");
					}
				}, "com.ruoyi.", "com." + CHANGE_TO + ".");

				modifyFileContent(targetDirectory2, new FileFilter() {

					@Override
					public boolean accept(File f) {
						return f.getName().endsWith(".java");
					}
				}, "com.ruoyi.", "com." + CHANGE_TO + ".");

				modifyFileContent(targetDirectory2, new FileFilter() {

					@Override
					public boolean accept(File f) {
						return f.getName().endsWith(".java");
					}
				}, "@author ruoyi", "@author " + CHANGE_TO);
			}
		}
	}

	/**
	 * 修改模块目录名
	 * 
	 * @param root
	 * @param from
	 * @param target
	 * @throws IOException
	 */
	private static void modifyModules(String root, String from, String target) throws IOException {
		File[] dirs = new File(root).listFiles(f -> f.isDirectory() && f.getName().startsWith("ruoyi-"));
		for (File dir : dirs) {
			File targetDirectory = new File(dir.getParent() + File.separator + dir.getName().replace(from, target));
			FileUtils.moveDirectory(dir, targetDirectory);
			System.out.println("modify module: " + dir.getAbsolutePath() + " -> " + targetDirectory.getAbsolutePath());
		}
	}

	private static void modifyProjectName(String root, String newRoot) throws IOException {
		File file = new File(root);
		File target = new File(file.getParent() + File.separator + newRoot);
		FileUtils.moveDirectory(file, target);
	}

	/**
	 * 递归替换指定目录下的下所有文件内容，将from替换成target
	 * 
	 * @param file
	 * @param findStr
	 * @param replacement
	 * @throws IOException
	 */
	private static void modifyFileContent(File file, FileFilter fileFilter, String findStr, String replacement)
			throws IOException {
		if (file.isFile()) {
			if (fileFilter == null || fileFilter.accept(file)) {
				String content = org.apache.commons.io.FileUtils.readFileToString(file, CHARSET);
				String replaceContent = StringUtils.replace(content, findStr, replacement);
				org.apache.commons.io.FileUtils.writeStringToFile(file, replaceContent, CHARSET);
				System.out.println("modify file: " + file.getAbsolutePath() + " | " + findStr + " -> " + replacement);
			}
		} else {
			File[] subFiles = file.listFiles();
			for (File subFile : subFiles) {
				modifyFileContent(subFile, fileFilter, findStr, replacement);
			}
		}
	}

	private static void modifyFileName(File file, FileFilter fileFilter, String[] findStrs, String[] replacements,
			Map<String, String> result) throws IOException {
		if (file.isFile()) {
			if (fileFilter.accept(file)) {
				String replace = StringUtils.replaceEach(file.getName(), findStrs, replacements);
				if (!file.getName().equals(replace)) {
					File target = new File(file.getParent() + File.separator + replace);
					FileUtils.moveFile(file, target);
					result.put(file.getAbsolutePath(), target.getAbsolutePath());
					System.out.println(
							"modify java file name: " + file.getAbsolutePath() + " -> " + target.getAbsolutePath());
				}
			}
		} else {
			File[] subFiles = file.listFiles();
			for (File subFile : subFiles) {
				modifyFileName(subFile, fileFilter, findStrs, replacements, result);
			}
		}
	}
}
