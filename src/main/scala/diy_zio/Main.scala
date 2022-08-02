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

trait BusinessLogic:
  def evenPicturesOf(topic: String): Boolean

object BusinessLogic:
  lazy val live: ZIO[Google, Nothing, BusinessLogic] =
    ZIO.fromFunction(make)
  def make(google: Google): BusinessLogic =
    topic => google.countPicturesOf(topic) % 2 == 0

trait Google:
  def countPicturesOf(topic: String): Int

object GoogleImpl:
  lazy val live: ZIO[Any, Nothing, Google] = ZIO.succeed(make)
  lazy val make: Google                    = topic => if topic == "cats" then 27193 else 183208

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
  Runtime.default.unsafeRunSync(program)

  lazy val program =
    for
      businessLogic <- DependencyGraph.live
      _             <- console.putStrLn(businessLogic.evenPicturesOf("cats").toString)
      _             <- console.putStrLn(businessLogic.evenPicturesOf("dogs").toString)
    yield ()
