package modules

import chisel3._
import chisel3.util._

class ADSR(val w: Int) extends Module {
	val io = IO(new Bundle{
		val A_INTERVAL = Input(UInt(w.W))
		val D_INTERVAL = Input(UInt(w.W))
		val R_INTERVAL = Input(UInt(w.W))
		val SUS_LVL = Input(UInt(7.W))

		val START = Input(Bool())

		val OUTVAL = Output(UInt(7.W))
		val RUNNING = Output(Bool())
	})
	val out = RegInit(0.U(7.W))

	val STARTr = Module(new EdgeBuffer)
  	STARTr.io.in := io.START

	val sIdle :: sAttack :: sDecay :: sSustain :: sRelease :: Nil = Enum(5)
	val state = RegInit(sIdle)
	io.RUNNING := (state != sIdle)

	var stateTmr = Module(new Timer(w))

	when(STARTr.io.rising){
		out := 0.U
		state := sAttack
	}
	when(STARTr.io.falling && (state != sIdle) && io.SUS_LVL != 0.U){ 
		state := sRelease 
	}
	when(state === sAttack){
		stateTmr.io.period := io.A_INTERVAL
		when( out === 127.U ){ state := sDecay }
		when( stateTmr.io.fire ){ out := out + 1.U }
	}
	when(state === sDecay){
		stateTmr.io.period := io.D_INTERVAL

		//when sustain level is 0, exit here
		when( out === 0.U ){ state := sIdle }
		when( out === io.SUS_LVL ){ state := sSustain }

		when( stateTmr.io.fire ){ out := out - 1.U }
	}
	when(state === sSustain){
		out := io.SUS_LVL
	}
	when(state === sRelease){
		stateTmr.io.period := io.R_INTERVAL
		when( out === 0.U ){ state := sIdle }
		when( stateTmr.io.fire ){ out := out - 1.U }
	}
	io.OUTVAL := out
}