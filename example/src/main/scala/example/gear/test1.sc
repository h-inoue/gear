import rescala._
import gear._

def pmdef(src:Int)(cop:Cop[Int,String])(x:Int):String ={
  if(src <= 10) "pm1:" + x + " " + cop.proceed(src + x)
  else "pm1:cs is bigger than 10!! "
}

def pmdef2(src:Int)(cop:Cop[Int,String])(x:Int):String ={
  "pm2:" + x + " " + cop.proceed(src + x)
}

def pmdef3(src:Int)(cop:Cop[Int,String])(x:Int):String ={
  "pm3:" + x + " " + cop.proceed(src + x)
}

val sint = Var(3)
val src = new ContextSource(sint)

val sint2 = Var(30)
val src2 = new ContextSource(sint2)

val pm = new CDF1(src,pmdef)
val pm2 = new CDF1(src,pmdef2)
val p_pm2 = new CDF1(src2,pmdef2,(_:Int)<30)
val p_pm3 = new CDF1(src2,pmdef3,(x:Int) => x >= 30)

val lf1 = new LayeredFunction(fToShiftF((x:Int) => "."))
lf1 += pm
val lf2 = new LayeredFunction(pm)

val act1 = () => println(lf1(1))

act1()
sint2() = 15
act1()
sint() = 12
lf1 += p_pm2
lf1 += p_pm3
act1()
sint2() = 50
act1()