package modules

import chisel3._

//TODO: this should stop at the end of the waveform
class Volume extends Module {
	val io = IO(new Bundle{
		val IN = Input(UInt(16.W))
		val OUT = Output(UInt(16.W))
		val MUL = Input(UInt(3.W))
	})

	val scaled = io.IN(14, 0) >> io.MUL

	when( io.IN(15) ){ io.OUT := 0x8000.U - scaled }
	.otherwise { io.OUT := 0x8000.U + scaled }
}