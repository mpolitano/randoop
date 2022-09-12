package randoop;

import java.lang.reflect.Array;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import canonicalizer.BFHeapCanonicalizer;
import canonicalizer.CanonicalizerConfig;
import randoop.main.GenInputsAbstract;
import randoop.operation.MethodCall;
import randoop.operation.ConstructorCall;

import randoop.operation.NonreceiverTerm;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Variable;
import randoop.util.OperationsLog;
import representations.FieldExtensions;


public class MethodClassifierVisitor implements ExecutionVisitor {

	public enum OperationType {
		CONSTRUCTOR,
		MODIFIER,
		OBSERVER,
		NOTEXECUTED
	}
	
	public MethodClassifierVisitor(List<TypedOperation> operations) {
		CanonicalizerConfig cfg = new CanonicalizerConfig();
		canonicalizer = new BFHeapCanonicalizer(cfg);
    	for (TypedOperation op: operations) {
    		if (op instanceof TypedClassOperation) {
    			TypedClassOperation top = (TypedClassOperation) op;
    			opTypes.put(getOperationSignature(top), OperationType.NOTEXECUTED);
    			opCoverageTypes.put(createStringOperation(top), OperationType.NOTEXECUTED);

    		}    
    	}
	}
	
	private String getOperationSignature(TypedClassOperation op) {
		if (op.getRawSignature() == null){
			return "";
		}	
		return op.getRawSignature().toString();
	}

	private BFHeapCanonicalizer canonicalizer;
	Map<String, OperationType> opTypes = new HashMap<>();
	Map<String, OperationType> opCoverageTypes = new HashMap<>();

	List<FieldExtensions> prevObjsRepr;
	TypedClassOperation top;
	String topSig;

	private String createStringOperation(TypedClassOperation op) {
		java.lang.reflect.Field f;
		Executable nameCall = null;
	    StringBuilder sb = new StringBuilder("");
	    try {
			if(op.isMethodCall()) {
				f = java.lang.reflect.Method.class.getDeclaredField("signature");
				f.setAccessible(true);
				nameCall = ((MethodCall) op.getOperation()).getMethod();
			    sb.append(op.getDeclaringType().getCanonicalName());
			    sb.append(".");
			    sb.append(op.getOperation().getName());

			}else 
				if(op.isConstructorCall()) {
					f = java.lang.reflect.Constructor.class.getDeclaredField("signature");		
					f.setAccessible(true);
				    sb.append(op.getDeclaringType().getCanonicalName());
				    sb.append(".<init>");
					nameCall = ((ConstructorCall) op.getOperation()).getConstructor();	
				}
	    } catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		
		String sig = op.toString();
	    sb.append("(");
	    if (nameCall == null)
	    	return "";
	    for(Class<?> c : nameCall.getParameterTypes()) 
	        sb.append((sig=Array.newInstance(c, 0).toString())
	            .substring(1, sig.indexOf('@')));
	    
    	sb.append(')');
    	if (nameCall.getClass() == Method.class) {
    		sb.append(
        		((Method)nameCall).getReturnType()==void.class?"V":
        			(sig=Array.newInstance(((Method) nameCall).getReturnType(), 0).toString()).substring(1, sig.indexOf('@'))
    			);
    	}else
    		sb.append("V"); //Constructor is void
    	
    	return sb.toString();
    }
	
	private boolean modifierOrConstructor(String topSig) {
		OperationType topType = opTypes.get(topSig);
		return topType == OperationType.CONSTRUCTOR || topType == OperationType.MODIFIER;
	}
	
	@Override
	public void visitBeforeStatement(ExecutableSequence eseq, int i) {
		if (i != eseq.size() - 1)
			return;
		
		TypedOperation op = eseq.sequence.getOperation();
		if (op instanceof TypedClassOperation) {
			top = (TypedClassOperation) op;
			topSig = getOperationSignature(top);
			if (modifierOrConstructor(topSig))
				return;
			
			if (top.isConstructorCall()) {
				opTypes.put(topSig, OperationType.CONSTRUCTOR);
    			opCoverageTypes.put(createStringOperation(top), OperationType.CONSTRUCTOR);
				return;
			}
		}  
		else {
			top = null;
			return;
		}
		
		prevObjsRepr = new ArrayList<FieldExtensions>();

		List<Variable> variables = eseq.sequence.getInputs(i);
		Object[] inputValues = eseq.getRuntimeInputs(variables);
		
		for (int j = 0; j < inputValues.length; j++) {
			FieldExtensions extensions = new FieldExtensions();

			if(!canonicalizer.canonicalize(inputValues[j], extensions)) 
				assert false;
				
			prevObjsRepr.add(j,extensions);
		}
	}
	

	@Override
	public void initialize(ExecutableSequence eseq) {

	}

	@Override
	public void visitAfterStatement(ExecutableSequence eseq, int i) {
		// top == null -> last operation in the sequence is not a TypedClassOperation
		if (i != eseq.size() - 1)
			return;
		
		if (!eseq.isNormalExecution() || top == null || modifierOrConstructor(topSig))
			return;
		
		if (!top.getOutputType().isVoid()) {
			ExecutionOutcome statementResult = eseq.getResult(i);
			Object res = ((NormalExecution)statementResult).getRuntimeValue();
			if (res != null) {
				Class<?> objectClass = res.getClass();
				// Non primitive result, top is modifier
				if (!(NonreceiverTerm.isNonreceiverType(objectClass) && !objectClass.equals(Class.class))) {
					opTypes.put(getOperationSignature(top), OperationType.MODIFIER);
					opCoverageTypes.put(createStringOperation(top), OperationType.MODIFIER);
					return;
				}
			}
		}
		
		List<Variable> variables = eseq.sequence.getInputs(i);
		Object[] inputValues = eseq.getRuntimeInputs(variables);
		
		for (int j = 0; j < inputValues.length; j++) {
			FieldExtensions extensions = new FieldExtensions();

			if(!canonicalizer.canonicalize(inputValues[j], extensions)) 
				assert false;
			
			FieldExtensions prevExtensions = prevObjsRepr.get(j);
			if (!prevExtensions.equals(extensions)) {
				opTypes.put(getOperationSignature(top), OperationType.MODIFIER);
				opCoverageTypes.put(createStringOperation(top), OperationType.MODIFIER);
				return;
			}
		}
		
		if (opTypes.get(topSig) == OperationType.NOTEXECUTED) {
			opTypes.put(getOperationSignature(top), OperationType.OBSERVER);
			opCoverageTypes.put(createStringOperation(top), OperationType.OBSERVER);
		}
	}


	@Override
	public void visitAfterSequence(ExecutableSequence eseq) {
		// TODO Auto-generated method stub
		
	}
	
	
	public void writeToLogFile() {
	    assert GenInputsAbstract.operations_log != null;

    	for (String op: opTypes.keySet()) {
   			OperationsLog.logPrintf("%s=%s\n", op, opTypes.get(op));
    	}
    	for (String op: opCoverageTypes.keySet()) {
   			OperationsLog.logCoveragePrintf("%s=%s\n", op, opCoverageTypes.get(op));
    	}
	}


}
