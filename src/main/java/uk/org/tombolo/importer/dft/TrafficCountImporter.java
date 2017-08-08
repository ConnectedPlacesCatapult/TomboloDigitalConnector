package uk.org.tombolo.importer.dft;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.Config;
import uk.org.tombolo.importer.ConfigurationException;
import uk.org.tombolo.importer.DataSourceID;
import uk.org.tombolo.importer.Importer;
import uk.org.tombolo.importer.utils.CoordinateUtils;

import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

/**
 * This importer imports traffic count information from Department for Transport.
 * The subjects imported are "trafficCounter".
 * The geography scopes can be any local-authority or region in the UK.
 * There is no temporal scope.
 *
 * Traffic volume
 * - http://api.dft.gov.uk/v3/trafficcounts/export/data/traffic/region/London.csv
 * - http://api.dft.gov.uk/v3/trafficcounts/export/data/traffic/la/Aberdeen+City.csv
 *
 * Traffic counts
 * - http://api.dft.gov.uk/v3/trafficcounts/export/region/London.csv
 * - http://api.dft.gov.uk/v3/trafficcounts/export/la/Aberdeen+City.csv
 *
 */
public class TrafficCountImporter extends AbstractDFTImporter implements Importer {
	private static final String REGION = "region/";
	private static final String LA = "la/";
	private static final String CSV_POSTFIX = ".csv";
	private static final int YEAR_INDEX = 0;
	private static final int ID_INDEX = 1;
	private static final int EASTING_INDEX = 8;
	private static final int NORTHING_INDEX = 9;
	private static final int ROADNAME_INDEX = 6;
	private static final int STARTJUNCTION_INDEX = 10;
	private static final int ENDJUNCTION_INDEX = 11;

	private static final int TIMED_VALUE_BUFFER_SIZE = 100000;

	protected enum DatasourceId {
		trafficVolume(new DataSourceID(
				"trafficVolume",
				"Annual volume of traffic",
				"Total volume of traffic on the stretch of road for the whole year. " +
						"Units = thousand vehicle miles.",
				"http://www.dft.gov.uk/traffic-counts/",
				null),
				"http://api.dft.gov.uk/v3/trafficcounts/export/data/traffic/"
				),
		trafficCounts(new DataSourceID(
				"trafficCounts",
				"Annual average daily flow",
				"The number of vehicles that will drive on that stretch of road on an average day of the year. " +
						"Units = vehicles per day.",
				"http://www.dft.gov.uk/traffic-counts/",
				null),
				"http://api.dft.gov.uk/v3/trafficcounts/export/"
		);

		private DataSourceID dataSourceID;
		private String urlPrefix;

		DatasourceId(DataSourceID dataSourceID, String urlPrefix) {
			this.dataSourceID = dataSourceID;
			this.urlPrefix = urlPrefix;
		}
	};

	private static final String TRAFFIC_COUNTER_SUBJECT_TYPE_LABEL = "trafficCounter";
	private static final String TRAFFIC_COUNTER_SUBJECT_TYPE_DESC = "Traffic counter from Department for Transport";

	// Fixed attributes that are only necessary once per traffic counter
	protected enum FixedAttribute {
		RoadName(new Attribute(
				null,
				"RoadName",
				"Road Name",
				"Road Name",
				Attribute.DataType.string),
				6),
		RoadCategory(new Attribute(
				null,
				"RoadCategory",
				"Road Category",
				"Road Category",
				Attribute.DataType.string),
				7),
		StartJunction(new Attribute(
				null,
				"StartJunction",
				"Start Junction",
				"Start Junction",
				Attribute.DataType.string),
				10),
		EndJunction(new Attribute(
				null,
				"EndJunction",
				"End Junction",
				"End Junction",
				Attribute.DataType.string),
				11);

		private Attribute attribute;
		private int columnIndex;

		FixedAttribute(Attribute attribute, int columnIndex){
			this.attribute = attribute;
			this.columnIndex = columnIndex;
		}
	}

	// Timed Value Attributes in the traffic count file
	protected enum CountAttribute {
		CountPedalCycles(new Attribute(
				null,
				"CountPedalCycles",
				"Pedal Cycle Count",
				"Pedal Cycle Count",
				Attribute.DataType.numeric),
				14),
		CountMotorcycles(new Attribute(
				null,
				"CountMotorcycles",
				"Motorycle Count",
				"Motorcycle Count",
				Attribute.DataType.numeric),
				15),
		CountCarsTaxis(new Attribute(
				null,
				"CountCarsTaxis",
				"Count of cars and taxis",
				"Count of cars and taxis",
				Attribute.DataType.numeric),
				16),
		CountBusesCoaches(new Attribute(
				null,
				"CountBusesCoaches",
				"Count of buses and coaches",
				"Count of buses and coaches",
				Attribute.DataType.numeric),
				17),
		CountLightGoodsVehicles(new Attribute(
				null,
				"CountLightGoodsVehicles",
				"Count of light goods vehicles",
				"Count of light goods vehicles",
				Attribute.DataType.numeric),
				18),
		CountHeavyGoodsVehicles(new Attribute(
				null,
				"CountHeavyGoodsVehicles",
				"Count of heavy goods vehicles",
				"Count of heavy goods vehicles",
				Attribute.DataType.numeric),
				25);

