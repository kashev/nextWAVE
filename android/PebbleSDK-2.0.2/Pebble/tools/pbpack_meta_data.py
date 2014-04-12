#!/usr/bin/env python

import argparse
import os
import struct

import stm32_crc

MAX_NUM_FILES = 256
BYTES_PER_TABLE_ENTRY = 16

def cmd_manifest(args):
    with open(args.manifest_file, 'wb') as manifest_file:
        with open(args.data_chunk_file, 'rb') as data_file:
            crc = stm32_crc.crc32(data_file.read())
            manifest_file.write(struct.pack('<III', int(args.num_files), crc, int(args.timestamp)))

def cmd_table(args):
    with open(args.table_file, 'wb') as table_file:

        cur_file_id = 1
        next_free_byte = 0

        for filename in args.pack_file_list:
            with open(filename, 'rb') as data_file:
                content = data_file.read()
                length = len(content)
                table_file.write(struct.pack('<IIII', cur_file_id, next_free_byte, length, stm32_crc.crc32(content)))
                cur_file_id += 1
                next_free_byte += length

        # pad the rest of the file
        for i in range(len(args.pack_file_list), MAX_NUM_FILES):
            table_file.write(struct.pack('<IIII', 0, 0, 0, 0))

def main():
    # process an individual file
    parser = argparse.ArgumentParser(description="Generate the meta data chunks of the pbpack")
    subparsers = parser.add_subparsers(help="commands", dest='which')

    manifest_parser = subparsers.add_parser('manifest', help="make the manifest file")
    manifest_parser.add_argument('manifest_file', metavar="MANIFEST_FILE", help="File to write the manifest to")
    manifest_parser.add_argument('num_files', metavar="NUM_FILES", help="Number of files in this resource pack")
    manifest_parser.add_argument('timestamp', metavar="TIMESTAMP", help="timestamp to label this pack with", type=int)
    manifest_parser.add_argument('data_chunk_file', metavar="DATA_CHUNK_FILE", help="The data file to CRC for this pack")
    manifest_parser.set_defaults(func=cmd_manifest)
    
    table_parser = subparsers.add_parser('table', help="make the metadata table")
    table_parser.add_argument('table_file', metavar='TABLE_FILE', help="file to write the table chunk to")
    table_parser.add_argument('pack_file_list', metavar='PACK_FILE_LIST', nargs="*", help="a list of <pack_file_path>s")
    table_parser.set_defaults(func=cmd_table)

    args = parser.parse_args()
    args.func(args)

if __name__ == "__main__":
    main()
