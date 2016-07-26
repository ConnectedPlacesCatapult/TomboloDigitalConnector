package uk.org.tombolo.importer.dft;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.*;
import uk.org.tombolo.core.utils.*;
import uk.org.tombolo.importer.AbstractImporter;
import uk.org.tombolo.importer.Importer;
import uk.org.tombolo.importer.utils.CoordinateUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 
 * 
 * - https://data.gov.uk/dataset/gb-road-traffic-counts/datapackage.zip
 * - http://api.dft.gov.uk/v2/trafficcounts/export/data/traffic/region/London.csv
 * - http://api.dft.gov.uk/v2/trafficcounts/export/data/traffic/region/North+East.csv
 * - http://api.dft.gov.uk/v2/trafficcounts/export/data/traffic/la/Aberdeen+City.csv
 * - http://api.dft.gov.uk/v2/trafficcounts/export/data/traffic/la/Bristol%2C+City+of.csv
 */
public class TrafficCountImporter extends AbstractImporter implements Importer {
	public static final Provider PROVIDER = new Provider(
			"uk.gov.dft",
			"Department for Transport"
			);
	
	protected static enum COUNT_TYPE 
		{CountPedalCycles, CountMotorcycles, CountCarsTaxis, CountBusesCoaches, CountLightGoodsVehicles, CountHeavyGoodsVehicles};
		
	private static final String[] REGIONS = {"East Midlands","East of England","London",
			"Merseyside","North East","North West","Scotland","South East","South West",
			"Wales","West Midlands","Yorkshire and The Humber"};
	private static List<String> regions;
	
	private static final String[] LOCAL_AUTHORITIES = {"Aberdeen City","Aberdeenshire",
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
			"Wrexham","York"};
	private static List<String> localAuthorities;
	
	private static final Logger log = LoggerFactory.getLogger(TrafficCountImporter.class);

	public TrafficCountImporter() {
		if (regions == null)
			regions = Arrays.asList(REGIONS);
		if (localAuthorities == null)
			localAuthorities = Arrays.asList(LOCAL_AUTHORITIES);
	}

	@Override
	protected String getCacheKeyForDatasourceId(String datasourceId) {
		return getClass().getCanonicalName() + "@" + datasourceId;
	}

	@Override
	public Provider getProvider() {
		return PROVIDER;
	}

	@Override
	public List<Datasource> getAllDatasources() throws Exception {
		List<Datasource> datasources = new ArrayList<Datasource>();
		
		for (String region : regions){
			datasources.add(getDatasource(region));
		}
		for (String localAuthority : localAuthorities){
			datasources.add(getDatasource(localAuthority));
		}
		return datasources;
	}

	@Override
	public Datasource getDatasource(String datasourceId) throws Exception {

		if (!regions.contains(datasourceId) && !localAuthorities.contains(datasourceId))
			return null;
		
		Datasource datasource = new Datasource(
				datasourceId,
				getProvider(), 
				"Traffic Counts for "+datasourceId, 
				"Traffic Counts for "+datasourceId);

		// Update attribute list
		datasource.addAllAttributes(getAttributes());
		
		// Update links to local and remote files
		String remoteId = datasourceId.replaceAll(" ", "+").replaceAll(",", "%2C");
		String localId = datasourceId.replaceAll(" ", "_").replaceAll(",", "_");
		
		if (regions.contains(datasourceId)){
			datasource.setUrl("http://www.dft.gov.uk/traffic-counts/");
			datasource.setLocalDatafile("dft/traffic/region/"+localId+".csv");
			datasource.setRemoteDatafile("http://api.dft.gov.uk/v2/trafficcounts/export/data/traffic/region/"+remoteId+".csv");
		}else if (localAuthorities.contains(datasourceId)){
			datasource.setUrl("http://www.dft.gov.uk/traffic-counts/");
			datasource.setLocalDatafile("dft/traffic/la/"+localId+".csv");
			datasource.setRemoteDatafile("http://api.dft.gov.uk/v2/trafficcounts/export/data/traffic/la/"+remoteId+".csv");			
		}
		
		return datasource;
	}

