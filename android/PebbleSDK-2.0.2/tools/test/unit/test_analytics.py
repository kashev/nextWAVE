import contextlib
import imp
import os
import random
import re
import shutil
import sys
import tempfile
import unittest
import urllib2
import urlparse
import argparse
import pprint
import sh
import platform
from mock import patch, MagicMock


# Allow us to run even if not at the root libpebble directory.
root_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), os.pardir, 
                                        os.pardir))
sys.path.insert(0, root_dir) 
import pebble.PblAnalytics

# Our command line arguments are stored here by the main logic 
g_cmd_args = None

@contextlib.contextmanager
def temp_chdir(path):
    """ Convenience function for setting and restoring current working directory
    
    Usage:
         with temp_chdir(new_path):
            do_something()
    """
    starting_directory = os.getcwd()
    try:
        os.chdir(path)
        yield
    finally:
        os.chdir(starting_directory)



class TestAnalytics(unittest.TestCase):

    @classmethod
    def setUpClass(self):
        """ Load in the main pebble shell module """
        global root_dir
        self.root_dir = root_dir
        pebble_shell = imp.load_source('pebble_shell', 
                                       os.path.join(root_dir, 'pebble.py'))
        from pebble_shell import PbSDKShell
        self.p_sh = PbSDKShell()
        self.sdk_version = self.p_sh._get_version()
        
        # What directory is our data in?
        self.data_dir = os.path.join(os.path.dirname(__file__), 
                                     'analytics_data')
        
        # Create a temp directory to use
        self.tmp_dir = tempfile.mkdtemp()
        
        # Process command line options
        global g_cmd_args
        if g_cmd_args is not None:
            self.debug = g_cmd_args.debug
        else:
            self.debug = False
            
        # Setup the pebble command arguments
        self.pebble_cmd_line = ['pebble']
        if self.debug:
            self.pebble_cmd_line += ['--debug']
        
        # Delete the NO_TRACKING file if it exists
        self.no_tracking_file_path = os.path.normpath(os.path.join(root_dir, 
                            os.pardir, 'NO_TRACKING'))
        self.has_no_tracking_file = os.path.exists(self.no_tracking_file_path)
        if self.has_no_tracking_file:
            os.remove(self.no_tracking_file_path)

        # pre-cache path to pebble settings dir
        home_dir = os.path.expanduser("~")
        self.settings_dir = os.path.join(home_dir, ".pebble")

        # Create the set of common fields we expect to see in every event
        analytics = pebble.PblAnalytics._Analytics.get()
        client_id = open(os.path.join(self.settings_dir, 
                                 "client_id")).read()
        self.common_evt_fields = {
            'v': '1',
            'tid': analytics.tracking_id,
            'cid': client_id,
            'cn': re.escape(platform.platform()),
            'ck': re.escape(platform.python_version()),
            'cs': client_id,
        }
        
            
    def _printTestHeader(self):
        """ Print out what test we are running
        """
        print "\n###############################################################"
        print "Running test: %s.%s..." % (self.__class__, self._testMethodName)

    
    @classmethod
    def tearDownClass(self):
        """ Clean up after all tests """
        
        # Remove our temp directory
        shutil.rmtree(self.tmp_dir, ignore_errors=True)
        
        # Restore tracking file?
        if self.has_no_tracking_file:
            with open(self.no_tracking_file_path, 'w') as fd:
                fd.write(" ")
        

    def use_project(self, project_name):
        """ Copies project_name from the analytics_data subdirectory to a
        new temp directory, sets that as the current working dir, and returns 
        that path. 
        
        Parameters:
        -------------------------------------------------------------------
        project_name:     name of the project
        retval: path to project after copied into a temp location
        """
        working_dir = os.path.join(self.tmp_dir, project_name)
        if os.path.exists(working_dir):
            shutil.rmtree(working_dir)
        shutil.copytree(os.path.join(self.data_dir, project_name), 
                        working_dir)
        print "Running '%s' project in directory %s" % (project_name, 
                                                        working_dir)
        return working_dir


    
    def find_evt(self, mock_urlopen, items_filter):
        """ Walk through all the calls to our mock urlopen() and look for one 
        that satisfies items_filter, which is a dict with the key/value pairs we
        need to satisfy. The values are regular expressions.
        
        Parameters:
        -------------------------------------------------------------------
        mock_urlopen: the mock urlopen instance
        items_filter: dict with desired key/value pairs
        retval: (header, data) if event was found
                (None, None) if not found
        """

        found = False
        for call in mock_urlopen.call_args_list:
            req = call[0][0]
            if isinstance(req, urllib2.Request):
              header = req.headers
              data = urlparse.parse_qs(req.get_data())
              if self.debug:
                  print "Parsing event: %s" % (pprint.pformat(data))
              matches = True
              for (key, value) in items_filter.items():
                  if not re.match(value, data[key][0]):
                      matches = False
                      break
              if matches:
                  return (header, data)

        return (None, None)
    
    
    def assert_evt(self, mock_urlopen, items_filter, include_common=True):
        """ Walk through all the calls to our mock urlopen() and look for one 
        that satisfies items_filter, which is a dict with the key/value pairs we
        need to satisfy. The values are regular expressions.
        
        If not found, raise an assertion
        
        Parameters:
        -------------------------------------------------------------------
        mock_urlopen: the mock urlopen instance
        items_filter: dict with desired key/value pairs
        """
        if include_common:
            for (key, value) in self.common_evt_fields.items():
                if key in items_filter:
                    continue
                items_filter[key] = value
 
        (header, data) = self.find_evt(mock_urlopen, items_filter)
        self.assertIsNotNone(header, "Did not find expected event "
                        "matching constraints: %s" % (str(items_filter)))
        
        # Check the header contents
        self.assertTrue(header['User-agent'].startswith('Pebble SDK/%s' % 
                                                        (self.sdk_version)))
        

    @patch('pebble.PblAnalytics.urlopen')
    def test_missing_packages(self, mock_urlopen):
        """ Test that we get the correct analytics produced when we run \
        a pebble command  without necessary python packages installed
        """
        self._printTestHeader()
        
        # Run at least once to load all modules in
        sys.argv = self.pebble_cmd_line + ['clean' ]
        self.p_sh.main()

        # Unload the websocket package and remove it from sys.modules and
        #  clear out sys.path so we can't find it again. We have insured that
        #  the pebble.py module tries to import websocket
        sys.modules.pop('websocket')
        old_path = sys.path
        sys.path = []
        
        sys.argv = self.pebble_cmd_line + ['clean' ]
        try:
            new_shell_module = imp.load_source('new_shell_module', 
                                       os.path.join(root_dir, 'pebble.py'))
        except:
            pass
        
        # Restore sys.path
        sys.path = old_path
        
        # Verify that we sent a missing python dependency event
        self.assert_evt(mock_urlopen,
            {'ec': 'install', 'ea': 'import', 
             'el': 'fail: missing import:.*'})


    @patch('pebble.PblAnalytics.urlopen')
    def test_invalid_project(self, mock_urlopen):
        """ Test that we get the correct analytics produced when we run \
        a pebble command in an invalid project directory. 
        """
        self._printTestHeader()

        sys.argv = self.pebble_cmd_line + ['clean' ]
        retval = self.p_sh.main()
        
        # Verify that we sent an invalid project event
        self.assert_evt(mock_urlopen,
            {'ec': 'pebbleCmd', 'ea': 'clean', 
             'el': 'fail: invalid project'})


    @patch('pebble.PblAnalytics.urlopen')
    def test_outdated_project(self, mock_urlopen):
        """ Test that we get the correct analytics produced when we run \
        a pebble command in an outdated project directory. 
        
        We made this project "outdated" by putting a resource_map.json
        file in the resources/src directory. 
        """
        self._printTestHeader()

        # Copy the desired project to temp location
        working_dir = self.use_project('outdated_project')
        
        with temp_chdir(working_dir):
            sys.argv = self.pebble_cmd_line + ['build' ]
            retval = self.p_sh.main()
        
        # Verify that we sent an outdated project event
        self.assert_evt(mock_urlopen,
            {'ec': 'pebbleCmd', 'ea': 'build', 
             'el': 'fail: outdated project'})


    @patch('pebble.PblAnalytics.urlopen')
    def test_app_too_big(self, mock_urlopen):
        """ Test that we get the correct analytics produced when we run \
        a pebble command in an app which is too big.
        
        We made this project "too big" by declaring a large static array
        in the main source file (hello_world.c):
           static int foo[25000] = {0};
        """
        self._printTestHeader()

        # Copy the desired project to temp location
        working_dir = self.use_project('too_big')
        
        with temp_chdir(working_dir):
            sys.argv = self.pebble_cmd_line + ['build' ]
            retval = self.p_sh.main()
        
        # Verify that we sent a app too big event
        self.assert_evt(mock_urlopen,
            {'ec': 'pebbleCmd', 'ea': 'build', 
             'el': 'fail: application too big'})


    @patch('pebble.PblAnalytics.urlopen')
    def test_compilation_error(self, mock_urlopen):
        """ Test that we get the correct analytics produced when we build \
        an app with a compilation error
        """
        self._printTestHeader()

        # Copy the desired project to temp location
        working_dir = self.use_project('good_c_app')
        
        # Introduce a compilation error
        with open(os.path.join(working_dir, "src", "hello_world.c"), 'w') \
                    as fd:
            fd.write("foo")
        
        with temp_chdir(working_dir):
            sys.argv = self.pebble_cmd_line + ['build' ]
            retval = self.p_sh.main()
        
        # Verify that we sent an compilation error event
        self.assert_evt(mock_urlopen,
            {'ec': 'pebbleCmd', 'ea': 'build', 
             'el': 'fail: compilation error'})


    @patch('pebble.PblAnalytics.urlopen')
    def test_clean_success(self, mock_urlopen):
        """ Test for correct event sent in response to the 'clean' command """
        self._printTestHeader()
        
        # Copy the desired project to temp location
        working_dir = self.use_project('good_c_app')
        
        with temp_chdir(working_dir):
            sys.argv = self.pebble_cmd_line + ['clean' ]
            retval = self.p_sh.main()

        # Verify that we sent a success event
        self.assert_evt(mock_urlopen,
            {'ec': 'pebbleCmd', 'ea': 'clean', 'el': 'success'})
        

    @patch('pebble.PblAnalytics.urlopen')
    def test_no_tracking_support(self, mock_urlopen):
        """ Test that we don't generate any events if we detect a 
        NO_TRACKING file"""
        self._printTestHeader()

        # Create a NO_TRACKING file and catch exceptions so that we
        #  are sure to delete it aftewards        
        try:
            with open(self.no_tracking_file_path, 'w') as fd:
                fd.write(" ")
            
            # Force a reload of the analytics instance
            pebble.PblAnalytics._Analytics.unload()
            
            # Copy the desired project to temp location
            working_dir = self.use_project('good_c_app')
                        
            with temp_chdir(working_dir):
                sys.argv = self.pebble_cmd_line + ['clean' ]
                retval = self.p_sh.main()
                
            call_count = mock_urlopen.call_count
            
        finally:
            # Undo tracking file
            os.remove(self.no_tracking_file_path)
            pebble.PblAnalytics._Analytics.unload()
                
        # Verify that no events were sent out
        self.assertEqual(call_count, 0, "Expected no URL "
            "requests with tracking off but got %d" % call_count)


    @patch('pebble.PblAnalytics.urlopen')
    def test_build_success_c_app(self, mock_urlopen):
        """ Test that we send the correct events after building a C app """
        self._printTestHeader()
        
        # Copy the desired project to temp location
        working_dir = self.use_project('good_c_app')
        uuid = '19aac3eb-870b-47fb-a708-0810edc4322e'
        
        with temp_chdir(working_dir):
            sys.argv = self.pebble_cmd_line + ['build']
            retval = self.p_sh.main()

        # Verify that we sent the correct events
        self.assert_evt(mock_urlopen,
            {'ec': 'pebbleCmd', 'ea': 'build', 'el': 'success'})
        
        self.assert_evt(mock_urlopen,
            {'ec': 'appCode', 'ea': 'totalSize', 'el': uuid, 'ev': '8.*'})

        self.assert_evt(mock_urlopen,
            {'ec': 'appResources', 'ea': 'totalSize', 'el': uuid, 'ev': '0'})

        self.assert_evt(mock_urlopen,
            {'ec': 'appResources', 'ea': 'totalCount', 'el': uuid, 'ev': '0'})

        for name in ['raw', 'image', 'font']:
            self.assert_evt(mock_urlopen,
                {'ec': 'appResources', 'ea': '%sSize' % (name), 'el': uuid, 
                 'ev': '0'})
    
            self.assert_evt(mock_urlopen,
                {'ec': 'appResources', 'ea': '%sCount' % (name), 'el': uuid, 
                 'ev': '0'})

        self.assert_evt(mock_urlopen,
            {'ec': 'appCode', 'ea': 'cLineCount', 'el': uuid, 'ev': '108'})

        self.assert_evt(mock_urlopen,
            {'ec': 'appCode', 'ea': 'jsLineCount', 'el': uuid, 'ev': '0'})

        self.assert_evt(mock_urlopen,
            {'ec': 'appCode', 'ea': 'hasJavaScript', 'el': uuid, 'ev': '0'})



    @patch('pebble.PblAnalytics.urlopen')
    def test_build_success_js_app(self, mock_urlopen):
        """ Test that we send the correct events after building a JS app """
        self._printTestHeader()
        
        # Copy the desired project to temp location
        working_dir = self.use_project('good_js_app')
        uuid = '74460383-8a0f-4bb6-971f-8937c2ed4441'
        
        with temp_chdir(working_dir):
            sys.argv = self.pebble_cmd_line + ['build']
            retval = self.p_sh.main()

        # Verify that we sent the correct events
        self.assert_evt(mock_urlopen,
            {'ec': 'pebbleCmd', 'ea': 'build', 'el': 'success'})
        
        self.assert_evt(mock_urlopen,
            {'ec': 'appCode', 'ea': 'totalSize', 'el': uuid, 'ev': '15..'})

        self.assert_evt(mock_urlopen,
            {'ec': 'appResources', 'ea': 'totalSize', 'el': uuid, 'ev': '16131'})

        self.assert_evt(mock_urlopen,
            {'ec': 'appResources', 'ea': 'totalCount', 'el': uuid, 'ev': '7'})

        sizes = {'raw': '4961', 'image': '3888', 'font': '7282'}
        counts = {'raw': '2', 'image': '4', 'font': '1'}
        for name in ['raw', 'image', 'font']:
            self.assert_evt(mock_urlopen,
                {'ec': 'appResources', 'ea': '%sSize' % (name), 'el': uuid, 
                 'ev': sizes[name]})
    
            self.assert_evt(mock_urlopen,
                {'ec': 'appResources', 'ea': '%sCount' % (name), 'el': uuid, 
                 'ev': counts[name]})

        self.assert_evt(mock_urlopen,
            {'ec': 'appCode', 'ea': 'cLineCount', 'el': uuid, 'ev': '136'})

        self.assert_evt(mock_urlopen,
            {'ec': 'appCode', 'ea': 'jsLineCount', 'el': uuid, 'ev': '107'})

        self.assert_evt(mock_urlopen,
            {'ec': 'appCode', 'ea': 'hasJavaScript', 'el': uuid, 'ev': '1'})


    def test_new_sdk_install(self):
        """ Test that we get the correct analytics produced when we run \
        a pebble command on a newly installed or recently upgraded SDK. 
        """
        self._printTestHeader()

        # Temporarily remove the .pebble directory
        save_settings_dir = self.settings_dir + ".bck"
        if os.path.exists(self.settings_dir):
            if os.path.exists(save_settings_dir):
                shutil.rmtree(save_settings_dir)
            os.rename(self.settings_dir, save_settings_dir)
        else:
            save_settings_dir = None
        
        # Force a re-instantiation of the Analytics object
        pebble.PblAnalytics._Analytics.unload()
        sys.argv = self.pebble_cmd_line + ['clean' ]
        with patch('pebble.PblAnalytics.urlopen') as mock_urlopen:
            self.p_sh.main()
    
            # Verify that a client id file and SDK version file got generated
            client_id = open(os.path.join(self.settings_dir, 
                                         "client_id")).read()            
            sdk_version = open(os.path.join(self.settings_dir, 
                                            "sdk_version")).read()

            # Verify that we got an install event
            self.assert_evt(mock_urlopen,
                {'ec': 'install', 'ea': 'firstTime', 'cid': client_id,
                 'cs': client_id})
        
        # Modify the SDK version, we should get an upgrade event and
        #  verify that the right client id got used
        with open(os.path.join(self.settings_dir, "sdk_version"), 'w') as fd:
            fd.write("foo")
        pebble.PblAnalytics._Analytics.unload()
        with patch('pebble.PblAnalytics.urlopen') as mock_urlopen:
            self.p_sh.main()
            self.assert_evt(mock_urlopen,
                {'ec': 'install', 'ea': 'upgrade', 'cid': client_id,
                 'cs': client_id})

        # Verify that the client_id file can have something like 'PEBBLE_INTERNAL'
        # in it
        with open(os.path.join(self.settings_dir, "client_id"), 'w') as fd:
            fd.write("PEBBLE_INTERNAL")
        pebble.PblAnalytics._Analytics.unload()
        with patch('pebble.PblAnalytics.urlopen') as mock_urlopen:
            self.p_sh.main()
            self.assert_evt(mock_urlopen,
                {'ec': 'pebbleCmd', 'ea': 'clean', 'cid': 'PEBBLE_INTERNAL',
                 'cs': 'PEBBLE_INTERNAL'})
            
        # Restore original .pebble dir            
        if save_settings_dir is not None:
            shutil.rmtree(self.settings_dir)
            os.rename(save_settings_dir, self.settings_dir)
        

    @patch('pebble.PblAnalytics.urlopen')
    def test_missing_tools(self, mock_urlopen):
        """ Test that we send the right event when the ARM tools are missing"""
        self._printTestHeader()
        
        # Rename the tools directory so that it can't be found
        tools_dir = os.path.join(root_dir, os.pardir, 'arm-cs-tools')
        save_tools_dir = tools_dir + ".bck"
        if os.path.exists(tools_dir):            
            os.rename(tools_dir, save_tools_dir)
        else:
            save_tools_dir = None
            
        # If we can still find it, remove it from the path
        save_os_environ = os.environ['PATH']
        paths = save_os_environ.split(':')
        while True: 
            where = sh.which('arm-none-eabi-size')
            if where is None:
                break
            dir = os.path.split(where)[0]
            paths.remove(dir)
            os.environ['PATH'] = ":".join(paths)
            
        # Copy the desired project to temp location
        working_dir = self.use_project('good_c_app')
        with temp_chdir(working_dir):
            sys.argv = self.pebble_cmd_line + ['build']
            retval = self.p_sh.main()

        # Verify that we sent missing tools event
        self.assert_evt(mock_urlopen,
            {'ec': 'install', 'ea': 'tools', 'el': 'fail: The compiler.*'})
        
        
        # Restore environment
        if save_tools_dir is not None:
            os.rename(save_tools_dir, tools_dir)
        os.environ['PATH'] = save_os_environ
        

    @patch('pebble.PblAnalytics.urlopen')
    def test_app_install(self, mock_urlopen):
        """ Test that we send the correct events when installing an app """
        self._printTestHeader()
        
        # Copy the desired project to temp location
        working_dir = self.use_project('good_c_app')
        uuid = '19aac3eb-870b-47fb-a708-0810edc4322e'
        
        with contextlib.nested (
             patch('pebble.LibPebblesCommand.libpebble'),
             patch.object(pebble.LibPebblesCommand.LibPebbleCommand, 'tail')
             ) as (mock_libpebble, mock_tail):

            attrs = {'get_phone_info.return_value': 
                     'FakeOS,FakeOSVersion,FakeModel'}
            Pebble_mock = MagicMock(**attrs)
            mock_libpebble.Pebble = MagicMock(return_value=Pebble_mock)
            
            with temp_chdir(working_dir):
                sys.argv = self.pebble_cmd_line + ['build']
                self.p_sh.main()
                sys.argv = self.pebble_cmd_line + ['install']
                self.p_sh.main()
    
                # Verify that we sent the correct events
                self.assert_evt(mock_urlopen,
                    {'ec': 'pebbleCmd', 'ea': 'install', 'el': 'success'})
            
                self.assert_evt(mock_urlopen,
                    {'ec': 'phone', 'ea': 'os', 'el': 'FakeOS'})
            
                self.assert_evt(mock_urlopen,
                    {'ec': 'phone', 'ea': 'osVersion', 'el': 'FakeOSVersion'})
            
                self.assert_evt(mock_urlopen,
                    {'ec': 'phone', 'ea': 'model', 'el': 'FakeModel'})

                # Verify that install --logs produces the right action
                mock_urlopen.reset_mock()
                sys.argv = self.pebble_cmd_line + ['install', '--logs']
                self.p_sh.main()
                self.assert_evt(mock_urlopen,
                    {'ec': 'pebbleCmd', 'ea': re.escape('install --logs'), 
                     'el': 'success'})
            
            
        

