//package grocery;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.Vector;


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
			//load state from stack
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
					states.push(new SearchState(newbags, currentItem+1));
				}
			}
			if(!states.empty()) {
			currentstate = states.pop();
			//System.out.println(currentItem);
			}
			else {
				fail();
			}
		}
		//System.out.println(currentstate.bags);
		System.out.println("success");
		for(Bag b: currentstate.bags) 
		{
			
			for(int i: b.items) {
				System.out.print(rhmap.get(i)+"\t");
			}
			if(!b.items.isEmpty())
				System.out.println();
		}
		//System.out.println((double)(System.currentTimeMillis()-start)/1000 + " seconds");
		}
		else if(args[1].equals("-breadth")) 
		{

			//System.out.println("breadth");
			String  mode = "-depth";
			
//			System.out.println("args.length: " + args.length);
			if(args.length==0 || args.length>2) {
				System.out.println("failure"); System.exit(1);
			}// if len==0.
			if(args.length==1) {
//				System.out.println("args[0]: "+ args[0]);
			}// if len==1.
			if(args.length==2) {
//				System.out.println("args[0]: "+ args[0]);
//				System.out.println("args[1]: "+ args[1]);
				mode = args[1];
			}// if len==2.
			
//			System.out.println("------------------------------");
			
			// TH: unique sets to be distributed out to all available buckets.
			Vector< Vector<Integer>> unique_sets = new Vector< Vector<Integer>>();
			
			// https://www.geeksforgeeks.org/different-ways-reading-text-file-java/
			File file = new File(args[0]);
//			File file = new File("C:\\Users\\tungh\\eclipse-workspace\\CS457-P1\\P1_\\src\\test_0_.txt");
			
			// TH: hashMap of item: id, weight.
			Map< String,Integer> hm = new HashMap< String,Integer>();
			Map< String,Integer> hm_weight = new HashMap< String,Integer>();
			
			// TH: hashMap of item: Integer, String.
			Map< Integer, String> hm_reverse = new HashMap< Integer, String>();
			
			// TH: hashMap of compatibility.
			Map< String,Vector<String>> hm_plus = new HashMap< String,Vector<String>>();
			Map< String,Vector<String>> hm_minus = new HashMap< String,Vector<String>>();
			
			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String st; StringTokenizer tokens; int id=-3;
				
				try {
					labelA:
					while ((st = br.readLine()) != null) {
//						System.out.println(st); 
						if (numbags==-1) {
							// TH: parses integer for number of bags with try/catch.
							try {
								numbags = Integer.parseInt(st);
								if (numbags<0) {
//									throw new IllegalArgumentException("Invalid Input: Number of Bags !!");
									System.out.println("failure"); System.exit(1);
								}//if < 0.
//								System.out.println(numbags); 
								id++; continue labelA;
							}//try.
							catch (NumberFormatException e){
//								System.out.println("Invalid Input: Number of Bags !!");
//								e.printStackTrace();
								System.out.println("failure"); System.exit(1);
							}// catch NumberFormatException.
						}//if numbags.
						if(sizebags==-1) {
							// TH: parses integer for size of bags with try/catch.
							try {
								sizebags = Integer.parseInt(st);
								if (sizebags<0) {
//									throw new IllegalArgumentException("Invalid Input: Size of Bags !!");
									System.out.println("failure"); System.exit(1);
								}//if < 0.
//								System.out.println(sizebags); 
								id++; continue labelA;
							}//try.
							catch (NumberFormatException e){
//								System.out.println("Invalid Input: Size of Bags !!");
//								e.printStackTrace();
								System.out.println("failure"); System.exit(1);
							}// catch NumberFormatException.
						}//if sizebags.
						
						// TH: parses individual remaining lines.
						tokens = new StringTokenizer(st);
						
						// https://www.geeksforgeeks.org/map-interface-java-examples/
						if(tokens.countTokens()>2) {
							
							// TH: parses item, weight, and sign.
							String item="dummy", weight="-1", sign="?";
							if(tokens.hasMoreTokens()) { item = tokens.nextToken(); }
							if(tokens.hasMoreTokens()) { 
								weight = tokens.nextToken();
								if(Integer.parseInt(weight)<0) {
//									throw new IllegalArgumentException("Invalid Input: Item Weight !!");
									System.out.println("failure"); System.exit(1);
								}//if weight < 0.
								if(Integer.parseInt(weight)>sizebags) {
									System.out.println("failure"); System.exit(1);
								}//if weight>sizebags.
							}//if hasMoreTokens for weight.
							if(tokens.hasMoreTokens()) {
								if(((String)sign).compareTo("?")==0) {
									sign = tokens.nextToken();
//									System.out.println("Sign: "+ sign); 
									if(((String)sign).length()!=1) {
//										throw new IllegalArgumentException("Invalid Input: Item Signage 0 !!");
										System.out.println("failure"); System.exit(1);
									}// if len!=1.
									if(((String)sign).compareTo("+")!=0 && ((String)sign).compareTo("-")!=0) {
//										throw new IllegalArgumentException("Invalid Input: Item Signage 1 !!");
										System.out.println("failure"); System.exit(1);
									}// if NOT + and NOT - signs.
								}
							}//if hasMoreTokens for signage.
							
							// TH: checks empty compatibility list.
							if(((String)sign).compareTo("+")==0 || ((String)sign).compareTo("-")==0) {
								if(!tokens.hasMoreTokens()) {
//									throw new IllegalArgumentException("Invalid Input: Not Enough Item Information !!");
									System.out.println("failure"); System.exit(1);
								}
							}//if + or -, but NO compatible items.
							
							// TH: updates hashMap for item: id, weight.
							id++; 
							hm.put(item, (Integer) id);
//							if(Integer.parseInt(weight)<0) throw new IllegalArgumentException("Invalid Input: Item Weight !!");
							hm_weight.put(item, Integer.parseInt(weight));
							
							if(((String)sign).compareTo("+")==0) {
								// TH: updates positive dependency.
								Vector<String> items_plus = new Vector<String>();
								while(tokens.hasMoreTokens()) { 
//									String temp_=(String)tokens.nextToken();
//									System.out.println(temp_);
									items_plus.add((String)tokens.nextToken());
								}//while hasMoreTokens AND list of + items.
								hm_plus.put(item, items_plus);
							}//if +.
							else if(((String)sign).compareTo("-")==0) {
								// TH: updates negative dependency.
								Vector<String> items_minus = new Vector<String>();
								while(tokens.hasMoreTokens()) { 
//									String temp_=(String)tokens.nextToken();
//									System.out.println(temp_);
									items_minus.add((String)tokens.nextToken());
								}//while hasMoreTokens AND list of - items.
								hm_minus.put(item, items_minus);
							}//else if -.
							else {
//								System.out.println("WARNING: Should NOT be here !!");
								System.out.println("failure"); System.exit(1);
							}
							continue labelA;
						}// if >2.
						else if(tokens.countTokens()==2) {
							
							/*
							 * TH: no compatibility available, simply stores id and weight.
							 */
							
//							System.out.println("No Compatibility Provided.");
							
							String item = tokens.nextToken();
							String weight = tokens.nextToken();
							if(Integer.parseInt(weight)<0) {
//								throw new IllegalArgumentException("Invalid Input: Item Weight !!");
								System.out.println("failure"); System.exit(1);
							}//if weight<0.
							// TH: updates hashMap for item: id, weight.
							id++; 
							hm.put(item, (Integer) id);
							hm_weight.put(item, Integer.parseInt(weight));
							
							// TH: stores in unique sets.
							Vector<Integer> temp_ = new Vector<Integer>();
							temp_.add(id);
							unique_sets.add(temp_);
							
							continue labelA;
						}// if==2.
						else {
//							throw new IllegalArgumentException("Invalid Input: Not Enough Item Information !!");
							System.out.println("failure"); 
							System.exit(1);
						}// else<2.
						
					}//while readLine.
					
				}// try readLine.
				catch (IOException e) {
//					System.out.println("readLine Failed !!");
//					e.printStackTrace();
					System.out.println("failure"); 
					System.exit(1);
				}//catch IOException.
				
			} //try BufferedReader.
			catch (FileNotFoundException e) {
//				System.out.println("BufferedReader or FileReader Failed !!");
//				e.printStackTrace();
				System.out.println("failure"); 
				System.exit(1);
			}//catch FileNotFoundException.
			

//			System.out.println("Mapping String To Id:");
//			Set< Map.Entry< String,Integer> > st_hm = hm.entrySet();
//			for (Map.Entry< String,Integer> map_entry:st_hm) {
//				System.out.print(map_entry.getKey()+": "); 
//				System.out.println(map_entry.getValue() + " (id)"); 
//			}
			
//			System.out.println("Mapping String To Weight:");
//			Set< Map.Entry< String,Integer> > st_hm_weight = hm_weight.entrySet();
//			for (Map.Entry< String,Integer> map_entry:st_hm_weight) {
//				System.out.print(map_entry.getKey()+": "); 
//				System.out.println(map_entry.getValue() + " (weight)"); 
//			}
			
			Set< Map.Entry< String,Integer> > st_hm_2nd = hm.entrySet();
			for (Map.Entry< String,Integer> map_entry:st_hm_2nd) {
				hm_reverse.put(map_entry.getValue(), map_entry.getKey());
			}
			
//			System.out.println("Mapping Id To String:");
//			Set< Map.Entry< Integer,String> > st_hm_reverse = hm_reverse.entrySet();
//			for (Map.Entry< Integer,String> map_entry:st_hm_reverse) {
//				System.out.print(map_entry.getKey()+": "); 
//				System.out.println(map_entry.getValue() + " (id)"); 
//			}
			
//			System.out.print("unique_sets (Generated From File): ");
//			System.out.println(unique_sets);
			
//			System.out.print("hm_plus (Generated From File): ");
//			System.out.println(hm_plus);
			
