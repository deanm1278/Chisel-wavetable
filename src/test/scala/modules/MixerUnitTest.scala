import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import modules.Mixer
import org.scalatest.Matchers
import scala.util.Random

class MixerTest(c: Mixer) extends PeekPokeTester(c) {
  val r = Random
  val max = (1 << 12) - 1

  step(1)

  for(n <- 1 to 25) {
    val pre = r.nextInt(1 << 12)
    val mul = r.nextInt(1 << 5)
    val env = r.nextInt(1 << 7)

    poke(c.io.PRE, pre)
    poke(c.io.MUL, mul)
    poke(c.io.ENV, env)
    step(1)
    val calc = pre + (env * mul)

    if(calc > max){ expect(c.io.POST, max) }
    else { expect(c.io.POST, calc) }
  }
}

class MixerTester extends ChiselFlatSpec {
  private val backendNames = Array[String]("firrtl", "verilator")
  for ( backendName <- backendNames ) {
    "Mixer" should s"multiply env signal and add it to the pre value (with $backendName)" in {
      Driver(() => new Mixer(12), backendName) {
        c => new MixerTest(c)
      } should be (true)
    }
  }
}