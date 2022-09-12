package randoop.redundancy;

import java.util.ArrayList;
import java.util.List;

import canonicalizer.BFHeapCanonicalizer;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Variable;
import randoop.types.Type;
import representations.ObjectRepresentation;

public abstract class RedundancyStrategyTemplate implements RedudantObjectsStrategy {
	
	private List<ObjectRepresentation> representations;
	private List<Object> objects;
	private BFHeapCanonicalizer canonicalizer;
	
	public RedundancyStrategyTemplate(BFHeapCanonicalizer canonicalizer) {
		this.canonicalizer = canonicalizer;
	}

	@Override
	public void clearRedundantObjectsIndices(ExecutableSequence eSeq) {
		assert eSeq.isNormalExecution();
		
    	// FIXME: Hack to allow all constructors to build new objects. This is due to JAVA not allowing 
    	// to detect generic types instantiations in empty collections at runtime. 
    	// Disabling it would result in only the first type instantiation of a collection being
    	// used throughout the generation process.
    	// We should get the static type information in the future to fix this.
    	boolean seqSize1 = false;
    	if (eSeq.sequence.size() == 1)
    		seqSize1 = true;
    	
    	List<Type> formalTypes = eSeq.sequence.getTypesForLastStatement();
    	List<Variable> arguments = eSeq.sequence.getVariablesOfLastStatement();
    	representations = new ArrayList<ObjectRepresentation>();
    	objects = new ArrayList<Object>();
		eSeq.outScope = false;
    	
    	// Clear all flags and return if sequence creates objects exceeding the scopes 
    	for (int i = 0; i < formalTypes.size(); i++) {
    		Variable argument = arguments.get(i);
   			Object object = eSeq.getValue(argument.getDeclIndex());
   			objects.add(object);
   			
    		ObjectRepresentation objRepr = createNewRepresentation();
    		representations.add(objRepr);
    		if (eSeq.sequence.isActive(argument.getDeclIndex())) {
    			if (!canonicalizer.canonicalize(object, objRepr)) {
    				eSeq.sequence.clearAllActiveFlags();
    				eSeq.outScope = true;
    				return;
    			}
    		}
    	}
    	// Clear flags for indexes that do not create new objects according to the t-wise criteria 
    	// currently in use 
    	for (int i = 0; i < formalTypes.size(); i++) {
    		Variable argument = arguments.get(i);
    		if (eSeq.sequence.isActive(argument.getDeclIndex())) {
    			if (!isObjectRepresentationNew(eSeq, i, objects.get(i), representations.get(i))) {
    				if (!seqSize1)
    					eSeq.sequence.clearActiveFlag(argument.getDeclIndex());
    			} 
    			else {
    				executeOnNewRepresentation(eSeq, i, objects.get(i), representations.get(i));
    			}
    		}
    	}
    }

}