	@Override
	protected int importDatasource(Datasource datasource) throws Exception {
		
		// Save provider
		ProviderUtils.save(datasource.getProvider());
		
		// Save attributes
		AttributeUtils.save(datasource.getAttributes());
		
		// Read timed values
		GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), Subject.SRID);
		SubjectType sensorType = SubjectTypeUtils.getSubjectTypeByLabel("sensor");
		Set<Long> trafficCounters = new HashSet<Long>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(downloadUtils.getDatasourceFile(datasource)), "utf8"));
		String line = null;
		int valueCounter = 0;
		while((line = reader.readLine()) != null){
			String[] fields = line.split("\",\"");
			
			if (fields.length == 1)
				continue;
			
			long id = Long.valueOf(fields[1].replaceAll("\"", ""));
			String label = "DfT-TrafficCounter-"+id;
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
				
				String name = road+" ("+startJunction+" to "+endJunction+")";
				
				Subject subject = new Subject(sensorType, label, name, point);
				List<Subject> subjectList = new ArrayList<Subject>();
				subjectList.add(subject);
				SubjectUtils.save(subjectList);
			}
			
			Subject subject = SubjectUtils.getSubjectByLabel(label);
			LocalDateTime timestamp = TimedValueUtils.parseTimestampString(year);

			// Pedal cycles
			Attribute pcAttribute = AttributeUtils.getByProviderAndLabel(getProvider(), COUNT_TYPE.CountPedalCycles.name());
			double pcCount = Double.valueOf(fields[12].replaceAll("\"", ""));
			TimedValue pedalCycleCount = new TimedValue(subject, pcAttribute, timestamp, pcCount);
			TimedValueUtils.save(pedalCycleCount);
			valueCounter++;

			// Motorcycles
			Attribute mcAttribute = AttributeUtils.getByProviderAndLabel(getProvider(), COUNT_TYPE.CountMotorcycles.name());
			double mcCount = Double.valueOf(fields[13].replaceAll("\"", ""));
			TimedValue motorcycleCount = new TimedValue(subject, mcAttribute, timestamp, mcCount);
			TimedValueUtils.save(motorcycleCount);
			valueCounter++;

			// Cars & taxis
			Attribute ctAttribute = AttributeUtils.getByProviderAndLabel(getProvider(), COUNT_TYPE.CountCarsTaxis.name());
			double ctCount = Double.valueOf(fields[14].replaceAll("\"", ""));
			TimedValue carTaxiCount = new TimedValue(subject, ctAttribute, timestamp, ctCount);
			TimedValueUtils.save(carTaxiCount);
			valueCounter++;

			// Buses and Coaches
			Attribute bcAttribute = AttributeUtils.getByProviderAndLabel(getProvider(), COUNT_TYPE.CountBusesCoaches.name());
			double bcCount = Double.valueOf(fields[15].replaceAll("\"", ""));
			TimedValue busCoachCount = new TimedValue(subject, bcAttribute, timestamp, bcCount);
			TimedValueUtils.save(busCoachCount);
			valueCounter++;

			// Light Goods Vehicles
			Attribute lgvAttribute = AttributeUtils.getByProviderAndLabel(getProvider(), COUNT_TYPE.CountLightGoodsVehicles.name());
			double lgvCount = Double.valueOf(fields[16].replaceAll("\"", ""));
			TimedValue lightGoodsVehicleCount = new TimedValue(subject, lgvAttribute, timestamp, lgvCount);
			TimedValueUtils.save(lightGoodsVehicleCount);
			valueCounter++;

			// Heavy Goods Vehicles
			Attribute hgvAttribute = AttributeUtils.getByProviderAndLabel(getProvider(), COUNT_TYPE.CountLightGoodsVehicles.name());
			double hgvCount = Double.valueOf(fields[23].replaceAll("\"", ""));
			TimedValue heavyGoodsVehicleCount = new TimedValue(subject, hgvAttribute, timestamp, hgvCount);
			TimedValueUtils.save(heavyGoodsVehicleCount);
			valueCounter++;
		}
		reader.close();
		
		return valueCounter;
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
