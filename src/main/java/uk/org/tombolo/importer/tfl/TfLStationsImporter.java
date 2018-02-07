package uk.org.tombolo.importer.tfl;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TfLStationsImporter extends TfLImporter {

	protected enum DatasourceId {
		StationList(new DatasourceSpec(
				TfLStationsImporter.class,
				"StationList",
				"TfL Stations",
				"A list of TfL Stations",
				"https://data.tfl.gov.uk/")
		);

		private DatasourceSpec datasourceSpec;
		DatasourceId(DatasourceSpec datasourceSpec) { this.datasourceSpec = datasourceSpec; }
	}
	private enum AttributeName {ServingLineCount}
	private enum SubjectTypeName {TfLStation}

	private static final String STATIONS_API_SUFFIX = ".xml";
	private static final String STATIONS_API = "https://data.tfl.gov.uk/tfl/syndication/feeds/stations-facilities.xml";

	XPathFactory xPathFactory = XPathFactory.newInstance();
	XPath xpath = xPathFactory.newXPath();

	public TfLStationsImporter() throws IOException {
		datasourceIds = stringsFromEnumeration(DatasourceId.class);
	}

	@Override
	public DatasourceSpec getDatasourceSpec(String datasourceId) throws Exception {
		verifyConfiguration();
		return DatasourceId.valueOf(datasourceId).datasourceSpec;
	}

	@Override
	protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope, List<String> datasourceLocation) throws Exception {

		// Save timed values
		DatasourceId datasourceIdObject = DatasourceId.valueOf(datasource.getDatasourceSpec().getId());
		switch (datasourceIdObject){
		case StationList:
			GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), Subject.SRID);
			File xmlFile = downloadUtils.fetchFile(
					new URL(STATIONS_API
							+"?app_id="+properties.getProperty(PROP_API_APP_ID)
							+"&app_key="+properties.getProperty(PROP_API_APP_KEY)),
					getProvider().getLabel(),
					STATIONS_API_SUFFIX
			);

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document xmlDocument = builder.parse(xmlFile);
			Element rootElement = xmlDocument.getDocumentElement();

			String publishDateTime = (String)xpath.evaluate("/Root/Header/PublishDateTime/@canonical", rootElement, XPathConstants.STRING);
			publishDateTime = publishDateTime.replaceFirst(" ", "T").substring(0, 19);
			LocalDateTime timestamp = LocalDateTime.parse(publishDateTime);

			NodeList stations = rootElement.getElementsByTagName("station");

			// Save stations
			List<Subject> subjects = new ArrayList<Subject>();
			for (int i=0; i< stations.getLength(); i++){
				Node station = stations.item(i);

				String stationLabel = stationLabelFromNode(station);
				Node nameNode = (Node) xpath.evaluate("./Placemark/name", station, XPathConstants.NODE);
				String stationName = nameNode.getTextContent().trim();

				Node coordNode = (Node) xpath.evaluate("./Placemark/Point/coordinates", station, XPathConstants.NODE);
				String[] coords = coordNode.getTextContent().trim().split(",");
				Double longitude = Double.parseDouble(coords[0]);
				Double latitude = Double.parseDouble(coords[1]);
				Coordinate coordinate = new Coordinate(longitude,latitude);
				Point point = geometryFactory.createPoint(coordinate);

				subjects.add(new Subject(datasource.getUniqueSubjectType(), stationLabel, stationName, point));
			}
			saveAndClearSubjectBuffer(subjects);

			// Timed Values
			List<TimedValue> timedValues = new ArrayList<TimedValue>();
			Attribute servingLines = AttributeUtils.getByProviderAndLabel(getProvider(), AttributeName.ServingLineCount.name());
			for (int i=0; i< stations.getLength(); i++){
				Node station = stations.item(i);
				String stationLabel = stationLabelFromNode(station);
				Subject subject = SubjectUtils.getSubjectByTypeAndLabel(datasource.getUniqueSubjectType(), stationLabel);

				// Serving Line Count
				NodeList servingLineList = (NodeList) xpath.evaluate("./servingLines/servingLine", station, XPathConstants.NODESET);
				double count = Long.valueOf(servingLineList.getLength()).doubleValue();
				
				timedValues.add(new TimedValue(subject,servingLines, timestamp, count));
			}
			saveAndClearTimedValueBuffer(timedValues);
		}
	}
	
	private String stationLabelFromNode(Node station) throws XPathExpressionException{
		String stationId = (String) xpath.evaluate("@id", station, XPathConstants.STRING);
		String stationType = (String) xpath.evaluate("@type", station, XPathConstants.STRING);
		
		return "tfl:station:"+stationType+":"+stationId;
	}

	@Override
	public List<SubjectType> getSubjectTypes(String datasourceId) {
		return Collections.singletonList(new SubjectType(getProvider(), SubjectTypeName.TfLStation.name(), "Transport for London Station"));
	}

	@Override
	public List<Attribute> getTimedValueAttributes(String datasourceId) {
		List<Attribute> attributes = new ArrayList<Attribute>();
		attributes.add(new Attribute(getProvider(), AttributeName.ServingLineCount.name(),
				"The number of lines serving a station"));
		return attributes;
	}
}
