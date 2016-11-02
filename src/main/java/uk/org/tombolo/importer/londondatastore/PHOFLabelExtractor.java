package uk.org.tombolo.importer.londondatastore;

import org.apache.commons.codec.digest.DigestUtils;
import uk.org.tombolo.importer.utils.excel.LabelExtractor;

/**
 * This is an outdated way of handling excel
 *
 * @deprecated use excel extractors instead
 */
@Deprecated
public class PHOFLabelExtractor implements LabelExtractor {

	@Override
	public String extractLabel(String name) throws Exception {
		return DigestUtils.md5Hex(name);		
	}

}
