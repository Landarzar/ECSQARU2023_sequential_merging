package kappaMerge;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a participant in the experiment.
 *
 */
public class Participant {
    
    String id;
    // List of records associated with this participant.
    List<DataRecord> pRecords = new LinkedList<>();
    // List of records, in which the participant did not give a logically consistent answer.
    List<DataRecord> wrongAnswers = new LinkedList<>();
//    Set<Pair<List<Forula<Propso>>>>
    
    
    public Participant(String id) {
	this.id = id;
    }

    
    public String toString() {
    	return id;
    }
}
