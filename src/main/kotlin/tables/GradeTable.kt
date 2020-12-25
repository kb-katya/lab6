package tables

import model.Grade
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.`java-time`.date
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import repo.ItemTable

class GradeTable: ItemTable<Grade>() {
    val value = integer("value")
    val date = date("date")
    val studentId = reference("studentId", studentTable)
    val taskId = reference("taskId", taskTable)

    override fun fill(builder: UpdateBuilder<Int>, item: Grade) {
        builder[value] = item.value
        builder[date] = item.date
        builder[studentId] = item.studentId
        builder[taskId] = item.taskId
    }

    override fun readResult(result: ResultRow)=
        Grade(
            result[id].value,
            result[value],
            result[date],
            result[studentId].value,
            result[taskId].value
        )
}

val gradeTable = GradeTable()