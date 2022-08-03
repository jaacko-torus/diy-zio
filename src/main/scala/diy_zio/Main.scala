package jaackotorus
package diy_zio

//import zio.*
import diy.zio.*

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
  type BusinessLogic = Has[BusinessLogic.Service]

  object BusinessLogic:
    trait Service:
      def evenPicturesOf(topic: String): ZIO[Any, Nothing, Boolean]

    lazy val live: ZIO[Google, Nothing, Service] =
      ZIO.fromFunction(make)
    def make(google: Google): Service =
      (topic: String) => google.countPicturesOf(topic).map(_ % 2 == 0)

  def evenPicturesOf(topic: String): ZIO[BusinessLogic, Nothing, Boolean] =
    ZIO.accessM(_.get.evenPicturesOf(topic))

trait Google:
  def countPicturesOf(topic: String): ZIO[Any, Nothing, Int]

object GoogleImpl:
  lazy val live: ZIO[Any, Nothing, Google] = ZIO.succeed(make)
  lazy val make: Google = topic => ZIO.succeed(if topic == "cats" then 27193 else 183208)

object DependencyGraph:
  lazy val live: ZIO[Any, Nothing, businessLogic.BusinessLogic.Service] =
    for
      googleMaker        <- GoogleImpl.live
      businessLogicMaker <- businessLogic.BusinessLogic.live.provide(googleMaker)
    yield businessLogicMaker

  lazy val make: businessLogic.BusinessLogic.Service =
    val _google        = GoogleImpl.make
    val _businessLogic = businessLogic.BusinessLogic.make(_google)
    _businessLogic

object Main extends scala.App:
//  println("Hello World!")
////  Runtime.default.unsafeRunSync(program.provide(DependencyGraph.make))
  Runtime.default.unsafeRunSync(program)

  lazy val program =
    for
      logic <- DependencyGraph.live
      p     <- makeProgram.provideSome[ZEnv](_ `union` Has(logic))
    yield p

//  def makeProgram(businessLogic: BusinessLogic) =
  lazy val makeProgram =
    for
      env  <- ZIO.environment[console.Console & businessLogic.BusinessLogic]
      cats <- businessLogic.evenPicturesOf("cats")
      _    <- console.putStrLn(cats.toString)
      dogs <- businessLogic.evenPicturesOf("dogs")
      _    <- console.putStrLn(dogs.toString)
    yield ()
