# Helper class to build importers for Digital Connector using python
# Dependencies:
#   py4j: pip install py4j
#   Digital Connector: clone it from https://github.com/FutureCitiesCatapult/TomboloDigitalConnector.git
#   Setup Digital Connector by following instruction given on https://github.com/FutureCitiesCatapult/TomboloDigitalConnector
# How it works:
#   Py4j opens a tcp connection with jvm which allows the exchange of objects between python and java
# Note: Please make sure you compile DigitalConnector before using this Python Script

from recipe import is_of_type, is_list_object, all_same_type
from py4j.java_gateway import JavaGateway, GatewayParameters, CallbackServerParameters
from pathlib import Path
import threading
import subprocess as sp

# Getting path of user home directory
home_dir = str(Path.home())
server_started = False
gateway = None

class Provider(object):
    """Creates a Provider Object

    Args:
        `label`: A string label, which represents from where the data is coming,
            e.g:
                `uk.gov.dft` in case Department for Transport is providing the data
        `name`: A more user friendly description of label.
            e.g:
                `Department for Transport` as name for label `uk.gov.dft`
    
    Methods:
        `to_java_provider`: Coverts a python object to a Java object
    """
    def __init__(self, label, name):
        self._label = label
        self._name = name

    def to_java_provider(self):
        global gateway
        return gateway.jvm.uk.org.tombolo.core.Provider(self._label, self._name)


class Attribute(object):
    """Creates a Attribute Object

    Args:
        `provider`: A python provider object
        `label`: A string from datasource that user of the importer can use to, 
                a value, in conjunction with Subjects.
            e.g:
                `NO2 40 ug/m3 as an annual mean`, which user could provide in a 
                recipe file in order to fetch values related for `NO2`, 
                `40 ug/m3 as annual mean`
        `description`: A more user friendly description for label
    
    Methods:
        `to_java_attribute`: Coverts a python object to a Java object
    """
    def __init__(self, provider, label, description):
        is_of_type(Provider, provider)

        self._provider = provider
        self._label = label
        self._description = description

    def to_java_attribute(self):
        global gateway
        return gateway.jvm.uk.org.tombolo.core.Attribute(self._provider.to_java_provider(), 
                self._label, self._description)

class SubjectType(object):
    """Creates a SubjectType Object

    Args:
        `label`: A String that could represent the type of all Subjects
        `name`: A more user friendly description
        `provider`: A python object of Provider type
    
    Methods:
        `to_java_subject_type`: Coverts a python object to a Java object
    """
    def __init__(self, provider, label, name):
        is_of_type(Provider, provider)

        self._provider = provider
        self._label = label
        self._name = name

    def to_java_subject_type(self):
        global gateway
        return gateway.jvm.uk.org.tombolo.core.SubjectType(self._provider.to_java_provider(),
                self._label, self._name)

class Subject(object):
    """Creates a Subject Object

    Args:
        `subject_type`: A python object of type SubjectType
        `label`: Usually a code for geography, 
            e.g:
                for local authority Barnet, label = E09000003 
        `name`: Usually an actual name of geography,
            e.g:
                for local authority label = E09000003, name would be `Barnet`
        `shape`: Actual dimensions of the geography, it could be point, lines, 
                polygons or multipolygons
    
    Methods:
        `to_java_subject`: Coverts a python object to a Java object
    """
    def __init__(self, subject_type, label, name, shape):
        is_of_type(SubjectType, subject_type)

        self._subject_type = subject_type
        self._label = label
        self._name = name
        self._shape = shape
    
    def to_java_subject(self):
        global gateway
        return gateway.jvm.uk.org.tombolo.core.Subject(self._subject_type.to_java_subject_type(),
            self._label, self._name, self._shape.to_java_geometry())

class Geometry(object):
    """Creates a Geometry Object

    Args:
        `latitude`: Latitude value as a String
        `longitude`: Longitude value as a String
    
    Methods:
        `to_java_geometry`: Coverts a python object to a Java object
    """
    def __init__(self, latitude, longitude):
        self._latitute = latitude
        self._longitute = longitude

    def to_java_geometry(self):
        global gateway
        geometry = gateway.jvm.com.vividsolutions.jts.geom.Geometry
        coordinates = gateway.jvm.com.vividsolutions.jts.geom.Coordinate(gateway.jvm.java.lang.Double.parseDouble(self._longitute), 
                        gateway.jvm.java.lang.Double.parseDouble(self._latitute))
        percision_model = gateway.jvm.com.vividsolutions.jts.geom.PrecisionModel()
        srid = gateway.jvm.int
        srid = 4326
        geo_factory = gateway.jvm.com.vividsolutions.jts.geom.GeometryFactory(percision_model, srid)
        geometry = geo_factory.createPoint(coordinates)
        return geometry

