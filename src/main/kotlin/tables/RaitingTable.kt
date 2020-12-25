package tables

import model.Raiting
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import repo.ItemTable

class RaitingTable: ItemTable<Raiting>(){
    val studentId = reference("studentId", studentTable)
    val courseId = reference("courseId", courseTable)
    val rank = double("rank")

    override fun fill(builder: UpdateBuilder<Int>, item: Raiting) {
        builder[studentId] = item.studentId
        builder[courseId] = item.courseId
        builder[rank] = item.getValue
    }

    override fun readResult(result: ResultRow) =
        Raiting(
            result[id].value,
            result[studentId].value,
            result[courseId].value,
        )
}

val raitingTable = RaitingTable()