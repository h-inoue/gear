package gear

import scala.util.{Try, Success, Failure}
import scala.util.continuations._

trait PartialLayeredFunction[P,R]{
  private[gear] def apply(v:P, thisfun:LayeredFunction[P,R]):P@cpsParam[R,R]

  def pred: Unit => Boolean = _ => true

  private[gear] def cache[T](thunk: => T) = thunk
}

case class Cop[P,R](thisfun: P => R, proceed: P => R)

class ProceedingFunctionNotFoundException extends RuntimeException

object CDF{
  def apply[S,P,R](src: ContextSource[S], f: S => Cop[P,R] => P => R) = {
    new ContextDependentFunction1(src,f)
  }

  def apply[S,P,R](src: ContextSource[S])
                  (f: S => Cop[P,R] => P => R,
                   p: S => Boolean) = {
    new ContextDependentFunction1(src,f,p)
  }

  def apply[S1,S2,P,R](src1: ContextSource[S1],
                       src2: ContextSource[S2],
                       f: S1 => S2 => Cop[P,R] => P => R) = {
    new ContextDependentFunction2(src1,src2,f)
  }

  def apply[S1,S2,P,R](src1: ContextSource[S1], src2: ContextSource[S2])
                      (f: S1 => S2 => Cop[P,R] => P => R,
                       p: S1 => S2 => Boolean) = {
    new ContextDependentFunction2(src1,src2,f,p)
  }

  def apply[S1,S2,S3,P,R](src1: ContextSource[S1], src2: ContextSource[S2], src3: ContextSource[S3])
                         (f: S1 => S2 => S3 => Cop[P,R] => P => R) = {
    new ContextDependentFunction3(src1,src2,src3,f)
  }
}


class ContextDependentFunction1[S, P, R](src: ContextSource[S],
                                         f: S => Cop[P,R] => P => R,
                                         p: S => Boolean = (_:S) => true)
  extends PartialLayeredFunction[P,R]{

  private[gear] def apply(v:P, thisfun:LayeredFunction[P,R]):P@cpsParam[R,R]  =
    shift{
      proceed:(P => R) =>
        val tmp_s = src.value()
        if (p(tmp_s)) f(tmp_s)(Cop(thisfun.resetDispatch _,proceed))(v)
        else proceed(v)
    }

  override def pred = _ => p(src.value())

  override def cache[T](thunk: => T) =
    src.cache(thunk)
}

class ContextDependentFunction2[S1, S2, P, R]
(s1: ContextSource[S1],
 s2: ContextSource[S2],
 f: S1 => S2 => Cop[P,R] => P => R,
 p: S1 => S2 => Boolean = (_:S1) => (_:S2) => true)
  extends PartialLayeredFunction[P,R]{

  private[gear] def apply(v:P, thisfun:LayeredFunction[P,R]):P@cpsParam[R,R]  =
    shift{
      proceed:(P => R) =>
        val tmp_s1 = s1()
        val tmp_s2 = s2()
        if (p(tmp_s1)(tmp_s2)) f(tmp_s1)(tmp_s2)(
          Cop(thisfun.resetDispatch _,proceed))(v)
        else proceed(v)
    }

  override def pred = _ => p(s1.value())(s2.value())

  override def cache[T](thunk: => T) =
    s1.cache(s2.cache(thunk))
}

class ContextDependentFunction3[S1, S2, S3, P, R]
(s1: ContextSource[S1],
 s2: ContextSource[S2],
 s3: ContextSource[S3],
 f: S1 =>S2 => S3 => Cop[P,R] => P => R,
 p: (S1 => S2 => S3 => Boolean) = (_:S1) => (_:S2) => (_:S3) => true)
   extends PartialLayeredFunction[P,R]{

  private[gear] def apply(v:P, thisfun:LayeredFunction[P,R]):P@cpsParam[R,R]  =
    shift{
      proceed:(P => R) =>
        val tmp_s1 = s1()
        val tmp_s2 = s2()
        val tmp_s3 = s3()
        if (p(tmp_s1)(tmp_s2)(tmp_s3)) f(tmp_s1)(tmp_s2)(tmp_s3)(
          Cop(thisfun.resetDispatch _,proceed))(v)
        else proceed(v)
    }

  override def pred = _ => p(s1.value())(s2.value())(s3.value())

  override def cache[T](thunk: => T) =
    s1.cache(s2.cache(s3.cache(thunk)))
}

// layered method is a set of sensitive function of same signatures
class LayeredFunction[P,R](plf: PartialLayeredFunction[P,R])
  extends (P => R) {
  var plfs:List[PartialLayeredFunction[P,R]] = List(plf)

  private def dispatch(plfs:List[PartialLayeredFunction[P,R]], v:P): R @cpsParam[R,R] = {
    var param = v
    plfs match {
      case plf :: rest => {
        param = plf(v,this)
        dispatch(rest, param)
      }
      /* Todo: Unit => Unit maybe ok */
      case Nil => throw new ProceedingFunctionNotFoundException
    }
  }

  def lift(): P => Try[R] = (v:P) => try{
    Success(this.apply(v))
  } catch {
    case e: ProceedingFunctionNotFoundException => Failure(e)
    case e: Throwable => throw e
  }

  def +=(plf: PartialLayeredFunction[P,R]) = {
    plfs = plf :: plfs
  }

  def -=(plf: PartialLayeredFunction[P,R]) = {
    plfs = plfs.filter(_ eq plf)
  }

  def isDefinedNow: Boolean ={
    plfs.foldRight(false)((plf,b) => b || plf.pred())
  }

  private[gear] def resetDispatch(v:P): R = reset {
    dispatch(plfs, v)
  }

  def apply(v:P):R = resetDispatch(v)
}
