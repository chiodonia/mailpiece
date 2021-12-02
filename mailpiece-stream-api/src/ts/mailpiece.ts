import { DateTime } from 'apikana/default-types'
import { Product } from './product';

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
     * The product.
     */
     product: Product

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
