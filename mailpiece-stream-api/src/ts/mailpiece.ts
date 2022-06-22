import { DateTime } from 'apikana/default-types'
import { Priority } from './priority'

export interface Mailpiece {
    /**
     * The id.
     */
    id: string

    /**
     * The state.
     */
    state: MailpieceState

    /**
     * The priority.
     */
    priority: Priority

    /**
     * The events.
     */
    events: Event[]
}

export enum MailpieceState {
    INGESTED,
    DELIVERED
}

export interface Event {
    timestamp: DateTime
    zip: string
    state: MailpieceState
}
