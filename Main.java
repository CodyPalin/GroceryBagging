//package grocery;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.Collections;
import java.util.Comparator;


public class Main{

	private static int bagsize = 0;
	public static int numbags=-1;
	public static int sizebags=-1;
	private static int totalItemWeight = 0;
//	private static long start;
	private static Stack<SearchState> states;
	private static ArrayList<Bag> bags;
	private static boolean doItOnce =  true;
	
	// TODO: adjusts 'doItOnce' in order to print out all depth-first search solutions.
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		if(args.length ==0) {
			System.out.println("failure");
			System.exit(1);
		}
		if(args.length ==1||args[1].equals("-depth")) {
//		start = System.currentTimeMillis();
		ArrayList<Item> Items = new ArrayList<Item>();
		HashMap<String, Integer> hmap = new HashMap<String, Integer>();
		HashMap<Integer, String> rhmap = new HashMap<Integer, String>();
		Set<Integer> allindexes = new HashSet<Integer>();
		File filename = new File(args[0]);
		if(!filename.exists()) {
			System.out.println("failure");
			System.exit(1);
		}
		Scanner sc=null;
		try {
			sc = new Scanner(filename);
		} catch (FileNotFoundException e) {
			System.out.println("failure");
			System.exit(1);
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
				fail();
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
//		System.out.println("Items: " + Items);
		if(totalItemWeight > bagsize*numbags) fail();
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
//				System.out.println("Items: " + Items);
			}
			cscan.close();
		}

		sc.close();
		
//		System.out.println("Items (Before Sort):");
//		for(Item item : Items) System.out.println(item.toString());
		Collections.sort(Items, new ItemSort());
//		System.out.println("Items (After Sort):");
//		for(Item item : Items) System.out.println(item.toString());
		
		states = new Stack<SearchState>();
		bags = new ArrayList<Bag>();
		for(int i = 0;i<numbags;i++)
		{
			Bag b = new Bag();
			b.max = bagsize;
			bags.add(b);
		}
//		System.out.println("bags: " + bags);
		int minBagsUsed = Integer.MAX_VALUE;
		SearchState bestbags = new SearchState(bags, 0);
		
		//initializes starting state.
		SearchState currentstate = new SearchState(bags, 0);
//		System.out.println("currentstate: " + currentstate);
		labelB:
			while(!currentstate.IsGoalState(Items.size())) 
			{
				//load state from stack
				int currentItem = currentstate.addingitem;
				Item cItem = Items.get(currentItem);
				bags = currentstate.bags;
	
				ArrayList<SearchState> tempstates = new ArrayList<SearchState>();
				for(Bag b : bags) 
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
				
				{
					Stack<SearchState> toBeRemoved = new Stack<SearchState>();
					
					labelC:
					for(int i=0; i<tempstates.size()-1; i++) {
							SearchState temp_ = tempstates.get(i);
							
							labelD:
							for(int j=i+1; j<tempstates.size(); j++) {
								SearchState temp_target_ = tempstates.get(j);
								
								if(temp_.bags.size()!=temp_target_.bags.size()) continue labelD;
								for(Bag b: temp_.bags) Collections.sort(b.items);
								for(Bag b: temp_target_.bags) Collections.sort(b.items);
								
								ArrayList<Bag> temp_bags_ = new ArrayList<Bag>();
								temp_bags_.addAll((ArrayList<Bag>) temp_.bags.clone());
								ArrayList<Bag> temp_target_bags_ = new ArrayList<Bag>();
								temp_target_bags_.addAll((ArrayList<Bag>) temp_target_.bags.clone());
								
								labelF:
								for(int k=0; k<temp_bags_.size(); k++) {
									Bag temp_bags_each_ = temp_bags_.get(k);
									ListIterator<Bag> target_itr = temp_target_bags_.listIterator();
									labelE:
									while(target_itr.hasNext()) {
										Bag target_itr_bag_each_ = target_itr.next();
										if(temp_bags_each_.items.size()!=target_itr_bag_each_.items.size()) continue labelE;
										int g;
										for(g=0; g<temp_bags_each_.items.size(); g++) {
											if(temp_bags_each_.items.size()==0 && target_itr_bag_each_.items.size()==0) {
												temp_target_bags_.remove(target_itr_bag_each_); continue labelF;
											}//if size==0.
											if(target_itr_bag_each_.items.contains((Integer)temp_bags_each_.items.get(g)));
											else continue labelE;
										}//for g.
										temp_target_bags_.remove(target_itr_bag_each_);
										if(temp_target_bags_.size()!=0) continue labelF;
										toBeRemoved.add(temp_target_); continue labelC;
									}//while hasNext.
									//if(temp_target_bags_.size()==0) continue labelC;
									continue labelD;
								}//for k.
								
							}//for j.
							
					}//for i.
					
					while(!toBeRemoved.empty()) {
						tempstates.remove(toBeRemoved.pop());
					}//while !empty.
					
				}
				
				for(SearchState s:tempstates) {
					states.push(s);
				}
				if(!states.empty()) {
					
					do {
						currentstate = states.pop();
						if(currentstate.IsGoalState(Items.size())) {
							bags = currentstate.bags;
							
							int numbags = 0;
							for(Bag b: bags)
							{
								if(b.items.size() != 0) 
									numbags++;
							}
							if(numbags < minBagsUsed) {
								minBagsUsed = numbags;
								bestbags = currentstate;
							}
							
							Collections.sort(currentstate.bags, new BagSort());
							
							System.out.println("success");
							for(Bag b: currentstate.bags) 
							{
								Collections.sort(b.items);
								for(int i: b.items) {
									System.out.print(rhmap.get(i)+"\t");
								}
								if(!b.items.isEmpty()) {
									System.out.println();
								}
							}
							if(doItOnce) {
								System.exit(0);
							}
							
						}// if IsGoalState.
						else { continue labelB; }
						
					}while(!states.empty());
					
				}// if !states.empty.
				
				else { fail(); }
				
			}// while !IsGoalState.
			
		// displays the best solution (i.e. one of the best solutions).
//		System.out.println("bestbags: ");
		for(Bag b: bestbags.bags) 
		{
//				for(int i: b.items) {
//					System.out.print(rhmap.get(i)+"\t");
//				}
				if(!b.items.isEmpty()) {
//					System.out.println();
				}
		}
			
	}//depth search.
		
}//main.

	private static void fail() {
		System.out.println("failure");
		//System.out.println((double)(System.currentTimeMillis()-start)/1000 + " seconds");
		System.exit(1);
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
	}// ItemSort.
	
	static class BagSort implements Comparator<Bag> 
	{ 
		@Override
	    public int compare(Bag a, Bag b) 
	    { 
	    	if(a.weight > b.weight)
	    		return -1;
	    	else if(a.weight < b.weight)
	    		return 1;
	    	else return 0;
	    } 
	}// BagSort.
	
}//Main_v2.
