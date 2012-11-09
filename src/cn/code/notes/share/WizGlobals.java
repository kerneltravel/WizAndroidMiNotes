package cn.code.notes.share;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.UUID;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipException;

import cn.code.notes.R;
import cn.code.notes.data.Notes;
import cn.code.notes.data.NoteColumnsInterface.DeletedColumns;
import cn.code.notes.data.NoteColumnsInterface.NoteColumns;
import cn.code.notes.model.WorkingNote;
import cn.code.notes.tool.DataUtils;
import cn.code.notes.ui.NoteEditActivity;
import cn.code.notes.ui.SystemEditActivity;

import redstone.xmlrpc.XmlRpcStruct;
import redstone.xmlrpc.util.HTMLUtil;
import redstone.xmlrpc.zip.ZipEntry;
import redstone.xmlrpc.zip.ZipFile;
import redstone.xmlrpc.zip.ZipOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

public class WizGlobals {

	public static final String DATA_EXTRAS_STRING_PATH = "notePath";
	public static final String DATA_INFO_TYPE_DOCUMENT = "document";
	public static final String DATA_INFO_TYPE_TAG = "tag";
	public static final String DATA_INFO_TYPE_ATTACHMENT = "attachment";
	public static final String DATA_INFO_TYPE_LOCATION = "location";

	public static final String ActionNameForDownload = "WizDownloadDocument";
	public final static String ActionNameForGetRecent = "WizGetRecentDocuments";
	public final static String ActionNameForDownloadAtt = "WizDownloadAttachment";
	public static final String ActionNameForSync = "WizSync";
	public static final String ActionNameForCreate = "WizCreateAccount";
	public static final String ActionNameForVerify = "WizVerifyAccount";
	public static final String ActionNameForGetCert = "WizGetCert";

	public static final int WIZ_ACCOUNT_MENU_QUICK_SEARCH_ACTION = 1;

	public static final int VIEW_CURRENT_DOCUMENT = 1005;
	public static final int IS_EDIT_DOCUMENT_TRUE = 1006;
	public static final String IS_ENCRYPTION_DOCUMENT = "isEncryptionDocument";

	// 进制转换
	private static String hexDigit(byte x) {
		StringBuffer sb = new StringBuffer();
		char c;
		// First nibble
		c = (char) ((x >> 4) & 0xf);
		if (c > 9) {
			c = (char) ((c - 10) + 'a');
		} else {
			c = (char) (c + '0');
		}
		sb.append(c);
		// Second nibble
		c = (char) (x & 0xf);
		if (c > 9) {
			c = (char) ((c - 10) + 'a');
		} else {
			c = (char) (c + '0');
		}
		sb.append(c);
		return sb.toString();
	}

	static public boolean isMD5Password(String password) {
		if (password == null || password.length() != 36)//
			return false;
		// 判断以前是否有明文密码，如果有转换成MD5格式。
		return "md5.".equals(password.substring(0, 4));
	}

	static public String makeMD5Password(String password) {
		if (isMD5Password(password))
			return password;
		return "md5." + makeMD5(password);
	}

	// 对字符串加密
	static public String makeMD5(String text) {
		//
		MessageDigest md5;
		try {
			// 生成一个MD5加密计算摘要
			md5 = MessageDigest.getInstance("MD5"); // 计算md5函数
			byte b[] = text.getBytes();
			md5.update(b);
			// digest()最后确定返回md5 hash值，返回值为8wei字符串。因为md5
			// hash值是16位的hex值，实际上就是8位的字符
			byte digest[] = md5.digest();
			StringBuffer hexString = new StringBuffer();
			int digestLength = digest.length;
			for (int i = 0; i < digestLength; i++) {
				hexString.append(hexDigit(digest[i]));
			}
			return hexString.toString();
		} catch (Exception e) {
			// e.printStackTrace();
		}
		return text;
	}

	// 对字节数组加密
	static public String makeMD5(byte[] b, int size) {
		//
		MessageDigest md5;
		try {
			// 生成一个MD5加密计算摘要
			md5 = MessageDigest.getInstance("MD5"); // 计算md5函数
			// byte b[] = text.getBytes();
			md5.update(b, 0, size);
			// digest()最后确定返回md5 hash值，返回值为8wei字符串。因为md5
			// hash值是16位的hex值，实际上就是8位的字符
			byte digest[] = md5.digest();
			StringBuffer hexString = new StringBuffer();
			int digestLength = digest.length;
			for (int i = 0; i < digestLength; i++) {
				hexString.append(hexDigit(digest[i]));
			}
			return hexString.toString();
		} catch (Exception e) {
			// e.printStackTrace();
		}
		return null;
	}

	// 对字节数组加密
	static public String makeMD5(byte[] b) {
		//
		MessageDigest md5;
		try {
			// 生成一个MD5加密计算摘要
			md5 = MessageDigest.getInstance("MD5"); // 计算md5函数
			// byte b[] = text.getBytes();
			md5.update(b);
			// digest()最后确定返回md5 hash值，返回值为8wei字符串。因为md5
			// hash值是16位的hex值，实际上就是8位的字符
			byte digest[] = md5.digest();
			StringBuffer hexString = new StringBuffer();
			int digestLength = digest.length;
			for (int i = 0; i < digestLength; i++) {
				hexString.append(hexDigit(digest[i]));
			}
			return hexString.toString();
		} catch (Exception e) {
			// e.printStackTrace();
		}
		return null;
	}

	/**
	 * 默认的密码字符串组合，apache校验下载的文件的正确性用的就是默认的这个组合
	 */
	protected static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public static String makeMD5ForFile(String fileName) {
		if (isEmptyString(fileName))
			return "";
		File file = new File(fileName);
		return makeMD5ForFile(file);
	}

	/**
	 * 不使用NIO的计算MD5，不会造成文件被锁死
	 * 
	 * @param file
	 * @return
	 */
	public static String makeMD5ForFile(File file) {
		FileInputStream in = null;
		String md5 = null;
		MessageDigest digest = null;
		byte buffer[] = new byte[1024];
		int len;
		try {
			digest = MessageDigest.getInstance("MD5");
			in = new FileInputStream(file);

			while ((len = in.read(buffer, 0, 1024)) != -1) {
				digest.update(buffer, 0, len);
			}
			md5 = bufferToHex(digest.digest());

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return md5;
	}

	public static boolean existsCret(WizCretData data) {
		if (data == null)
			return false;
		if (isEmptyString(data.getmEncryptedD()))
			return false;
		if (isEmptyString(data.getmRsaE()))
			return false;
		if (isEmptyString(data.getmRsaN()))
			return false;

		return true;

	}

	public static boolean decryptStream(Context mContext,
			String mAccountUserId, String guid, String key, String mRsaN,
			String mRsaE, String mD) {
		if (isEmptyString(key))
			return false;

		FileInputStream in = null;
		String iv = "0123456789abcdef";
		String cretFileName = getCertFile(mContext, mAccountUserId, guid);
		String zipFileName = getZipFile(mContext, mAccountUserId, guid);

		try {

			if (!fileExists(cretFileName))
				return false;

			in = new FileInputStream(cretFileName);

			byte[] mFileTypeArr = new byte[4];
			in.read(mFileTypeArr);

			if (!isEncryptFile(mFileTypeArr))
				return false;

			int version = readIntFromStream(in);
			if (1 != version)
				return false;

			WizCertAESUtil aes = new WizCertAESUtil(key, iv);
			String mRsaD = aes.decryptString(mD);
			if (isEmptyString(mRsaD))
				return false;

			int keyLength = readIntFromStream(in);
			byte[] mAESKeyArr = new byte[128];
			in.read(mAESKeyArr);

			WizCertRSAUtil rsa = new WizCertRSAUtil(mRsaN, mRsaE, mRsaD);
			String mAESKey = rsa.decryptStream(mAESKeyArr, 0, keyLength);

			if (isEmptyString(mAESKey))
				return false;

			WizCertAESUtil aes1 = new WizCertAESUtil(mAESKey, iv);
			if (!aes1.decryptStream(in, zipFileName, 16))
				return false;
			if (!unZipDocumentData(mContext, mAccountUserId, guid, zipFileName))
				return false;
			WizIndex.updateDocument(mContext, mAccountUserId, guid);
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		} finally {
			try {
				in.close();
			} catch (IOException e) {
			}
			deleteFile(zipFileName);
		}
		return true;
	}

	private static int readIntFromStream(InputStream ins) throws IOException {
		byte[] data = new byte[4];
		ins.read(data);
		return byteToInt2(data);
	}

	public static int byteToInt2(byte[] res) {

		int targets = (res[0] & 0xff) | ((res[1] << 8) & 0xff00) // | 表示安位或
				| ((res[2] << 24) >>> 8) | (res[3] << 24);
		return targets;
	}

	private static String bufferToHex(byte bytes[]) {
		return bufferToHex(bytes, 0, bytes.length);
	}

	private static String bufferToHex(byte bytes[], int m, int n) {
		StringBuffer stringbuffer = new StringBuffer(2 * n);
		int k = m + n;
		for (int l = m; l < k; l++) {
			appendHexPair(bytes[l], stringbuffer);
		}
		return stringbuffer.toString();
	}

	private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
		char c0 = hexDigits[(bt & 0xf0) >> 4];
		char c1 = hexDigits[bt & 0xf];
		stringbuffer.append(c0);
		stringbuffer.append(c1);
	}

	// 获取文件类型
	public static String extractFileExt(String filename) {
		return extractFileExt(filename, "");
	}

	public static String extractFileExt(String filename, String defExt) {
		if ((filename != null) && (filename.length() > 0)) {
			int i = filename.lastIndexOf('.');

			if ((i > -1) && (i < (filename.length() - 1))) {
				return filename.substring(i);
			}
		}
		return defExt;
	}

	// 获取文件的Title部分(不包含.后面的内容也即文件类型)
	public static String extractFileTitle(String filename) {
		String title = extractFileName(filename);
		//
		if ((title != null) && (title.length() > 0)) {
			int i = title.lastIndexOf('.');
			if ((i > -1) && (i < (title.length()))) {
				return title.substring(0, i);
			}
		}
		return title;
	}

	// 获取文件路径
	public static String extractFilePath(String filename) {
		filename = pathRemoveBackslash(filename);
		//
		int pos = filename.lastIndexOf('/');
		if (-1 == pos)
			return "";
		return filename.substring(0, pos);
	}

	// 获取目录
	public static String[] getFilePath(String filename) {

		String[] paths = null;

		if (getPathSlash(filename)) {
			paths = filename.split("/");
			if (paths.length == 0) {
				paths = filename.split("\\");
			}
		} else {
			filename = filename + "/";
			paths = filename.split("/");
		}

		return paths;
	}

	// 判断字符段中是否存在反斜线
	static boolean getPathSlash(String path) {
		if (path == null)
			return false;
		if (path.length() == 0)
			return false;
		//
		if (path.indexOf("/") == -1 || path.indexOf("\\") == -1) {
			return false;
		}
		return true;
	}

	// 获取文件名
	public static String extractFileName(String filename) {
		if ((filename != null) && (filename.length() > 0)) {
			int i = filename.lastIndexOf('/');
			if ((i > -1) && (i < (filename.length()))) {
				return filename.substring(i + 1);
			}
		}
		return filename;
	}

	// 从流中 读取文件
	public static String loadTextFromStream(FileInputStream stream,
			String charset) {
		String ret = "";
		try {
			// 读取输入流
			InputStreamReader reader = new InputStreamReader(stream, charset);
			//
			BufferedReader br = new BufferedReader(reader);
			//
			StringBuffer sb = new StringBuffer();
			//
			try {
				String line;
				line = br.readLine();
				while (line != null) {
					sb.append(line);
					sb.append("\n");
					line = br.readLine();
				}
			} catch (Exception err) {
				err.printStackTrace();
			}
			//
			br.close();
			reader.close();
			//
			ret = sb.toString();
		} catch (FileNotFoundException err) {

		} catch (IOException err) {

		}
		return ret;
	}

