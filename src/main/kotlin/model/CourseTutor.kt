package model

import kotlinx.serialization.Serializable

@Serializable
class CourseTutor (
    val course_id: Int,
    val tutor_id: Int,
    val id: Int = -1
)
