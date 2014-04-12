#pragma once

#include <stdint.h>

//! @addtogroup Foundation
//! @{
//!   @addtogroup App
//!   @{

//! Application metadata flags.
//! Flags can be combined using the `|` operator.
//! @see PBL_APP_INFO
typedef enum {
  //! Use to indicate the application is not a watchface, but a "standard" app.
  //! The system will show the app in the main menu.
  APP_INFO_STANDARD_APP = 0,
  //! Use to indicate the application is a watchface.
  //! The system will show the app in the watchfaces menu.
  APP_INFO_WATCH_FACE = 1 << 0,
  //! Use to hide the application.
  APP_INFO_VISIBILITY_HIDDEN = 1 << 1,
  //! Use to hide the application, unless there is ongoing communication with
  //! the companion smartphone application.
  APP_INFO_VISIBILITY_SHOWN_ON_COMMUNICATION = 1 << 2,
  //! Use to indicate the watch app allows Javascript API access
  APP_INFO_ALLOW_JS = 1 << 3,
} PebbleAppFlags;

//!   @} // group App
//! @} // group Foundation

// struct PebbleAppInfo change log
// ================================
// struct_version (little endian):
// 0x0800 -- sdk_version and app_version uint16_t fields added (Grand Slam / 1.7)
// .major:0x08 .minor:0x01 -- all version fields split up into minor/major; uuid field appended (Junior Whopper / 2.0?)
// .major:0x08 .minor:0x02 -- ?
// .major:0x09 .minor:0x00 -- 2.0, no more reloc_list_start
// .major:0x10 .minor:0x00 -- 2.0, added virtual_size
#define APP_INFO_CURRENT_STRUCT_VERSION_MAJOR 0x10
#define APP_INFO_CURRENT_STRUCT_VERSION_MINOR 0x0

// app info version for last know 1.x
// let this be a warning to engineers everywhere
// who want to design a system with fancy versioning and
// support
#define APP_INFO_LEGACY_STRUCT_VERSION_MAJOR 0x08

// SDK change log
// ================================
// sdk.major:4 .minor:0 -- Bump the SDK version to make 1.x and 2.x apps distinguishable
// sdk.major:5 .minor:0 -- Bump the SDK version for breaking AppMessage changes b/t 2.x alpha and beta releases
#define APP_INFO_CURRENT_SDK_VERSION_MAJOR 0x5
#define APP_INFO_CURRENT_SDK_VERSION_MINOR 0x0

#define APP_NAME_BYTES 32
#define COMPANY_NAME_BYTES 32

//! @internal
//! Version data structure with minor & major versions: When making non-backwards-compatible changes,
//! the major version should get bumped. When making a change (e.g. to the PebbleAppInfo struct) that is backwards
//! compatible (e.g. adding a field at the end), you should only bump the minor version.
typedef struct __attribute__((__packed__)) {
  uint8_t major; //!< "compatibility" version number
  uint8_t minor;
} Version;

//! @internal
// WARNING: changes in this struct must be reflected in:
// - tintin/waftools/inject_metadata.py
// - iOS/PebblePrivateKit/PebblePrivateKit/PBBundle.m
typedef struct __attribute__((__packed__)) {
  char header[8];                   //!< Sentinal value, should always be 'PBLAPP'
  Version struct_version;           //!< version of this structure's format
  Version sdk_version;              //!< version of the SDK used to build this app
  Version app_version;              //!< version of the app
  uint16_t load_size;               //!< size of the app binary in flash, including this metadata but not the reloc table
  uint32_t offset;                  //!< The entry point of this executable
  uint32_t crc;                     //!< CRC of the app data only, ie, not including this struct or the reloc table at the end
  char name[APP_NAME_BYTES];        //!< Name to display on the menu
  char company[COMPANY_NAME_BYTES]; //!< Name of the maker of this app
  uint32_t icon_resource_id;        //!< Resource ID within this app's bank to use as a 32x32 icon
  uint32_t sym_table_addr;          //!< The system will poke the sdk's symbol table address into this field on load
  uint32_t flags;                   //!< Bitwise OR of PebbleAppFlags
  uint32_t num_reloc_entries;       //!< The number of entries in the address relocation list
  struct __attribute__((__packed__)) {
    uint8_t byte0;
    uint8_t byte1;
    uint8_t byte2;
    uint8_t byte3;
    uint8_t byte4;
    uint8_t byte5;
    uint8_t byte6;
    uint8_t byte7;
    uint8_t byte8;
    uint8_t byte9;
    uint8_t byte10;
    uint8_t byte11;
    uint8_t byte12;
    uint8_t byte13;
    uint8_t byte14;
    uint8_t byte15;
  } uuid;                           //!< The app's UUID
  uint32_t resource_crc;            //!< CRC of the resource data only
  uint32_t resource_timestamp;      //!< timestamp of the resource data
  uint16_t virtual_size;            //!< The total amount of memory used by the app (.text + .data + .bss)
} PebbleAppInfo;

//! @internal
typedef struct __attribute__((__packed__)) {
  char header[8];                   //!< Sentinal value, should always be 'PBLAPP'
  Version struct_version;           //!< version of this structure's format
  Version sdk_version;              //!< version of the SDK used to build this app
  Version app_version;              //!< version of the app
  uint16_t load_size;               //!< size of the app binary in flash, including this metadata but not the reloc table
  uint32_t offset;                  //!< The entry point of this executable
  uint32_t crc;                     //!< CRC of the app data only, ie, not including this struct or the reloc table at the end
  char name[APP_NAME_BYTES];        //!< Name to display on the menu
  char company[COMPANY_NAME_BYTES]; //!< Name of the maker of this app
  uint32_t icon_resource_id;        //!< Resource ID within this app's bank to use as a 32x32 icon
  uint32_t sym_table_addr;          //!< The system will poke the sdk's symbol table address into this field on load
  uint32_t flags;                   //!< Bitwise OR of PebbleAppFlags
  uint32_t reloc_list_start;        //!< The offset of the address relocation list
  uint32_t num_reloc_entries;       //!< The number of entries in the address relocation list
  struct __attribute__((__packed__)) {
    uint8_t byte0;
    uint8_t byte1;
    uint8_t byte2;
    uint8_t byte3;
    uint8_t byte4;
    uint8_t byte5;
    uint8_t byte6;
    uint8_t byte7;
    uint8_t byte8;
    uint8_t byte9;
    uint8_t byte10;
    uint8_t byte11;
    uint8_t byte12;
    uint8_t byte13;
    uint8_t byte14;
    uint8_t byte15;
  } uuid;                           //!< The app's UUID
} LegacyPebbleAppInfo;


