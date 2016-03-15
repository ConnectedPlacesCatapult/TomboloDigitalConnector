package uk.org.tombolo.importer.londondatastore;

import org.apache.commons.codec.digest.DigestUtils;

import uk.org.tombolo.importer.LabelExtractor;

public class PHOFLabelExtractor implements LabelExtractor {

	@Override
	public String extractLabel(String name) throws Exception {
		return DigestUtils.md5Hex(name);		
	}

}
