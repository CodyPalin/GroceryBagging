package grocery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Bag {
	public int max = 0;
	public int weight = 0;
	public Set<Integer> constraints = new HashSet<Integer>();
	public ArrayList<Integer> items = new ArrayList<Integer>();
	public Bag() {}
	public Bag(Bag b) {
		max = b.max;
		weight = b.weight;
		constraints.addAll(b.constraints);
		items.addAll(b.items);
	}
	public Bag add(Item item) {
		Bag b = new Bag(this);
		b.weight += item.weight;
		b.items.add(item.ID);
		b.constraints.addAll(item.constraints);
		return b;
	}
	public boolean canAdd(Item item) {
		if(weight+item.weight >max)
			return false;
		if(constraints.contains(item.ID))
			return false;
		
		return true;
	}
	public String toString() {
		String itemlist = "";
		for(int ID: items) {
			itemlist += (ID+"\t");
		}
		return itemlist;
		
	}
}
