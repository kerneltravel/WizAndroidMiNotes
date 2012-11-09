package cn.code.notes.share;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class WizIni {

	static Properties getFile(String fileName) {
		FileInputStream is = null;
		//
		Properties ini = new Properties();

		try {

			File file = new File(fileName);
			is = new FileInputStream(file);
			ini.load(is);

		} catch (FileNotFoundException e) {

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != is) {
				try {
					is.close();
				} catch (IOException e) {

					e.printStackTrace();
				}
			}
		}
		//
		return ini;
	}

	/**
	 * 读取INI信息
	 * */
	public static String getIniKey(String fileName, String key, String def) {
		Properties ini = getFile(fileName);
		if (!ini.containsKey(key)) {
			return def;
		}
		String ret = ini.get(key).toString();
		return ret;
	}

	/**
	 * 修改INI信息
	 * */
	public static void setIniKey(String fileName, String key, String value) {
		Properties ini = getFile(fileName);
		ini.put(key, value);
		//
		FileOutputStream os = null;
		try {
			File file = new File(fileName);
			os = new FileOutputStream(file);
			ini.store(os, "");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != os) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		//
	}
}