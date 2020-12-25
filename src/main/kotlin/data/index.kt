package data

import repo.ItemRepo
import tables.*

val tutors = ItemRepo(tutorTable)
val students = ItemRepo(studentTable)
val courses = ItemRepo(courseTable)
val courseStudentTable = CourseStudentTable()
val courseTutorTable = CourseTutorTable()

val weight = listOf(
    "lecture" to 0.5,
    "laboratory" to 0.9,
    "test" to 0.7
)