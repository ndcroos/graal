/*
 * Copyright (C) Inria Sophia Antipolis - Méditerranée / LIRMM
 * (Université de Montpellier & CNRS) (2014 - 2016)
 *
 * Contributors :
 *
 * Clément SIPIETER <clement.sipieter@inria.fr>
 * Mélanie KÖNIG
 * Swan ROCHER
 * Jean-François BAGET
 * Michel LECLÈRE
 * Marie-Laure MUGNIER <mugnier@lirmm.fr>
 *
 *
 * This file is part of Graal <https://graphik-team.github.io/graal/>.
 *
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */
/**
* 
*/
package fr.lirmm.graphik.graal.kb;

import java.util.Collections;
import java.util.List;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.AtomSet;
import fr.lirmm.graphik.graal.api.core.AtomSetException;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.api.core.Query;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.api.core.RulesCompilation;
import fr.lirmm.graphik.graal.api.core.Substitution;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.api.core.UnionOfConjunctiveQueries;
import fr.lirmm.graphik.graal.api.forward_chaining.Chase;
import fr.lirmm.graphik.graal.api.forward_chaining.ChaseException;
import fr.lirmm.graphik.graal.api.homomorphism.Homomorphism;
import fr.lirmm.graphik.graal.api.homomorphism.HomomorphismException;
import fr.lirmm.graphik.graal.api.homomorphism.HomomorphismWithCompilation;
import fr.lirmm.graphik.graal.api.io.Parser;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBase;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBaseException;
import fr.lirmm.graphik.graal.backward_chaining.pure.PureRewriter;
import fr.lirmm.graphik.graal.core.DefaultUnionOfConjunctiveQueries;
import fr.lirmm.graphik.graal.core.atomset.graph.DefaultInMemoryGraphAtomSet;
import fr.lirmm.graphik.graal.core.compilation.IDCompilation;
import fr.lirmm.graphik.graal.core.factory.DefaultAtomFactory;
import fr.lirmm.graphik.graal.core.factory.DefaultConjunctiveQueryFactory;
import fr.lirmm.graphik.graal.core.ruleset.LinkedListRuleSet;
import fr.lirmm.graphik.graal.core.term.DefaultTermFactory;
import fr.lirmm.graphik.graal.forward_chaining.ChaseWithGRD;
import fr.lirmm.graphik.graal.forward_chaining.ConfigurableChase;
import fr.lirmm.graphik.graal.grd.GraphOfRuleDependencies;
import fr.lirmm.graphik.graal.homomorphism.DefaultHomomorphismFactory;
import fr.lirmm.graphik.graal.homomorphism.StaticHomomorphism;
import fr.lirmm.graphik.graal.rulesetanalyser.Analyser;
import fr.lirmm.graphik.graal.rulesetanalyser.util.AnalyserRuleSet;
import fr.lirmm.graphik.util.AbstractProfilable;
import fr.lirmm.graphik.util.MethodNotImplementedError;
import fr.lirmm.graphik.util.stream.CloseableIterator;
import fr.lirmm.graphik.util.stream.CloseableIteratorWithoutException;
import fr.lirmm.graphik.util.stream.IteratorException;

/**
 * @author Clément Sipieter (INRIA) {@literal <clement@6pi.fr>}
 * 
 */
public class DefaultKnowledgeBase extends AbstractProfilable implements KnowledgeBase {

	private static final ConjunctiveQuery BOTTOM_QUERY = DefaultConjunctiveQueryFactory.instance().create(
	    DefaultAtomFactory.instance().create(Predicate.BOTTOM,
	        Collections.<Term> singletonList(DefaultTermFactory.instance().createVariable("X"))));

	private final RuleSet ruleset;
	private final AtomSet store;

	private RulesCompilation ruleCompilation;
	private AnalyserRuleSet analysedRuleSet;
	private Analyser analyse;

