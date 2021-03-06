package repo

import model.CourseStudent
import model.CourseTutor
import data.*
import model.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import tables.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

private fun addTutorByName(name: String, post: String = "Professor", id: Int=-1) {
    if (tutors.read().find { it.name == name } == null){
        if (id == -1) tutors.create(Tutor(name = name, post = post)) else tutors.create(Tutor(id, name, post))
    } else return
}

private fun addStudentByName(name: String, group: String = "Main group", id: Int=-1){
    if (students.read().find { it.name == name } == null){
        if (id == -1) students.create(Student(name = name, group = group)) else students.create(Student(id, name, group))
    } else return
}

private fun addCourseByName(name: String, id: Int=-1){
    if (courses.read().find { it.name == name } == null){
        if (id == -1) courses.create(Course(name = name)) else courses.create(Course(id, name))
    } else return
}

private fun putTutorToCourse(tutorName: String, courseName: String){
    val courseId= courses.read().firstOrNull{it.name == courseName}?.id
    val tutorId= tutors.read().firstOrNull{it.name == tutorName}?.id
    if (courseId != null && tutorId != null) {
        transaction {
            courseTutorTable.insert { fill(it, CourseTutor(courseId, tutorId)) }
        }
    } else return
}

private fun putStudentAtCourse(studentName: String, courseName: String){
    val courseId= courses.read().firstOrNull{it.name == courseName}?.id
    val studentId= students.read().firstOrNull{it.name == studentName}?.id
    if (courseId != null && studentId != null) {
        transaction {
            courseStudentTable.insert { fill(it, CourseStudent(courseId, studentId)) }
        }
    } else return
}

private fun getStudentsByGroup(group: String) = students.read().filter { it.group == group }

private fun getTutorsByPost(post: String) = tutors.read().filter { it.post == post }

fun setGradeTest() {
    val math = courses.read().find { it.name=="Math" } ?: fail()
    math.setGrade("UML", "Howard", 1)
    val umlTask = math.getTasks().find { it.name == "UML" }?: fail()
    assertEquals(
            1,
            math.studentGrades("Howard",umlTask.id)
    )
}

fun studentGradesTest() {
    val math = courses.read().find { it.name=="Math" } ?: fail()
    math.setGrade("Intro", "Penny", 1)
    math.setGrade("Uml lab", "Penny", 3)
    val umlLabTask = math.getTasks().find { it.name == "Uml lab" }?: fail()
    val introTask = math.getTasks().find { it.name == "Intro" }?: fail()
    assertEquals(1, math.studentGrades("Penny",introTask.id))
    assertEquals(3, math.studentGrades("Penny", umlLabTask.id))
}

fun tutorSetToplistTest(){
    val math = courses.read().find { it.name=="Math" } ?: fail()
    math.setGrade("Uml lab", "Howard", 3)
    math.setRaiting()
    assertEquals(0.1, math.getRaitingByName("Penny")?.getValue)
    assertEquals(0.3, math.getRaitingByName("Howard")?.getValue)
}

fun studentOrTutorReadRankTest(){
    val math = courses.read().find { it.name=="Math" } ?: fail()
    math.setRaiting()
    val howardRank = math.getRaitingByName("Howard")?: fail()
    val pennyRank = math.getRaitingByName("Penny") ?: fail()
    assertEquals(0.3, howardRank.getValue)
    assertEquals(0.1, pennyRank.getValue)
}

fun studentOrTutorReadGradesTest(){
    val math = courses.read().find { it.name=="Math" } ?: fail()
    val umlLabTask = math.getTasks().find { it.name == "Uml lab" }?: fail()
    assertEquals(3, math.studentGrades("Penny", umlLabTask.id))
    assertEquals(5, math.studentGrades("Howard",umlLabTask.id))
}

fun tutorSetGradeTest(){
    val math = courses.read().find { it.name=="Math" } ?: fail()
    math.setGrade("Intro","Howard",1)
    math.setGrade("Uml lab", "Howard",5)
    val umlLabTask = math.getTasks().find { it.name == "Uml lab" }?: fail()
    val introTask = math.getTasks().find { it.name == "Intro" }?: fail()
    assertEquals(1, math.studentGrades("Howard",introTask.id))
    assertEquals(5, math.studentGrades("Howard", umlLabTask.id))
}

