package modules

import chisel3._

class Mixer(val w: Int) extends Module {
	val io = IO(new Bundle{
		val ENV = Input(UInt(7.W))
		val PRE = Input(UInt(w.W))
		val MUL = Input(UInt(6.W))
		val POST = Output(UInt(w.W))
	})
	val max = ((1 << w) - 1)
	val mult = (io.ENV * io.MUL)

	val tmp = io.PRE + mult
	io.POST := Mux(tmp > max.U, max.U, tmp)

}