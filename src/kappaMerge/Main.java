package kappaMerge;

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.cs.ai.alchourron.logic.Formula;
import edu.cs.ai.alchourron.logic.logics.propositional.PropositionalLogic;
import edu.cs.ai.alchourron.logic.logics.propositional.PropositionalSignature;
import edu.cs.ai.alchourron.logic.semantics.interpretations.PropositionalInterpretation;
import kappaMerge.operators.SequentialMergingOperator;
import kappaMerge.operators.merging.GMaxMerging;
import kappaMerge.operators.merging.MaxMerging;
import kappaMerge.operators.merging.MinMerging;
import kappaMerge.operators.merging.RMinMerging;
import kappaMerge.operators.merging.RSumMerging;
import kappaMerge.operators.merging.SumMerging;
import kappaMerge.operators.rankingConstruction.BiConditionalPatternAndFEM;
import kappaMerge.operators.rankingConstruction.BiConditionalPatternAndMM;
import kappaMerge.operators.rankingConstruction.PreferredInterpretationsAndMM;
import kappaMerge.operators.rankingConstruction.FullyExplicitModels;
import kappaMerge.operators.rankingConstruction.PreferredInterpretationsAndFEM;
import kappaMerge.operators.rankingConstruction.MentalModels;

/**
 * This is the entry point of the program, which contains a list of all used
 * data operators (a combination of a ranking principle and a merging operator).
 * Operators can be removed or added here. Starts the parser, constructs
 * severals structures required for the evaluation and starts a new evaluation.
 * 
 * 
 * @author Eda Ismail-Tsaous
 * @author Kai Sauerwald
 *
 */
public class Main {

    private final static PropositionalSignature<Character> signature = new PropositionalSignature<>('a', 'b');
    private static List<PropositionalInterpretation<Character>> interpretations = new LinkedList<>();
    private final static PropositionalLogic<Character> logic = new PropositionalLogic<>();

    public static void main(String[] args) throws IOException {

	DataReader dataReader = new DataReader();
	// parse data
	dataReader.readData();
	interpretations = signature.stream().collect(Collectors.toList());
	// construct structures required for the evaluation
	List<DataRecord> records = dataReader.records;
	List<UniqueTask> listOfTasks = new LinkedList<>();
	List<Participant> listOfParticipants = new LinkedList<>();

	// Extract unique tasks in the data set.
	for (Map.Entry<List<Formula<PropositionalSignature<Character>>>, List<Formula<PropositionalSignature<Character>>>> task : dataReader.uniqueTasks
		.entrySet()) {
	    listOfTasks.add(new UniqueTask(task.getKey(), task.getValue()));
	}
	for (String participant : dataReader.participants) {
	    listOfParticipants.add(new Participant(participant));

	}

	// Count how often a unique task occurs in the data set
	for (UniqueTask task : listOfTasks) {
	    for (DataRecord record : records) {
		if (record.task.equals(task.premises))
		    task.frequency++;
	    }
	}

	Comparator<UniqueTask> lexicographicOrder = (task1, task2) -> {
	    for (int i = 0; i < task1.premises.size(); i++) {
		int value = task1.premises.get(i).toString().compareTo(task2.premises.get(i).toString());
		if (value != 0)
		    return value;
	    }
	    return 0;
	};

	listOfTasks.sort(lexicographicOrder);

	List<DataOperator> listOfOperators = buildListOfOperators();
	Evaluation.analyze(records, listOfTasks, listOfOperators, listOfParticipants);

    }

