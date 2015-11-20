/*
 * Information Retrieval Programming Assignment
 * 
 * Mihir Kulkarni
 * Person Number: 50168610
 * mihirdha@buffalo.edu
 * (716)907-5846
 * 
 *  
 */
//package irProject;

import java.io.*;
import java.util.Map.Entry;
import java.util.*;

public class CSE535Assignment {

	static String fileName;
	static String outputFile;
	static String queryFile;
	static Integer docCount,compCount,secCount;
	static double startTime;
	static double stopTime;
	static double elapsedTime;
	static LinkedList<ListIterator> iList;
	static LinkedList<LinkedList<Integer>> pListList;
	static LinkedList<Entry<String,LinkedList<posting>>> optimizedQueryTerms;
	
	
	/*
	 * Class to store term and postings list
	 */
	static class term {
		String name;
		LinkedList<posting> pl;
		Integer nPostings;
		term(String name){
			this.name=name;
		}
	}
	/*
	 * Individual posting of a term.
	 */
	static class posting {
		Integer docID;
		Integer num;
		
	}
	static Integer K;
	/*
	 * HashMap representing whole index file
	 */
	static HashMap<String,LinkedList<posting>> termsHM=new HashMap<String,LinkedList<posting>> ();
	/*
	 * Coparator to compare terms based on number of postings
	 */
	static public class nPostingsComp implements Comparator<term>{	 
	    @Override
	    public int compare(term t1,term t2) {
	        if(t1.nPostings < t2.nPostings)
	            return 1;
	        else if (t1.nPostings == t2.nPostings)
	            return 0;
	        else
	        	return -1;
	    }
	}
	/*
	 * Compares two Integers
	 */
	static public class integerComp implements Comparator<Integer>{	 
	    @Override
	    public int compare(Integer i1,Integer i2) {
	        if(i1 < i2)
	            return -1;
	        else if (i1==i2)
	            return 0;
	        else
	        	return 1;
	    }
	}
	/*
	 * Compares two postings based on document IDs
	 */
	static public class docIDComp implements Comparator<posting>{	 
	    @Override
	    public int compare(posting p1,posting p2) {
	        if(p1.docID < p2.docID)
	            return -1;
	        else if (p1.docID == p2.docID)
	        	return 0;
	        else 
	            return 1;
	    }
	}
	/*
	 * Compares query terms based on Number of postings
	 */
	static public class queryTermComp implements Comparator<Entry<String,LinkedList<posting>>>{	 
	    @Override
	    public int compare(Entry<String,LinkedList<posting>> e1,Entry<String,LinkedList<posting>> e2) {
	        if(e1.getValue().size() < e2.getValue().size())
	            return -1;
	        else if (e1.getValue().size() == e2.getValue().size())
	        	return 0;
	        else 
	            return 1;
	    }
	}
	/*
	 * Compares two postings based on term frequencies
	 */
	static public class TFComp implements Comparator<posting>{
	    @Override
	    public int compare(posting p1,posting p2) {
	        if(p1.num < p2.num)
	            return 1;
	        else if (p1.num == p2.num)
	        	return 0;
	        else
	            return -1;
	    }
	}
	
