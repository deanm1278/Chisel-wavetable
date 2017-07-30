package modules

import chisel3._
import chisel3.experimental._ // To enable experimental features
import chisel3.util._ // To enable experimental features

class SB_RAM40_4K extends BlackBox(Map("READ_MODE" -> 0,
	"WRITE_MODE" -> 0)) {

	val io = IO(new Bundle {
		val RDATA = Output(UInt(16.W))
		val RADDR = Input(UInt(8.W))
		val RCLK = Input(Bool())
		val RCLKE = Input(Bool())
		val RE = Input(Bool())
		val WADDR = Input(UInt(8.W))
		val WCLK = Input(Bool())
		val WCLKE = Input(Bool())
		val WDATA = Input(UInt(16.W))
		val WE = Input(Bool())
		val MASK = Input(UInt(16.W))
	})

	/* TODO: I guess we can't do this :( find some other way to simulate
	val bram = Mem(256, UInt(16.W))
	when (io.WCLK && io.WE && io.WCLKE) { bram(io.WADDR) := io.WDATA }
	when (io.RCLK && io.RE && io.RCLKE) { io.RDATA := bram(io.RADDR) }
	*/
}

class RamArb extends Module {
	val io = IO(new Bundle {
		val RDATA = Output(UInt(16.W))
		val RADDR = Input(UInt(8.W))
		val RCLK = Input(Bool())
		val RBANK = Input(UInt(2.W))
		val WDATA = Input(UInt(16.W))
		val WADDR = Input(UInt(8.W))
		val WCLK = Input(Bool())
		val WBANK = Input(UInt(2.W))
		val WE = Input(Bool())
	})

	//create the register map
	//val banks = Range(0, 4).map(_ => Module(new SB_RAM40_4K))
	val banks = Vec(Seq.fill(4){ Module(new SB_RAM40_4K()).io })
	for (k <- 0 until 3) {
		banks(k).RADDR := io.RADDR
		banks(k).RCLK := io.RCLK
		banks(k).RCLKE := true.B
		banks(k).RE := (io.RBANK === k.U)
		banks(k).WDATA := io.WDATA
		banks(k).WADDR := io.WADDR
		banks(k).WCLK := io.WCLK
		banks(k).WCLKE := true.B
		banks(k).WE := (io.WBANK === k.U && io.WE)
		banks(k).MASK := 0x0000.U
	}
	io.RDATA := banks(io.RBANK).RDATA
} 