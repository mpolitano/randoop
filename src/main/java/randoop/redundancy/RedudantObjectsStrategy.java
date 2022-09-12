package randoop.redundancy;

import randoop.sequence.ExecutableSequence;
import representations.ObjectRepresentation;

public interface RedudantObjectsStrategy {

	ObjectRepresentation createNewRepresentation(); 

	boolean isObjectRepresentationNew(ExecutableSequence eSeq, int objIndex, Object object,
			ObjectRepresentation representation);

	void executeOnNewRepresentation(ExecutableSequence eSeq, int objIndex, Object object,
			ObjectRepresentation representation);

	public void clearRedundantObjectsIndices(ExecutableSequence eSeq);
	
}
