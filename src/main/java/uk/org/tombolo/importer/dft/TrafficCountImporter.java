package uk.org.tombolo.importer.dft;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.AttributeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.core.utils.TimedValueUtils;
import uk.org.tombolo.importer.ConfigurationException;
import uk.org.tombolo.importer.Importer;
import uk.org.tombolo.importer.utils.CoordinateUtils;

import java.io.BufferedReader;
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
 * - https://data.gov.uk/dataset/gb-road-traffic-counts/datapackage.zip
 * - http://api.dft.gov.uk/v2/trafficcounts/export/data/traffic/region/London.csv
 * - http://api.dft.gov.uk/v2/trafficcounts/export/data/traffic/region/North+East.csv
 * - http://api.dft.gov.uk/v2/trafficcounts/export/data/traffic/la/Aberdeen+City.csv
 * - http://api.dft.gov.uk/v2/trafficcounts/export/data/traffic/la/Bristol%2C+City+of.csv
 */
public class TrafficCountImporter extends AbstractDFTImporter implements Importer {
	private enum DatasourceId {trafficCounts};

	private static final String TRAFFIC_COUNTER_SUBJECT_TYPE_LABEL = "trafficCounter";
	private static final String TRAFFIC_COUNTER_SUBJECT_TYPE_DESC = "Traffic counter from Department of Transport";

	protected static enum COUNT_TYPE 
		{CountPedalCycles, CountMotorcycles, CountCarsTaxis, CountBusesCoaches, CountLightGoodsVehicles, CountHeavyGoodsVehicles};

	private static final List<String> regions = Arrays.asList("East Midlands","East of England","London",
			"Merseyside","North East","North West","Scotland","South East","South West",
			"Wales","West Midlands","Yorkshire and The Humber");

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

	public TrafficCountImporter() {
		super();
		datasourceIds = stringsFromEnumeration(DatasourceId.class);
		geographyLabels = new ArrayList<>(regions);
		geographyLabels.addAll(localAuthorities);
	}

	@Override
	public Datasource getDatasource(String datasourceId) throws Exception {

		Datasource datasource = new Datasource(
				getClass(),
				datasourceId,
				getProvider(), 
				"Traffic Counts",
				"Traffic Counts from Department for Transport.");

		// Update attribute list
		datasource.addAllTimedValueAttributes(getAttributes());
		datasource.setUrl("http://www.dft.gov.uk/traffic-counts/");

		datasource.addSubjectType(new SubjectType(TRAFFIC_COUNTER_SUBJECT_TYPE_LABEL, TRAFFIC_COUNTER_SUBJECT_TYPE_DESC));

		return datasource;
	}

