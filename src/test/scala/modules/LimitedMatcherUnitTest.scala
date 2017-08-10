import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import modules.LimitedMatcher
import org.scalatest.Matchers
import scala.util.Random

class LimitedMatcherTest(c: LimitedMatcher) extends PeekPokeTester(c) {
  val r = Random
  poke(c.io.EN, 0)
  poke(c.io.SET, 0)

  for(k <- 0 until 10){

    val cminor = r.nextInt(1 << 4)
    val cmajor = r.nextInt(1 << 2)

    poke(c.io.major_target, cmajor)
    poke(c.io.minor_target, cminor)

    step(1)
    poke(c.io.SET, 1)
    step(1)
    poke(c.io.SET, 0)

    expect(c.io.major_output, cmajor)
    expect(c.io.minor_output, cminor)

    val tminor = r.nextInt(1 << 4)
    val tmajor = r.nextInt(1 << 2)

    poke(c.io.major_target, tmajor)
    poke(c.io.minor_target, tminor)
    poke(c.io.EN, 1)
    step(64)
    poke(c.io.EN, 0)
    expect(c.io.major_output, tmajor)
    expect(c.io.minor_output, tminor)
  }

}

class LimitedMatcherTester extends ChiselFlatSpec {
  private val backendNames = Array[String]("firrtl", "verilator")
  for ( backendName <- backendNames ) {
    "LimitedMatcher" should s"match major and minor numbers across set boundaries (with $backendName)" in {
      Driver(() => new LimitedMatcher(14, 3, 2, 4), backendName) {
        c => new LimitedMatcherTest(c)
      } should be (true)
    }
  }
}