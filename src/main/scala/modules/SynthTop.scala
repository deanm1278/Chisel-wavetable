package modules

import chisel3._
import chisel3.experimental._ // To enable experimental features

//we need to declare the PLL as a black box since it's got a device-specific implementation
class SB_PLL40_CORE extends BlackBox(Map("FEEDBACK_PATH" -> "SIMPLE",
	"PLLOUT_SELECT" -> "GENCLK",
	"DIVR" -> 0,
	"DIVF" -> 79,
	"DIVQ" -> 3,
	"FILTER_RANGE" -> 1)) {

	val io = IO(new Bundle {
		val LOCK = Output(Bool())
		val RESETB = Input(Bool())
		val BYPASS = Input(Bool())
		val REFERENCECLK = Input(Clock())
		val PLLOUTCORE = Output(Clock())
	})
}

class DReg(val w: Int) extends Module {
  val io = IO(new Bundle {
    val en = Input(Bool())
    val din = Input(UInt(w.W))
    val dout = Output(UInt(w.W))
  })

  val x = RegInit(0.U(w.W))
  when(io.en) { x := io.din }

  io.dout := x
}

class Hello extends Module {
	val io = IO(new Bundle {
		val led0 = Output(Bool())
		val led1 = Output(Bool())
		val led2 = Output(Bool())

		val MOSI = Input(Bool())
		val MISO = Output(Bool())
		val SCK  = Input(Bool())
		val SSEL = Input(Bool())
	})

	//instantiate the PLL. We need to explicitly define the clock and reset
	val pll = Module(new SB_PLL40_CORE)
	pll.io.RESETB := !this.reset
	pll.io.REFERENCECLK := this.clock

	withClock(pll.io.PLLOUTCORE){

		//create the SPI interface
		val spi = Module(new SPISlave(32))
		spi.io.MOSI := io.MOSI
		io.MISO := spi.io.MISO
		spi.io.SCK := io.SCK
		spi.io.SSEL := io.SSEL

		val decoder = Module(new SPIDecode)
		decoder.io.dataIn := spi.io.DATA
		decoder.io.trigger := spi.io.DATA_READY

		//create the register map
		val regs = Range(0, 4).map(_ => Module(new DReg(16)))
		for (k <- 0 until 4) {
			regs(k).io.din := decoder.io.dataOut
			regs(k).io.en := (decoder.io.addr === k.U && decoder.io.wclk)
		}

		//create the PWMs
		val pwms = Range(0, 4).map(_ => Module(new PWM(12)))
		for (k <- 0 until 4) {
			pwms(k).io.per := 4095.U
			pwms(k).io.en := 1.U
		}
		pwms(0).io.dc := regs(1).io.dout
		pwms(1).io.dc := regs(2).io.dout

		//set the pwm output pins
		io.led1 := pwms(0).io.out
		io.led2 := pwms(1).io.out

		//turn one LED on
		io.led0 := 1.U
	}
}

object HelloDriver extends App{
  chisel3.Driver.execute(args, () => new Hello())
}