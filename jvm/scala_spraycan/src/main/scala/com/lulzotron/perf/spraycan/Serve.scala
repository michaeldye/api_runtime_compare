package com.lulzotron.perf.spraycan

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.io.IO
import spray.can.Http
import spray.routing.SimpleRoutingApp
import com.lulzotron.perf.seq.SequenceGenerator

object Serve extends App with SimpleRoutingApp {
  val gen = new SequenceGenerator()
  implicit val system = ActorSystem("seq")

  startServer(interface = "localhost", port = 9009) {
    pathPrefix("api") {
      pathPrefix("count" / IntNumber) { upTo =>
        get {
          complete {
            gen.count(upTo, SequenceGenerator.plain)
          }
        }
      } ~
      pathPrefix("fib" / IntNumber) { n =>
        get {
          complete {
            gen.fib(n, SequenceGenerator.plain)
          }
        }
      }
    }
  }
}