			private Attribute attribute;
			private int columnIndex;

			CountAttribute(Attribute attribute, int columnIndex){
				this.attribute = attribute;
				this.columnIndex = columnIndex;
			}
		};

	// Timed Value Attributes in the traffic volume file
	protected static enum VolumeAttribute
	{
		VolumePedalCycles(new Attribute(
				null,
				"VolumePedalCycles",
				"Pedal Cycle Volume",
				"Pedal Cycle Volume",
				Attribute.DataType.numeric),
				13),
		VolumeMotorcycles(new Attribute(
				null,
				"VolumeMotorcycles",
				"Motorycle Volume",
				"Motorcycle Volume",
				Attribute.DataType.numeric),
				14),
		VolumeCarsTaxis(new Attribute(
				null,
				"VolumeCarsTaxis",
				"Volume of cars and taxis",
				"Volume of cars and taxis",
				Attribute.DataType.numeric),
				15),
		VolumeBusesCoaches(new Attribute(
				null,
				"VolumeBusesCoaches",
				"Volume of buses and coaches",
				"Volume of buses and coaches",
				Attribute.DataType.numeric),
				16),
		VolumeLightGoodsVehicles(new Attribute(
				null,
				"VolumeLightGoodsVehicles",
				"Volume of light goods vehicles",
				"Volume of light goods vehicles",
				Attribute.DataType.numeric),
				17),
		VolumeHeavyGoodsVehicles(new Attribute(
				null,
				"VolumeHeavyGoodsVehicles",
				"Volume of heavy goods vehicles",
				"Volume of heavy goods vehicles",
				Attribute.DataType.numeric),
				24);

		private Attribute attribute;
		private int columnIndex;

		VolumeAttribute(Attribute attribute, int columnIndex){
			this.attribute = attribute;
			this.columnIndex = columnIndex;
		}
	};

	// Regions for which data is served by DfT
	private static final List<String> regions = Arrays.asList("East Midlands","East of England","London",
			"Merseyside","North East","North West","Scotland","South East","South West",
			"Wales","West Midlands","Yorkshire and The Humber");

