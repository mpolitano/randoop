package randoop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import canonicalizer.BFHeapCanonicalizer;
import canonicalizer.CanonicalizerConfig;
import randoop.main.GenInputsAbstract;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Variable;
import representations.FieldRepresentation;
import representations.FieldValueRepresentation;
import utils.ExtensionsType;

public class PrintObjectsVisitor implements ExecutionVisitor {

	public static Map<String, TupleList> abs = new HashMap<String, TupleList>();
	private String classUnderTest;
	private BFHeapCanonicalizer canonicalizer;

	@Override
	public void visitBeforeStatement(ExecutableSequence eseq, int i) {
		FieldRepresentation fieldRepresentation = null;
		if (i != eseq.size() - 1)
			return;

		System.out.println("--Sequence--");
		System.out.println(eseq.sequence.toCodeString());

		List<Variable> variables = eseq.sequence.getInputs(i);
		Object[] inputValues = eseq.getRuntimeInputs(variables);
		System.out.println("--Inputs--");
		String opKey = eseq.sequence.getOperation().getSignatureString();

		List<FieldValueRepresentation> representation = new ArrayList<FieldValueRepresentation>();

		for (int j = 0; j < inputValues.length; j++) {

			// initialize(eseq);//TODO: dont like.
			fieldRepresentation = new FieldRepresentation();
			fieldRepresentation.dontSaveFields(GenInputsAbstract.ignore_fields);

			Variable v = variables.get(j);
			// FieldType.createFieldExtensions();

			// if the field is not class under test, set representation.
			if (!v.getType().getCanonicalName().equals(classUnderTest))
				fieldRepresentation.setRepresentation(ExtensionsType.CONCRETE);
			else
				fieldRepresentation.setAbstractionFuction(GenInputsAbstract.abstraction);

			if (!canonicalizer.canonicalize(inputValues[j], fieldRepresentation)) {// Check if creates objects exceeding
																					// the scopes
				System.out.println("exceeding the scope");
				eseq.redundant = true;
				break;
			} else
				representation.add(j, fieldRepresentation.getExtensions()); // TODO: change name
		}
		//
		// check if the sequences is redundant.

		// if don't find in the map or the key exist but is diferent oldstate
		if (!eseq.redundant) {
			TupleList list = abs.get(opKey);
			if (list == null)
				list = new TupleList();
			abs.put(opKey, list);
			if (!list.add(representation)) {
				System.out.println("==== Redundant ==== ");
				eseq.redundant = true;
			}

		}

		System.out.println(abs);
	}

	@Override
	public void initialize(ExecutableSequence eseq) {
		classUnderTest = GenInputsAbstract.testclass.get(0); // TODO
		CanonicalizerConfig cfg = new CanonicalizerConfig();
		cfg.setMaxObjects(GenInputsAbstract.max_objects);
		canonicalizer = new BFHeapCanonicalizer(cfg);

	}

	@Override
	public void visitAfterStatement(ExecutableSequence eseq, int i) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitAfterSequence(ExecutableSequence eseq) {
		// TODO Auto-generated method stub

	}

}
