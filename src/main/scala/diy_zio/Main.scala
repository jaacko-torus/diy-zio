package jaackotorus
package diy_zio

//import zio.*
import diy.zio.*

object businessLogic:
  type BusinessLogic = Has[BusinessLogic.Service]

  object BusinessLogic:
    trait Service:
      def evenPicturesOf(topic: String): ZIO[Any, Nothing, Boolean]

    lazy val live: ZLayer[google.Google, Nothing, BusinessLogic] =
      ZLayer.fromService(make)
    def make(_google: google.Google.Service): Service =
      topic => _google.countPicturesOf(topic).map(_ % 2 == 0)

  def evenPicturesOf(topic: String): ZIO[BusinessLogic, Nothing, Boolean] =
    ZIO.accessM(_.get.evenPicturesOf(topic))

object google:
  type Google = Has[Google.Service]
  object Google:
    trait Service:
      def countPicturesOf(topic: String): ZIO[Any, Nothing, Int]
  def countPicturesOf(topic: String): ZIO[Google, Nothing, Int] =
    ZIO.accessM(_.get.countPicturesOf(topic))

object GoogleImpl:
  lazy val live: ZLayer[Any, Nothing, google.Google] = ZLayer.succeed(make)
  def make: google.Google.Service = topic => ZIO.succeed(if topic == "cats" then 27193 else 183208)

object controller:
  type Controller = Has[Controller.Service]
  object Controller:
    trait Service:
      def run: ZIO[Any, Nothing, Unit]

    lazy val live: ZLayer[businessLogic.BusinessLogic & console.Console, Nothing, Controller] =
      ZLayer.fromServices(make)

    def make(bl: businessLogic.BusinessLogic.Service, c: console.Console.Service): Service =
      new:
        lazy val run: ZIO[Any, Nothing, Unit] =
          for
            cats <- bl.evenPicturesOf("cats")
            _    <- c.putStrLn(cats.toString)
            dogs <- bl.evenPicturesOf("dogs")
            _    <- c.putStrLn(dogs.toString)
          yield ()

  lazy val run: ZIO[Controller, Nothing, Unit] =
    ZIO.accessM(_.get.run)

object DependencyGraph:
  lazy val live: ZLayer[Any, Nothing, controller.Controller] =
    for
      (googleMaker, consoleMaker) <- GoogleImpl.live.zip(console.Console.live)
      businessLogicMaker          <- businessLogic.BusinessLogic.live.provide(googleMaker)
      controllerMaker <- controller.Controller.live.provide(
        businessLogicMaker `union` consoleMaker,
      )
    yield controllerMaker

  lazy val make: controller.Controller.Service =
    val (_google, _console) = (GoogleImpl.make, console.Console.make)
    val _businessLogic      = businessLogic.BusinessLogic.make(_google)
    val _controller         = controller.Controller.make(_businessLogic, _console)
    _controller

object Main extends scala.App:
  Runtime.default.unsafeRunSync(program)

  lazy val program =
    // DependencyGraph.live.zio.flatMap(_.get.run)
    // DependencyGraph.live.zio.flatMap(r => controller.run.provide(r))
    controller.run.provideLayer(DependencyGraph.live)
