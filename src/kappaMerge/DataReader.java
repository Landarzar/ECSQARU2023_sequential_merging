package kappaMerge;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cs.ai.alchourron.logic.Formula;
import edu.cs.ai.alchourron.logic.logics.propositional.PropositionalSignature;

/**
 * Reader for the csv data file.
 *
 */

class DataReader {


    Set<String> participants = new HashSet<>();
    List<DataRecord> records = new LinkedList<DataRecord>();
    Map<List<Formula<PropositionalSignature<Character>>>, List<Formula<PropositionalSignature<Character>>>> uniqueTasks = new HashMap<>();

    void readData() throws IOException {
	BufferedReader csvReader = new BufferedReader(new FileReader("data\\data_cleaned.csv"));
	String row;
	boolean isFirst = true;
	while ((row = csvReader.readLine()) != null) {
	    if (isFirst) {
		isFirst = false;
		continue;
	    }
	    String[] dataRaw = row.split(",");
	    DataRecord record = new DataRecord();
	    record.fill(dataRaw);
	    participants.add(record.participant);
	    records.add(record);
	    uniqueTasks.put(record.task, record.answerOptions);
	}
	csvReader.close();
    }
}
