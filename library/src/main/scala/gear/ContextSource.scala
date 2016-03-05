package gear

import rescala._

import scala.util.DynamicVariable

case class ContextSource[T](val src: Signal[T]) {
  val enclosing = new DynamicVariable[Signal[T]](src)

  def value = enclosing.value
  def apply():T = enclosing.value()

  def withValue[S](newval:Signal[T])(thunk: => S): S = {
    enclosing.withValue(newval){thunk}
  }

  def cache[S](thunk: => S): S = {
    withValue(Var(value())){thunk}
  }
}

