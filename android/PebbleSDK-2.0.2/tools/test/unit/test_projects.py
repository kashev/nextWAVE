import contextlib
import imp
import os
import random
import re
import shutil
import sys
import tempfile
import unittest
import argparse
import pprint
import sh
import platform


# Allow us to run even if not at the root libpebble directory.
root_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), os.pardir, 
                                        os.pardir))
sys.path.insert(0, root_dir) 

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



class TestProjects(unittest.TestCase):

    @classmethod
    def setUpClass(self):
        """ Load in the main pebble shell module """
        global root_dir
        self.root_dir = root_dir
        pebble_shell = imp.load_source('pebble_shell', 
                                       os.path.join(root_dir, 'pebble.py'))
        from pebble_shell import PbSDKShell
        self.p_sh = PbSDKShell()
        
        # What directory is our data in?
        self.data_dir = os.path.join(os.path.dirname(__file__), 
                                     'projects')
        
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
                

    def use_project(self, project_name):
        """ Copies project_name from the data subdirectory to a
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


    def test_with_spaces(self):
        """ Test for correct behavior when the project path has spaces in it"""
        self._printTestHeader()
        
        # Copy the desired project to temp location
        working_dir = self.use_project('with spaces')
        
        with temp_chdir(working_dir):
            sys.argv = self.pebble_cmd_line + ['build' ]
            retval = self.p_sh.main()

        # Verify that we sent a success event
        self.assertEqual(retval, 0)
        

    def test_convert(self):
        """ Test for correct behavior during project conversion from 1.x to
        2.x """
        self._printTestHeader()
        
        # Copy the desired project to temp location
        working_dir = self.use_project('needs_convert')
        
        with temp_chdir(working_dir):
            sys.argv = self.pebble_cmd_line + ['convert-project' ]
            retval = self.p_sh.main()

        # Verify that we sent a success event
        self.assertEqual(retval, 0)
        

        

#############################################################################
def _getTestList():
  """ Get the list of tests that can be run from this module"""
  suiteNames = ['TestProjects']
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