	@Override
	protected void importDatasource(Datasource datasource, List<String> geographyScope, List<String> temporalScope) throws Exception {

		if (geographyScope == null || geographyScope.isEmpty())
			throw new ConfigurationException("Missing geography scope");

		for (String geogrpahyLabel : geographyScope) {

			URL url = new URL(getTrafficCountUrl(geogrpahyLabel));

			// Read timed values
			GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), Subject.SRID);
			Set<Long> trafficCounters = new HashSet<Long>();
			BufferedReader reader = new BufferedReader(new InputStreamReader(downloadUtils.fetchCSVStream(url)));
			String line = null;
			List<TimedValue> timedValueBuffer = new ArrayList<TimedValue>();
			while ((line = reader.readLine()) != null) {
				String[] fields = line.split("\",\"");

				if (fields.length == 1)
					continue;

				long id = Long.valueOf(fields[1].replaceAll("\"", ""));
				String label = "DfT-TrafficCounter-" + id;
				String year = fields[0].replaceAll("\"", "");

			// Save subject object
			if (!trafficCounters.contains(id)){
				// We have not seen this id before
				long easting = Long.valueOf(fields[6].replaceAll("\"", ""));
				long northing = Long.valueOf(fields[7].replaceAll("\"", ""));
				String localAuthority = fields[3].replaceAll("\"", "");
				String road = fields[4].replaceAll("\"", "");
				String roadCategory = fields[5].replaceAll("\"", "");
				String startJunction = fields[8].replaceAll("\"", "");
				String endJunction = fields[9].replaceAll("\"", "");

					Coordinate coordinate = CoordinateUtils.osgbToWgs84(easting, northing);
					Point point = geometryFactory.createPoint(coordinate);

					String name = road + " (" + startJunction + " to " + endJunction + ")";

					// FIXME: Add fixed values

					Subject subject = new Subject(datasource.getUniqueSubjectType(), label, name, point);
					List<Subject> subjectList = new ArrayList<Subject>();
					subjectList.add(subject);
					SubjectUtils.save(subjectList);
					trafficCounters.add(id);
					subjectCount++;
				}

				Subject subject = SubjectUtils.getSubjectByLabel(label);
				LocalDateTime timestamp = TimedValueUtils.parseTimestampString(year);

			// Pedal cycles
			Attribute pcAttribute = AttributeUtils.getByProviderAndLabel(getProvider(), COUNT_TYPE.CountPedalCycles.name());
			double pcCount = Double.valueOf(fields[11].replaceAll("\"", ""));
			TimedValue pedalCycleCount = new TimedValue(subject, pcAttribute, timestamp, pcCount);
			timedValueBuffer.add(pedalCycleCount);
			timedValueCount++;

			// Motorcycles
			Attribute mcAttribute = AttributeUtils.getByProviderAndLabel(getProvider(), COUNT_TYPE.CountMotorcycles.name());
			double mcCount = Double.valueOf(fields[12].replaceAll("\"", ""));
			TimedValue motorcycleCount = new TimedValue(subject, mcAttribute, timestamp, mcCount);
			timedValueBuffer.add(motorcycleCount);
			timedValueCount++;

			// Cars & taxis
			Attribute ctAttribute = AttributeUtils.getByProviderAndLabel(getProvider(), COUNT_TYPE.CountCarsTaxis.name());
			double ctCount = Double.valueOf(fields[13].replaceAll("\"", ""));
			TimedValue carTaxiCount = new TimedValue(subject, ctAttribute, timestamp, ctCount);
			timedValueBuffer.add(carTaxiCount);
			timedValueCount++;

			// Buses and Coaches
			Attribute bcAttribute = AttributeUtils.getByProviderAndLabel(getProvider(), COUNT_TYPE.CountBusesCoaches.name());
			double bcCount = Double.valueOf(fields[14].replaceAll("\"", ""));
			TimedValue busCoachCount = new TimedValue(subject, bcAttribute, timestamp, bcCount);
			timedValueBuffer.add(busCoachCount);
			timedValueCount++;

			// Light Goods Vehicles
			Attribute lgvAttribute = AttributeUtils.getByProviderAndLabel(getProvider(), COUNT_TYPE.CountLightGoodsVehicles.name());
			double lgvCount = Double.valueOf(fields[15].replaceAll("\"", ""));
			TimedValue lightGoodsVehicleCount = new TimedValue(subject, lgvAttribute, timestamp, lgvCount);
			timedValueBuffer.add(lightGoodsVehicleCount);
			timedValueCount++;

			// Heavy Goods Vehicles
			Attribute hgvAttribute = AttributeUtils.getByProviderAndLabel(getProvider(), COUNT_TYPE.CountHeavyGoodsVehicles.name());
			double hgvCount = Double.valueOf(fields[22].replaceAll("\"", ""));
			TimedValue heavyGoodsVehicleCount = new TimedValue(subject, hgvAttribute, timestamp, hgvCount);
			timedValueBuffer.add(heavyGoodsVehicleCount);
			timedValueCount++;

				if (timedValueBuffer.size() > BUFFER_THRESHOLD)
					saveBuffer(timedValueBuffer, timedValueCount);

			}
			saveBuffer(timedValueBuffer, timedValueCount);
			reader.close();
		}
	}

	protected String getTrafficCountUrl(String geographyLabel) throws ConfigurationException {
		String remoteId = geographyLabel.replaceAll(" ", "+").replaceAll(",", "%2C");

		if (regions.contains(geographyLabel)){
			return "http://api.dft.gov.uk/v2/trafficcounts/export/data/traffic/region/"+remoteId+".csv";
		}else if (localAuthorities.contains(geographyLabel)){
			return "http://api.dft.gov.uk/v2/trafficcounts/export/data/traffic/la/"+remoteId+".csv";
		}
		throw new ConfigurationException("Unknown Geography Scope: " + geographyLabel);
	}

	private List<Attribute> getAttributes(){
		List<Attribute> attributes = new ArrayList<Attribute>();
		
		attributes.add(new Attribute(getProvider(), COUNT_TYPE.CountPedalCycles.name(), "Pedal Cycle Count", "Pedal Cycle Count", Attribute.DataType.numeric));
		attributes.add(new Attribute(getProvider(), COUNT_TYPE.CountMotorcycles.name(), "Motorcycle Count", "Motorcycle Count", Attribute.DataType.numeric));
		attributes.add(new Attribute(getProvider(), COUNT_TYPE.CountCarsTaxis.name(), "Car and Taxi Count", "Car and Taxi Count", Attribute.DataType.numeric));
		attributes.add(new Attribute(getProvider(), COUNT_TYPE.CountBusesCoaches.name(), "Bus and Coach Count", "Bus and Coach Count", Attribute.DataType.numeric));
		attributes.add(new Attribute(getProvider(), COUNT_TYPE.CountLightGoodsVehicles.name(), "Light Goods Vehicle Count", "Light Goods Vehicle Count", Attribute.DataType.numeric));
		attributes.add(new Attribute(getProvider(), COUNT_TYPE.CountHeavyGoodsVehicles.name(), "Heavy Goods Vehicle Count", "Heavy Goods Vehicle Count", Attribute.DataType.numeric));
		
		return attributes;
	}
}
