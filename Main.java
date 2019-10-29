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
	public static int numitems=-1;
	public static int minweight = Integer.MAX_VALUE;
	public static int sizebags=-1;
	public static boolean local = false;
	public static boolean arcconsistency = false;
	public static boolean debug = true;
	private static int totalItemWeight = 0;
	private static long start;
	public static ArrayList<Item> unsortedItems = new ArrayList<Item>();
	private static Stack<SearchState> states;
	private static ArrayList<Bag> bags;
	
	private static HashMap<String, Integer> hmap = new HashMap<String, Integer>();
	private static HashMap<Integer, String> rhmap = new HashMap<Integer, String>();
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		if(args.length ==1||!args[1].equals("-slow")) {
			arcconsistency = true;
		}
		if((args.length >1 && args[1].equals("-debug"))||(args.length >2 && args[2].equals("-debug"))||(args.length >3 && args[3].equals("-debug")))
			debug = true;
		if((args.length >1 && args[1].equals("-local")) || (args.length >2 && args[2].equals("-local")))
			local = true;
		start = System.currentTimeMillis();
		ArrayList<Item> Items = new ArrayList<Item>();
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
			minweight = Integer.min(minweight, i.weight);
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
		numitems = Items.size();
		sc.close();
		unsortedItems= new ArrayList<Item>(Items);
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
				if(cItem.constraints.size()+1 == numitems) //if the item has constraints against every item, we save time by putting the emptiest bag first, as it must be in a bag by itself.
					Collections.reverse(uniquebags);
				int currentindex = Items.indexOf(cItem);
				Item nextItem = null;
				if(currentindex !=Items.size()-1) {
					nextItem= Items.get(currentindex+1);
				}
				 
				for(Bag b : uniquebags) 
				{
					
					if(b.canAdd(cItem))
					{
						boolean topflag = false;
						if(arcconsistency) {
							if(nextItem != null && !b.canAdd(nextItem)) {
								topflag = true;
							}
						}
						//add onto stack
						ArrayList<Bag> newbags = new ArrayList<Bag>(bags);
						newbags.set(bags.indexOf(b), b.add(cItem));
						//states.push(new SearchState(newbags, currentItem+1));
						tempstates.add(new SearchState(newbags, currentItem+1,topflag));
					}
				}
				//randomizes stack
				//tempstates = randomize(tempstates); shuffles states
				
				ArrayList<SearchState> toptemp = new ArrayList<SearchState>();
				if(statesexpanded > 1) {
					for(SearchState s:tempstates) {
						if(s.topflag) {
							toptemp.add(s);
						}
						else
							states.push(s);
					}
					if(!toptemp.isEmpty())
					{
						for(SearchState s:toptemp) {
						states.push(s);
						}
					}
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
			ArrayList<Bag> usedbags = new ArrayList<Bag>();
			for(Bag b: bags)
			{
				if(b.items.size() != 0) 
				{
					usedbags.add(b);
					numbags++;
				}
			}
			/*if(numbags < minBagsUsed) {
				minBagsUsed = numbags;
				//bestbags = currentstate;
			}*/
			if(debug||!local)
			printBags(usedbags);
		//}
			double stddev = getstddev(usedbags);
			double idealWeightDistribution = (double)totalItemWeight/usedbags.size();
			if(debug) {
				System.out.println((double)(System.currentTimeMillis()-start)/1000 + " seconds");
				System.out.println("States expanded: "+statesexpanded);
				System.out.println("Bags used:"+ numbags);
				System.out.println("Ideal Weight Distribution:"+idealWeightDistribution);
				System.out.println("Standard Deviation: "+stddev);
			}
			if(local) {
				long t= System.currentTimeMillis();
				long end = t+15000;
				Collections.sort(usedbags, new BagSortWeight());
				ArrayList<Bag> tempbags = new ArrayList<Bag>(usedbags);
				while(System.currentTimeMillis()<end && stddev >1) {
					//pull out an item
					Item pull = null;
					Bag pulledFrom = null;
					int i = 0;
					while(pull==null && i<bagsize-idealWeightDistribution) {
						for(Bag b: tempbags) {
							if(b.weight > idealWeightDistribution)
							{
								for(int item: b.items)
								{
									Item potential =unsortedItems.get(item);
									if(potential.weight == b.weight-(int)idealWeightDistribution+i || potential.weight == b.weight-(int)idealWeightDistribution-i) {
										pull = potential;
										pulledFrom = b;
										
									}
								}
							}
						}
						i++;
					}
					if(pull == null)
						break;
					else {
						pulledFrom.remove(pull); //pull item out of bag
						tempbags.remove(pulledFrom);//remove bag from bag list
						usedbags.remove(pulledFrom);
						tempbags.add(pulledFrom); //add bag to back of bag list
						usedbags.add(pulledFrom);
					}
					for(Bag b: tempbags) {	
						if(tempbags.indexOf(b)==tempbags.size()-1)
							break;
						if(pull.weight == (int)idealWeightDistribution-b.weight) 
						{
							if(b.canAdd(pull))
							{
								b.add(pull);
								pull = null;
								break;
							}
						}
					}
					if(pull != null) //if no solution found yet
					for(Bag b: tempbags) {
						if(pull.weight <= (int)idealWeightDistribution-b.weight)
						{
							if(b.canAdd(pull))
							{
								b.add(pull);
								pull = null;
								break;
							}
						}
					}
					if(pull==null) {
						usedbags = (ArrayList<Bag>) tempbags.clone();
						if(debug) {
							stddev = getstddev(usedbags);
							//printBags(usedbags);
							System.out.println("Standard Deviation: "+stddev);
						}
					}
					else {
						tempbags=(ArrayList<Bag>) usedbags.clone();
						continue;
					}
				}
				printBags(usedbags);
				if(debug) {
					System.out.println("Ideal Weight Distribution:"+idealWeightDistribution);
					System.out.println("Standard Deviation: "+stddev);
				}
			}
			//implement local search
			//while searching: (set specific amount of time to search? 5 seconds?)
				//-if it is possible to have a solution with fewer bags (by weight, check items with constraints against every other item)
					//-go through all the bags that have one item in them, attempt to add that item to other bags where weight is not full
				//-if this is possible, continue searching from this solution, remove this empty bag from used bags.
				//-if this is not possible, attempt to remove an item from a bag and add it to another bag so that the weight is 
				//	on average distributed more evenly than the previous solution.
				//-to determine if weight is distributed evenly:
					//compute the total sum of the weights of all bags, and divide by number of bags, this is the ideal weight in each bag
					//compute the standard deviation from this weight
					//lower standard deviation is a better solution
				//if a more distributed solution is found, continue searching from this new solution.
			
		}
		

	private static void printBags(ArrayList<Bag> usedbags) {
		for(Bag b: usedbags) 
		{
			
			for(int i: b.items) {
				System.out.print(rhmap.get(i)+"\t");
			}
			if(debug) {
				System.out.print("weight: "+b.weight); //print weight of bag
				System.out.print("\t num constraints:"+b.constraints.size());
			}
			System.out.println();
		}
		
	}

	private static double getstddev(ArrayList<Bag> usedbags) {
		int sum = 0;
		for(Bag b: usedbags) {
			sum+=b.weight;
		}
		double mean = (double)sum/usedbags.size();
		int sum2 =0;
		for(Bag b: usedbags) {
			sum2+=Math.pow(b.weight-mean,2);
		}
		double stdev = Math.sqrt((double)sum2/usedbags.size());
		return stdev;
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

	/*private static ArrayList<Bag> getsortedbags(ArrayList<Bag> bags) {
		ArrayList<Bag> sortedBags = new ArrayList<Bag>(bags);
		Collections.sort(sortedBags, new BagSort());
		return sortedBags;
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
			else {
				if(b.weight+minweight <= b.max && b.constraints.size()+b.items.size() < numitems) //if item weight is full
					uniquebags.add(b);
			}
		}
		Collections.reverse(uniquebags); //lcv heuristic, attempting to add to first non full, non empty bag first, sorting each time takes too long.
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
	static class BagSort implements Comparator<Bag> 
	{ 
		//negative if bag a is more constrained than bag b
		//0 if the items are equally constrained
		//positive if bag a is less constrained than bag b
		@Override
	    public int compare(Bag a, Bag b) 
	    { 
	    	if(a.items.size() > b.items.size())
	    		return -1;
	    	else if(a.items.size() < b.items.size())
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
	static class BagSortWeight implements Comparator<Bag> 
	{ 
		//negative if bag a is more constrained than bag b
		//0 if the items are equally constrained
		//positive if bag a is less constrained than bag b
		@Override
	    public int compare(Bag a, Bag b) 
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
