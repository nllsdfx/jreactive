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

package com.jreactive.auth.config

import com.jreactive.commons.util.properties.Props
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

object DataBaseConfig {

    private val props = Props("db/db.properties")

    fun dataSource(): HikariDataSource {

        val config = HikariConfig()

        with(config) {
            driverClassName = props.get("db.driver")
            jdbcUrl = props.get("db.url")
            username = props.get("db.username")
            password = props.get("db.password")
        }

        return HikariDataSource(config)
    }

}