package randoop.redundancy;

import randoop.sequence.ExecutableSequence;
import representations.ObjectRepresentation;

public class NoRedudancyStrategy implements RedudantObjectsStrategy {

	@Override
	public void clearRedundantObjectsIndices(ExecutableSequence eSeq) {

	}

	@Override
	public ObjectRepresentation createNewRepresentation() {
		return null;
	}

	@Override
	public boolean isObjectRepresentationNew(ExecutableSequence eSeq, int objIndex, Object object,
			ObjectRepresentation representation) {
		return false;
	}

	@Override
	public void executeOnNewRepresentation(ExecutableSequence eSeq, int objIndex, Object object,
			ObjectRepresentation representation) {

	}

}
