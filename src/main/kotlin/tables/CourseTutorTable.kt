package tables

import model.CourseTutor
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class CourseTutorTable: IntIdTable(){
    val course_id = reference("course_id", courseTable)
    val tutor_id = reference("tutor_id", tutorTable)

    fun fill(builder: UpdateBuilder<Int>, item: CourseTutor) {
        builder[course_id] = item.course_id
        builder[tutor_id] = item.tutor_id
    }
    fun readResult(result: ResultRow) =
        CourseTutor(
            result[course_id].value,
            result[tutor_id].value,
            result[id].value
        )
}