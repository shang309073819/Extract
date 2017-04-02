package com.rsclouds.Extract;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ReadFile {
	private List<String> absolutePathList = new ArrayList<String>();

	public List<String> getAbsolutePathList() {
		return absolutePathList;
	}

	public void setAbsolutePathList(List<String> absolutePathList) {
		this.absolutePathList = absolutePathList;
	}

	public ReadFile() {
	}

	/**
	 * 读取某个文件夹下的所有文件
	 * 
	 * @filepath 文件路径
	 * @name 文件类型
	 */
	public boolean readfile(String filepath, String name)
			throws FileNotFoundException, IOException {
		try {

			File file = new File(filepath);
			if (!file.isDirectory()) {
				if (file.getAbsolutePath().endsWith(name)) {
					absolutePathList.add(file.getAbsolutePath());
				}
			} else if (file.isDirectory()) {
				String[] filelist = file.list();
				for (int i = 0; i < filelist.length; i++) {
					File readfile = new File(filepath + File.separator
							+ filelist[i]);
					if (!readfile.isDirectory()) {
						if (readfile.getAbsolutePath().endsWith(name)) {
							absolutePathList.add(readfile.getAbsolutePath());
						}
					} else if (readfile.isDirectory()) {
						readfile((filepath + File.separator + filelist[i]),
								name);
					}
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("readfile()   Exception:" + e.getMessage());
		}
		return true;
	}

	/**
	 * 删除某个文件夹下的所有文件夹和文件
	 */

	public static boolean deletefile(String delpath)
			throws FileNotFoundException, IOException {
		try {

			File file = new File(delpath);
			if (!file.isDirectory()) {
				file.delete();
			} else if (file.isDirectory()) {
				String[] filelist = file.list();
				for (int i = 0; i < filelist.length; i++) {
					File delfile = new File(delpath + File.separator
							+ filelist[i]);
					if (!delfile.isDirectory()) {
						delfile.delete();
					} else if (delfile.isDirectory()) {
						deletefile(delpath + File.separator + filelist[i]);
					}
				}
				file.delete();

			}
		} catch (FileNotFoundException e) {
			System.out.println("deletefile()   Exception:" + e.getMessage());
		}
		return true;
	}

	public static void main(String[] args) {
		ReadFile readFile = new ReadFile();
		try {
			readFile.readfile(
					"/Users/chenshang/Downloads/flume-ng-1.5.0-cdh5.1.2/",
					".xml");

			List<String> pathList = readFile.getAbsolutePathList();
			for (String path : pathList) {
				System.out.println(path);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
