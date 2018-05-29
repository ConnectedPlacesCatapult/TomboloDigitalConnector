import json
from pathlib import Path
import subprocess as sp
import sys
import os
import platform
import ntpath

base_dir = str(Path.home())

class Recipe(object):
    """Creates a Recipe Object

    Args: 
        `dataset`: accepts an object of Dataset class.  
        `exporter`: (Optional) accepts a String value, class name of the exporter, e.g uk.org.tombolo.exporter.GeoJsonExporter.  
        `timestamp`: (Optional) accepts boolean value of true or false.    
    """

    def __init__(self, dataset, exporter='uk.org.tombolo.exporter.GeoJsonExporter', timestamp=False):
        if not isinstance(dataset, Dataset):
            raise TypeError('dataset should be of type Dataset')

        self.dataset = dataset
        self.exporter = exporter
        self.timeStamp = timestamp

    def build_recipe(self, output_location=None, console_print=False):
        """Builds the recipe from the class object

        Args: 
            `output_location`: (Optional) If you would like to save the recipe file, pass the location.    
            `console_print`: (Optional) To print the recipe on console.    
        """
        self.recipe = json.dumps(self, indent=2, default=lambda a: a.__dict__)
        if output_location is not None:
            output_location = output_location.replace(os.sep, ntpath.sep)
            to_save = os.path.join(base_dir, output_location)
            with open(to_save, 'w') as recipe_file:
                recipe_file.write(self.recipe)
        if console_print:
            print(self.recipe)

    def run_recipe(self, tombolo_path, output_path, force_imports=None, clear_database_cache=False, gradle_path=None, console_print=True):
        """Runs the recipe directly from Python console

        Args: 
            `tombolo_path`: Path of the tombolo project.  
            `output_path`: Path of the file where you want the output to be saved.  
            `force_imports`: (Optional) If you would like to import the datasource for the importer again.    
            `clear_database_cache`: (Optional) To clear the database.    
            `gradle_path`: (Optional) If gradle path is not set in env variables, use this option to pass gradle path.
            `console_print`: (Optional) To print the output logs on Terminal
        """

        shell = False
        if platform.system() == 'Windows':
            self.recipe = self.recipe.replace('\n', '')
            self.recipe = json.dumps(self.recipe)
            tombolo_path = tombolo_path.replace(os.sep, ntpath.sep)
            output_path = output_path.replace(os.sep, ntpath.sep)
            if gradle_path is not None:
                gradle_path = gradle_path.replace(os.sep, ntpath.sep)
            shell = True

        to_save = os.path.join(base_dir, output_path)
        gradle = gradle_path if gradle_path is not None else ''
        gradle_command = os.path.join(gradle, 'gradle')
        args = [gradle_command, "runExport", "-Ps", 
                "-Precipe=" + self.recipe, "-Poutput=" + to_save]
        if force_imports is not None:
            is_of_type(Datasource, force_imports, 'The parameter expects Datasource Object')
            args.append("-Pforce=" + force_imports.importerClass)
        if clear_database_cache:
            args.append("-Pclear=true")
        output = sp.Popen(args, stdout=sp.PIPE, cwd=os.path.join(base_dir, tombolo_path), shell=shell)

        if console_print:
            for log in iter(output.stdout.readline, b''):
                sys.stdout.write(str(log) + '\n')

        output.communicate()[0]
        if output.returncode != 0:
            if platform.system() == 'Windows':
                sys.exit(1)
            print('Program quit with an error!!!')
        else:
            print('Execution completed Successfully!!!!')
        output.stdout.close()


