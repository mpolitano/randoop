package randoop.redundancy;

import canonicalizer.BFHeapCanonicalizer;
import randoop.sequence.ExecutableSequence;
import representations.FieldExtensions;
import representations.ObjectRepresentation;
import representations.ObjectsByTypeStore;

public class FERedundancyStrategy extends RedundancyStrategyTemplate {
	
	private ObjectsByTypeStore store = new ObjectsByTypeStore();
	
	public FERedundancyStrategy(BFHeapCanonicalizer canonicalizer) {
		super(canonicalizer);
	}

	@Override
	public ObjectRepresentation createNewRepresentation() {
		return new FieldExtensions();
	}
	
	@Override
	public void executeOnNewRepresentation(ExecutableSequence eSeq,
			int objIndex,
			Object object, 
			ObjectRepresentation representation) { 
		
	}

	@Override
	public boolean isObjectRepresentationNew(ExecutableSequence eSeq,
			int objIndex,
			Object object, 
			ObjectRepresentation representation) {
		return store.add(object, representation);
	}	

}
