package kappaMerge;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import edu.cs.ai.alchourron.logic.Formula;
import edu.cs.ai.alchourron.logic.logics.propositional.PropositionalSignature;
import edu.cs.ai.alchourron.logic.semantics.interpretations.PropositionalInterpretation;
import edu.cs.ai.alchourron.logic.semantics.interpretations.RankingFunction;
import edu.cs.ai.math.settheory.Pair;

/**
 * This class provides the evaluation methods. 
 * 
 * 
 *
 */
public class Evaluation {

    private static List<DataRecord> records;
    private static List<UniqueTask> listOfTasks;
    private static List<DataOperator> operators;
    private static List<Participant> participants;
    
    // Stores operators that show the same behavior.
    private static Set<DataOperator> redundantOperators;
    static Comparator<DataOperator> opComparator = (op1, op2) -> {
	return op1.toString().compareTo(op2.toString());
    };
    static Comparator<Participant> participantComparator = (op1, op2) -> {
	return op1.toString().compareTo(op2.toString());
    };

    public static void analyze(List<DataRecord> listOfRecords, List<UniqueTask> listOfUniqueTasks,
	    List<DataOperator> operators, List<Participant> listOfParticipants) {

	Evaluation.records = listOfRecords;
	Evaluation.listOfTasks = listOfUniqueTasks;
	operators.sort(opComparator);
	Evaluation.operators = operators;
	listOfParticipants.sort(participantComparator);
	Evaluation.participants = listOfParticipants;

	printToc();
	printHeader1("I) Operator Predictions");

	computeOperatorPredictionsForUniqueTasks(operators);
	listOfTasks.stream().forEach(task -> {
	    printHeader2("Predicted answers and their frequency for task " + task);
	    task.predictions.keySet().forEach(prediction -> {

		System.out.println(
			prediction + ": " + task.predictions.get(prediction).size() + "  -- Predicting operators:");
		System.out.println("");
		List<DataOperator> tempList = task.predictions.get(prediction);
		tempList.sort(opComparator);
		System.out.println(tempList);
		System.out.println();
	    });
	});

	/*
	 * Aggregate operators that show the same behavior for the given tasks, i.e.
	 * yield the exact same ranking functions, the identical TPOs or the identical
	 * predictions for all tasks. Comment/uncomment the marked lines in order to
	 * subsequently remove redundant operators or to consider them all in an
	 * aggregation step.
	 * 
	 * The output of final ranking functions and tpo's is deactivated by default and
	 * can be activated by uncommenting the marked blocks below.
	 * 
	 * 
	 */

	printHeader1("II) Operator Groups");

	redundantOperators = new HashSet<>();
	Set<List<DataOperator>> operatorPredictionGroups = groupOperators();
	// Do NOT deactivate the removal of redundant operators in predictive groups.
	redundantOperators.stream().forEach(operator -> operators.remove(operator));
	printHeader2("The following " + operatorPredictionGroups.size()
		+ " groups of operators give the exact same predictions for all the given tasks. -> Exclude "
		+ redundantOperators.size() + " redundant operators from analysis.");
	for (List<DataOperator> groups : operatorPredictionGroups) {
	    groups.sort(opComparator);
	    System.out.println(groups);
	}

	printHeader2("Remaining " + operators.size()
		+ " operators representing operator groups with different predictive behavior for the given tasks:");
	for (DataOperator operator : operators) {
	    System.out.println(operator + ": ");
	    for (UniqueTask task : listOfTasks) {
		for (Formula<PropositionalSignature<Character>> key : task.predictions.keySet()) {
		    if (task.predictions.get(key).contains(operator)) {
			System.out.printf("%-80s %-15s %-6s%n", task, "Prediction: ", key);
		    }
		}
	    }
	}

	printHeader1("III) Operators: Predictive Performance");
	printHeader1("III-1) General Predictive Performance");

	Map<DataOperator, HashMap<DataRecord, Boolean>> predictivePerformances = computePredictivePerformances(records);
	for (DataOperator operator : operators) {

	    for (UniqueTask task : listOfTasks) {
		task.resetCounter();
		task.operatorPerformance.put(operator, 0);

	    }
	    HashMap<DataRecord, Boolean> predictivePerformanceOfOperator = predictivePerformances.get(operator);
	    computeAccuracy(operator, predictivePerformanceOfOperator);
	}

	printHeader1("III-2) Accuracy for Unique Tasks");

	computeAccuracyForUniqueTasks();

	printHeader1("III-3) Unpredicted Answers");

	System.out.println();
	List<Pair<String, String>> listOfUnpredictedAnswers = findUnpredictedAnswers();
	listOfUnpredictedAnswers.stream().sorted((op1, op2) -> op1.getFirst().compareTo(op2.getFirst()))
		.forEach(task -> {
		    System.out.println(task.getFirst() + " Response: " + task.getSecond());
		});

	System.out.println();

	printHeader1("III-4) Predictive Performance of the Operators for Individuals");

	for (Participant participant : participants) {
	    // Get records associated with the participant
	    for (DataRecord record : records) {
		if (record.participant.equals(participant.id)) {
		    participant.pRecords.add(record);

		    // Check if participant gave a correct answer in the task.
		    verifyResponse(participant, record);
		}
	    }

	    printHeader2("Accuracy of predictions for participant: " + participant);
	    Map<DataOperator, HashMap<DataRecord, Boolean>> predictivePerformancesForParticipant = new HashMap<>();

	    predictivePerformancesForParticipant = computePredictivePerformances(participant.pRecords);

	    for (DataOperator operator : operators) {

		for (UniqueTask task : listOfTasks) {
		    task.resetCounter();
		    task.operatorPerformance.put(operator, 0);

		}
		HashMap<DataRecord, Boolean> predictivePerformanceOfOperatorForParticipant = predictivePerformancesForParticipant
			.get(operator);
		computeAccuracy(operator, predictivePerformanceOfOperatorForParticipant);

	    }
	}

    }


