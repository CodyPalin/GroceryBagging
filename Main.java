package grocery;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

public class Main {

	private static int numbags = 0;
	private static int bagsize = 0;
	private static int totalItemWeight = 0;
	private static long start;
	private static Stack<SearchState> states;
	private static ArrayList<Bag> bags;
	
	public static void main(String[] args) {
		start = System.currentTimeMillis();
		ArrayList<Item> Items = new ArrayList<Item>();
		HashMap<String, Integer> hmap = new HashMap<String, Integer>();
		Set<Integer> allindexes = new HashSet<Integer>();
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
			allindexes.add(i.ID);
			i.name= linesc.next();
			i.weight = Integer.parseInt(linesc.next());
			if(i.weight > bagsize)
				fail();
			totalItemWeight+=i.weight;
			if(linesc.hasNextLine())
				i.ConstraintString = linesc.nextLine();
			else
				i.ConstraintString = "";
			Items.add(i);
			hmap.put(i.name, i.ID);
			x++;
			linesc.close();
		}
		if(totalItemWeight > bagsize*numbags)
			fail();
		for(Item i : Items)
		{
			Scanner cscan = new Scanner(i.ConstraintString);
			if(cscan.hasNext()) 
			{
				String constraintType = cscan.next();
				if(constraintType.equals("-")) 
				{
					while(cscan.hasNext())
					{
						String c = cscan.next();
						int cindex = hmap.get(c);
						i.constraints.add(cindex);
						Items.get(cindex).constraints.add(i.ID);
					}
				}
				else if(constraintType.equals("+"))
				{
					Set<Integer> negativeconstraints = new HashSet<Integer>(allindexes);
					Set<Integer> positiveconstraints = new HashSet<Integer>();
					while(cscan.hasNext())
					{
						String c = cscan.next();
						int cindex = hmap.get(c);
						positiveconstraints.add(cindex);
					}
					negativeconstraints.removeAll(positiveconstraints);
					negativeconstraints.remove(i.ID);
					for(int n : negativeconstraints) {
						i.constraints.add(n);
						Items.get(n).constraints.add(i.ID);
					}
				}
			}
			cscan.close();
		}
		/*for(Item item : Items)
		{
			System.out.println(item.toString());
		}*/

		sc.close();
		states = new Stack<SearchState>();
		bags = new ArrayList<Bag>();
		for(int i = 0;i<numbags;i++)
		{
			Bag b = new Bag();
			b.max = bagsize;
			bags.add(b);
		}
		//initialize starting state
		SearchState currentstate = new SearchState(bags, 0);
		while(!currentstate.IsGoalState(Items.size())) 
		{
			int currentItem = currentstate.addingitem;
			Item cItem = Items.get(currentItem);
			bags = currentstate.bags;
			for(Bag b : bags) 
			{
				
				if(b.canAdd(cItem))
				{
					//add onto stack
					ArrayList<Bag> newbags = new ArrayList<Bag>(bags);
					newbags.set(bags.indexOf(b), b.add(cItem));
					states.push(new SearchState(newbags, currentItem+1)); //check that there are more items
				}
			}
			if(!states.empty())
			currentstate = states.pop();
			else {
				fail();
			}
		}
		System.out.println("success");
		for(Bag b: currentstate.bags) 
		{
			System.out.println(b.toString());
		}
		System.out.println((double)(System.currentTimeMillis()-start)/1000 + " seconds");
	}

	private static void fail() {
		System.out.println("failure");
		System.out.println((double)(System.currentTimeMillis()-start)/1000 + " seconds");
		System.exit(0);
	}

}