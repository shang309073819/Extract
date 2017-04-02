package com.rsclouds;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import com.rsclouds.Extract.GZip;
import com.rsclouds.Extract.ReadFile;
import com.rsclouds.util.MD5Calculate;
import com.rsclouds.util.RedisUtils;
import com.rsclouds.util.TransCoding;

public class ExtractImport {

	private Configuration conf;

	public ExtractImport() {
		conf = HBaseConfiguration.create();
	}

	public ExtractImport(Configuration conf) {
		this.conf = conf;
	}

	public boolean ImportDirFromLocal(String inputPath, String outputPath,
			String metaName, String resourceName) {
		HTable metaTable = null;
		try {
			metaName = metaName == null ? ParameterDefine.META_TABLENAME
					: metaName;
			resourceName = resourceName == null ? ParameterDefine.RESOURCE_TABLENAME
					: resourceName;
			metaTable = new HTable(conf, metaName);
			// Encode path
			while (outputPath.length() > 1 && outputPath.endsWith("/")) {
				outputPath = outputPath.substring(0, outputPath.length() - 1);
			}
			outputPath = TransCoding.UrlEncode(outputPath, "utf-8");

			StringBuilder strBuilder = new StringBuilder(outputPath);
			strBuilder.insert(outputPath.lastIndexOf('/'), '/');
			System.out.println("======nanlin=====debug: input rowkey: "
					+ strBuilder.toString());
			File file = new File(inputPath);
			if (!file.exists()) {
				System.out
						.println("======nanlin=====debug: file doesn't exist!");
			}

			Put metaPut = new Put(Bytes.toBytes(strBuilder.toString()));
			metaPut.add(ParameterDefine.META_FAMILY, ParameterDefine.META_SIZE,
					Bytes.toBytes("-1"));
			metaPut.add(ParameterDefine.META_FAMILY, ParameterDefine.META_URL,
					Bytes.toBytes(""));
			metaPut.add(ParameterDefine.META_FAMILY, ParameterDefine.META_TIME,
					Bytes.toBytes("" + System.currentTimeMillis()));
			metaPut.add(ParameterDefine.META_FAMILY, ParameterDefine.META_DFS,
					ParameterDefine.ZERO);
			metaTable.put(metaPut);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (metaTable != null) {
					metaTable.flushCommits();
					metaTable.close();
				}
			} catch (IOException e) {
				return false;
			}
		}
		return true;
	}

	public boolean ImportFromLocal(String inputPath, String outputPath,
			String metaName, String resourceName) {
		HTable metaTable = null;
		HTable resTable = null;
		try {
			metaName = metaName == null ? ParameterDefine.META_TABLENAME
					: metaName;
			resourceName = resourceName == null ? ParameterDefine.RESOURCE_TABLENAME
					: resourceName;
			metaTable = new HTable(conf, metaName);
			resTable = new HTable(conf, resourceName);
			// Encode path
			while (outputPath.length() > 1 && outputPath.endsWith("/")) {
				outputPath = outputPath.substring(0, outputPath.length() - 1);
			}
			outputPath = TransCoding.UrlEncode(outputPath, "utf-8");

			StringBuilder strBuilder = new StringBuilder(outputPath);
			strBuilder.insert(outputPath.lastIndexOf('/'), '/');
			System.out.println("======nanlin=====debug: input rowkey: "
					+ strBuilder.toString());
			File file = new File(inputPath);
			if (!file.exists()) {
				System.out
						.println("======nanlin=====debug: file doesn't exist!");
			}
			long fileSize = file.length();
			byte[] md5Bytes = Bytes.toBytes(MD5Calculate
					.LocalfileMD5(inputPath));
			System.out.println("======nanlin=====debug: file's md5: "
					+ new String(md5Bytes));
			Put metaPut = new Put(Bytes.toBytes(strBuilder.toString()));
			metaPut.add(ParameterDefine.META_FAMILY, ParameterDefine.META_SIZE,
					Bytes.toBytes("" + fileSize));
			metaPut.add(ParameterDefine.META_FAMILY, ParameterDefine.META_URL,
					md5Bytes);
			metaPut.add(ParameterDefine.META_FAMILY, ParameterDefine.META_TIME,
					Bytes.toBytes("" + System.currentTimeMillis()));

			Put resPut = new Put(md5Bytes);
			Get get = new Get(md5Bytes);

			// the file has exicts on the gt-data
			Result result = resTable.get(get);
			if (result != null && !result.isEmpty()) {
				System.out
						.println("======nanlin=====debug: file exists on gt-data");
				if (fileSize < 16777216) {
					metaPut.add(ParameterDefine.META_FAMILY,
							ParameterDefine.META_DFS, ParameterDefine.ZERO);
				} else {
					metaPut.add(ParameterDefine.META_FAMILY,
							ParameterDefine.META_DFS, ParameterDefine.ONE);
				}
				metaTable.put(metaPut);

				String links_count = new String(result.getValue(
						ParameterDefine.RESOURCE_FAMILY,
						ParameterDefine.RESOURCE_LINKS));
				int linksInt = Integer.parseInt(links_count) + 1;
				resPut.add(ParameterDefine.RESOURCE_FAMILY,
						ParameterDefine.RESOURCE_LINKS,
						Bytes.toBytes("" + linksInt));
				resTable.put(resPut);

			} else {// file doesn't exists on gt-data
				System.out
						.println("======nanlin=====debug: file doesn't exists on gt-data");
				resPut.add(ParameterDefine.RESOURCE_FAMILY,
						ParameterDefine.RESOURCE_LINKS, ParameterDefine.ONE);

				if (fileSize < 16777216) {// less than 16MB,input hbase
					metaPut.add(ParameterDefine.META_FAMILY,
							ParameterDefine.META_DFS, ParameterDefine.ZERO);
					byte[] value = new byte[(int) fileSize];
					InputStream in = new FileInputStream(file);
					int length = value.length;
					int readLen = 0;
					int off = 0;
					while ((readLen = in.read(value, off, length)) != 0) {
						off += readLen;
						length -= readLen;
					}
					resPut.add(ParameterDefine.RESOURCE_FAMILY,
							ParameterDefine.RESOURCE_DATA, value);
					in.close();

				} else {// more than 16MB, input hdfs
					metaPut.add(ParameterDefine.META_FAMILY,
							ParameterDefine.META_DFS, ParameterDefine.ONE);
					FileSystem fs = FileSystem.get(conf);
					byte[] value = new byte[1024];
					int readLen;
					InputStream in = new FileInputStream(file);
					FSDataOutputStream out = fs.create(new Path(
							ParameterDefine.HDFSHOST_PATH
									+ new String(md5Bytes)));
					while ((readLen = in.read(value, 0, 1024)) != -1) {
						out.write(value, 0, readLen);
					}
					in.close();
					out.close();
					fs.close();
				}
				metaTable.put(metaPut);
				resTable.put(resPut);
				;
			}
			// redis check
			RedisUtils.redisCheck(strBuilder.toString());
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (metaTable != null) {
					metaTable.flushCommits();
					metaTable.close();
				}
				if (resTable != null) {
					resTable.flushCommits();
					resTable.close();
				}
			} catch (IOException e) {
				return false;
			}
		}
		return true;
	}

	// 去除文件的后缀名
	public static String trimExtension(String filename) {
		if ((filename != null) && (filename.length() > 0)) {
			int i = filename.lastIndexOf('.');
			if ((i > -1) && (i < (filename.length()))) {
				return filename.substring(0, i);
			}
		}
		return filename;
	}

	public static void upload(String argPath, String args_1, String args_2,
			String args_3, String args_4) {

		String metaTable = args_3;
		String resourcetable = args_4;
		// 解析参数
		String localString = null;
		String localName = null;
		String localName2 = null;
		int index = argPath.lastIndexOf("/");
		localString = argPath.substring(0, index + 1);
		localName = argPath.substring(index + 1);
		localName2 = trimExtension(localName); // 去除.gz
		localName2 = trimExtension(localName2); // 去除.tar

		if (!args_1.endsWith("/")) {
			args_1 = args_1 + File.separator;
		}

		if (!args_2.endsWith("/")) {
			args_2 = args_2 + File.separator;
		}

		// 上传压缩文件
		ExtractImport importData = new ExtractImport();
		importData.ImportFromLocal(argPath, args_1 + localName, metaTable,
				resourcetable);

		// 开始解压
		GZip gzip = new GZip();
		boolean flag1 = gzip.unTargzFile(argPath);
		if (flag1) {
			System.out.println("解压成功");
		} else {
			System.out.println("解压失败");
		}

		// 上传解压的文件
		List<String> nameList = gzip.getNameList();
		for (String name : nameList) {
			// 判断解压后是否包含文件夹
			if (name.contains(localName2 + File.separator)) {
				System.out.println("local path is:" + localString + localName2
						+ File.separator + name);
				importData.ImportFromLocal(localString + localName2
						+ File.separator + name, args_2 + name, metaTable,
						resourcetable);
			} else {
				System.out.println("local path is:" + localString + localName2
						+ File.separator + name);
				importData.ImportFromLocal(localString + localName2
						+ File.separator + name, args_2 + localName2
						+ File.separator + name, metaTable, resourcetable);
			}
		}

		// 写入文件夹
		List<String> dirList = gzip.getDirList();
		if (dirList.isEmpty()) {
			importData.ImportDirFromLocal(localString + localName2
					+ File.separator, args_2 + localName2, metaTable,
					resourcetable);
		} else {
			for (String name : dirList) {
				// 判断解压后是否包含文件夹
				if (name.contains(localName2 + File.separator)) {
					importData.ImportDirFromLocal(localString + localName2
							+ File.separator + name, args_2 + name, metaTable,
							resourcetable);
				} else {
					importData.ImportDirFromLocal(localString + localName2
							+ File.separator + name, args_2 + localName2
							+ File.separator + name, metaTable, resourcetable);
				}
			}
		}

		// 删除文件夹
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
	}

	public static void main(String[] args) {

		String args_3 = null;
		String args_4 = null;
		if (args.length < 3) {
			System.out
					.println("usage:<InputPath>, <OutputPath>,<ExtractPath> <meta_table>, <resource_table>");
		} else if (args.length == 5) {
			args_3 = args[3];
			args_4 = args[4];
		}

		// 遍历输入路径，找到要解压的文件
		ReadFile readFile = new ReadFile();
		try {
			readFile.readfile(args[0], ".tar.gz");

			List<String> pathList = readFile.getAbsolutePathList();
			for (String path : pathList) {
				upload(path, args[1], args[2], args_3, args_4);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
