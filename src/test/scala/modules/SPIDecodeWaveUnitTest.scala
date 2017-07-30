package modules.test

import chisel3._

import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import modules.SPIDecodeWave
import org.scalatest.Matchers
import scala.util.Random

class SPIDecodeWaveTest(c: SPIDecodeWave) extends PeekPokeTester(c) {

  for (i <- 0 until 1023 + 4) {
    poke(c.io.trigger, 1)
    step(1)
    if(i >= 4){
      expect(c.io.WADDR, (i - 3) % 256)
      expect(c.io.WBANK, (i - 3)/256)
    }
    poke(c.io.trigger, 0)
    step(1)
  }
}

class SPIDecodeWaveTester extends ChiselFlatSpec {
  private val backendNames = Array[String]("firrtl", "verilator")
  for ( backendName <- backendNames ) {
    "SPIDecodeWave" should s"return decoded address after some dummy cycles (with $backendName)" in {
      Driver(() => new SPIDecodeWave(1023, 4), backendName) {
        c => new SPIDecodeWaveTest(c)
      } should be (true)
    }
  }
}