package com.example.attendxbackendv2.servicelayer.interfaces;

public interface SessionService {

    /**
     * Allows a student to attend a session identified by its session ID.
     *
     * @param sessionId The ID of the session the student is attending.
     * @param studentID The ID of the student attending the session.
     * @return true if the student successfully attends the session, false otherwise.
     */
    boolean attendToSession(Long sessionId, String studentID);

    /**
     * Initiates a session with the specified session ID.
     *
     * @param sessionId The ID of the session to be started.
     * @return true if the session is successfully started, false otherwise.
     */
    boolean startSession(Long sessionId);

}