class Dataset(object):
    """Creates a Dataset Object

    Args: 
        `subjects`: accepts list of objects of type Subject.   
        `datasources`: accepts list of objects of type Datasource.  
        `fields`: accepts list of objects of type Field.    
    """

    def __init__(self, subjects, datasources, fields):
        all_same_type(list, [subjects, datasources, fields], 'subjects, datasources and fields should be list')
        all_same_type(Subject, subjects, 'subjects should be of type Subject')
        all_same_type(Datasource, datasources, 'datasources should be of type Datasource')
        all_same_type(Field, fields, 'fields should be a type Field')

        self.subjects = subjects
        self.datasources = datasources
        self.fields = fields

    def __str__(self):
        return print_(self)


    def build_recipe(self, recipe_output_location=None, console_print=False):
        """Builds the recipe from the class object

        Args: 
            `recipe_output_location`: (Optional) If you would like to save the recipe file, pass the location.    
            `console_print`: (Optional) To print the recipe on console.    
        """

        recipe = Recipe(self)
        recipe.build_recipe(output_location=recipe_output_location, console_print=console_print)

    def build_and_run(self, tombolo_path, model_output_location, force_imports=None, 
                      clear_database_cache=False, gradle_path=None, model_output_console_print=True,
                      recipe_output_location=None, recipe_console_print=False):
        """Runs the recipe directly from Python console

        Args: 
            `tombolo_path`: Path of the tombolo project.  
            `model_output_location`: Path of the file where you want the output to be saved.  
            `force_imports`: (Optional) If you would like to import the datasource for the importer again.    
            `clear_database_cache`: (Optional) To clear the database.    
            `gradle_path`: (Optional) If gradle path is not set in env variables, use this option to pass gradle path.
            `recipe_output_location`: (Optional) If you would like to save the recipe file, pass the location.
            `recipe_console_print`: (Optional) To print the output logs on Terminal
            `model_output_console_print`: (Optional) To print the output logs on Terminal
        """

        recipe = Recipe(self)
        recipe.build_recipe(output_location=recipe_output_location, console_print=recipe_console_print)
        recipe.run_recipe(tombolo_path=tombolo_path, output_path=model_output_location, force_imports=force_imports,
                          clear_database_cache=clear_database_cache, gradle_path=gradle_path, 
                          console_print=model_output_console_print)


class AttributeMatcher(object):
    """Creates a AttributeMatcher Object

    Args: 
        `provider`: accepts String value, every importer has its own provider, for more info visit github.   
        `label`: accepts a String value.  
        `values`: (Optional) accepts List of String values.    
    """

    def __init__(self, provider, label, values=[]):
        self.provider = provider
        self.label = label
        is_list_object(values, 'values should be of type list')
        if values:
            self.values = values

    def __str__(self):
        return print_(self)

class Datasource(object):
    """Creates a Datasource Object

    Args: 
        `importer_class`: accepts String value, it has to be a full package name like 
                        uk.org.tombolo.importer.dft.TrafficCountImporter.   
        `datasource_id`: accepts a String value, different importers have different datasource ids, 
                        you could check documentation to find more about them.  
        `geography_scope`: (Optional) accepts List of String values, restricts the output to that region.    
        `temporal_scope`: (Optional) accepts List of int values in form of years, restricts the output to that year.     
        `local_data`: (Optional) accepts List of String values, need to pass the absolute path of the locally saved data.
    """

    def __init__(self, importer_class, datasource_id, geography_scope=[], temporal_scope=[], local_data=[]):
        self.importerClass = importer_class
        self.datasourceId = datasource_id
        if geography_scope:
            self.geographyScope = geography_scope
        if temporal_scope:
            self.temporalScope = temporal_scope
        if local_data:
            self.localData = local_data

    def __str__(self):
        return print_(self)

class Subject(object):
    """Creates a Subject Object

    Args: 
        `subject_type_label`: accepts String value, every importer has its own subjects, visit github for more info.   
        `provider_label`: accepts a String value, every importer has its own provider, visit github for more info.  
        `match_rule`: (Optional) accepts an object of Match_Rule type.    
        `geo_match_rule`: (Optional) accepts an object of Geo_Match_Rule type.     
    """

    def __init__(self, subject_type_label, provider_label, match_rule=None, geo_match_rule=None):
        self.subjectType = subject_type_label
        self.provider = provider_label
        if match_rule is not None:
            is_of_type(Match_Rule, match_rule, 'match_rule must be of type Match_Rule')
            self.matchRule = match_rule

        if geo_match_rule is not None:
            is_of_type(Geo_Match_Rule, geo_match_rule, 'geo_match_rule must be of type Geo_Match_Rule')
            self.geoMatchRule = geo_match_rule

    def __str__(self):
        return print_(self)

class Geo_Match_Rule(object):
    """Creates a Geo_Match_Rule Object

    Args: 
        `geo_relation`: accepts String value, currently supported value is `within`.   
        `subjects`: accepts a list of object of Subject type.     
    """

    def __init__(self, geo_relation, subjects):
        self.geoRelation = geo_relation
        is_list_object(subjects, 'subjects should be list of subjects')
        all_same_type(Subject, subjects, 'subjects should be a list of subjects')
        self.subjects = subjects

    def __str__(self):
        return print_(self)

