package xlike_hr.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileUtil {
	private static Logger log = LogManager.getLogger(FileUtil.class);

	public static void deleteFileContent(File file){
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter(file));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writer.print("");
		writer.close();
	}
	public static File createTempFile() throws Exception {

//		String property = "java.io.tmpdir";
//		String tempDirPath = System.getProperty(property);
//		log.debug("OS current temporary directory is " + tempDirPath);
//		File tempDir = new File(tempDirPath);
//		File newFile = File.createTempFile("tmp", ".conll", tempDir);
		
		File newFile = new File(
				java.util.UUID.randomUUID().toString()+ ".conll");

		log.debug("New Temp file created: " +newFile.getPath());
		return newFile;
	}

	public static void writeFile(File file, String content) throws Exception {
		FileWriter fstream = null;
		BufferedWriter out = null;
		try {
			fstream = new FileWriter(file);
			out = new BufferedWriter(fstream);
			out.write(content);
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			out.close();
		}
	}

	public static String readFile(String fileName) throws Exception {

		File file = new File(fileName);
		char[] buffer = null;
		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
		buffer = new char[(int) file.length()];
		int i = 0;
		try {
			int c = bufferedReader.read();
			while (c != -1) {
				buffer[i++] = (char) c;
				c = bufferedReader.read();
			}
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			bufferedReader.close();
		}
		return new String(buffer);
	}

}
