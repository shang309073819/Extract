package com.rsclouds.Extract;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class Test {
	public static void main(String[] args) {

		GZip gzip = new GZip();
		boolean flag = gzip
				.unTargzFile("/Users/chenshang/Downloads/test.tar.gz");
		if (flag) {
			System.out.println("解压成功");
		} else {
			System.out.println("解压失败");
		}

		List<String> nameList = gzip.getNameList();
		for (String name : nameList) {
			System.out.println(name);
		}

		System.out.println(gzip.getOutputDir());

		try {
			boolean flag2 = ReadFile.deletefile(gzip.getOutputDir());
			if (flag2) {
				System.out.println("删除成功");
			} else {
				System.out.println("删除失败");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// List<String> dirList = gzip.getDirList();
		// if (dirList.isEmpty()) {
		// System.out.println("nihao");
		// }
		// for (String name : dirList) {
		// if (name == " ") {
		// System.out.println("nihao");
		// } else {
		// System.out.println("buhao");
		// }
		// }
	}
}
