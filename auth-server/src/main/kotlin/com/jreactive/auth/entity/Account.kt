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
    val sessionKey = varchar("session_key", 80)
    val v = varchar("v", 64)
    val s = varchar("s", 64)
    val tokenKey = varchar("token_key", 100)
    val online = bool("online")

}

class UserAccount(id: EntityID<Long>) : LongEntity(id) {

    companion object : LongEntityClass<UserAccount>(Account)

    var login by Account.userName
    var email by Account.email
    var shaPass by Account.shaPass
    var sessionKey by Account.sessionKey
    var v by Account.v
    var s by Account.s
    var tokenKey by Account.tokenKey
    var online by Account.online
}

object Account_Banned : LongIdTable() {
    val accountId = long("account_id")
    val banDate = datetime("ban_date")
    val unbanDate = datetime("unbad_date")
    val bannedBy = long("banned_by")
    val banReason = text("ban_reason")
    val banned = bool("banned")
}

class AccountBanned(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<AccountBanned>(Account_Banned)
    val accountId by UserAccount referrersOn Account.id
    var banDate by Account_Banned.banDate
    var unbanDate by Account_Banned.unbanDate
    val bannedBy by UserAccount referrersOn Account.id
    var banReason by Account_Banned.banReason
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

