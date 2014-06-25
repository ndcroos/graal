package fr.lirmm.graphik.graal.incubator;

import java.util.LinkedList;
import java.util.List;

import fr.lirmm.graphik.graal.core.Rule;
import fr.lirmm.graphik.graal.core.atomset.AtomSet;
import fr.lirmm.graphik.graal.solver.SolverException;
import fr.lirmm.graphik.graal.solver.SolverFactoryException;

public class DataGenerator {
	
	private Iterable<Rule> rules;
	
	public DataGenerator(Iterable<Rule> rules) {
		this.rules = rules;
	}
	
	public void run() throws SolverFactoryException, SolverException {
		List<AtomSet> facts = new LinkedList<AtomSet>();
		/*Graph g = new DefaultDirectedGraph<AtomSet, Substitution>(Substitution.class);
		for(Rule r : rules) {
			
			for(AtomSet f : facts) {
				for(Atom a : r.getBody()) {
					LinkedListAtomSet set = new LinkedListAtomSet();
					set.add(a);
					ConjunctiveQuery q = new ConjunctiveQuery(set);
					for(Substitution s : Util.execute(q, f)) {
						System.out.println("##########################");
						System.out.println(q);
						System.out.println(f);
						System.out.println(s);
					}
				}
			}
			facts.add(r.getBody());

		}*/
	}
}