package uk.org.tombolo.execution.spec;

import java.util.Map;
import java.util.TreeMap;

public class SubjectSpecification {

	public static class SubjectMatchRule {
		public enum MatchableAttribute {label, name};
		public final MatchableAttribute attribute;
		public final String pattern;

		public SubjectMatchRule(MatchableAttribute attribute, String pattern) {
			this.attribute = attribute;
			this.pattern = pattern;
		}
	}

	SubjectMatchRule matchRule;
	String subjectType;
	
	Map<String,String> attributes = new TreeMap<String,String>();
	
	public SubjectSpecification(SubjectMatchRule matchRule, String subjectType){
		this.matchRule = matchRule;
		this.subjectType = subjectType;
	}

	public SubjectMatchRule getMatchRule() {
		return this.matchRule;
	}

	public String getSubjectType() {
		return subjectType;
	}
	
	public Map<String,String> getAttributes(){
		return attributes;
	}
	
}
