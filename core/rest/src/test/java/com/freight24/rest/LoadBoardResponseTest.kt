package com.freight24.rest

import com.freight24.rest.response.LoadBoardResponse
import com.freight24.rest.response.PaginatedLoadBoardResponse
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the load-board DTOs against the wire shape drifting again.
 *
 * `load-board-summary` and `load-board-paginated` both went through a
 * snake_case revision and now both answer camelCase with nested `loading` /
 * `unloading` / `truckType` objects. Two rounds of silent breakage came from
 * that, so the live shape of each endpoint is pinned here.
 */
class LoadBoardResponseTest {
    private val json = Json { ignoreUnknownKeys = true }

    /** Trimmed from a live `/api/v1/indents/trucker/load-board-summary` response. */
    private val summaryPayload =
        """
        {
          "success": true,
          "data": {
            "groups": [
              {
                "groupId": 1,
                "groupName": "Bengaluru",
                "groupType": "OPERATIONAL_CITY",
                "totalLoads": 2,
                "latestLoads": [
                  {
                    "tat": { "hours": 8, "enabled": true },
                    "price": 11000,
                    "weight": 9,
                    "loading": {
                      "area": "Hoskote",
                      "city": "Bengaluru",
                      "address": null,
                      "contactPhone": null,
                      "contactPerson": null
                    },
                    "employee": { "name": "Admin", "phone": "+919611223344" },
                    "indentId": "dd8d5d73-c63f-4743-b5fe-30872c5c3a04",
                    "isMaster": true,
                    "material": "Bundles / Rolls",
                    "priority": "medium",
                    "createdAt": "2026-10-03T15:57:43.457636+00:00",
                    "expiresAt": "2026-10-04T21:57:43.269445+00:00",
                    "groupType": "OPERATIONAL_CITY",
                    "truckType": {
                      "id": 10,
                      "name": "32 Feet Single Axle",
                      "bodyType": "Closed",
                      "displayName": "32 ft SXL"
                    },
                    "unloading": { "area": null, "city": "Chennai" },
                    "updatedAt": "2026-10-03T15:57:43.457636+00:00",
                    "indentCode": "IND-000133",
                    "isInterested": false,
                    "indentGroupId": "067434b0-6169-4b69-8e78-730be9a35b82",
                    "isPriceHidden": false,
                    "interestStatus": "NOT_INTERESTED",
                    "masterIndentId": null,
                    "operationalCityId": 1
                  },
                  {
                    "tat": { "hours": null, "enabled": false },
                    "price": null,
                    "weight": 18,
                    "loading": {
                      "area": "Nelamangala",
                      "city": "Bengaluru",
                      "address": null,
                      "contactPhone": null,
                      "contactPerson": null
                    },
                    "employee": { "name": "Admin", "phone": "+919611223344" },
                    "indentId": "39a33c76-e7d9-43a8-adad-640c9c24141a",
                    "isMaster": true,
                    "material": "Carton Box",
                    "priority": "medium",
                    "createdAt": "2026-10-03T10:10:18.992422+00:00",
                    "expiresAt": "2026-10-04T16:10:18.722833+00:00",
                    "groupType": "OPERATIONAL_CITY",
                    "truckType": {
                      "id": 12,
                      "name": "32 Feet Multi Axle",
                      "bodyType": "Closed",
                      "displayName": "32 ft MXL"
                    },
                    "unloading": { "area": "Medchal", "city": "Hyderabad" },
                    "updatedAt": "2026-10-03T14:25:26.439+00:00",
                    "indentCode": "IND-000130",
                    "isInterested": false,
                    "indentGroupId": "20cf2540-0d73-403b-ac94-3da158e90657",
                    "isPriceHidden": true,
                    "interestStatus": "NOT_INTERESTED",
                    "masterIndentId": null,
                    "operationalCityId": 1
                  }
                ],
                "operationalCityId": 1
              },
              {
                "groupId": 5,
                "groupName": "Mumbai",
                "groupType": "OPERATIONAL_CITY",
                "totalLoads": 1,
                "latestLoads": [],
                "operationalCityId": 5
              }
            ],
            "summary": {
              "totalLoads": 5,
              "totalGroups": 3,
              "fallbackCityGroups": 0,
              "operationalCityGroups": 3
            },
            "pagination": {
              "page": 1,
              "loadLimit": 10,
              "groupLimit": 100,
              "hasNextPage": false,
              "totalGroups": 3
            }
          }
        }
        """.trimIndent()

    @Test
    fun `summary data decodes as an object of groups, not an array`() {
        val response = json.decodeFromString<LoadBoardResponse>(summaryPayload)
        val data = response.data!!

        assertTrue(response.success)
        assertEquals(3, data.summary!!.totalGroups)
        assertEquals(5, data.summary!!.totalLoads)
        assertEquals(1, data.pagination!!.page)
        assertEquals(100, data.pagination!!.groupLimit)
        assertEquals(2, data.groups.size)
    }