	private boolean isSaturated = false;
	private boolean isSemiSaturated = false;
	private boolean isCompiled = false;
	private boolean isAnalysed = false;
	private boolean isFESSaturated = false;
	private RuleSet fesRuleSet;
	private RuleSet fusRuleSet;
	private GraphOfRuleDependencies fesGRD;

	private Priority priority = Priority.FUS;

	// /////////////////////////////////////////////////////////////////////////
	// CONSTRUCTOR
	// /////////////////////////////////////////////////////////////////////////

	public DefaultKnowledgeBase(Parser<Object> parser) throws AtomSetException {
		this.ruleset = new LinkedListRuleSet();
		this.store = new DefaultInMemoryGraphAtomSet();
		this.load(parser);
		init();
	}

	public DefaultKnowledgeBase(AtomSet facts, Parser<Object> parser) throws AtomSetException {
		this.ruleset = new LinkedListRuleSet();
		this.store = facts;
		this.load(parser);
		init();
	}

	public DefaultKnowledgeBase(AtomSet facts, RuleSet ontology) {
		this.ruleset = ontology;
		this.store = facts;
		init();
	}

	private final void init() {
		// this.grd = new GraphOfRuleDependencies(this.ruleset);
	}

	// /////////////////////////////////////////////////////////////////////////
	// GETTERS/SETTERS
	// /////////////////////////////////////////////////////////////////////////

	/**
	 * @return the ruleset
	 */
	@Override
	public RuleSet getOntology() {
		return ruleset;
	}

	/**
	 * @return the atomset
	 */
	@Override
	public AtomSet getFacts() {
		return store;
	}

	@Override
	public boolean isConsistent() throws KnowledgeBaseException {
		try {
			return !this.query(BOTTOM_QUERY).hasNext();
		} catch (IteratorException e) {
			throw new KnowledgeBaseException(e);
		}
	}

	@Override
	public void saturate() throws KnowledgeBaseException {
		if (!this.isSaturated) {
			this.analyse();
			if (this.analyse.isFES()) {
				GraphOfRuleDependencies grd = this.analysedRuleSet.getGraphOfRuleDependencies();
				ChaseWithGRD chase = new ChaseWithGRD(grd, this.store);
				chase.setProfiler(this.getProfiler());
				try {
					chase.execute();
				} catch (ChaseException e) {
					throw new KnowledgeBaseException(e);
				}

				this.isSaturated = true;
				this.isFESSaturated = true;
				this.isSemiSaturated = true;
			} else {
				throw new KnowledgeBaseException("There is no proof for FES decidability");
			}
		}
	}

	@Override
	public void semiSaturate() throws KnowledgeBaseException {
		if (!this.isSemiSaturated) {
			this.compileRule();
			Chase chase = new ConfigurableChase(this.ruleCompilation.getSaturation(), this.store);
			chase.setProfiler(this.getProfiler());
			try {
				chase.next();
			} catch (ChaseException e) {
				throw new KnowledgeBaseException(e);
			}

			this.isSemiSaturated = true;
		}
	}

	public void fesSaturate() throws ChaseException {
		if (!isFESSaturated) {
			GraphOfRuleDependencies grd = this.getFESGraphOfRuleDependencies();
			ChaseWithGRD chase = new ChaseWithGRD(grd, this.store);
			chase.setProfiler(this.getProfiler());
			chase.execute();
			this.isFESSaturated = true;
		}
	}

	@Override
	public CloseableIterator<Substitution> homomorphism(Query query) throws KnowledgeBaseException {
		try {
			return StaticHomomorphism.instance().execute(query, this.store);
		} catch (HomomorphismException e) {
			throw new KnowledgeBaseException(e);
		}
	}

