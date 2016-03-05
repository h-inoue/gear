package example.gear

import rescala._
import makro.SignalMacro.{SignalM => Signal}
import rescala.commons.time._
import gear._

object TimerTest extends App{
  val t = Timer(300)
  val st = t.time
  val stt = t.localTime.map(_.s)

  implicit val src:CS[Time] = new ContextSource(st)

  val lf = new LF(new CDF1[Time,Unit,Unit](src,x => cc => _ => {
    println("in lf at time " + x)
  }))

  val lf2 = new LF(new CDF1[Time,Unit,Unit](src,x => cc => _ => {
    println("in lf2 at time " + x)
    Thread.sleep(500)
    lf()
  }))

  new Thread{
    override def run() = {
      Timer.runAll
    }
  }.start

  while(true){
    lf()
    println()
    Thread.sleep(1000)
    println("normal")
    lf2()
    println()
    Thread.sleep(1000)
    src.withValue(Var(src.value())){
      println("value caching")
      lf2()
      println
    }
    Thread.sleep(1000)
  }
}
