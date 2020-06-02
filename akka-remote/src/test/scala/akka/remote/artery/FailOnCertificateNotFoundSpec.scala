/*
 * Copyright (C) 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.remote.artery

import akka.testkit.AkkaSpec

class FailOnCertificateNotFoundSpec extends AkkaSpec(
  """
    |akka {
    |      actor {
    |        provider = remote
    |      }
    |      remote.warn-about-direct-use = off
    |      remote.artery {
    |        transport = tls-tcp
    |        enabled = on
    |        canonical {
    |          hostname = localhost
    |          port = 0
    |        }
    |        ssl.config-ssl-engine {
    |          key-store = "/file/not/found"
    |          trust-store = "/file/not/found"
    |        }
    |      }
    |    }
    """.stripMargin) {

  "Starting Artery with an SSL config referencing a file that does not exist" should {
    "fail and shut down the actor system" in {
      // This will try to create a remoting connection, but fail before even attempting
      // to create the connection because our keystore is not found:
      system.actorSelection("akka://name@127.0.0.1:1/foo/bar") ! 42
    }
  }
}
