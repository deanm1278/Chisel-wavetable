package modules.test

import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import modules.Volume
import org.scalatest.Matchers
import scala.util.Random

class VolumeUnitTester(c: Volume) extends PeekPokeTester(c) {

  val r = Random

  for(i <- 0 until 10){
    val mul = r.nextInt(1 << 3)
    val dataIn = (r.nextInt(Integer.MAX_VALUE) & 0xFFFF);
    val msb = ((dataIn & 0x8000) >> 15) & 0x01
    poke(c.io.IN, dataIn)
    poke(c.io.MUL, mul)
    step(4)
    var out = 0
    if(msb == 1){
      out = 0x8000 - ( (dataIn & 0x7FFF) >> mul)
    }
    else{
      out = 0x8000 + ( (dataIn & 0x7FFF) >> mul)
    }
    expect(c.io.OUT, out)
  }
}

class VolumeTester extends ChiselFlatSpec with Matchers {
  private val backendNames = Array[String]("firrtl", "verilator")
  for ( backendName <- backendNames ) {
    "Volume" should s"detect when a value changes from a given value (with $backendName)" in {
      Driver(() => new Volume, backendName) {
        c => new VolumeUnitTester(c)
      } should be (true)
    }
  }
}