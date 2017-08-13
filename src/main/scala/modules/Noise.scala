package modules

import chisel3._
import chisel3.util._

class Noise(val w: Int) extends Module {
	val io = IO(new Bundle{
		val period = Input(UInt(w.W))
		val out = Output(Bool())
	})

	var periodTimer = Module(new Timer(w))
	periodTimer.io.period := io.period

	val res = RegInit(1.U(16.W))
	  when (periodTimer.io.fire) { 
	    val nxt_res = Cat(res(0)^res(2)^res(3)^res(5), res(15,1)) 
	    res := nxt_res
	  }
	  io.out := res
}