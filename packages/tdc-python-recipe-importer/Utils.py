import urllib.request as url
import hashlib
from pathlib import Path


class Utils(object):
    def __init__(self):
        pass
    
    @classmethod
    def download_data(self, data_url, suffix, data_cache_directory='/tmp'):
        if Path(data_url).is_file():
            print('Reading from Local Data Store', data_url)
            with open(data_url, 'r') as data:
                return data.read()
        
        _data = None
        encode_url = hashlib.md5(data_url.encode())
        local_dataset = Path(data_cache_directory + '/TomboloData/' + encode_url.hexdigest() + '.' + suffix)
        if local_dataset.is_file():
            print('Reading from Local Data Store', local_dataset)
            with open(local_dataset, 'r') as data:
                _data = data.read()
        else:
            print('Downloading data from', data_url)
            response_obj = url.urlopen(data_url)
            _data = response_obj.read()
            save_data = open(local_dataset, 'wb')
            save_data.write(_data)
            response_obj.close()
            save_data.close()
        return _data