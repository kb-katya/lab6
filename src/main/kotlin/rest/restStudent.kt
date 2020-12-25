package rest

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.KSerializer
import model.Student
import repo.Repo

fun Application.restStudent(
        repo: Repo<Student>,
        path: String= "/students",
        serializer: KSerializer<Student>
){
    routing {
        route(path){
            get {
                call.respond(repo.read())
            }
            post{
                call.respond(
                    parseBody(serializer)?.let {elem ->
                        if (repo.create(elem))
                            HttpStatusCode.OK
                        else
                            HttpStatusCode.NotFound
                    }?: HttpStatusCode.BadRequest
                )
            }
        }
        route("$path/{itemId}"){
            get {
                call.respond(
                    parseId()?.let { id ->
                        repo.read(id) ?: HttpStatusCode.NotFound
                    } ?: HttpStatusCode.BadRequest
                )
            }
            put {
                call.respond(
                    parseBody(serializer)?.let { elem ->
                        parseId()?.let { id ->
                            if(repo.update(id, elem))
                                HttpStatusCode.OK
                            else
                                HttpStatusCode.NotFound
                        }
                    }?: HttpStatusCode.BadRequest
                )
            }
            delete {
                call.respond(
                    parseId()?.let { id ->
                        if (repo.delete(id))
                            HttpStatusCode.OK
                        else
                            HttpStatusCode.NotFound
                    } ?: HttpStatusCode.BadRequest
                )
            }
        }
    }
}