class Match_Rule(object):
    """Creates a Match_Rule Object

    Args: 
        `attribute_to_match_on`: accepts String value.   
        `pattern`: accepts a String value as a pattern which are accepted by postgresql e.g `E090%`.     
    """

    def __init__(self, attribute_to_match_on, pattern):
        self.attribute = attribute_to_match_on
        self.pattern = pattern

    def __str__(self):
        return print_(self)


class Field(object):
    """Creates a Field Object

    Args: 
        `field_class`: accepts canonical name of field class as String.   
        `label`: accepts a String value.     
    """

    def __init__(self, field_class, label):
        self.fieldClass = field_class
        if label is not None:
            self.label = label

"""
below are the field class from transformation package
"""
package_name_transformation = 'uk.org.tombolo.field.transformation.'
class AreaField(Field):
    """Creates a AreaField Object

    Args: 
        `target_crs_code`: accepts int value.   
        `label`: (Optional) accepts a String value.     
    """

    def __init__(self, target_crs_code, label=None):
        is_of_type(int, target_crs_code, 'target_crs_code should be of type int')
        
        super().__init__(field_class=package_name_transformation + 'AreaField', label=label)
        self.targetCRSCode = target_crs_code

    def __str__(self):
        return print_(self)

class ArithmeticField(Field):
    """Creates a ArithmeticField Object

    Args: 
        `operation`: accepts String value for performing mathematical operation e.g. `sum` to add different fields.   
        `operation_on_field_1`: accepts an object of Field type.     
        `operation_on_field_2`: accepts an object of Field type.     
        `label`: (Optional) accepts a String value.     
    """

    def __init__(self, operation, operation_on_field_1, operation_on_field_2, label=None):
        all_same_type(Field, [operation_on_field_1, operation_on_field_2], 
                        'operation_on_field_1 and operation_on_field_2 should be of classes that are inherited' \
                        'from Field base class')

        super().__init__(field_class=package_name_transformation + 'ArithmeticField', label=label)
        self.operation = operation
        self.field1 = operation_on_field_1
        self.field2 = operation_on_field_2

    def __str__(self):
        return print_(self)

class DescriptiveStatisticsField(Field):
    """Creates a DescriptiveStatisticsField Object

    Args: 
        `statistic`: accepts String value for performing statistic operation e.g. `mean`.   
        `fields`: accepts a list of objects of type Field.     
        `label`: (Optional) accepts a String value.     
    """

    def __init__(self, statistic, fields, label=None):
        is_list_object(fields, 'fields should of type list of Field')
        all_same_type(Field, fields, 'fields should be of type Field')

        super().__init__(field_class=package_name_transformation + 'DescriptiveStatisticsField', label=label)
        self.statistic = statistic
        self.fields = fields

    def __str__(self):
        return print_(self)

class FieldValueSumField(Field):
    """Creates a FieldValueSumField Object

    Args: 
        `name`: accepts String value.   
        `fields`: accepts a list of objects of type Field.     
        `label`: (Optional) accepts a String value.     
    """

    def __init__(self, name, fields, label=None):
        is_list_object(fields, 'fields should be an instance of list')
        all_same_type(Field, fields, 'fields should be of type Field')

        super().__init__(field_class=package_name_transformation + 'FieldValueSumField', label=label)
        self.name = name
        self.fields = fields

    def __str__(self):
        return print_(self)

class FractionOfTotalField(Field):
    """Creates a FractionOfTotalField Object

    Args: 
        `dividend_attributes`: accepts a list of object of type AttributeMatcher.   
        `divisor_attribute`: accepts an object of type AttributeMatcher.     
        `label`: (Optional) accepts a String value.     
    """

    def __init__(self, dividend_attributes, divisor_attribute, label=None):
        is_list_object(dividend_attributes)
        all_same_type(AttributeMatcher, dividend_attributes, 'dividend_attributes must be of type AttributeMatcher')
        is_of_type(AttributeMatcher, divisor_attribute, 'divisor_attribute should be of type AttributeMatcher')

        super().__init__(field_class=package_name_transformation + 'FractionOfTotalField', label=label)
        self.dividendAttributes = dividend_attributes
        self.divisorAttribute = divisor_attribute

    def __str__(self):
        return print_(self)

