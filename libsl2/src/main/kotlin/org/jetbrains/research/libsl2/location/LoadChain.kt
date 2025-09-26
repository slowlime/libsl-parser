package org.jetbrains.research.libsl2.location

/**
 * Tells what lead to loading a module.
 */
sealed interface LoadChain {
    val parent: LoadChain?

    /**
     * The module is loaded because due to an import declaration at the [location].
     */
    class Imported(val location: Location) : LoadChain {
        override val parent
            get() = location.loadChain
    }

    /**
     * The module is loaded due to an explicit load request for the [path].
     */
    class TopLevel(val path: String) : LoadChain {
        override val parent
            get() = null
    }
}
