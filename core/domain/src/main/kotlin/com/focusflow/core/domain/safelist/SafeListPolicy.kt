package com.focusflow.core.domain.safelist

/**
 * Enforces the Safe List safety invariant (AGENTS.md Hard Constraint):
 * "The safe list can never be blocked: dialer, emergency calling, system settings,
 * launcher, this app, and user-marked essentials."
 *
 * This policy is pure Kotlin and decoupled from Android framework APIs.
 */
object SafeListPolicy {

    /** FocusFlow app package prefix. */
    const val OWN_PACKAGE_NAME: String = "com.focusflow"

    /**
     * Well-known critical system package names that are unconditionally safe.
     */
    val CRITICAL_SYSTEM_PACKAGES: Set<String> = setOf(
        OWN_PACKAGE_NAME,
        "com.android.settings",
        "com.google.android.settings",
        "com.android.systemui",
        "com.android.phone",
        "com.android.server.telecom",
        "com.android.dialer",
        "com.google.android.dialer",
        "com.samsung.android.dialer",
        "com.android.emergency",
        "com.google.android.apps.safetyhub",
    )

    /**
     * Determines whether a given package is safe-listed and immune to blocking.
     *
     * @param packageName Package identifier to evaluate.
     * @param isDefaultLauncher True if this package is the user's active home launcher.
     * @param isDefaultDialer True if this package is the active telephony dialer.
     * @param userEssentials Set of package names user-designated as essential.
     * @return True if the package can NEVER be blocked.
     */
    fun isSafeListed(
        packageName: String,
        isDefaultLauncher: Boolean = false,
        isDefaultDialer: Boolean = false,
        userEssentials: Set<String> = emptySet(),
    ): Boolean {
        if (packageName.isBlank()) return true
        if (packageName.startsWith(OWN_PACKAGE_NAME)) return true
        if (CRITICAL_SYSTEM_PACKAGES.contains(packageName)) return true
        if (isDefaultLauncher) return true
        if (isDefaultDialer) return true
        if (userEssentials.contains(packageName)) return true
        return false
    }

    /**
     * Inverse of [isSafeListed]: returns true if and only if the package is eligible for blocking.
     */
    fun canBeBlocked(
        packageName: String,
        isDefaultLauncher: Boolean = false,
        isDefaultDialer: Boolean = false,
        userEssentials: Set<String> = emptySet(),
    ): Boolean = !isSafeListed(
        packageName = packageName,
        isDefaultLauncher = isDefaultLauncher,
        isDefaultDialer = isDefaultDialer,
        userEssentials = userEssentials,
    )
}
