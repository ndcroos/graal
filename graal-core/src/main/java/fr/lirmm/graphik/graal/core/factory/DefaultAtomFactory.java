/*
 * Copyright (C) Inria Sophia Antipolis - Méditerranée / LIRMM
 * (Université de Montpellier & CNRS) (2014 - 2017)
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
package fr.lirmm.graphik.graal.core.factory;

import java.util.List;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.api.factory.AtomFactory;
import fr.lirmm.graphik.graal.core.DefaultAtom;
import fr.lirmm.graphik.graal.core.term.DefaultTermFactory;

/**
 * @author Clément Sipieter (INRIA) {@literal <clement@6pi.fr>}
 *
 */
public final class DefaultAtomFactory implements AtomFactory {

	private static DefaultAtomFactory instance = new DefaultAtomFactory();
	
	/**
	 * This instance of Atom represents Bottom, it is always interpreted as
	 * false.
	 */
	private static final Atom BOTTOM = new DefaultAtom(Predicate.BOTTOM, DefaultTermFactory.instance().createVariable(
	        "X"));
	private static final Atom TOP = new DefaultAtom(Predicate.TOP, DefaultTermFactory.instance().createVariable("X"));

	private DefaultAtomFactory() {
		super();
	}

	public static DefaultAtomFactory instance() {
		return instance;
	}

	@Override
	public Atom createEquality(Term t1, Term t2) {
		return this.create(Predicate.EQUALITY, t1, t2);
	}

	@Override
    public Atom create(Predicate predicate) {
		return new DefaultAtom(predicate);
	}

	@Override
    public Atom create(Predicate predicate, List<Term> terms) {
		return new DefaultAtom(predicate, terms);
	}

	@Override
    public Atom create(Predicate predicate, Term... terms) {
		return new DefaultAtom(predicate, terms);
	}

	@Override
    public Atom create(Atom atom) {
		return new DefaultAtom(atom);
	}

	@Override
    public Atom getTop() {
		return TOP;
	}

	@Override
    public Atom getBottom() {
		return BOTTOM;
	}

}