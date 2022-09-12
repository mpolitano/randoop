package randoop;

import java.util.HashSet;
import java.util.List;

import representations.FieldValueRepresentation;


public class TupleList {
	
//	private Map<List,FieldValueRepresentation> tuples;
	private HashSet<List<FieldValueRepresentation>>  tuples;

	public TupleList() {
		tuples =  new HashSet<>();
	 }
	 
	
	public boolean add(List<FieldValueRepresentation> f) {
		return tuples.add(f);
	}
	
	public boolean contains(List<FieldValueRepresentation> l) {
		return tuples.contains(l);
	}
	
	@Override
	public String toString() {
		String result = "[tuples = ";
		for (List t : tuples) {
			result += t.toString();

		}
		 result +=  " ]"+ "\n";
		 return result;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tuples == null) ? 0 : tuples.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
//			if (getClass() != obj.getClass())
//				return false;
		if (!(obj instanceof TupleList))
			return false;
		TupleList other = (TupleList) obj;
		if (tuples == null) {
			if (other.tuples != null)
				return false;
		} else if (!tuples.equals(other.tuples))
			return false;
		return true;
	}


}