package modules.test

import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import modules.Wavetable
import org.scalatest.Matchers
import scala.util.Random

class WavetableUnitTester(c: Wavetable) extends PeekPokeTester(c) {

  poke(c.io.Disable, 0)

  for (i <- 0 until 10) {
  	val stp = rnd.nextInt((1 << 3) - 1)
  	val freq = rnd.nextInt((1 << 5) - 1)

    poke(c.io.step, stp)
    poke(c.io.freq, freq)

    for(k <- 0 until (freq * 1023) ){
	    step(1)
	  }
  }
}

class WavetableTester extends ChiselFlatSpec with Matchers {
  private val backendNames = Array[String]("firrtl", "verilator")
  for ( backendName <- backendNames ) {
    "Wavetable" should s"increment read pos and bank at passed frequency (with $backendName)" in {
      Driver(() => new Wavetable, backendName) {
        c => new WavetableUnitTester(c)
      } should be (true)
    }
  }
}