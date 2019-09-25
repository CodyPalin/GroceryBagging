package grocery;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Main {

	private static int numbags = 0;
	private static int bagsize = 0;
	private static int totalItemWeight = 0;
	
	public static void main(String[] args) {
		ArrayList<Item> Items = new ArrayList<Item>();
		HashMap<String, Integer> hmap = new HashMap<String, Integer>();
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
		sc.nextLine();
		int x = 0;
		while (sc.hasNextLine()) 
		{
			String line = sc.nextLine();
			Scanner linesc = new Scanner(line);
			Item i = new Item();
			i.ID = x;
			i.name= linesc.next();
			i.weight = Integer.parseInt(linesc.next());
			if(i.weight > bagsize)
				fail();
			totalItemWeight+=i.weight;
			i.ConstraintString = linesc.nextLine();
			Items.add(i);
			hmap.put(i.name, i.ID);
			x++;
			linesc.close();
		}
		if(totalItemWeight > bagsize*numbags)
			fail();
		for(Item i : Items)
		{
			
		}
		
		
		
		sc.close();
	}

	private static void fail() {
		System.out.println("failure");
		System.exit(0);
	}

}