class LinearCombinationField(Field):
    """Creates a LinearCombinationField Object

    Args: 
        `scalars`: accepts a list of float values.   
        `fields`: accepts a list of Field objects.     
        `label`: (Optional) accepts a String value.     
    """

    def __init__(self, scalars, fields, label=None):
        is_list_object(fields)
        all_same_type(Field, fields, 'fields Should be of type Field')

        is_list_object(scalars)
        all_same_type(float, scalars, 'scalars should be of type float')

        super().__init__(field_class=package_name_transformation + 'LinearCombinationField', label=label)
        self.scalars = scalars
        self.fields = fields

    def __str__(self):
        return print_(self)

class ListArithmeticField(Field):
    """Creates a ListArithmeticField Object

    Args: 
        `operation`: accepts String value as an operation on two fields e.g `sum`.   
        `fields`: accepts a list of Field objects.     
        `label`: (Optional) accepts a String value.     
    """

    def __init__(self, operation, fields, label=None):
        is_list_object(fields)
        all_same_type(Field, fields, 'fields should be of type Field')

        super().__init__(field_class=package_name_transformation + 'ListArithmeticField', label=label)
        self.operation = operation
        self.fields = fields

    def __str__(self):
        return print_(self)

class PercentilesField(Field):
    """Creates a PercentilesField Object

    Args: 
        `name`: accepts String value.   
        `field`: accepts a Field object.     
        `subjects`: accepts a list of Subject objects.      
        `percentile_count`: accepts an int value.      
        `inverse`: accepts a boolean value.       
        `label`: (Optional) accepts a String value.     
    """

    def __init__(self, field, percentile_count, inverse, normalization_subjects=None, name=None, label=None):
        is_of_type(Field, field, 'field should be of type Field')
        is_of_type(int, percentile_count)
        is_of_type(bool, inverse)

        super().__init__(field_class=package_name_transformation + 'PercentilesField', label=label)
        self.percentileCount = percentile_count
        self.inverse = inverse
        self.field = field
        if normalization_subjects is not None:
            is_list_object(normalization_subjects)
            all_same_type(Subject, normalization_subjects, 'subjects should be of type Subject')
            self.subjects = normalization_subjects

    def __str__(self):
        return print_(self)


"""
below are the field classes from value package
"""
package_name_value = 'uk.org.tombolo.field.value.'
class BasicValueField(Field):
    """Creates a BasicValueField Object

    Args: 
        `attribute_matcher`: accepts an object of AttributeMatcher type.   
        `field_class`: accepts canonical name of Field class from DigitalConnector.     
        `label`: (Optional) accepts a String value.     
    """

    def __init__(self, attribute_matcher, field_class, label=None):
        super().__init__(field_class=field_class, label=label)
        if attribute_matcher is not None:
            is_of_type(AttributeMatcher, attribute_matcher, 'attribute_matcher should be of type AttributeMatcher')
            self.attribute = attribute_matcher

    def __str__(self):
        return print_(self)

class ConstantField(Field):
    """Creates a ConstantField Object

    Args: 
        `value`: accepts String value.   
        `label`: (Optional) accepts a String value.     
    """

    def __init__(self, value, label=None):
        super().__init__(field_class=package_name_value + 'ConstantField', label=label)
        self.value = value

    def __str__(self):
        return print_(self)

class FixedValueField(BasicValueField):
    """Creates a FixedValueField Object

    Args: 
        `attribute_matcher`: (Optional) accepts an object of AttributeMatcher type.   
        `label`: (Optional) accepts a String value.     
    """

    def __init__(self, attribute_matcher=None, label=None):
        super().__init__(attribute_matcher=attribute_matcher, field_class=package_name_value + 'FixedValueField', 
                        label=label)

    def __str__(self):
        return print_(self)

class LatestValueField(BasicValueField):
    """Creates a LatestValueField Object

    Args: 
        `attribute_matcher`: (Optional) accepts an object of AttributeMatcher type.   
        `label`: (Optional) accepts a String value.     
    """

    def __init__(self, attribute_matcher=None, label=None):
        super().__init__(attribute_matcher=attribute_matcher, field_class=package_name_value + 'LatestValueField', 
                        label=label)

    def __str__(self):
        return print_(self)

