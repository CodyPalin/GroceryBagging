package grocery;

import java.util.HashSet;
import java.util.Set;

public class Item {
	public int ID;
	public String name;
	public String ConstraintString; //string that contains the literal constraint of the item.
	public int weight;
	public Set<Integer> constraints = new HashSet<Integer>();
	public Item() {
		
	}
	public String toString(){
		
		return ("{ID= "+ID+", name="+name+", weight="+weight+", constraints="+constraints+"}");
		
	}
}