	// Local Authorities for which data is served by DfT
	private static final List<String> localAuthorities = Arrays.asList("Aberdeen City","Aberdeenshire",
			"Angus","Argyll and Bute","Barking and Dagenham","Barnet","Barnsley",
			"Bath and North East Somerset","Bedford","Bedfordshire","Bexley","Birmingham",
			"Blackburn with Darwen","Blackpool","Blaenau Gwent","Bolton","Bournemouth",
			"Bracknell Forest","Bradford","Brent","Bridgend","Brighton and Hove",
			"Bristol, City of","Bromley","Buckinghamshire","Bury","Caerphilly","Calderdale",
			"Cambridgeshire","Camden","Cardiff","Carmarthenshire","Central Bedfordshire",
			"Ceredigion","Cheshire","City of Edinburgh","City of London","Clackmannanshire",
			"Comhairle nan Eilean Siar","Conwy","Cornwall excluding Isles of Scilly",
			"Coventry","Croydon","Cumbria","Darlington","Denbighshire","Derby","Derbyshire",
			"Devon","Doncaster","Dorset","Dudley","Dumfries and Galloway","Dundee City",
			"Durham","Ealing","East Ayrshire","East Cheshire","East Dunbartonshire",
			"East Lothian","East Renfrewshire","East Riding of Yorkshire","East Sussex",
			"Enfield","Essex","Falkirk","Fife","Flintshire","Gateshead","Glasgow City",
			"Gloucestershire","Greenwich","Gwynedd","Hackney","Halton","Hammersmith and Fulham",
			"Hampshire","Haringey","Harrow","Hartlepool","Havering","Herefordshire, County of",
			"Hertfordshire","Highland","Hillingdon","Hounslow","Inverclyde","Isle of Anglesey",
			"Isle of Wight","Isles of Scilly","Islington","Kensington and Chelsea","Kent",
			"Kingston upon Hull, City of","Kingston upon Thames","Kirklees","Knowsley",
			"Lambeth","Lancashire","Leeds","Leicester","Leicestershire","Lewisham",
			"Lincolnshire","Liverpool","Luton","Manchester","Medway","Merthyr Tydfil",
			"Merton","Middlesbrough","Midlothian","Milton Keynes","Monmouthshire","Moray",
			"Neath Port Talbot","Newcastle upon Tyne","Newham","Newport","Norfolk",
			"North Ayrshire","North East Lincolnshire","North Lanarkshire","North Lincolnshire",
			"North Somerset","North Tyneside","North Yorkshire","Northamptonshire",
			"Northumberland","Nottingham","Nottinghamshire","Oldham","Orkney Islands",
			"Oxfordshire","Pembrokeshire","Perth and Kinross","Peterborough","Plymouth",
			"Poole","Portsmouth","Powys","Reading","Redbridge","Redcar and Cleveland",
			"Renfrewshire","Rhondda, Cynon, Taff","Richmond upon Thames","Rochdale",
			"Rotherham","Rutland","Salford","Sandwell","Scottish Borders","Sefton",
			"Sheffield","Shetland Islands","Shropshire","Slough","Solihull","Somerset",
			"South Ayrshire","South Gloucestershire","South Lanarkshire","South Tyneside",
			"Southampton","Southend-on-Sea","Southwark","St. Helens","Staffordshire",
			"Stirling","Stockport","Stockton-on-Tees","Stoke-on-Trent","Suffolk","Sunderland",
			"Surrey","Sutton","Swansea","Swindon","Tameside","Telford and Wrekin",
			"The Vale of Glamorgan","Thurrock","Torbay","Torfaen","Tower Hamlets",
			"Trafford","Wakefield","Walsall","Waltham Forest","Wandsworth","Warrington",
			"Warwickshire","West Berkshire","West Cheshire","West Dunbartonshire",
			"West Lothian","West Sussex","Westminster","Wigan","Wiltshire",
			"Windsor and Maidenhead","Wirral","Wokingham","Wolverhampton","Worcestershire",
			"Wrexham","York");

	private static final Logger log = LoggerFactory.getLogger(TrafficCountImporter.class);

	public TrafficCountImporter(Config config) {
		super(config);
		datasourceIds = stringsFromEnumeration(DatasourceId.class);
		geographyLabels = new ArrayList<>(regions);
		geographyLabels.addAll(localAuthorities);
	}

	@Override
	public int getTimedValueBufferSize() {
		return TIMED_VALUE_BUFFER_SIZE;
	}

	@Override
	public Datasource getDatasource(String datasourceIdString) throws Exception {
		DatasourceId datasourceId = DatasourceId.valueOf(datasourceIdString);

		Datasource datasource = new Datasource(
				getClass(),
				datasourceId.dataSourceID.getLabel(),
				getProvider(),
				datasourceId.dataSourceID.getName(),
				datasourceId.dataSourceID.getDescription()
		);

		// Update attribute list
		datasource.addAllTimedValueAttributes(getTimedValueAttributes(datasourceId));
		datasource.setUrl(datasourceId.dataSourceID.getUrl());

		SubjectType subjectType = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(getProvider().getLabel(), TRAFFIC_COUNTER_SUBJECT_TYPE_LABEL);
		if (subjectType == null) {
			datasource.addSubjectType(new SubjectType(getProvider(), TRAFFIC_COUNTER_SUBJECT_TYPE_LABEL, TRAFFIC_COUNTER_SUBJECT_TYPE_DESC));
		} else {
			datasource.addSubjectType(subjectType);
		}
		
		return datasource;
	}

