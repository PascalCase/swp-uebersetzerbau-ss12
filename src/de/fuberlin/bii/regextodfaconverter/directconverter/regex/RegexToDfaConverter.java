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

package de.fuberlin.bii.regextodfaconverter.directconverter.regex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import de.fuberlin.bii.regextodfaconverter.Regex;
import de.fuberlin.bii.regextodfaconverter.RegexInvalidException;
import de.fuberlin.bii.regextodfaconverter.directconverter.DirectConverterException;
import de.fuberlin.bii.regextodfaconverter.directconverter.PositionToPayloadMap;
import de.fuberlin.bii.regextodfaconverter.directconverter.lrparser.grammar.Terminal;
import de.fuberlin.bii.regextodfaconverter.directconverter.regex.operatortree.RegexOperatorTree;
import de.fuberlin.bii.regextodfaconverter.directconverter.regex.operatortree.RegularExpressionElement;
import de.fuberlin.bii.regextodfaconverter.directconverter.regex.operatortree.TerminalNode;
import de.fuberlin.bii.regextodfaconverter.directconverter.syntaxtree.node.Leaf;
import de.fuberlin.bii.regextodfaconverter.directconverter.syntaxtree.node.TreeNode;
import de.fuberlin.bii.regextodfaconverter.directconverter.syntaxtree.node.TreeNodeCollection;
import de.fuberlin.bii.regextodfaconverter.directconverter.syntaxtree.node.TreeNodeSet;
import de.fuberlin.bii.regextodfaconverter.fsm.FiniteStateMachine;
import de.fuberlin.bii.regextodfaconverter.fsm.State;
import de.fuberlin.bii.regextodfaconverter.fsm.StatePayload;
import de.fuberlin.bii.regextodfaconverter.fsm.Transition;
import de.fuberlin.bii.utils.Notification;
import de.fuberlin.bii.utils.Test;


/**
 * Stellt Funktionalitäten bereit, um einen vereinfachten regulären Ausdruck in
 * eine DFA umzuwandeln.
 * 
 * @author Johannes Dahlke
 * 
 * @see <a
 *      href="http://kontext.fraunhofer.de/haenelt/kurs/folien/Haenelt_FSA_RegExFSA.pdf">Fraunhofer
 *      Institut: Überführung regulärer Ausdrücke in endliche Automaten</a>
 * @see <a
 *      href="http://kontext.fraunhofer.de/haenelt/kurs/folien/Haenelt_RegEx-FSA-GMY.pdf">Fraunhofer
 *      Institut: Der Algorithmus von Glushkov und McNaughton/Yamada</a>
 * @see <a
 *      href="http://kontext.fraunhofer.de/haenelt/kurs/folien/FSA-RegA-6.pdf">Endliche
 *      Automaten: Reguläre Mengen, Reguläre Ausdrücke, reguläre Sprachen und
 *      endliche Automaten</a>
 * @see <a
 *      href="http://kontext.fraunhofer.de/haenelt/kurs/Skripten/FSA-Skript/Haenelt_EA_RegEx2EA.pdf">Überführung
 *      regulärer Ausdrücke in endliche Automaten</a>
 */
public class RegexToDfaConverter {

	

	
	
	/**
	 * Wandelt einen vereinfachten regulären Ausdruck in einen DFA um.
	 * 
	 * @param Regex
	 *          der reguläre Ausdruck in vereinfachter Form.
	 * @param <StatePayloadType>
	 *          der Inhalt, welcher Zuständen zugeordnet sein kann.
	 * @return ein DFA
	 * @throws Exception
	 * 
	 */
	public static FiniteStateMachine<Character, ? extends de.fuberlin.bii.tokenmatcher.StatePayload> convert( String regex, StatePayload commonPayload)
			throws DirectConverterException {
		PositionToPayloadMap<StatePayload> positionToPayloadMap = new PositionToPayloadMap<StatePayload>();
		return convert( regex, positionToPayloadMap, commonPayload);
	}
	
	public static FiniteStateMachine<Character, ? extends de.fuberlin.bii.tokenmatcher.StatePayload> convert( RegexToPayloadMap<StatePayload> regexToPayloadMap)
			throws DirectConverterException {
				String concatenatedRegex = "";
		PositionToPayloadMap<StatePayload> positionToPayloadMap = new PositionToPayloadMap<StatePayload>();
		
		for ( String regex : regexToPayloadMap.keySet()) {
			if ( !concatenatedRegex.isEmpty())
				concatenatedRegex += "|";
		  concatenatedRegex += "(" + regex +")";
		  positionToPayloadMap.put( concatenatedRegex.length() -1, regexToPayloadMap.get( regex));  
   	}
		System.out.println( concatenatedRegex);
		return convert( concatenatedRegex, positionToPayloadMap);
	}
	
