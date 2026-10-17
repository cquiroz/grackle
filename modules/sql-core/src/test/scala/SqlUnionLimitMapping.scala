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

import grackle._
import grackle.Query.{Binding, Limit, Unique}
import grackle.QueryCompiler.{Elab, SelectElaborator}
import grackle.Value.{AbsentValue, IntValue, NullValue}
import grackle.syntax._

trait SqlUnionLimitMapping[F[_]] extends SqlTestMapping[F] {

  object root extends TableDef("union_limit_root") {
    val id = col("id", varchar)
    val name = col("name", nullable(varchar))
  }

  object listA extends TableDef("union_limit_lista") {
    val id = col("id", varchar)
    val rootId = col("root_id", nullable(varchar))
  }

  object listB extends TableDef("union_limit_listb") {
    val id = col("id", varchar)
    val rootId = col("root_id", nullable(varchar))
  }

  object assoc extends TableDef("union_limit_assoc") {
    val rootId = col("root_id", varchar)
    val targetId = col("target_id", varchar)
  }

  object target extends TableDef("union_limit_target") {
    val id = col("id", varchar)
    val name = col("name", nullable(varchar))
  }

  val schema =
    schema"""
      type Query {
        root(limit: Int): [Root!]!
      }
      type Root {
        id: String!
        name: String
        lists: Lists!
        firstTarget: Target
      }
      type Lists {
        listA: [ElemA!]!
        listB: [ElemB!]!
      }
      type ElemA {
        id: String!
      }
      type ElemB {
        id: String!
      }
      type Target {
        id: String!
        name: String
      }
    """

  val QueryType = schema.ref("Query")
  val RootType = schema.ref("Root")
  val ListsType = schema.ref("Lists")
  val ElemAType = schema.ref("ElemA")
  val ElemBType = schema.ref("ElemB")
  val TargetType = schema.ref("Target")

  val typeMappings =
    List(
      ObjectMapping(
        tpe = QueryType,
        fieldMappings = List(
          SqlObject("root")
        )
      ),
      ObjectMapping(
        tpe = RootType,
        fieldMappings = List(
          SqlField("id", root.id, key = true),
          SqlField("name", root.name),
          SqlObject("lists"),
          SqlObject("firstTarget", Join(root.id, assoc.rootId), Join(assoc.targetId, target.id))
        )
      ),
      ObjectMapping(
        tpe = ListsType,
        fieldMappings = List(
          SqlField("id", root.id, key = true, hidden = true),
          SqlObject("listA", Join(root.id, listA.rootId)),
          SqlObject("listB", Join(root.id, listB.rootId))
        )
      ),
      ObjectMapping(
        tpe = ElemAType,
        fieldMappings = List(
          SqlField("id", listA.id, key = true),
          SqlField("rootId", listA.rootId, hidden = true)
        )
      ),
      ObjectMapping(
        tpe = ElemBType,
        fieldMappings = List(
          SqlField("id", listB.id, key = true),
          SqlField("rootId", listB.rootId, hidden = true)
        )
      ),
      ObjectMapping(
        tpe = TargetType,
        fieldMappings = List(
          SqlField("id", target.id, key = true),
          SqlField("name", target.name)
        )
      )
    )

  def mkLimit(query: Query, limit: Value): Result[Query] =
    limit match {
      case AbsentValue | NullValue => query.success
      case IntValue(num) if num > 0 => Limit(num, query).success
      case other => Result.failure(s"Expected limit > 0, found $other")
    }

  override val selectElaborator = SelectElaborator {
    case (QueryType, "root", List(Binding("limit", limit))) =>
      Elab.transformChild(child => mkLimit(child, limit))

    case (RootType, "firstTarget", Nil) =>
      Elab.transformChild(child => Unique(Limit(1, child)).success)
  }
}
