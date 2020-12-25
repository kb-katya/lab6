package repo

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder

@Serializable
class TestItem(
    val name: String,
    override var id: Int = -1
) : Item {
    override fun toString(): String {
        return "id=$id, name=$name"
    }
}

class TestItemTable : ItemTable<TestItem>() {
    val name = varchar("name", 50)
    override fun fill(builder: UpdateBuilder<Int>, item: TestItem) {
        builder[name] = item.name
    }
    override fun readResult(result: ResultRow) =
        TestItem(
            result[name],
            result[id].value
        )
}

val testItemTable = TestItemTable()