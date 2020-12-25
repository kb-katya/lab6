package model
import kotlinx.serialization.Serializable
import repo.Item

@Serializable
class Tutor(
    override var id: Int = -1,
    val name: String,
    val post: String,
) : Item