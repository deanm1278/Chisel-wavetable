import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import modules.ADSR
import org.scalatest.Matchers
import scala.util.Random

class ADSRTest(c: ADSR) extends PeekPokeTester(c) {
  val r = Random

  for(n <- 1 to 25) {
    val aInterval = r.nextInt(1 << 8)
    val dInterval = r.nextInt(1 << 8)
    val rInterval = r.nextInt(1 << 8)
    val sLvl = r.nextInt(1 << 7)

    val stopCycle = r.nextInt((1 << 16))

    poke(c.io.A_INTERVAL, aInterval)
    poke(c.io.D_INTERVAL, dInterval)
    poke(c.io.SUS_LVL, sLvl)
    poke(c.io.R_INTERVAL, rInterval)
    poke(c.io.START, 1)
    step(1)

    for(k <- 1 to (1 << 16)){
      if(k < stopCycle){
        if(k == aInterval * 127){ expect(c.io.OUTVAL, 127) }
        if(k == (aInterval * 127) + (dInterval * (127 - sLvl))){ expect(c.io.OUTVAL, sLvl) }
      }
      if(k == stopCycle){ poke(c.io.START, 0) }
      if(k > stopCycle){

      }
      step(1)
    }

  }
}

class ADSRTester extends ChiselFlatSpec {
  private val backendNames = Array[String]("firrtl", "verilator")
  for ( backendName <- backendNames ) {
    "ADSR" should s"create an ADSR envelope (with $backendName)" in {
      Driver(() => new ADSR(8), backendName) {
        c => new ADSRTest(c)
      } should be (true)
    }
  }
}