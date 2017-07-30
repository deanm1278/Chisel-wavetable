import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import modules.SPIWave
import org.scalatest.Matchers
import scala.util.Random

class SPIWaveTest(c: SPIWave) extends PeekPokeTester(c) {
  val r = Random
  poke(c.io.SSEL, 1)
  poke(c.io.SCK, 0)
  poke(c.io.EN, 1)

  step(10)

  for(n <- 1 to 100) {
    val dataIn = r.nextInt(1 << c.w);

    var mosi = dataIn

    poke(c.io.SSEL, 0)

    //spin
    step(10)

    for(i <- 1 to c.w) {
      val mosi_bit = ((mosi & 0x8000) >> 15) & 0x01
      poke(c.io.MOSI, mosi_bit)

      step(2)
      poke(c.io.SCK, 1)
      step(4)
      poke(c.io.SCK, 0)
      mosi = mosi << 1
      step(3)
    }

    //spin
    step(5)
    expect(c.io.DATA, dataIn)

    poke(c.io.SSEL, 1)
    step(5)
  }
}

class SPIWaveTester extends ChiselFlatSpec {
  private val backendNames = Array[String]("firrtl", "verilator")
  for ( backendName <- backendNames ) {
    "SPIWave" should s"receive SPI data (with $backendName)" in {
      Driver(() => new SPIWave(16), backendName) {
        c => new SPIWaveTest(c)
      } should be (true)
    }
  }
}