	/*
	 * returns postings sorted by document IDs
	 */
	static LinkedList<posting> getPostingsByDocID(String query){
				if(termsHM.get(query)==null)
					return null;
				termsHM.get(query).sort(new docIDComp());
				return termsHM.get(query);
	}
	/*
	 * returns postings sorted by term frequencies
	 */
	static LinkedList<posting> getPostingsByTF(String query){
		if(termsHM.get(query)==null)
			return null;
		termsHM.get(query).sort(new TFComp());
		return termsHM.get(query);
	}
	/*
	 * returns linkedlist of top K terms
	 */
	static LinkedList<Entry<String, LinkedList<posting>>> getTopK(){
		LinkedList<Entry<String, LinkedList<posting>>> temp=new LinkedList<Entry<String, LinkedList<posting>>>();
		List<Entry<String, LinkedList<posting>>> l;
		Set<Entry<String, LinkedList<posting>>> s;
		s= termsHM.entrySet();
		l= new ArrayList<Entry<String, LinkedList<posting>>>(s);//store in list to sort
		Collections.sort( l, new Comparator<Entry<String,LinkedList<posting>>>(){//comparator to sort entries
			@Override
			public int compare(Entry<String, LinkedList<posting>> e1, Entry<String, LinkedList<posting>> e2) {
				// TODO Auto-generated method stub
				if(e1.getValue().size()< e2.getValue().size())
					return 1;
				else if(e1.getValue().size()==e2.getValue().size())
					return 0;
				return -1;
			}
        	} );
		for(int j=0;j<K;j++){//Show first K elements
			temp.add(l.get(j));
		}
		return temp;
	}
	/*
	 * performs logical OR(term at a time) of two linked lists
	 */
	@SuppressWarnings("unchecked")
	static LinkedList<Integer> getOR(LinkedList<Integer> l1, LinkedList<Integer> l2){
		LinkedList<Integer> result= new LinkedList<Integer>();
		if(l1==null && l2==null)
			return null;//both null
		else if(l1==null)
			return l2;
		else if(l2==null)
			return l1;
		Iterator<Integer> iterator = l2.iterator();
		boolean contains;
		
		result= (LinkedList<Integer>) l1.clone();//add whole list  1 in result
		Iterator<Integer> resultit = result.iterator();
		
		while(iterator.hasNext()){//while list 2 has elements
			Integer p= (Integer) iterator.next();
			resultit = result.iterator();
			contains = false;
			while(resultit.hasNext()){//iterate over result
				compCount++;
				if(p.equals(resultit.next())){//check if element already present
					contains=true;
				}
				
			}
			if(contains==false){//if element not present
				result.add(p);// add in result
			}
			
		}
		return result;
			
	}
	/*
	 * performs logical AND(term at a time) of two linked lists
	 */
	static LinkedList<Integer> getAND(LinkedList<Integer> l1, LinkedList<Integer> l2){
		LinkedList<Integer> result= new LinkedList<Integer>();
		Integer temp;
		if(l1==null && l2==null)
			return null;//both null
		else if(l1==null)
			return l2;
		else if(l2==null)
			return l1;
		Iterator<Integer> it1=l1.iterator();
		Iterator<Integer> it2=l2.iterator();
		while(it1.hasNext()){//while list 1 has elements
			it2=l2.iterator();
			temp=(Integer) it1.next();
			while(it2.hasNext()){//iterate over list 2
				compCount++;
				if(temp.equals(it2.next())){//if element present in both
					result.add(temp);//add in result
				}
				
			}
		}
		
		return result;
		
	}
	/*
	 * performs logical AND of postings list(term at a time) of N query terms given by queryList
	 */
	static LinkedList<Integer> termAtATimeQueryAnd(List<String> queryList){
		boolean termsFound=true;
		startTime=System.currentTimeMillis();
		LinkedList<Integer> result= new LinkedList<Integer>();
		LinkedList<Integer> tempResult= new LinkedList<Integer>();
		docCount=0;
		compCount=0;
		for(String q:queryList){
			if(termsHM.get(q)!=null)
				docCount=docCount+termsHM.get(q).size();
		}
		
		result=stripTF(getPostingsByTF(queryList.get(0)));//gets list of document IDs of 0th query term sorted by term frequencies
		if (result==null)
			termsFound=false;//atleast 1 query term is not found in index
		for(int i=1;i<queryList.size();i++){
			tempResult=stripTF(getPostingsByTF(queryList.get(i)));//gets list of document IDs of i th query term sorted by term frequencies
			if(tempResult==null)
				termsFound=false;//atleast 1 query term is not found in index
			result=getAND(result,tempResult);//iteratively AND with result
		}
		stopTime = System.currentTimeMillis();
		elapsedTime=(stopTime-startTime)/1000.0;
		if(termsFound==false)
			return null;//atleast 1 quer term is not found in index, so return null
		result.sort(new integerComp());//sort by document IDs
		return result;//all query terms found in index, return AND of their postings list
	}
	/*
	 * performs logical OR of postings list(term at a time) of N query terms given by queryList
	 */
	static LinkedList<Integer> termAtATimeQueryOr(List<String> queryList){
		LinkedList<Integer> tempResult= new LinkedList<Integer>();
		boolean termsFound=false;
		startTime=System.currentTimeMillis();
		LinkedList<Integer> result= new LinkedList<Integer>();
		docCount=0;
		compCount=0;
		for(String q:queryList){
			if(termsHM.get(q)!=null)
				docCount=docCount+termsHM.get(q).size();
		}
		result=stripTF(getPostingsByTF(queryList.get(0)));
		if (result!=null)
			termsFound=true;//atleast 1 query term is found in index
		for(int i=1;i<queryList.size();i++){
			tempResult=stripTF(getPostingsByTF(queryList.get(i)));//gets list of document IDs of i th query term sorted by term frequencies
			if(tempResult!=null)
				termsFound=true;//atleast 1 query term is found in index
			result=getOR(result,tempResult);
		}
		stopTime = System.currentTimeMillis();
		elapsedTime=(stopTime-startTime)/1000.0;
		if(termsFound==false)
			return null;//none of the query terms found in index, return null
		result.sort(new integerComp());//sort by document IDs
		return result;//atleast 1 query terms found in index, return OR of their postings list
	}
	/*
	 * performs logical AND of postings list(document at a time) of N query terms given by queryList
	 */
	static LinkedList docAtATimeQueryAnd(List<String> queryList){
		docCount=0;
		compCount=0;
		startTime=System.currentTimeMillis();
		
		LinkedList result= new LinkedList();
		pListList=new LinkedList<LinkedList<Integer>>();//Linked list to store postings list of query terms
		iList= new LinkedList<ListIterator>();//linked list of iterators
		for(String q:queryList){
			docCount=docCount+termsHM.get(q).size();//If queryTerm matches the term, increase document count by number of elements in posting list
			
			pListList.add(stripTF(getPostingsByDocID(q)));//populate pListList
			iList.add(pListList.getLast().listIterator());//populate corrosponding iterator
		}
		while(hasNextNList()){//while all iterators have atleast 1 untraversed element
			if(checkEqual()){//check if all iterators are pointing to same document IDs
				result.add(iList.get(0).next());//add in result
				if(hasNextNList()){
					advanceTill(findMax());//find maximum number pointed by iterator in iterator list and advance all iterator to that number or greater
				}
				else
					continue;
			}
			else{
				if(hasNextNList())
					advanceTill(findMax());
			}
				
		}
		stopTime = System.currentTimeMillis();
		elapsedTime=(stopTime-startTime)/1000.0;
		return result;
	}
	