//			System.out.print("hm_minus (Generated From File): ");
//			System.out.println(hm_minus);
			
			// TH: attempts to connect a loner to everybody else, in case NO positive logic has been generated.
			while(unique_sets.size()>0) {
				Vector<Integer> temp_vect_ = unique_sets.remove(0);
				Integer temp_int_ = temp_vect_.remove(0);
				
				Vector<Integer> hm_plus_vect_ = new Vector<Integer> ();
				Set< Map.Entry< String,Integer> > st_hm_3rd = hm.entrySet();
				for (Map.Entry< String,Integer> map_entry:st_hm_3rd) {
					hm_plus_vect_.add(map_entry.getValue());
				}
				if(hm_plus_vect_.contains(temp_int_)) hm_plus_vect_.remove((Integer)temp_int_);
				
				// TH: represents a Vector of positive compatibility for the loner.
//				System.out.println("hm_plus_vect_: " + hm_plus_vect_);
				
				// hm_plus.put(item, everybody);
				// Map< String,Vector<String>> hm_plus ;
				
				String loner = hm_reverse.get(temp_int_);
				Vector<String> everybody = new Vector<String> ();
				while(hm_plus_vect_.size()>0) {
					everybody.add(hm_reverse.get(hm_plus_vect_.remove(0)));
				}// while size>0.
				
				hm_plus.put(loner, everybody);
				
//				System.out.print("hm_plus (After Initial Processing): ");
//				System.out.println(hm_plus);
				
			}// while size>0.
			
//			System.out.print("unique_sets (After Initial Processing): ");
//			System.out.println(unique_sets);
			
			// TH: 	
			//	There  is NO need to implement the following incompatibility: hm_minus.put(item, everybody);
			//	Because the assumption is that a loner is compatible with everybody by default.
			
			
			
			// TH: positive logic.
//			System.out.println("Mapping String To Plus List:");
			Map< Integer,Vector<Integer>> pos_rc_pos_list = new HashMap< Integer,Vector<Integer>>();
			Map< Integer,Vector<Integer>> neg_rc_pos_list = new HashMap< Integer,Vector<Integer>>();
			Set< Map.Entry< String,Vector<String>> >st_hm_plus = hm_plus.entrySet();
			for (Map.Entry< String,Vector<String>> map_entry_plus:st_hm_plus) {
				String row_pos=map_entry_plus.getKey();
//				System.out.print(map_entry_plus.getKey()+": "); 
//				System.out.println(map_entry_plus.getValue() + " (id)"); 
				
				Iterator<String> itr = map_entry_plus.getValue().iterator(); 
				
//				System.out.println("row_pos: "+ hm.get(row_pos));
				String col_pos="-1";
				Vector<Integer> col_pos_arr = new Vector<Integer>();
				// TH: makes sure that positive dependents of one target are also targets themselves. 
				// TH: collects items_plus id for columns of positive logic.
				while (itr.hasNext()) {
					col_pos=itr.next();
					if(!hm.containsKey(col_pos)) {
//						throw new IllegalArgumentException("Invalid Input: Item NOT Found !!");
						System.out.println("failure"); 
						System.exit(1);
					}
//					System.out.println("col_pos: "+ hm.get(col_pos));
					col_pos_arr.add(hm.get(col_pos));
				}// while hasNext.
				
				Map<Integer,Integer> col_neg_arr = new HashMap<Integer,Integer>();			
				for(int i=0; i<hm.size(); i++) col_neg_arr.put((Integer)i,(Integer)i);
				
//				System.out.println("Negative Logic For Columns (Before):");
				Set< Map.Entry<Integer,Integer> >st_col_neg_arr = col_neg_arr.entrySet();
//				for (Map.Entry<Integer,Integer> map_entry_col_neg_arr:st_col_neg_arr) {
//					System.out.print(map_entry_col_neg_arr.getKey()+": "); 
//					System.out.println(map_entry_col_neg_arr.getValue()); 
//				}
				
				Iterator<Integer> itr_arr = col_pos_arr.iterator();
				while (itr_arr.hasNext()) {
					Integer temp = itr_arr.next();
//					System.out.println("temp: " + temp);
					if(col_neg_arr.containsKey(temp)) {
						col_neg_arr.remove(temp, temp);
					}
				}
				
//				System.out.println("Negative Logic For Columns (After):");
//				st_col_neg_arr = col_neg_arr.entrySet();
//				for (Map.Entry<Integer,Integer> map_entry_col_neg_arr:st_col_neg_arr) {
//					System.out.print(map_entry_col_neg_arr.getKey()+": "); 
//					System.out.println(map_entry_col_neg_arr.getValue()); 
//				}
				
//				System.out.println("row_pos: "+ hm.get(row_pos));
				col_neg_arr.remove(hm.get(row_pos),hm.get(row_pos));
				
				Vector<Integer> col_neg_arr_list = new Vector<Integer>();
//				System.out.println("Negative Logic For Columns (After):");
				st_col_neg_arr = col_neg_arr.entrySet();
				for (Map.Entry<Integer,Integer> map_entry_col_neg_arr:st_col_neg_arr) {
//					System.out.print(map_entry_col_neg_arr.getKey()+": "); 
//					System.out.println(map_entry_col_neg_arr.getValue()); 
					col_neg_arr_list.add(map_entry_col_neg_arr.getValue());
				}
				
				/*
				 * TH:	assembles positive-logic row-column pairs (for each item in items_plus) 
				 * 				a) either these are the solutions.
				 * 				b) they will be filtered by negative-logic row-column pairs in negative-logic section. 
				 * 				AND avoid duplicates.
				 */
				pos_rc_pos_list.put((hm.get(row_pos)), col_pos_arr);
				
				/*
				 * 	TH:	assembles negative-logic row-column pairs (for each item NOT in items_plus) 
				 * 				to filter positive-logic row-column pairs in negative-logic section.
				 * 					hm.get(row_pos), col_neg_arr[:]
				 * 					i.e. (0,3), (0,4), (0,5)
				 * 				AND avoid duplicates.
				*/
				neg_rc_pos_list.put((hm.get(row_pos)), col_neg_arr_list);
				
			}// for st_hm_plus.
			
//			System.out.print("neg_rc_pos_list (Debug_2): "); System.out.println(neg_rc_pos_list);
			
//			System.out.println("Positive Logics From Positive Lists:");
//			{
//				Set<Entry<Integer, Vector<Integer>>>st_pos_rc_pos_list = pos_rc_pos_list.entrySet();
//				for (Entry<Integer, Vector<Integer>> map_entry:st_pos_rc_pos_list) {
//					System.out.print(map_entry.getKey()+": "); 
//					System.out.println(map_entry.getValue()); 
//				}
//			}
			
			// TH: generates a vector of vectors for positive logics from positive lists.
			Vector< Map<Integer,Integer>> pos_log_pos_list = new Vector< Map<Integer,Integer>>();
			Set<Entry<Integer, Vector<Integer>>>st_pos_rc_pos_list = pos_rc_pos_list.entrySet();
			Vector<Vector<Integer>> pos_log_pos_list_vect = new Vector<Vector<Integer>> ();
			for (Entry<Integer, Vector<Integer>> map_entry:st_pos_rc_pos_list) {
//				System.out.print(map_entry.getKey()+": "); 
//				System.out.println(map_entry.getValue()); 
				Vector<Integer> temp_vect= new Vector<Integer>();
				temp_vect.add(map_entry.getKey());
				Iterator<Integer> itr = map_entry.getValue().iterator(); 
				Integer pos_col_pos_list=-1;
				while (itr.hasNext()) {
					pos_col_pos_list=itr.next();
					temp_vect.add(pos_col_pos_list);
					Map< Integer,Integer> temp_ = new HashMap< Integer,Integer>();
					temp_.put(map_entry.getKey(), pos_col_pos_list);
					pos_log_pos_list.add(temp_);
				}// while hasNext.
				pos_log_pos_list_vect.add(temp_vect);
			}// for Entry.
//			System.out.print("pos_log_pos_list_vect (Initial): ");
//			System.out.println(pos_log_pos_list_vect);
			
			{
				// TH: sorts pos_log_pos_list_vect.
				Iterator<Vector<Integer>> itr = pos_log_pos_list_vect.iterator(); 
				while (itr.hasNext()) {
					Vector<Integer> temp_ = itr.next();
					// https://beginnersbook.com/2014/06/how-to-sort-vector-using-collections-sort-in-java-example/
					Collections.sort(temp_);
				}// while hasNext.
			}
//			System.out.print("pos_log_pos_list_vect (Sorted): ");
//			System.out.println(pos_log_pos_list_vect);
			
			// TH: avoids duplicates any pos_log_pos_list_vect.
			// https://way2java.com/collections/vector/removing-duplicates-from-vector/
			{
				HashSet<Vector<Integer>> hs1 = new HashSet<Vector<Integer>>(pos_log_pos_list_vect); 
				pos_log_pos_list_vect = new Vector< Vector<Integer>>(hs1);
			}
//			System.out.print("pos_log_pos_list_vect (No Duplicate): ");
//			System.out.println(pos_log_pos_list_vect);
			
//			System.out.println("Negative Logics From Positive Lists:");
			Vector< Map<Integer,Integer>> neg_log_pos_list = new Vector< Map<Integer,Integer>>();
			Set<Entry<Integer, Vector<Integer>>>st_neg_rc_pos_list = neg_rc_pos_list.entrySet();
			for (Entry<Integer, Vector<Integer>> map_entry:st_neg_rc_pos_list) {
//				System.out.print(map_entry.getKey()+": "); 
//				System.out.println(map_entry.getValue()); 
				
				if(map_entry.getValue().size()==0) {
//					System.out.println(map_entry.getKey() + ": compatible with everyone else !!");
				}// if empty == compatible with everyone else.
				else {
					Iterator<Integer> itr = map_entry.getValue().iterator(); 
					Integer neg_col_pos_list=-1;
					while (itr.hasNext()) {
						neg_col_pos_list=itr.next();
						Map< Integer,Integer> temp_ = new HashMap< Integer,Integer>();
						
						temp_.put(map_entry.getKey(), neg_col_pos_list);
						neg_log_pos_list.add(temp_);
					}// while hasNext.
				}// else.
			}// for Entry.
