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
    val locked = bool("locked")
    val lastIP = varchar("last_ip", 15)
    val failedLogins = long("failed_logins")
//    val ban = optReference("ban", Account_Banned)

}

class UserAccount(id: EntityID<Long>) : LongEntity(id) {

    companion object : LongEntityClass<UserAccount>(Account)

    val login by Account.userName
    val email by Account.email
    val shaPass by Account.shaPass
    val sessionKey by Account.sessionKey
    var v by Account.v
    var s by Account.s
    val tokenKey by Account.tokenKey
    val online by Account.online
    val locked by Account.locked
    val lastIP by Account.lastIP
    val failedLogins by Account.failedLogins
    val ban by AccountBanned referrersOn  Account_Banned.accountId
}

object Account_Banned : LongIdTable() {
    val accountId = reference("account_id", Account.id)
    val banDate = datetime("ban_date")
    val unbanDate = datetime("unbad_date")
    val bannedBy = long("banned_by")
    val banReason = text("ban_reason")
    val banned = bool("banned")
}

class AccountBanned(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<AccountBanned>(Account_Banned)
    val accountId by UserAccount referrersOn Account.id
    val banDate by Account_Banned.banDate
    val unbanDate by Account_Banned.unbanDate
    val bannedBy by UserAccount referrersOn Account.id
    val banReason by Account_Banned.banReason
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

    val ip by IP_Banned.ip
    val banDate by IP_Banned.banDate
    val unbanDate by IP_Banned.unbanDate
    val bannedBy by IP_Banned.bannedBy
    val banReason by IP_Banned.banDate
}

