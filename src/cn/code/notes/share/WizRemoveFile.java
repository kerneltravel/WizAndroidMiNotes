package cn.code.notes.share;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class WizRemoveFile {

	public static void moveFile(String oldPath, String newPath) {
		copyFile(oldPath, newPath);
		delFile(oldPath);
	}

	/**
	 * ���Ƶ����ļ�
	 * 
	 * @param oldPath
	 *            String ԭ�ļ�·�� �磺c:/fqf.txt
	 * @param newPath
	 *            String ���ƺ�·�� �磺f:/fqf.txt
	 * @return boolean 147.
	 */
	@SuppressWarnings("unused")
	public static void copyFile(String oldPath, String newPath) {
		try {
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPath);
			if (oldfile.exists()) { // �ļ�����ʱ
				// ����ԭ�ļ�
				InputStream inStream = new FileInputStream(oldPath);
				FileOutputStream fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[1444];
				int length;
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread; // �ֽ��� �ļ���С
					System.out.println(bytesum);
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
			}
		} catch (Exception e) {
			System.out.println("���Ƶ����ļ���������");
			e.printStackTrace();
		}
	}

	/**
	 * ɾ���ļ�
	 * 
	 * @param filePathAndName
	 *            String �ļ�·�������� ��c:/fqf.txt
	 * @param fileContent
	 *            String
	 * @return boolean
	 */
	public static void delFile(String filePathAndName) {
		try {
			String filePath = filePathAndName;
			filePath = filePath.toString();
			java.io.File myDelFile = new java.io.File(filePath);
			myDelFile.delete();
		} catch (Exception e) {
			// System.out.println("ɾ���ļ���������");
			e.printStackTrace();
		}
	}
}
