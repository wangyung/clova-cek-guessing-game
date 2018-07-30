package com.linecorp.clova.extension.guessing.handler

import com.linecorp.clova.extension.client.*
import com.linecorp.clova.extension.converter.jackson.JacksonObjectMapper
import com.linecorp.clova.extension.guessing.rule.GuessingNumberRule
import com.linecorp.clova.extension.model.response.*
import com.linecorp.clova.extension.model.util.simpleResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class DefaultHandler {
    private val logger: Logger = LoggerFactory.getLogger("guessing-game")
    private val sessions: MutableMap<String, GuessingNumberRule> = hashMapOf()

    val client: ClovaClient = clovaClient("idv.freddie.game.guess") {
        objectMapper = JacksonObjectMapper()
        launchHandler { _, session ->
            val rule = GuessingNumberRule()
            sessions[session.sessionId] = rule
            response(message = GAME_STARTED, sessionId = session.sessionId, endSession = false)
        }

        intentHandler { intentRequest, session ->
            if (sessions[session.sessionId] == null) {
                sessions[session.sessionId] = GuessingNumberRule()
            }
            val (name, slots) = intentRequest.intent
            val value = slots[SLOT_NUMBER]?.value
            val expectedNumber = (session.sessionAttributes[MY_NUMBER] as? String)?.let { it }
                    ?: sessions[session.sessionId]?.expectedNumber

            when (name.toLowerCase()) {
                SLOT_NAME_NUMBER -> return@intentHandler guessNumber(
                        sessionId = session.sessionId,
                        expectedNumber = expectedNumber,
                        userNumber = value)

                SLOT_NAME_ANSWER -> {
                    sessions.remove(session.sessionId)
                    return@intentHandler simpleResponse(
                            message = "正答は ${expectedNumber} です, またね",
                            endSession = true)
                }
                else -> return@intentHandler retryResponse(session.sessionId)
            }
        }

        sessionEndedHandler { _, _ -> simpleResponse(message = BYE) }
    }

    fun healthCheck(request: ServerRequest): Mono<ServerResponse> =
            ServerResponse.ok().body(BodyInserters.fromObject("ok"))

    fun guessing(request: ServerRequest): Mono<ServerResponse> {
        return request
                .bodyToMono(String::class.java)
                .flatMap { requestBody ->
                    logger.info("-> $requestBody\n")
                    val response = client.handleClovaRequest(requestBody, request.headers().asHttpHeaders())
                    logger.info("<- $response\n")
                    return@flatMap ServerResponse.ok().body(
                            BodyInserters.fromObject(response))
                }
    }

    private fun guessNumber(sessionId: String, expectedNumber: String?, userNumber: String?): ClovaExtensionResponse {
        if (userNumber == null) {
            return retryResponse(sessionId)
        }

        sessions[sessionId]?.let { rule ->
            val (result, message) = rule.compareNumber(
                    userNumber, expectedNumber?.let { it } ?: rule.expectedNumber)

            return response(
                    sessionId = sessionId,
                    endSession = result,
                    message = message,
                    reprompt = REPROMPT_MESSAGE)
        } ?: run {
            sessions[sessionId] = GuessingNumberRule()
        }
        return retryResponse(sessionId)
    }

    private fun response(sessionId: String, endSession: Boolean, message: String, reprompt: String? = null): ClovaExtensionResponse {
        val expectedNumber: Map<String, Any> = sessions[sessionId]?.expectedNumber?.let { number ->
            mapOf( MY_NUMBER to number)
        } ?: mapOf()

        val speech = SimpleSpeech(
                SpeechInfo(
                        type = SpeechInfoType.PlainText,
                        lang = SupportedLanguage.JA,
                        value = message)
        )

        return ResponseBuilder().apply {
            shouldEndSession = endSession
            outputSpeech = speech
            sessionAttributes = expectedNumber
            repromptMessage = reprompt
        }.build()
    }

    private fun retryResponse(sessionId: String): ClovaExtensionResponse =
            response(
                    sessionId = sessionId,
                    endSession = false,
                    message = CANT_RECOGNIZE,
                    reprompt = REPROMPT_MESSAGE
            )

    companion object {
        const val MY_NUMBER = "myNumber"
        const val SLOT_NUMBER = "Number"
        const val SLOT_NAME_NUMBER = "number"
        const val SLOT_NAME_ANSWER = "answer"
        const val GAME_STARTED = "はい、ゲームが始めます"
        const val REPROMPT_MESSAGE = "もう一度話しますか"
        const val CANT_RECOGNIZE = "話した数字がわかりませんので、もう一度話してください"
        const val BYE = "またね"
    }
}
