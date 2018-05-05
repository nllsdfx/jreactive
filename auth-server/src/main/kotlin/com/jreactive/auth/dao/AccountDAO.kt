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

package com.jreactive.auth.dao

import akka.actor.AbstractActor
import akka.actor.ActorRef
import akka.actor.Props
import akka.dispatch.Futures
import com.jreactive.auth.entity.*
import com.jreactive.auth.server.AccountInfoArray
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class AccountDAO : AbstractActor() {

    private val ctx = context.system.dispatchers().lookup("jdbc-dispatcher")

    override fun createReceive(): Receive {
        return receiveBuilder()
                .match(IPBanCheck::class.java, { checkIP(it) })
                .match(AccountInfoMsg::class.java, { accountInfo(it, sender) })
                .build()
    }

    private fun checkIP(msg: IPBanCheck) {
        Futures.future({
            transaction {
                msg.callback.invoke(!IpBan.searchQuery(IP_Banned.ip eq msg.address).empty())
            }
        }, ctx)
    }

    private fun accountInfo(msg: AccountInfoMsg, s: ActorRef) {

        Futures.future({
            transaction {

                var user: UserAccount? = null

                try {
                    user = UserAccount.wrapRow(Account.leftJoin(Account_Banned)
                            .select {
                                Account.userName.eq(msg.login.toLowerCase())
                            }
                            .single())
                } finally {
                    s.tell(AccountInfoArray(user), ActorRef.noSender())
                }

            }
        }, ctx)
    }
}

fun props(): Props {
    return Props.create(AccountDAO::class.java)
}

