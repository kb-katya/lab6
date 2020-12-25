package model

import kotlinx.serialization.Serializable
import repo.Item

@Serializable
class Type(
    override var id: Int = -1,
    val name: String,
    val shortName: String ,
): Item