	// 读取指定text文件
	public static String loadTextFromFile(String fileName, String charset)
			throws FileNotFoundException, IOException {
		String ret = "";
		String path = extractFilePath(fileName);
		String name = extractFileName(fileName);
		//
		java.io.File file = new java.io.File(path, name);
		// 产生一个输入流文件
		FileInputStream stream = new FileInputStream(file);

		try {
			// 读文件：：charset=字节编码格式
			ret = loadTextFromStream(stream, charset);
		} finally {
			stream.close();
		}
		return ret;
	}

	// 读取字节文件
	public static byte[] loadByteFromFile(String fileName)
			throws FileNotFoundException, IOException {
		byte[] ret = null;
		//
		String path = extractFilePath(fileName);
		String name = extractFileName(fileName);
		//
		java.io.File file = new java.io.File(path, name);
		//
		FileInputStream stream = new FileInputStream(file);
		try {
			int length = stream.available();
			ret = new byte[length];
			//
			stream.read(ret);
		} finally {
			stream.close();
		}

		return ret;
	}

	// 保存图片
	public static void saveBitmap(Bitmap bmp,
			android.graphics.Bitmap.CompressFormat format, String fileName)
			throws IOException {
		java.io.FileOutputStream out = new java.io.FileOutputStream(fileName);
		bmp.compress(format, 100, out);
		out.flush();
		out.close();
	}

	public static String toValidFileName(String fileName) {
		fileName = fileName.replace(':', '-');
		fileName = fileName.replace('/', '-');
		fileName = fileName.replace('\\', '-');
		fileName = fileName.replace(',', '-');
		fileName = fileName.replace('?', '-');
		fileName = fileName.replace('.', '-');
		fileName = fileName.replace('!', '-');
		fileName = fileName.replace('\'', '-');
		fileName = fileName.replace('"', '-');
		fileName = fileName.replace('`', '-');
		fileName = fileName.replace('\r', '-');
		fileName = fileName.replace('\n', '-');
		if (fileName.length() > 100)
			fileName = fileName.substring(0, 100);
		//
		return fileName;
	}

	public static String getDateTimeFileTitle() {
		Date dt = new Date();
		return getDateTimeFileTitle(dt);
	}

	public static String getDateTimeFileTitle(Date dt) {
		String fileName = dt.toLocaleString();// 格式化时间类型
		return toValidFileName(fileName);//
	}

	public static String formatInt(int n, int width) {
		String str = Integer.toString(n);
		int count = width - str.length();
		for (int i = 0; i < count; i++) {
			str = "0" + str;
		}
		return str;
	}

	public static String getCurrentSQLDateTimeString() {
		return getSQLDateTimeString(new Date());
	}

	public static String getCurrentSQLDateTimeString(long time) {
		return getSQLDateTimeString(time);
	}

	public static String getCurrentSQLDateTimePastStringForDay(int countDay) {
		return getSQLDateTimeString(new Date(), countDay);
	}

	// Time Past
	public static String getCurrentSQLDateTimePastStringForWeek(int countWeek) {
		return getCurrentSQLDateTimePastStringForDay(countWeek * 7);
	}

	public static String getCurrentSQLDateTimePastStringForMonth(int countMonth) {
		return getCurrentSQLDateTimePastStringForDay(countMonth * 30);
	}

	public static final String pattern = "yyyy-MM-dd HH:mm:ss";
	public static final String patternWeeHours = "yyyy-MM-dd 00:00:00";

