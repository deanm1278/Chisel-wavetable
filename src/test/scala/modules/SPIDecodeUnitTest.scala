import chisel3._

import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import modules.SPIDecode
import org.scalatest.Matchers
import scala.util.Random

class SPIDecodeTest(c: SPIDecode) extends PeekPokeTester(c) {
  val r = Random

  for (i <- 0 until 100) {
    val write = r.nextBoolean()
    val dataIn: BigInt = if (write) BigInt(r.nextInt(Integer.MAX_VALUE)) << 1 else BigInt(r.nextInt(Integer.MAX_VALUE))
    val wen = ((dataIn & 0x80000000) >> 31) & 0x01
    val trig = r.nextBoolean()

    poke(c.io.trigger, if (trig) 1 else 0)
    poke(c.io.dataIn, dataIn)
    step(1)

    expect(c.io.wclk, if ( wen > 0 && trig ) 1 else 0)
    if(trig){
      val in = dataIn.U
      expect(c.io.dataOut, in(15, 0))
      expect(c.io.addr, in(26, 22))
    }
  }
}

class SPIDecodeTester extends ChiselFlatSpec {
  private val backendNames = Array[String]("firrtl", "verilator")
  for ( backendName <- backendNames ) {
    "SPIDecode" should s"decode spi data (with $backendName)" in {
      Driver(() => new SPIDecode, backendName) {
        c => new SPIDecodeTest(c)
      } should be (true)
    }
  }
}