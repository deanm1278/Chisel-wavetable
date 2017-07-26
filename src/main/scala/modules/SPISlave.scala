package modules

import chisel3._
import chisel3.util._
import chisel3.experimental._ // To enable experimental features


class EdgeBuffer extends Module {
  val io = IO(new Bundle {
    val in  = Input(Bool())
    val out = Output(Bool())
    val rising = Output(Bool())
    val falling = Output(Bool())
  })
  val r0 = RegNext(io.in)
  val r1 = RegNext(r0)
  val r2 = RegNext(r1)
  io.out := r1

  io.rising := (!r2 && r1)
  io.falling := (r2 && !r1)
}

object ShiftIn {
  def shift(w: Int, en: Bool, v: Bool): UInt = {
    val x = RegInit(0.U(w.W))
    when (en) { x := Cat(x, v) }
    x
  }
}

object Counter {

  def wrapAround(n: UInt, max: UInt) = 
    Mux(n > max, 0.U, n)

  def counter(max: UInt, en: Bool, amt: UInt = 1.U): UInt = {
    val x = RegInit(0.U(max.getWidth.W))
    when (en) { x := wrapAround(x + amt, max) }
    x
  }

}

class Counter(val w: Int) extends Module {
  val io = IO(new Bundle {
    val inc = Input(Bool())
    val tot = Output(UInt(w))
    val amt = Input(UInt(w))
  })

  io.tot := Counter.counter(w.U, io.inc, io.amt)
}

class SPISlave(val w: Int) extends Module {
	val io = IO(new Bundle {
		val MOSI = Input(Bool())
		val MISO = Output(Bool())
		val SCK = Input(Bool())
		val SSEL = Input(Bool())
		val DATA_READY = Output(Bool())
		val DATA = Output(UInt(w.W))
	})

	val MOSI_DATA = ShiftRegister(io.MOSI, 1)
  val SCKr = Module(new EdgeBuffer)
  SCKr.io.in := io.SCK

  val SSELr = Module(new EdgeBuffer)
  SSELr.io.in := io.SSEL

  withReset(SSELr.io.out){
  	val bitcnt = Module(new Counter(w - 1))
    bitcnt.io.inc := SCKr.io.rising
    
    val byte_received = RegNext(bitcnt.io.tot === (w - 1).U && SCKr.io.rising)

    io.DATA_READY := byte_received
  }

  io.DATA := ShiftIn.shift(w, SCKr.io.rising && !SSELr.io.out, MOSI_DATA)

}