package model

import kotlinx.serialization.Serializable

@Serializable
class CourseStudent (
    val course_id: Int,
    val student_id: Int,
    val id: Int = -1
)
