#!/bin/bash

sbt run

yosys -p "synth_ice40 -blif Hello.blif" Hello.v
arachne-pnr -d 8k -P tq144:4k -p wavetable.pcf Hello.blif -o Hello.asc
icepack Hello.asc Hello.bin
