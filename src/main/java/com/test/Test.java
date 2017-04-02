package com.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import de.innosystec.unrar.Archive;
import de.innosystec.unrar.exception.RarException;
import de.innosystec.unrar.rarfile.FileHeader;

public class Test {
	/**
	 * * 解压缩rar文件 *
	 * 
	 * @param rarFileName
	 * @param extPlace
	 */

	public static boolean decompressionRarFiles(String rarFileName,
			String extPlace) {
		boolean flag = false;
		Archive archive = null;
		File out = null;
		File file = null;
		File dir = null;
		FileOutputStream os = null;
		FileHeader fh = null;
		String path, dirPath = "";
		try {
			file = new File(rarFileName);
			System.out.println("AbsolutePath is:" + file.getAbsolutePath());
			archive = new Archive(file);
		} catch (RarException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (file != null) {
				file = null;
			}
		}
		if (archive != null) {
			try {
				fh = archive.nextFileHeader();
				while (fh != null) {
					String fileName = " ";
					if(fh.isUnicode()){  
					  fileName = fh.getFileNameW().trim();  
					}else{  
					  fileName = fh.getFileNameString().trim();    
					}  
					

					//fileName = fileName.replaceAll("\\", "/");
					path = (extPlace + fileName);
							//replaceAll("\\", File.separator);
					int end = path.lastIndexOf("/");
					if (end != -1) {
						dirPath = path.substring(0, end);
					}
					try {
						dir = new File(dirPath);
						if (!dir.exists()) {
							dir.mkdirs();
						}
					} catch (RuntimeException e1) {
						e1.printStackTrace();
					} finally {
						if (dir != null) {
							dir = null;
						}
					}
					if (fh.isDirectory()) {
						fh = archive.nextFileHeader();
						continue;
					}
					out = new File(extPlace + fh.getFileNameString().trim());
					try {
						os = new FileOutputStream(out);
						archive.extractFile(fh, os);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (RarException e) {
						e.printStackTrace();
					} finally {
						if (os != null) {
							try {
								os.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						if (out != null) {
							out = null;
						}
					}
					fh = archive.nextFileHeader();
				}
			} catch (RuntimeException e) {
				e.printStackTrace();
			} finally {
				fh = null;
				if (archive != null) {
					try {
						archive.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			flag = true;
		}
		return flag;
	}

	public static void main(String[] args) {
		String absPath = "/Users/chenshang/Downloads/hadoop.rar";
		// 文件绝对目录
		String toPath = "/Users/chenshang/Downloads/";
		// 文件目录
		boolean flag = Test.decompressionRarFiles(absPath, toPath);
		System.out.println("flag ---" + flag);
	}

}