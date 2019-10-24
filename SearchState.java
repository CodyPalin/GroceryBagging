//package grocery;

import java.util.ArrayList;

public class SearchState {
	public ArrayList<Bag> bags;
	public int addingitem;
	public boolean topflag = false;
	public SearchState(ArrayList<Bag> bags, int item)
	{
		this.bags = (ArrayList<Bag>) bags.clone();
		addingitem = item;
	}
	public SearchState(ArrayList<Bag> bags, int item, boolean topflag)
	{
		this.bags = (ArrayList<Bag>) bags.clone();
		addingitem = item;
		this.topflag = topflag;
	}
	public boolean IsGoalState(int numitems) {
		
		int baggeditems = 0;
			for(Bag b: bags){
				baggeditems += b.items.size();
			}
		if(baggeditems != numitems)
			return false;
		//do all checks
		return true;
	}
}