    /**
     * Computes the final ranking functions for all unique tasks for the given
     * operators. Based on the belief set of the ranking function an answer option
     * is chosen as prediction. The answer option, whose set of models contains all
     * rank 0 worlds is the predicted answer.
     * 
     * @param operators List of data operators.
     */
    private static void computeOperatorPredictionsForUniqueTasks(List<DataOperator> operators) {
	for (UniqueTask task : listOfTasks) {
	    task.finalRankingFunctions = new HashMap<>();
	    task.predictions = new HashMap<>();

	    for (DataOperator operator : operators) {
		RankingFunction<PropositionalInterpretation<Character>> finalState = operator.compute(task.premises);
		task.finalRankingFunctions.put(operator, finalState);
		// Compute prediction from the belief set of the final ranking function.
		Formula<PropositionalSignature<Character>> prediction = null;
		for (Formula<PropositionalSignature<Character>> answerOption : task.answerOptions) {
		    if (Main.getLogic().modelsOf(answerOption, Main.getSignature())
			    .containsAll(finalState.getByRank(0))) {
			prediction = answerOption;
		    }
		}

		if (task.predictions.containsKey(prediction)) {
		    task.predictions.get(prediction).add(operator);
		} else {
		    LinkedList<DataOperator> newOpList = new LinkedList<>();
		    newOpList.add(operator);
		    task.predictions.put(prediction, newOpList);

		}
	    }

	}
    }

    /**
     * This function groups the operators according to the given criterion. Accepted
     * values are "Final state", "TPO" and "Prediction". Operators are assigned a
     * group only if the criterion is equal for every task. For example, two
     * operators are in the same group if they yield the exact same ranking function
     * for every unique task. If the resulting ranking function differs for one
     * task, the two operators are not grouped.
     * 
     * @param criterion String identifier of the criterion according to which the
     *                  operators are sorted: "Final state", "TPO" or "Prediction".
     * @return A set of operator lists grouped by the given criterion.
     */
    private static Set<List<DataOperator>> groupOperators() {
	Set<List<DataOperator>> operatorGroups = new HashSet<>();
	for (DataOperator operator1 : operators) {
	    // Operator is already grouped.
	    if (redundantOperators.contains(operator1))
		continue;
	    // New potential group.
	    Set<DataOperator> tempGroup = new HashSet<>();
	    tempGroup.add(operator1);
	    for (DataOperator operator2 : operators) {
		if (operator1.equals(operator2) || redundantOperators.contains(operator2))
		    continue;
		else {
		    int counter = 0;		    
			for (UniqueTask task : listOfTasks) {
			    for (Formula<PropositionalSignature<Character>> prediction : task.predictions.keySet()) {
				if (task.predictions.get(prediction).contains(operator1)
					&& (task.predictions.get(prediction).contains(operator2))) {
				    counter++;
				    break;
				}

			    }
			    // The predictions for every unique task are the same. Add operator 2 to the
			    // group set.
			    if (counter == listOfTasks.size()) {
				tempGroup.add(operator2);
				redundantOperators.add(operator2);
			    }
			}
			break;
		    }		   
	    }
	    // If there are operators with the exact same behavior, add them to a list.
	    if (tempGroup.size() > 1) {
		List<DataOperator> operatorGroup = new LinkedList<>();
		for (DataOperator op : tempGroup) {
		    operatorGroup.add(op);
		}
		operatorGroups.add(operatorGroup);
	    }
	}
	return operatorGroups;
    }

