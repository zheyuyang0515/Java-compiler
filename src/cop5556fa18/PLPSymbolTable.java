/**
 * This is the class of symbol table
 * including implementation methods and data structure: hashtable and stack
 * Name: Zheyu Yang
 * Assignment number: Project #5
 * Date Due: November 05, 2018
 */
package cop5556fa18;

import java.util.HashMap;
import java.util.Map;

import cop5556fa18.PLPScanner.Kind;
import cop5556fa18.PLPScanner.Token;
import cop5556fa18.PLPAST.Declaration;
import cop5556fa18.PLPAST.VariableDeclaration;

public class PLPSymbolTable {
	int  current_scope, next_scope = -1;
	//hash table
	HashMap<VariableDeclaration, Integer> hashTable; 
	private StackNode topNode;		//top node
	private int stackSize;			//stack current size
	public class StackNode{
		int data;
		StackNode next;
		public StackNode(int data, StackNode next) {
			this.data = data;
			this.next = next;
		}
	}
	public class SymbolScope{
		String identifier;
		int scopeNum;
		public SymbolScope(String identifier, int scopeNum) {
			this.identifier = identifier;
			this.scopeNum = scopeNum;
		}
	}
	//constructor
	public PLPSymbolTable() {
		//description of the hash table
		hashTable = new HashMap<VariableDeclaration, Integer>();
		//description of the stack
		topNode = null;		
	}
	/*implementation of the stack
	 * push(),pop(),fetchTop()
	 */
	private void push(int scopeNum) {
		topNode = new StackNode(scopeNum, topNode);
		stackSize++;
	}
	private int pop() {
		StackNode node = topNode; 
		topNode = topNode.next; 
		node.next = null;
	    stackSize --;
	    return node.data;
	}
	private StackNode fetchTop() {
		if(stackSize != 0) {
            return topNode;
		} else {
			return null;
		}
	}
	public void enterScope() {
		current_scope = next_scope + 1; 
		push(current_scope);
		next_scope++;
	}
	public void closeScope(){  
		pop();
		StackNode topNode = fetchTop();
		if(topNode == null) {
			current_scope = 0;
			next_scope = 0;
		} else {
			current_scope = fetchTop().data;
		}		
	}

	/*implementation of the hash table
	 * add(), lookup()
	 */
	public void add(VariableDeclaration d, int scopeNum) {
		//SymbolScope symbolScope = new SymbolScope(identifier, scopeNum);
		hashTable.put(d, scopeNum);
		//System.out.println("hashtable size: " + hashTable.size());
	}
	public int checkScope(String identifier) {
		//int identNum;
		int scopeNum0 = -1;
		//int scopeNum;
		//int closest;
		StackNode s = fetchTop();
		while(s != null) {
			//identNum = 0;
			//scopeNum0 = -1;
			//scopeNum = 0;
			//closest = 0;
			for(Map.Entry<VariableDeclaration, Integer> entry: hashTable.entrySet())
	        {
				if(entry.getKey().name.equals(identifier) && entry.getValue() == s.data) {
					scopeNum0 = entry.getValue();
					break;
				}
	        }
			if(scopeNum0 != -1) {
				break;
			}
			s = s.next;
		}
		return scopeNum0;
	}
	public Declaration lookup(String identifier) {
		VariableDeclaration d = null;
		//int closest = 0;
		//int identNum = 0;
		StackNode s = fetchTop();
		while(s != null) {
			//closest = 0;
			//identNum = 0;
			//d = null;
			for(Map.Entry<VariableDeclaration, Integer> entry: hashTable.entrySet())
	        {
				/*if(entry.getKey().identifier.equals(identifier)) {
					scopeNum = entry.getKey().scopeNum;
					if((current_scope - scopeNum) >= 0) {
						identNum++;
						if(identNum == 1 || (current_scope - scopeNum) < closest) {
							closest = (current_scope - scopeNum);
							type = entry.getValue();
						}
					}
				}*/
				if(entry.getKey().name.equals(identifier) && entry.getValue() == s.data) {
					//System.err.println(entry.getKey().name);
					d = entry.getKey();
					break;
				}
	        }
			if(d != null) {
				break;
			}
			s = s.next;
		}	
		return d;
	}			
}
