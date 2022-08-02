package jaackotorus
package diy_zio

//import _root_.zio.*
import zio.*

//object Main extends App:
//  Runtime.default.unsafeRunSync(program)
//
//  lazy val program =
//    for
//      _    <- console.putStrLn("What is your name?")
//      name <- ZIO.succeed("Julian") // console.getStrLn
//      _    <- console.putStrLn(s"Hello $name")
//    yield ()

object businessLogic:
  trait BusinessLogic:
    def evenPicturesOf(topic: String): ZIO[Any, Nothing, Boolean]

  object BusinessLogic:
    lazy val live: ZIO[Google, Nothing, BusinessLogic] =
      ZIO.fromFunction(make)
    def make(google: Google): BusinessLogic =
      topic => google.countPicturesOf(topic).map(_ % 2 == 0)

  def evenPicturesOf(topic: String): ZIO[BusinessLogic, Nothing, Boolean] =
    ZIO.accessM(_.evenPicturesOf(topic))

import businessLogic.BusinessLogic

trait Google:
  def countPicturesOf(topic: String): ZIO[Any, Nothing, Int]

object GoogleImpl:
  lazy val live: ZIO[Any, Nothing, Google] = ZIO.succeed(make)
  lazy val make: Google = topic => ZIO.succeed(if topic == "cats" then 27193 else 183208)

object DependencyGraph:
  lazy val live: ZIO[Any, Nothing, BusinessLogic] =
    for
      googleMaker        <- GoogleImpl.live
      businessLogicMaker <- BusinessLogic.live.provide(googleMaker)
    yield businessLogicMaker

  lazy val make: BusinessLogic =
    val google        = GoogleImpl.make
    val businessLogic = BusinessLogic.make(google)
    businessLogic

object Main extends App:
  Runtime.default.unsafeRunSync(program.provide(DependencyGraph.make))

  lazy val program =
    for
      cats <- businessLogic.evenPicturesOf("cats")
      _    <- console.putStrLn(cats.toString)
      dogs <- businessLogic.evenPicturesOf("dogs")
      _    <- console.putStrLn(dogs.toString)
    yield ()
