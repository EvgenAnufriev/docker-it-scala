package com.whisk.docker

import com.spotify.docker.client.messages.PortBinding
import org.scalatest.Suite

import scala.concurrent.duration._

trait DockerPostgresService extends DockerTestKitForAll { self: Suite =>

  def PostgresAdvertisedPort = 5432
  def PostgresExposedPort = 44444
  val PostgresUser = "nph"
  val PostgresPassword = "suitup"

  val postgresContainer = ContainerSpec("postgres:9.6.5")
    .withPortBindings((PostgresAdvertisedPort, PortBinding.of("0.0.0.0", PostgresExposedPort)))
    .withEnv(s"POSTGRES_USER=$PostgresUser", s"POSTGRES_PASSWORD=$PostgresPassword")
    .withReadyChecker(
      DockerReadyChecker
        .Jdbc(
          driverClass = "org.postgresql.Driver",
          urlFunc = port => s"jdbc:postgresql://${dockerClient.getHost}:$port/",
          user = PostgresUser,
          password = PostgresPassword
        )
        .looped(15, 1.second)
    )
    .toContainer

  override val managedContainers: ManagedContainers = postgresContainer.toManagedContainer
}