	public static long getSQLDateTimeLong(String dt) {
		SimpleDateFormat simpledateformat = new SimpleDateFormat(pattern,
				Locale.SIMPLIFIED_CHINESE);
		Date date = null;
		try {
			date = simpledateformat.parse(dt);// 将参数按照给定的格式解析参数
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date.getTime();
	}

	public static String getSQLDateTimeString(long dt) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(pattern);
			return sdf.format(dt);
		} catch (Exception e) {
			return "";
		}
	}

	public static String getSQLDateTimeString(Date dt) {
		SimpleDateFormat date = new SimpleDateFormat(pattern);
		if (dt == null)
			dt = new Date();
		return date.format(dt);
	}

	public static String getSQLDateTimeString(Date dt, int countDay) {
		SimpleDateFormat date = new SimpleDateFormat(patternWeeHours);
		if (dt == null)
			dt = new Date();
		//
		int day = dt.getDate() - countDay;
		dt.setDate(day);
		return date.format(dt);
	}

	public static Date getDateFromSqlDateTimeString(String sqlDateTimeString) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(pattern);
		try {
			return dateFormatter.parse(sqlDateTimeString);
		} catch (ParseException e) {
			return new Date();
		}
	}

	public static Date getDateFromSqlDateTimeString(long longtime) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		SimpleDateFormat dateFormatter = new SimpleDateFormat(pattern);
		try {
			return dateFormatter.parse(sdf.format(longtime));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	// 获取第一行
	public static String getFirstLineOfText(String text) {
		int pos = text.indexOf('\n');
		if (pos == -1) {
			if (text.length() > 100)
				return text.substring(0, 100);
			return text;
		}
		return text.substring(0, pos);
	}

	public static void copyFile(String srcFileName, String destFileName)
			throws FileNotFoundException, IOException {
		java.io.FileInputStream stream = new java.io.FileInputStream(
				srcFileName);
		int length = stream.available();
		byte[] buffer = new byte[length];
		//
		stream.read(buffer);
		stream.close();
		//
		java.io.FileOutputStream out = new java.io.FileOutputStream(
				destFileName);
		out.write(buffer);
		out.flush();
		out.close();
	}

	public static void copyFile(InputStream is, OutputStream out)
			throws FileNotFoundException, IOException {
		BufferedInputStream brAtt = null;
		try {
			byte[] byteData = new byte[1024];
			brAtt = new BufferedInputStream(is);
			int len = 0;
			while ((len = brAtt.read(byteData)) != -1) {
				out.write(byteData, 0, len);
			}
		} finally {
			brAtt.close();
		}
	}

	public static boolean saveFile(InputStream inStream, String destFileName)
			throws FileNotFoundException, IOException {
		java.io.OutputStream outPuts = null;
		BufferedInputStream brAtt = null;
		File attFile = new File(destFileName);
		try {
			outPuts = new FileOutputStream(attFile);
			byte[] byteData = new byte[1024];
			brAtt = new BufferedInputStream(inStream);
			int len = 0;
			while ((len = brAtt.read(byteData)) != -1) {
				outPuts.write(byteData, 0, len);
			}
			return true;

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			outPuts.close();
		}
		return false;
	}

	// 转移指定文件
	public static void moveSpecificFiles(String oldFile, String newFile)
			throws FileNotFoundException, IOException {

		try {
			File oldfile = new File(oldFile);
			File newfile = new File(newFile);
			oldfile.renameTo(newfile);

		} catch (IllegalStateException e) {
			e.printStackTrace();
		}

	}

	public static void moveFile(String srcFileName, String destFileName)
			throws FileNotFoundException, IOException {
		copyFile(srcFileName, destFileName);
		deleteFile(srcFileName);
	}

	// 文件查找
	public static boolean fileExists(String fileName) {
		try {
			String path = extractFilePath(fileName);// 获取文件路径
			String name = extractFileName(fileName);// 获取文件名
			java.io.File file = new java.io.File(path, name);
			boolean bExists = file.exists();// 判断是否能在path中找到name文件，如果找到返回true否则返回false
			return bExists;
		} catch (Exception err) {
			err.printStackTrace();
			return false;
		}
	}

	//
	public static boolean fileExists(android.content.Context ctx, String name) {
		String[] files = ctx.fileList();
		for (int i = 0; i < files.length; i++) {
			if (files[i].equalsIgnoreCase(name))
				return true;
		}
		return false;
	}

	public static boolean pathExists(String path) {
		try {
			java.io.File myFilePath = new java.io.File(path);
			return myFilePath.exists();
		} catch (Exception err) {
			return false;
		}
	}

	public static void saveTextToFile(String fileName, String text,
			String charset) throws FileNotFoundException, IOException {

		String path = extractFilePath(fileName);
		String name = extractFileName(fileName);
		java.io.File file = new java.io.File(path, name);
		FileOutputStream out = new FileOutputStream(file);
		addUTF8Head(charset, out);
		OutputStreamWriter writer = new OutputStreamWriter(out, charset);
		writer.write(text);
		writer.flush();
		writer.close();
		out.close();
	}

	public static void addUTF8Head(String charset, FileOutputStream out) {
		if (charset.equals("utf-8") || charset.equals("UTF-8")) {
			byte[] b = { (byte) 0xef, (byte) 0xbb, (byte) 0xbf };
			try {
				out.write(b);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	//
	public static String getStorageCardPath() {
		return Environment.getExternalStorageDirectory().getPath();
	}

	// 创建目录
	public static boolean ensurePathExists(String path) {
		java.io.File myFilePath = new java.io.File(pathRemoveBackslash(path));
		if (myFilePath.exists())
			return true;
		//
		synchronized (WizGlobals.class) {
			return myFilePath.mkdirs();
		}
	}

	// 去掉path中的反斜线
	public static String pathRemoveBackslash(String path) {
		if (path == null)
			return path;
		if (path.length() == 0)
			return path;
		//
		char ch = path.charAt(path.length() - 1);
		if (ch == '/' || ch == '\\')
			return path.substring(0, path.length() - 1);
		return path;

	}

	// 在path中添加反斜线
	public static String pathAddBackslash(String path) {
		if (path == null)
			return java.io.File.separator;
		if (path.length() == 0)
			return java.io.File.separator;
		//
		char ch = path.charAt(path.length() - 1);
		if (ch == '/' || ch == '\\')
			return path;
		return path + java.io.File.separator;
	}

	// 返回根目录
	public static String getDataRootPath(Context ctx) {
		try {
			String basePath = getStorageCardPath();
			basePath = pathAddBackslash(basePath) + "wiz";
			ensurePathExists(basePath);
			//
			try {
				String noMediaFileName = basePath + "/.nomedia";
				if (!fileExists(noMediaFileName)) {
					saveTextToFile(noMediaFileName, "", "utf-8");
				}
			} catch (Exception err) {
			}
			//
			return basePath;
		} catch (Exception err) {
			return ctx.getFilesDir().getPath();
		}
	}

	// 返回摘要文件名
	public static String getStringAbstractName(Context ctx,
			String accountUserId, String documentGUID) {
		if (accountUserId == null || accountUserId.length() == 0)
			return "";
		//
		String path = getAbstractPath(ctx, accountUserId) + "txt";
		ensurePathExists(path);
		String txtName = pathAddBackslash(path) + documentGUID + ".txt";

		return txtName;
	}

	// 返回摘要文件名
	public static String getImageAbstractName(Context ctx,
			String accountUserId, String documentGUID) {
		if (accountUserId == null || accountUserId.length() == 0)
			return "";
		//
		String txtName = getAbstractPath(ctx, accountUserId) + "img";
		ensurePathExists(txtName);
		txtName = pathAddBackslash(txtName) + documentGUID + ".jpg";

		return txtName;
	}

	// 返回摘要目录
	public static String getAbstractPath(Context ctx, String accountUserId) {
		if (accountUserId == null || accountUserId.length() == 0)
			return "";
		String abstractPath = getAccountPath(ctx, accountUserId);
		abstractPath = pathAddBackslash(abstractPath) + "abstract";
		ensurePathExists(abstractPath);
		//
		return pathAddBackslash(abstractPath);
	}

	// 返回用户目录
	public static String getAccountPath(Context ctx, String accountUserId) {
		if (accountUserId == null || accountUserId.length() == 0)
			return "";
		//
		String basePath = getDataRootPath(ctx);
		//
		basePath = pathAddBackslash(basePath);
		//
		String accountPath = basePath + accountUserId;

		ensurePathExists(accountPath);
		//
		return pathAddBackslash(accountPath);
	}

	// 加密笔记的目录
	public static String getCertPath(Context ctx, String accountUserId) {
		if (accountUserId == null || accountUserId.length() == 0)
			return "";
		//
		String basePath = getDataRootPath(ctx);
		//
		basePath = pathAddBackslash(basePath);
		//
		String accountPath = basePath + accountUserId;
		accountPath = pathAddBackslash(accountPath);
		//
		String cretPath = accountPath + "cert";
		ensurePathExists(cretPath);
		//
		return pathAddBackslash(cretPath);
	}

	public static String getWizTaskPath(Context ctx) {
		//
		String basePath = getDataRootPath(ctx);
		//
		basePath = pathAddBackslash(basePath);
		//
		String cretPath = basePath + "task";
		ensurePathExists(cretPath);
		//
		return pathAddBackslash(cretPath);
	}

	public static String getWizTaskCurrentPath(Context ctx, String guid) {
		//
		String basePath = getDataRootPath(ctx);
		basePath = pathAddBackslash(basePath);
		//
		String cretPath = basePath + "task";
		cretPath = pathAddBackslash(cretPath);
		//
		String currentPath = cretPath + guid;
		ensurePathExists(currentPath);
		//
		return pathAddBackslash(currentPath);
	}

	// 加密笔记的文件
	public static String getCertFile(Context ctx, String accountUserId,
			String guid) {
		if (isEmptyString(accountUserId))
			return "";
		if (isEmptyString(guid))
			return "";

		String certPath = getCertPath(ctx, accountUserId);
		certPath = pathAddBackslash(certPath);

		String cretFileName = certPath + guid + ".zip";
		//
		return cretFileName;
	}

	// 加密笔记的文件
	public static String getZipFile(Context ctx, String accountUserId,
			String guid) {
		if (isEmptyString(accountUserId))
			return "";
		if (isEmptyString(guid))
			return "";

		String zipFilePath = getAccountPath(ctx, accountUserId);
		zipFilePath = pathAddBackslash(zipFilePath);
		String zipFileName = zipFilePath + guid + ".zip";

		//
		return zipFileName;
	}

	//
	public static void showException(Context ctx, Exception err) {
		Toast.makeText(ctx, err.getMessage(), Toast.LENGTH_LONG).show();
	}

	//
	public static void showMessage(Context ctx, String msg) {
		Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
	}

	public static void showMessage(Context ctx, int strId, boolean longTime) {
		Toast.makeText(ctx, getResourcesString(ctx, strId),
				longTime ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
	}

	public static void showMessage(Context ctx, int strId, String param1) {
		String str = getResourcesString(ctx, strId);
		str = str.replace("%1", param1);
		Toast.makeText(ctx, str, Toast.LENGTH_SHORT).show();
	}

	// 获得guid
	public static String genGUID() {
		UUID uuid = UUID.randomUUID();
		String guid = uuid.toString();
		return guid;
	}

	//
	// java代码和html代码转换标准
	public static String text2HtmlCore(String text) {
		String html = text;
		//
		html = html.replace("\r", "");
		html = html.replace("&", "&amp;");
		html = html.replace("<", "&lt;");
		html = html.replace(">", "&gt;");
		html = html.replace("\n", "<br />\n");
		html = html.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
		//
		//
		return html;
	}

	// java代码转换成html代码
	public static String text2Html(String text, String title) {
		String html = text;
		//
		html = text2HtmlCore(html);
		//
		html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /><title>"
				+ title + "</title></head><body>" + html + "</body></html>";
		//
		return html;
	}

	// image添加到html中(html仅是字段，不是文件)
	public static String image2Html(String filename, String text, String title) {
		if (title == null)
			title = "";
		//
		if (text == null || text.length() == 0) {
			String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /><title>"
					+ title
					+ "</title></head><body><img src=\""
					+ filename
					+ "\"></img></body></html>";
			return html;
		} else {
			String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /><title>"
					+ title
					+ "</title></head><body><p>"
					+ text2HtmlCore(text)
					+ "</p><img src=\"" + filename + "\"></img></body></html>";
			return html;
		}
		//
	}

	// image组添加到html中(html仅是字段，不是文件)
	public static String noteImages2Html(ArrayList<WizAttachment> fileArray,
			String text, String title) {
		if (title == null)
			title = "";

		String html;
		//
		if (text == null || text.length() == 0) {
			html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /><title>"
					+ title + "</title></head><body>";
		} else {
			html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /><title>"
					+ title
					+ "</title></head><body><p>"
					+ text2HtmlCore(text)
					+ "</p>";
		}
		if (fileArray != null && fileArray.size() > 0) {
			for (int i = 0; i < fileArray.size(); i++) {

				html = html + /* fileArray.get(i).description + "</p>" + */
				"<img src=\"index_files/" + fileArray.get(i).name
						+ "\"></img></p>";
			}
		}

		return html + "</body></html>";
		//
	}

	// image添加到html中(html仅是字段，不是文件)
	public static String voice2Html(String filename, String text, String title) {
		if (title == null)
			title = "";
		//
		if (text == null || text.length() == 0) {
			String html = "<html><head><title>" + title
					+ "</title></head><body><img src=\"file://" + filename
					+ "\"></img></body></html>";
			return html;
		} else {
			String html = "<html><head><title>" + title
					+ "</title></head><body><p>" + text2HtmlCore(text)
					+ "</p><img src=\"file://" + filename
					+ "\"></img></body></html>";
			return html;
		}
		//
	}

	// 删除文件
	public static void deleteFile(String filename) {
		if (fileExists(filename)) {
			java.io.File file = new java.io.File(filename);
			//
			file.delete();
		}
	}

	public static void deleteEncryption(Context ctx, String userId, String guid) {
		WizGlobals.deleteEncryptionFile(ctx, userId, guid);
		// WizTemp.deleteAbstract(ctx, userId, guid);
	}

	// 删除文件
	public static void deleteEncryptionFile(Context ctx, String userId,
			String guid) {
		String docPath = WizIndex.getDocumentFilePathEx(ctx, userId, guid,
				false);
		if (!pathExists(docPath))
			return;

		docPath = pathAddBackslash(docPath);
		// 删除index_files目录
		String index_files = docPath + "index_files";
		File index_file = new File(index_files);
		if (fileExists(index_files) && index_file.isDirectory())
			deleteDirectory(index_files);
		// 删除index.html
		String index = docPath + "index.html";
		deleteFile(index);
		// 删除index.html
		String wiz_abstract = docPath + "wiz_abstract.html";
		deleteFile(wiz_abstract);
		// 删除index.html
		String wiz_full = docPath + "wiz_full.html";
		deleteFile(wiz_full);

		// 删除index.html
		String wiz_mobile = docPath + "wiz_mobile.html";
		deleteFile(wiz_mobile);

		// 删除index.html
		String meta = docPath + "meta.xml";
		deleteFile(meta);
	}

	/*
	 * public static boolean isNetworkAvailable(android.content.Context context)
	 * { android.net.ConnectivityManager cm = (android.net.ConnectivityManager)
	 * context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
	 * android.net.NetworkInfo info = cm.getActiveNetworkInfo();
	 * 
	 * return (info != null && info.isConnected()); }
	 * 
	 * public static boolean isWifiConnected(android.content.Context context) {
	 * return getNetworkState(context,
	 * android.net.ConnectivityManager.TYPE_WIFI) ==
	 * android.net.NetworkInfo.State.CONNECTED; }
	 * 
	 * public static boolean isMobileConnected(android.content.Context context)
	 * { return getNetworkState(context,
	 * android.net.ConnectivityManager.TYPE_MOBILE) ==
	 * android.net.NetworkInfo.State.CONNECTED; }
	 * 
	 * private static android.net.NetworkInfo.State
	 * getNetworkState(android.content.Context context, int networkType) {
	 * android.net.ConnectivityManager cm = (android.net.ConnectivityManager)
	 * context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
	 * android.net.NetworkInfo info = cm.getNetworkInfo(networkType);
	 * 
	 * return info == null ? null : info.getState(); }
	 */
	public static void saveDataToFile(String fileName, byte[] data)
			throws FileNotFoundException, IOException {
		String path = extractFilePath(fileName);
		String name = extractFileName(fileName);
		java.io.File file = new java.io.File(path, name);
		FileOutputStream out = new FileOutputStream(file);
		out.write(data);
		out.flush();
		out.close();
	}

	// 解压笔记
	public static boolean unZipDocumentDataInCret(Context mContext,
			String mAccountUserId, String guid, String data) {
		if (unZipDocumentData(mContext, mAccountUserId, guid, data)) {
			deleteFile(data);
			WizIndex.updateDocument(mContext, mAccountUserId, guid);
		} else {
			return false;
		}
		return true;
	}

	// 解压笔记
	public static boolean unZipDocumentData(Context mContext,
			String mAccountUserId, String dataGuid, String data) {
		File file = new File(data);
		return unZipDocumentData(mContext, mAccountUserId, file, dataGuid);
	}

	// 解压笔记
	public static boolean unZipDocumentData(Context mContext,
			String mAccountUserId, File data, String guid) {
		String dataPath = WizGlobals.getWizTaskCurrentPath(mContext, guid);
		try {
			if (data != null) {
				WizGlobals.unZipByApache(data, dataPath, null);
				return true;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	// 解压附件
	static public boolean unZipAttachmentData(Context mContext,
			String mAccountUserId, File data, WizAttachment att) {
		WizIndex index = new WizIndex(mContext, mAccountUserId);
		try {
			String dataPath = WizIndex.getDocumentFilePath(mContext,
					mAccountUserId, att.docGuid);

			if (data != null) {

				WizGlobals.unZipByApache(data, dataPath, att);
			}

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			index.closeDatabase();
		}
	}

	/*
	 * 解压ZIP文件 *使用此解压缩文件时其中的文件名不能为中文
	 */
	public static void unzip(java.util.zip.ZipInputStream in,
			String outputDirectory) throws Exception {
		WizGlobals.ensurePathExists(outputDirectory);
		outputDirectory = WizGlobals.pathAddBackslash(outputDirectory);
		//
		java.util.zip.ZipEntry z;
		while ((z = in.getNextEntry()) != null) {
			System.out.println("unziping " + z.getName());
			if (z.isDirectory()) {
				String name = z.getName();
				WizGlobals.ensurePathExists(outputDirectory + name);
			} else {
				String fileName = outputDirectory + z.getName();
				WizGlobals.ensurePathExists(WizGlobals
						.extractFilePath(fileName));

				java.io.File f = new java.io.File(fileName);
				f.createNewFile();
				java.io.FileOutputStream out = new java.io.FileOutputStream(f);
				//
				byte[] buffer = new byte[1024];
				while (true) {
					int read = in.read(buffer);
					if (read == -1)
						break;
					//
					out.write(buffer, 0, read);
				}
				out.close();
			}
		}
	}

	/**
	 * 使用 org.apache.tools.zip.ZipFile 解压文件，它与 java 类库中的 java.util.zip.ZipFile
	 * 使用方式是一新的，只不过多了设置编码方式的 接口。
	 * 
	 * 注，apache 没有提供 ZipInputStream 类，所以只能使用它提供的ZipFile 来读取压缩文件。
	 * 
	 * @param archive
	 *            压缩包路径
	 * @param decompressDir
	 *            解压路径
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws ZipException
	 */

	// archive=一个文件的路径+文件名
	// public static void unZipByApache(String archive, String decompressDir)
	@SuppressWarnings("rawtypes")
	public static void unZipByApache(File archive, String decompressDir,
			WizAttachment wizAtt) throws IOException, FileNotFoundException,
			ZipException {
		BufferedInputStream bi;
		ZipFile zf = new ZipFile(archive, "GBK");// 支持中文

		Enumeration e = zf.getEntries();
		while (e.hasMoreElements()) {
			ZipEntry ze2 = (ZipEntry) e.nextElement();
			String entryName = null;
			if (wizAtt == null) {
				entryName = ze2.getName();
			} else {
				entryName = wizAtt.name;
			}
			String path = decompressDir + "/" + entryName;
			if (ze2.isDirectory()) {
				File decompressDirFile = new File(path);
				if (!decompressDirFile.exists()) {
					decompressDirFile.mkdirs();
				}
			} else {
				String fileDir = path.substring(0, path.lastIndexOf("/"));
				File fileDirFile = new File(fileDir);
				if (!fileDirFile.exists()) {
					fileDirFile.mkdirs();
				}
				BufferedOutputStream bos = new BufferedOutputStream(
						new FileOutputStream(decompressDir + "/" + entryName));
				bi = new BufferedInputStream(zf.getInputStream(ze2));
				byte[] readContent = new byte[1024];
				int readCount = bi.read(readContent);
				while (readCount != -1) {
					bos.write(readContent, 0, readCount);
					readCount = bi.read(readContent);
				}
				bos.close();
			}
		}
		zf.close();
	}

	/*
	 * 使用Apache进行压缩 srcFile指向未压缩的文件 zipFile指向已压缩的文件
	 */
	public static void ZipByApache(File srcFile, File zipFile) {
		ZipOutputStream out = null;
		boolean boo = false;// 是否压缩成功
		try {
			CheckedOutputStream cos = new CheckedOutputStream(
					new FileOutputStream(zipFile), new CRC32());
			out = new ZipOutputStream(cos);
			File files[] = srcFile.listFiles();
			if (files != null && files.length > 0) {
				for (int i = 0; i < files.length; i++) {

					Zip(files[i], out, "", true);
				}
			}
			boo = true;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (IOException ex) {
				throw new RuntimeException("关闭Zip输出流出现异常", ex);
			} finally {
				// 清理操作
				if (!boo && zipFile.exists())// 压缩不成功,
					zipFile.delete();
			}
		}
	}

	/**
	 * 压缩zip文件
	 * 
	 * @param file
	 *            压缩的文件对象
	 * @param out
	 *            输出ZIP流
	 * @param dir
	 *            相对父目录名称
	 * @param boo
	 *            是否把空目录压缩进去
	 */
	public static void Zip(File file, ZipOutputStream out, String dir,
			boolean boo) throws IOException {
		if (file.isDirectory()) {// 是目录
			File[] listFile = file.listFiles();// 得出目录下所有的文件对象
			if (listFile.length == 0 && boo) {// 空目录压缩
				out.putNextEntry(new ZipEntry(dir + file.getName() + "/"));// 将实体放入输出ZIP流中
				System.out.println("压缩." + dir + file.getName() + "/");
				return;
			} else {
				for (File cfile : listFile) {
					Zip(cfile, out, dir + file.getName() + "/", boo);// 递归压缩
				}
			}
		} else if (file.isFile()) {// 是文件
			System.out.println("压缩." + dir + file.getName() + "/");
			byte[] bt = new byte[2048 * 2];
			ZipEntry ze = new ZipEntry(dir + file.getName());// 构建压缩实体
			// 设置压缩前的文件大小
			ze.setSize(file.length());
			out.putNextEntry(ze);// //将实体放入输出ZIP流中
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);
				int i = 0;
				while ((i = fis.read(bt)) != -1) {// 循环读出并写入输出Zip流中
					out.write(bt, 0, i);
				}
			} catch (IOException ex) {
				// throw new IOException("写入压缩文件出现异常",ex);
				throw new IOException("写入压缩文件出现异常");
			} finally {
				try {
					if (fis != null)
						fis.close();// 关闭输入流
				} catch (IOException ex) {
					throw new IOException("关闭输入流出现异常");
				}
			}
		}
	}

	public static Dialog createAlertDialog(Context ctx, int titleId,
			String[] itemsArray, int index,
			android.content.DialogInterface.OnClickListener onSelect,
			android.content.DialogInterface.OnClickListener onOKClick,
			android.content.DialogInterface.OnClickListener onCancelClick) {

		return new AlertDialog.Builder(ctx)
				.setTitle(getResourcesString(ctx, titleId))
				.setSingleChoiceItems(itemsArray, index, onSelect)
				// .setPositiveButton(R.string.alert_dialog_ok, onOKClick)
				.setNegativeButton(R.string.cancel, onCancelClick).create();
	}

	public static Dialog createAlertDialog(Context ctx, int titleId, View view,
			String[] itemsArray, boolean[] flags,
			DialogInterface.OnMultiChoiceClickListener onSelect,
			android.content.DialogInterface.OnClickListener onOKClick,
			android.content.DialogInterface.OnClickListener onCancelClick) {

		return new AlertDialog.Builder(ctx)
				.setTitle(getResourcesString(ctx, titleId)).setView(view)
				.setMultiChoiceItems(itemsArray, flags, onSelect)
				.setPositiveButton(R.string.ok, onOKClick)
				.setNegativeButton(R.string.cancel, onCancelClick).create();
	}

	public static Dialog createAlertDialog(Context ctx, int titleId,
			String message, String onOKtext, String onCancelText,
			android.content.DialogInterface.OnClickListener onOKClick,
			android.content.DialogInterface.OnClickListener onCancelClick) {
		return new AlertDialog.Builder(ctx)
				.setIcon(R.drawable.alert_dialog_icon)
				.setTitle(getResourcesString(ctx, titleId)).setMessage(message)
				.setPositiveButton(onOKtext, onOKClick)
				.setNegativeButton(onCancelText, onCancelClick).create();
	}

	public static Dialog createAlertDialog(Context ctx, int titleId,
			String message,
			android.content.DialogInterface.OnClickListener onOKClick,
			int onOkId,
			android.content.DialogInterface.OnClickListener onCancelClick,
			int onCancelId) {
		return new AlertDialog.Builder(ctx)
				.setIcon(R.drawable.alert_dialog_icon)
				.setTitle(getResourcesString(ctx, titleId))
				.setMessage(message)
				.setPositiveButton(onOkId == 0 ? R.string.ok : onOkId,
						onOKClick)
				.setNegativeButton(
						onCancelId == 0 ? R.string.cancel : onCancelId,
						onCancelClick).create();
	}

	//
	// public static Dialog createAlertDialog(Context ctx, int titleId,
	// String message,
	// android.content.DialogInterface.OnClickListener onOKClick,
	// android.content.DialogInterface.OnClickListener onCancelClick) {
	// return new AlertDialog.Builder(ctx)
	// .setIcon(R.drawable.alert_dialog_icon)
	// .setTitle(getResourcesString(ctx, titleId)).setMessage(message)
	// .setPositiveButton(R.string.ok, onOKClick)
	// .setNegativeButton(R.string.cancel, onCancelClick)
	// .create();
	// }

	public static Dialog createAlertDialog(Context ctx, int titleId,
			String message,
			android.content.DialogInterface.OnClickListener onOKClick) {
		return new AlertDialog.Builder(ctx)
				.setIcon(R.drawable.alert_dialog_icon)
				.setTitle(getResourcesString(ctx, titleId)).setMessage(message)
				.setPositiveButton(R.string.ok, onOKClick).create();
	}

	public static Dialog createAlertDialog(Context ctx, int titleId,
			String message,
			android.content.DialogInterface.OnClickListener onOKClick,
			boolean isOk) {
		return new AlertDialog.Builder(ctx)
				.setIcon(R.drawable.alert_dialog_icon)
				.setTitle(getResourcesString(ctx, titleId))
				.setMessage(message)
				.setPositiveButton(isOk ? R.string.ok : R.string.dialog_close,
						onOKClick).create();
	}

	public static Dialog createAlertDialog(Context ctx, int titleId,
			boolean showIcon, String message,
			android.content.DialogInterface.OnClickListener onOKClick) {
		if (showIcon) {

			return new AlertDialog.Builder(ctx)
					.setIcon(R.drawable.alert_dialog_icon)
					.setTitle(getResourcesString(ctx, titleId))
					.setMessage(message)
					.setPositiveButton(R.string.dialog_close, onOKClick)
					.create();
		} else {

			return new AlertDialog.Builder(ctx)
					.setTitle(getResourcesString(ctx, titleId))
					.setMessage(message)
					.setPositiveButton(R.string.dialog_close, onOKClick)
					.create();
		}
	}

	public static Dialog createAlertDialog(Context ctx, int titleId, View view,
			android.content.DialogInterface.OnClickListener onOKClick,
			android.content.DialogInterface.OnClickListener onCancelClick) {
		return new AlertDialog.Builder(ctx)
				.setIcon(R.drawable.alert_dialog_icon)
				.setTitle(getResourcesString(ctx, titleId)).setView(view)
				.setPositiveButton(R.string.ok, onOKClick)
				.setNegativeButton(R.string.cancel, onCancelClick).create();
	}

	public static Dialog createAlertDialog(Context ctx, int titleId, View view,
			android.content.DialogInterface.OnClickListener onOKClick,
			int okId,
			android.content.DialogInterface.OnClickListener onCancelClick,
			int cancelId) {
		return new AlertDialog.Builder(ctx)
				.setIcon(R.drawable.alert_dialog_icon)
				.setTitle(getResourcesString(ctx, titleId))
				.setView(view)
				.setPositiveButton(okId == 0 ? R.string.ok : okId, onOKClick)
				.setNegativeButton(cancelId == 0 ? R.string.cancel : cancelId,
						onCancelClick).create();
	}

	public static Dialog createAlertDialog(Context ctx, int titleId,
			String message, String okButtonText,
			android.content.DialogInterface.OnClickListener onOKClick,
			String cancelButtonText,
			android.content.DialogInterface.OnClickListener onCancelClick) {
		return new AlertDialog.Builder(ctx)
				.setIcon(R.drawable.alert_dialog_icon)
				.setTitle(getResourcesString(ctx, titleId)).setMessage(message)
				.setPositiveButton(okButtonText, onOKClick)
				.setNegativeButton(cancelButtonText, onCancelClick).create();
	}

	public static Dialog createListDialog(Context ctx, String title,
			String[] list,
			android.content.DialogInterface.OnClickListener onListClick) {
		return new AlertDialog.Builder(ctx).setTitle(title)
				.setItems(list, onListClick).create();
	}

	public static Dialog createListDialog(Context ctx, String title,
			String[] list,
			android.content.DialogInterface.OnClickListener onListClick,
			String cancelButtonText,
			android.content.DialogInterface.OnClickListener onCancelClick) {
		return new AlertDialog.Builder(ctx).setTitle(title)
				.setItems(list, onListClick)
				.setNegativeButton(cancelButtonText, onCancelClick).create();
	}

	public static ProgressDialog createProgressDialog(Context ctx, int titleId,
			int messageId, boolean indeterminate, boolean cancelable) {
		return createProgressDialog(ctx, titleId,
				ctx.getResources().getString(messageId), indeterminate,
				cancelable);
	}

	public static ProgressDialog createProgressDialog(Context ctx, int titleId,
			String message, int buttonText,
			android.content.DialogInterface.OnClickListener onCancelClick,
			boolean indeterminate, boolean cancelable) {
		if (message == null || message.length() == 0) {
			message = "...";
		}
		return createProgressDialog(ctx, titleId, message, ctx.getResources()
				.getString(buttonText), onCancelClick, indeterminate,
				cancelable);
	}

	public static ProgressDialog createProgressDialog(Context ctx, int titleId,
			String message, boolean indeterminate, boolean cancelable) {
		ProgressDialog dialog = new ProgressDialog(ctx);
		dialog.setTitle(getResourcesString(ctx, titleId));
		dialog.setMessage(message);
		dialog.setIndeterminate(indeterminate);
		dialog.setCancelable(cancelable);
		return dialog;
	}

	public static ProgressDialog createProgressDialog(Context ctx, int titleId,
			String message, String cancelButtonText,
			android.content.DialogInterface.OnClickListener onCancelClick,
			boolean indeterminate, boolean cancelable) {
		ProgressDialog dialog = new ProgressDialog(ctx);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setMessage(message);

		/*
		 * 将来会修改这几项
		 */
		dialog.setTitle(getResourcesString(ctx, titleId));
		dialog.setMessage(message);
		dialog.setIndeterminate(indeterminate);
		// dialog.setCancelable(cancelable);
		dialog.setButton(cancelButtonText, onCancelClick);
		return dialog;
	}

	static public boolean isWifi(Context ctx) {
		android.net.ConnectivityManager connectivity = (android.net.ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {

			android.net.NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getTypeName().equals("WIFI")
							&& info[i].isConnected()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	// ?
	static public boolean isCMWAP(Context ctx) {
		android.net.ConnectivityManager cm = (android.net.ConnectivityManager) ctx
				.getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
		android.net.NetworkInfo info = cm.getActiveNetworkInfo();

		if (info != null && info.getTypeName().equals("MOBILE")
				&& info.getExtraInfo().equals("cmwap")) {
			return true;
		}
		return false;
	}

	//
	static public boolean isCMWAPNetwork(Context ctx) {

		android.net.ConnectivityManager cwjManager = (android.net.ConnectivityManager) ctx
				.getSystemService(android.content.Context.CONNECTIVITY_SERVICE);

		if (cwjManager != null && cwjManager.getActiveNetworkInfo() != null)
			return cwjManager.getActiveNetworkInfo().isAvailable();

		return false;
	}

	// 缩放图片
	public static Bitmap resizeBitmap(Bitmap bmp, int newWidth, int newHeight) {
		if (newWidth <= 0 || newHeight <= 0) {
			return bmp;
		}
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		// 设置想要的大小
		// 计算缩放比例
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// 取得想要缩放的matrix参数
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		// 得到新的图片
		Bitmap newBmp = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix,
				true);
		return newBmp;
	}

	public static long getFileSize(String fileName) {
		try {
			java.io.File file = new java.io.File(fileName);
			return file.length();
		} catch (Exception e) {
			return 0;
		}
	}

	public static void WriteLog(Context ctx, String log) {
		try {
			String logFileName = getDataRootPath(ctx) + "/wiz_log.txt";
			long fileSize = getFileSize(logFileName);
			if (fileSize > 1024 * 1024) {
				deleteFile(logFileName);
			}
			//
			FileWriter writer = new FileWriter(logFileName, true);
			try {
				writer.write("\n");
				writer.write((new Date()).toLocaleString());
				writer.write(":\t");
				writer.write(log);
			} finally {
				writer.close();
			}
		} catch (Exception err) {

		}
	}

	/**
	 * 删除目录（文件夹）以及目录下的文件
	 * 
	 * @param dir
	 *            被删除目录的文件路径
	 * @return 目录删除成功返回true,否则返回false
	 */
	public static boolean deleteDirectory(String dir) {
		if (!pathExists(dir))
			return true;
		//
		// 如果dir不以文件分隔符结尾，自动添加文件分隔符
		if (!dir.endsWith(java.io.File.separator)) {
			dir = dir + java.io.File.separator;
		}
		java.io.File dirFile = new java.io.File(dir);
		// 如果dir对应的文件不存在，或者不是一个目录，则退出
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			return false;
		}
		// 删除文件夹下的所有文件(包括子目录)
		java.io.File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			// 删除子文件
			if (files[i].isFile()) {
				deleteFile(files[i].getAbsolutePath());
			}
			// 删除子目录
			else {
				deleteDirectory(files[i].getAbsolutePath());
			}
		}

		// 删除当前目录
		return dirFile.delete();
	}

	public static boolean deleteIndexFiles(Context ctx, String userId,
			String guid) {
		String imageIndexFilesPath = WizIndex.getDocumentImagePathEx(ctx,
				userId, guid, false);
		if (!pathExists(imageIndexFilesPath))
			return true;

		ArrayList<WizAttachment> arr = WizIndex.getAttachmentsByDocGuid(ctx,
				userId, guid);

		if (arr == null || arr.size() <= 0)
			return deleteDirectory(imageIndexFilesPath);

		ArrayList<String> arrStr = new ArrayList<String>();

		imageIndexFilesPath = pathAddBackslash(imageIndexFilesPath);
		for (int i = 0; i < arr.size(); i++) {
			arrStr.add(imageIndexFilesPath + arr.get(i).name);
		}

		if (arrStr == null || arrStr.size() <= 0)
			return deleteDirectory(imageIndexFilesPath);

		File files[] = (new File(imageIndexFilesPath)).listFiles();
		if (files == null || files.length <= 0)
			return true;

		String file = "";
		for (int i = 0; i < files.length; i++) {
			file = files[i].getName();
			if (existsFile(file, arrStr)) {
				continue;
			}
			try {
				files[i].delete();
			} catch (Exception e) {
			}
		}
		// 删除当前目录
		return true;
	}

	public static boolean existsFile(String str, ArrayList<String> arr) {

		if (arr == null || arr.size() <= 0)
			return false;
		for (int i = 0; i < arr.size(); i++)
			if (str.equals(arr.get(i)))
				return true;

		return false;
	}

	// 获取当前时间
	public static String getNowTime() {
		Date nowTime = new Date();
		// int Year = nowTime.getYear() + 1900;
		// int Month = nowTime.getMonth() + 1;
		// int Day = nowTime.getDate();
		int Hour = nowTime.getHours();
		int Minute = nowTime.getMinutes();
		int Second = nowTime.getSeconds();

		String time = DateFormat.getDateInstance(DateFormat.DEFAULT).format(
				nowTime);
		time = time + " " + Dateint2String(Hour) + ":" + Dateint2String(Minute)
				+ ":" + Dateint2String(Second);
		return time;

	}

	// 将日期中除了表示年以外的数字转化成占两个字节的String字符
	static String Dateint2String(int time) {
		if (time < 10) {
			return 0 + String.valueOf(time);
		} else {
			return String.valueOf(time);
		}

	}

	public static final String mWizPublicTagName = "$public-documents$";
	public static final String mWizMyNotesName = "My Notes";
	public static final String mWizMyDraftsName = "My Drafts";
	public static final String mWizMyTasksName = "My Tasks";
	public static final String mWizMyMobilesName = "My Mobiles";
	public static final String mWizDeleteItems = "Deleted Items";
	public static final String mWizMyEventsName = "My Events";
	public static final String mWizMyJournalsName = "My Journals";
	public static final String mWizCompletedName = "Completed";
	public static final String mWizInboxName = "Inbox";

	public static String getAlias(Context ctx, String showName) {
		// android.content.res.Resources res = ctx.getResources();
		if (showName.equals(mWizMyNotesName))
			return getResourcesString(ctx, R.string.key_my_notes);
		else if (showName.equals(mWizMyDraftsName))
			return getResourcesString(ctx, R.string.key_my_drafts);
		else if (showName.equals(mWizMyTasksName))
			return getResourcesString(ctx, R.string.key_my_tasks);
		else if (showName.equals(mWizMyMobilesName))
			return getResourcesString(ctx, R.string.key_my_mobiles);
		else if (showName.equals(mWizDeleteItems))
			return getResourcesString(ctx, R.string.key_delete_items);
		else if (showName.equals(mWizMyEventsName))
			return getResourcesString(ctx, R.string.key_my_events);
		else if (showName.equals(mWizMyJournalsName))
			return getResourcesString(ctx, R.string.key_my_journals);
		else if (showName.equals(mWizCompletedName))
			return getResourcesString(ctx, R.string.key_completed);
		else if (showName.equals(mWizInboxName))
			return getResourcesString(ctx, R.string.key_inbox);
		return showName;
	}

	static ArrayList<WizLocation> getLocationObject(Context ctx,
			ArrayList<String> locations, int parentLevel) {
		String outlineTitle;
		WizLocation mCurrentLocation;
		ArrayList<WizLocation> mLocations = new ArrayList<WizLocation>();
		for (int i = 0; i < locations.size(); i++) {
			String subLocation = locations.get(i);
			String[] filePath;

			filePath = getFilePath(subLocation);

			if (filePath.length == parentLevel + 2
					|| filePath[parentLevel + 2] == null
					|| filePath[parentLevel + 2] == ""
					|| filePath[parentLevel + 2].length() == 0) {
				continue;
			}
			outlineTitle = filePath[parentLevel + 1 + 1];
			mCurrentLocation = new WizLocation();
			mCurrentLocation.name = outlineTitle;
			mCurrentLocation.rName = getAlias(ctx, outlineTitle);
			mCurrentLocation.mCount = 0;
			mLocations.add(mCurrentLocation);
		}
		Collections.sort(mLocations);
		return mLocations;
	}

	public final static int DOCUMENT_ORDER_BY_DATE = -1;
	public final static int DOCUMENT_ORDER_BY_TITLE = 0;

	static ArrayList<WizDocument> getDocumentsObject(
			ArrayList<WizDocument> docArray, int orderBy) {
		switch (orderBy) {
		case DOCUMENT_ORDER_BY_DATE:
			break;
		case DOCUMENT_ORDER_BY_TITLE:
			Collections.sort(docArray);
			break;
		}
		return docArray;
	}

	public static String[] getResourcesStringArray(Context ctx, int arrId) {
		return ctx.getResources().getStringArray(arrId);
	}

	public static String getResourcesString(Context ctx, int strId) {
		return ctx.getResources().getString(strId);
	}

	public static Bitmap getResourcesBitmap(Context ctx, int iconId) {
		return BitmapFactory.decodeResource(ctx.getResources(), iconId);
	}

	public static Bitmap getResourcesBitmap(String name) {
		return BitmapFactory.decodeFile(name);
	}

	public static Drawable getResourcesDrawable(Context ctx, int iconId) {
		return ctx.getResources().getDrawable(iconId);
	}

	public static String getErrorMessage(Context ctx, int strId,
			String errorMessage) {
		if (strId == -1) {
			return "";
		} else if (strId == 0) {
			return errorMessage;
		} else {
			try {
				return getResourcesString(ctx, strId);
			} catch (Exception e) {
				return "";
			}
		}
	}

	// 判断sd Card是否插入
	public static boolean issdCardExit() {
		return Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
	}

	// 开启录音播放程序
	public static Intent getAudioIntent(File f) {

		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);

		intent.setDataAndType(Uri.fromFile(f), "audio");

		return intent;
	}

	public static final String ATTTYPE_JPG = "JPG";
	public static final String ATTTYPE_MP3 = "MP3";
	public static final String ATTTYPE_RMVB = "RMVB";
	public static final String ATTTYPE_WORD = "WORD";
	public static final String ATTTYPE_EXECL = "EXECL";
	public static final String ATTTYPE_PPT = "PPT";

	public static String getFileType(String mAttType) {
		if (mAttType.equals(".JPG") || mAttType.equals(".jpg")
				|| mAttType.equals(".png") || mAttType.equals(".JPEG")
				|| mAttType.equals(".jpeg") || mAttType.equals(".gif")) {
			return ATTTYPE_JPG;
		} else if (mAttType.equals(".amr") || mAttType.equals(".mp3")
				|| mAttType.equals(".wav")) {
			return ATTTYPE_MP3;
		} else if (mAttType.equals(".avi") || mAttType.equals(".rmvb")
				|| mAttType.equals(".mp4")) {
			return ATTTYPE_RMVB;
		} else if (mAttType.equals(".doc") || mAttType.equals(".docx")
				|| mAttType.equals(".mp4")) {
			return ATTTYPE_WORD;
		} else if (mAttType.equals(".xls") || mAttType.equals(".xlsx")) {
			return ATTTYPE_EXECL;
		} else if (mAttType.equals(".ppt") || mAttType.equals(".pptx")) {
			return ATTTYPE_PPT;
		} else {
			return "";
		}

	}

	// 获取String类型数组
	public static String[] getStringArray(String str) {
		String[] mCurrentStringArray = null;
		if (str.indexOf(" ") != -1) {

			mCurrentStringArray = str.split(" ");
		} else if (str.indexOf("'") != -1) {

			mCurrentStringArray = str.split("'");
		} else if (str.indexOf("‘") != -1) {

			mCurrentStringArray = str.split("‘");
		} else if (str.indexOf("’") != -1) {

			mCurrentStringArray = str.split("’");
		} else if (str.indexOf("“") != -1) {

			mCurrentStringArray = str.split("“");
		} else if (str.indexOf("”") != -1) {

			mCurrentStringArray = str.split("”");
		} else {

			mCurrentStringArray = new String[] { str };
		}
		return mCurrentStringArray;
	}

	public static final int DIALOG_SEARCH_MENU_TODAY = 0;
	public static final int DIALOG_SEARCH_MENU_YESTERDAY = 1;
	public static final int DIALOG_SEARCH_MENU_BEFORE_YESTERDAY = 2;
	public static final int DIALOG_SEARCH_MENU_A_WEEK = 3;
	public static final int DIALOG_SEARCH_MENU_TWO_WEEK = 4;
	public static final int DIALOG_SEARCH_MENU_A_MONTH = 5;

	public static ArrayList<WizDocument> searchAction(Context ctx,
			String userId, int witch) {
		switch (witch) {
		case DIALOG_SEARCH_MENU_TODAY:
			return WizIndex.getDocumentsByDay(ctx, userId, 0);

		case DIALOG_SEARCH_MENU_YESTERDAY:
			return WizIndex.getDocumentsByDay(ctx, userId, 1);

		case DIALOG_SEARCH_MENU_BEFORE_YESTERDAY:
			return WizIndex.getDocumentsByDay(ctx, userId, 2);

		case DIALOG_SEARCH_MENU_A_WEEK:
			return WizIndex.getDocumentsByWeek(ctx, userId, 1);

		case DIALOG_SEARCH_MENU_TWO_WEEK:
			return WizIndex.getDocumentsByWeek(ctx, userId, 2);

		case DIALOG_SEARCH_MENU_A_MONTH:
			return WizIndex.getDocumentsByMonth(ctx, userId, 1);

		default:
			return WizIndex.getRecentDocuments(ctx, userId);
		}
	}

	public static String getOpenFileType(String file_CanonicalPath) {
		return MimeTypeMap.getSingleton().getMimeTypeFromExtension(
				MimeTypeMap.getFileExtensionFromUrl(file_CanonicalPath));
	}

	public static boolean isEncryptFile(File zipFile) {
		FileInputStream in = null;
		try {
			in = new FileInputStream(zipFile);
			byte[] part_data = new byte[4];
			in.read(part_data);
			if (part_data != null) {// 90 73 87 82
				if (part_data[0] == 90 && part_data[1] == 73
						&& part_data[2] == 87 && part_data[3] == 82) {
					return true;
				}
			}
		} catch (Exception e) {
		} finally {
			try {
				in.close();
			} catch (IOException e) {
			}
		}
		return false;
	}

	public static boolean isEncryptFile(byte[] partData) {
		if (partData != null && partData[0] == 90 && partData[1] == 73
				&& partData[2] == 87 && partData[3] == 82)
			return true;

		return false;
	}

	public static Bitmap getResizeBmp(Bitmap bmp, int i, boolean setHeight) {
		if (bmp != null) {
			if (bmp.getWidth() > i || bmp.getHeight() > i) {// 缩放图片
				int width = bmp.getWidth();
				int height = bmp.getHeight();
				double fWidth = width / i;
				double fHeight = height / i;
				double fRate = Math.max(fWidth, fHeight);
				if (setHeight)
					fRate = fHeight;
				int newWidth = (int) (width / fRate);
				int newHeight = (int) (height / fRate);
				//
				if (setHeight)
					return resizeBitmap(bmp, newWidth, i);
				return resizeBitmap(bmp, newWidth, newHeight);
			}
		}
		return bmp;
	}

	public static final String WIZ_ABSTRACT_TYPE_PHONE = "Phone";
	public static final String WIZ_ABSTRACT_TYPE_PAD = "Pad";

	// public static WizAbstract readAbstract(Context ctx, String userId,
	// String documentGUID, String type) {
	// WizAbstract mCurrentAbstract = WizTemp.getAbstract(ctx, userId,
	// documentGUID, type);
	//
	// if (mCurrentAbstract != null)
	// return mCurrentAbstract;
	//
	// mCurrentAbstract = iniAbstract(ctx, userId, documentGUID, type);
	//
	// if (isAbstract(mCurrentAbstract))
	// WizTemp.upDateAbstract(ctx, userId, documentGUID, type,
	// mCurrentAbstract);
	//
	// return mCurrentAbstract;
	//
	// }

	public static boolean isAbstract(WizAbstract mCurrentAbstract) {
		if (mCurrentAbstract == null)
			return false;

		if (mCurrentAbstract.getmAbstractImage() != null)
			return true;
		if (mCurrentAbstract.getmAbstractText() != null
				&& mCurrentAbstract.getmAbstractText().length() > 0)
			return true;

		return false;
	}

	public static WizAbstract iniAbstract(Context ctx, String userId,
			String documentGUID, String type) {
		Bitmap bmp = null;
		String txt = "";

		WizAbstract mCurrentAbstract = new WizAbstract();

		bmp = iniAbstractImage(ctx, userId, documentGUID, type);
		txt = iniAbstractTextString(ctx, userId, documentGUID, type);

		mCurrentAbstract.setmDocumentGuid(documentGUID);
		mCurrentAbstract.setmAbstractImage(bmp);
		mCurrentAbstract.setmAbstractText(txt);
		mCurrentAbstract.setmAbstractType(type);

		return mCurrentAbstract;
	}

	public static boolean isFileUTF16(String str) {
		java.io.InputStream is = null;
		byte[] b = new byte[2];
		try {
			is = new FileInputStream(new File(str));
			is.read(b);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
		} finally {

			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (b[0] == (byte) 0xff && b[1] == (byte) 0xfe)
			return true;
		return false;
	}

	// 获取wizabstract对象的text
	public static String iniAbstractTextString(Context ctx, String userId,
			String documentGUID, String type) {
		String docName = WizIndex.getDocumentAbstractFileName(ctx, userId,
				documentGUID);
		if (docName == null || docName.length() == 0 || !fileExists(docName)) {
			docName = WizIndex.getDocumentFileName(ctx, userId, documentGUID);
		}
		if (docName == null || docName.length() == 0 || !fileExists(docName))
			return "";
		//
		String html = loadTextFromFile(docName);
		//
		String sumStr = "";
		if (type.equals(WIZ_ABSTRACT_TYPE_PHONE))
			sumStr = html2Text(html, ABSTRACT_STRING_LENGTH_PHONE);
		else if (type.equals(WIZ_ABSTRACT_TYPE_PAD))
			sumStr = html2Text(html, ABSTRACT_STRING_LENGTH_PAD);
		return sumStr;
	}

	static public String loadTextFromFile(String docName) {

		try {
			if (isFileUTF16(docName))
				return loadTextFromFile(docName, "UTF-16");
			else
				return loadTextFromFile(docName, "UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	// 摘要的字数长度
	private final static int ABSTRACT_STRING_LENGTH_PAD = 200;
	private final static int ABSTRACT_STRING_LENGTH_PHONE = 100;

	private final static int ABSTRACT_IMAGE_LENGTH_PAD = 150;
	private final static int ABSTRACT_IMAGE_LENGTH_PHONE = 100;

	// 获取wizabstract对象的image
	public static Bitmap iniAbstractImage(Context ctx, String userId,
			String documentGUID, String type) {
		String imagePath = WizIndex.getDocumentFilePath(ctx, userId,
				documentGUID);
		imagePath = pathAddBackslash(imagePath) + "index_files";
		if (pathExists(imagePath)) {
			File[] files = (new File(imagePath)).listFiles();

			if (files != null && files.length > 0) {
				File file = getLargeFile(files);
				if (file == null)
					return null;
				return getAbstractBitmap(file, imagePath, type);
			}
		}
		return null;
	}

	// 获取wizabstract对象的image-Bitmap
	public static Bitmap getAbstractBitmap(File file, String imagePath,
			String type) {
		String fileName = file.getName();
		if (!isEmptyString(getJpegFile(fileName))) {
			Bitmap bmpOrg = getResourcesBitmap(pathAddBackslash(imagePath)
					+ fileName);

			if (isRecycleBmp(bmpOrg))
				return null;

			Bitmap bmpNew = null;
			if (type.equals(WIZ_ABSTRACT_TYPE_PHONE))
				bmpNew = getResizeBmp(bmpOrg, ABSTRACT_IMAGE_LENGTH_PHONE,
						false);
			else if (type.equals(WIZ_ABSTRACT_TYPE_PAD))
				bmpNew = getResizeBmp(bmpOrg, ABSTRACT_IMAGE_LENGTH_PAD, false);

			if (bmpOrg != bmpNew)
				bmpOrg.recycle();

			if (isRecycleBmp(bmpNew))
				return null;

			return bmpNew;
		}
		return null;
	}

	static public boolean isRecycleBmp(Bitmap bmp) {
		if (bmp == null)
			return true;

		int height = bmp.getHeight();
		int widght = bmp.getHeight();
		int i1 = height / widght;
		int i2 = widght / height;

		if (height <= 20 || widght <= 20 || i1 > 10 || i2 > 10) {
			bmp.recycle();
			return true;
		}
		return false;

	}

	// 取得较大的文件
	public static File getLargeFile(File[] files) {
		File file = null;
		for (int i = 0; i < files.length; i++) {
			String name = files[i].getName();
			if (isEmptyString(getJpegFile(name)))
				continue;

			if (file == null || file.length() < files[i].length())
				file = files[i];

		}
		return file;
	}

	static String getJpegFile(String name) {
		String file = null;

		int lastIndexOf = name.lastIndexOf(".");
		if (lastIndexOf <= 0)
			return file;

		String type = name.substring(lastIndexOf);
		if (getFileType(type).equals(ATTTYPE_JPG))
			file = name;

		return file;
	}

	// 去掉html
	public static String html2Text(String input, int length) {

		if (input == null || input.trim().equals("")) {
			return "";
		}
		// 恢复HTML转义字符
		input = HTMLUtil.clean(input);
		// 去掉所有html元素,
		input = HTMLUtil.delAllTag(input);

		input = input.trim();
		if (length <= 0)
			return input;
		//
		int len = input.length();
		if (len <= length) {
			return input;
		} else {
			input = input.substring(0, length);
			input += "......";
		}
		return input;
	}

	private final static int GET_HTML_TEXT = -1;

	// 获取index.html页面的内容text
	public static String iniHtml2Text(Context ctx, String userId,
			String documentGUID) {
		try {
			String docName = WizIndex.getDocumentFileName(ctx, userId,
					documentGUID);
			if (docName == null || docName.length() == 0
					|| !fileExists(docName))
				throw new FileNotFoundException();
			//
			String html = "";

			if (isFileUTF16(docName))
				html = loadTextFromFile(docName, "UTF-16");
			else
				html = loadTextFromFile(docName, "UTF-8");
			//
			String sumStr = html2Text(html, GET_HTML_TEXT);
			return sumStr;
		} catch (FileNotFoundException e) {
			return "";
		} catch (IOException e) {
			return "";
		} catch (Exception e) {
			return "";
		}
	}

	public static byte[] bitmap2ByteArray(Bitmap bmp) {
		java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
		try {
			// 将Bitmap压缩成PNG编码，质量为100%存储
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, os);
			return os.toByteArray();
		} catch (Exception e) {
			return null;
		} finally {
			try {
				os.close();
				// if (bmp != null)
				// bmp.recycle();
			} catch (Exception e1) {
			}
		}
	}

	public static Bitmap byte2Bitmap(byte[] image) {
		if (image != null && image.length > 0)
			return android.graphics.BitmapFactory.decodeByteArray(image, 0,
					image.length);

		return null;

	}

	private static final int screenLength = 1024;

	public static boolean isPhone(android.app.Activity act) {
		boolean isPhone = true;
		int screenHeight = act.getWindowManager().getDefaultDisplay()
				.getHeight();
		int screenWidth = act.getWindowManager().getDefaultDisplay().getWidth();

		isPhone = screenHeight < screenLength && screenWidth < screenLength;

		String str = WizGlobals.isShowLayoutType(act);

		if (str.equals(PREFERENCE_VALUE_DEFAULT_LAYOUT_IPHONE)) {

			isPhone = true;
		} else if (str.equals(PREFERENCE_VALUE_DEFAULT_LAYOUT_PAD)) {

			isPhone = false;
		}

		return isPhone;
	}

	public static final int PREFERENCE_VALUE_TYPE_STRING = 0;
	public static final int PREFERENCE_VALUE_TYPE_BOOLEAN = 1;
	public static final int PREFERENCE_VALUE_TYPE_INT = 2;
	public static final int PREFERENCE_VALUE_TYPE_FLOAT = 3;
	public static final int PREFERENCE_VALUE_TYPE_LONG = 4;

	public static final String PREFERENCE_VALUE_TYPE_BOOLEAN_TRUE = "true";
	public static final String PREFERENCE_VALUE_TYPE_BOOLEAN_FALSE = "false";
	public static final String PREFERENCE_VALUE_DEFAULT_DOWNLOAD = "1";
	public static final String PREFERENCE_VALUE_DEFAULT_READ_NOTE = "0";
	public static final String PREFERENCE_VALUE_DEFAULT_SYSTEM_PASSWORD = "6688";
	public static final String PREFERENCE_VALUE_DEFAULT_LAYOUT_DEFAULT = "0";
	public static final String PREFERENCE_VALUE_DEFAULT_LAYOUT_IPHONE = "1";
	public static final String PREFERENCE_VALUE_DEFAULT_LAYOUT_PAD = "2";
	public static final String PREFERENCE_VALUE_DEFAULT_SYSTEM_NULL = "";

	public static String getSettingMessage(Context ctx, String key, int getId,
			String mDefault) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		switch (getId) {
		case PREFERENCE_VALUE_TYPE_STRING:
			return settings.getString(key, mDefault);

		case PREFERENCE_VALUE_TYPE_BOOLEAN:
			Boolean bool = settings.getBoolean(key,
					mDefault.equals(PREFERENCE_VALUE_TYPE_BOOLEAN_TRUE));
			return bool.toString();

		case PREFERENCE_VALUE_TYPE_INT:
			return String.valueOf(settings.getInt(key,
					Integer.parseInt(mDefault)));

		case PREFERENCE_VALUE_TYPE_FLOAT:
			return String.valueOf(settings.getFloat(key,
					Float.parseFloat(mDefault)));

		case PREFERENCE_VALUE_TYPE_LONG:
			return String.valueOf(settings.getLong(key,
					Long.parseLong(mDefault)));
		}
		return "";
	}

	public static void upDateSettingMessage(Context ctx, String key, int getId,
			String mDefault) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = settings.edit();
		switch (getId) {
		case PREFERENCE_VALUE_TYPE_STRING:
			editor.putString(key, mDefault);
			break;

		case PREFERENCE_VALUE_TYPE_BOOLEAN:
			editor.putBoolean(key,
					mDefault.equals(PREFERENCE_VALUE_TYPE_BOOLEAN_TRUE));
			break;

		case PREFERENCE_VALUE_TYPE_INT:
			editor.putInt(key, Integer.parseInt(mDefault));
			break;

		case PREFERENCE_VALUE_TYPE_FLOAT:
			editor.putFloat(key, Float.parseFloat(mDefault));
			break;

		case PREFERENCE_VALUE_TYPE_LONG:
			editor.putLong(key, Long.parseLong(mDefault));
			break;
		}
		editor.commit();
	}

	public static boolean isPasswordProtection(Context ctx) {
		return getSettingMessage(ctx,
				getResourcesString(ctx, R.string.system_settings_checkbox),
				PREFERENCE_VALUE_TYPE_BOOLEAN,
				PREFERENCE_VALUE_TYPE_BOOLEAN_FALSE).equals(
				PREFERENCE_VALUE_TYPE_BOOLEAN_TRUE);

	}

	public static void setPasswordProtection(Context ctx, String mValue) {
		upDateSettingMessage(ctx,
				getResourcesString(ctx, R.string.system_settings_checkbox),
				PREFERENCE_VALUE_TYPE_BOOLEAN, mValue);
	}

	public static boolean isAutoSync(Context ctx) {
		return getSettingMessage(ctx,
				getResourcesString(ctx, R.string.auto_sync_checkbox),
				PREFERENCE_VALUE_TYPE_BOOLEAN,
				PREFERENCE_VALUE_TYPE_BOOLEAN_TRUE).equals(
				PREFERENCE_VALUE_TYPE_BOOLEAN_TRUE);

	}

	public static void setAutoSync(Context ctx) {
		upDateSettingMessage(ctx,
				getResourcesString(ctx, R.string.auto_sync_checkbox),
				PREFERENCE_VALUE_TYPE_BOOLEAN,
				PREFERENCE_VALUE_TYPE_BOOLEAN_TRUE);

	}

	public static String isWifiOnlyDownLoadData(Context ctx) {
		return getSettingMessage(
				ctx,
				getResourcesString(ctx, R.string.download_type_list_preference),
				PREFERENCE_VALUE_TYPE_STRING, PREFERENCE_VALUE_DEFAULT_DOWNLOAD);

	}

	public static void setWifiOnlyDownLoadData(Context ctx) {
		upDateSettingMessage(
				ctx,
				getResourcesString(ctx, R.string.download_type_list_preference),
				PREFERENCE_VALUE_TYPE_STRING, PREFERENCE_VALUE_DEFAULT_DOWNLOAD);

	}

	public static String isShowLayoutType(Context ctx) {
		return getSettingMessage(
				ctx,
				getResourcesString(ctx,
						R.string.system_set_show_layout_way_list_preference),
				PREFERENCE_VALUE_TYPE_STRING,
				PREFERENCE_VALUE_DEFAULT_LAYOUT_DEFAULT);

	}

	public static String isSystemPassword(Context ctx) {
		return getSettingMessage(ctx,
				getResourcesString(ctx, R.string.system_settings_password),
				PREFERENCE_VALUE_TYPE_STRING,
				PREFERENCE_VALUE_DEFAULT_SYSTEM_PASSWORD);
	}

	public static void setSystemPassword(Context ctx, String mDefaultValue) {
		upDateSettingMessage(ctx,
				getResourcesString(ctx, R.string.system_settings_password),
				PREFERENCE_VALUE_TYPE_STRING, mDefaultValue);

	}

	public static String isReadingWay(Context ctx) {
		return getSettingMessage(
				ctx,
				getResourcesString(ctx,
						R.string.system_set_show_note_way_list_preference),
				PREFERENCE_VALUE_TYPE_STRING,
				PREFERENCE_VALUE_DEFAULT_READ_NOTE);

	}

	public static void setReadingWay(Context ctx) {
		upDateSettingMessage(
				ctx,
				getResourcesString(ctx,
						R.string.system_set_show_note_way_list_preference),
				PREFERENCE_VALUE_TYPE_STRING,
				PREFERENCE_VALUE_DEFAULT_READ_NOTE);

	}

	public static String isUserIdForWidget(Context ctx) {
		return getSettingMessage(ctx,
				getResourcesString(ctx, R.string.widget_select_account),
				PREFERENCE_VALUE_TYPE_STRING,
				PREFERENCE_VALUE_DEFAULT_SYSTEM_NULL);
	}

	public static void setUserIdForWidget(Context ctx, String mDefaultValue) {
		upDateSettingMessage(ctx,
				getResourcesString(ctx, R.string.widget_select_account),
				PREFERENCE_VALUE_TYPE_STRING, mDefaultValue);

	}

	public static WizPDFOutlineElement isExistsParent(
			ArrayList<WizPDFOutlineElement> arr, String parentId, int endIndex) {
		WizPDFOutlineElement mCurrentElement = null;
		for (int i = endIndex - 1; i >= 0; i--) {
			mCurrentElement = arr.get(i);
			if (mCurrentElement.getId().equals(parentId)
					|| mCurrentElement.getId() == parentId)
				return mCurrentElement;
		}

		for (int i = endIndex + 1; i < arr.size(); i++) {
			mCurrentElement = arr.get(i);
			if (mCurrentElement.getId() == null)
				continue;
			if (mCurrentElement.getId().equals(parentId)
					|| mCurrentElement.getId() == parentId)
				return mCurrentElement;
		}
		return null;
	}

	public static boolean isExistsTagChild(Context ctx, String userId,
			String tagGuid, boolean checkDocument) {
		try {
			return WizIndex.isExistsTagChild(ctx, userId, tagGuid,
					checkDocument);
		} catch (Exception e) {
		}
		return false;
	}

	public static boolean isExistsDocumentChild(Context ctx, String userId,
			String path, boolean checkDocument) {
		try {
			return WizIndex.isExistsDocumentChild(ctx, userId, path,
					checkDocument);
		} catch (Exception e) {
		}
		return false;
	}

	public static ArrayList<WizAttachment> getAttachmentInfo(Context ctx,
			String userId, String docGuid) {
		ArrayList<WizAttachment> mCurrentAtts = WizIndex
				.getAttachmentsByDocGuid(ctx, userId, docGuid);

		ArrayList<WizAttachment> wizAtts = new ArrayList<WizAttachment>();

		if (mCurrentAtts != null && mCurrentAtts.size() > 0) {

			WizAttachment mCurrentAtt;
			String documentPath = WizIndex.getDocumentFilePath(ctx, userId,
					docGuid);
			WizGlobals.ensurePathExists(documentPath);

			for (int i = 0; i < mCurrentAtts.size(); i++) {
				mCurrentAtt = mCurrentAtts.get(i);
				mCurrentAtt.location = documentPath;
				wizAtts.add(mCurrentAtt);
			}
		}
		return wizAtts;
	}

	public static WizPDFOutlineElement reloadPdfOutlineElement(
			Context mContext, String userId, String mDocGuid,
			WizPDFOutlineElement mElement) {
		if (mElement == null)
			return null;

		WizDocument doc = WizIndex.documentFromGUID(mContext, userId, mDocGuid);
		mElement.setDoc(doc);
		mElement.setLocation(doc.location);
		mElement.setOutlineTitle(doc.title);
		return mElement;

	}

	// 判断字段是否为空
	public static boolean isEmptyString(String str) {
		return TextUtils.isEmpty(str);
	}

	public static boolean existsWizCretData(WizCretData cret) {
		if (cret == null)
			return false;

		if (isEmptyString(cret.getmRsaN()))
			return false;

		if (isEmptyString(cret.getmRsaE()))
			return false;

		if (isEmptyString(cret.getmEncryptedD()))
			return false;

		return true;
	}

	public static boolean isDocumentCanEdit(Context ctx, String userId,
			String docGUID) {
		WizDocument doc = WizIndex.documentFromGUID(ctx, userId, docGUID);

		if (doc == null)
			return false;

		String documentType = doc.type;
		if (!isEmptyString(documentType)) {
			if (documentType.equals("todolist")
					|| documentType.equals("todolist2"))
				return false;
		}
		return true;
	}

	public static boolean isWizVersionUpdate(Context ctx, String userId) {
		int indexVersionCode = WizIndex.isWizVersion(ctx, userId);
		int localVersionCode = getVersionCode(ctx);
		if (localVersionCode <= 0)
			return false;
		return indexVersionCode > localVersionCode;
	}

	public static int getVersionCode(Context ctx) {
		try {
			android.content.pm.PackageManager pm = ctx.getPackageManager();
			android.content.pm.PackageInfo pi = pm.getPackageInfo(
					ctx.getPackageName(), 0);
			return pi.versionCode;// 获取在AndroidManifest.xml中配置的版本号VersionCode

		} catch (android.content.pm.PackageManager.NameNotFoundException e) {
			return 0;
		}
	}

	public static String getVersionName(Context ctx) {
		try {
			android.content.pm.PackageManager pm = ctx.getPackageManager();
			android.content.pm.PackageInfo pi = pm.getPackageInfo(
					ctx.getPackageName(), 0);
			return pi.versionName;// 获取在AndroidManifest.xml中配置的版本号VersionName
		} catch (android.content.pm.PackageManager.NameNotFoundException e) {
			return "";
		}
	}

	public static void setDataForLogin(Context mContext, String userId,
			XmlRpcStruct obj) {
		String mInvitationCode = obj.getString("invite_code");
		if (!WizGlobals.isEmptyString(mInvitationCode))
			WizIndex.setWizAccountInvitationCode(mContext, userId,
					mInvitationCode);

		String mVersionCode = obj.getString("version_code");
		if (!WizGlobals.isEmptyString(mVersionCode))
			WizIndex.setWizVersion(mContext, userId,
					Integer.parseInt(mVersionCode));
	}

	public static boolean noteToHtml(Context mContext, String selection,
			String[] selectionArgs, String fileName, String title) {

		try {
			String text = WizSQLite.getHtmlText(mContext, selection,
					selectionArgs);
			String html = text2Html(text, title);
			saveTextToFile(fileName, html, "utf-8");
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static boolean alert2Txt() {
		return false;
	}

	static public File getDocZipFile(Context mContext, String mAccountUserId,
			WizDocument data) {

		if (data == null)
			return null;

		String dataOrgFileName = WizGlobals.getNoteHtmlFileName(mContext);

		String guid = data.guid;

		String zipFileName = getZipFile(mContext, guid);
		File zipFile = new File(zipFileName);

		String newFolderName = getUpdateFolderFile(mContext, guid);
		File newFolder = new File(newFolderName);

		String selection = NoteColumns.ID + "=?";
		String[] selectionArgs = { String.valueOf(data.noteId) };

		try {
			if (!noteToHtml(mContext, selection, selectionArgs,
					dataOrgFileName, data.title))
				return null;

			alert2Txt();

			int fileNameIndex = dataOrgFileName.lastIndexOf("/");
			String fileName = dataOrgFileName.substring(fileNameIndex + 1);
			String newFileName = WizGlobals.pathAddBackslash(newFolderName)
					+ fileName;

			WizGlobals.copyFile(dataOrgFileName, newFileName);
			WizGlobals.ZipByApache(newFolder, zipFile);

		} catch (FileNotFoundException e) {
			WizIndex.setDocumentInfoByUpload(mContext, mAccountUserId, guid, "");
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			/* 删除临时文件夹及其子文件 */
			WizGlobals.deleteDirectory(newFolderName);
		}
		return zipFile;
	}

	static String getNoteHtmlFileName(Context mContext) {
		String filePath = getWizTaskPath(mContext);
		return pathAddBackslash(filePath) + "index.html";
	}

	static String getNoteHtmlFileName(Context mContext, String guid) {
		String docFilePath = getWizTaskCurrentPath(mContext, guid);
		return pathAddBackslash(docFilePath) + "index.html";
	}

	static String getZipFile(Context mContext, String guid) {
		String basePath = WizGlobals.getDataRootPath(mContext);
		String zip_file_name = WizGlobals.pathAddBackslash(basePath) + guid
				+ ".zip";

		return zip_file_name;
	}

	static String getUpdateFolderFile(Context mContext, String guid) {
		String basePath = WizGlobals.getDataRootPath(mContext);
		String newFolderName = WizGlobals.pathAddBackslash(basePath) + guid;
		ensurePathExists(newFolderName);
		return newFolderName;
	}

	public static boolean saveNote(Context mContext, WizDocument doc,
			String text) {
		if (WizGlobals.isEmptyString(text))
			text = "";

		String guid = doc.guid;
		String location = doc.location;
		location = getLocation(location, false);

		long noteId = 0;
		noteId = WizSQLite.getNoteIdFromConnect(mContext, guid);
		WorkingNote mWorkingNote = null;
		if (noteId > 0) {
			mWorkingNote = WorkingNote.load(mContext, noteId);
		} else {
			long folderId = createFolder(mContext, location, 0);
			mWorkingNote = WorkingNote.createEmptyNote(mContext, folderId, 0,
					0, 0);
		}
		mWorkingNote.iniContentValues(guid, doc.title, doc.dateCreated,
				doc.dateModified, doc.type, doc.fileType, location,
				doc.attachmentCount, doc.tagGUIDs);
		mWorkingNote.setWorkingText(text);
		return mWorkingNote.saveNote();
	}

	public static long createFolder(Context mContext, String location,
			long parentId) {
		if (isEmptyString(location))
			return 0;
		String name = location;
		int index = location.lastIndexOf("/");
		if (index != -1)
			name = getFolderName(location);
		else
			name = location;
		return newFolder(mContext, name, parentId);
	}

	public static long newFolder(Context mContext, String name, long parentId) {

		ContentResolver mContentResolver = mContext.getContentResolver();
		if (DataUtils.checkVisibleFolderName(mContentResolver, name)) {
			return WizSQLite.getFolderIdFromConnect(mContext, name);
		}
		ContentValues values = new ContentValues();
		values.put(NoteColumns.SNIPPET, name);
		values.put(NoteColumns.TYPE, Notes.TYPE_FOLDER);
		values.put(NoteColumns.PARENT_ID, parentId);
		Uri uri = mContentResolver.insert(Notes.CONTENT_NOTE_URI, values);
		return Long.valueOf(uri.getPathSegments().get(1));
	}

	public static String getFolderName(String str) {
		if (str.lastIndexOf("/") <= -1)
			return "";
		String[] mCurrentStringArray = str.split("/");
		String name = "";
		for (int i = 0; i < mCurrentStringArray.length; i++) {
			name = mCurrentStringArray[i];
			if (!WizGlobals.isEmptyString(name))
				return name;
		}
		return "";
	}

	public static final String TASK_ROOT_PATH = "/My Sticky Notes/";

	static String getLocation(String location, boolean addRootPath) {
		if (isEmptyString(location))
			location = "";
		if (addRootPath) {
			location = TASK_ROOT_PATH + location;
			location = pathAddBackslash(location);
		} else {
			if (location.indexOf(TASK_ROOT_PATH) == 0) {
				location = location.replace(TASK_ROOT_PATH, "");
			}
		}
		return location;
	}

	public static boolean saveNote(Context mContext, WizDocument doc) {
		String docFileName = getNoteHtmlFileName(mContext, doc.guid);
		String html = loadTextFromFile(docFileName);
		String text = html2Text(html, 0);
		if (isEmptyString(text))
			text = "";
		return saveNote(mContext, doc, text);
	}

	public static ContentValues iniDeletedContentValues(String guid) {
		ContentValues values = new ContentValues();
		values.put(DeletedColumns.DELETED_GUID, guid);
		values.put(DeletedColumns.GUID_TYPE, DATA_INFO_TYPE_DOCUMENT);
		values.put(DeletedColumns.DT_DELETED,
				WizGlobals.getCurrentSQLDateTimeString());
		return values;
	}

	public static void deleteCurrentNote(Context ctx, long id) {
		if (id <= 0)
			return;
		deleteCurrentNote(ctx, WorkingNote.load(ctx, id));
	}

	public static void deleteCurrentNote(Context ctx, WorkingNote mWorkingNote) {
		if (mWorkingNote.existInDatabase()) {
			HashSet<Long> ids = new HashSet<Long>();
			long id = mWorkingNote.getNoteId();
			if (id != Notes.ID_ROOT_FOLDER) {
				ids.add(id);
			} else {
				System.out.println("Wrong note id, should not happen");
			}
			if (!isSyncMode(ctx)) {
				if (!DataUtils.batchDeleteNotes(ctx.getContentResolver(), ids)) {
					System.out.println("Delete Note error");
				}
			} else {
				if (!DataUtils.batchMoveToFolder(ctx.getContentResolver(), ids,
						Notes.ID_TRASH_FOLER)) {
					System.out
							.println("Move notes to trash folder error, should not happens");
				}
			}
			WizSQLite.addDeletedInfo(ctx, id);
		}
		mWorkingNote.markDeleted(true);
	}

	public static boolean isSyncMode(Context ctx) {
		return SystemEditActivity.getSyncAccountName(ctx).trim().length() > 0;
	}

	public static void sendToDesktop(Context ctx, Activity activity, long id) {
		if (id <= 0)
			return;
		sendToDesktop(ctx, activity, WorkingNote.load(ctx, id));
	}

	public static void sendToDesktop(Context ctx, Activity activity,
			WorkingNote mWorkingNote) {
		/**
		 * Before send message to home, we should make sure that current editing
		 * note is exists in databases. So, for new note, firstly save it
		 */

		if (mWorkingNote.getNoteId() > 0) {
			Intent sender = new Intent();
			Intent shortcutIntent = new Intent();
			shortcutIntent.setClass(ctx, NoteEditActivity.class);
			shortcutIntent.setAction(Intent.ACTION_VIEW);
			shortcutIntent.putExtra(Intent.EXTRA_UID, mWorkingNote.getNoteId());
			sender.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
			sender.putExtra(Intent.EXTRA_SHORTCUT_NAME,
					makeShortcutIconTitle(mWorkingNote.getContent()));
			sender.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
					Intent.ShortcutIconResource.fromContext(ctx,
							R.drawable.icon_app));
			sender.putExtra("duplicate", true);
			sender.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
			showMessage(ctx, R.string.info_note_enter_desktop, false);
			ctx.sendBroadcast(sender);
		} else {
			/**
			 * There is the condition that user has input nothing (the note is
			 * not worthy saving), we have no note id, remind the user that he
			 * should input something
			 */
			System.out.println("Send to desktop error");
			showMessage(ctx, R.string.error_note_empty_for_send_to_desktop,
					false);
		}
	}

	public static final String TAG_CHECKED = String.valueOf('\u221A');
	public static final String TAG_UNCHECKED = String.valueOf('\u25A1');
	public static final int SHORTCUT_ICON_TITLE_MAX_LEN = 10;

	public static String makeShortcutIconTitle(String content) {
		content = content.replace(TAG_CHECKED, "");
		content = content.replace(TAG_UNCHECKED, "");
		return content.length() > SHORTCUT_ICON_TITLE_MAX_LEN ? content
				.substring(0, SHORTCUT_ICON_TITLE_MAX_LEN) : content;
	}

}
