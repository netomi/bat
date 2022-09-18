# Bytecode Analysis Toolkit

![Build status](https://github.com/netomi/bat/workflows/build-status/badge.svg)
[![GitHub license](https://img.shields.io/github/license/netomi/bat)](https://github.com/netomi/bat/blob/master/LICENSE)

This goal of this project is to provide tools to work with various java related bytecode formats, mainly JVM and dalvik bytecode.

The following modules are currently available

* `classfile`: reads and writes **class** files
* `dexfile`: reads and writes **dex** files
* `dexdump`: prints the contents of **dex** files in the same format as the _dexdump_ tool
* `classdump`: prints the contents of **class** files in the same format as the _javap_ tool
* `smali`: assembler/disassembler for **dex** files
* `jasm`: assembler/disassembler for **class** files
* `tools`: convenient command line tools for the different modules

## Usage

1. In the root directory run: ./gradlew distZip
2. cd tools/build/distributions
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

## Dependency

To use the different modules via e.g. gradle in your own application, you can add the following to your build configuration:

```
repositories {
    ...
    maven("https://jitpack.io")
}

dependencies {
    ...
    implementation("com.github.netomi.bat:<module>:<tag>|<commit>|<release>")
}
```

where **module** might be one of

* `classfile` 
* `dexfile`
* `classdump`
* `dexdump`
* `smali`
* `jasm`
* `tinydvm`

## License
[Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)
