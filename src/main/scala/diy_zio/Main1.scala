package jaackotorus
package diy_zio

// import zio.*
import diy.zio.*

import java.io.IOException

object scope1:
  object businessLogic:
    type BusinessLogic = Has[BusinessLogic.Service]

    object BusinessLogic:
      trait Service:
        def evenPicturesOf(topic: String): ZIO[Any, Nothing, Boolean]

      lazy val any: ZLayer[BusinessLogic, Nothing, BusinessLogic] =
        ZLayer.requires

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
      lazy val any: ZLayer[Google, Nothing, Google] = ZLayer.requires
    def countPicturesOf(topic: String): ZIO[Google, Nothing, Int] =
      ZIO.accessM(_.get.countPicturesOf(topic))

  object GoogleImpl:
    lazy val live: ZLayer[Any, Nothing, google.Google] = ZLayer.succeed(make)
    def make: google.Google.Service = topic =>
      ZIO.succeed(if topic == "cats" then 27193 else 183208)

  object controller:
    type Controller = Has[Controller.Service]
    object Controller:
      trait Service:
        def run: ZIO[Any, IOException, Unit]

      lazy val any: ZLayer[Controller, Nothing, Controller] =
        ZLayer.requires

      lazy val live: ZLayer[businessLogic.BusinessLogic & console.Console, Nothing, Controller] =
        ZLayer.fromServices(make)

      def make(bl: businessLogic.BusinessLogic.Service, c: console.Console.Service): Service =
        new:
          override lazy val run: ZIO[Any, IOException, Unit] =
            for
              cats <- bl.evenPicturesOf("cats")
              _    <- c.putStrLn(cats.toString)
              dogs <- bl.evenPicturesOf("dogs")
              _    <- c.putStrLn(dogs.toString)
            yield ()

    lazy val run: ZIO[Controller, IOException, Unit] =
      ZIO.accessM(_.get.run)

  object DependencyGraph:
    lazy val env: ZLayer[Any, Nothing, controller.Controller] =
      (GoogleImpl.live >>> businessLogic.BusinessLogic.live ++ console.Console.live) >>>
        controller.Controller.live

    lazy val partial: ZLayer[console.Console, Nothing, controller.Controller] =
      ((GoogleImpl.live >>> businessLogic.BusinessLogic.live) ++ console.Console.any) >>>
        controller.Controller.live

object Main1 extends scala.App:
  Runtime.default.unsafeRunSync(program)

  import scope1.*

  object FancyConsole:
    lazy val any: ZLayer[console.Console, Nothing, console.Console] =
      ZLayer.requires

    lazy val live: ZLayer[Any, Nothing, console.Console] =
      ZLayer.succeed(make)

    lazy val make: console.Console.Service = new:
      override def putStrLn(line: String): ZIO[Any, Nothing, Unit] =
        ZIO.succeed(scala.Console.println(scala.Console.GREEN + line + scala.Console.RESET))
      override def getStrLn(): ZIO[Any, Nothing, Unit] =
        ZIO.succeed(scala.io.StdIn.readLine())
      // For compat with the real ZIO
      def putStr(line: String): ZIO[Any, Nothing, Unit] =
        ZIO.succeed(scala.Console.print(scala.Console.GREEN + line + scala.Console.RESET))
      def putStrLnErr(line: String): ZIO[Any, Nothing, Unit] =
        ZIO.succeed(scala.Console.err.println(scala.Console.RED + line + scala.Console.RESET))
      def putStrErr(line: String): ZIO[Any, Nothing, Unit] =
        ZIO.succeed(scala.Console.err.print(scala.Console.RED + line + scala.Console.RESET))

  object FakeBusinessLogic:
    lazy val live: ZLayer[Any, Nothing, businessLogic.BusinessLogic] =
      ZLayer.succeed(make)

    def make: businessLogic.BusinessLogic.Service =
      topic => ZIO.succeed(false)

  lazy val fakeEnv =
    (FakeBusinessLogic.live ++ console.Console.live) >>>
      controller.Controller.live

  lazy val program =
    // DependencyGraph.live.zio.flatMap(_.get.run)
    // DependencyGraph.live.zio.flatMap(r => controller.run.provide(r))
    controller.run.provideLayer(fakeEnv)
    // controller.run.provideLayer(FancyConsole.live >>> DependencyGraph.partial)
    // controller.run.provideSomeLayer(DependencyGraph.partial).provideLayer(FancyConsole.live)
    // controller.run.provideCustomLayer(DependencyGraph.partial)