fun tutorSetTaskTest(){
    val math = courses.read().find { it.name=="Math" } ?: fail()
    val lecType = transaction {
        typeTable.selectAll().mapNotNull { typeTable.readResult(it) }
    }.find { it.name == "Lecture" }?: fail()
    val labType=transaction {
        typeTable.selectAll().mapNotNull { typeTable.readResult(it) }
    }.find { it.name == "Laboratory" }?: fail()
    transaction {
        taskTable.insertAndGetIdItem(Task(name = "test1", courseId = math.id,typeId = lecType.id)).value
        true
    }
    transaction {
        taskTable.insertAndGetIdItem(Task(name = "test2", typeId = labType.id, courseId = math.id)).value
        true
    }
    transaction {
        taskTable.insertAndGetIdItem(Task(name = "test3", typeId = lecType.id, courseId = math.id)).value
        true
    }
    assertEquals("test1", math.getTasks().find { it.name == "test1" }!!.name)
    assertEquals("test2", math.getTasks().find { it.name == "test2" }!!.name)
    assertEquals("test3", math.getTasks().find { it.name == "test3" }!!.name)
}

fun tutorAddStudentToCourse(){
    val math = courses.read().find { it.name=="Math" } ?: fail()
    addStudentByName("Bob","Newcomers")
    putStudentAtCourse("Bob", "Math")
    addStudentByName("Charlie","Newcomers")
    putStudentAtCourse("Charlie", "Math")
    assertEquals("Bob", math.getStudent("Bob")?.name)
    assertEquals("Charlie", math.getStudent("Charlie")?.name)
}

fun adminSetCourseTest(){
    listOf("Rocket science", "Basic rocket piloting", "Space navigation").forEach {
        addCourseByName(it)
    }
    assertEquals("Rocket science", courses.read().find { it.name=="Rocket science" }?.name)
    assertEquals("Basic rocket piloting", courses.read().find { it.name=="Basic rocket piloting" }?.name)
    assertEquals("Space navigation", courses.read().find { it.name=="Space navigation" }?.name)
}

fun getStudentsByGroupTest(){
    assertEquals(2, getStudentsByGroup("Footprint on the Moon").size)
}

fun getTutorByPostTest(){
    assertEquals(2, getTutorsByPost("Professor").size)
}

class MainTest {

    @Test
    fun testAllUseCases() {
        Database.connect(
                "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
                driver = "org.h2.Driver"
        )
        transaction {
            SchemaUtils.create(tutorTable, studentTable, courseStudentTable, courseTable, courseTutorTable, taskTable, gradeTable, raitingTable, typeTable)
        }

        mapOf(
            "Sheldon" to "Professor",
            "Leonard" to "Professor"
        ).forEach {
            addTutorByName(it.key, it.value)
        }
        mapOf(
            "Howard" to "Footprint on the Moon",
            "Raj" to "Footprint on the Moon",
            "Penny" to "Waitress"
        ).forEach {
            addStudentByName(it.key, it.value)
        }
        addCourseByName("Math")
        addCourseByName("Phys")
        addCourseByName("History")
        transaction {
            typeTable.insertAndGetIdItem(Type(name = "Lecture", shortName = "lec")).value
            true
        }
        transaction {
            typeTable.insertAndGetIdItem(Type(name = "Laboratory", shortName = "lab")).value
            true
        }
        val lecType = transaction {
            typeTable.selectAll().mapNotNull { typeTable.readResult(it) }
        }.find { it.name == "Lecture" }
        val labType=transaction {
            typeTable.selectAll().mapNotNull { typeTable.readResult(it) }
        }.find { it.name == "Laboratory" }

        val math = courses.read().find { it.name =="Math"} ?: fail("Wrong course name")
        courses.read()
                .find {it.name == "Math"}?.run {
            addTutorByName("Sheldon")
            putTutorToCourse("Sheldon", "Math")
            addStudentByName("Howard")
            putStudentAtCourse("Howard", "Math")
            addStudentByName("Penny")
            putStudentAtCourse("Penny", "Math")
            transaction {
                taskTable.insertAndGetIdItem(Task(name = "Intro", typeId = lecType!!.id, courseId = math.id)).value
                true
            }
            transaction {
                taskTable.insertAndGetIdItem(Task(name = "UML", typeId = lecType!!.id, courseId = math.id)).value
                true
            }
            transaction {
                taskTable.insertAndGetIdItem(Task(name = "Uml lab", typeId = labType!!.id, courseId = math.id, maxValue = 5)).value
                true
            }
            setGrade("Uml lab", "Howard", 5)
            setGrade("Uml lab", "Penny", 3)
            setGrade("Intro", "Penny", 1)
        }

        setGradeTest()
        studentGradesTest()
        tutorSetToplistTest()
        studentOrTutorReadRankTest()
        studentOrTutorReadGradesTest()
        tutorSetGradeTest()
        tutorSetTaskTest()
        tutorAddStudentToCourse()
        adminSetCourseTest()
        getStudentsByGroupTest()
        getTutorByPostTest()

        transaction {
            SchemaUtils.drop(courseStudentTable, courseTutorTable, tutorTable, gradeTable, taskTable, raitingTable, courseTable, studentTable, typeTable)
        }
    }
}