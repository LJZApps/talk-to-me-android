package de.ljz.talktome.data.api.core.annotations

/**
 * Marks an API route as an authenticated route
 *
 * @property optional Indicates
 * @constructor Create empty Authenticated
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Authenticated(val optional: Boolean = false)