    /**
     * This function checks for each operator if its response prediction coincides
     * with the participant's answer in the data records. 
     * 
     * 
     * @param records List of all data records.
     * @return Collection that maps the evaluations of all records to an operator.
     */
    private static Map<DataOperator, HashMap<DataRecord, Boolean>> computePredictivePerformances(
	    List<DataRecord> records) {

	Map<DataOperator, HashMap<DataRecord, Boolean>> predictivePerformances = new HashMap<>();

	for (DataOperator op : operators) {

	    HashMap<DataRecord, Boolean> operatorPerformance = new HashMap<>();
	    for (DataRecord record : records) {
		boolean isConsistent = false;
		for (UniqueTask task : listOfTasks) {
		    if (record.task.equals(task.premises)) {
			// Checks if the operator is contained in the list of operators that predict the
			// answer given by the participant.
			if (task.predictions.get(record.response) == null) {
			    isConsistent = false;
			} else if (task.predictions.get(record.response).contains(op))
			    isConsistent = true;
		    }
		}
		operatorPerformance.put(record, isConsistent);
	    }
	    predictivePerformances.put(op, operatorPerformance);
	}
	return predictivePerformances;
    }

    /**
     * 
     * This function computes the accuracy of an operator for all tasks, and for the
     * four task groups "Conditional", "Biconditional", "Inclusive Disjunction" and
     * "Exclusive Disjunction", but not for the group "Other".
     * 
     * @param operator                        The analyzed DataOperator
     * @param predictivePerformanceOfOperator The map of the task performances of
     *                                        the operator.
     */
    private static void computeAccuracy(DataOperator operator,
	    HashMap<DataRecord, Boolean> predictivePerformanceOfOperator) {

	int countAllTrue = 0, countAll = 0, countCondTrue = 0, countCond = 0, countBiTrue = 0, countBi = 0,
		countIDTrue = 0, countID = 0, countEDTrue = 0, countED = 0, countOther = 0, countOtherTrue = 0;

	for (Map.Entry<DataRecord, Boolean> opTaskPerformance : predictivePerformanceOfOperator.entrySet()) {
	    countAll++;
	    for (UniqueTask task : listOfTasks) {
		if (opTaskPerformance.getKey().task.equals(task.premises)) {
		    task.counter++;
		    switch (task.getTaskType()) {
		    case Conditional: {
			countCond++;
			break;
		    }
		    case Biconditional: {
			countBi++;
			break;
		    }
		    case InclusiveDisjunction: {
			countID++;
			break;
		    }
		    case ExclusiveDisjunction: {
			countED++;
			break;
		    }

		    case Other: {
			countOther++;
			break;
		    }
		    }
		    if (opTaskPerformance.getValue() == true) {
			countAllTrue++;

			Integer frequency = task.operatorPerformance.get(operator);
			task.operatorPerformance.put(operator, frequency == null ? 1 : frequency + 1);
			switch (task.getTaskType()) {
			case Conditional: {
			    countCondTrue++;
			    break;
			}
			case Biconditional: {
			    countBiTrue++;
			    break;
			}
			case InclusiveDisjunction: {
			    countIDTrue++;
			    break;
			}
			case ExclusiveDisjunction: {
			    countEDTrue++;
			    break;
			}
			case Other: {
			    countOtherTrue++;
			    break;
			}
			}

		    }

		}

	    }

	}
	double accuracyGeneral = (double) countAllTrue / countAll;
	double accuracyConditional = (double) countCondTrue / countCond;
	double accuracyBiConditional = (double) countBiTrue / countBi;
	double accuracyInclusiveDisjunction = (double) countIDTrue / countID;
	double accuracyExclusiveDisjunction = (double) countEDTrue / countED;
//	double accuracyOther = (double) countOtherTrue / countOther;

	System.out.println(
		"----------------------------------------------------------------------------------------------------------------------");
	System.out.printf("%-30s %-22s  %-22s  %-22s  %-22s  %-22s%n", "Operator", "General Accuracy", "Conditional",
		"Biconditional", "Inclusive Disjunction", "Exclusive Disjunction");
	System.out.printf(
		"%-30s %-4s of %-4s = %5s    %-4s of %-4s = %5s    %-4s of %-4s = %5s    %-4s of %-4s = %5s    %-4s of %-4s = %5s %n",
		operator, countAllTrue, countAll, Math.round(10000.0 * accuracyGeneral) / 100.0, countCondTrue,
		countCond, Math.round(10000.0 * accuracyConditional) / 100.0, countBiTrue, countBi,
		Math.round(10000.0 * accuracyBiConditional) / 100.0, countIDTrue, countID,
		Math.round(10000.0 * accuracyInclusiveDisjunction) / 100.0, countEDTrue, countED,
		Math.round(10000.0 * accuracyExclusiveDisjunction) / 100.0);

    }

