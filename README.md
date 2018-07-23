# Chisel-wavetable

This is a single-cycle wavetable synthesizer for a Lattice ICE40 FPGA. It is controllable via SPI from a host processor.

This is used on [the bottomfeeder synthesizer](https://github.com/deanm1278/bottomfeeder)
A demo video of this synth can be found [here](https://youtu.be/Ub5NRgZzTfE)

[Chisel](https://chisel.eecs.berkeley.edu/) is used to generate verilog which can then be synthesized into hardware using the free Lattice synthesis tools.
