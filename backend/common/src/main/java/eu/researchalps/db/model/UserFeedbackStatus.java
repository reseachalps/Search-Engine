package eu.researchalps.db.model;


/**
 * Status of the user feedback.
 * <ul>
 * <li>SUBMITTED: just created by the user</li>
 * <li>PROCESSED: the feedback is marked as processed by the administrator of scanr</li>
 * </ul>
 */
public enum UserFeedbackStatus {
    SUBMITTED, PROCESSED
}