class TimedValue(object):
    """Creates a TimedValue Object

    Args:
        `subject`: A python object of type Subject
        `attribute`: A python object of type Attribute
        `timestamp`: A timestamp at which the value was recorded
        `value`: An acutal value for that subject and attribute
    
    Methods:
        `to_java_timed_value`: Coverts a python object to a Java object
    """
    def __init__(self, subject, attribute, timestamp, value):
        is_of_type(Subject, subject)
        is_of_type(Attribute, attribute)

        self._subject = subject
        self._attribute = attribute
        self._timestamp = timestamp
        self._value = value

    def to_java_timed_value(self):
        global gateway
        return gateway.jvm.uk.org.tombolo.core.TimedValue(self._subject.to_java_subject(),
            self._attribute.to_java_attribute(), gateway.jvm.uk.org.tombolo.core.utils.TimedValueUtils.parseTimestampString(self._timestamp), 
            gateway.jvm.java.lang.Double.parseDouble(self._value))

class FixedValue(object):
    """Creates a FixedValue Object

    Args:
        `subject`: A python object of type Subject
        `attribute`: A python object of type Attribute
        `value`: An acutal value for that subject and attribute
    
    Methods:
        `to_java_fixed_value`: Coverts a python object to a Java object
    """
    def __init__(self, subject, attribute, value):
        is_of_type(Subject, subject)
        is_of_type(Attribute, attribute)

        self._subject = subject
        self._attribute = attribute
        self._value = value

    def to_java_fixed_value(self):
        global gateway
        return gateway.jvm.uk.org.tombolo.core.FixedValue(self._subject.to_java_subject(),
            self._attribute.to_java_attribute(), self._value)
    

