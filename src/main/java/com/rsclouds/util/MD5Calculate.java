package com.rsclouds.util;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class MD5Calculate {

	public static String byteArrayToHex(byte[] byteArray) {
		StringBuilder hs = new StringBuilder();
		String stmp = "";
		for (int n = 0; n < byteArray.length; n++) {
			stmp = (Integer.toHexString(byteArray[n] & 0XFF));
			if (stmp.length() == 1) {
				hs.append("0" + stmp);
			} else {
				hs.append(stmp);
			}
			if (n < byteArray.length - 1) {
				hs.append("");
			}
		}
		return hs.toString();
	}

	public static String LocalfileMD5(String inputFile) throws IOException {
		// 缓冲区大小（这个可以抽出一个参数）
		int bufferSize = 256 * 1024;
		FileInputStream fileInputStream = null;
		DigestInputStream digestInputStream = null;
		try {
			// 拿到一个MD5转换器（同样，这里可以换成SHA1）
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			// 使用DigestInputStream
			fileInputStream = new FileInputStream(inputFile);

			digestInputStream = new DigestInputStream(fileInputStream,
					messageDigest);
			// read的过程中进行MD5处理，直到读完文件
			byte[] buffer = new byte[bufferSize];
			while (digestInputStream.read(buffer) > 0)
				;
			// 获取最终的MessageDigest
			messageDigest = digestInputStream.getMessageDigest();
			// 拿到结果，也是字节数组，包含16个元素
			byte[] resultByteArray = messageDigest.digest();
			// 同样，把字节数组转换成字符串
			return byteArrayToHex(resultByteArray);
		} catch (NoSuchAlgorithmException e) {
			return null;
		} finally {
			try {
				digestInputStream.close();
			} catch (Exception e) {
			}
			try {
				fileInputStream.close();
			} catch (Exception e) {
			}
		}

	}

	public static String HDFSfileMD5(FileSystem fs, Path path)
			throws IOException {
		// 缓冲区大小（这个可以抽出一个参数）
		int bufferSize = 256 * 1024;
		FSDataInputStream in = null;
		DigestInputStream digestInputStream = null;
		try {
			// 拿到一个MD5转换器（同样，这里可以换成SHA1）
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");

			// 使用DigestInputStream
			in = fs.open(path);
			digestInputStream = new DigestInputStream(in, messageDigest);

			// read的过程中进行MD5处理，直到读完文件
			byte[] buffer = new byte[bufferSize];
			while (digestInputStream.read(buffer) > 0)
				;
			// 获取最终的MessageDigest
			messageDigest = digestInputStream.getMessageDigest();
			// 拿到结果，也是字节数组，包含16个元素
			byte[] resultByteArray = messageDigest.digest();
			// 同样，把字节数组转换成字符串
			return byteArrayToHex(resultByteArray);
		} catch (NoSuchAlgorithmException e) {
			return null;
		} finally {
			try {
				digestInputStream.close();
			} catch (Exception e) {
			}
			try {
				in.close();
			} catch (Exception e) {
			}
		}
	}

	public static String fileByteMD5(byte[] filebyte) {
		MessageDigest messageDigest;
		DigestInputStream digestInputStream = null;
		InputStream in = null;
		int bufferSize = 256 * 1024;
		try {
			messageDigest = MessageDigest.getInstance("MD5");
			in = new ByteArrayInputStream(filebyte);
			digestInputStream = new DigestInputStream(in, messageDigest);

			byte[] buffer = new byte[bufferSize];
			while (digestInputStream.read(buffer) > 0)
				;
			messageDigest = digestInputStream.getMessageDigest();
			byte[] resultByteArray = messageDigest.digest();
			return byteArrayToHex(resultByteArray);

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				digestInputStream.close();
			} catch (Exception e) {
			}
			try {
				in.close();
			} catch (Exception e) {
			}
		}

	}
}
