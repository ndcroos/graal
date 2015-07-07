/* Graal v0.7.4
 * Copyright (c) 2014-2015 Inria Sophia Antipolis - Méditerranée / LIRMM (Université de Montpellier & CNRS)
 * All rights reserved.
 * This file is part of Graal <https://graphik-team.github.io/graal/>.
 *
 * Author(s): Clément SIPIETER
 *            Mélanie KÖNIG
 *            Swan ROCHER
 *            Jean-François BAGET
 *            Michel LECLÈRE
 *            Marie-Laure MUGNIER
 */
 package fr.lirmm.graphik.graal.rulesetanalyser.property;

import java.util.Set;

import fr.lirmm.graphik.graal.core.Atom;
import fr.lirmm.graphik.graal.core.Rule;
import fr.lirmm.graphik.graal.core.term.Term;

/**
 * At least one atom in the body (called a frontier-guard) contains all the
 * variables from the frontier.
 * 
 * @author Clément Sipieter (INRIA) {@literal <clement@6pi.fr>}
 * @author Swan Rocher
 * 
 */
public final class FrontierGuardedProperty extends AbstractRuleProperty {

	private static FrontierGuardedProperty instance = null;
	
	private FrontierGuardedProperty(){}
	
	public static synchronized FrontierGuardedProperty getInstance() {
		if(instance == null) {
			instance = new FrontierGuardedProperty();
		}
		return instance;	
	}
	
	@Override
	public Boolean check(Rule rule) {
		Set<Term> frontier = rule.getFrontier();
		boolean isGuarded = true;

		for (Atom a : rule.getBody()) {
			isGuarded = true;
			for (Term v : frontier) {
				if (!a.getTerms().contains(v)) {
					isGuarded = false;
					break;
				}
			}
			if (isGuarded) {
				break;
			}
		}

		return isGuarded;
	}

	@Override
	public String getLabel() {
		return "fg";
	}

}