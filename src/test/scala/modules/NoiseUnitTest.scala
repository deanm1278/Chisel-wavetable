import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import modules.Noise
import org.scalatest.Matchers
import scala.util.Random

class NoiseTest(c: Noise) extends PeekPokeTester(c) {
  val r = Random
  val per = r.nextInt(1 << 5)

  poke(c.io.period, per)

  //kinda no way to test since it's supposed to generate random numbers, but lets just see w/ the simulator
  for(k <- 0 until 256){
    step(1)
  }

}

class NoiseTester extends ChiselFlatSpec {
  private val backendNames = Array[String]("firrtl", "verilator")
  for ( backendName <- backendNames ) {
    "Noise" should s"multiply env signal and add it to the pre value (with $backendName)" in {
      Driver(() => new Noise(5), backendName) {
        c => new NoiseTest(c)
      } should be (true)
    }
  }
}