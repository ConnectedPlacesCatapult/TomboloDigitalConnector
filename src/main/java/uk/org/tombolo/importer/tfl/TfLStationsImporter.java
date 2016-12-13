package uk.org.tombolo.importer.tfl;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.Importer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TfLStationsImporter extends TfLImporter implements Importer {
	protected static enum DatasourceId {StationList};
	private static enum AttributeName {ServingLineCount};
	private static enum SubjectTypeName {TfLStation};

	Logger log = LoggerFactory.getLogger(TfLStationsImporter.class);
	
	XPathFactory xPathFactory = XPathFactory.newInstance();
	XPath xpath = xPathFactory.newXPath();

	public TfLStationsImporter() throws IOException {
		super();
	}

	@Override
	public List<Datasource> getAllDatasources() throws Exception {
		return datasourcesFromEnumeration(DatasourceId.class);
	}

	@Override
	public Datasource getDatasource(String datasourceId) throws Exception {
		verifyConfiguration();
		DatasourceId datasourceIdObject = DatasourceId.valueOf(datasourceId);
		switch (datasourceIdObject){
			case StationList:
				Datasource datasource = new Datasource(getClass(), DatasourceId.StationList.name(), getProvider(), "TfL Stations", "A list of TfL Stations");
				datasource.setLocalDatafile("tfl/stations/stations-facilities.xml");

				datasource.setRemoteDatafile(
						"https://data.tfl.gov.uk/tfl/syndication/feeds/stations-facilities.xml"
								+"?app_id="+properties.getProperty(PROP_API_APP_ID)
								+"&app_key="+properties.getProperty(PROP_API_APP_KEY));
				datasource.addAllTimedValueAttributes(getStationAttributes());
				datasource.addSubjectType(new SubjectType(SubjectTypeName.TfLStation.name(), "Transport for London Station"));
				return datasource;
		}
		return null;
	}

	@Override
	protected int importDatasource(Datasource datasource) throws Exception {
		saveDatasourceMetadata(datasource);
		
		// Save timed values
		DatasourceId datasourceIdObject = DatasourceId.valueOf(datasource.getId());
		switch (datasourceIdObject){
		case StationList:
			GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), Subject.SRID);
			File xmlFile = downloadUtils.getDatasourceFile(datasource);

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
			SubjectUtils.save(subjects);

			// Timed Values
			List<TimedValue> timedValues = new ArrayList<TimedValue>();
			Attribute servingLines = AttributeUtils.getByProviderAndLabel(getProvider(), AttributeName.ServingLineCount.name());
			for (int i=0; i< stations.getLength(); i++){
				Node station = stations.item(i);
				String stationLabel = stationLabelFromNode(station);
				Subject subject = SubjectUtils.getSubjectByLabel(stationLabel);

				// Serving Line Count
				NodeList servingLineList = (NodeList) xpath.evaluate("./servingLines/servingLine", station, XPathConstants.NODESET);
				double count = Long.valueOf(servingLineList.getLength()).doubleValue();
				
				timedValues.add(new TimedValue(subject,servingLines, timestamp, count));
			}
			int saved = TimedValueUtils.save(timedValues);

			return saved;
		}
		return 0;
	}
	
	private String stationLabelFromNode(Node station) throws XPathExpressionException{
		String stationId = (String) xpath.evaluate("@id", station, XPathConstants.STRING);
		String stationType = (String) xpath.evaluate("@type", station, XPathConstants.STRING);
		
		return "tfl:station:"+stationType+":"+stationId;
	}
	
	private List<Attribute> getStationAttributes(){
		List<Attribute> attributes = new ArrayList<Attribute>();
		attributes.add(new Attribute(getProvider(), AttributeName.ServingLineCount.name(), "Serving Lines", "The number of lines serving a station", Attribute.DataType.numeric));
		return attributes;
	}
}
