// Copyright (c) 2016-2025 Association of Universities for Research in Astronomy, Inc. (AURA)
// Copyright (c) 2016-2025 Grackle Contributors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package grackle.sql.test

import cats.effect.IO
import io.circe.literal._
import munit.CatsEffectSuite

import grackle._
import grackle.test.GraphQLResponseTests.assertWeaklyEqualIO

trait SqlUnionLimitSuite extends CatsEffectSuite {
  def mapping: Mapping[IO]

  test("limit with sibling lists and a singular multi-hop child") {
    val query = """
      query {
        root(limit: 2) {
          id
          name
          lists {
            listA { id }
            listB { id }
          }
          firstTarget { id name }
        }
      }
    """

    val expected = json"""
      {
        "data" : {
          "root" : [
            {
              "id" : "r0",
              "name" : "Root 0",
              "lists" : {
                "listA" : [ { "id" : "a00" }, { "id" : "a01" } ],
                "listB" : [ { "id" : "b00" }, { "id" : "b01" } ]
              },
              "firstTarget" : { "id" : "t0", "name" : "Target 0" }
            },
            {
              "id" : "r1",
              "name" : "Root 1",
              "lists" : {
                "listA" : [ { "id" : "a10" }, { "id" : "a11" } ],
                "listB" : [ { "id" : "b10" }, { "id" : "b11" } ]
              },
              "firstTarget" : { "id" : "t2", "name" : "Target 2" }
            }
          ]
        }
      }
    """

    val res = mapping.compileAndRun(query)

    assertWeaklyEqualIO(res, expected)
  }

  test("no limit with sibling lists and a singular multi-hop child") {
    val query = """
      query {
        root {
          id
          lists {
            listA { id }
            listB { id }
          }
          firstTarget { id }
        }
      }
    """

    val expected = json"""
      {
        "data" : {
          "root" : [
            {
              "id" : "r0",
              "lists" : {
                "listA" : [ { "id" : "a00" }, { "id" : "a01" } ],
                "listB" : [ { "id" : "b00" }, { "id" : "b01" } ]
              },
              "firstTarget" : { "id" : "t0" }
            },
            {
              "id" : "r1",
              "lists" : {
                "listA" : [ { "id" : "a10" }, { "id" : "a11" } ],
                "listB" : [ { "id" : "b10" }, { "id" : "b11" } ]
              },
              "firstTarget" : { "id" : "t2" }
            },
            {
              "id" : "r2",
              "lists" : {
                "listA" : [ { "id" : "a20" }, { "id" : "a21" } ],
                "listB" : [ { "id" : "b20" }, { "id" : "b21" } ]
              },
              "firstTarget" : null
            }
          ]
        }
      }
    """

    val res = mapping.compileAndRun(query)

    assertWeaklyEqualIO(res, expected)
  }
}
