package modules

import chisel3._
import chisel3.experimental._ // To enable experimental features

//we need to declare the PLL as a black box since it's got a device-specific implementation
class SB_PLL40_CORE extends BlackBox(Map("FEEDBACK_PATH" -> "SIMPLE",
	"PLLOUT_SELECT" -> "GENCLK",
	"DIVR" -> 0,
	"DIVF" -> 23,
	"DIVQ" -> 2,
	"FILTER_RANGE" -> 2)) {

	val io = IO(new Bundle {
		val LOCK = Output(Bool())
		val RESETB = Input(Bool())
		val BYPASS = Input(Bool())
		val REFERENCECLK = Input(Clock())
		val PLLOUTCORE = Output(Clock())
	})
}

class DReg extends Module {
  val io = IO(new Bundle {
    val en = Input(Bool())
    val din = Input(UInt(16.W))
    val dout = Output(UInt(16.W))
  })

  val x = RegInit(0.U(16.W))
  when(io.en) { x := io.din }

  io.dout := x
}

class Hello extends Module {
	val io = IO(new Bundle {
		val PWM_OUT = Output(Vec(5, Bool()))

		val MOSI = Input(Bool())
		val MISO = Output(Bool())
		val SCK  = Input(Bool())
		val SSEL = Input(Bool())

		val WAVE_MOSI = Input(Bool())
		val WAVE_SCK  = Input(Bool())
		val WAVE_SSEL = Input(Bool())

		val OSC0	= Output(UInt(16.W))
		val OSC1	= Output(UInt(16.W))
		val OSC2	= Output(UInt(16.W))
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

		//SPI command decoder
		val decoder = Module(new SPIDecode)
		decoder.io.dataIn := spi.io.DATA
		decoder.io.trigger := spi.io.DATA_READY

		//create the register map
		val regs = Vec(Seq.fill(20){ Module(new DReg()).io })
		for (k <- 0 until 19) {
			regs(k).din := decoder.io.dataOut
			regs(k).en := (decoder.io.addr === k.U && decoder.io.wclk)
		}
		spi.io.READ_OUT := regs(decoder.io.addr).dout

		//create the wave spi interface
		val spiWave = Module(new SPIWave(16))
		spiWave.io.MOSI := io.WAVE_MOSI
		spiWave.io.SCK := io.WAVE_SCK
		spiWave.io.SSEL := io.WAVE_SSEL
		spiWave.io.EN := regs(9).dout(7)

		val waveDecode = Module(new SPIDecodeWave(1028, 4))
		waveDecode.io.trigger := spiWave.io.DATA_READY

		//make wavetables
		val wavetables = Range(0, 3).map(_ => Module(new Wavetable()))
		for (k <- 0 until 2) {
			wavetables(k).io.En := regs(9).dout(8) //Key pressed
			wavetables(k).io.freq := regs(k).dout(15, 3)
			wavetables(k).io.step := regs(k).dout(2, 0)
		}

		val brams = Range(0, 3).map(_ => Module(new RamArb()))
		for (k <- 0 until 2) {
			brams(k).io.RBANK := wavetables(k).io.RBANK
			brams(k).io.RADDR := wavetables(k).io.RADDR
			brams(k).io.RCLK := wavetables(k).io.RCLK
			brams(k).io.WBANK := waveDecode.io.WBANK
			brams(k).io.WADDR := waveDecode.io.WADDR
			brams(k).io.WCLK := spiWave.io.DATA_READY
			brams(k).io.WE := regs(9).dout(4 + k) //write enable bits
		}

		//TODO: these need to go through volume modules
		io.OSC0 := brams(0).io.RDATA
		io.OSC1 := brams(1).io.RDATA
		io.OSC2 := brams(2).io.RDATA

		//create the PWMs
		val pwms = Range(0, 5).map(_ => Module(new PWM(12)))
		for (k <- 0 until 4) {
			pwms(k).io.per := 4095.U
			pwms(k).io.en := 1.U
			pwms(k).io.dc := regs(k + 3).dout
			io.PWM_OUT(k) := pwms(k).io.out
		}
	}
}

object HelloDriver extends App{
  chisel3.Driver.execute(args, () => new Hello())
}