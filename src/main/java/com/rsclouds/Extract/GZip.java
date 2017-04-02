package com.rsclouds.Extract;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

/**
 * 解压tar.gz文件包
 *
 */
public class GZip {

	private BufferedOutputStream bufferedOutputStream;
	private List<String> nameList = new ArrayList<String>();
	private List<String> dirList = new ArrayList<String>();
	private String outputDir = null;

	public String getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	public List<String> getNameList() {
		return nameList;
	}

	public void setNameList(List<String> nameList) {
		this.nameList = nameList;
	}

	public List<String> getDirList() {
		return dirList;
	}

	public void setDirList(List<String> dirList) {
		this.dirList = dirList;
	}

	String zipfileName = null;

	public GZip() {

	}

	// 去除文件的后缀名
	public String trimExtension(String filename) {
		if ((filename != null) && (filename.length() > 0)) {
			int i = filename.lastIndexOf('.');
			if ((i > -1) && (i < (filename.length()))) {
				return filename.substring(0, i);
			}
		}
		return filename;
	}

	/*
	 * 执行入口,rarFileName为需要解压的文件路径(具体到文件),
	 */

	public boolean unTargzFile(String rarFileName) {
		this.zipfileName = rarFileName;
		int index = rarFileName.lastIndexOf("/");
		String name = rarFileName.substring(index + 1);
		name = trimExtension(name); // 去除.gz
		name = trimExtension(name);// 去除.tar
		String outputDirectory = rarFileName.substring(0, index)
				+ File.separator + name;
		outputDir = outputDirectory;

		File file = new File(outputDirectory);
		if (!file.exists()) {
			file.mkdir();
		}
		return unzipOarFile(outputDirectory);

	}

	public boolean unzipOarFile(String outputDirectory) {
		FileInputStream fis = null;
		TarArchiveInputStream in = null;
		BufferedInputStream bufferedInputStream = null;
		try {
			fis = new FileInputStream(zipfileName);
			GZIPInputStream is = new GZIPInputStream(new BufferedInputStream(
					fis));
			in = (TarArchiveInputStream) new ArchiveStreamFactory()
					.createArchiveInputStream("tar", is);
			bufferedInputStream = new BufferedInputStream(in);
			TarArchiveEntry entry = in.getNextTarEntry();

			while (entry != null) {
				String name = entry.getName();
				/*
				 * 获取文件名存入到list中 by chenshang
				 */
				if (!name.endsWith("/") && (!name.startsWith("pax"))
						&& (!name.contains("/._"))) {
					nameList.add(name);
				}
				if (name.endsWith("/")) {
					dirList.add(name);
				}

				String[] names = name.split("/");
				String fileName = outputDirectory;
				for (int i = 0; i < names.length; i++) {
					String str = names[i];
					fileName = fileName + File.separator + str;
				}
				if (name.endsWith("/")) {
					mkFolder(fileName);
				} else {
					File file = mkFile(fileName);
					bufferedOutputStream = new BufferedOutputStream(
							new FileOutputStream(file));
					int b;
					while ((b = bufferedInputStream.read()) != -1) {
						bufferedOutputStream.write(b);
					}
					bufferedOutputStream.flush();
					bufferedOutputStream.close();
				}
				entry = (TarArchiveEntry) in.getNextEntry();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (ArchiveException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (bufferedInputStream != null) {
					bufferedInputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	private void mkFolder(String fileName) {
		File f = new File(fileName);
		if (!f.exists()) {
			f.mkdir();
		}
	}

	private File mkFile(String fileName) {
		File f = new File(fileName);
		try {
			f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return f;
	}

	public static void main(String[] args) {
		GZip gZip = new GZip();
		gZip.unTargzFile("/Users/chenshang/Downloads/test.tar.gz");
	}
}