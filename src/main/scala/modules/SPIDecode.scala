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

class SPIDecodeWave(val w: Int, val dummyCycles: Int) extends Module {
	val io = IO(new Bundle {
		val WBANK = Output(UInt(2.W))
		val WADDR = Output(UInt(8.W))
		val trigger = Input(Bool())	
	})

	val dummyCounter = Module(new Counter(w))
    dummyCounter.io.inc := io.trigger
    dummyCounter.io.amt := 1.U

    val byteCount = dummyCounter.io.tot - dummyCycles.U

	io.WBANK := byteCount(9, 8)
	io.WADDR := byteCount(7, 0)
}