    @Test
    fun `summary latest load exposes nested truck type, tat and locations`() {
        val load =
            json.decodeFromString<LoadBoardResponse>(summaryPayload)
                .data!!.groups.first().latestLoads.first()

        assertEquals("dd8d5d73-c63f-4743-b5fe-30872c5c3a04", load.indentId)
        assertEquals("32 ft SXL", load.truckType!!.displayName)
        assertEquals(8, load.tat!!.hours)
        assertTrue(load.tat!!.enabled!!)
        assertEquals("Hoskote", load.loading!!.area)
        assertEquals("Chennai", load.unloading!!.city)
        assertNull(load.unloading!!.area)
        assertEquals("Admin", load.employee!!.name)
        assertEquals(9.0, load.weight!!, 0.0)
    }

    @Test
    fun `hidden price stays null and is flagged rather than omitted`() {
        val hidden =
            json.decodeFromString<LoadBoardResponse>(summaryPayload)
                .data!!.groups.first().latestLoads[1]

        assertNull(hidden.price)
        assertEquals(true, hidden.isPriceHidden)
    }

    @Test
    fun `fractional weight survives as a decimal`() {
        val payload = summaryPayload.replace("\"weight\": 9", "\"weight\": 2.5")
        val load =
            json.decodeFromString<LoadBoardResponse>(payload)
                .data!!.groups.first().latestLoads.first()

        assertEquals(2.5, load.weight!!, 0.0)
    }

    /** Captured from a live `/api/v1/indents/trucker/load-board-paginated` response. */
    private val paginatedPayload =
        """
        {
          "success": true,
          "data": {
            "loads": [
              {
                "tat": { "hours": 8, "enabled": true },
                "price": 11000,
                "weight": 9,
                "loading": {
                  "area": "Hoskote",
                  "city": "Bengaluru",
                  "address": null,
                  "contactPhone": null,
                  "contactPerson": null
                },
                "employee": { "name": "Admin", "phone": "+919611223344" },
                "indentId": "dd8d5d73-c63f-4743-b5fe-30872c5c3a04",
                "isMaster": true,
                "material": "Bundles / Rolls",
                "priority": "medium",
                "createdAt": "2026-10-03T15:57:43.457636+00:00",
                "expiresAt": "2026-10-04T21:57:43.269445+00:00",
                "truckType": {
                  "id": 10,
                  "name": "32 Feet Single Axle",
                  "bodyType": "Closed",
                  "displayName": "32 ft SXL"
                },
                "unloading": { "area": null, "city": "Chennai" },
                "updatedAt": "2026-10-03T15:57:43.457636+00:00",
                "indentCode": "IND-000133",
                "isInterested": false,
                "indentGroupId": "067434b0-6169-4b69-8e78-730be9a35b82",
                "isPriceHidden": false,
                "interestStatus": "NOT_INTERESTED",
                "masterIndentId": null,
                "operationalCity": "Bengaluru",
                "operationalCityId": 1
              }
            ],
            "pagination": {
              "page": 1,
              "limit": 10,
              "hasMore": false,
              "totalLoads": 2
            }
          }
        }
        """.trimIndent()

    @Test
    fun `paginated feed decodes from the same camelCase shape as the summary`() {
        val data = json.decodeFromString<PaginatedLoadBoardResponse>(paginatedPayload).data!!

        assertEquals(1, data.loads.size)
        val load = data.loads.first()
        assertEquals("dd8d5d73-c63f-4743-b5fe-30872c5c3a04", load.indentId)
        assertEquals("32 ft SXL", load.truckType!!.displayName)
        assertEquals("Hoskote", load.loading!!.area)
        assertEquals("Chennai", load.unloading!!.city)
        assertEquals("Bengaluru", load.operationalCity)

        assertEquals(1, data.pagination.page)
        assertEquals(10, data.pagination.limit)
        assertEquals(2, data.pagination.totalLoads)
        assertFalse(data.pagination.hasMore)
    }

    @Test
    fun `a load with no loading area still resolves through the city column`() {
        val payload =
            paginatedPayload
                .replace("\"area\": \"Hoskote\"", "\"area\": null")
                .replace("\"weight\": 9", "\"weight\": 2.5")

        val load = json.decodeFromString<PaginatedLoadBoardResponse>(payload).data!!.loads.first()

        assertNull(load.loading!!.area)
        assertEquals("Bengaluru", load.loading!!.city)
        assertEquals(2.5, load.weight!!, 0.0)
    }
}
