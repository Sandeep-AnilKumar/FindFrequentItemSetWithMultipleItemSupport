package pkotha6_sanilk2_CS583_Project1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class GetFrequentItemSets {
    public static Set<SingleItem> M = new TreeSet<>(new MISComparator());
    public static Set<SingleItem> F1 = new TreeSet<>(new IDComparator());
    public static Set<SingleItem> L = new TreeSet<>(new IDComparator());
    public static List<ItemSet> Fk = new LinkedList<>();
    public static int noOfTransactions = 0;
    public static Map<Integer, SingleItem> singleItemMap = new HashMap<>();

    public static void readInput() throws IOException {
	BufferedReader  br = new BufferedReader(new FileReader("input-data.txt"));
	String currentLine = "";

	// building transactions add singleItems
	List<HashSet<Integer>> transactions = new ArrayList<HashSet<Integer>>();
	Map<Integer, Integer> singleItemCount = new TreeMap<>();
	while ((currentLine = br.readLine()) != null) {
	    noOfTransactions++;
	    System.out.println(currentLine);
	    currentLine = currentLine.replace("{", "");
	    currentLine = currentLine.replace("}", "");
	    currentLine = currentLine.replace(",", "");

	    HashSet<Integer> currTransaction = new HashSet<>();
	    for (String itemID : currentLine.split(" ")) {
		int item = Integer.parseInt(itemID);
		if(singleItemCount.containsKey(item)) {
		    singleItemCount.put(item, singleItemCount.get(item)+1);
		}
		else {
		    singleItemCount.put(item, 1);
		}
		currTransaction.add(item);	    
	    }
	    transactions.add(currTransaction);

	}
	Transactions.transactions = transactions;
	buildListofSingleItems(singleItemCount, noOfTransactions);
	br.close();
    }

    public static void buildListofSingleItems(Map<Integer, Integer> singleItemCount, double noOfTransactions) throws IOException {
	Map<Integer, Double> itemMisMap = getMIS();
	int count = 0;
	SingleItem currSingleItem = null;
	for(Integer itemId : singleItemCount.keySet()) {
	    currSingleItem = new SingleItem();
	    count = singleItemCount.get(itemId);
	    currSingleItem.setCount(count);
	    currSingleItem.setItemID(itemId);
	    currSingleItem.setMIS(itemMisMap.get(itemId));
	    currSingleItem.setSupport(count/noOfTransactions);
	    M.add(currSingleItem);
	    if(!singleItemMap.containsKey(itemId)) {
		singleItemMap.put(itemId, currSingleItem);
	    }
	}

	// printing the built datastructure
	for(SingleItem s : M) {
	    System.out.println(s);
	}
    }

    public static Map<Integer, Double> getMIS() throws IOException {
	BufferedReader br = new BufferedReader(new FileReader("parameter-file.txt"));
	String currLine = "";
	int itemId = 0;
	double itemMis = 0d;
	Map<Integer, Double> itemMisMap = new HashMap<>();
	while((currLine = br.readLine()) != null) {
	    if(currLine.contains("MIS")) {
		itemId = Integer.parseInt( currLine.substring(4, currLine.indexOf(')')));
		itemMis = Double.parseDouble(currLine.substring(currLine.indexOf('=')+2));
		itemMisMap.put(itemId, itemMis);	
	    }
	    else {
		break;
	    }
	}
	br.close();
	return itemMisMap;
    }


    public static void initialPass() {
	if(M == null) {
	    return;
	}

	SingleItem firstItem = null;
	SingleItem curItem = null; 
	for(Iterator<SingleItem> i = M.iterator(); i.hasNext();) {
	    curItem = i.next();
	    if(firstItem == null) {
		if(curItem.getSupport() >= curItem.getMIS()) {
		    firstItem = curItem;
		    L.add(curItem);
		}
		else {
		    M.remove(curItem);
		}
	    }
	    else {
		if(curItem.getSupport() >= firstItem.getMIS()) {
		    L.add(curItem);
		}
	    }
	}

	for (SingleItem item : L) {
	    System.out.println(item);
	}
    }

    public static void generateF1() {
	if(M == null) {
	    return;
	}
	for(SingleItem item : M) {
	    if(item.getSupport() > item.getMIS()) {
		F1.add(item);
	    }
	}

	for(SingleItem item : F1) {
	    System.out.println(item);
	}
    }

    public static void AprioriAlgorithm() {
	int k = 2;
	List<ItemSet> Ck = new ArrayList<>();
	int[] curItems = new int[]{};
	while(k == 2 || Fk != null) {
	    if(k == 2) {
		Ck = generateLevel2Candidate(0.1);
	    }
	    else {
		Ck = generateMSCandidate(0.1);
	    }

	    Fk = new LinkedList<>();
	    for(ItemSet i : Ck) {
		i.setCount(Transactions.getItemSetCount(i));
	    }

	    for(ItemSet i : Ck) {
		curItems = new int[i.getItemsSet().size()];
		int index = 0;
		for(int item : i.getItemsSet()) {
		    curItems[index++] = item;
		}

		if(i.getCount() >= singleItemMap.get(curItems[0]).getMIS()) {
		    Fk.add(i);
		}
	    }
	    k++;
	}
	//Union;
	return;
    }

    public static List<ItemSet> generateLevel2Candidate(double p) {
	List<ItemSet> c2 = new ArrayList<>();
	List<SingleItem> listL = new ArrayList<SingleItem>(L);
	SingleItem firstItem = null;
	for(int index = 0; index < listL.size(); index++) {
	    SingleItem currItem = listL.get(index);

	    if(currItem.getSupport() >= currItem.getMIS()){
		firstItem = currItem;
		for(int h = index+1; h < listL.size(); h++) {
		    SingleItem secondItem = listL.get(h);

		    if ((secondItem.getSupport() >= firstItem.getMIS()) && 
			    (Math.abs(currItem.getSupport() - secondItem.getSupport())<=p)) {
			ItemSet currItemSet = new ItemSet();
			currItemSet.add(currItem.getItemID());
			currItemSet.add(secondItem.getItemID());
			c2.add(currItemSet);
		    }
		}
	    }
	}

	for(ItemSet itemSet : c2) {
	    System.out.println(itemSet.getItemsSet());
	}
	return c2;
    }

    public static List<ItemSet> generateMSCandidate(double SDC) {

	List<ItemSet> Ck = new ArrayList<>();
	ItemSet currC;
	int bound = Fk.size();
	boolean addCandidateToCk = true;
	for(int i =0; i<bound-1; i++) {
	    for (int j=i+1; j<bound ; j++ ) {
		List<Integer> c = combinef1f2(Fk.get(i), Fk.get(j), SDC);
		if(c != null) {
		    // checking if the k-1 size subsets are present in Fk-1 or not
		    addCandidateToCk = sPresentInK(c);
		    if(addCandidateToCk) {
			currC = new ItemSet();
			for(int cItem : c) {
			    currC.add(cItem);
			}
			Ck.add(currC);
		    }
		}
		else {
		    break;
		}
	    }
	}
	return Ck;
    }

    public static boolean sPresentInK (List<Integer> c) {
	boolean present = true;
	boolean MIScondition = MISCheck(c);
	for (int index=0; index < c.size(); index++) {
	    List<Integer> currSubSet = c;
	    currSubSet.remove(index);
	    if(currSubSet.contains(c.get(0)) || MIScondition) {
		ItemSet currSubSetItemSet = new ItemSet();
		for(int currSubSetItem : currSubSet) {
		    currSubSetItemSet.add(currSubSetItem);
		}
		if(!Fk.contains(currSubSetItemSet)) {
		    present = false;
		    break;
		}
	    }
	    else {
		break;
	    }
	}
	return present;
    }

    public static boolean MISCheck(List<Integer> c) {
	if(c == null || c.size() < 2) {
	    return false;
	}

	return singleItemMap.get(c.get(0)).getMIS() == singleItemMap.get(c.get(1)).getMIS();
    }


    public static List<Integer> combinef1f2(ItemSet f1, ItemSet f2, double SDC) {
	// sanity check
	if (f1==null || f2 ==null) return null;
	if(f1.getItemsSet().size()!=f2.getItemsSet().size()) return null;

	int bound = f1.getItemsSet().size();

	List<Integer> f1List = new ArrayList<Integer> (f1.getItemsSet());
	List<Integer> f2List = new ArrayList<Integer> (f2.getItemsSet());

	int f1Last = f1List.remove(bound-1);
	int f2Last = f2List.remove(bound-1);

	double f1LastSup = singleItemMap.get(f1Last).getSupport();
	double f2LastSup = singleItemMap.get(f2Last).getSupport();

	if  (f1List.equals(f2List) && Math.abs(f1LastSup - f2LastSup)<=SDC) {

	    f1List.add(f1Last);
	    f1List.add(f2Last);
	    return f1List;
	}

	return null;

	/*for (int i=0; i< bound-1; i++) {
	   if(f1List.get(i) != f2List.get(i)) {
	       return false;
	   }
	}

	return true;*/

    }

    public static void main(String[] args) throws IOException {
	readInput();  
	System.out.println("generating L");
	initialPass();
	System.out.println("generating F1");
	generateF1();
	System.out.println("printing c2");
	generateLevel2Candidate(0.1);
	System.out.println("Implementing Algorithm");
	AprioriAlgorithm();

	System.out.println("Printing Fk's");
	for(ItemSet f : Fk) {
	    System.out.println(f.getItemsSet());
	}
	TestSuites();
    }

    public static void TestSuites() throws IOException {
	ItemSet i = new ItemSet();
	i.add(20);
	i.add(30);
	i.add(40);
	System.out.println(Transactions.getItemSetCount(i)==0);
	//0 because it is not present.

	ItemSet j = new ItemSet();
	j.add(20);
	j.add(30);
	j.add(50);

	System.out.println(Transactions.getItemSetCount(j)==2);
	//2 because it is present in first and last.

	System.out.println(combinef1f2(i, j, 0.1));

	i = new ItemSet();
	j = new ItemSet();

	i.add(20);
	i.add(30);
	i.add(40);
	i.add(50);

	j.add(20);
	j.add(30);
	j.add(50);
	j.add(60);

	//System.out.println(!differInLastItem(i, j));
    }
}