class SubjectLatitudeField(Field):
    """Creates a SubjectLatitudeField Object

    Args: 
        `label`: (Optional) accepts a String value.     
    """

    def __init__(self, label=None):
        super().__init__(field_class=package_name_value + 'SubjectLatitudeField', label=label)

    def __str__(self):
        return print_(self)

class SubjectLongitudeField(Field):
    """Creates a SubjectLongitudeField Object

    Args: 
        `label`: (Optional) accepts a String value.     
    """

    def __init__(self, label=None):
        super().__init__(field_class=package_name_value + 'SubjectLongitudeField', label=label)

    def __str__(self):
        return print_(self)

class SubjectNameField(Field):
    """Creates a SubjectNameField Object

    Args: 
        `label`: (Optional) accepts a String value.     
    """

    def __init__(self, label=None):
        super().__init__(field_class=package_name_value + 'SubjectNameField', label=label)

    def __str__(self):
        return print_(self)

class TimeseriesField(BasicValueField):
    """Creates a TimeseriesField Object

    Args: 
        `attribute_matcher`: (Optional) accepts an object of AttributeMatcher type.   
        `label`: (Optional) accepts a String value.     
    """

    def __init__(self, label=None, attribute_matcher=None):
        super().__init__(field_class=package_name_value + 'TimeseriesField', label=label, 
                        attribute_matcher=attribute_matcher)

    def __str__(self):
        return print_(self)

class TimeseriesMeanValueField(BasicValueField):
    """Creates a TimeseriesMeanValueField Object

    Args: 
        `attribute_matcher`: (Optional) accepts an object of AttributeMatcher type.   
        `label`: (Optional) accepts a String value.     
    """

    def __init__(self, label=None, attribute_matcher=None):
        super().__init__(field_class=package_name_value + 'TimeseriesMeanValueField', label=label, 
                        attribute_matcher=attribute_matcher)

    def __str__(self):
        return print_(self)


"""
below are the field classes from aggregation package
"""
package_name_aggregation = 'uk.org.tombolo.field.aggregation.'
class BackOffField(Field):
    """Creates a BackOffField Object

    Args: 
        `fields`: accepts a list of Field objects.   
        `label`: (Optional) accepts a String value.     
    """

    def __init__(self, fields, label=None):
        is_list_object(fields)
        all_same_type(Field, fields, 'fields should be of type Field')
        
        super().__init__(field_class=package_name_aggregation + 'BackOffField', label=label)
        self.fields = fields

    def __str__(self):
        return print_(self)

class GeographicAggregationField(Field):
    """Creates a GeographicAggregationField Object

    Args: 
        `subject`: accepts an object of Subject type.   
        `function`: accepts a String value as an operation to perfom on field.      
        `field`: accepts an object of Field type.     
        `label`: (Optional) accepts a String value.     
    """

    def __init__(self, subject, function, field, label=None):
        is_of_type(Subject, subject, 'subject should be of type Subject')
        is_of_type(Field, field, 'field should be of type Field')

        super().__init__(field_class=package_name_aggregation + 'GeographicAggregationField', label=label)
        self.subject = subject
        self.function = function
        self.field = field

    def __str__(self):
        return print_(self)

class MapToContainingSubjectField(Field):
    """Creates a MapToContainingSubjectField Object

    Args: 
        `subject`: accepts an object of Subject type.   
        `field`: accepts an object of Field type.     
        `label`: (Optional) accepts a String value.     
    """

    def __init__(self, subject, field, label=None):
        is_of_type(Subject, subject, 'subject should be of type Subject')
        is_of_type(Field, field, 'field should be of type Field')

        super().__init__(field_class=package_name_aggregation + 'MapToContainingSubjectField', label=label)
        self.subject = subject
        self.field = field

    def __str__(self):
        return print_(self)

class MapToNearestSubjectField(Field):
    """Creates a MapToNearestSubjectField Object

    Args: 
        `subject`: accepts an object of Subject type.   
        `max_radius`: accepts a float value.      
        `field`: accepts an object of Field type.     
        `label`: (Optional) accepts a String value.     
    """

    def __init__(self, subject, max_radius, field, label=None):
        is_of_type(Subject, subject, 'subject should be of type Subject')
        is_of_type(Field, field, 'field should be of type Field')
        is_of_type(float, max_radius, 'max_radius should be of type double')

        super().__init__(field_class=package_name_aggregation + 'MapToNearestSubjectField', label=label)
        self.maxRadius = max_radius
        self.subject = subject
        self.field = field

    def __str__(self):
        return print_(self)

