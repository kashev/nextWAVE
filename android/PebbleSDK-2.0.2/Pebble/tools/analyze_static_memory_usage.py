#!/usr/bin/env python
import argparse
import os
import re
import sh
import sys

cleanup_path_func = None

def cleanup_path(f):
    return os.path.relpath(f)

cleanup_path_func = cleanup_path

class Symbol(object):
    def __init__(self, name, size):
        self.name = name
        self.size = size

    def __str__(self):
        return '<Symbol %s: %u>' % (self.name, self.size)

class FileInfo(object):
    def __init__(self, filename):
        self.filename = filename
        self.size = 0
        self.symbols = {}

    def add_entry(self, symbol_name, size):
        if symbol_name in self.symbols:
            return

        self.size += size
        self.symbols[symbol_name] = Symbol(symbol_name, size)

    def remove_entry(self, symbol_name):
        result = self.symbols.pop(symbol_name, None)
        if result is not None:
            self.size -= result.size
        return result

    def pprint(self, verbose):
        print '  %s: size %u' % (self.filename, self.size)
        if verbose:
            l = sorted(self.symbols.itervalues(), key=lambda x: -x.size)
            for s in l:
                print '    %6u %-36s' % (s.size, s.name)

    def __str__(self):
        return '<FileInfo %s: %u>' % (self.filename, self.size)

class SectionInfo(object):
    def __init__(self, name):
        self.name = name
        self.count = 0
        self.size = 0
        self.files = {}

    def add_entry(self, name, filename, size):
        self.count += 1
        self.size += size

        if cleanup_path_func is not None:
            filename = cleanup_path_func(filename)

        if filename not in self.files:
            self.files[filename] = FileInfo(filename)

        self.files[filename].add_entry(name, size)

    def remove_unknown_entry(self, name):
        result = self.files['Unknown'].remove_entry(name)
        if result is not None:
            self.size -= result.size
        return result

    def get_files(self):
        return self.files.values()

    def pprint(self, summary, verbose):
        print '%s: count %u size %u' % (self.name, self.count, self.size)

        if not summary:
            l = self.files.values()
            l = sorted(l, key=lambda f: -f.size)
            for f in l:
                f.pprint(verbose)

def nm_generator(f):
    infile = sh.arm_none_eabi_nm('-l', '-S', f)

    line_pattern = re.compile(r"""([0-9a-f]+)\s+ # address
                                  ([0-9a-f]+)\s+ # size
                                  ([dDbBtTrR])\s+ # section type
                                  (\S+) # name
                                  \s*(\S+)?$ # filename + line
                                  """, flags=re.VERBOSE)

    for line in infile:
        match = line_pattern.match(line)

        if match is None:
            continue

        addr = int(match.group(1), 16)
        size = int(match.group(2), 16)
        section = match.group(3).lower()
        if section == 'r': section = 't'
        symbol_name = match.group(4)
        filename_line = match.group(5)

        yield (section, symbol_name, filename_line, size)

def analyze_elf(elf_file, sections):
    for (section, symbol_name, filename_line, size) in nm_generator(elf_file):
        if filename_line is None:
            filename_line = 'Unknown'

        filename = filename_line.split(':')[0]

        if section in sections:
            sections[section].add_entry(symbol_name, filename, size)

def make_sections_dict(sections_string):
    sections = {}
    for s in sections_string:
        if s == 'b':
            sections['b'] = SectionInfo('.bss')
        elif s == 'd':
            sections['d'] = SectionInfo('.data')
        elif s == 't':
            sections['t'] = SectionInfo('.text')
        else:
            raise Exception('Invalid section <%s>, must be a combination of [bdt] characters\n' % s)

    return sections

if (__name__ == '__main__'):
    parser = argparse.ArgumentParser()
    parser.add_argument('--verbose', action='store_true')
    parser.add_argument('--summary', action='store_true')
    parser.add_argument('--sections', default='bdt')
    parser.add_argument('elf_file')
    args = parser.parse_args()

    sections = make_sections_dict(args.sections)

    analyze_elf(args.elf_file, sections)

    for s in sections.itervalues():
        s.pprint(args.summary, args.verbose)