class AbstractImporter(object):
    """Creates AbstractImporter class object

    Args:
        `tombolo_path`: Path of the TomboloDigitalConnector Project
        `print_data`: Print the dataset sent by DigitalConnector callback
    """
    def __init__(self, tombolo_path, print_data=False):
        global gateway
        self._tombolo_path = tombolo_path
        self._print_data = print_data
        self._data = None
        self.start_server()
        gateway = self.gateway_obj()

    class Java:
        """Py4j Server Callback

            This class implements an interface declared in DigitalConnector, 
            to initiate communication from Java to Python
        """
        implements = ["uk.org.tombolo.Py4jServerInterface"]

    def streamData(self, data):
        """Callback Method

            This method is the implementation of the method declared in 
            Py4jServerInterface, in order to enable communication from Java to Python
        """
        self._data = data
        if self._print_data:
            print(data)
        return self._data

    def start_server(self):
        """Starts the Py4j Server

        Creates an object for RunPy4jServer class and starts the server on a different thread.
        If the server is already running, it doesn't spin off a new connection, instead uses the 
        old one
        """
        global server_started
        if not server_started:
            run_server = RunPy4jServer(tombolo_path=self._tombolo_path)
            run_server.start()
            server_started = True
            import time
            # Giving py4j time to start the sever and start accepting connection
            time.sleep(1)
        else:
            print('Server is already running')

    def gateway_obj(self):
        """Py4j Gateway object
        
        Return JavaGateway object
        """
        gateway = JavaGateway()
        return gateway
    
    def save_provider(self, provider):
        """Saves Provider object

        Args:
            `provider`: Takes python provider class object as a parameter

        Converts the Python Provider class object to Java object
        Passes it to saveProvider method of PythonImporter in DigitalConnector.
        """
        global gateway
        is_of_type(Provider, provider)
        gateway.entry_point.saveProvider(provider.to_java_provider())

    def save_attribute(self, attributes):
        """Saves Attribute object

        Args:
            `attributes`: Takes list of python Attribute class object as a parameter

        Converts the Python list of Attribute class object to Java object
        Passes it to saveAttributes method of PythonImporter in DigitalConnector.
        """
        global gateway
        is_list_object(attributes)
        all_same_type(Attribute, attributes)

        attr_list = gateway.jvm.java.util.ArrayList()
        for attribute in attributes:
            attr_list.append(attribute.to_java_attribute())
        gateway.entry_point.saveAttributes(attr_list)

    def save_timed_values(self, timed_values):
        """Saves TimedValue object

        Args:
            `timed_values`: Takes python list of TimedValue class object as a parameter

        Converts the Python list of TimedValue class objects to Java object
        Passes it to saveAndClearTimedValueBuffer method of PythonImporter in DigitalConnector.
        """
        global gateway
        is_list_object(timed_values)
        all_same_type(TimedValue, timed_values)

        time_list = gateway.jvm.java.util.ArrayList()
        for value in timed_values:
            time_list.append(value.to_java_timed_value())
        gateway.entry_point.saveAndClearTimedValueBuffer(time_list)


    def save_fixed_values(self, fixed_values):
        """Saves FixedValue object

        Args:
            `fixed_values`: Takes python list of FixedValue class object as a parameter

        Converts the Python list of FixedValue class objects to Java object
        Passes it to saveAndClearFixedValueBuffer method of PythonImporter in DigitalConnector.
        """
        global gateway
        is_list_object(fixed_values)
        all_same_type(FixedValue, fixed_values)

        fixed_list = gateway.jvm.java.util.ArrayList()
        for value in fixed_values:
            fixed_list.append(value.to_java_fixed_value())
        gateway.entry_point.saveAndClearFixedValueBuffer(fixed_list)

    def save_subject_types(self, subject_types):
        """Saves SubjectTypes object

        Args:
            `subject_types`: Takes python list of SubjectType class object as a parameter

        Converts the Python list of SubjectType class objects to Java object
        Passes it to saveSubjectTypes method of PythonImporter in DigitalConnector.
        """
        global gateway
        is_list_object(subject_types)
        all_same_type(SubjectType, subject_types)

        sub_type_list = gateway.jvm.java.util.ArrayList()
        for sub_types in subject_types:
            sub_type_list.append(sub_types.to_java_subject_type())
        gateway.entry_point.saveSubjectTypes(sub_type_list)

    def save_subjects(self, subjects):
        """Saves Subjects object

        Args:
            `subjects`: Takes python list of Subject class object as a parameter

        Converts the Python list of Subject class objects to Java object
        Passes it to saveAndClearSubjectBuffer method of PythonImporter in DigitalConnector.
        """
        global gateway
        is_list_object(subjects)
        all_same_type(Subject, subjects)

        subject_list = gateway.jvm.java.util.ArrayList()
        for subject in subjects:
            subject_list.append(subject.to_java_subject())
        gateway.entry_point.saveAndClearSubjectBuffer(subject_list)


    def save(self, provider=None, attributes=None, subject_types=None, subjects=None, fixed_values=None, timed_values=None):
        """Save to Database

        Args:
            `provider`: (Optional) Takes object of Python Provider class
            `attributes`: (Optional) Takes a list of Python Attribute class object
            `subject_types`: (Optional) Takes a list of Python SubjectType class object
            `subjects`: (Optional) Takes a list of Python Subject class object
            `fixed_values`: (Optional) Takes a list of Python FixedValue class object
            `timed_values`: (Optional) Takes a list of Python TimedValue class object

        Save the objects in Hierarchy
        e.g:
            If a list of TimedValue or FixedValue object is passed, then there is no need to 
            pass provider, attributes, subject_types, subjects as list of TimedValue and FixedValue 
            contains those objects and would save those in hierarchy. Thus no need to call save 
            multiple times.
            Please check the implementation of Importers in Importers folder of this repo.
        """
        global gateway
        self.start_server()

        params = [provider, attributes, subject_types, subjects, fixed_values, timed_values]
        func = {'Provider': self.save_provider, 'Attribute': self.save_attribute, 
                'SubjectType': self.save_subject_types, 'Subject': self.save_subjects, 
                'FixedValue': self.save_fixed_values, 'TimedValue': self.save_timed_values}

        database = gateway.jvm.uk.org.tombolo.core.utils.HibernateUtil()
        database.startUpForPython()
        for p in params:
            if p is not None:
                if isinstance(p, list):
                    v = p[0]
                    func[v.__class__.__name__](p)
                else:
                    func[p.__class__.__name__](p)

        database.shutdown()
        gateway.shutdown()


class RunPy4jServer(threading.Thread):
    """Start Py4j Server

    Args:
        `tombolo_path`: Takes path of DigitalConnector as a String

    Adds all the jars to the classpath and al the class files.
    Starts the server on a different thread by calling the Py4jServer 
    class of DigitalConnector.
    """
    def __init__(self, tombolo_path):
        threading.Thread.__init__(self)
        self._tombolo_path = tombolo_path

    def run(self):
        global server_started
        if not server_started:
            jars_for_classpath = self.class_path_files()
            args = ['java', '-cp', 
            home_dir + self._tombolo_path +  'build/classes/java/main:' + 
            ':'.join(jars_for_classpath), 'uk.org.tombolo.Py4jServer']
            p = sp.Popen(args, cwd=home_dir + self._tombolo_path)
        server_started = True
    
    def class_path_files(self):
        import os, os.path
        dirs = []
        for directory_path, _, file_names in os.walk(home_dir + "/.gradle/caches/modules-2/files-2.1"):
            for file_name in [f_names for f_names in file_names if f_names.endswith(".jar")]:
                dirs.append(os.path.join(directory_path, file_name))
        return dirs





