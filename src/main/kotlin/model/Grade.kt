package model

import `object`.DateSerializer
import kotlinx.serialization.Serializable
import repo.Item
import java.time.LocalDate

@Serializable
class Grade (
    override var id: Int = -1,
    val value: Int,
    @Serializable(with= DateSerializer::class)
    val date: LocalDate = LocalDate.now(),
    val studentId: Int,
    val taskId: Int
) : Item