/*
 * Copyright (C) 2018 JReactive <http://www.jreactive.com/>
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.jreactive.auth.entity

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.LongIdTable

object Account : LongIdTable() {
    val userName = varchar("username", 32).index()
    val email = varchar("email", 320).index()
    val shaPass = varchar("sha_pass_hash", 40)
}

object IP_Banned : LongIdTable() {
    val ip = varchar("ip", 15).index()
    val banDate = datetime("bandate")
    val unbanDate = datetime("unbandate")
    val bannedBy = varchar("bannedby", 50)
    val banReason = varchar("banreason", 255)
}

class IpBan(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<IpBan>(IP_Banned)
    var ip by IP_Banned.ip
    var banDate by IP_Banned.banDate
    var unbanDate by IP_Banned.unbanDate
    var bannedBy by IP_Banned.bannedBy
    var banReason by IP_Banned.banDate
}

