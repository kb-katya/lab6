package tables

import model.Type
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import repo.ItemTable

class TypeTable : ItemTable<Type>() {
    val name = varchar("name", 50)
    val shortName = varchar("shortName", 10)

    override fun fill(builder: UpdateBuilder<Int>, item: Type) {
        builder[name] = item.name
        builder[shortName] = item.shortName
    }

    override fun readResult(result: ResultRow) =
        Type(
            result[id].value,
            result[name],
            result[shortName]
        )
}

val typeTable = TypeTable()