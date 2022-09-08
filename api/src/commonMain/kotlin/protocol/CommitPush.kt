/*
 * Animation Garden App
 * Copyright (C) 2022  Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.him188.animationgarden.api.protocol

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import me.him188.animationgarden.api.model.Commit

// pushed from server via websocket
@Serializable
data class CommitEvent(
    @ProtoNumber(1) val baseRef: CommitRef,
    @ProtoNumber(2) val newRef: CommitRef,
    @ProtoNumber(3) val commit: @Polymorphic Commit,
    @ProtoNumber(4) val committer: Committer,
)
