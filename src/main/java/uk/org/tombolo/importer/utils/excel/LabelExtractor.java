package uk.org.tombolo.importer.utils.excel;

/**
 * This is an outdated way of handling excel
 *
 * @deprecated use excel extractors instead
 */
@Deprecated
public interface LabelExtractor {
	public String extractLabel(String name) throws Exception;
}
