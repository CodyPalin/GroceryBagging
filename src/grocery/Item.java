package grocery;

import java.util.HashSet;
import java.util.Set;

public class Item {
	public int ID;
	public String name;
	public int weight;
	public Set<Integer> constraints = new HashSet<Integer>();
}