//			System.out.print("neg_log_pos_list (Debug_1): ");
//			System.out.println(neg_log_pos_list);
			
			// TH: generates a vector of vectors for negative logics from positive lists.
			Vector< Vector<Integer>> neg_log_pos_list_vect = new Vector< Vector<Integer>>();
			{
				Iterator<Map<Integer,Integer>> itr = neg_log_pos_list.iterator(); 
				while (itr.hasNext()) {
					Map<Integer,Integer> temp_ = itr.next();
					Set< Map.Entry< Integer,Integer> > st_temp_ = temp_.entrySet();
					Vector<Integer> temp_vect= new Vector<Integer>();
					for (Map.Entry< Integer,Integer> map_entry:st_temp_) {
//						System.out.print(map_entry.getKey()+": "); 
//						System.out.println(map_entry.getValue()); 
						temp_vect.add(map_entry.getKey());
						temp_vect.add(map_entry.getValue());
						neg_log_pos_list_vect.add(temp_vect);
					}// for Entry.
				}// while hasNext.
			}
//			System.out.print("neg_log_pos_list_vect (Initial): ");
//			System.out.println(neg_log_pos_list_vect);
			
			{
				// TH: sorts neg_log_pos_list_vect.
				Iterator<Vector<Integer>> itr = neg_log_pos_list_vect.iterator(); 
				while (itr.hasNext()) {
					Vector<Integer> temp_ = itr.next();
					// https://beginnersbook.com/2014/06/how-to-sort-vector-using-collections-sort-in-java-example/
					Collections.sort(temp_);
				}// while hasNext.
			}
//			System.out.print("neg_log_pos_list_vect (Sorted): ");
//			System.out.println(neg_log_pos_list_vect);
			
			// TH: avoids duplicates any neg_log_pos_list_vect.
			// https://way2java.com/collections/vector/removing-duplicates-from-vector/
			{
				HashSet<Vector<Integer>> hs1 = new HashSet<Vector<Integer>>(neg_log_pos_list_vect); 
				neg_log_pos_list_vect = new Vector< Vector<Integer>>(hs1);
			}
//			System.out.print("neg_log_pos_list_vect (No Duplicate): ");
//			System.out.println(neg_log_pos_list_vect);
			
			// TH: negative logic.
//			System.out.println("Mapping String To Minus List:");
			Map< Integer,Vector<Integer>> pos_rc_neg_list = new HashMap< Integer,Vector<Integer>>();
			Map< Integer,Vector<Integer>> neg_rc_neg_list = new HashMap< Integer,Vector<Integer>>();
			Set< Map.Entry< String,Vector<String>> >st_hm_minus = hm_minus.entrySet();
			for (Map.Entry< String,Vector<String>> map_entry_minus:st_hm_minus) {
				String row_neg=map_entry_minus.getKey();
//				System.out.print(map_entry_minus.getKey()+": "); 
//				System.out.println(map_entry_minus.getValue() + " (id)"); 
				
				Iterator<String> itr = map_entry_minus.getValue().iterator(); 
				
//				System.out.println("row_neg: "+ hm.get(row_neg));
				String col_neg="-1";
				Vector<Integer> col_neg_arr = new Vector<Integer>();
				// TH: makes sure that negative dependents of one target are also targets themselves. 
				// TH: collects items_minus id for columns of negative logic.
				while (itr.hasNext()) {
					col_neg=itr.next();
					if(!hm.containsKey(col_neg)) {
//						throw new IllegalArgumentException("Invalid Input: Item NOT Found !!");
						System.out.println("failure"); 
						System.exit(1);
					}
//					System.out.println("col_neg: "+ hm.get(col_neg));
					col_neg_arr.add(hm.get(col_neg));
				}// while hasNext.
				
//				System.out.print("col_neg_arr (Debug_3): "); System.out.println(col_neg_arr);
				
				
				Map<Integer,Integer> col_pos_arr = new HashMap<Integer,Integer>();			
				for(int i=0; i<hm.size(); i++) col_pos_arr.put((Integer)i,(Integer)i);
				
//				System.out.print("col_pos_arr (Debug_4): "); System.out.println(col_pos_arr);
				
//				System.out.println("Positive Logic For Columns (Before):");
				Set< Map.Entry<Integer,Integer> >st_col_pos_arr = col_pos_arr.entrySet();
//				for (Map.Entry<Integer,Integer> map_entry_col_pos_arr:st_col_pos_arr) {
//					System.out.print(map_entry_col_pos_arr.getKey()+": "); 
//					System.out.println(map_entry_col_pos_arr.getValue()); 
//				}
				
				Iterator<Integer> itr_arr = col_neg_arr.iterator();
				while (itr_arr.hasNext()) {
					Integer temp = itr_arr.next();
//					System.out.println("temp: " + temp);
					if(col_pos_arr.containsKey(temp)) {
						col_pos_arr.remove(temp, temp);
					}// if containsKey.
				}// while hasNext.
				
//				System.out.println("row_neg: "+ row_neg + " (" + hm.get(row_neg) + ")");
				col_pos_arr.remove(hm.get(row_neg),hm.get(row_neg));
				
//				System.out.print("col_pos_arr (Debug_5): "); System.out.println(col_pos_arr);
				
				Vector<Integer> col_pos_arr_list = new Vector<Integer>();
//				System.out.println("Positive Logic For Columns (After):");
				st_col_pos_arr = col_pos_arr.entrySet();
				for (Map.Entry<Integer,Integer> map_entry_col_pos_arr:st_col_pos_arr) {
//					System.out.print(map_entry_col_pos_arr.getKey()+": "); 
//					System.out.println(map_entry_col_pos_arr.getValue()); 
					col_pos_arr_list.add(map_entry_col_pos_arr.getValue());
				}
				
				/*
				 * TH:	assembles negative-logic row-column pairs (for each item in items_minus) 
				 * 				a) they will help filter out positive-logic row-column pairs in positive-logic section.
				 */
				neg_rc_neg_list.put((hm.get(row_neg)), col_neg_arr);
				
				/*
				 * 	TH:	assembles positive-logic row-column pairs (for each item NOT in items_minus) 
				 * 				to be filtered out by negative-logic row-column pairs in positive-logic section.
				 * 					hm.get(col_pos), col_pos_arr[:]
				 * 					i.e. (0,3), (0,4), (0,5)
				*/
				pos_rc_neg_list.put((hm.get(row_neg)), col_pos_arr_list);
				
			}// for st_hm_minus.
			
//			System.out.print("pos_rc_neg_list: "); System.out.println(pos_rc_neg_list);
			
//			System.out.println("Negative Logics From Negative Lists:");
//			{
//				Set<Entry<Integer, Vector<Integer>>>st_neg_rc_neg_list = neg_rc_neg_list.entrySet();
//				for (Entry<Integer, Vector<Integer>> map_entry:st_neg_rc_neg_list) {
//					System.out.print(map_entry.getKey()+": "); 
//					System.out.println(map_entry.getValue()); 
//				}
//			}
			
			// TH: avoids duplicates in Negative Logics From Negative Lists.
			Vector< Map<Integer,Integer>> neg_log_neg_list = new Vector< Map<Integer,Integer>>();
			Set<Entry<Integer, Vector<Integer>>>st_neg_rc_neg_list = neg_rc_neg_list.entrySet();
			
			for (Entry<Integer, Vector<Integer>> map_entry:st_neg_rc_neg_list) {
//				System.out.print(map_entry.getKey()+": "); 
//				System.out.println(map_entry.getValue()); 
				Vector<Integer> temp_vect= new Vector<Integer>();
				temp_vect.add(map_entry.getKey());
				Iterator<Integer> itr = map_entry.getValue().iterator(); 
				Integer neg_col_neg_list=-1;
				while (itr.hasNext()) {
					neg_col_neg_list=itr.next();
					temp_vect.add(neg_col_neg_list);
					Map< Integer,Integer> temp_ = new HashMap< Integer,Integer>();
					temp_.put(map_entry.getKey(), neg_col_neg_list);
					neg_log_neg_list.add(temp_);
				}// while hasNext.
			}// for Entry.
//			System.out.print("neg_log_neg_list (Debug_6): ");
//			System.out.println(neg_log_neg_list);
			
			// TH: generates a vector of vectors for neg_log_neg_list_vect.
			Vector< Vector<Integer>> neg_log_neg_list_vect = new Vector< Vector<Integer>>();
			{
				Iterator<Map<Integer,Integer>> itr = neg_log_neg_list.iterator(); 
				while (itr.hasNext()) {
					Map<Integer,Integer> temp_ = itr.next();
					Set< Map.Entry< Integer,Integer> > st_temp_ = temp_.entrySet();
					Vector<Integer> temp_vect= new Vector<Integer>();
					for (Map.Entry< Integer,Integer> map_entry:st_temp_) {
//						System.out.print(map_entry.getKey()+": "); 
//						System.out.println(map_entry.getValue()); 
						temp_vect.add(map_entry.getKey());
						temp_vect.add(map_entry.getValue());
						neg_log_neg_list_vect.add(temp_vect);
					}// for Entry.
				}// while hasNext.
			}
//			System.out.print("neg_log_neg_list_vect (Initial): ");
//			System.out.println(neg_log_neg_list_vect);
			
			{
				// TH: sorts neg_log_neg_list_vect.
				Iterator<Vector<Integer>> itr = neg_log_neg_list_vect.iterator(); 
				while (itr.hasNext()) {
					Vector<Integer> temp_ = itr.next();
					// https://beginnersbook.com/2014/06/how-to-sort-vector-using-collections-sort-in-java-example/
					Collections.sort(temp_);
				}// while hasNext.
			}
//			System.out.print("neg_log_neg_list_vect (Sorted): ");
//			System.out.println(neg_log_neg_list_vect);
			
			// TH: avoids duplicates any neg_log_neg_list_vect.
			// https://way2java.com/collections/vector/removing-duplicates-from-vector/
			{
				HashSet<Vector<Integer>> hs1 = new HashSet<Vector<Integer>>(neg_log_neg_list_vect);
				neg_log_neg_list_vect = new Vector< Vector<Integer>>(hs1);
			}
//			System.out.print("neg_log_neg_list_vect (No Duplicate): ");
//			System.out.println(neg_log_neg_list_vect);
			
