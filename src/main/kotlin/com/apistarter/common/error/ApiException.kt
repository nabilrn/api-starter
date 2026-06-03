package com.apistarter.common.error

import org.springframework.http.HttpStatus

open class ApiException(
    val status: HttpStatus,
    override val message: String,
    val details: Any? = null,
) : RuntimeException(message)

class NotFoundException(
    resource: String,
) : ApiException(HttpStatus.NOT_FOUND, "$resource not found")

class ConflictException(
    message: String,
    details: Any? = null,
) : ApiException(HttpStatus.CONFLICT, message, details)

class BadRequestException(
    message: String,
    details: Any? = null,
) : ApiException(HttpStatus.BAD_REQUEST, message, details)
