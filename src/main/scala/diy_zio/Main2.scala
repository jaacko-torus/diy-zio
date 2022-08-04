package jaackotorus
package diy_zio

// import zio.*
import diy.zio.*

import java.io.IOException

object scope2:
  object businessLogic:
    type BusinessLogic = Has[BusinessLogic.Service]

    object BusinessLogic:
      trait Service:
        def evenPicturesOf(topic: String): ZIO[google.Google, Nothing, Boolean]

      lazy val any: ZLayer[BusinessLogic, Nothing, BusinessLogic] =
        ZLayer.requires

      lazy val live: ZLayer[Any, Nothing, BusinessLogic] =
        ZLayer.succeed(make)

      def make: Service =
        topic => google.countPicturesOf(topic).map(_ % 2 == 0)

    def evenPicturesOf(topic: String): ZIO[BusinessLogic & google.Google, Nothing, Boolean] =
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
        def run
            : ZIO[businessLogic.BusinessLogic & google.Google & console.Console, IOException, Unit]

      lazy val any: ZLayer[Controller, Nothing, Controller] =
        ZLayer.requires

      lazy val live: ZLayer[Any, Nothing, Controller] =
        ZLayer.succeed(make)

      def make: Service =
        new:
          override lazy val run: ZIO[
            businessLogic.BusinessLogic & google.Google & console.Console,
            IOException,
            Unit,
          ] =
            for
              cats <- businessLogic.evenPicturesOf("cats")
              _    <- console.putStrLn(cats.toString)
              dogs <- businessLogic.evenPicturesOf("dogs")
              _    <- console.putStrLn(dogs.toString)
            yield ()

    lazy val run: ZIO[
      Controller & businessLogic.BusinessLogic & google.Google & console.Console,
      IOException,
      Unit,
    ] =
      ZIO.accessM(_.get.run)

  object DependencyGraph:
    lazy val env =
      GoogleImpl.live ++ businessLogic.BusinessLogic.live ++ controller.Controller.live ++
        console.Console.live

    lazy val partial =
      GoogleImpl.live ++ businessLogic.BusinessLogic.live ++ controller.Controller.live /*++
        console.Console.live*/

object Main2 extends scala.App:
  Runtime.default.unsafeRunSync(program)

  import scope2.*

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

  lazy val program =
    // DependencyGraph.live.zio.flatMap(_.get.run)
    // DependencyGraph.live.zio.flatMap(r => controller.run.provide(r))
    // controller.run.provideLayer(DependencyGraph.env)
    // controller.run.provideLayer(FancyConsole.live >>> DependencyGraph.partial)
    // controller.run
    //   .provideSomeLayer[console.Console](DependencyGraph.partial)
    //   .provideLayer(FancyConsole.live)
    controller.run.provideCustomLayer(DependencyGraph.partial)