	public static FiniteStateMachine<Character, ? extends de.fuberlin.bii.tokenmatcher.StatePayload> convert( String regex, PositionToPayloadMap<StatePayload> positionToPayloadMap)
			throws DirectConverterException {
		return convert( regex, positionToPayloadMap, null);
	}
	
	public static FiniteStateMachine<Character, ? extends de.fuberlin.bii.tokenmatcher.StatePayload> convert( String regex, PositionToPayloadMap<StatePayload> positionToPayloadMap,  StatePayload commonPayload)
			throws DirectConverterException {
		
		int regexLength = regex.length();
		
		RegularExpressionElement[] regularExpression = new RegularExpressionElement[regexLength];
		for ( int i = 0; i < regexLength; i++) {
			regularExpression[i] = new RegularExpressionElement( regex.charAt( i), positionToPayloadMap.get( i));
		}
		
		return convert( regularExpression, commonPayload);
	}
	
	
	public static FiniteStateMachine<Character, ? extends de.fuberlin.bii.tokenmatcher.StatePayload> convert( RegularExpressionElement<StatePayload>[] regularExpression, StatePayload commonPayload)
			throws DirectConverterException {
		try {
			RegexOperatorTree regexTree = convertRegexToTree( regularExpression);
			FiniteStateMachine<Character, StatePayload> dfa = convertRegexTreeToDfa( regexTree, commonPayload);
		  return dfa;
		} catch ( Exception e) {
			Notification.printDebugException( e);
			String regexExpression = "";
			for ( RegularExpressionElement<StatePayload> regularExpressionElement : regularExpression) {
				regexExpression += regularExpressionElement.getValue();
			}
			throw new DirectConverterException( String.format( "Cannot convert regex '%s' to DFA.", regularExpression));
		}
	}
	
	

	/**
	 * 
	 * @param Regex
	 * @return
	 * @throws Exception 
	 */
	private static RegexOperatorTree convertRegexToTree( RegularExpressionElement[] regularExpression) throws Exception {
		RegexOperatorTree regexTree = new RegexOperatorTree( regularExpression);
		return regexTree;
	}


	private static StatePayload getBestPayloadFromTreeNodeCollectionForCharacter( TreeNodeCollection collection, Character theCharacter) {
		StatePayload result = null;
		for ( TreeNode node : collection) {
			if ( node instanceof TerminalNode) {
				RegularExpressionElement<StatePayload> nodeValue = (RegularExpressionElement<StatePayload>)((TerminalNode)node).getValue();
				if ( nodeValue.getValue().equals( theCharacter)) { 
					StatePayload currentPayload = nodeValue.getPayload();
					if ( Test.isAssigned(  currentPayload)) {
						if (Test.isUnassigned(  result))
							result = currentPayload;
						else {
							if ( result.getPriority() < currentPayload.getPriority())
								result = currentPayload;
						}
					} 
				}
			}
		}
		return result;
	}
	
	
	
	private static boolean setPayloadPriorityDependentForTransitionFromStateToStateByCharacter( Map<State,Map<State, Map<Character,StatePayload>>> stateToStateMap, State fromState, State toState, Character theCharacter, StatePayload thePayloadToSet) {
	
	  
	  Map<State, Map<Character,StatePayload>> stateToCharacterPayloadMap = stateToStateMap.get( toState);
	  if ( Test.isUnassigned( stateToCharacterPayloadMap)) {
	  	stateToCharacterPayloadMap = new HashMap<State, Map<Character,StatePayload>>();
	  	stateToStateMap.put( toState, stateToCharacterPayloadMap);
	  }	
	  	
	  Map<Character,StatePayload> characterToPayloadMap = stateToCharacterPayloadMap.get( fromState);
	  if ( Test.isUnassigned( characterToPayloadMap)) { 
	  	characterToPayloadMap = new HashMap<Character,StatePayload>();
	  	stateToCharacterPayloadMap.put( fromState, characterToPayloadMap);
	  }

	  StatePayload storedPayload = characterToPayloadMap.get( theCharacter);
	  if ( Test.isAssigned( storedPayload)) {
	  	if ( storedPayload.getPriority() < thePayloadToSet.getPriority()) {
	  		characterToPayloadMap.put( theCharacter, thePayloadToSet);
	  		return true;
	  	}
	  } else {
	  	characterToPayloadMap.put( theCharacter, thePayloadToSet);
	  	return true;
	  }
  	return false;
	}

