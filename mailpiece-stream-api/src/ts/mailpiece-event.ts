import { DateTime } from 'apikana/default-types'
import { Product } from './product';

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
        product: Product
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
