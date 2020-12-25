package tables

import model.Course
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import repo.ItemTable

class CourseTable: ItemTable<Course>() {
    val name = varchar("name",50)

    override fun fill(builder: UpdateBuilder<Int>, item: Course) {
        builder[name] = item.name
    }

    override fun readResult(result: ResultRow) =
        Course(
            result[id].value,
            result[name],
        )
}

val courseTable = CourseTable()