    /**
     * This function counts for each unique task how often the operators made right
     * predictions and determines the operators that performed best.
     * 
     */
    private static void computeAccuracyForUniqueTasks() {
	for (UniqueTask ut : listOfTasks) {
	    System.out.println();
	    System.out.println("-------------------------------------------------------------------------");
	    System.out.println("Accuracy for task " + ut);
	    System.out.println("-------------------------------------------------------------------------");
	    int best = 0;
	    List<DataOperator> bestOps = new LinkedList<>();
	    for (Map.Entry<DataOperator, Integer> performance : ut.operatorPerformance.entrySet()) {
		if (performance.getValue() > best) {
		    bestOps.clear();
		    bestOps.add(performance.getKey());
		    best = performance.getValue();
		} else if (performance.getValue() >= best)
		    bestOps.add(performance.getKey());

		System.out.println(performance.getKey() + ": " + performance.getValue() + " of " + ut.counter + " = "
			+ Math.round(10000.0 * (double) performance.getValue() / (double) ut.counter) / 100.0 + "%");
	    }
	    System.out.println();
	    bestOps.sort(opComparator);
	    System.out.println("Highest number of correct predictions: " + best
		    + "  - made by the following operators: " + bestOps);
	    System.out.println();

	}
    }

    /**
     * This function finds all task and answer combinations that are not predicted
     * by any of the operators.
     * 
     * @return A list of Pairs of String representations of the task and answer
     *         combination that is unpredicted.
     */
    private static List<Pair<String, String>> findUnpredictedAnswers() {

	List<Pair<String, String>> result = new LinkedList<>();
	Set<Pair<String, String>> tempSet = new HashSet<>();

	for (DataRecord record : records) {
	    for (UniqueTask utask : listOfTasks) {

		if (record.task.equals(utask.premises)) {
		    if (record.response == null) {
			if (!utask.predictions.keySet().contains(null)) {
			    tempSet.add(new Pair<String, String>(utask.toString(), "nothing"));

			} else
			    break;

		    } else {

			if (utask.predictions.keySet().contains(record.response)) {
			    break;
			} else {
			    tempSet.add(new Pair<String, String>(utask.toString(), record.response.toString()));
			}
		    }

		}
	    }

	}
	result.addAll(tempSet);
	return result;
    }

    /**
     * Checks if the participant chose the correct answer in the task. If not the
     * record is added to a list of individual errors of the participant, and the
     * chosen response is added to a list of false answers of the unique task.
     * 
     * @param participant The participant in the record
     * @param record      The record with the task.
     */
    private static void verifyResponse(Participant participant, DataRecord record) {
	for (UniqueTask task : listOfTasks) {
	    if (record.task.equals(task.premises)) {
		if (record.response == null) {
		    if (task.logicallyConsistentAnswer == null) {
			break;
		    } else {
			participant.wrongAnswers.add(record);
			task.wrongAnswerFrequencies.put(record.response,
				task.wrongAnswerFrequencies.get(record.response) == null ? 1
					: task.wrongAnswerFrequencies.get(record.response) + 1);
		    }

		} else {
		    if (task.logicallyConsistentAnswer == null) {
			participant.wrongAnswers.add(record);
			task.wrongAnswerFrequencies.put(record.response,
				task.wrongAnswerFrequencies.get(record.response) == null ? 1
					: task.wrongAnswerFrequencies.get(record.response) + 1);

		    } else if (!(record.response.equals(task.logicallyConsistentAnswer))) {
			participant.wrongAnswers.add(record);
			task.wrongAnswerFrequencies.put(record.response,
				task.wrongAnswerFrequencies.get(record.response) == null ? 1
					: task.wrongAnswerFrequencies.get(record.response) + 1);

		    }
		}
	    }
	}
    }

   
    private static void printToc() {
	printHeader1("Contents");

	System.out.printf("%-6s %-30s%n", "I)", "Operator Predictions");
	System.out.printf("%-6s %-30s%n", "II)", "Operator Groups");
	System.out.printf("%-6s %-30s%n", "III)", "Operators: Predictive Performance");
	System.out.printf("%-6s %-30s%n", "III-1)", "General Predictive Performance");
	System.out.printf("%-6s %-30s%n", "III-2)", "Accuracy for Unique Tasks");
	System.out.printf("%-6s %-30s%n", "III-3)", "Unpredicted Answers");
	System.out.printf("%-6s %-30s%n", "III-4)", "Predictive Performance of the Operators for Individuals");
    }

    private static void printHeader1(String s) {
	System.out.println();
	System.out.println();
	System.out.println(
		"----------------------------------------------------------------------------------------------");
	System.out.println(s);
	System.out.println(
		"----------------------------------------------------------------------------------------------");
	System.out.println();
    }

    private static void printHeader2(String s) {
	System.out.println();
	System.out.println(
		"----------------------------------------------------------------------------------------------");
	System.out.println(s);
	System.out.println(
		"----------------------------------------------------------------------------------------------");
    }
}
