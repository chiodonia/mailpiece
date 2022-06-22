import { DateTime } from 'apikana/default-types'
import { Priority } from './priority'

export interface MailpieceEvent {
    /**
     * Key.
     */
    id: string

    /**
     * When the event occured.
     */
    timestamp: DateTime

    /**
     * Ingested.
     */
    ingested?: {
        /**
         * zip.
         */
        zip: string
        priority: Priority
    }

    /**
     * Delivered.
     */
    delivered?: {
        /**
         * zip.
         */
        zip: string
    }
}
