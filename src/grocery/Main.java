package grocery;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main {

	private static int numbags = 0;
	private static int bagsize = 0;
	
	public static void main(String[] args) {
		System.out.print(args[0]+"\n");
		File filename = new File(args[0]);
		if(!filename.exists()) {
			System.out.println("File not found.\n");
			return;
		}
		Scanner sc;
		try {
			sc = new Scanner(filename);
		} catch (FileNotFoundException e) {
			System.out.println("File not found.");
			return;
		}
		
		numbags = Integer.parseInt(sc.next());
		bagsize = Integer.parseInt(sc.next());
		System.out.println("number of bags: "+numbags);
		System.out.println("bag size: "+bagsize);
		//while (sc.hasNext()) 
	    //  System.out.println(sc.next());
		
		
		
		sc.close();
	}

}