    private static List<DataOperator> buildListOfOperators() {

	/*
	 * _______________________________________________________________________________________________
	 * Data Operators: All Data Operators are a combination of 1) a principle for
	 * the ranking construction 2) a merging operator
	 * 
	 * Comment/Uncomment operators in order to exclude/include them in the analysis.
	 * ___________________________________________________________________________________________
	 *
	 */

	List<DataOperator> listOfOperators = new LinkedList<>();

	/*
	 * _______________ Min-Operators ______________
	 */

	listOfOperators.add(new SequentialMergingOperator(new FullyExplicitModels(), new MinMerging()));
	listOfOperators.add(new SequentialMergingOperator(new FullyExplicitModels(), new RMinMerging()));
	listOfOperators.add(new SequentialMergingOperator(new MentalModels(), new MinMerging()));
	listOfOperators.add(new SequentialMergingOperator(new MentalModels(), new RMinMerging()));
	listOfOperators.add(new SequentialMergingOperator(new PreferredInterpretationsAndFEM(), new MinMerging()));
	listOfOperators.add(new SequentialMergingOperator(new PreferredInterpretationsAndFEM(), new RMinMerging()));
	listOfOperators.add(new SequentialMergingOperator(new PreferredInterpretationsAndMM(), new MinMerging()));
	listOfOperators.add(new SequentialMergingOperator(new PreferredInterpretationsAndMM(), new RMinMerging()));
	listOfOperators.add(new SequentialMergingOperator(new BiConditionalPatternAndFEM(), new MinMerging()));
	listOfOperators.add(new SequentialMergingOperator(new BiConditionalPatternAndFEM(), new RMinMerging()));
	listOfOperators.add(new SequentialMergingOperator(new BiConditionalPatternAndMM(), new MinMerging()));
	listOfOperators.add(new SequentialMergingOperator(new BiConditionalPatternAndMM(), new RMinMerging()));

	/*
	 * _______________ Max-Operators ______________
	 */

	listOfOperators.add(new SequentialMergingOperator(new FullyExplicitModels(), new MaxMerging()));
	listOfOperators.add(new SequentialMergingOperator(new FullyExplicitModels(), new GMaxMerging()));
	listOfOperators.add(new SequentialMergingOperator(new MentalModels(), new MaxMerging()));
	listOfOperators.add(new SequentialMergingOperator(new MentalModels(), new GMaxMerging()));
	listOfOperators.add(new SequentialMergingOperator(new PreferredInterpretationsAndFEM(), new MaxMerging()));
	listOfOperators.add(new SequentialMergingOperator(new PreferredInterpretationsAndFEM(), new GMaxMerging()));
	listOfOperators.add(new SequentialMergingOperator(new PreferredInterpretationsAndMM(), new MaxMerging()));
	listOfOperators.add(new SequentialMergingOperator(new PreferredInterpretationsAndMM(), new GMaxMerging()));
	listOfOperators.add(new SequentialMergingOperator(new BiConditionalPatternAndFEM(), new MaxMerging()));
	listOfOperators.add(new SequentialMergingOperator(new BiConditionalPatternAndFEM(), new GMaxMerging()));
	listOfOperators.add(new SequentialMergingOperator(new BiConditionalPatternAndMM(), new MaxMerging()));
	listOfOperators.add(new SequentialMergingOperator(new BiConditionalPatternAndMM(), new GMaxMerging()));

	/*
	 * _______________ Sum-Operators ______________
	 */

	listOfOperators.add(new SequentialMergingOperator(new FullyExplicitModels(), new SumMerging()));
	listOfOperators.add(new SequentialMergingOperator(new FullyExplicitModels(), new RSumMerging()));
	listOfOperators.add(new SequentialMergingOperator(new MentalModels(), new SumMerging()));
	listOfOperators.add(new SequentialMergingOperator(new MentalModels(), new RSumMerging()));
	listOfOperators.add(new SequentialMergingOperator(new PreferredInterpretationsAndFEM(), new SumMerging()));
	listOfOperators.add(new SequentialMergingOperator(new PreferredInterpretationsAndFEM(), new RSumMerging()));
	listOfOperators.add(new SequentialMergingOperator(new PreferredInterpretationsAndMM(), new SumMerging()));
	listOfOperators.add(new SequentialMergingOperator(new PreferredInterpretationsAndMM(), new RSumMerging()));
	listOfOperators.add(new SequentialMergingOperator(new BiConditionalPatternAndFEM(), new SumMerging()));
	listOfOperators.add(new SequentialMergingOperator(new BiConditionalPatternAndFEM(), new RSumMerging()));
	listOfOperators.add(new SequentialMergingOperator(new BiConditionalPatternAndMM(), new SumMerging()));
	listOfOperators.add(new SequentialMergingOperator(new BiConditionalPatternAndMM(), new RSumMerging()));

	return listOfOperators;
    }

    public static PropositionalLogic<Character> getLogic() {
	return logic;
    }

    public static PropositionalSignature<Character> getSignature() {
	return signature;
    }

    public static List<PropositionalInterpretation<Character>> getInterpretations() {
	return interpretations;
    }

}