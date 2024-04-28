
package kappaMerge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import edu.cs.ai.alchourron.logic.Formula;
import edu.cs.ai.alchourron.logic.logics.propositional.PropositionalSignature;
import edu.cs.ai.alchourron.logic.semantics.interpretations.PropositionalInterpretation;
import edu.cs.ai.alchourron.logic.semantics.interpretations.RankingFunction;
import edu.cs.ai.alchourron.logic.syntax.formula.*;

/**
 * Represents an unique task in the experimental data consisting of premises,
 * answer options and the logically consistent answer. Stores results of the
 * evaluation concerning a task such as final ranking functions, predictions an
 * the predictive performance of the operators.
 *
 */
public class UniqueTask {

    public String name;
    public List<Formula<PropositionalSignature<Character>>> premises;
    public List<Formula<PropositionalSignature<Character>>> answerOptions;

    // Collection of the final ranking functions that the operators yield for this
    // task.
    public Map<DataOperator, RankingFunction<PropositionalInterpretation<Character>>> finalRankingFunctions;
    public Map<Formula<PropositionalSignature<Character>>, LinkedList<DataOperator>> predictions = new HashMap<>();
    public Map<DataOperator, Integer> operatorPerformance = new HashMap<>();
    public TaskGroup taskType;
    public Formula<PropositionalSignature<Character>> logicallyConsistentAnswer;
    int counter = 0;
    int frequency;
    int errorCounter = 0;

    public Map<Formula<PropositionalSignature<Character>>, Integer> wrongAnswerFrequencies = new HashMap<>();

    public int getFrequency() {
	return frequency;
    }

    public void setFrequency(int frequency) {
	this.counter = frequency;
    }

    public TaskGroup getTaskType() {
	return taskType;
    }

    public void resetCounter() {
	counter = 0;
    }

    public UniqueTask(List<Formula<PropositionalSignature<Character>>> premises,
	    List<Formula<PropositionalSignature<Character>>> choices) {

	this.premises = premises;
	this.answerOptions = choices;
	taskType = determineTaskGroup();
	logicallyConsistentAnswer = determineLogicallyConsistentAnswer();
	;
    }

    // Determines the task group for this task. Currently four groups are supported.
    // Most of the negated or compound possibilities are assigned the group 'other'.
    private TaskGroup determineTaskGroup() {

	TaskGroup result = null;
	boolean negatedConjunction = false;
	for (Formula<PropositionalSignature<Character>> premise : premises) {

	    // No task group is determined yet

	    if (result == null) {

		// The minor premise does not determine the task group.
		if (premise instanceof FormulaPropositionalAtom) {
		    continue;
		}

		// Assign task group based on major premise.
		if (premise instanceof FormulaBiImplication) {
		    result = TaskGroup.Biconditional;
		    continue;
		}

		if (premise instanceof FormulaImplication) {
		    result = TaskGroup.Conditional;
		    continue;
		}

		if (premise instanceof FormulaOR) {
		    result = TaskGroup.InclusiveDisjunction;
		    continue;
		}

		if (premise instanceof FormulaNeg) {
		    List<Formula<PropositionalSignature<Character>>> operands = ((FormulaNeg<PropositionalSignature<Character>>) premise)
			    .getOperands();

		    if (operands.size() < 2) {
			// The minor premise does not determine the task group.
			if (operands.get(0) instanceof FormulaPropositionalAtom) {
			    continue;
			}
			// Negated formulas are only supported to a limited extend, assign group
			// 'other'.
			result = TaskGroup.Other;
			// Negated conjunction can be part of a exclusive disjunction, set flag.
			if (operands.get(0) instanceof FormulaAND) {
			    negatedConjunction = true;
			}
		    }
		    // All negated compound formulas are only supported to a limited extend, assign
		    // group 'other'.
		    else {
			result = TaskGroup.Other;
		    }
		    // All other non-negated compound cases are assigned group 'other'.
		} else
		    result = TaskGroup.Other;

		// Result =/= null , i.e., a task group was already assigned due to a prior
		// premise.
	    } else {

		// The minor premise does not determine the task group.
		if (premise instanceof FormulaPropositionalAtom)
		    continue;

		if (result == TaskGroup.InclusiveDisjunction) {

		    if (premise instanceof FormulaNeg) {
			List<Formula<PropositionalSignature<Character>>> operands = ((FormulaNeg<PropositionalSignature<Character>>) premise)
				.getOperands();
			if (operands.size() < 2) {
			    // The minor premise does not determine the task group.
			    if (operands.get(0) instanceof FormulaPropositionalAtom) {
				continue;
			    }
			    // Check if the task is an exclusive disjunction
			    // Since there can be only two variables, it is not necessary to check if the
			    // variables in the disjunction and conjunction are the same
			    if (operands.get(0) instanceof FormulaAND) {
				result = TaskGroup.ExclusiveDisjunction;
			    }
			}
		    }
		    // This premise is neither an atom, nor a negated atom nor a negated
		    // conjunction.
		    else {
			result = TaskGroup.Other;
		    }

		}
		// The order of the negated conjunction and the disjunction can be switched.
		else if ((result == TaskGroup.Other) && (negatedConjunction)) {
		    if (premise instanceof FormulaOR) {
			result = TaskGroup.ExclusiveDisjunction;
		    }
		}
		// Otherwise do not change the group.
	    }

	}

	// Only categorical premises in the task.
	if (result == null) {
	    result = TaskGroup.Other;
	}

	return result;

    }

    // Computes the correct answer for this task.
    private Formula<PropositionalSignature<Character>> determineLogicallyConsistentAnswer() {

	Formula<PropositionalSignature<Character>> result = null;
	Set<PropositionalInterpretation<Character>> premiseModels = new HashSet<>();
	premiseModels.addAll(Main.getLogic().modelsOf(premises.get(0), Main.getSignature()));

	for (int i = 1; i < premises.size(); i++)
	    premiseModels.retainAll(Main.getLogic().modelsOf(premises.get(i), Main.getSignature()));

	for (Formula<PropositionalSignature<Character>> answerOption : answerOptions) {
	    if (Main.getLogic().modelsOf(answerOption, Main.getSignature()).containsAll(premiseModels)) {
		result = answerOption;
		break;
	    }
	}
	return result;

    }

    @Override
    public String toString() {

	String taskText = "| ";

	for (int i = 1; i <= premises.size(); i++) {
	    taskText = taskText.concat("Premise " + i + ": " + premises.get(i - 1) + " | ");

	}
	return taskText;
    }

    @Override
    public int hashCode() {
	return Objects.hash(answerOptions, premises);
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	UniqueTask other = (UniqueTask) obj;
	return Objects.equals(answerOptions, other.answerOptions) && Objects.equals(premises, other.premises);
    }

}
