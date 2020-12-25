package model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import repo.Item
import java.time.LocalDate
import tables.*
import data.*

@Serializable
class Course(
    override var id: Int = -1,
    val name: String,
): Item {

    fun getStudents(): List<Student> {
        val result = mutableListOf<Student>()
        transaction {
            courseStudentTable.selectAll().mapNotNull { courseStudentTable.readResult(it) }
        }.filter { it.course_id == this.id }.forEach {
            result.add(students.read(it.student_id)!!)
        }
        return result.toList()
    }

    fun getTutors(): List<Tutor> {
        val result = mutableListOf<Tutor>()
        transaction {
            courseTutorTable.selectAll().mapNotNull { courseTutorTable.readResult(it) }
        }.filter { it.course_id == this.id }.forEach {
            result.add(tutors.read(it.tutor_id)!!)
        }
        return result.toList()
    }


    fun setRaiting() {
        var nextID = transaction {
            raitingTable.selectAll().mapNotNull { raitingTable.readResult(it) }
        }.size
        getStudents().forEach { student ->
            val currentRank = Raiting(student.id, id, ++nextID)
            val duplicate = transaction {
                raitingTable.selectAll().mapNotNull { raitingTable.readResult(it) }
            }.firstOrNull { it.studentId == student.id }
            if (duplicate == null) {
                transaction {
                    raitingTable.insertAndGetIdItem(currentRank).value
                    true
                }
            } else{
                transaction {
                    raitingTable.updateItem(duplicate.id, currentRank) > 0
                }
            }
        }
    }

    fun getCourseRaiting(): List<Raiting> {
        setRaiting()
        val result = mutableListOf<Raiting>()
        transaction {
            raitingTable.selectAll().mapNotNull { raitingTable.readResult(it) }
        }.forEach { rank ->
            if (rank.courseId == this.id) result.add(rank)
        }
        return result.toList()
    }

    fun setGrade(taskName: String, studentName: String, value: Int, date: LocalDate = LocalDate.now()) {
        val task = this.getTasks().find { it.name == taskName } ?: return
        val student = getStudents().find { it.name == studentName } ?: return
        if (value !in 0..task.maxValue) return
        val currentGrade = transaction {
            gradeTable.selectAll().mapNotNull { gradeTable.readResult(it) }
        }.firstOrNull { it.taskId == task.id && it.studentId == student.id }
        if (currentGrade == null || currentGrade.value < value) {
            val grade = Grade(task.id, value, date, student.id, task.id)
            transaction {
                gradeTable.insertAndGetId {
                    fill(it, grade)
                }.value
                true
            }
        } else return
    }

    fun studentGrades(studentName: String, task_id: Int): Int? {
        val student = getStudents().find { it.name == studentName } ?: return null
        return transaction {
            gradeTable.selectAll().mapNotNull { gradeTable.readResult(it) }
        }.find { it.taskId == task_id && it.studentId == student.id }?.value
    }

    fun getStudent(studentName: String): Student? {
        return getStudents().find { it.name == studentName }
    }

    fun getTutor(tutorName: String): Tutor? {
        return getTutors().find { it.name == tutorName }
    }

    fun getRaitingByName(studentName: String): Raiting? {
        val student = getStudents().find { it.name == studentName }
        return if (student != null) {
            transaction {
                raitingTable.selectAll().mapNotNull { raitingTable.readResult(it) }
            }.find { it.studentId == student.id && this.id == it.courseId }
        } else null
    }

    fun getTask(taskId: Int): Task?{
        return getTasks().find { it.id == taskId }
    }

    fun getTasks(): List<Task> {
        val result = mutableListOf<Task>()
        transaction {
            taskTable.selectAll().mapNotNull { taskTable.readResult(it) }
        }.forEach { task ->
            if (task.courseId == this.id) result.add(task)
        }
        return result.toList()
    }

    fun addTask(task: Task)= transaction {
        taskTable.insertAndGetIdItem(task).value
            true
        }

    fun removeTask(taskId: Int): Boolean {
        val taskExists= transaction {
            taskTable.selectAll().mapNotNull { taskTable.readResult(it) }
        }.firstOrNull {
            it.id == taskId
                    && it.courseId == this.id }
        return if (taskExists != null) {
            transaction {
                gradeTable.deleteWhere { gradeTable.taskId eq taskId }
            }
            transaction {
                taskTable.deleteWhere { (taskTable.courseId eq this@Course.id) and (taskTable.id eq taskId) } > 0
            }
        } else false
    }

    fun addTutor(tutorName: String): Boolean {
        val tutorId = tutors.read().firstOrNull { it.name == tutorName }?.id
        return if (tutorId != null) {
            transaction {
                courseTutorTable.insert { fill(it, CourseTutor(this@Course.id, tutorId)) }
                true
            }
            true
        } else false
    }

    fun addTutorById(tutorId: Int): Boolean{
        if (tutors.read(tutorId) == null) return false
        return transaction {
            courseTutorTable.insert { fill(it, CourseTutor(this@Course.id, tutorId)) }
            true
        }
    }

    fun addStudentById(studentId: Int): Boolean{
        if (students.read(studentId) == null) return false
        return transaction {
            courseStudentTable.insert { fill(it, CourseStudent(this@Course.id, studentId)) }
            true
        }
    }

    fun addStudent(studentName: String) : Boolean {
        val studentId = students.read().firstOrNull { it.name == studentName }?.id
        return if (studentId != null) {
            transaction {
                courseStudentTable.insert { fill(it, CourseStudent(this@Course.id, studentId)) }
                true
            }
        } else false
    }

    fun deleteTutor(tutorId: Int): Boolean {
        val tutor = tutors.read(tutorId)?.id
        return if (tutor != null){
            transaction {
                courseTutorTable.deleteWhere {
                    (courseTutorTable.course_id eq this@Course.id) and (courseTutorTable.tutor_id eq tutorId)
                } > 0
            }
        } else false
    }

    fun deleteStudent(studentId: Int): Boolean{
        val student = students.read(studentId)?.id
        return if (student != null){
            transaction {
                courseStudentTable.deleteWhere {
                    (courseStudentTable.course_id eq this@Course.id) and (courseStudentTable.student_id eq studentId)
                } > 0
            }
        } else false
    }
}