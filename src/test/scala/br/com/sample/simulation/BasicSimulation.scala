package br.com.sample.simulation

import br.com.sample.simulation.Operations.{retrieve, save}
import io.gatling.core.Predef._
import io.gatling.core.feeder.Record
import io.gatling.core.scenario.Simulation
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._

import scala.concurrent.duration._
import scala.util.Random

class BasicSimulation extends Simulation {

  /**
   * Parameters
   */
  private val baseUrl = "https://8ee7c5ae-646c-44ad-a40f-bee43f631cea.mock.pstmn.io"
  private val users = "users.csv"
  private val maxUsers = 10
  private val rampDuration = 1
  private val rampDurationUnit = MINUTES
  private val duration = 2
  private val durationUnit = MINUTES

  /**
   * Feeders
   */
  private val userFeeder: Seq[Record[Any]] = csv(users).readRecords
  private val requestFeeder = Iterator.continually({
    val head = Random.shuffle(userFeeder).head
    val id = head("user")
    Map("userId" -> id, "userName" -> s"User $id")
  })

  def perHour(rate: Double): Double = rate / 3600

  def perMinute(rate: Double): Double = rate / 60

  before {
    println("================================= Running Test =================================")
    println(s"baseUrl=$baseUrl")
    println(s"users=$users")
    println(s"maxUsers=$maxUsers")
    println(s"duration=$duration")
    println(s"durationUnit=$durationUnit")
    println(s"rampDuration=$rampDuration")
    println(s"rampDurationUnit=$rampDurationUnit")
  }

  /**
   * Protocol
   */
  private val httpProtocol = http.baseUrl(baseUrl).contentTypeHeader("application/json")

  /**
   * Scenario
   */
  private val firstScenario = scenario(s"Save users test with $maxUsers users/sec")
    .feed(requestFeeder)
    .exec(save)
    .exec(retrieve)

  /**
   * Request frequency
   */
  private val requests = {
    rampUsersPerSec(1) to maxUsers during FiniteDuration(rampDuration, rampDurationUnit)
    constantUsersPerSec(perMinute(maxUsers)) during FiniteDuration(duration, durationUnit)
  }

  setUp(firstScenario.inject(requests)).protocols(httpProtocol)

  after {
    println("================================= Test Completed =================================")
  }
}

object Operations {
  val save: ChainBuilder =
    exec(
      http("Save User")
        .post("/users")
        .body(ElFileBody("bodies/save.json")).asJson
        .check(jsonPath("$.id").exists)
        .check(jsonPath("$.name").exists)
        .check(jsonPath("$.id").optional.saveAs("result"))
        .check(status.is(200))
        .check(bodyString.saveAs("responseBody"))
    )

  val retrieve: ChainBuilder =
    doIf(session => session.attributes.isDefinedAt("result")) {
      exec(
        http("Retrieve")
          .get("/users")
          .check(jsonPath("$.id").exists)
          .check(jsonPath("$.name").exists)
          .check(status.is(200))
          .check(bodyString.saveAs("responseBody"))
      )
    }

  val log: ChainBuilder = exec(session => {
    println("============> Session attributes")
    println(session)
    session
  })
}
