import rescala._
import gear._


val sint = Var(3)
val src = new ContextSource(sint)

val sint2 = Var(30)
val src2 = new ContextSource(sint2)

val sf = new CDF1[Int,Int,Int](src,x => cc => y => {
  if(y>0){
    println(y,x)
    cc.thisfun(y-1)
  } else
    0
},x => x>0)
val lf = new LF[Int,Int](sf)

lf(5)
sint() = -10
lf.lift()(4)


