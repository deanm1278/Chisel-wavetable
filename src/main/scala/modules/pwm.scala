package modules

import chisel3._

class PWM(val w: Int) extends Module {
  val io = IO(new Bundle {
  	val dc = Input(UInt(w.W))
  	val per = Input(UInt(w.W))
  	val en = Input(Bool())
  	val out = Output(Bool())
  })

  var cnt = RegInit(0.U(w.W))
  cnt := cnt + 1.U

  when (cnt >= io.per) { cnt := 0.U }
  io.out := io.en && (cnt > io.dc)
}
