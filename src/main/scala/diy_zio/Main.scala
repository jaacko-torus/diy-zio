package jaackotorus
package diy_zio

import zio.*

object Main extends App:
  Runtime.default.unsafeRunSync(program)

  lazy val program =
    for
      _    <- console.putStrLn("What is your name?")
      name <- ZIO.succeed("Julian") // console.getStrLn
      _    <- console.putStrLn(s"Hello $name")
    yield ()

//  def run(args: List[String]) = program.exitCode
