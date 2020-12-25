package tables

import model.Task
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.`java-time`.date
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import repo.ItemTable

class TaskTable : ItemTable<Task>() {
    val name = varchar("name", 50)
    val typeId = reference("typeId", typeTable)
    val courseId = reference("courseId", courseTable)
    val description = varchar("description", 100)
    val maxValue = integer("maxValue")
    val deadline = date("deadline")

    override fun fill(builder: UpdateBuilder<Int>, item: Task) {
        builder[name] = item.name
        builder[typeId] = item.typeId
        builder[courseId] = item.courseId
        builder[description] = item.description
        builder[maxValue] = item.maxValue
        builder[deadline] = item.deadline
    }

    override fun readResult(result: ResultRow) =
        Task(
            result[id].value,
            result[name],
            result[description],
            result[maxValue],
            result[deadline],
            result[typeId].value,
            result[courseId].value,
        )
}

val taskTable = TaskTable()