#############################################################################
def _getTestList():
  """ Get the list of tests that can be run from this module"""
  suiteNames = ['TestAnalytics']
  testNames = []
  for suite in suiteNames:
    for f in dir(eval(suite)):
      if f.startswith('test'):
        testNames.append('%s.%s' % (suite, f))

  return testNames


if __name__ == '__main__':
    usage_string = "%(prog)s [options] [-- unittestoptions] " \
                "[suitename.testname | suitename]"
                
    help_string = """Run Analytics tests.
    
Examples:
    python %(prog)s --help      # to see this help message
    python %(prog)s -- --help   # to see all unittest options
    python %(prog)s --debug  TestAnalytics 
    python %(prog)s --debug  -- --failfast TestAnalytics  
    python %(prog)s  TestAnalytics.test_build_success
     
Available suitename.testnames: """     
    all_tests = _getTestList()
    for test in all_tests:
        help_string += "\n   %s" % (test)
    
    parser = argparse.ArgumentParser(description = help_string,
                    formatter_class=argparse.RawTextHelpFormatter,
                    usage=usage_string)
    parser.add_argument('ut_args', metavar='arg', type=str, nargs='*', 
                        help=' arguments for unittest module')
    parser.add_argument('-d', '--debug', action='store_true', 
                        help=' run with debug output on')
    
    args = parser.parse_args()
    g_cmd_args = args

    unittest.main(argv=['unittest'] + args.ut_args)
