import rescala._

import scala.util.continuations._

package object gear {
  implicit def fToShiftF[P,R](f:P=>R) =
    new PartialLayeredFunction[P,R]{
      def apply(v:P, thisfun: LayeredFunction[P,R]):P@cpsParam[R,R] =
        shift{k:(P => R) => f.apply(v)}
    }

  type LF[P,R] = LayeredFunction[P,R]

  type PLF[P,R] = PartialLayeredFunction[P,R]

  type CDF1[S,P,R] = ContextDependentFunction1[S,P,R]
  type CDF2[S1,S2,P,R] = ContextDependentFunction2[S1,S2,P,R]
  type CDF3[S1,S2,S3,P,R] = ContextDependentFunction3[S1,S2,S3,P,R]

  type CS[S] = ContextSource[S]

  def cache[S,R](src:ContextSource[S])(body: => R) = {
    src.withValue(Var(src())){body}
  }
}
