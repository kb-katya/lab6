package rest

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.KSerializer
import model.Course
import model.Grade
import model.Task
import repo.Repo

fun Application.restCourse(
        repo: Repo<Course>,
        path: String = "/courses",
        courseSerializer: KSerializer<Course> = Course.serializer(),
        taskSerializer: KSerializer<Task> = Task.serializer(),
        gradeSerializer: KSerializer<Grade> = Grade.serializer()
) {
    routing {
        route(path) {
            get {
                call.respond(repo.read())
            }
            post {
                call.respond(
                    parseBody(courseSerializer)?.let { elem ->
                        if (repo.create(elem))
                            HttpStatusCode.Created
                        else
                            HttpStatusCode.NotFound
                    } ?: HttpStatusCode.BadRequest
                )
            }
        }
        route("$path/{itemId}") {
            get {
                call.respond(
                        parseId()?.let { id ->
                            repo.read(id) ?: HttpStatusCode.NotFound
                        } ?: HttpStatusCode.BadRequest
                )
            }
            put {
                call.respond(
                    parseBody(courseSerializer)?.let { elem ->
                        parseId()?.let { id ->
                            if (repo.update(id, elem))
                                HttpStatusCode.OK
                            else
                                HttpStatusCode.NotAcceptable
                        }
                    } ?: HttpStatusCode.BadRequest
                )
            }
            delete {
                call.respond(
                    parseId()?.let { id: Int ->
                        if (repo.delete(id))
                            HttpStatusCode.OK
                        else
                            HttpStatusCode.NotFound
                    } ?: HttpStatusCode.BadRequest
                )
            }
        }
        route("$path/{itemId}/tutor") {
            get {
                call.respond(
                    parseId()?.let { id: Int ->
                        repo.read(id)?.let { elem ->
                            elem.getTutors()
                        } ?:HttpStatusCode.NotFound
                    } ?: HttpStatusCode.BadRequest
                )
            }
        }
        route("$path/{itemId}/tutor/{tutorId}") {
            post {
                call.respond(
                    parseId()?.let { id ->
                        repo.read(id)?.let { elem ->
                            parseId("tutorId")?.let { tutorId ->
                                if (elem.addTutorById(tutorId))
                                    HttpStatusCode.Created
                                else
                                    HttpStatusCode.NotFound
                            } ?: HttpStatusCode.BadRequest
                        } ?: HttpStatusCode.NotFound
                    } ?: HttpStatusCode.BadRequest
                )
            }
            delete {
                call.respond(
                    parseId()?.let { id ->
                        repo.read(id)?.let { elem ->
                            parseId("tutorId")?.let { tutorId ->
                                if (elem.deleteTutor(tutorId))
                                    HttpStatusCode.OK
                                else
                                    HttpStatusCode.NotFound
                            } ?: HttpStatusCode.BadRequest
                        } ?: HttpStatusCode.NotFound
                    } ?: HttpStatusCode.BadRequest
                )
            }
        }
        route("$path/{itemId}/student") {
            get {
                call.respond(
                    parseId()?.let { id ->
                        repo.read(id)?.getStudents() ?: HttpStatusCode.NotFound
                    } ?: HttpStatusCode.BadRequest
                )
            }
        }
        route("$path/{itemId}/student/{studentId}") {
            post {
                call.respond(
                    parseId()?.let { id ->
                        repo.read(id)?.let { elem ->
                            parseId("studentId")?.let { studentId ->
                                if (elem.addStudentById(studentId))
                                    HttpStatusCode.Created
                                else
                                    HttpStatusCode.NotFound
                            } ?: HttpStatusCode.BadRequest
                        } ?: HttpStatusCode.NotFound
                    } ?: HttpStatusCode.NotFound
                )
            }
            delete {
                call.respond(
                    parseId()?.let { id ->
                        repo.read(id)?.let { elem ->
                            parseId("studentId")?.let { studentId ->
                                if (elem.deleteStudent(studentId))
                                    HttpStatusCode.OK
                                else
                                    HttpStatusCode.NotFound
                            } ?: HttpStatusCode.BadRequest
                        } ?: HttpStatusCode.NotFound
                    } ?: HttpStatusCode.BadRequest
                )
            }
        }
        route("$path/{itemId}/raiting") {
            get {
                call.respond(
                    parseId()?.let { id ->
                        repo.read(id)?.getCourseRaiting() ?: HttpStatusCode.NotFound
                    } ?: HttpStatusCode.BadRequest
                )
            }
        }
        route("$path/{itemId}/task") {
            get {
                call.respond(
                    parseId()?.let { id ->
                        repo.read(id)?.let { elem ->
                            elem.getTasks()
                        } ?: HttpStatusCode.NotFound
                    } ?: HttpStatusCode.BadRequest
                )
            }
            post {
                call.respond(
                    parseId()?.let { id ->
                        repo.read(id)?.let { elem ->
                            parseBody(taskSerializer)?.let {
                                if (elem.addTask(it))
                                    HttpStatusCode.Created
                                else
                                    HttpStatusCode.NotFound
                            } ?: HttpStatusCode.BadRequest
                        } ?: HttpStatusCode.NotFound
                    } ?: HttpStatusCode.BadRequest
                )
            }
        }
        route("$path/{itemId}/task/{taskId}") {
            get {
                call.respond(
                    parseId()?.let { id ->
                        repo.read(id)?.let { elem ->
                            parseId("taskId")?.let { taskId ->
                                elem.getTask(taskId) ?: HttpStatusCode.NotFound
                            } ?: HttpStatusCode.BadRequest
                        } ?: HttpStatusCode.NotFound
                    } ?: HttpStatusCode.BadRequest
                )
            }
            delete {
                call.respond(
                    parseId()?.let { id ->
                        repo.read(id)?.let { elem ->
                            parseId("taskId")?.let { taskid ->
                                if (elem.removeTask(taskid))
                                    HttpStatusCode.OK
                                else
                                    HttpStatusCode.NotFound
                            } ?: HttpStatusCode.BadRequest
                        } ?: HttpStatusCode.NotFound
                    } ?: HttpStatusCode.BadRequest
                )
            }
        }
        route("$path/{itemId}/task/{taskId}/grade"){
            get {
                call.respond(
                    parseId()?.let { id ->
                        repo.read(id)?.let { elem ->
                            parseId("taskId")?.let { taskId ->
                                elem.getTask(taskId)?.getGrades()
                            } ?: HttpStatusCode.BadRequest
                        } ?: HttpStatusCode.NotFound
                    } ?: HttpStatusCode.BadRequest
                )
            }
            post {
                call.respond(
                    parseId()?.let { id ->
                        repo.read(id)?.let { elem ->
                            parseId("taskId")?.let { taskId ->
                                parseBody(gradeSerializer)?.let {
                                    if (elem.getTask(taskId)?.addGrade(it) == true)
                                        HttpStatusCode.Created
                                    else
                                        HttpStatusCode.BadRequest
                                } ?: HttpStatusCode.BadRequest
                            } ?: HttpStatusCode.BadRequest
                        } ?: HttpStatusCode.NotFound
                    } ?: HttpStatusCode.BadRequest
                )
            }
        }
    }
}