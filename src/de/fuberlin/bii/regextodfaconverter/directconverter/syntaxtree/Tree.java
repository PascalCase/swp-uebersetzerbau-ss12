/*
 * 
 * Copyright 2012 lexergen.
 * This file is part of lexergen.
 * 
 * lexergen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * lexergen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with lexergen.  If not, see <http://www.gnu.org/licenses/>.
 *  
 * lexergen:
 * A tool to chunk source code into tokens for further processing in a compiler chain.
 * 
 * Projectgroup: bi, bii
 * 
 * Authors: Johannes Dahlke
 * 
 * Module:  Softwareprojekt Übersetzerbau 2012 
 * 
 * Created: Apr. 2012 
 * Version: 1.0
 *
 */

package de.fuberlin.bii.regextodfaconverter.directconverter.syntaxtree;

import java.util.Collection;
import java.util.HashSet;

import de.fuberlin.bii.regextodfaconverter.directconverter.lrparser.grammar.Grammar;
import de.fuberlin.bii.regextodfaconverter.directconverter.syntaxtree.node.Leaf;
import de.fuberlin.bii.regextodfaconverter.directconverter.syntaxtree.node.TreeNode;
import de.fuberlin.bii.utils.Test;


/**
 * Schnittstelle für den Zugriff auf einen Baumes.
 * 
 * @author Johannes Dahlke
 *
 */
@SuppressWarnings("rawtypes")
public interface Tree extends Iterable<TreeNode> {

	/**
	 * Liefert den Wurzelknoten eines Baumes.
	 * @return
	 */
	TreeNode getRoot();
	
	/**
	 * Liefert die dem Baum zugrunde liegende Grammatik.
	 * @return
	 */
	Grammar getGrammar();
	
	/**
	 * Liefert die Menge aller Blätter.
	 * @return
	 */
	Collection<Leaf> getLeafSet(); 
	
}