	private static StatePayload getPayloadForTransitionFromStateToStateByCharacter( Map<State,Map<State, Map<Character,StatePayload>>> stateToStateMap, State fromState, State toState, Character theCharacter) {

	  Map<State, Map<Character,StatePayload>> stateToCharacterPayloadMap = stateToStateMap.get( toState);
	  if ( Test.isUnassigned( stateToCharacterPayloadMap))
	  	return null;
	  	
	  Map<Character,StatePayload> characterToPayloadMap = stateToCharacterPayloadMap.get( fromState);
	  if ( Test.isUnassigned( characterToPayloadMap)) 
	  	return null;

	  StatePayload storedPayload = characterToPayloadMap.get( theCharacter);
	  if ( Test.isUnassigned( storedPayload))
	  	return null;
	  
	  return storedPayload;
	}
		
	
	/**
	 * Konvertiert einen annotierten Syntaxbaum in einen deterministischen
	 * endlichen Automaten
	 * 
	 * @param syntaxTree
	 * @return
	 * @throws DirectConverterException
	 * @throws Exception
	 */
	private static FiniteStateMachine<Character, StatePayload> convertRegexTreeToDfa( RegexOperatorTree<StatePayload> regexTree, StatePayload commonPayload) throws DirectConverterException {
		try {
			
			HashMap<TreeNodeCollection, State<Character, StatePayload>> unhandledStates = new HashMap<TreeNodeCollection, State<Character, StatePayload>>();

			HashMap<TreeNodeCollection, State<Character, StatePayload>> handledStates = new HashMap<TreeNodeCollection, State<Character, StatePayload>>();

			FiniteStateMachine<Character, StatePayload> dfa = new FiniteStateMachine<Character, StatePayload>();

			// add start state as unhandled
			unhandledStates.put( regexTree.getFirstPositions().get( regexTree.getRoot()), dfa.getInitialState());

			// maps the target states to a map of source states with corresponding payloads
			Map<State,Map<State, Map<Character,StatePayload>>> payloadToStateMap = new HashMap<State,Map<State, Map<Character,StatePayload>>>();
			
			StatePayload currentStatePayload = null;

			Set<RegularExpressionElement<StatePayload>> alphabetSubset = new HashSet<RegularExpressionElement<StatePayload>>();
			for ( Leaf leaf : regexTree.getLeafSet()) {
				alphabetSubset.add( (RegularExpressionElement<StatePayload> ) leaf.getValue());
			} 
			
			State<Character, StatePayload> currentState;
			TreeNodeCollection currentCollection;
			while ( !unhandledStates.isEmpty()) {
				// get the next unhandled state ...
				currentCollection = unhandledStates.keySet().iterator().next();
				currentState = unhandledStates.remove( currentCollection);
				dfa.setCurrentState( currentState);
				// ... and mark it as handled
				handledStates.put( currentCollection, currentState);

				HashMap<Character, TreeNodeCollection> stateCandidates = new HashMap<Character, TreeNodeCollection>();
				Character currentTerminalCharacter;
				
				for ( RegularExpressionElement<StatePayload> currentRegexElement : alphabetSubset) {
					TreeNodeCollection followPositionsOfTerminal = new TreeNodeSet();
					for ( TreeNode node : currentCollection) {
						if ( node instanceof TerminalNode) {
							RegularExpressionElement terminalNodeRegexElement = (RegularExpressionElement)((TerminalNode)node).getValue();
							//	System.out.println( terminalNodeRegexElement + " <> " + currentRegexElement);
							if ( terminalNodeRegexElement.equals( currentRegexElement)) { // use equals() instead of equalsTotally()
								followPositionsOfTerminal.addAll( regexTree.getFollowPositions().get( node));
							}
						}
					}


					// if set not empty, then add set to states
					State<Character, StatePayload> targetState = null;
				
					if ( !followPositionsOfTerminal.isEmpty()) {
						
						// setze Übergang-spezifischen Payload 
						currentStatePayload = getBestPayloadFromTreeNodeCollectionForCharacter( currentCollection, currentRegexElement.getValue()); 
						// Oder falls keiner definiert, dann den allgemeinen Payload, sofern es sich um das Ende handelt
						if ( Test.isUnassigned( currentStatePayload)
								&& followPositionsOfTerminal.contains( regexTree.getTerminatorNode()))						
							currentStatePayload = commonPayload;
					
						// Ansonsten wie im Algorithmus von Glushkov / McNaughton and Yamada beschrieben verfahren 
						if ( !handledStates.containsKey( followPositionsOfTerminal) && !unhandledStates.containsKey( followPositionsOfTerminal)) {
							targetState = new State<Character, StatePayload>();
							unhandledStates.put( followPositionsOfTerminal, targetState);
						} else if ( handledStates.containsKey( followPositionsOfTerminal)) {
							targetState = handledStates.get( followPositionsOfTerminal);
						} else {
							targetState = unhandledStates.get( followPositionsOfTerminal);
						}

						// setze Übergang
						dfa.addTransition( targetState, currentRegexElement.getValue());

						// falls ein Payload gegeben, setze finite
						if ( Test.isAssigned( currentStatePayload) &&
							   followPositionsOfTerminal.contains( regexTree.getTerminatorNode())) {
							
							//System.out.println("%%: " + currentStatePayload  +  " for   "  + targetState.getUUID()  + "  from  " + currentState.getUUID());
							if ( targetState.isFiniteState()) {
								// es ist bereits ein payload gesetzt.
								// überschreibe je nach Priorität
								if ( Test.isAssigned( currentStatePayload)) {
									if ( Test.isUnassigned( targetState.getPayload())
											|| ( Test.isAssigned( targetState.getPayload())
													&& targetState.getPayload().getPriority() < currentStatePayload.getPriority()))
										targetState.setPayload( currentStatePayload); 

									
									setPayloadPriorityDependentForTransitionFromStateToStateByCharacter( payloadToStateMap, currentState, targetState, currentRegexElement.getValue(), currentStatePayload);
								}
							} else {
								// setze Folgezustand finite
								targetState.setFinite( true);
								// and set payload
								targetState.setPayload( currentStatePayload);
								// speichere Datum für die Nachbereitung
								setPayloadPriorityDependentForTransitionFromStateToStateByCharacter(payloadToStateMap, currentState, targetState, currentRegexElement.getValue(), currentStatePayload);
							}
						
							
						} else if (followPositionsOfTerminal.contains( regexTree.getTerminatorNode())) {
							// falls ein Endzustand nach dem Terminalzeichen # ist, dann setzte Zustand in jedem Fall final
							targetState.setFinite( true);
						}
						
					}

				}

			}
			
		
			// +++ slightly Modification of algorithm of Glushkov / McNaughton and Yamada +++
			// posthumously untie the finate and terminating node by payloads
			Map<UUID, State<Character, StatePayload>> dfaStates = (Map<UUID, State<Character, StatePayload>>) dfa.getStates().clone();
			State<Character, StatePayload> currentDfaState;
			Set<UUID> knownFiniteStates = new HashSet<UUID>();
			for ( UUID stateId : dfaStates.keySet()) {
				// Untersuche alle Zustände auf Übergänge in den final Zustand
				currentDfaState = dfaStates.get( stateId);
				
				Set< Transition<Character, StatePayload>> transitionSet = currentDfaState.getTransitions();
				Set< Transition<Character, StatePayload>> transitionSetCopy = (Set<Transition<Character, StatePayload>>) currentDfaState.getTransitions().clone();
				for ( Transition<Character, StatePayload> transition : transitionSetCopy) {
					// wenn der Übergang in einen final Zustand führt
					if ( transition.getState().isFiniteState() 
							// und von dort keine weiteren Übergänge mehr ausgehen.
							&& transition.getState().getElementsOfOutgoingTransitions().isEmpty()) {
					  
						// get original payload
						State targetState = transition.getState();
						State sourceState = currentDfaState;
										
						StatePayload payload = getPayloadForTransitionFromStateToStateByCharacter( payloadToStateMap, sourceState, targetState, transition.getCondition());
						if ( Test.isAssigned( payload)) { 
							if ( !knownFiniteStates.contains( transition.getState().getUUID())) {
								// the first transition must not handled
								knownFiniteStates.add( transition.getState().getUUID());
								// but update the payload
								transition.getState().setPayload( payload);
							} else {
								Character terminal = transition.getCondition();
								// Biege den Übergang auf einen neuen Endzustand um.
								transitionSet.remove( transition);
								State<Character, StatePayload> newFinalState = new State<Character, StatePayload>( payload, true);
								dfa.setCurrentState( currentDfaState);
								dfa.addTransition( newFinalState, terminal);
							}
						}

					}
				} 
			}
			
			

			assert dfa.isDeterministic();

			return dfa;

		} catch ( Exception e) {
			Notification.printDebugException( e);
			throw new DirectConverterException( "Cannot convert syntax tree to DFA. " + e.getMessage());
		}

	}


	
}