	/*
	 * Advances all postings pointers till document ID=m 
	 */
	private static void advanceTill(Integer m) {
		for (int i=0;i<iList.size();i++){
			int t=(int) iList.get(i).next();
			while(t<m && iList.get(i).hasNext() ){//till iterator hasnt reached value m
				compCount++;//increase comparison count
				t=(int) iList.get(i).next();//advance iterator
			}
			if(iList.get(i).hasPrevious()&&iList.get(i).hasNext())
				iList.get(i).previous();// move back iterator by 1 so that current element can be accessed by caalling next
		}
	}
	/*
	 * Finds maximum element pointed between all posting iterators 
	 */
	private static Integer findMax() {
		Integer max=pListList.get(0).get(iList.get(0).nextIndex());
		for (int i=1;i<iList.size();i++){
			compCount++;
			if (pListList.get(i).get(iList.get(i).nextIndex())>max){//if we get element greater than max
				max=pListList.get(i).get(iList.get(i).nextIndex());// set max to that element
			}
		}
		return max;
	}
	/*
	 * Check if elements pointed by all iteratoors are equal
	 */
	private static boolean checkEqual() {
		Integer temp=pListList.get(0).get(iList.get(0).nextIndex());
		for (int i=0;i<iList.size();i++){
			int temp2=pListList.get(i).get(iList.get(i).nextIndex());
			if (temp2!=temp){
				return false;//atleast two elements are not equal
			}
		}
		return true;// all elements are equal
	}
	/*
	 * Check if all iterators have atleast one more element remaining to be traversed
	 */
	private static boolean hasNextNList() {
		for (ListIterator i:iList){
			if(!i.hasNext())
				return false;//atleast one list has reached it's end
		}
		return true;//all lists have elements remaining
	}
	/*
	 * performs logical AND of postings list(document at a time) of N query terms given by queryList
	 */
	static LinkedList docAtATimeQueryOr(List<String> queryList){
		docCount=0;
		compCount=0;
		startTime=System.currentTimeMillis();
		LinkedList<Integer> result= new LinkedList();
		pListList=new LinkedList<LinkedList<Integer>>();
		iList= new LinkedList<ListIterator>();
		for(String q:queryList){
			if(termsHM.get(q)!=null)
				docCount=docCount+termsHM.get(q).size();
		
			if(stripTF(getPostingsByDocID(q))!=null){
				pListList.add(stripTF(getPostingsByDocID(q)));//populate pListList
				iList.add(pListList.getLast().listIterator());//populate iterator list
			}
		}
			/*
			
			Algorithm
			while at least one hasnext
				Find Min of those which havenext
				Add min in result
				Find all iterators with value as min
				Advance those iterators if they hasNext
		*/
		
		while(hasNextOr()){
			Integer x;
			x=findMin();
			result.add(x);
			advanceIteratorsWithValue(x);
		}
		
		stopTime = System.currentTimeMillis();
		elapsedTime=(stopTime-startTime)/1000.0;
		return result;
	}
	/*
	 * advances iterators by 1 which pooint to value x
	 */
	private static void advanceIteratorsWithValue(Integer x) {
		for (int i=0;i<iList.size();i++){
			int val;
			if (iList.get(i).hasNext()){//iterate over list if iterators
				val = pListList.get(i).get(iList.get(i).nextIndex());
				compCount++;
				if(val==x){// check if iterator points to element given by x
				iList.get(i).next();//advance that iterator by 1
				}
			}
		}
	}
	/*
	 * finds minimum of the elements pointed by the iterators
	 */
	private static Integer findMin() {
		if(!hasNextOr())
			return null;
		Integer min=(int) Double.POSITIVE_INFINITY;
		for (int i=0;i<iList.size();i++){
			compCount++;
			if ( iList.get(i).hasNext()&& pListList.get(i).get(iList.get(i).nextIndex())<min){// if elemnt found less than min
				min=pListList.get(i).get(iList.get(i).nextIndex());//set min to that element
			}
		}
		return min;
	}
	/*
	 * checks if atleast one iterator has atleast 1 element to be traversed
	 */
	private static boolean hasNextOr() {
		for (ListIterator i:iList){
			if(i.hasNext())
				return true;//atleast 1 iterator has elemnt remaining
		}
		return false;//no iterator has elemnt remaining
	}
	/*
	 * eliminates frequency from postings list to get list of just document IDs
	 */
	static LinkedList<Integer> stripTF(LinkedList<posting> l){
		LinkedList<Integer> temp=new LinkedList<Integer>();
		if(l==null)
			return null;
		for (posting p:l)//iterate over postings list
			temp.add(p.docID);//add only document ID
		return temp;
	}
	/*
	 * main reads files and arguments and calls 
	 * parseadd() on each line of query file
	 * Calls printlog() to print the log file
	 */
	public static void main(String[] args) throws IOException {
		LinkedList<posting> resultp= new LinkedList();
		LinkedList result= new LinkedList();
		LinkedList<String> queryList=new LinkedList();
		
		//get program arguments
		fileName=args[0];
		outputFile=args[1];
		K=Integer.valueOf(args[2]);
		queryFile=args[3];
		
		String line= null;
		LinkedList terms=new LinkedList();
		
		//read index file
		FileReader f=  new FileReader(fileName);
		BufferedReader b=new BufferedReader(f);
		while((line=b.readLine())!=null)//for each line in index file
			parseAdd(line);//parse line and add in linked list data structure
		f.close();
		
		printLog();//call the algos and print output in log file
	}
	/*
	 * Calls all functions and writes result in log file
	 */
	static void printLog() throws IOException{
		PrintWriter writer = new PrintWriter(outputFile, "UTF-8");
		
		
		LinkedList<Entry<String, LinkedList<posting>>> resultt= new LinkedList();
		LinkedList<posting> postingsByDocID=new LinkedList();
		LinkedList<posting> postingsByTF=new LinkedList();
		LinkedList temp = new LinkedList();
		LinkedList<Integer> resultLLAnd= new LinkedList<Integer>();
		LinkedList<Integer> resultLLOr= new LinkedList<Integer>();
		String result;
		
		
		//Top K Terms
		writer.println("FUNCTION: getTopK "+K);
		resultt=getTopK();
		writer.print("Result: ");
		temp.clear();
		for(Entry<String, LinkedList<posting>> e:resultt){
			temp.add(e.getKey());
		}
		writer.println(temp.toString().replaceAll("\\[", "").replaceAll("\\]",""));
		
		
		//Query File
		String queryLine;
		List<String> query;
		List<String> optimizedQuery = new ArrayList();
		optimizedQueryTerms=new LinkedList();
		FileReader q=  new FileReader(queryFile);
		BufferedReader bq=new BufferedReader(q);
		while((queryLine=bq.readLine())!=null){//for each query line
			optimizedQueryTerms.clear();
			optimizedQuery.clear();
			query = Arrays.asList(queryLine.split(" "));
			
		//FUNCTION: getPostings
			for(String queryTerm:query){//for each query term
				writer.println("FUNCTION: getPostings "+ queryTerm);
				writer.print("Ordered by doc IDs: ");
				postingsByDocID=getPostingsByDocID(queryTerm);//postings by document IDs
				if(postingsByDocID!=null){
					temp.clear();
					for(posting p:postingsByDocID){
						temp.add(p.docID);
					}
					writer.println(temp.toString().replaceAll("\\[","").replaceAll("\\]",""));
				}
				else
					writer.println("term not found");
				
				writer.print("Ordered by TF: ");
				postingsByTF=getPostingsByTF(queryTerm);//postings by term frequency
				if(postingsByTF!=null){
					temp.clear();
					for(posting p:postingsByTF){
						temp.add(p.docID);
					}
					writer.println(temp.toString().replaceAll("\\[","").replaceAll("\\]",""));
				}
				else
					writer.println("term not found");
			}
			
			
		//Optimize the query
			Set<Entry<String, LinkedList<posting>>> s = termsHM.entrySet();
	        List<Entry<String, LinkedList<posting>>> l = new ArrayList<Entry<String, LinkedList<posting>>>(s);
			for(String qTerm:query){
				for(Entry<String,LinkedList<posting>> e:l){
					if(qTerm.equals(e.getKey()))
						optimizedQueryTerms.add(e);
				}
			}
			optimizedQueryTerms.sort(new queryTermComp());//sort according to size of postings list
			for(Entry<String,LinkedList<posting>> e:optimizedQueryTerms){
				optimizedQuery.add(e.getKey());
			}
			
			
		//FUNCTION: termAtATimeQueryAnd 
			writer.println("FUNCTION: termAtATimeQueryAnd "+query.toString().replaceAll("\\[","").replaceAll("\\]","")); 
			resultLLAnd=termAtATimeQueryAnd(query);
			if(resultLLAnd==null)
				writer.println("terms not found");
			else{
				result=resultLLAnd.toString().replaceAll("\\[","").replaceAll("\\]","");//call to function
				writer.println(docCount + " documents are found");
				writer.println(compCount + " comparisions are made");	
				writer.println(elapsedTime +" seconds are used");
				result=termAtATimeQueryAnd(optimizedQuery).toString().replaceAll("\\[","").replaceAll("\\]","");//optimized call
				writer.println(compCount + " comparisons are made with optimization (optional bonus part)");
				writer.println("Result: "+result);
			}
		//FUNCTION: termAtATimeQueryOr 
			writer.println("FUNCTION: termAtATimeQueryOr "+query.toString().replaceAll("\\[","").replaceAll("\\]","")); 
			resultLLOr=termAtATimeQueryOr(query);
			if(resultLLOr==null)
				writer.println("terms not found");
			else{
				result=resultLLOr.toString().replaceAll("\\[","").replaceAll("\\]","");//call to function
				writer.println(docCount + " documents are found");
				writer.println(compCount + " comparisions are made");
				writer.println(elapsedTime +" seconds are used");
				result=termAtATimeQueryOr(optimizedQuery).toString().replaceAll("\\[","").replaceAll("\\]","");//optimized call
				writer.println(compCount + " comparisons are made with optimization (optional bonus part)");
				writer.println("Result: "+result);
			}	
			
		//FUNCTION: docAtATimeQueryAnd 
			writer.println("FUNCTION: docAtATimeQueryAnd "+query.toString().replaceAll("\\[","").replaceAll("\\]","")); 
			if(resultLLAnd==null)
				writer.println("terms not found");
			else{
				result=docAtATimeQueryAnd(query).toString().replaceAll("\\[","").replaceAll("\\]","");//call to function
				writer.println(docCount + " documents are found");
				writer.println(compCount + " comparisions are made");
				writer.println(elapsedTime +" seconds are used");
				writer.println("Result: "+result);
			}	
		//FUNCTION: docAtATimeQueryOr
				writer.println("FUNCTION: docAtATimeQueryOr "+query.toString().replaceAll("\\[","").replaceAll("\\]","")); 
				if(resultLLOr==null)
					writer.println("terms not found");
				else{
				result=docAtATimeQueryOr(query).toString().replaceAll("\\[","").replaceAll("\\]","");//call to function
				writer.println(docCount + " documents are found");
				writer.println(compCount + " comparisions are made");
				writer.println(elapsedTime +" seconds are used");
				writer.println("Result: "+result);
				}
		}
		
		writer.close();
	}
	/*
	 * parses the term.idx file and stores the index in the LinkedList 
	 * data structure created
	 */
	static void parseAdd(String line){
		String postingsList;
		
		//line parsing
		String t=line.split("\\\\c")[0];
		termsHM.put(line.split("\\\\c")[0], new LinkedList<posting>());//add term name
		int nPostings=Integer.valueOf(line.split("\\\\c")[1].split("\\\\m")[0]);
		postingsList=line.split("\\\\c")[1].split("\\\\m")[1];
		postingsList=postingsList.replaceAll("\\[","");
		postingsList=postingsList.replaceAll("\\]","");
		for(int i=0;i<nPostings;i++)
		{
			posting p=new posting();//temp object
			p.docID=Integer.valueOf(postingsList.split(",")[i].split("/")[0].trim());//add document ID
			p.num=Integer.valueOf(postingsList.split(",")[i].split("/")[1].trim());//add frequency
			if(termsHM.get(t) != null)
				termsHM.get(t).add(p);//add object in posting list of recently added term
			else
			{
				termsHM.put(t, new LinkedList<posting>());//intialize and them add
				termsHM.get(t).add(p);
			}
		}
		termsHM.get(t).sort(new docIDComp());//sort postings list by document ID
	}

}
//Reference:To sort map: http://java2novice.com/java-interview-programs/sort-a-map-by-value/