package uk.org.tombolo.execution.spec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SubjectSpecification {

	public static class SubjectMatcher {
		public final String attribute;
		public final String pattern;

		public SubjectMatcher(String attribute, String pattern) {
			this.attribute = attribute;
			this.pattern = pattern;
		}
	}

	List<SubjectMatcher> matchers;
	String subjectType;
	
	Map<String,String> attributes = new TreeMap<String,String>();
	
	public SubjectSpecification(List<SubjectMatcher> matchers, String subjectType){
		this.matchers = matchers;
		this.subjectType = subjectType;
	}

	public List<SubjectMatcher> getMatchers() {
		if (null == this.matchers)
			return new ArrayList<>();
		return this.matchers;
	}

	public String getSubjectType() {
		return subjectType;
	}
	
	public Map<String,String> getAttributes(){
		return attributes;
	}
	
}
