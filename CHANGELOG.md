# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [0.2.0] - 2022-09-18

### Added

- class file assembler / disassembler, work in progress
- added support for releases on jitpack.io

### Changed

- completed classfile module
- completed class dump tool

### Removed

- Removed guava dependency

## [0.1.0] - 2022-08-12

### Added

- dex assembler tool (`bat-smali`), feature complete
- dex disassembler tool (`bat-baksmali`), feature complete
- dex merger tool (`bat-dexmerge`), feature complete
- dex dump tool (`bat-dexdump`), feature complete
- class dump tool (`bat-classdump`), not complete yet
- tinydvm: simple virtual machine to execute dalvik bytecode, not complete yet