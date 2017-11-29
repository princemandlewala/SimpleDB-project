package simpledb.test;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Long[] array = new Long[1];
		array[0] = (long)-1;
		if(array[0]==-1) {
			System.out.println("Entering the program");
		}
		else {
			System.out.println("Leaving the program");
		}
		System.out.println(Boolean.toString(true));
		String d = "Sun Nov 26 15:12:00 EST 2017";
		Date date = null;
		try {
			date = new SimpleDateFormat().parse(d);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println(date.toString());
	}
}
