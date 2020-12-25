package model

import kotlinx.serialization.Serializable
import repo.Item

@Serializable
class Student (
    override var id: Int = -1,
    val name: String,
    val group: String,
): Item