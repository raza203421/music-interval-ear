package com.musicinterval.ear.domain.model

class SessionStore {
    private val sessions = linkedMapOf<String, DrillSession>()
    private val results = linkedMapOf<String, DrillResult>()
    fun put(session: DrillSession) {
        sessions[session.id] = session
        while (sessions.size > 3) {
            val oldest = sessions.keys.first()
            sessions.remove(oldest)
            results.remove(oldest)
        }
    }
    fun session(id: String): DrillSession? = sessions[id]
    fun finish(result: DrillResult) { results[result.id] = result }
    fun result(id: String): DrillResult? = results[id]
}