	@Override
	public CloseableIterator<Substitution> query(Query query) {
		try {
			if (query instanceof ConjunctiveQuery) {
				ConjunctiveQuery cq = (ConjunctiveQuery) query;

				this.analyse();
				if (this.analyse.isDecidable()) {
					this.fesSaturate();
					this.compileRule();
					RuleSet fusRuleSet = this.getFUSRuleSet();

					PureRewriter pure = new PureRewriter(false);
					CloseableIteratorWithoutException<ConjunctiveQuery> it = pure.execute(cq, fusRuleSet,
					    this.ruleCompilation);
					UnionOfConjunctiveQueries ucq = new DefaultUnionOfConjunctiveQueries(cq.getAnswerVariables(), it);

					Homomorphism solver = DefaultHomomorphismFactory.instance().getSolver(ucq, this.store);
					if (solver instanceof HomomorphismWithCompilation) {
						return ((HomomorphismWithCompilation) solver).execute(ucq, this.store, this.ruleCompilation);
					} else {
						it = PureRewriter.unfold(ucq, this.ruleCompilation);
						ucq = new DefaultUnionOfConjunctiveQueries(cq.getAnswerVariables(), it);
						return solver.execute(ucq, this.store);
					}

				} else {
					throw new KnowledgeBaseException("No decidable combinaison found.");
				}
			} else {
				return StaticHomomorphism.instance().execute(query, this.store);
			}
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	// /////////////////////////////////////////////////////////////////////////
	// PACKAGE METHODS
	// /////////////////////////////////////////////////////////////////////////

	void setPriority(Priority p) {
		this.priority = p;
	}

	// /////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	// /////////////////////////////////////////////////////////////////////////

	private RuleSet getFESRuleSet() {
		if (this.fesRuleSet == null) {
			this.analyse();
			fesRuleSet = new LinkedListRuleSet();
			int[] combine = getDecidableCombination();
			List<AnalyserRuleSet> scc = this.analysedRuleSet.getSCC();
			for (int i = 0; i < combine.length; ++i) {
				if ((combine[i] & Analyser.COMBINE_FES) != 0) {
					fesRuleSet.addAll(scc.get(i).iterator());
				}
			}
		}
		return this.fesRuleSet;
	}

	private RuleSet getFUSRuleSet() {
		if (this.fusRuleSet == null) {
			this.analyse();
			this.fusRuleSet = new LinkedListRuleSet();
			int[] combine = getDecidableCombination();
			List<AnalyserRuleSet> scc = this.analysedRuleSet.getSCC();
			for (int i = 0; i < combine.length; ++i) {
				if ((combine[i] & Analyser.COMBINE_FUS) != 0) {
					this.fusRuleSet.addAll(scc.get(i).iterator());
				}
			}
		}
		return this.fusRuleSet;
	}

	private GraphOfRuleDependencies getFESGraphOfRuleDependencies() {
		if (this.fesGRD == null) {
			RuleSet fesRuleSet = this.getFESRuleSet();
			this.fesGRD = new GraphOfRuleDependencies(fesRuleSet);
		}
		return this.fesGRD;
	}

	private int[] getDecidableCombination() {
		if (priority == Priority.FES) {
			return this.analyse.combineFES();
		} else {
			return this.analyse.combineFUS();
		}
	}

	private void analyse() {
		if (!this.isAnalysed) {
			this.analysedRuleSet = new AnalyserRuleSet(this.ruleset);
			this.analyse = new Analyser(analysedRuleSet);
			this.isAnalysed = true;
		}
	}

	private void compileRule() {
		if (!this.isCompiled) {
			this.ruleCompilation = new IDCompilation();
			this.ruleCompilation.compile(this.getFUSRuleSet().iterator());
			this.isCompiled = true;
		}
	}

	private void load(Parser<Object> parser) throws AtomSetException {
		Object o;
		try {
			while (parser.hasNext()) {
				o = parser.next();
				if (o instanceof Rule) {
					this.getOntology().add((Rule) o);
				} else if (o instanceof Atom) {
					this.getFacts().add((Atom) o);
				}
			}
		} catch (IteratorException e) {
			throw new AtomSetException(e);
		}
	}

	@Override
	public void saturate(Chase chase) {
		// TODO implement this method
		throw new MethodNotImplementedError();
	}

};
