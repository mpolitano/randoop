package randoop.redundancy;

import canonicalizer.BFHeapCanonicalizer;
import randoop.sequence.ExecutableSequence;
import representations.ObjectRepresentation;
import representations.TWise;
import representations.TWiseByType;

public class TWiseRedundancyStrategy extends RedundancyStrategyTemplate {
	
	private TWiseByType globalStore;
	private TWise twiseType;
	
	public TWiseRedundancyStrategy(BFHeapCanonicalizer canonicalizer, TWise twiseType) {
		super(canonicalizer);
		this.twiseType = twiseType;
		this.globalStore = new TWiseByType(this.twiseType);
	}

	@Override
	public ObjectRepresentation createNewRepresentation() {
		return new TWiseByType(twiseType);
	}
	
	@Override
	public void executeOnNewRepresentation(ExecutableSequence eSeq,
			int objIndex,
			Object object, 
			ObjectRepresentation representation) {
		int newSize = globalStore.size();
		int diff = newSize - prevSize;
		if (diff > 10000)
			System.out.println(String.format("\nVery large object at index %s in %s. %s added: %s. Test:\n %s.",// Store: %s\n", 
					objIndex, 
					eSeq.statementToCodeString(eSeq.size() -1),
					twiseType.toString().toLowerCase(),
					diff,
					eSeq.toCodeString()));
					// globalStore.toString()));	
	}

	private int prevSize;
	@Override
	public boolean isObjectRepresentationNew(ExecutableSequence eSeq,
			int objIndex,
			Object object, 
			ObjectRepresentation representation) {
		prevSize = globalStore.size();
		return globalStore.addAll(representation);
	}	

}
