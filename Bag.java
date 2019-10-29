//package grocery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
//import java.util.HashSet;
//import java.util.Set;
import java.util.Vector;

public class Bag {
	public int max = 0;
	public int weight = 0;
	public Vector<Integer> constraints = new Vector<Integer>();
	public ArrayList<Integer> items = new ArrayList<Integer>();
	public Bag() {}
	public Bag(Bag b) {
		max = b.max;
		weight = b.weight;
		constraints.addAll(b.constraints);
		items.addAll(b.items);
		{
			HashSet<Integer> hs1 = new HashSet<Integer>(constraints); 
			constraints = new Vector<Integer>(hs1);
		}
		Collections.sort(constraints);
	}
	public Bag add(Item item) {
		Bag b = new Bag(this);
		b.weight += item.weight;
		b.items.add(item.ID);
		b.constraints.addAll(item.constraints);
		{
			HashSet<Integer> hs1 = new HashSet<Integer>(b.constraints); 
			b.constraints = new Vector<Integer>(hs1);
		}
		Collections.sort(b.constraints);
		return b;
	}
	public boolean canAdd(Item item) {
		if(weight+item.weight >max)
			return false;
//		if(constraints.contains(item.ID))
		if(Collections.binarySearch(constraints,(Integer)item.ID)>=0)
			return false;
		
		return true;
	}
	public void remove(Item item) {
		items.remove((Object)item.ID);
		constraints.clear();
		weight = 0;
		for(int i: items){
			Item i2 =Main.unsortedItems.get(i);
			weight+=i2.weight;
			constraints.addAll(i2.constraints);
		}
	}
	public String toString() {
		String itemlist = "";
		for(int ID: items) {
			itemlist += (ID+"\t");
		}
		return itemlist;
		
	}
}
