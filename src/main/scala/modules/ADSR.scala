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

	val STARTr = Module(new EdgeBuffer)
  	STARTr.io.in := io.START

	val sIdle :: sAttack :: sDecay :: sSustain :: sRelease :: Nil = Enum(5)
	val state = RegInit(sIdle)
	io.RUNNING := (state != sIdle)

	var stateTmr = Module(new Timer(w))

	when(STARTr.io.rising){
		io.OUTVAL := 0.U
		state := sAttack
	}
	when(STARTr.io.falling){ 
		state := sDecay 
	}
	when(state === sAttack){
		stateTmr.io.period := io.A_INTERVAL
		when( stateTmr.io.fire ){ io.OUTVAL := io.OUTVAL + 1.U }
		when( io.OUTVAL === 127.U ){ state === sDecay }
	}
	when(state === sDecay){
		stateTmr.io.period := io.D_INTERVAL
		when( stateTmr.io.fire ){ io.OUTVAL := io.OUTVAL - 1.U }

		//when sustain level is 0, exit here
		when( io.OUTVAL === 0.U ){ state === sIdle }
		.elsewhen( io.OUTVAL === io.SUS_LVL ){ state === sSustain }
	}
	when(state === sSustain){
		io.OUTVAL := io.SUS_LVL
	}
	when(state === sRelease){
		stateTmr.io.period := io.R_INTERVAL
		when( stateTmr.io.fire ){ io.OUTVAL := io.OUTVAL - 1.U }
		when( io.OUTVAL === 0.U ){ state := sIdle }
	}
}