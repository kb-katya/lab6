package tables

import model.Tutor
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import repo.ItemTable

class TutorTable : ItemTable<Tutor>() {
    val name = varchar("name", 50)
    val post = varchar("post", 50)

    override fun fill(builder: UpdateBuilder<Int>, item: Tutor) {
        builder[name] = item.name
        builder[post] = item.post
    }
    override fun readResult(result: ResultRow) =
        Tutor(
            result[id].value,
            result[name],
            result[post],
        )
}

val tutorTable = TutorTable()