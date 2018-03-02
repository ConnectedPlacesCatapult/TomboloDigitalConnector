package uk.org.tombolo.recipe;

import java.util.List;

public class SubjectRecipe {

	public static class SubjectAttributeMatchRule {
		public enum MatchableAttribute {label, name};
		public final MatchableAttribute attribute;
		public final String pattern;

		public SubjectAttributeMatchRule(MatchableAttribute attribute, String pattern) {
			this.attribute = attribute;
			this.pattern = pattern;
		}
	}

	public static class SubjectGeoMatchRule {
		public enum GeoRelation {within, equals, disjoint, intersects, touches, crosses, contains, overlaps};
		public final GeoRelation geoRelation;
		public final List<SubjectRecipe> subjects;

		public SubjectGeoMatchRule(GeoRelation geoRelation, List<SubjectRecipe> subjectRecipes){
			this.geoRelation = geoRelation;
			this.subjects = subjectRecipes;
		}
	}

	// Required Subject type
	String subjectType;

	// Required Provider
	String provider;

	// Optional Attribute Match Rule
	SubjectAttributeMatchRule matchRule;

	// Optional Geo Match Rule
	SubjectGeoMatchRule geoMatchRule;

	public SubjectRecipe(String provider, String subjectType, SubjectAttributeMatchRule matchRule, SubjectGeoMatchRule geoMatchRule){
		this.provider = provider;
		this.subjectType = subjectType;
		this.matchRule = matchRule;
		this.geoMatchRule = geoMatchRule;
	}

	public SubjectAttributeMatchRule getMatchRule() {
		return this.matchRule;
	}

	public SubjectGeoMatchRule getGeoMatchRule() {
		return this.geoMatchRule;
	}

	public String getSubjectType() {
		return subjectType;
	}

	public String getProvider() { return provider; }

}