//			System.out.println("Positive Logics From Negative Lists:");
			Vector< Map<Integer,Integer>> pos_log_neg_list = new Vector< Map<Integer,Integer>>();
			Set<Entry<Integer, Vector<Integer>>>st_pos_rc_neg_list = pos_rc_neg_list.entrySet();
			Vector<Integer> incompatibility = new Vector<Integer> ();
			for (Entry<Integer, Vector<Integer>> map_entry:st_pos_rc_neg_list) {
//				System.out.print(map_entry.getKey()+": "); 
//				System.out.println(map_entry.getValue()); 
				if(map_entry.getValue().size()==0) {
					// TH: stores in unique sets.
					Vector<Integer> temp_ = new Vector<Integer>();
					temp_.add(map_entry.getKey());
//					System.out.println("temp_: "+temp_);
					unique_sets.add(temp_);
					incompatibility.add(map_entry.getKey());
//					System.out.println("incompatible with everyone else !!"); 
				}// if empty == incompatible with everyone else.
				else {
					Iterator<Integer> itr = map_entry.getValue().iterator(); 
					Integer pos_col_neg_list=-1;
					while (itr.hasNext()) {
						pos_col_neg_list=itr.next();
						Map< Integer,Integer> temp_ = new HashMap< Integer,Integer>();
						temp_.put(map_entry.getKey(), pos_col_neg_list);
						pos_log_neg_list.add(temp_);
					}// while hasNext.
				}// else.
			}//for Entry.
//			System.out.print("pos_log_neg_list: ");
//			System.out.println(pos_log_neg_list);
			
			// TH: avoids duplicates any incompatibility.
			// https://way2java.com/collections/vector/removing-duplicates-from-vector/
			{
				HashSet<Integer> hs1 = new HashSet<Integer>(incompatibility); 
				incompatibility = new Vector<Integer>(hs1);
			}
			
			// TH: generates a vector of vectors for pos_log_neg_list_vect.
			Vector< Vector<Integer>> pos_log_neg_list_vect = new Vector< Vector<Integer>>();
			{
				Iterator<Map<Integer,Integer>> itr = pos_log_neg_list.iterator(); 
				while (itr.hasNext()) {
					Map<Integer,Integer> temp_ = itr.next();
					Set< Map.Entry< Integer,Integer> > st_temp_ = temp_.entrySet();
					Vector<Integer> temp_vect= new Vector<Integer>();
					for (Map.Entry< Integer,Integer> map_entry:st_temp_) {
//						System.out.print(map_entry.getKey()+": "); 
//						System.out.println(map_entry.getValue()); 
						temp_vect.add(map_entry.getKey());
						temp_vect.add(map_entry.getValue());
						pos_log_neg_list_vect.add(temp_vect);
					}// for Entry.
				}// while hasNext.
			}
//			System.out.print("pos_log_neg_list_vect (Initial): ");
//			System.out.println(pos_log_neg_list_vect);
			
			{
				// TH: sorts pos_log_neg_list_vect.
				Iterator<Vector<Integer>> itr = pos_log_neg_list_vect.iterator(); 
				while (itr.hasNext()) {
					Vector<Integer> temp_ = itr.next();
					// https://beginnersbook.com/2014/06/how-to-sort-vector-using-collections-sort-in-java-example/
					Collections.sort(temp_);
				}// while hasNext.
			}
//			System.out.print("pos_log_neg_list_vect (Sorted): ");
//			System.out.println(pos_log_neg_list_vect);
			
			// TH: avoids duplicates any pos_log_neg_list_vect.
			// https://way2java.com/collections/vector/removing-duplicates-from-vector/
			{
				HashSet<Vector<Integer>> hs1 = new HashSet<Vector<Integer>>(pos_log_neg_list_vect); 
				pos_log_neg_list_vect = new Vector< Vector<Integer>>(hs1);
			}
//			System.out.print("pos_log_neg_list_vect (No Duplicate): ");
//			System.out.println(pos_log_neg_list_vect);
			
			// TH: filters out positive logics from negative lists using negative logics from positive lists. 
			{
				Vector< Vector<Integer>> pos_log_neg_list_vect_copy = new Vector< Vector<Integer>>(pos_log_neg_list_vect);
				Iterator<Vector<Integer>> itr = pos_log_neg_list_vect.iterator(); 
				while (itr.hasNext()) {
					Vector<Integer> temp_ = itr.next();
					if(neg_log_pos_list_vect.contains(temp_)) {
//						System.out.println("temp_: ");
//						System.out.println(temp_);
						pos_log_neg_list_vect_copy.remove(temp_);
					}// if contains.
				}//while hasNext.
				pos_log_neg_list_vect = pos_log_neg_list_vect_copy;
			}
			
//			System.out.print("pos_log_neg_list_vect (Debug_0): "); System.out.println(pos_log_neg_list_vect);
			
			// TH: merges negative logics together.
			Vector< Vector<Integer>> neg_log_vect = new Vector< Vector<Integer>>();
			{
				Iterator<Vector<Integer>> itr = neg_log_neg_list_vect.iterator();
				while (itr.hasNext()) {
					neg_log_vect.add(itr.next());
				}// while hasNext.
				
				itr = neg_log_pos_list_vect.iterator(); 
				while (itr.hasNext()) {
					Vector<Integer> temp_ = itr.next();
					if(neg_log_neg_list_vect.contains(temp_)==false) {
						neg_log_vect.add(temp_);
					}// if false.
				}// while hasNext.
			}
//			System.out.print("neg_log_vect (No Duplicate): "); System.out.println(neg_log_vect);
			
//			System.out.print("pos_log_pos_list_vect (Debug_7a): "); System.out.println(pos_log_pos_list_vect);
			
			// 	breaks into key,value==Vector(sorted)
			// 	checks [key,value[i]] against neg_log_vect.
			// 		if contains in neg_log_vect, then remove element from value==Vector(sorted).
			{
				// TH: filters positive logics from positive lists using combined negative logics, neg_log_vect.
				int numElements = pos_log_pos_list_vect.size();
				for(int k=0; k<numElements; k++) {
					Vector<Integer> toBeRemoved = new Vector<Integer>();
					Vector<Integer> vect_temp_ = pos_log_pos_list_vect.remove(0);
					Integer key_ = vect_temp_.get(0);
					int vect_temp_size = vect_temp_.size();
					for(int i=1; i<vect_temp_size; i++) {
						Integer value_ = vect_temp_.get(i);
						Vector<Integer> target = new Vector<Integer>();
						target.add(key_); target.add(value_);
						Collections.sort(target);
//						System.out.print("target (Debug_8): "); System.out.println(target);
						if(neg_log_vect.contains(target)) {
							toBeRemoved.add(value_);
						}//if contains.
//						System.out.print("toBeRemoved (Debug_9): "); System.out.println(toBeRemoved);
						
					}//for i.
					for(int i=0; i<toBeRemoved.size(); i++) {
						vect_temp_.remove((Integer)toBeRemoved.get(i));
					}//for i.
					
					pos_log_pos_list_vect.add(vect_temp_);
					
				}// for k.
				
			}
			
//			System.out.print("pos_log_pos_list_vect (Debug_7b): "); System.out.println(pos_log_pos_list_vect);
			
//			System.out.print("incompatibility: "); System.out.println(incompatibility);
//			System.out.print("pos_log_neg_list_vect (Debug_10a): "); System.out.println(pos_log_neg_list_vect);
			
			{
				// TH: filters out incompatible loners in positive logics from negative lists.
				Iterator<Vector<Integer>> itr = pos_log_neg_list_vect.iterator(); 
				while (itr.hasNext()) {
					Vector<Integer> temp_= itr.next();
					Iterator<Integer> itr_ = incompatibility.iterator();
					while (itr_.hasNext()) {
						Integer incomp_loner = itr_.next();
						if(temp_.contains(incomp_loner)) {
							temp_.remove(incomp_loner);
						}// if contains.
					}// while hasNext.
					// TH: stores in unique sets: 
					if(temp_.size()>1) {
//						System.out.println("temp_: ");
//						System.out.println(temp_);
						unique_sets.add(temp_);
					}// if size>1.
					
				}// while hasNext.
			}
			
//			System.out.print("pos_log_neg_list_vect (Debug_10b): "); System.out.println(pos_log_neg_list_vect);
			
			{
				// TH: avoids singletons already grouped in pairs in pos_log_neg_list_vect.
				Vector<Integer> indexToBeDeleted=new Vector<Integer>();
				if(pos_log_neg_list_vect.size()>1) {
					for(int i=0; i<pos_log_neg_list_vect.size()-1; i++) {
						Vector<Integer> reference = pos_log_neg_list_vect.get(i);
						if(reference.size()==1) {
							Integer temp_=reference.get(0);
							for(int j=i+1; j<pos_log_neg_list_vect.size(); j++) {
								Vector<Integer> target=pos_log_neg_list_vect.get(j);
								if(target.contains((Integer) temp_)) {
//									System.out.print("temp_: "); System.out.print(temp_);
//									System.out.print("\t@ index "); System.out.println(i);
									indexToBeDeleted.add(i);
								}// if contains.
							}// for j.
						}// if size==1. 
					}// for i.
				}// if size>1.
//				System.out.print("indexToBeDeleted: "); System.out.println(indexToBeDeleted);
				Iterator<Integer> itr = indexToBeDeleted.iterator();
				while (itr.hasNext()) {
					int index_ = itr.next();
					pos_log_neg_list_vect.remove(index_);
				}// while hasNext.
			}
			
//			System.out.print("pos_log_neg_list_vect (Debug_10c): "); System.out.println(pos_log_neg_list_vect);
//			System.out.print("incompatibility (Debug_11a): "); System.out.println(incompatibility);
//			System.out.print("unique_sets (Debug_11a): "); System.out.println(unique_sets);
			
			/**
			* 	TH:	recovers incompatible loners for unique_sets.
			* 			processes neg_log_vect --- Vector< Vector<Integer>>
			* 			potentially, adds loners to incompatibility.
			*/
			{
				for(int i=0; i<hm.size(); i++){
					Iterator<Vector<Integer>> itr=neg_log_vect.iterator();
					Vector<Integer> reference = new Vector<Integer>();
					Vector<Integer> target = new Vector<Integer>();
					for(int j=0; j<hm.size(); j++) {
						reference.add(j);
					}// for j.
		//			System.out.println("i: "+i);
					while(itr.hasNext()) {
						Vector<Integer> temp_=itr.next();
//						System.out.println("temp_: "+temp_);
						if(temp_.get(0)==i) {
							reference.remove((Integer)i);
							target.add(temp_.get(1));
						}// if key==i.
						else if(temp_.get(1)==i) {
							reference.remove((Integer)i);
							target.add(temp_.get(0));
						}//else if value==i.
					}// while hasNext.
//					System.out.println("i: "+i);
//					System.out.println("reference: "+reference);
					Collections.sort(target);
//					System.out.println("target: "+target);
					if(target.equals(reference)) {
						incompatibility.add((Integer)i);
						Vector<Integer> loner = new Vector<Integer>();
						loner.add((Integer)i);
						unique_sets.add(loner);
					}
				}// for i < hm.size()
			}
