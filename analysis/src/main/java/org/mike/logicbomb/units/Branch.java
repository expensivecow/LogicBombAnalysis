package org.mike.logicbomb.units;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mike.logicbomb.helpers.Truple;
import org.mike.logicbomb.helpers.Usage;

import soot.Local;
import soot.Unit;
import soot.Value;
import soot.jimple.BinopExpr;
import soot.jimple.CastExpr;
import soot.jimple.DefinitionStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Ref;
import soot.jimple.UnopExpr;
import soot.toolkits.graph.Block;

/**
 * Abstraction of a Branch
 */
public class Branch {
	private final List<Condition> predicates;
	private final HashMap<Condition, List<Usage>> variableUses;
	private final HashMap<Value, Truple> variables;
	private final Block start;
	private final Set<Block> blocks;
	private final Set<Value> conditionVariables;
	private final String name;
	private int totalUnits;
	private int assignments;

	public Branch(String name, Block start, List<Condition> predicates){
		this.name = name;
		this.start = start;
		this.totalUnits = 0;
		this.variableUses = new HashMap<>();
		this.predicates = new ArrayList<>(predicates);
		this.blocks = new HashSet<>();
		this.variables = new HashMap<>();
		this.assignments = 0;
		this.conditionVariables = new HashSet<>();
	}

	public Branch(String name, Block start){
		this(name, start,new ArrayList<>());
	}

	private void addCondUsage(Condition c, Usage u){
		if(!variables.keySet().contains(c)){
			variableUses.put(c,new ArrayList<>());
		}
		variableUses.get(c).add(u);
	}

	public void explore(){

		initCondVariables();
		int totalUnitNumber = 0;
		for(Block b : blocks){
			for(Unit u : b){

				//System.out.println("UNIT : "+ u + "\nDEF : " + u.getDefBoxes() + "\nUSE : " + u.getUseBoxes() +"\n");

				totalUnitNumber ++;
				//Need to consider every possible type

				//Definition Stmt also includes IdentityStmt and AssignStmt
				if(u instanceof DefinitionStmt){
					Value left = ((soot.jimple.DefinitionStmt)u).getLeftOp();
					Value right = ((soot.jimple.DefinitionStmt)u).getRightOp();

					//If the left side was never used, we have a new variable
					boolean leftIsCondVar = false;
					for(Condition c : predicates){
						if(c.isInExpr(left)){
							leftIsCondVar = true;
						}
					}

					if(!variables.keySet().contains(left) && !leftIsCondVar) {
						variables.put(left, new Truple(0,0,0));
					}
					else{
						assignments ++;
					}

					//Right is a local i.e a single variable
					if(right instanceof Local){
						exploreHelperOneValue(right);
					}
					//Right is a Binop. Expression, need to analyse both sides
					else if(right instanceof BinopExpr){
						BinopExpr expr = (BinopExpr)right;
						exploreHelperOneValue(expr.getOp1());
						exploreHelperOneValue(expr.getOp2());
					}
					//Right is a cast, only one op to check
					else if(right instanceof CastExpr){
						exploreHelperOneValue(((CastExpr)right).getOp());
					}
					//Right is a Unop, only one op to check
					else if(right instanceof UnopExpr){
						exploreHelperOneValue(((UnopExpr)right).getOp());
					}
					//Right is an Invoke, check the arguments of the invoke
					else if(right instanceof InvokeExpr){
						for(Value v : ((InvokeExpr)right).getArgs()){
							exploreHelperOneValue(v);
						}
					}
					//Right is a Reference, check if the reference is in the list of variables
					else if(right instanceof Ref){
						exploreHelperOneValue(right);
					}

				}
				//Invoke stmt -> Method call
				else if(u instanceof InvokeStmt){
					InvokeExpr ie = ((InvokeStmt)u).getInvokeExpr();
					List<Value> arguments = ie.getArgs();

					for(Value v : arguments) {
						for (Condition c : predicates) {
							if (c.isInExpr(v)) {
								Usage usage = new Usage(c, v);
								if(!variables.keySet().contains(c)){
									variableUses.put(c,new ArrayList<>());
								}
								variableUses.get(c).add(usage);
							}
						}
						for (Value k : variables.keySet()) {
							if (v.equivTo(k)) {
								variables.get(k).incB();
							}
						}
					}
				}
			}
		}

		this.totalUnits = totalUnitNumber;
	}

	private void exploreHelperOneValue(Value val){
		for(Condition c : predicates){
			if(c.isInExpr(val)){
				Usage usage = new Usage(c,val);
				addCondUsage(c, usage);
			}
		}

		boolean found = false;
		for(Value v : variables.keySet()){
			if(val.equivTo(v)){
				variables.get(v).incB();
				found = true;
			}
		}
		if(!found){
			variables.put(val, new Truple(1,0,0));
		}
	}

	public void addBlock(Block b){
		this.blocks.add(b);
	}

	public boolean containsBlock(Block b){
		return blocks.contains(b);
	}

	public int getTotalUnits(){
		return this.totalUnits;
	}

	public Set<Block> getBlocks() {
		return blocks;
	}

	public void removeBlock(Block block){
		blocks.remove(block);
	}

	public void initCondVariables(){
		for(Condition c : predicates){
			conditionVariables.addAll(c.extractVariables());
		}
	}

	public Set<Value> getConditionVariables(){
		return this.conditionVariables;
	}

	public int getAssignments(){
		return assignments;
	}

	public double getTotalUse(){
		return getCondVariableUses() + getNonCondVariableUses();
	}

	public Block getStart(){
		return start;
	}

	public double getCondVariableUses(){
		int size = 0;
		for(List<Usage> uses : variableUses.values()){
			size += uses.size();
		}
		return (double)size;
	}

	public int getNonCondVariableUses(){
		int c = 0;
		for(Truple i : variables.values()){
			c += i.b();
		}
		return c;
	}

	public int getNbNonCondVariables(){
		return variables.keySet().size();
	}

	public Set<Value> getNonCondVariables(){
		return variables.keySet();
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Branch " + name + " [");
		sb.append(this.getTotalUnits() + " & ");
		sb.append(this.getCondVariableUses() + " & ");
		sb.append(this.getNbNonCondVariables() + " & ");
		sb.append(this.assignments+" & ");
		sb.append(this.getNonCondVariableUses() + this.getCondVariableUses() + " & ");
		sb.append(this.getCondVariableUses()/((this.getTotalUnits() == 0)? 1 : this.getTotalUnits())+ " & ");
		sb.append(this.getCondVariableUses()/((this.getTotalUse()==0)? 1 : this.getTotalUse())+" \\ ]\n");

		return sb.toString();
	}

}