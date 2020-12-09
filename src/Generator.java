/**
 * Generate sentences from a CFG
 * 
 * @author sihong
 *
 */

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

public class Generator {
	
	private Grammar grammar;

	/**
	 * Constructor: read the grammar.
	 */
	public Generator(String grammar_filename) {
		grammar = new Grammar(grammar_filename);
	}

	/**
	 * Generate a number of sentences.
	 */
	public ArrayList<String> generate(int numSentences) {
		ArrayList<String> result = new ArrayList<>();
		for(int s=0;s<numSentences;s++) {
			ArrayList<String> sentence = new ArrayList<>();
			Stack<String> nodeStack = new Stack<>();
			Random random = new Random();
			String node="ROOT";
			nodeStack.add(node);
			while (!nodeStack.isEmpty()) {
				node = nodeStack.pop();
				if(grammar.symbolType(node)!=0) {
					sentence.add(" (" + node +" ");
					int Xindex=-1;
					if(grammar.findProductions(node).size()!=1) {
						Xindex = 0;
						for (int i = 0; i < grammar.findProductions(node).size(); i++) {
							if (grammar.findProductions(node).get(i).getProb() <
									grammar.findProductions(node).get(Xindex).getProb()) {
								Xindex = i;
							}
						}
						if (Xindex == 0) {
							if (grammar.findProductions(node).get(0).getProb() ==
									grammar.findProductions(node).get(1).getProb())
							{
								Xindex=-1;
							}
						}
					}
					int index=-2;
					do{
						index = random.nextInt(grammar.findProductions(node).size());
					}while (index==Xindex);
					ArrayList<String> children = new ArrayList<>();
					children.add(grammar.findProductions(node).get(index).second());
					children.add(grammar.findProductions(node).get(index).first());
					if (children != null && !children.isEmpty()) {
						nodeStack.push(")");
						for (String child : children) {
							if(child!=null)
							nodeStack.push(child);
						}
					}
				}
				else {
					sentence.add(node);
				}
			}
			String r="";
			for(int i=0;i<sentence.size();i++)
			{
				r=r+sentence.get(i);

			}
			//System.out.print(r);
			result.add(r);
		}
		return result;
	}
	
	public static void main(String[] args) throws IOException {
		// the first argument is the path to the grammar file.
		Generator g = new Generator(args[0]);
		ArrayList<String> res = g.generate(1);
		//FileWriter fw1 = new FileWriter("./results/sentence.txt");
		//BufferedWriter bw1 = new BufferedWriter(fw1);
		for (String s : res) {
			System.out.println(s);
			//bw1.write(s);
			//bw1.flush();
		}
		//bw1.close();
	}
}
