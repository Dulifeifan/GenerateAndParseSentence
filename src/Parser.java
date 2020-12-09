/**
 * Parser based on the CYK algorithm.
 * 
 * Good for parsing a single sentence only.
 * 
 * @author sihong
 *
 */

import java.io.*;
import java.util.*;

public class Parser {

	/**
	 * This class defines a cell in the CYK table.
	 * 
	 * A cell at (i,j) contains productions that can generate words from i to j.
	 * Notes:
	 * 		1) more than one productions can have the same LHS
	 * 		2) the same production can be constructed using different constituents.
	 * 		3) need to store down-pointers pointing to the compromising constituents of each productions.
	 * 		4) a cell can be empty.
	 * 		5) shall facilitate LHS lookup.
	 * 
	 * @author sihong
	 *
	 */
	
	/**
	 * An entry in a cell.
	 * LHS -> RHS (left, right)
	 * @author sihong
	 *
	 */
	private class Entry {
		// LHS non-terminal
		private String lhs;
		
		// first symbol on the RHS of the production, referencing to an Entry object in the cell to the left of the current cell. 
		private Entry first_rhs;
		
		private Entry second_rhs;
		
		// terminal if this entry has a pre-terminal as its lhs
		private String word;
		
		// entry for non-terminals with two children
		public Entry(String _lhs, Entry _first_rhs, Entry _second_rhs) {
			this.lhs = _lhs;
			this.first_rhs = _first_rhs;
			this.second_rhs = _second_rhs;
			this.word = null;
		}
		
		// entry for pre-terminal rules
		public Entry(String _lhs, String _word) {
			this.lhs = _lhs;
			this.first_rhs = null;
			this.second_rhs = null;
			this.word = _word;
		}
	}
	
	private class Cell {
		// a list of partial parses for the current cell.
		// the key is for fast look up of non-terminals to construct constituents higher up. 
		//private Hashtable<String, ArrayList<Entry>> parses;
		// can have multiple entries with the same LHS.
		private ArrayList<Entry> parses;
		
		public Cell() {
			parses = new ArrayList<Entry>();
		}
		
		public void add(Entry e) {			
			parses.add(e);
		}
		
		public ArrayList<Entry> getParses() {
			return parses;
		}
		
		public Entry findNonTerminal(String lhs) {
			for (Entry e : parses) {
				if (e.lhs.equals(lhs)) {
					return e;
				}
			}
			return null;
		}
	}
	
	public Grammar g;
	
	private int numWords;
	
	private ArrayList<ArrayList<Cell> > table;
	
	public Parser(String grammar_filename) {
		g = new Grammar(grammar_filename);
	}
	
	// use partial parses in the left and right cell to build larger parses
	private ArrayList<Entry> buildLargerParses(Cell left, Cell right) {
		
		ArrayList<Entry> largerParses = new ArrayList<Entry>();
		for(int i=0;i<left.getParses().size();i++)
		{
			for(int j=0;j<right.getParses().size();j++)
			{
				if(g.findLHS(left.getParses().get(i).lhs+ " "+ right.getParses().get(j).lhs)!=null){
				for(int a=0; a<g.findLHS(left.getParses().get(i).lhs+ " "+ right.getParses().get(j).lhs).size();a++) {
					Entry e = new Entry(g.findLHS(left.getParses().get(i).lhs + " " + right.getParses().get(j).lhs).get(a),
							left.getParses().get(i), right.getParses().get(j));
					largerParses.add(e);
				}
				}
			}
		}
		if(largerParses.isEmpty())
		{
			return null;
		}
		return largerParses;
	}
	
	public void parse(ArrayList<String> sentence) {
		numWords=sentence.size();
		//System.out.println("strat parse");
		table=new ArrayList<>();
		ArrayList<Cell> inner1=new ArrayList<>();
		for(int tt=0;tt<sentence.size()+1;tt++){
			for(int ii=0;ii<sentence.size()+1;ii++)
			{
				inner1.add(new Cell());
			}
			table.add(inner1);}
		for(int j=1;j<=sentence.size();j++)
		{
			ArrayList<Cell> inner=new ArrayList<>();
			for(int ii=0;ii<sentence.size()+1;ii++)
			{
				inner.add(new Cell());
			}
			//System.out.println("j= "+ j);
			Cell c=new Cell();
			for(int a=0;a<g.findPreTerminals(sentence.get(j-1)).size();a++)
			{
				//System.out.println("a= " + a);
				Entry e = new Entry(g.findPreTerminals(sentence.get(j-1)).get(a),sentence.get(j-1));
				c.add(e);

			}
			inner.set(j,c);
			table.set(j-1,inner);
			Cell c1=new Cell();
			for(int i=j-2;i>=0;i--)
			{
				//System.out.println("i= "+i);
				for(int k=i+1;k<=j-1;k++)
				{
					ArrayList<Entry> lp = new ArrayList<Entry>();
					lp=buildLargerParses(table.get(i).get(k), table.get(k).get(j));
					if(lp!=null) {
						//System.out.println("k= "+k);
						for (Entry e : buildLargerParses(table.get(i).get(k), table.get(k).get(j))) {
							c1.add(e);
						}
						inner.set(j, c1);
						table.set(i, inner);
					}


				}
			}
		}



	}
	private String printEntry(Entry e) {
		String res = " (" + e.lhs + "";
		
		if (e.first_rhs != null) {
			res += printEntry(e.first_rhs) + "";
		}
		
		if (e.second_rhs != null) {
			res += printEntry(e.second_rhs) + "";
		}
		
		if (e.word != null) {
			res += (" " + e.word);
		}
		res += ")";
		return res;
	}
	
	public String PrintOneParse() {

		//ArrayList<Entry> res = table.get(0).get(numWords - 1).getParses();
		ArrayList<Entry> res = table.get(1).get(numWords).getParses();
		if (res.size() == 0) {
			//System.out.println("Failed to parse the sentence.");
			return null;
		}
		return printEntry(res.get(numWords-2));
//		System.out.println(parseStr);
	}
	
	public static void main(String[] args) throws IOException {
		Parser parser = new Parser(args[0]);
		String end = ".";
		//ArrayList<String> sentence = new ArrayList<String>();
		
		/*String []array = new String[] {"the",
						                "sandwich",
						                "ate",
						                "a",
						                "staff",
						                "."}; // I can't handle . and !
		*/				                /*"with",
						                "the",
						                "sandwich",
						                "."};*/ 
		ArrayList<String> sentence = new ArrayList<String>();
		/*for (String s: array) {
			 // I can't handle . and !
			if (!s.equals(".") && !s.equals("!")) {
				sentence.add(s);
			} else {
				end = s;
			}
		}*/
		
		// read a parse tree from a bash pipe
		try {
			InputStreamReader isReader = new InputStreamReader(System.in);
			BufferedReader bufReader = new BufferedReader(isReader);
			while(true) {
				String line = null;
				if((line=bufReader.readLine()) != null) {
					String []words = line.split(" ");
					for (String word : words) {
						word = word.replaceAll("[^a-zA-Z]", "");
						if (word.length() == 0) {
							continue;
						}
//						System.out.println(word);
						// use the grammar to filter out non-terminals and pre-terminals
						if (parser.g.symbolType(word) == 0 && (!word.equals(".") && !word.equals("!"))) {
							sentence.add(word);
							//System.out.println(word);
						}
					}
				}
				else {
					break;
				}
			}
			bufReader.close();
			isReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}


		parser.parse(sentence);
	//	System.out.print(sentence);
		System.out.println("(ROOT " + parser.PrintOneParse() + " " + end + ")");

	}

}