//			System.out.print("incompatibility (Debug_11b): "); System.out.println(incompatibility);
//			System.out.print("unique_sets (Debug_11b): "); System.out.println(unique_sets);
			
			// TH: avoids duplicates any incompatibility.
			// https://way2java.com/collections/vector/removing-duplicates-from-vector/
			{
				HashSet<Integer> hs1 = new HashSet<Integer>(incompatibility); 
				incompatibility = new Vector<Integer>(hs1);
			}
			
//			System.out.print("pos_log_pos_list_vect (Debug_12a): "); System.out.println(pos_log_pos_list_vect);
			
			{
				// TH: filters out incompatible loners in positive logics from positive lists.
				Iterator<Vector<Integer>> itr = pos_log_pos_list_vect.iterator(); 
				while (itr.hasNext()) {
					Vector<Integer> temp_= itr.next();
					Iterator<Integer> itr_ = incompatibility.iterator();
					while (itr_.hasNext()) {
						Integer incomp_loner = itr_.next();
//						System.out.print("incomp_loner: ");
//						System.out.println(incomp_loner);
						if(temp_.contains(incomp_loner)) {
							temp_.remove(incomp_loner);
						}// if contains.
					}// while hasNext.
				}// while hasNext.
			}
			
//			System.out.print("pos_log_pos_list_vect (Debug_12b): "); System.out.println(pos_log_pos_list_vect);
			
			{
				// TH: avoids singletons already grouped in pairs in pos_log_pos_list_vect.
				Vector<Integer> indexToBeDeleted=new Vector<Integer>();
				if(pos_log_pos_list_vect.size()>1) {
					for(int i=0; i<pos_log_pos_list_vect.size()-1; i++) {
						Vector<Integer> reference = pos_log_pos_list_vect.get(i);
						if(reference.size()==1) {
							Integer temp_=reference.get(0);
							for(int j=i+1; j<pos_log_pos_list_vect.size(); j++) {
								Vector<Integer> target=pos_log_pos_list_vect.get(j);
								if(target.contains((Integer) temp_)) {
//									System.out.print("temp_: "); System.out.print(temp_);
//									System.out.print("\t@ index "); System.out.println(i);
									indexToBeDeleted.add(i);
								}// if contains.
							}// for j.
						}// if size==1. 
					}// for i.
				}// if size>1.
//				System.out.print("indexToBeDeleted: "); System.out.println(indexToBeDeleted);
				Iterator<Integer> itr = indexToBeDeleted.iterator();
				while (itr.hasNext()) {
					int index_ = itr.next();
					pos_log_pos_list_vect.remove(index_);
				}// while hasNext.
			}
			
//			System.out.print("pos_log_pos_list_vect (Debug_12c): "); System.out.println(pos_log_pos_list_vect);
			
			for(int i=0; i<neg_log_vect.size(); i++){
				if(unique_sets.contains(neg_log_vect.get(i))) {unique_sets.remove(neg_log_vect.get(i));}
			}// for i.
			
//			System.out.print("unique_sets (Debug_13a): "); System.out.println(unique_sets);
			
			{
				// TH: adds pos_log_pos_list_vect to unique_sets.
				Iterator<Vector<Integer>> itr = pos_log_pos_list_vect.iterator();
				while (itr.hasNext()) {
					Vector<Integer> temp_ = itr.next();
					unique_sets.add(temp_);
				}//while hasNext.
			}
			
//			System.out.print("unique_sets (Debug_13b): "); System.out.println(unique_sets);
			
			// TH: avoids duplicates in unique_sets.
			// https://way2java.com/collections/vector/removing-duplicates-from-vector/
			{
				HashSet<Vector<Integer>> hs1 = new HashSet<Vector<Integer>>(unique_sets); 
				unique_sets = new Vector< Vector<Integer>>(hs1);
			}
			
//			System.out.print("pos_log_pos_list_vect (Filtered): ");
//			System.out.println(pos_log_pos_list_vect);
			
//			System.out.print("pos_log_neg_list_vect (Filtered): ");
//			System.out.println(pos_log_neg_list_vect);
			
//			System.out.print("incompatibility: ");
//			System.out.println(incompatibility);
			
//			System.out.print("unique_sets (Initial, Before): ");
//			System.out.println(unique_sets);
			
//			for(int i=0; i<unique_sets.size(); i++){
//				Vector<Integer> vect_temp_ = unique_sets.get(i);
//				Collections.sort(vect_temp_);
//			}// for i.
			
			// TH: sorted and avoids duplicates.
			
			for(int i=0; i<neg_log_vect.size(); i++){
				if(unique_sets.contains(neg_log_vect.get(i))) {unique_sets.remove(neg_log_vect.get(i));}
			}//for i.
			
			for(int i=0; i<unique_sets.size(); i++) {
				Collections.sort(unique_sets.get(i));
			}// for i.
			
			// TH: avoids duplicates in unique_sets.
			// https://way2java.com/collections/vector/removing-duplicates-from-vector/
			{
				HashSet<Vector<Integer>> hs1 = new HashSet<Vector<Integer>>(unique_sets); 
				unique_sets = new Vector< Vector<Integer>>(hs1);
			}
			
			for(int i=0; i<unique_sets.size(); i++) {
				if(unique_sets.get(i).size()==0) unique_sets.remove(i);
			}// for i.
			
//			System.out.print("unique_sets (Initial, After): ");
//			System.out.println(unique_sets);
			
			/**
			 * TH: sorted by length of items within enclosing vector.
			 * Before: 	unique_sets: [[4], [0, 1, 2], [3, 5]]
			 * After:		unique_sets: [[4], [3, 5], [0, 1, 2]]
			 */
			{
				// TH: holds lengths of all Vectors in initial unique_sets.
				Vector<Integer> unique_sets_len_vect = new Vector<Integer>();
				// TH: holds mappings of lengths of Vectors to their contents.
				Vector< Map<Integer,Vector<Integer>>> unique_sets_len_vect_map = new Vector< Map<Integer,Vector<Integer>>>();
				Iterator<Vector<Integer>> itr = unique_sets.iterator();
				while (itr.hasNext()) {
					Vector<Integer> temp_ = itr.next();
					Integer len_temp_ = temp_.size();
					unique_sets_len_vect.add(len_temp_);
					// TH: maps length of each Vector to its contents.
					Map<Integer,Vector<Integer>> unique_sets_len_map = new HashMap<Integer,Vector<Integer>>();
					unique_sets_len_map.put(len_temp_,temp_);
					unique_sets_len_vect_map.add(unique_sets_len_map);
				}//while hasNext.
				
//				System.out.print("unique_sets_len_vect (Initial): ");
//				System.out.println(unique_sets_len_vect);
				
				Collections.sort(unique_sets_len_vect);
				
//				System.out.print("unique_sets_len_vect (Sorted): "); System.out.println(unique_sets_len_vect);
				
//				System.out.print("unique_sets_len_vect_map: ");
//				System.out.println(unique_sets_len_vect_map);
				
				Vector< Vector<Integer>> unique_sets_copy = new Vector< Vector<Integer>>();
				labelB:
				for(int i=0; i<unique_sets_len_vect.size(); i++) {
					Integer temp_len_ = unique_sets_len_vect.get(i);
					for(int j=0; j<unique_sets_len_vect_map.size(); j++) {
						Map<Integer,Vector<Integer>> temp_vect_ = unique_sets_len_vect_map.get(j);
						if(temp_vect_.containsKey(temp_len_)) {
							unique_sets_copy.add(temp_vect_.get(temp_len_));
							unique_sets_len_vect_map.remove(j);
							continue labelB;
						}// if key found.
						else {
							// TH: skips this iteration.
						}// else key NOT found.
						
					}// for j.
					
				}// for i.
				
				unique_sets = unique_sets_copy;
				
			}
			
//			System.out.print("unique_sets (Sorted): "); System.out.println(unique_sets);
			
			{
				
				Vector<Vector<Integer>> toBeRemoved = new Vector<Vector<Integer>>();
				
				// TH: removes duplicates by absorbing smaller Vectors into larger Vectors.
				labelG:
				for(int i=0; i<unique_sets.size()-1; i++) {
					
					Vector<Integer> vect_temp_ = unique_sets.get(i);
					for(int j=i+1; j<unique_sets.size(); j++) {
						
						if(unique_sets.get(j).containsAll(vect_temp_)) {
							
//							System.out.println("unique_sets.get(j): "); System.out.println(unique_sets.get(j));
							toBeRemoved.add(vect_temp_); continue labelG;
						}//if containsAll.
						
					}// for j.
					
				}// for i.
				
				while(toBeRemoved.size()>0) {
					
					Vector<Integer> purge = toBeRemoved.remove(0);
					unique_sets.remove(purge);
					
				}// while size>0.
				
			}
			
