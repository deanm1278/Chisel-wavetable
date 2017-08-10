package modules

import chisel3._

class LimitedMatcher(val upper: Int, val lower: Int, val majorWidth: Int, val minorWidth: Int) extends Module {
	val io = IO(new Bundle{
		val EN = Input(Bool())
		val SET = Input(Bool())
		val major_target = Input(UInt(majorWidth.W))
		val minor_target = Input(UInt(minorWidth.W))
		val major_output = Output(UInt(majorWidth.W))
		val minor_output = Output(UInt(minorWidth.W))
	})
	val rmajor = RegInit(0.U)
	val rminor = RegInit(0.U)

	when(io.SET){
		rmajor := io.major_target
		rminor := io.minor_target
	}
	when(io.EN){
		when(io.major_target > rmajor){
			when(rminor > lower.U){ rminor := rminor - 1.U }
			.otherwise{ 
				rmajor := rmajor + 1.U
				rminor := upper.U
			}
		}
		.elsewhen(io.major_target < rmajor){
			when(rminor < upper.U){ rminor := rminor + 1.U }
			.otherwise{ 
				rmajor := rmajor - 1.U
				rminor := lower.U
			}
		}
		.elsewhen(io.minor_target > rminor){ rminor := rminor + 1.U }
		.elsewhen(io.minor_target < rminor){ rminor := rminor - 1.U }
	}

	io.major_output := rmajor
	io.minor_output := rminor
}

class Port extends Module {
	val io = IO(new Bundle{
		val fire = Input(Bool())
		val SET = Input(Bool())
		val target_freq = Input(UInt(13.W))
		val target_step = Input(UInt(3.W))
		val freq = Output(UInt(13.W))
		val step = Output(UInt(3.W))
	})

	val matcher = Module(new LimitedMatcher(0x7CB, 0x3E7, 3, 13))
	matcher.io.major_target := io.target_step
	matcher.io.minor_target := io.target_freq

	matcher.io.SET := io.SET
	matcher.io.EN := io.fire

	io.freq := matcher.io.minor_output
	io.step := matcher.io.major_output
}