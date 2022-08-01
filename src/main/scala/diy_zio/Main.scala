package jaackotorus
package diy_zio

//import _root_.zio.*
import zio.*

object Main extends App:
  Runtime.default.unsafeRunSync(program)

  lazy val program =
    for
      _    <- console.putStrLn("What is your name?")
      name <- ZIO.succeed("Julian") // console.getStrLn
      _    <- console.putStrLn(s"Hello $name")
    yield ()
