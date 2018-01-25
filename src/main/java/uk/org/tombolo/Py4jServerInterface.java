package uk.org.tombolo;

import uk.org.tombolo.core.Provider;

/*
Python module Importer implements this interface,
which allows two way communication with from 
python to jvm and jvm to python
*/
public interface Py4jServerInterface {
    
    public void streamData(String data);
    
}
