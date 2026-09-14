package com.orbitmessenger.backend.security

import at.favre.lib.crypto.bcrypt.BCrypt

object PasswordService {
    fun hash(password: String): String {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }

    fun verify(password: String, hash: String): Boolean {
        return BCrypt.verifyer().verify(password.toCharArray(), hash).verified
    }
}
