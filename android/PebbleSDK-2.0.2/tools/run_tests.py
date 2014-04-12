#!/usr/bin/env python

import argparse
import unittest
import os


###############################################################################
def main():
    """ Run all unit tests in the 'test' subdirectory (by default)
    """
    
    parser = argparse.ArgumentParser(description = 'Run SDK tests')
    parser.add_argument('-v', '--verbose', action='store_true', 
                        help='provide verbose output') 

    args = parser.parse_args()


    test_dir = os.path.join('test')
    unittest_args = ['unittest', 'discover', 
                     '--start-directory', test_dir]
    if args.verbose:
        unittest_args += ['--verbose']
        
    unittest.main(argv=unittest_args)


###############################################################################
if __name__ == '__main__':
    retval = main()
    sys.exit(retval)


