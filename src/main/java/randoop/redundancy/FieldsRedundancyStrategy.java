package randoop.redundancy;

import java.util.HashSet;

import canonicalizer.BFHeapCanonicalizer;
import randoop.main.GenInputsAbstract;
import randoop.sequence.ExecutableSequence;
import representations.FieldRepresentation;
import representations.FieldValueRepresentation;
import representations.ObjectRepresentation;


public class FieldsRedundancyStrategy extends RedundancyStrategyTemplate {
	
  	private HashSet<FieldValueRepresentation> fieldStore;
  	
	public FieldsRedundancyStrategy(BFHeapCanonicalizer canonicalizer) {
		super(canonicalizer);
		this.fieldStore = new HashSet<FieldValueRepresentation>();
	}

	@Override
	public ObjectRepresentation createNewRepresentation() {
		FieldRepresentation f = new FieldRepresentation();
		f.setAbstractionFuction(GenInputsAbstract.abstraction);
		//TODO: remove. Ignore. If field isn't on the abs, so remove it
		f.dontSaveFields(GenInputsAbstract.ignore_fields); 
		return f;
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
		if ( fieldStore.add(((FieldRepresentation) representation).getExtensions()))
			return true;
		else {
			eSeq.redundant = true;
			return false;
		}
	}	

}
