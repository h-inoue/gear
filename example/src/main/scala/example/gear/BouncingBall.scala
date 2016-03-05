package example.gear

import java.awt.{Color, Dimension, Graphics2D, Point}

import gear._
import makro.SignalMacro.{SignalM => Signal}
import rescala._

import scala.swing.{MainFrame, Panel, SimpleSwingApplication, Swing}
import scala.util.{Failure, Success, Try}

object BouncingBall extends SimpleSwingApplication {
  lazy val application = new BouncingBall
  def top = application.frame

  override def main(args: Array[String]) {
    super.main(args)
    while (true) {
      Swing onEDTWait { application.tick() += 1 }
      Thread sleep 20
    }
  }
}

class BouncingBall {
  val Size = 50
  val Max_X = 600
  val Max_Y = 600
  val initPosition = new Point(20, 10)
  val speed = new Point(10,8)

  val tick = Var(0)

  // Signals for x and y position
  // entirely functionally dependent on time (ticks)
  val x = Signal {
    val width = Max_X - Size
    val d = speed.x * tick() + initPosition.x
    if ((d / width) % 2 == 0) d % width else width - d % width
  }
  val y = Signal {
    val width = Max_Y - Size
    val d = speed.y * tick() + initPosition.y
    if ((d / width) % 2 == 0) d % width else width - d % width
  }

  val xcs = new CS(x)
  def print_pos(str:String)(sv:Int)(cc:Cop[Unit,Unit]) = (_:Unit) =>
    println(str + sv)

  def ppsf(str:String,p:Int=>Boolean) = new CDF1(xcs,print_pos(str) _,p)

  def changeColor(c:Color)(sv:Int)(cc:Cop[Unit,Color]) = (_:Unit) =>
    Try(cc.proceed()) match {
      case Success(x) => new Color(
        (x.getRed + c.getRed)/2,
        (x.getGreen + c.getGreen)/2,
        (x.getBlue + c.getBlue)/2)
      case Failure(_) => c
    }

  def ccsf(c:Color,p:Int=>Boolean) = new CDF1(xcs,changeColor(c) _,p)

  val lf = new LF(
    List(ppsf("right",(_:Int)>=300),
      ppsf("left",(_:Int)<300)))

  val cclf = new LF[Unit,Color]((_:Unit)=>Color.WHITE)
  List(ccsf(Color.BLUE,_>=300),
    ccsf(Color.GREEN,_<150),
    ccsf(Color.RED,_<300),
    ccsf(Color.PINK,_>450)).foreach(cclf += _)


  tick.changed += ((_ : Int) => {
    //lf()
    frame.repaint
  })

  // drawing code 
  val frame = new MainFrame {
    contents = new Panel() {
      preferredSize = new Dimension(600, 600)
      override def paintComponent(g: Graphics2D) {
        g.setColor(cclf())
        g.fillOval(x.get, y.get, Size, Size)
      }
    }
  }
}