//			System.out.print("unique_sets (Purged): "); System.out.println(unique_sets);
			
			// TH: stores all possible compatible combinations in a vector of vectors of vectors.
			Vector<Vector<Vector<Integer>>> vect_vect_vect_all = new Vector<Vector<Vector<Integer>>>();
			
			// TH: Map of groupId and len(each vector of vectors of vectors).
			Map< Integer,Integer> hm_vect_vect_vect_all_len = new HashMap< Integer,Integer>(); 
			
			// TH: specifies group id (i.e. only compatible items).
			int group=0;
			
			{
				
				// TH: generates all possible unique sets from unique_sets.
				Vector<Vector<Vector<Integer>>> vect_vect_vect = new Vector<Vector<Vector<Integer>>>();
				
				for(int i=0; i<unique_sets.size(); i++) {
					vect_vect_vect.clear();
					Vector<Integer> vect = unique_sets.get(i);
//					System.out.println("vect: "+vect);
//					System.out.println("vect.size == " + vect.size());
					for(int j=0; j<vect.size(); j++) {
						Integer temp_Int = vect.get(j);
//						System.out.println("temp_Int = " + temp_Int);
						// TH: if very 1st pass thru vect, then simply adds to 1st spot.
						if(j==0) {
							Vector<Vector<Integer>> vect_vect = new Vector<Vector<Integer>>();
							Vector<Integer> temp_Int_Vect = new Vector<Integer>();
							temp_Int_Vect.add(temp_Int);
							vect_vect.add(temp_Int_Vect);
//							System.out.println("vect_vect_vect (before): "+ vect_vect_vect);
							vect_vect_vect.add(vect_vect);
//							System.out.println("vect_vect_vect (after, first): "+ vect_vect_vect);
						}// if j==0.
						else {
							int temp_size_= vect_vect_vect.size();
//							System.out.println("vect_vect_vect.size == " + vect_vect_vect.size());
							int k=temp_size_;
							while(k>0) {
								
								/**
								 * TH: 	removes one item at a time: vect_vect_vect.get(k)
								 * 				processes with separate: adds a Vector to vect_vect and puts it back to vect_vect_vect
								 * 				processes with together: appends Integer to each element in each iteration
								 * 	For example:
								 * 			------------------------------------
								 * 			[4] 		->	empty
								 * 			empty	->	[[4]]
								 * 			------------------------------------
								 * 			[3,5]		->	empty
								 * 			[5]		->	[[3]]
								 * 			empty	->	[[3],[5]]
								 * 							[[3,5]]
								 * 			------------------------------------
								 * 			[0,1,2]	->	empty
								 * 			[1,2]		->	[[0]]
								 * 			[2]		->	[[0],[1]]
								 * 							[[0,1]]
								 * 			empty	->	[[0], [1], [2]]
								 * 							[[1], [0, 2]]
								 * 							[[0], [1, 2]]
								 * 							[[0, 1], [2]]
								 * 							[[0, 1, 2]]
								 * 			------------------------------------
								 */
								
								Vector<Vector<Integer>> vect_vect_input_ = vect_vect_vect.remove(0);
//								System.out.println("vect_vect_input_: "+ vect_vect_input_);
								if(vect_vect_input_.size()==1) {
									Vector<Integer> vect_input_ = vect_vect_input_.get(0);
//									System.out.println("vect_input_: " + vect_input_);
									{
										// TH: input into vect_vect_input_ for 'separate' case.
										Vector<Integer> vect_input__2nd = new Vector<Integer>();
										vect_input__2nd.add(temp_Int);
										
										Vector<Vector<Integer>> vect_vect_input_2nd = new Vector<Vector<Integer>>();
//										System.out.println("vect_vect_input_2nd (before): "+ vect_vect_input_2nd);
										vect_vect_input_2nd.add(vect_input_);
										vect_vect_input_2nd.add(vect_input__2nd);
//										System.out.println("vect_vect_input_2nd (after, separate): "+ vect_vect_input_2nd);
//										System.out.println("vect_vect_vect (before): "+ vect_vect_vect);
										vect_vect_vect.add(vect_vect_input_2nd);
//										System.out.println("vect_vect_vect (after, separate): " + vect_vect_vect);
									}// 'separate' case.
									
									{
										// TH: input into vect_vect_input_ for 'together' case.
										Vector<Integer> vect_input_copy = new Vector<Integer>();
										for(int p=0; p<vect_input_.size(); p++) {
											vect_input_copy.add(vect_input_.get(p));
										}// for p.
//										System.out.println("vect_input_copy (before): " + vect_input_copy);
										vect_input_copy.add(temp_Int);
//										System.out.println("vect_input_copy (after): " + vect_input_copy);
										Vector<Vector<Integer>> vect_vect_input_2nd = new Vector<Vector<Integer>>();
//										System.out.println("vect_vect_input_2nd (before): "+ vect_vect_input_2nd);
										vect_vect_input_2nd.add(vect_input_copy);
//										System.out.println("vect_vect_input_2nd (after, together): "+ vect_vect_input_2nd);
//										System.out.println("vect_vect_vect (before): " + vect_vect_vect);
										vect_vect_vect.add(vect_vect_input_2nd);
//										System.out.println("vect_vect_vect (after, together): " + vect_vect_vect);
									}// 'together' case.
									
								}//if size==1.
								else {
									
									{
										// TH: 'separate' case.
										Vector<Integer> vect_input__2nd = new Vector<Integer>();
										vect_input__2nd.add(temp_Int);
										Vector<Vector<Integer>> vect_vect_input_2nd = new Vector<Vector<Integer>>();
										for(int p=0; p<vect_vect_input_.size(); p++) {
											vect_vect_input_2nd.add((Vector<Integer>) vect_vect_input_.get(p).clone());
										}// for p.
//										System.out.println("vect_vect_input_2nd (before): "+ vect_vect_input_2nd);
										vect_vect_input_2nd.add(vect_input__2nd);
//										System.out.println("vect_vect_input_2nd (after, separate): "+ vect_vect_input_2nd);
//										System.out.println("vect_vect_vect (before): "+ vect_vect_vect);
										vect_vect_vect.add(vect_vect_input_2nd);
//										System.out.println("vect_vect_vect (after, separate): " + vect_vect_vect);
									}// separate case.
									
									{
										// TH: 'together' case.
										
										int m=0;
										while(m<vect_vect_input_.size()) {
											
//											System.out.println("m versus vect_vect_input_.size:  " + m + " versus " + vect_vect_input_.size());
//											System.out.println("vect_vect_input_: " + vect_vect_input_);
											
											Vector<Vector<Integer>> vect_input_copy = new Vector<Vector<Integer>>();
											for(int p=0; p<vect_vect_input_.size(); p++) {
												vect_input_copy.add((Vector<Integer>) vect_vect_input_.get(p).clone());
											}// for p.
//											System.out.println("vect_input_copy (before): "+ vect_input_copy);
											Vector<Integer> vect_input_add_temp_ = vect_input_copy.remove(m);
//											System.out.println("vect_input_add_temp_: " + vect_input_add_temp_);
//											System.out.println("vect_input_copy (after, removed): "+ vect_input_copy);
											Vector<Integer> vect_input_add = new Vector<Integer>();
											for(int h=0; h<vect_input_add_temp_.size(); h++) {
												vect_input_add.add(vect_input_add_temp_.get(h));
											}
											
											vect_input_add.add(temp_Int);
//											System.out.println("temp_Int: " + temp_Int);
//											System.out.println("vect_input_add (after, added): " + vect_input_add);
//											System.out.println("vect_input_copy (before): "+ vect_input_copy);
											vect_input_copy.add(vect_input_add);
//											System.out.println("vect_input_copy (after, added): "+ vect_input_copy);
//											System.out.println("vect_vect_vect (before): "+ vect_vect_vect);
											vect_vect_vect.add(vect_input_copy);
//											System.out.println("vect_vect_vect (after, together): " + vect_vect_vect);
											m++;
											
										}// while m<vect_vect_input_.size.
										
									}// together case.
									
								}//else size>1.
								
								k--;
								
							}// while k.
							
						}// else j>0.
						
					}// for j.
					
					// TH: adds to total collection.
					if(vect_vect_vect.size()==1) vect_vect_vect_all.add((Vector<Vector<Integer>>) vect_vect_vect.get(0).clone());
					else {
						for(int p=0; p<vect_vect_vect.size(); p++) {
							vect_vect_vect_all.add((Vector<Vector<Integer>>) vect_vect_vect.get(p).clone());
						}// for p.
					}// else.
					
					hm_vect_vect_vect_all_len.put((Integer)group,vect_vect_vect.size()); group++;
					
				}//for i:unique_sets.
				
//				System.out.println("vect_vect_vect_all: " + vect_vect_vect_all);
//				System.out.println("vect_vect_vect_all (Debug_14): ");
//				for(int index=0; index<vect_vect_vect_all.size(); index++) {
//					System.out.println(vect_vect_vect_all.get(index));
//				}// for index.
				
//				System.out.println("hm_vect_vect_vect_all_len: " + hm_vect_vect_vect_all_len);
				
			}
			
			// TH: K & R - Exercise 6.2
			// builds tree (AVL) for optimization.
			
			// TH: Input Arguments::
			// 	C:\Users\tungh\eclipse-workspace\CS457-P1\P1_\src\test_X3_.txt  -depth
			if(mode.equals("-depth")) {
				
//				System.out.println("Depth-First Search.");
				
//				System.out.println("Mapping Group To Length:");
				Set< Map.Entry< Integer,Integer> > st_hm_vect_vect_vect_all_len = hm_vect_vect_vect_all_len.entrySet();
//				System.out.println("st_hm_vect_vect_vect_all_len:" + st_hm_vect_vect_vect_all_len);
				
				// TH: tracks #items previously added back to vect_vect_vect_all before generating new combos.
				Integer group_length_previous=-1;
				
				labelC:
				for (Map.Entry< Integer,Integer> map_entry_len:st_hm_vect_vect_vect_all_len) {
					
//					System.out.print(map_entry_len.getKey()+": ");
//					System.out.println(map_entry_len.getValue());
					Integer group_position_ = map_entry_len.getKey();
					Integer group_length_   = map_entry_len.getValue();
					
					// TH: stores transits from original set.
					Vector<Vector<Vector<Integer>>> vect_vect_vect_all_transit = new Vector<Vector<Vector<Integer>>> ();
					for(int i=0; i<group_length_; i++) {
//						System.out.println("vect_vect_vect_all (before, remove(0)): " + vect_vect_vect_all);
						Vector<Vector<Integer>> vect_vector_temp_ = vect_vect_vect_all.remove(0);
//						System.out.println("vect_vect_vect_all (after, remove(0)): " + vect_vect_vect_all);
						vect_vect_vect_all_transit.add(vect_vector_temp_);
					}// for i.
					
					// TH: if very 1st transit, simply adds back to vect_vect_vect_all (i.e. stack-wise).
					if( group_position_==0) {
						Iterator<Vector<Vector<Integer>>> itr = vect_vect_vect_all_transit.iterator();
						while(itr.hasNext()) {
							Vector<Vector<Integer>> temp_ =  new Vector<Vector<Integer>>();
							temp_ = itr.next();
							vect_vect_vect_all.add(temp_);
						}// while hasNext.
						group_length_previous = group_length_;
						continue labelC;
					}// if group_position_==0.
					
					// TH: stores combinations, see below.
					Vector<Vector<Vector<Integer>>> vect_vect_vect_all_combo = new Vector<Vector<Vector<Integer>>> ();
					while(group_length_previous>0) {
						
						// TH: removes from top of stack.
						Vector<Vector<Integer>> vect_vect_temp_ = vect_vect_vect_all.remove(vect_vect_vect_all.size()-1);
						
						// TH: iterates over transit in order to generate combinations, see below.
						Iterator<Vector<Vector<Integer>>> itr = vect_vect_vect_all_transit.iterator();
						
						// TH: generates one combination at a time, stores in vect_vect_vect_all_combo. 
						while(itr.hasNext()) {
							// https://stackoverflow.com/questions/14737578/concatenate-vectors-in-java
							// TH: merges together in order to generate a new combination in vect_vect_vect_all_combo.
							Vector<Vector<Integer>> vect_vect_merge = new Vector<Vector<Integer>>();
							vect_vect_merge.addAll(vect_vect_temp_);
							vect_vect_merge.addAll(itr.next());
							vect_vect_vect_all_combo.add(vect_vect_merge);
						}// while hasNext.
						--group_length_previous;
						
					}// while group_length_previous>0.
					
					// TH: updates previous length, i.e. how many recently generated combos, see below.
					group_length_previous = vect_vect_vect_all_combo.size();
					
					// TH: adds all combinations back to vect_vect_vect_all, stack-wise.
					while(vect_vect_vect_all_combo.size()>0) {
						vect_vect_vect_all.add(vect_vect_vect_all_combo.remove(0));
					}// while combo.size>0.
					
				}// for Map.Entry.
				
//				System.out.println("Depth-First Search Candidates: ");
//				for(int i=0; i<vect_vect_vect_all.size(); i++) {
//					System.out.println(vect_vect_vect_all.get(i));
//				}//for i.
				
				labelD:
				while(vect_vect_vect_all.size()>0) {
					
					Vector<Vector<Integer>> candidate_ = vect_vect_vect_all.remove(vect_vect_vect_all.size()-1);
					
//					System.out.println("candidate_ (before, purged): " + candidate_);
					
					// TH: purges against duplicates in depth-first search.
					{
						Vector<Vector<Integer>> toBeRemoved = new Vector<Vector<Integer>>();
						labelG:
						for(int i=0; i<candidate_.size()-1; i++) {
							
							Vector<Integer> vect_temp_ = candidate_.get(i);
							for(int j=i+1; j<candidate_.size(); j++) {
								
								if(candidate_.get(j).containsAll(vect_temp_)) {
									
//									System.out.println("candidate_.get(j): "); System.out.println(candidate_.get(j));
									toBeRemoved.add(vect_temp_); continue labelG;
								}// if containsAll.
							}// for j.
						}// for i.
						
						while(toBeRemoved.size()>0) {
							
							Vector<Integer> purge = toBeRemoved.remove(0);
							candidate_.remove(purge);
							
						}// while size>0.
						
					}
					
					if(candidate_.size()>numbags) continue labelD;
//					System.out.println("candidate_ (after, purged): " + candidate_);
					
					Iterator<Vector<Integer>> itr = candidate_.iterator();
					labelE:
					while(itr.hasNext()) {
						
						Vector<Integer> bag_temp_ = itr.next();
						if(bag_temp_.size()==1) continue labelE;
						
						Integer total_per_bag_ = 0;
						Iterator<Integer> itr_weight = bag_temp_.iterator();
						while(itr_weight.hasNext()) {
							total_per_bag_ += hm_weight.get(hm_reverse.get(itr_weight.next()));
						}// while itr_weight.
						
						if(total_per_bag_>sizebags) continue labelD;
						
					}// while itr.
					
					// TH: purges a solution if needed against negative logic, depth-first search.
					{
						for(int i=0; i<candidate_.size(); i++) {
							
							Vector<Integer> vect_temp_ = candidate_.get(i);
							for(int j=0; j<neg_log_vect.size(); j++) {
								
								if(vect_temp_.containsAll(neg_log_vect.get(j))) {
//									System.out.println("vect_temp_: "); System.out.println(vect_temp_);
									continue labelD;
								}// if containsAll.
							}// for j.
						}// for i.
					}
					
					// TODO: 	purges a solution against overlaps, depth-first search.
					// 			see breadth-first search for details.
					{
						labelG:
						for(int i=0; i<candidate_.size()-1; i++) {
							Vector<Integer> vect_temp_ = candidate_.get(i);
//							System.out.println("vect_temp_: "); System.out.println(vect_temp_);
							for(int j=i+1; j<candidate_.size(); j++) {
								for(int k=0; k<vect_temp_.size(); k++){
									if(candidate_.get(j).contains(vect_temp_.get(k))) {
//										System.out.println("candidate_.get(j): "); System.out.println(candidate_.get(j));
										vect_temp_.remove(k);
										continue labelG;
									}
								}
							}// for j.
						}// for i.
					}
					
//					System.out.println("solution_ (after, purge): " + candidate_);
					
					// TH: prints out solution to console.
					if(candidate_.size()>numbags) continue labelD;
					
					for(int i=0; i<candidate_.size(); i++) {
						Vector<Integer> bag_ = candidate_.get(i);
//						System.out.println("bag_: " + bag_);
						int bag_weight=0;
						for(int j=0; j<bag_.size(); j++) {
							bag_weight+=(int)hm_weight.get(hm_reverse.get(bag_.get(j)));
						}// for j.
						if(bag_weight>sizebags) continue labelD;
					}// for i.
					
					itr = candidate_.iterator();
					System.out.println("success");
					labelF:
					while(itr.hasNext()) {
						Vector<Integer> bag_temp_ = itr.next();
						if(bag_temp_.size()==1) {
							System.out.println(hm_reverse.get(bag_temp_.get(0)));
							continue labelF;
						}// if size==1.
						
						Iterator<Integer> itr_multiple = bag_temp_.iterator();
						while(itr_multiple.hasNext()) {
							System.out.print(hm_reverse.get(itr_multiple.next()));
							if(itr_multiple.hasNext()) System.out.print("\t");
						}// while itr_multiple.hasNext.
						
						System.out.println();
						
					}// while itr.hasNext.
					
					// TH: depth-first search already complete --> Go To Sleep Now !!
					System.exit(0);
					
				}// while size>0.
				
			}// depth-first search.
			
			// TH: Input Arguments::
			// 	C:\Users\tungh\eclipse-workspace\CS457-P1\P1_\src\test_X3_.txt  -breadth
			else if(mode.equals("-breadth")) {
				
//				System.out.println("Breadth-First Search.");
				
//				System.out.println("Mapping Group To Length:");
				Set< Map.Entry< Integer,Integer> > st_hm_vect_vect_vect_all_len = hm_vect_vect_vect_all_len.entrySet();
//				System.out.println("st_hm_vect_vect_vect_all_len:" + st_hm_vect_vect_vect_all_len);
				
				// TH: tracks #items previously added back to vect_vect_vect_all before generating new combos.
				Integer group_length_previous=-1;
				
				labelC:
				for (Map.Entry< Integer,Integer> map_entry_len:st_hm_vect_vect_vect_all_len) {
					
//					System.out.print(map_entry_len.getKey()+": ");
//					System.out.println(map_entry_len.getValue());
					Integer group_position_ = map_entry_len.getKey();
					Integer group_length_   = map_entry_len.getValue();
					
					int beforeTransit = vect_vect_vect_all.size();
					int afterTransit   = beforeTransit - group_length_;
					
					// TH: stores transits from original set.
					Vector<Vector<Vector<Integer>>> vect_vect_vect_all_transit = new Vector<Vector<Vector<Integer>>> ();
					for(int i=0; i<group_length_; i++) {
//						System.out.println("vect_vect_vect_all (before, remove(0)): " + vect_vect_vect_all);
						Vector<Vector<Integer>> vect_vector_temp_ = vect_vect_vect_all.remove(0);
//						System.out.println("vect_vect_vect_all (after, remove(0)): " + vect_vect_vect_all);
						vect_vect_vect_all_transit.add(vect_vector_temp_);
					}// for i.
					
					// TH: if very 1st transit, simply adds back to vect_vect_vect_all (i.e. stack-wise).
					if( group_position_==0) {
						Iterator<Vector<Vector<Integer>>> itr = vect_vect_vect_all_transit.iterator();
						while(itr.hasNext()) {
							Vector<Vector<Integer>> temp_ =  new Vector<Vector<Integer>>();
							temp_ = itr.next();
							vect_vect_vect_all.add(temp_);
						}// while hasNext.
						group_length_previous = group_length_;
						continue labelC;
					}// if group_position_==0.
					
					// TH: stores combinations, see below.
					Vector<Vector<Vector<Integer>>> vect_vect_vect_all_combo = new Vector<Vector<Vector<Integer>>> ();
					int group_length_previous_copy = Integer.parseInt(Integer.toString(group_length_previous));
					
					while(group_length_previous>0) {
						
						// TH: removes from bottom of stack.
						Vector<Vector<Integer>> vect_vect_temp_ = new Vector<Vector<Integer>>();
						vect_vect_temp_ = vect_vect_vect_all.remove(afterTransit-group_length_previous_copy);
						
						// TH: iterates over transit in order to generate combinations, see below.
						Iterator<Vector<Vector<Integer>>> itr = vect_vect_vect_all_transit.iterator();
						
						// TH: generates one combination at a time, stores in vect_vect_vect_all_combo. 
						while(itr.hasNext()) {
							// https://stackoverflow.com/questions/14737578/concatenate-vectors-in-java
							// TH: merges together in order to generate a new combination in vect_vect_vect_all_combo.
							Vector<Vector<Integer>> vect_vect_merge = new Vector<Vector<Integer>>();
							vect_vect_merge.addAll(vect_vect_temp_);
							vect_vect_merge.addAll(itr.next());
							vect_vect_vect_all_combo.add(vect_vect_merge);
						}// while hasNext.
						--group_length_previous;
						
					}// while group_length_previous>0.
					
					// TH: updates previous length, i.e. how many recently generated combos, see below.
					group_length_previous = vect_vect_vect_all_combo.size();
					
					// TH: adds all combinations back to vect_vect_vect_all, stack-wise.
					while(vect_vect_vect_all_combo.size()>0) {
						vect_vect_vect_all.add(vect_vect_vect_all_combo.remove(0));
					}// while combo.size>0.
					
				}// for Map.Entry.
				
//				System.out.println("Breadth-First Search Candidates: ");
//				for(int i=0; i<vect_vect_vect_all.size(); i++) {
//					System.out.println(vect_vect_vect_all.get(i));
//				}//for i.
				
				// TH: holds solutions to be displayed on console.
				Vector<Vector<Vector<Integer>>> candidate_clones_ = new Vector<Vector<Vector<Integer>>>();
				
				labelD:
				while(vect_vect_vect_all.size()>0) {
					
					Vector<Vector<Integer>> candidate_ = vect_vect_vect_all.remove(vect_vect_vect_all.size()-1);
					
//					System.out.println("candidate_ (before, purged): " + candidate_);
					
					// TH: purges against duplicates in breadth-first search.
					{
						Vector<Vector<Integer>> toBeRemoved = new Vector<Vector<Integer>>();
						labelG:
						for(int i=0; i<candidate_.size()-1; i++) {
							
							Vector<Integer> vect_temp_ = candidate_.get(i);
//							System.out.println("vect_temp_: "); System.out.println(vect_temp_);
							for(int j=i+1; j<candidate_.size(); j++) {
								
								if(candidate_.get(j).containsAll(vect_temp_)) {
									
//									System.out.println("candidate_.get(j): "); System.out.println(candidate_.get(j));
									toBeRemoved.add(vect_temp_); continue labelG;
								}// if containsAll.
							}// for j.
						}// for i.
						
						while(toBeRemoved.size()>0) {
							
							Vector<Integer> purge = toBeRemoved.remove(0);
							candidate_.remove(purge);
							
						}// while size>0.
						
					}
					
					if(candidate_.size()>numbags) continue labelD;
//					System.out.println("candidate_ (after, purged: duplicates): " + candidate_);
					
					Iterator<Vector<Integer>> itr = candidate_.iterator();
					labelE:
					while(itr.hasNext()) {
						
						Vector<Integer> bag_temp_ = itr.next();
						if(bag_temp_.size()==1) continue labelE;
						
						Integer total_per_bag_ = 0;
						Iterator<Integer> itr_weight = bag_temp_.iterator();
						while(itr_weight.hasNext()) {
							total_per_bag_ += hm_weight.get(hm_reverse.get(itr_weight.next()));
						}// while itr_weight.
						
						if(total_per_bag_>sizebags) continue labelD;
						
					}// while itr.
					
					// TH: purges a solution if needed against negative logic, breadth-first search.
					{
						for(int i=0; i<candidate_.size(); i++) {
							
							Vector<Integer> vect_temp_ = candidate_.get(i);
							for(int j=0; j<neg_log_vect.size(); j++) {
								
								if(vect_temp_.containsAll(neg_log_vect.get(j))) {
//									System.out.println("vect_temp_: "); System.out.println(vect_temp_);
									continue labelD;
								}// if containsAll.
							}// for j.
						}// for i.
					}
					
//					System.out.println("candidate_ (after, purged: negative): " + candidate_);
					
					// TH: purges a solution against overlaps, breadth-first search.
					{
						
						int howManyClones = candidate_.size();
						labelF:
						for(int m=0; m<howManyClones; m++) {
							
							Vector<Vector<Integer>> candidate_clone_temp_ = new Vector<Vector<Integer>>();
							
							// TH: generates clones for processing.
							for(int h=0; h<candidate_.size(); h++) {
								candidate_clone_temp_.add((Vector<Integer>) candidate_.get(h).clone());
							}
							
//							System.out.println("candidate_clone_temp_ (before): " +candidate_clone_temp_);
							
							Vector<Integer> vect_insert_ = candidate_clone_temp_.remove(m);
							candidate_clone_temp_.add(0, vect_insert_);
							
							Vector<Integer> vect_temp_ = candidate_clone_temp_.get(0);
//							System.out.println("vect_temp_: "); System.out.println(vect_temp_);
							labelG:
							for(int j=1; j<candidate_clone_temp_.size(); j++) {
								for(int k=0; k<vect_temp_.size(); k++){
									if(candidate_clone_temp_.get(j).contains(vect_temp_.get(k))) {
//										System.out.println("candidate_clone_temp_.get(j): "); System.out.println(candidate_clone_temp_.get(j));
										vect_temp_.remove(k);
										continue labelG;
									}
								}//for k.
							}// for j.
							
							{
								int[] toBeRemoved = new int[candidate_clone_temp_.size()]; int k=0;
								for(int j=0; j<candidate_clone_temp_.size(); j++) {
									if(candidate_clone_temp_.get(j).isEmpty()) {
										toBeRemoved[k]=j; k++;
									}//if empty.
								}//for j.
								for(int n=0; n<k; n++) {
									candidate_clone_temp_.remove(toBeRemoved[k]);
								}//for m.
							}
							
							for(int i=0; i<candidate_clone_temp_.size()-1; i++) {
								Vector<Integer> vect_temp_2nd = candidate_clone_temp_.get(i);
//								System.out.println("vect_temp_2nd: "); System.out.println(vect_temp_2nd);
								for(int j=i+1; j<candidate_clone_temp_.size(); j++) {
									for(int k=0; k<vect_temp_2nd.size(); k++){
										if(candidate_clone_temp_.get(j).contains(vect_temp_2nd.get(k))) {
//											System.out.println("candidate_clone_temp_.get(j): "); System.out.println(candidate_clone_temp_.get(j));
											continue labelF;
										}
									}
								}// for j.
							}// for i.
							
//							System.out.println("candidate_clone_temp_ (after): " +candidate_clone_temp_);
//							System.out.println("candidate_clones_ (before addAll): " + candidate_clones_);
							candidate_clones_.add(candidate_clone_temp_);
//							System.out.println("candidate_clones_ (after addAll): " + candidate_clones_);
							
						}// for m.
						
//						System.out.println("candidate_clones_ (after, purge: overlaps): " + candidate_clones_);
						
					}
					
//					System.out.println("solution_ (after, purge: overlaps): " + candidate_clones_);
					
				}// while size>0.
				
//				System.out.println("candidate_clones_ (Debug_15a): " + candidate_clones_);
				
				// TH: sorts each vector.
				{
					for(int i=0; i<candidate_clones_.size(); i++) {
						Vector<Vector<Integer>> vect_vect_temp_ = candidate_clones_.get(i);
						for(int j=0; j<vect_vect_temp_.size(); j++) {
							Vector<Integer> vect_temp_ = vect_vect_temp_.get(j);
							Collections.sort(vect_temp_);
						}// for j.
					}// for i.
				}
				
				// TH: creates a Set of candidate_ from candidate_clones_.
				// TH: avoids duplicates in candidate_clones_.
				// https://way2java.com/collections/vector/removing-duplicates-from-vector/
				{
					HashSet<Vector<Vector<Integer>>> hs1 = new HashSet<Vector<Vector<Integer>>>(candidate_clones_); 
					candidate_clones_ = new Vector<Vector<Vector<Integer>>>(hs1);
				}
				
				// TH: removes duplicates.
				{
					Vector<Vector<Vector<Integer>>> toBeRemoved = new Vector<Vector<Vector<Integer>>>();
					for(int i=0; i<candidate_clones_.size()-1; i++) {
						Vector<Vector<Integer>> vect_temp_ = candidate_clones_.get(i);
//						System.out.println("vect_temp_: "); System.out.println(vect_temp_);
						for(int j=i+1; j<candidate_clones_.size(); j++) {
							
							if(candidate_clones_.get(j).containsAll(vect_temp_)) {
//								System.out.println("candidate_clones_.get(j): "); System.out.println(candidate_clones_.get(j));
								toBeRemoved.add((Vector<Vector<Integer>>) candidate_clones_.get(j).clone());
							}
							
						}// for j.
					}// for i.
					for(int i=0; i<toBeRemoved.size(); i++) {
						candidate_clones_.remove(toBeRemoved.get(i));
					}//for i.
				}
				
//				System.out.println("candidate_clones_ (Debug_15b): " + candidate_clones_);
				
//				System.out.println("candidate_clones_ (Debug_15c): " + candidate_clones_);
				
				// TH: iterates over all vectors of vectors.
				Iterator<Vector<Vector<Integer>>> itr_solution = candidate_clones_.iterator();
				boolean solutions = false;
				labelG:
				while(itr_solution.hasNext()) {
					Vector<Vector<Integer>> each_solution = itr_solution.next();
					
					if(each_solution.size()>numbags) continue labelG;
//					System.out.println("each_solution: " + each_solution);
					for(int i=0; i<each_solution.size(); i++) {
						Vector<Integer> bag_ = each_solution.get(i);
//						System.out.println("bag_: " + bag_);
						int bag_weight=0;
						for(int j=0; j<bag_.size(); j++) {
							bag_weight+=(int)hm_weight.get(hm_reverse.get(bag_.get(j)));
						}// for j.
						if(bag_weight>sizebags) continue labelG;
					}// for i.
					
					// TH: iterates over all bags per solution.
					Iterator<Vector<Integer>> each_solution_itr = each_solution.iterator();
					System.out.println("success");
					solutions = true;
					labelF:
					while(each_solution_itr.hasNext()) {
						Vector<Integer> bag_temp_ = each_solution_itr.next();
						if(bag_temp_.size()==1) {
							System.out.println(hm_reverse.get(bag_temp_.get(0)));
							continue labelF;
						}//if size==1.
						// TH: iterates over multiple items per bag.
						Iterator<Integer> itr_multiple = bag_temp_.iterator();
						while(itr_multiple.hasNext()) {
							System.out.print(hm_reverse.get(itr_multiple.next()));
							if(itr_multiple.hasNext()) System.out.print("\t");
						}// while itr_multiple.
						System.out.println();
					}// while each_solution_itr.
				}// while itr_solution.
				if(!solutions)
					System.out.println("failure");
				System.exit(0);
			}// breadth-first search.
			
		}//main.
		
	}

	private static void fail() {
		System.out.println("failure");
		//System.out.println((double)(System.currentTimeMillis()-start)/1000 + " seconds");
		System.exit(0);
	}

}