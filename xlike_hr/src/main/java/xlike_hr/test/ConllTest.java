package xlike_hr.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import xlike_hr.model.Conll;

public class ConllTest {

	/**
	 * @param args
	 */
	public static String conllText = readFile("c:\\conll_test.txt");//"d:\\conll\\entities-utf.conll"

	public static void main(String[] args) {
		Conll conll = new Conll();
		conll.parseConllFile(conllText);
		System.out.print(conll.toString());
		System.out.println("**");
	}

	public static String readFile(String fileName) {

		File file = new File(fileName);

		char[] buffer = null;

		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(
					file));

			buffer = new char[(int) file.length()];

			int i = 0;
			int c = bufferedReader.read();

			while (c != -1) {
				buffer[i++] = (char) c;
				c = bufferedReader.read();
			}
			bufferedReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new String(buffer);
	}

}
