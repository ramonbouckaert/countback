package io.bouckaert.countback

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class WebServer(private val serverConfig: ServerConfig.Config) {

    fun start() = server.start(wait = true)

    fun stop() = server.stop(1000, 2000)

    private val electionYears: Map<Int, Map<String, Collection<Candidate>>> = mapOf(
        2020 to loadCandidates(2020),
        2016 to loadCandidates(2016),
        2012 to loadCandidates(2012),
        2008 to loadCandidates(2008)
    )

    private val server: ApplicationEngine = embeddedServer(Netty, port = serverConfig.port) {
        install(DefaultHeaders) {
            header(HttpHeaders.AccessControlAllowOrigin, "*")
        }
        install(CallLogging)
        install(CORS) {
            allowMethod(HttpMethod.Get)
            anyHost()
            allowHeader(HttpHeaders.Authorization)
            allowHeader(HttpHeaders.ContentType)
            allowHeader("x-requested-with")
        }
        install(ContentNegotiation) {
            jackson {
                enable(SerializationFeature.INDENT_OUTPUT)
            }
        }

        routing {
            static {
                resources("webroot")
                defaultResource("webroot/${serverConfig.defaultResource}")
            }

            get("api/candidates/{year}") {
                val year: Int? = call.parameters["year"]?.toIntOrNull()
                val candidates = year?.let { electionYears[it] }

                when {
                    year == null -> call.respond(
                        status = HttpStatusCode.BadRequest,
                        message = "You must specify a valid election year"
                    )
                    candidates == null -> call.respond(
                        status = HttpStatusCode.BadRequest,
                        message = "Election year '$year' not supported"
                    )
                    else -> call.respond(
                        status = HttpStatusCode.OK,
                        candidates
                    )
                }
            }

            get("api/countback/{year}") {
                val year: Int? = call.parameters["year"]?.toIntOrNull()
                val electorate: String? = call.parameters["electorate"]?.decodeURLQueryComponent()
                val candidateToResign: String? = call.parameters["candidateToResign"]?.decodeURLQueryComponent()
                val candidatesToContest: List<Candidate> = call.parameters["candidatesToContest"]
                    ?.decodeURLQueryComponent()
                    ?.split(";")
                    ?.map(String::trim)
                    ?.filter { it.isNotBlank() }
                    ?.map(::Candidate)
                    ?: emptyList()

                when {
                    year == null || year !in electionYears.keys -> call.respond(
                        status = HttpStatusCode.BadRequest,
                        message = "You must specify a valid election year"
                    )
                    candidatesToContest.isEmpty() -> call.respond(
                        status = HttpStatusCode.BadRequest,
                        message = "You must specify at least one candidate to contest the vacancy"
                    )
                    electorate == null -> call.respond(
                        status = HttpStatusCode.BadRequest,
                        message = "You must specify the electorate of the countback"
                    )
                    candidateToResign == null -> call.respond(
                        status = HttpStatusCode.BadRequest,
                        message = "You must specify the candidate who will resign"
                    )
                    else -> {
                        call.respondOutputStream(
                            contentType = ContentType.Text.Plain,
                            status = HttpStatusCode.OK
                        ) {
                            this.writeln("Loading data...")
                            this.writeln("")

                            val election = try {
                                loadElectionData(year, electorate).let {
                                    Election(
                                        if (electorate == "Molonglo") 7 else 5,
                                        it.first,
                                        it.second,
                                        year <= 2012
                                    )
                                }
                            } catch (e: Throwable) {
                                this.writeln("Failed to load election data!")
                                null
                            }

                            if (election != null) {
                                this.writeln("*** Simulating initial distribution of preferences ***")
                                this.writeln("")
                                val initialDistribution = election.performCount(
                                    verbose = true,
                                    outputStream = this
                                )
                                val winningCandidates = initialDistribution.winnersAndVotes.keys
                                this.writeln("")
                                this.writeln("Winning candidates:")
                                winningCandidates.forEach { this.writeln(it.name) }
                                val resigningCandidatePile =
                                    initialDistribution.winnersAndVotes[Candidate(candidateToResign)]
                                val filteredCandidatesToContest =
                                    candidatesToContest.filter { c -> c !in winningCandidates }.toSet()
                                when {
                                    filteredCandidatesToContest.isEmpty() ->
                                        this.writeln("There are no candidates eligible to contest the countback.")
                                    resigningCandidatePile == null ->
                                        this.writeln("$candidateToResign was not in the field of winning candidates.")
                                    else -> {
                                        this.writeln("")
                                        this.writeln("*** Starting countback ***")
                                        Countback(
                                            resigningCandidatePile,
                                            initialDistribution.quota,
                                            filteredCandidatesToContest
                                        ).performCount(true, this)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadElectionData(year: Int, electorate: String): Pair<Set<Candidate>, List<Ballot>> {
        val classLoader = Thread.currentThread().contextClassLoader

        val dataLoader = ACTDataLoader {
            classLoader.getResourceAsStream("electiondata/$year.zip")
        }

        val candidatesMap = dataLoader.loadCandidates(
            dataLoader.loadElectorates()
        )[electorate]!!

        val ballots = dataLoader.loadBallots(electorate, candidatesMap)

        return candidatesMap.values.toSet() to ballots
    }

    private fun loadCandidates(year: Int): Map<String, Collection<Candidate>> {
        val classLoader = Thread.currentThread().contextClassLoader

        val dataLoader = ACTDataLoader {
            classLoader.getResourceAsStream("electiondata/$year.zip")
        }

        return dataLoader.loadCandidates(
            dataLoader.loadElectorates()
        ).mapValues { it.value.values }
    }
}