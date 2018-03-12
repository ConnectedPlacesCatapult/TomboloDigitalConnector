package uk.org.tombolo.importer.utils;

import javax.json.*;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JSONReader class is built to read any format of json file.
 * The class uses javax.json jar as @dependency.
 * it would flatten the json object and would return it as Array containing sections of json data.
 * Section: A section is a complete set of data from the outer most tag
 *          which has value to until it repeats itself within the same document
 * for example:
 *
                     {
                     "SiteObjectives": {
                     "Site": [
                         {
                             "@SiteCode": "BG1",
                             "@SiteName": "Barking and Dagenham - Rush Green",
                             "@SiteType": "Suburban",
                             "Objective": [
                            {
                                 "@SpeciesCode": "NO2",
                                 "@Achieved": "YES"
                            }
                     ]
                     },
                        {
                             "@SiteCode": "BG2",
                             "@SiteName": "Barking and Dagenham - Scrattons Farm",
                             "@SiteType": "Suburban",
                             "Objective": [
                            {
                                 "@SpeciesCode": "DUST",
                                 "@Achieved": "YES"
                            }
                                ]
                        }
                     }
                     }

 * The output of the above parsed json object would be an ArrayList object containing LinkedHashMap of String (for keys)
 * and List<String> for values of the key and every LinkedHashMap object would be section all by itself.
 *
 * For above example it would be:
 *
 * Section 1: Key                        Value
 *            @SiteCode                  [BG1]
 *            @SiteName                 [Barking and Dagenham - Rush Green]
 *            @SiteType                 [Suburban]
 *            @SpeciesCode              [NO2]
 *            @Achieved                 [Yes]
 *
 *
 * Section 2: Key                        Value
 *            @SiteCode                  [BG2]
 *            @SiteName                 [Barking and Dagenham - Scrattons Farm]
 *            @SiteType                 [Suburban]
 *            @SpeciesCode              [Dust]
 *            @Achieved                 [Yes]
 *
 * If there are multiple tags in the same section then there values will be appended in the single tag
 */
public class JSONReader {


    private InputStream is;

    private List<String> tags = new ArrayList<>();

    private final static String EMPTY_KEY = null;

    private String primaryNode = null;

    private ArrayList<LinkedHashMap<String, List<String>>> flatJsonTree
            = new ArrayList<>();

    private LinkedHashMap<String, List<String>> individualSectionOfTree
            = new LinkedHashMap<>();


    /*
     *   Constructors to accept request in different formats
     */


    /*
     * Constructor to accept url in @type String format.
     * @Param url of @type String
     */

    public JSONReader(String url) throws IOException {
        this(new URL(url));
    }


    /*
     * Constructor accepts @param @type URL url
     */

    public JSONReader(URL url) throws IOException {
        this(url, new ArrayList<>());
    }


    /*
     * Constructor accepts @param @type File file
     */

    public JSONReader(File file) throws FileNotFoundException {
        this(file, new ArrayList<>());

    }


    /*
     * Constructor accepts @param @type File file
     * @Param ArrayList<String> tags
     */

    public JSONReader(File file, ArrayList<String> tags) throws FileNotFoundException {
        this(new FileInputStream(file), tags);
    }


    /*
     * Constructor accepts @param @Type InputStream is
     */

    public JSONReader(InputStream is) {
        this(is, new ArrayList<>());
    }


    /*
     * Constructor accepts @params url and tags of @type String and ArrayList<String>
     */

    public JSONReader(String url, List<String> tags) throws IOException {
        this(new URL(url), tags);
    }

    /*
     * Constructor accepts @params url and tags of @type URL and ArrayList<String>
     */

    public JSONReader(URL url, List<String> tags) throws IOException {
        this(url.openStream(), tags);
    }


    /*
     * Constructor accepts @params is and tags of @type InputStream and ArrayList<String>
     */

    public JSONReader(InputStream is, List<String> tags){
        this.is = is;
        this.tags = tags;
    }

    public ArrayList<LinkedHashMap<String, List<String>>> getFlatJsonTree() {
        return flatJsonTree;
    }

    public void setFlatJsonTree(ArrayList<LinkedHashMap<String, List<String>>> flatJsonTree) {
        this.flatJsonTree = flatJsonTree;
    }

    public String getPrimaryNode() {
        return primaryNode;
    }

    public void setPrimaryNode(String primaryNode) {
        this.primaryNode = primaryNode;
    }


    /*
     * The method is responsible for return the flat structure of JSON file to the caller
     * It does it by passing the incoming stream to @method convertTreeToHashMap
     * The method @return ArrayList of LinkedHashMap that contain the json file divided into sections
     */

    public ArrayList<LinkedHashMap<String, List<String>>> getData() {

        JsonReader reader = Json.createReader(is);
        JsonValue value = reader.read();
        convertTreeToHashMap(value, EMPTY_KEY);
        getFlatJsonTree().add(individualSectionOfTree);
        setFlatJsonTree(flatJsonTree);

        return getFlatJsonTree();
    }


    /*
     * The method implements a recursive approach in order to traverse the JSON file tree
     * Its identifies different Json object types like an Object or Array and recursively calls
     * itself until it finds a tag with a value and passes that data to @method createFlatStructure()
     * @params value and key of @type JsonValue and String
     */

    private void convertTreeToHashMap(JsonValue value, String key) {
        switch (value.getValueType()) {

            case OBJECT:
                JsonObject object = (JsonObject) value;
                for (String k : object.keySet()) convertTreeToHashMap(object.get(k), k);
                break;
            case ARRAY:
                JsonArray array = (JsonArray) value;
                for (JsonValue j : array) convertTreeToHashMap(j, key);
                break;
            case STRING:
                JsonString string = (JsonString) value;
                if (key != null) createFlatStructure(key, string.getString());
                break;
            case NUMBER:
                JsonNumber num = (JsonNumber) value;
                if (key != null) createFlatStructure(key, num.toString());
                break;
            case NULL:
                // Log the type of the object
                break;

        }

    }


    /*
     * Post receiving the value and key from @method convertTreeToHashMap()
     * it keeps track of all the processed nodes and add new ones.
     * @params key and value of @type String and String
     */

    private void createFlatStructure (String key, String value) {

        if (key.equalsIgnoreCase(getPrimaryNode())) {
            getFlatJsonTree().add(individualSectionOfTree);
            individualSectionOfTree = new LinkedHashMap<>();
        }

        if (tags.size() > 0 && !tags.contains(key)) return;
        if (!individualSectionOfTree.containsKey(key)) individualSectionOfTree.put(key, new ArrayList<>());

        individualSectionOfTree.get(key).add(value);

        if (primaryNode == null) setPrimaryNode(key);

    }


    /*
     * @Returns whether a particular tag contains multiple values or not in a section
     * If it does then it mean that the tag is repeated in the section
     * @params tagName of @type String
     */

    public boolean containsMoreThanOneValues(String tagName) {
        return getFlatJsonTree().stream().anyMatch(sections -> sections.get(tagName).size() > 1);
    }


    /*
     * @Returns values of a tag from every section.
     * @params tagName of @type String
     */

    public ArrayList<String> getTagValueFromAllSections (String tagName) {
        ArrayList<String> values = new ArrayList<>();

        getFlatJsonTree().forEach(sections -> sections.keySet().stream()
                .filter(key -> key.equalsIgnoreCase(tagName))
                .map(sections::get).forEachOrdered(values::addAll));

        return values;
    }


    /*
     * @Returns value of tag positioned in specific section
     * @params tagName (tag whose value needs to be fetched) of @type String
     * @params index (index of the section) of @type int
     */

    public ArrayList<String> getTagValueOfSpecificSection (String tagName, int index) {
        ArrayList<String> values = new ArrayList<>();
        LinkedHashMap<String, List<String>> sections = getFlatJsonTree().get(index);

        sections.keySet().stream()
                .filter(key -> key.equalsIgnoreCase(tagName))
                .map(sections::get).forEachOrdered(values::addAll);

        return values;
    }


    /*
     * @Returns all unique keys exists in a Json document
     */

    public ArrayList<String> allUniquekeys() {
        return getFlatJsonTree().stream()
                .flatMap(sections -> sections.keySet().stream())
                .distinct().collect(Collectors.toCollection(ArrayList::new));

    }


    /*
     * @Returns value of a tag from all sections where a value of another tag matches.
     *
     * For example:
     * if user needs to find value of tag xyz where value of tag abc is London.
     * conditionalResults(get=xyz, where=abc, equals=London)
     *
     * @params get, where, equals of @type String
     * @Returns List of Lists of matching values from every section
     */

    public List<List<String>> conditionalResults(String get, String where, String equals) {
        List<List<String>> results = new ArrayList<>();

        getFlatJsonTree().forEach(sections -> sections.keySet().stream()
                .filter(key -> key.equalsIgnoreCase(where) &&
                        sections.get(key).get(0).equalsIgnoreCase(equals))
                .filter(key -> sections.containsKey(get)).map(key -> sections.get(get))
                .forEachOrdered(results::add));

        return results;
    }

}
