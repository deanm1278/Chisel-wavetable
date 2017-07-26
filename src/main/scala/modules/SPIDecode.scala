package modules

import chisel3._

class SPIDecode extends Module {
	val io = IO(new Bundle {
		val dataIn = Input(UInt(32.W))
		val dataOut = Output(UInt(16.W))
		val addr    = Output(UInt(8.W))
		val trigger = Input(Bool())
		val wclk	= Output(Bool())
	})

	when(io.trigger){
		io.addr := io.dataIn(26, 22)
		io.dataOut := io.dataIn(15, 0)
	}

	io.wclk := RegNext(io.dataIn(31) && io.trigger)
}