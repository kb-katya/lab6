package tables

import model.CourseStudent
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class CourseStudentTable: IntIdTable(){
    val course_id = reference("course_id", courseTable)
    val student_id = reference("student_id", studentTable)

    fun fill(builder: UpdateBuilder<Int>, item: CourseStudent) {
        builder[course_id] = item.course_id
        builder[student_id] = item.student_id
    }
    fun readResult(result: ResultRow) =
        CourseStudent(
            result[course_id].value,
            result[student_id].value,
            result[id].value
        )
}
