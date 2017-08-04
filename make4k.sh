#!/bin/bash

sbt run

yosys -p "synth_ice40 -blif Hello.blif" Hello.v
arachne-pnr -d 8k -P tq144:4k -p wavetable.pcf Hello.blif -o Hello.asc
icepack Hello.asc Hello.bin


#yosys -p "read_verilog -lib +/ice40/cells_sim.v; setattr -set keep 1 r:\\* w:\\*; hierarchy -check -top Hello;
#                                synth_ice40 -run flatten: -blif bf100.blif" Hello.v
#arachne-pnr -d 8k -P tq144:4k -p wavetable.pcf -w assignments.pcf bf100.blif -o bf100.txt
#icepack bf100.txt bf100.bin
