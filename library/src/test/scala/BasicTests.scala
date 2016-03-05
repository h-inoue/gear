import gear._
import rescala._
import org.scalatest.FunSuite

class BasicTests extends FunSuite{
  test("predicate of CSF"){
    val sig = Var(0)
    val cs = new CS[Int](sig)

    val lf = new LF[Unit,Int]((_:Unit) => -1)
    lf += CDF(cs)(x => cc => _ => x, _ > 0)

    assert(lf() == -1)
    sig() = 1
    assert(lf() == 1)
  }

  test("context caching test"){
    val sig = Var(0)
    val cs = new ContextSource(sig)

    val lf1 = new LF((_:Unit) => -1)
    lf1 += CDF(cs,(x:Int) => cc => _ => x)

    val lf2 = new LF((_:Unit) => (0,0))
    lf2 += CDF(cs, (x:Int) => cc => _ => {
      val ret1 = lf1()
      // simulate asynchronous change
      sig() = 2
      val ret2 = lf1()
      (ret1,ret2)
    })

    var ret = lf2()
    assert(ret._1 != ret._2)

    ret = cs.cache{lf2()}
    assert(ret._1 == ret._2)
  }

  test("proceeding test"){
    val sig = Var(0)
    val cs = new ContextSource(sig)

    val lf = new LF((_:Unit) => 0)
    lf += CDF(cs)(x => cc => _ => x + cc.proceed(), _ > 0)
    lf += CDF(cs)(x => cc => _ => x + cc.proceed(), _ > 1)
    lf += CDF(cs)(x => cc => _ => x + cc.proceed(), _ > 2)
    lf += CDF(cs)(x => cc => _ => x + cc.proceed(), _ > 3)

    assert(lf() == 0)
    sig() = 2
    assert(lf() == 4)
    sig() = 4
    assert(lf() == 16)
  }
}
