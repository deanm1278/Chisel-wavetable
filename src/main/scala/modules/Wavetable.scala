package modules

import chisel3._
import chisel3.experimental._ // To enable experimental features
import chisel3.util._

class Timer(val w: Int) extends Module {
	val io = IO(new Bundle{
		val period = Input(UInt(w.W))
		val fire = Output(Bool())
	})

	val cnt = Counter.counter(io.period, true.B, 1.U)
	io.fire := RegNext(cnt === io.period)
}

object ChangeDetect {
  def changeDetect(comp: UInt, value: UInt): UInt = {
    val x = RegNext(value)
    val changed = RegInit(false.B)

    when(x === comp && value != comp){ changed := true.B }
    .otherwise{ changed := false.B }

    changed
  }
}

class ChangeDetect(val w: Int) extends Module {
  val io = IO(new Bundle {
    val in = Input(w.U)
    val comp = Input(w.U)
    val changed = Output(Bool())
  })

  io.changed := ChangeDetect.changeDetect(io.comp, io.in)
}

object ResetLatch {
	def resetLatch(stopTrigger: Bool, stopHold: Bool): Bool = {
		val x = RegInit(true.B)
		when(stopHold){ x := false.B }
		.elsewhen( (stopTrigger && !stopHold) ){ x := true.B }
		x
	}
}

class ResetLatch extends Module {
  val io = IO(new Bundle {
    val trigger = Input(Bool())
    val hold = Input(Bool())
    val out = Output(Bool())
  })

  io.out := ResetLatch.resetLatch(io.trigger, io.hold)
}

class BlackBoxSubosc extends BlackBox with HasBlackBoxInline  {
  val io = IO(new Bundle() {
    val en = Input(Bool())
    val flip = Input(Bool())
    val clock = Input(Clock())
    val out = Output(Bool())
  })
  setInline("Subosc.v",
    s"""
      |module BlackBoxSubosc(
      |    input  en,
      |    input  flip,
      |    input clock,
      |    output out
      |);
      |  reg oscVal = 1'b0;
      |  always @(posedge clock) begin
      |    if(flip) oscVal <= ~oscVal;
      |  end
      |  assign out = (en ? oscVal : 1'bZ);
      |endmodule
    """.stripMargin)
}

class Wavetable extends Module {
	val io = IO(new Bundle{
		val RBANK = Output(UInt(2.W))
		val RADDR = Output(UInt(8.W))
		val Disable = Input(Bool())
		val freq = Input(UInt(13.W))
		val step = Input(UInt(3.W))
		val OVF = Output(Bool())
	})

	withReset(io.Disable || (io.freq === 0.U)){
		var periodTimer = Module(new Timer(16))
		periodTimer.io.period := io.freq

		val sampleCounter = Module(new Counter(1023))
	    sampleCounter.io.inc := periodTimer.io.fire
	    sampleCounter.io.amt := (1.U << io.step)

		io.RADDR := sampleCounter.io.tot(7, 0)
		io.RBANK := sampleCounter.io.tot(9, 8)

		io.OVF := ChangeDetect.changeDetect(3.U, io.RBANK)
	}
}