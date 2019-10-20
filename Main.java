//package grocery;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.Collections;
import java.util.Comparator;


public class Main {

	private static int bagsize = 0;
	public static int numbags=-1;
	public static int sizebags=-1;
	private static int totalItemWeight = 0;
	private static long start;
	private static Stack<SearchState> states;
	private static ArrayList<Bag> bags;
	
	public static void main(String[] args) {
		if(args.length ==1||args[1].equals("-depth")) {
			//System.out.println("depth");
		start = System.currentTimeMillis();
		ArrayList<Item> Items = new ArrayList<Item>();
		HashMap<String, Integer> hmap = new HashMap<String, Integer>();
		HashMap<Integer, String> rhmap = new HashMap<Integer, String>();
		Set<Integer> allindexes = new HashSet<Integer>();
		//System.out.print(args[0]+"\n");
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
		//System.out.println("number of bags: "+numbags);
		//System.out.println("bag size: "+bagsize);
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
				fail(0);
			totalItemWeight+=i.weight;
			if(linesc.hasNextLine())
				i.ConstraintString = linesc.nextLine();
			else
				i.ConstraintString = "";
			Items.add(i);
			hmap.put(i.name, i.ID);
			rhmap.put(i.ID, i.name);
			x++;
			linesc.close();
		}
		if(totalItemWeight > bagsize*numbags)
			fail(0);
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

		Collections.sort(Items, new ItemSort()); // this improves the solution in a noticeable way both in terms of bags used and time taken
		//Collections.reverse(Items); //this results in a much worse solution in a longer time.
		states = new Stack<SearchState>();
		bags = new ArrayList<Bag>();
		for(int i = 0;i<numbags;i++)
		{
			Bag b = new Bag();
			b.max = bagsize;
			bags.add(b);
		}
		//int minBagsUsed = Integer.MAX_VALUE;
		//SearchState bestbags = new SearchState(bags, 0);
		//for(int i2 = 0; i2<5; i2++) {
			//initialize starting state
			SearchState currentstate = new SearchState(bags, 0);
			int statesexpanded = 0;
			while(!currentstate.IsGoalState(Items.size())) 
			{
				//load state from stack
				statesexpanded++;
				int currentItem = currentstate.addingitem;
				Item cItem = Items.get(currentItem);
				bags = currentstate.bags;
	
				ArrayList<SearchState> tempstates = new ArrayList<SearchState>();
				ArrayList<Bag> uniquebags = getuniquebags(bags); //prevents duplicate states, this makes failures much quicker to find.
				for(Bag b : uniquebags) 
				{
					
					if(b.canAdd(cItem))
					{
						//add onto stack
						ArrayList<Bag> newbags = new ArrayList<Bag>(bags);
						newbags.set(bags.indexOf(b), b.add(cItem));
						//states.push(new SearchState(newbags, currentItem+1));
						tempstates.add(new SearchState(newbags, currentItem+1));
					}
				}
				//randomizes stack
				//tempstates = randomize(tempstates); shuffles states
				if(statesexpanded > 1)
				for(SearchState s:tempstates) {
					states.push(s);
				}
				else
				{
					states.push(tempstates.get(tempstates.size()-1));
				}
				if(!states.empty()) {
				currentstate = states.pop();
				//System.out.println(currentItem);
				}
				else {
					fail(statesexpanded);
				}
			}
			//System.out.println(currentstate.bags);
			System.out.println("success");
			int numbags = 0;
			for(Bag b: bags)
			{
				if(b.items.size() != 0) 
					numbags++;
			}
			/*if(numbags < minBagsUsed) {
				minBagsUsed = numbags;
				//bestbags = currentstate;
			}*/
				
			for(Bag b: currentstate.bags) 
			{
				
				for(int i: b.items) {
					System.out.print(rhmap.get(i)+"\t");
				}
				if(!b.items.isEmpty())
					System.out.println();
			}
		//}
			System.out.println((double)(System.currentTimeMillis()-start)/1000 + " seconds");
			System.out.println("States expanded: "+statesexpanded);
			System.out.println("Bags used:"+ numbags);
			/*System.out.println("Minimum bags used: "+minBagsUsed);
			System.out.println("Best Solution:");
			for(Bag b: bestbags.bags) 
			{
				
				for(int i: b.items) {
					System.out.print(rhmap.get(i)+"\t");
				}
				if(!b.items.isEmpty())
					System.out.println();
			}*/
		}
		
	}

	/*private static ArrayList<SearchState> randomize(ArrayList<SearchState> tempstates) {
		ArrayList<SearchState> tempstates2 = new ArrayList<SearchState>();
		int size = tempstates.size();
		for(int i = 0; i<size; i++)
		{
			int x =(int) (Math.random()*tempstates.size());
			tempstates2.add(tempstates.remove(x));
		}
		return tempstates2;
	}*/

	private static ArrayList<Bag> getuniquebags(ArrayList<Bag> bags) {
		ArrayList<Bag> uniquebags = new ArrayList<Bag>();
		boolean emptybagadded = false;
		for(Bag b: bags) {
			if(b.items.isEmpty()) {
				if(!emptybagadded)
				{
					uniquebags.add(b);
					emptybagadded = true;
				}
			}
			else
				uniquebags.add(b);
		}
		return uniquebags;
	}

	private static void fail(int statesexpanded) {
		System.out.println("failure");
		System.out.println("States expanded: "+statesexpanded);
		//System.out.println((double)(System.currentTimeMillis()-start)/1000 + " seconds");
		System.exit(0);
	}
	static class ItemSort implements Comparator<Item> 
	{ 
		//negative if item a is more constrained than item b
		//0 if the items are equally constrained
		//positive if item a is less constrained than item b
		@Override
	    public int compare(Item a, Item b) 
	    { 
	    	if(a.constraints.size() > b.constraints.size())
	    		return -1;
	    	else if(a.constraints.size() < b.constraints.size())
	    		return 1;
	    	else 
	    	{
	    		if(a.weight > b.weight)
	    			return -1;
	    		else if(a.weight < b.weight)
	    			return 1;
	    		else
	    			return 0;
	    	}
	    } 
	} 
}
