# Bytecode Analysis Toolkit

![Build status](https://github.com/netomi/bat/workflows/build-status/badge.svg)
[![GitHub license](https://img.shields.io/github/license/netomi/bat)](https://github.com/netomi/bat/blob/master/LICENSE)

This goal of this project is to provide tools to work with various java related bytecode formats, mainly JVM and dalvik bytecode.

The following modules are currently available

1. classfile: reads and writes **class** files
2. dexfile: reads and writes **dex** files
3. dexdump: prints the contents of **dex** files in the same format as the _dexdump_ tool
4. classdump: prints the contents of **class** files in the same format as the _javap_ tool
4. smali: assembler/disassembler for **dex** files
5. commands: convenient command line tools for the different modules

## Usage

1. In the root directory run: ./gradlew distZip
2. cd commands/build/distributions
3. Unzip the file 'bat-tools-${VERSION}.zip'
4. Run bat-${toolname}.sh from the unzipped directory

### Example usage:

* baksmali tool

```shell
Usage: bat-baksmali [-v] [-o=<outputFile>] inputfile
disassembles dex files.

Parameters:
      inputfile      input file to process (*.dex)

Options:
  -o=<outputFile>    output directory
  -v                 verbose output

```
> bat-baksmali.sh -v -o out classes.dex

The generated **smali** files will be created in the **out** directory in a directory structure resembling the package name of the disassembled classes.
## License
[Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)
