package org.jordimasip.stream

import java.nio.file.{ Path, Paths }
import java.nio.file.StandardOpenOption
import java.nio.file.StandardOpenOption._
import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.stream.{ ActorMaterializer, IOResult }
import akka.stream.scaladsl.{ FileIO, RunnableGraph, Source, Sink } //<co id="scaladsl"/>
import akka.util.ByteString

import com.typesafe.config.{ Config, ConfigFactory }

object StreamingCopy extends App {
  // executar
  // run C:\Users\Jordi\akkaProjectes\provaStreams\textEntrada.txt C:\Users\Jordi\akkaProjectes\provaStreams\textSortida.txt
  val config = ConfigFactory.load()
  val maxLine = config.getInt("log-stream-processor.max-line")

  if (args.length != 2) {
    System.err.println("Falten args: input-file output-file")
    System.exit(1)
  }

  val inputFile = FileArg.shellExpanded(args(0))
  val outputFile = FileArg.shellExpanded(args(1))

  val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(inputFile)
  val sink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(outputFile, Set(CREATE, WRITE, APPEND))
  val runnableGraph: RunnableGraph[Future[IOResult]] = source.to(sink)

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  runnableGraph.run().foreach{ result =>
    println(s"${result.status}, ${result.count} bytes read.")
    system.terminate()
  }
}
