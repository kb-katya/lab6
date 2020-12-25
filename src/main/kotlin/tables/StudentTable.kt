package tables

import model.Student
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import repo.ItemTable

class StudentTable : ItemTable<Student>() {
    val name = varchar("name", 50)
    val group = varchar("group", 50)

    override fun fill(builder: UpdateBuilder<Int>, item: Student) {
        builder[name] = item.name
        builder[group] = item.group
    }

    override fun readResult(result: ResultRow) =
        Student(
            result[id].value,
            result[name],
            result[group],
        )
}

val studentTable = StudentTable()