"""
below are the field classes from assertion package
"""
package_name_assertion = 'uk.org.tombolo.field.assertion.'
class AttributeMatcherField(Field):
    """Creates a AttributeMatcherField Object

    Args: 
        `attributes`: accepts a list of AttributeMatcher objects.   
        `field`: accepts an object of Field type.      
        `field_class`: (Optional) accepts canonical name of the field class from Digital Connector.     
        `label`: (Optional) accepts a String value.     
    """

    def __init__(self, attributes, field, label=None, field_class='AttributeMatcherField'):
        super().__init__(field_class=package_name_assertion + field_class, label=label)

        if attributes is not None:
            is_list_object(attributes)
            all_same_type(AttributeMatcher, attributes, 'all attributes must be of type AttributeMatcher')
            self.attributes = attributes
        
        if field is not None:
            is_of_type(Field, field, 'field should be of type Field')
            self.field = field

    def __str__(self):
        return print_(self)

class OSMBuiltInAttributeMatcherField(AttributeMatcherField):
    """Creates a OSMBuiltInAttributeMatcherField Object

    Args: 
        `attributes`: (Optional) accepts a list of objects of AttributeMatcher type.   
        `field`: (Optional) accepts an object of Field type.       
        `label`: (Optional) accepts a String value.     
    """

    def __init__(self, attributes=None, field=None, label=None):
        super().__init__(attributes=attributes, field=field, label=label, 
                        field_class='OSMBuiltInAttributeMatcherField')

    def __str__(self):
        return print_(self)

"""
below are the field classes from modelling package
"""
package_name_modelling = 'uk.org.tombolo.field.modelling.'
class BasicModellingField(Field):
    """Creates a BasicModellingField Object

    Args: 
        `recipe`: accepts String value as path to the recipe.   
        `datasources`: accepts a list of objects of Datasource type.      
        `field_class`: (Optional) accepts canonical name of the field class from Digital Connector.     
        `label`: (Optional) accepts a String value.     
    """

    def __init__(self, recipe, datasources, label=None, field_class='BasicModellingField'):
        super().__init__(field_class=package_name_modelling + field_class, label=label)
        self.recipe = recipe
        if datasources is not None:
            is_list_object(datasources)
            all_same_type(Datasource, datasources, 'datasources should be of type Datasource')
            self.datasources = datasources

    def __str__(self):
        return print_(self)

class SingleValueModellingField(BasicModellingField):
    """Creates a SingleValueModellingField Object

    Args: 
        `recipe`: accepts String value as path to the recipe.   
        `datasources`: (Optional) accepts a list of objects of Datasource type.      
        `label`: (Optional) accepts a String value.     
    """

    def __init__(self, recipe, datasources=None, label=None):
        super().__init__(recipe=recipe, datasources=datasources, label=label, 
                        field_class='SingleValueModellingField')

    def __str__(self):
        return print_(self)


"""
Helper functions
"""
def is_list_object(var, error_msg='Should be an instance of list'):
    """Checks if the passed object is of type list

    Args: 
        `var`: object whose type needs to be checked.   
        `error_msg`: (Optional) user defined message.      
    Raise:
        `TypeError`
    """

    if not isinstance(var, list):
        raise TypeError(error_msg)

def all_same_type(class_name, values, error_msg='must be of different type'):
    """Checks if the passed list has the same type of objects

    Args: 
        `class_name`: class name.   
        `values`: a list of objects whose type needs to checked against class_name.      
        `error_msg`: (Optional) user defined message.     
    Raise:
        `TypeError`
    """

    if not all(isinstance(v, class_name) for v in values):
        raise TypeError(error_msg)

def is_of_type(class_name, value, error_msg='should be of different type'):
    """Checks if the passed value is of the class_name type

    Args: 
        `class_name`: class name.   
        `value`: object whose type needs to checked against class_name.      
        `error_msg`: (Optional) user defined message.     
    Raise:
        `TypeError`
    """

    if not isinstance(value, class_name):
        raise TypeError(error_msg)


def print_(object_):
    """Converts the passed object in json hirerachy 

    Args:
        `object_`: Object of the class that needs to be converted

    """
    return json.dumps(object_, indent=2, default=lambda a: a.__dict__)

