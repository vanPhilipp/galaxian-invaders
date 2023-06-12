package eu.imeon.invaders.core.collision

class ColliderException(message: String? = null, cause: Throwable? = null) :
    Exception(message, cause) {
    constructor(cause: Throwable) : this(null, cause)
}