	@Override
	protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope) throws Exception {
		DatasourceId datasourceId = DatasourceId.valueOf(datasource.getId());

		if (geographyScope == null || geographyScope.isEmpty())
			throw new ConfigurationException("Missing geography scope");

		for (String geogrpahyLabel : geographyScope) {

			URL url = new URL(getTrafficCountUrl(datasourceId, geogrpahyLabel));

			// Read timed values
			GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), Subject.SRID);
			Set<Long> trafficCounters = new HashSet<Long>();
			List<TimedValue> timedValueBuffer = new ArrayList<TimedValue>();
			CSVParser csvParser = new CSVParser(new InputStreamReader(
					downloadUtils.fetchInputStream(url, getProvider().getLabel(), ".csv")), CSVFormat.RFC4180);
			for (CSVRecord record : csvParser) {
				long id;
				try {
					id = Long.valueOf(record.get(ID_INDEX));
				} catch (NumberFormatException e) {
					// We do not have a proper id ... could be header
					continue;
				}
				String label = "DfT-TrafficCounter-" + id;
				String year = record.get(YEAR_INDEX);

				// Save subject object
				if (!trafficCounters.contains(id)){
					// We have not seen this id before
					long easting = Long.valueOf(record.get(EASTING_INDEX));
					long northing = Long.valueOf(record.get(NORTHING_INDEX));
					String road = record.get(ROADNAME_INDEX);
					String startJunction = record.get(STARTJUNCTION_INDEX);
					String endJunction = record.get(ENDJUNCTION_INDEX);

					Coordinate coordinate = CoordinateUtils.osgbToWgs84(easting, northing);
					Point point = geometryFactory.createPoint(coordinate);

					String name = road + " (" + startJunction + " to " + endJunction + ")";

					// Save subject
					Subject subject = new Subject(datasource.getUniqueSubjectType(), label, name, point);
					List<Subject> subjectList = new ArrayList<Subject>();
					subjectList.add(subject);
					saveAndClearSubjectBuffer(subjectList);
					subject = SubjectUtils.getSubjectByTypeAndLabel(datasource.getUniqueSubjectType(), label);

					// Add fixed values
					List<FixedValue> fixedValueBuffer = new ArrayList<>();
					for (FixedAttribute fixedAttribute : FixedAttribute.values()){
						Attribute attribute = AttributeUtils.getByProviderAndLabel(getProvider(), fixedAttribute.attribute.getLabel());
						String stringValue = record.get(fixedAttribute.columnIndex);
						FixedValue fixedValue = new FixedValue(subject, attribute, stringValue);
						fixedValueBuffer.add(fixedValue);
					}
					saveAndClearFixedValueBuffer(fixedValueBuffer);

					// Add subject to list of added subjects
					trafficCounters.add(id);
				}

				Subject subject = SubjectUtils.getSubjectByTypeAndLabel(datasource.getUniqueSubjectType(), label);
				LocalDateTime timestamp = TimedValueUtils.parseTimestampString(year);

				// Import timed values
				switch (datasourceId){
					case trafficCounts:
						for (CountAttribute countAttribute : CountAttribute.values()){
							Attribute attribute = AttributeUtils.getByProviderAndLabel(getProvider(), countAttribute.attribute.getLabel());
							double count = Double.valueOf(record.get(countAttribute.columnIndex));
							TimedValue timedValue = new TimedValue(subject, attribute, timestamp, count);
							timedValueBuffer.add(timedValue);
						}
						break;
					case trafficVolume:
						for (VolumeAttribute volumeAttribute : VolumeAttribute.values()){
							Attribute attribute = AttributeUtils.getByProviderAndLabel(getProvider(), volumeAttribute.attribute.getLabel());
							double count = Double.valueOf(record.get(volumeAttribute.columnIndex));
							TimedValue timedValue = new TimedValue(subject, attribute, timestamp, count);
							timedValueBuffer.add(timedValue);
						}
						break;
				}

				if (timedValueBuffer.size() > getTimedValueBufferSize())
					saveAndClearTimedValueBuffer(timedValueBuffer);

			}
			saveAndClearTimedValueBuffer(timedValueBuffer);
			csvParser.close();
		}
	}

	protected String getTrafficCountUrl(DatasourceId datasourceId, String geographyLabel) throws ConfigurationException {
		String remoteId = geographyLabel.replaceAll(" ", "+").replaceAll(",", "%2C");

		if (regions.contains(geographyLabel)){
			return datasourceId.urlPrefix+REGION+remoteId+CSV_POSTFIX;
		}else if (localAuthorities.contains(geographyLabel)){
			return datasourceId.urlPrefix+LA+remoteId+CSV_POSTFIX;
		}
		throw new ConfigurationException("Unknown Geography Scope: " + geographyLabel);
	}

	private List<Attribute> getTimedValueAttributes(DatasourceId datasourceId){
		List<Attribute> attributes = new ArrayList<Attribute>();
		// Add fixed attributes
		for(FixedAttribute fixedAttribute : FixedAttribute.values()){
			fixedAttribute.attribute.setProvider(getProvider());
			attributes.add(fixedAttribute.attribute);
		}
		// Add timed value attributes
		switch (datasourceId){
			case trafficCounts:
				for(CountAttribute countAttribute : CountAttribute.values()){
					countAttribute.attribute.setProvider(getProvider());
					attributes.add(countAttribute.attribute);
				}
				break;
			case trafficVolume:
				for(VolumeAttribute volumeAttribute : VolumeAttribute.values()){
					volumeAttribute.attribute.setProvider(getProvider());
					attributes.add(volumeAttribute.attribute);
				}
				break;
		}

		return attributes;
	}
}
