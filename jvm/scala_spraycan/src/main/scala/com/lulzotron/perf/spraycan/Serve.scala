package com.lulzotron.perf.spraycan

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.io.IO
import spray.can.Http
import spray.routing.SimpleRoutingApp
import com.lulzotron.perf.seq.SequenceGenerator

object Serve extends App with SimpleRoutingApp {
  implicit val system = ActorSystem("seq")

  lazy val fibs: Stream[BigInt] = BigInt(0) #:: BigInt(1) #:: fibs.zip(fibs.tail).map { n => n._1 + n._2 }
  def plain(s: Seq[Any]): String = { s.mkString(" ") }

  startServer(interface = "localhost", port = 9009) {
    pathPrefix("api") {
      pathPrefix("count" / IntNumber) { upTo =>
        get {
          complete {
            plain(1 until upTo + 1)
          }
        }
      } ~
      pathPrefix("fib" / IntNumber) { n =>
        get {
          complete {
            plain(fibs take n + 1)
          }
        }
      }
    }
  }
}