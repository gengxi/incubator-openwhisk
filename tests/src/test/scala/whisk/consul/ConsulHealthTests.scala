/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package whisk.consul

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.time.Span.convertDurationToSpan

import common.StreamLogging
import common.WskActorSystem
import whisk.common.ConsulClient
import whisk.core.WhiskConfig
import whisk.core.WhiskConfig.consulServer

@RunWith(classOf[JUnitRunner])
class ConsulHealthTests
    extends FlatSpec
    with ScalaFutures
    with Matchers
    with WskActorSystem
    with StreamLogging {

    implicit val testConfig = PatienceConfig(5.seconds)

    private val config = new WhiskConfig(consulServer)
    private val consul = new ConsulClient(config.consulServer)

    "Consul" should "have all components passing" in {
        val services = consul.catalog.services().futureValue

        val health = services.map { service => consul.health.service(service) }.toList
        val healthResults = Future.sequence(health).futureValue

        val filteredHealth = services.map { service => consul.health.service(service, true) }.toList
        val filteredHealthResults = Future.sequence(filteredHealth).futureValue

        healthResults should contain theSameElementsAs filteredHealthResults
    }

}
