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
import akka.actor.Props
import akka.routing.FromConfig
import java.io.Serializable

class AccountDAOManager : AbstractActor() {

    private val router = context.actorOf(FromConfig.getInstance()
            .props(Props.create(AccountDAO::class.java)), "accountRouter")

    init {
        context.parent.path()
    }

    override fun createReceive(): Receive {
        return receiveBuilder()
                .match(AccountDAOMsg::class.java, { router.forward(it, context) })
                .build()
    }

}

interface AccountDAOMsg : Serializable

class IPBanCheck(val address: String, val callback: (Boolean) -> Unit) : AccountDAOMsg

class AccountInfoMsg(val login: String) : AccountDAOMsg

class RealmListCountMsg(val id: Long, callback: (Boolean) -> Unit) : AccountDAOMsg